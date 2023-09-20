package net.minecraft.block;

import java.util.Random;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

public class ChorusPlantBlock extends SixWayBlock {
   public ChorusPlantBlock(AbstractBlock.Properties p_i48428_1_) {
      super(0.3125F, p_i48428_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(UP, Boolean.valueOf(false)).setValue(DOWN, Boolean.valueOf(false)));
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.getStateForPlacement(pContext.getLevel(), pContext.getClickedPos());
   }

   public BlockState getStateForPlacement(IBlockReader pLevel, BlockPos pPos) {
      Block block = pLevel.getBlockState(pPos.below()).getBlock();
      Block block1 = pLevel.getBlockState(pPos.above()).getBlock();
      Block block2 = pLevel.getBlockState(pPos.north()).getBlock();
      Block block3 = pLevel.getBlockState(pPos.east()).getBlock();
      Block block4 = pLevel.getBlockState(pPos.south()).getBlock();
      Block block5 = pLevel.getBlockState(pPos.west()).getBlock();
      return this.defaultBlockState().setValue(DOWN, Boolean.valueOf(block == this || block == Blocks.CHORUS_FLOWER || block == Blocks.END_STONE)).setValue(UP, Boolean.valueOf(block1 == this || block1 == Blocks.CHORUS_FLOWER)).setValue(NORTH, Boolean.valueOf(block2 == this || block2 == Blocks.CHORUS_FLOWER)).setValue(EAST, Boolean.valueOf(block3 == this || block3 == Blocks.CHORUS_FLOWER)).setValue(SOUTH, Boolean.valueOf(block4 == this || block4 == Blocks.CHORUS_FLOWER)).setValue(WEST, Boolean.valueOf(block5 == this || block5 == Blocks.CHORUS_FLOWER));
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
         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      } else {
         boolean flag = pFacingState.getBlock() == this || pFacingState.is(Blocks.CHORUS_FLOWER) || pFacing == Direction.DOWN && pFacingState.is(Blocks.END_STONE);
         return pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), Boolean.valueOf(flag));
      }
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      boolean flag = !pLevel.getBlockState(pPos.above()).isAir() && !blockstate.isAir();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         Block block = pLevel.getBlockState(blockpos).getBlock();
         if (block == this) {
            if (flag) {
               return false;
            }

            Block block1 = pLevel.getBlockState(blockpos.below()).getBlock();
            if (block1 == this || block1 == Blocks.END_STONE) {
               return true;
            }
         }
      }

      Block block2 = blockstate.getBlock();
      return block2 == this || block2 == Blocks.END_STONE;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}