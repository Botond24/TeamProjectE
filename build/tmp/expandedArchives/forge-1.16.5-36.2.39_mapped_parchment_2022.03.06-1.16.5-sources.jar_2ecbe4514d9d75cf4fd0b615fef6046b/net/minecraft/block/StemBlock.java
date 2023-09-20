package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class StemBlock extends BushBlock implements IGrowable {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
   protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 4.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 8.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 12.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)};
   private final StemGrownBlock fruit;

   public StemBlock(StemGrownBlock p_i48318_1_, AbstractBlock.Properties p_i48318_2_) {
      super(p_i48318_2_);
      this.fruit = p_i48318_1_;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE_BY_AGE[pState.getValue(AGE)];
   }

   protected boolean mayPlaceOn(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return pState.is(Blocks.FARMLAND);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (!pLevel.isAreaLoaded(pPos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
      if (pLevel.getRawBrightness(pPos, 0) >= 9) {
         float f = CropsBlock.getGrowthSpeed(this, pLevel, pPos);
         if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt((int)(25.0F / f) + 1) == 0)) {
            int i = pState.getValue(AGE);
            if (i < 7) {
               pLevel.setBlock(pPos, pState.setValue(AGE, Integer.valueOf(i + 1)), 2);
            } else {
               Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
               BlockPos blockpos = pPos.relative(direction);
               BlockState blockstate = pLevel.getBlockState(blockpos.below());
               Block block = blockstate.getBlock();
               if (pLevel.isEmptyBlock(blockpos) && (blockstate.canSustainPlant(pLevel, blockpos.below(), Direction.UP, this) || block == Blocks.FARMLAND || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL || block == Blocks.GRASS_BLOCK)) {
                  pLevel.setBlockAndUpdate(blockpos, this.fruit.defaultBlockState());
                  pLevel.setBlockAndUpdate(pPos, this.fruit.getAttachedStem().defaultBlockState().setValue(HorizontalBlock.FACING, direction));
               }
            }
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
         }

      }
   }

   @Nullable
   protected Item getSeedItem() {
      if (this.fruit == Blocks.PUMPKIN) {
         return Items.PUMPKIN_SEEDS;
      } else {
         return this.fruit == Blocks.MELON ? Items.MELON_SEEDS : null;
      }
   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      Item item = this.getSeedItem();
      return item == null ? ItemStack.EMPTY : new ItemStack(item);
   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return pState.getValue(AGE) != 7;
   }

   public boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      int i = Math.min(7, pState.getValue(AGE) + MathHelper.nextInt(pLevel.random, 2, 5));
      BlockState blockstate = pState.setValue(AGE, Integer.valueOf(i));
      pLevel.setBlock(pPos, blockstate, 2);
      if (i == 7) {
         blockstate.randomTick(pLevel, pPos, pLevel.random);
      }

   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   public StemGrownBlock getFruit() {
      return this.fruit;
   }

   //FORGE START
   @Override
   public net.minecraftforge.common.PlantType getPlantType(IBlockReader world, BlockPos pos) {
      return net.minecraftforge.common.PlantType.CROP;
   }
}
