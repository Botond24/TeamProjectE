package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CropsBlock extends BushBlock implements IGrowable {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
   private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

   public CropsBlock(AbstractBlock.Properties p_i48421_1_) {
      super(p_i48421_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE_BY_AGE[pState.getValue(this.getAgeProperty())];
   }

   protected boolean mayPlaceOn(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return pState.is(Blocks.FARMLAND);
   }

   public IntegerProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   protected int getAge(BlockState pState) {
      return pState.getValue(this.getAgeProperty());
   }

   public BlockState getStateForAge(int pAge) {
      return this.defaultBlockState().setValue(this.getAgeProperty(), Integer.valueOf(pAge));
   }

   public boolean isMaxAge(BlockState pState) {
      return pState.getValue(this.getAgeProperty()) >= this.getMaxAge();
   }

   /**
    * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
    * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return !this.isMaxAge(pState);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (!pLevel.isAreaLoaded(pPos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
      if (pLevel.getRawBrightness(pPos, 0) >= 9) {
         int i = this.getAge(pState);
         if (i < this.getMaxAge()) {
            float f = getGrowthSpeed(this, pLevel, pPos);
            if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt((int)(25.0F / f) + 1) == 0)) {
               pLevel.setBlock(pPos, this.getStateForAge(i + 1), 2);
               net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
            }
         }
      }

   }

   public void growCrops(World pLevel, BlockPos pPos, BlockState pState) {
      int i = this.getAge(pState) + this.getBonemealAgeIncrease(pLevel);
      int j = this.getMaxAge();
      if (i > j) {
         i = j;
      }

      pLevel.setBlock(pPos, this.getStateForAge(i), 2);
   }

   protected int getBonemealAgeIncrease(World pLevel) {
      return MathHelper.nextInt(pLevel.random, 2, 5);
   }

   protected static float getGrowthSpeed(Block pBlock, IBlockReader pLevel, BlockPos pPos) {
      float f = 1.0F;
      BlockPos blockpos = pPos.below();

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            float f1 = 0.0F;
            BlockState blockstate = pLevel.getBlockState(blockpos.offset(i, 0, j));
            if (blockstate.canSustainPlant(pLevel, blockpos.offset(i, 0, j), net.minecraft.util.Direction.UP, (net.minecraftforge.common.IPlantable) pBlock)) {
               f1 = 1.0F;
               if (blockstate.isFertile(pLevel, pPos.offset(i, 0, j))) {
                  f1 = 3.0F;
               }
            }

            if (i != 0 || j != 0) {
               f1 /= 4.0F;
            }

            f += f1;
         }
      }

      BlockPos blockpos1 = pPos.north();
      BlockPos blockpos2 = pPos.south();
      BlockPos blockpos3 = pPos.west();
      BlockPos blockpos4 = pPos.east();
      boolean flag = pBlock == pLevel.getBlockState(blockpos3).getBlock() || pBlock == pLevel.getBlockState(blockpos4).getBlock();
      boolean flag1 = pBlock == pLevel.getBlockState(blockpos1).getBlock() || pBlock == pLevel.getBlockState(blockpos2).getBlock();
      if (flag && flag1) {
         f /= 2.0F;
      } else {
         boolean flag2 = pBlock == pLevel.getBlockState(blockpos3.north()).getBlock() || pBlock == pLevel.getBlockState(blockpos4.north()).getBlock() || pBlock == pLevel.getBlockState(blockpos4.south()).getBlock() || pBlock == pLevel.getBlockState(blockpos3.south()).getBlock();
         if (flag2) {
            f /= 2.0F;
         }
      }

      return f;
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      return (pLevel.getRawBrightness(pPos, 0) >= 8 || pLevel.canSeeSky(pPos)) && super.canSurvive(pState, pLevel, pPos);
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (pEntity instanceof RavagerEntity && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(pLevel, pEntity)) {
         pLevel.destroyBlock(pPos, true, pEntity);
      }

      super.entityInside(pState, pLevel, pPos, pEntity);
   }

   protected IItemProvider getBaseSeedId() {
      return Items.WHEAT_SEEDS;
   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(this.getBaseSeedId());
   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return !this.isMaxAge(pState);
   }

   public boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      this.growCrops(pLevel, pPos, pState);
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }
}
