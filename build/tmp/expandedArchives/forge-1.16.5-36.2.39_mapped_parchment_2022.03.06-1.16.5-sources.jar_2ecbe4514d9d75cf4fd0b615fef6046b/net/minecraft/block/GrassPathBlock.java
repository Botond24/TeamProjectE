package net.minecraft.block;

import java.util.Random;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

public class GrassPathBlock extends Block {
   protected static final VoxelShape SHAPE = FarmlandBlock.SHAPE;

   public GrassPathBlock(AbstractBlock.Properties p_i48386_1_) {
      super(p_i48386_1_);
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return !this.defaultBlockState().canSurvive(pContext.getLevel(), pContext.getClickedPos()) ? Block.pushEntitiesUp(this.defaultBlockState(), Blocks.DIRT.defaultBlockState(), pContext.getLevel(), pContext.getClickedPos()) : super.getStateForPlacement(pContext);
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == Direction.UP && !pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      FarmlandBlock.turnToDirt(pState, pLevel, pPos);
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.above());
      return !blockstate.getMaterial().isSolid() || blockstate.getBlock() instanceof FenceGateBlock;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}