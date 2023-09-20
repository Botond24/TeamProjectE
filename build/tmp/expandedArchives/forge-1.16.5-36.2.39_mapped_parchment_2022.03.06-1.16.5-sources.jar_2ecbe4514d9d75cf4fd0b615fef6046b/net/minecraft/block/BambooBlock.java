package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.SwordItem;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BambooLeaves;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BambooBlock extends Block implements IGrowable {
   protected static final VoxelShape SMALL_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
   protected static final VoxelShape LARGE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
   protected static final VoxelShape COLLISION_SHAPE = Block.box(6.5D, 0.0D, 6.5D, 9.5D, 16.0D, 9.5D);
   public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
   public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
   public static final IntegerProperty STAGE = BlockStateProperties.STAGE;

   public BambooBlock(AbstractBlock.Properties p_i49998_1_) {
      super(p_i49998_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(LEAVES, BambooLeaves.NONE).setValue(STAGE, Integer.valueOf(0)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE, LEAVES, STAGE);
   }

   /**
    * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
    */
   public AbstractBlock.OffsetType getOffsetType() {
      return AbstractBlock.OffsetType.XZ;
   }

   public boolean propagatesSkylightDown(BlockState pState, IBlockReader pReader, BlockPos pPos) {
      return true;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      VoxelShape voxelshape = pState.getValue(LEAVES) == BambooLeaves.LARGE ? LARGE_SHAPE : SMALL_SHAPE;
      Vector3d vector3d = pState.getOffset(pLevel, pPos);
      return voxelshape.move(vector3d.x, vector3d.y, vector3d.z);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }

   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      Vector3d vector3d = pState.getOffset(pLevel, pPos);
      return COLLISION_SHAPE.move(vector3d.x, vector3d.y, vector3d.z);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      if (!fluidstate.isEmpty()) {
         return null;
      } else {
         BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos().below());
         if (blockstate.is(BlockTags.BAMBOO_PLANTABLE_ON)) {
            if (blockstate.is(Blocks.BAMBOO_SAPLING)) {
               return this.defaultBlockState().setValue(AGE, Integer.valueOf(0));
            } else if (blockstate.is(Blocks.BAMBOO)) {
               int i = blockstate.getValue(AGE) > 0 ? 1 : 0;
               return this.defaultBlockState().setValue(AGE, Integer.valueOf(i));
            } else {
               BlockState blockstate1 = pContext.getLevel().getBlockState(pContext.getClickedPos().above());
               return !blockstate1.is(Blocks.BAMBOO) && !blockstate1.is(Blocks.BAMBOO_SAPLING) ? Blocks.BAMBOO_SAPLING.defaultBlockState() : this.defaultBlockState().setValue(AGE, blockstate1.getValue(AGE));
            }
         } else {
            return null;
         }
      }
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   /**
    * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
    * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getValue(STAGE) == 0;
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (pState.getValue(STAGE) == 0) {
         if (pLevel.isEmptyBlock(pPos.above()) && pLevel.getRawBrightness(pPos.above(), 0) >= 9) {
            int i = this.getHeightBelowUpToMax(pLevel, pPos) + 1;
            if (i < 16 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt(3) == 0)) {
               this.growBamboo(pState, pLevel, pPos, pRandom, i);
               net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
            }
         }

      }
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (!pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
      }

      if (pFacing == Direction.UP && pFacingState.is(Blocks.BAMBOO) && pFacingState.getValue(AGE) > pState.getValue(AGE)) {
         pLevel.setBlock(pCurrentPos, pState.cycle(AGE), 2);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      int i = this.getHeightAboveUpToMax(pLevel, pPos);
      int j = this.getHeightBelowUpToMax(pLevel, pPos);
      return i + j + 1 < 16 && pLevel.getBlockState(pPos.above(i)).getValue(STAGE) != 1;
   }

   public boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      int i = this.getHeightAboveUpToMax(pLevel, pPos);
      int j = this.getHeightBelowUpToMax(pLevel, pPos);
      int k = i + j + 1;
      int l = 1 + pRand.nextInt(2);

      for(int i1 = 0; i1 < l; ++i1) {
         BlockPos blockpos = pPos.above(i);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (k >= 16 || blockstate.getValue(STAGE) == 1 || !pLevel.isEmptyBlock(blockpos.above())) {
            return;
         }

         this.growBamboo(blockstate, pLevel, blockpos, pRand, k);
         ++i;
         ++k;
      }

   }

   /**
    * Get the hardness of this Block relative to the ability of the given player
    * @deprecated call via {@link IBlockState#getPlayerRelativeBlockHardness(EntityPlayer,World,BlockPos)} whenever
    * possible. Implementing/overriding is fine.
    */
   public float getDestroyProgress(BlockState pState, PlayerEntity pPlayer, IBlockReader pLevel, BlockPos pPos) {
      return pPlayer.getMainHandItem().getItem() instanceof SwordItem ? 1.0F : super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
   }

   protected void growBamboo(BlockState pState, World pLevel, BlockPos pPos, Random pRandom, int pMaxTotalSize) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      BlockPos blockpos = pPos.below(2);
      BlockState blockstate1 = pLevel.getBlockState(blockpos);
      BambooLeaves bambooleaves = BambooLeaves.NONE;
      if (pMaxTotalSize >= 1) {
         if (blockstate.is(Blocks.BAMBOO) && blockstate.getValue(LEAVES) != BambooLeaves.NONE) {
            if (blockstate.is(Blocks.BAMBOO) && blockstate.getValue(LEAVES) != BambooLeaves.NONE) {
               bambooleaves = BambooLeaves.LARGE;
               if (blockstate1.is(Blocks.BAMBOO)) {
                  pLevel.setBlock(pPos.below(), blockstate.setValue(LEAVES, BambooLeaves.SMALL), 3);
                  pLevel.setBlock(blockpos, blockstate1.setValue(LEAVES, BambooLeaves.NONE), 3);
               }
            }
         } else {
            bambooleaves = BambooLeaves.SMALL;
         }
      }

      int i = pState.getValue(AGE) != 1 && !blockstate1.is(Blocks.BAMBOO) ? 0 : 1;
      int j = (pMaxTotalSize < 11 || !(pRandom.nextFloat() < 0.25F)) && pMaxTotalSize != 15 ? 0 : 1;
      pLevel.setBlock(pPos.above(), this.defaultBlockState().setValue(AGE, Integer.valueOf(i)).setValue(LEAVES, bambooleaves).setValue(STAGE, Integer.valueOf(j)), 3);
   }

   /**
    * @return the number of continuous bamboo blocks above the position passed in, up to 16.
    */
   protected int getHeightAboveUpToMax(IBlockReader pLevel, BlockPos pPos) {
      int i;
      for(i = 0; i < 16 && pLevel.getBlockState(pPos.above(i + 1)).is(Blocks.BAMBOO); ++i) {
      }

      return i;
   }

   /**
    * @return the number of continuous bamboo blocks below the position passed in, up to 16.
    */
   protected int getHeightBelowUpToMax(IBlockReader pLevel, BlockPos pPos) {
      int i;
      for(i = 0; i < 16 && pLevel.getBlockState(pPos.below(i + 1)).is(Blocks.BAMBOO); ++i) {
      }

      return i;
   }
}
