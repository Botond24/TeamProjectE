package net.minecraft.block;

import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConcretePowderBlock extends FallingBlock {
   private final BlockState concrete;

   public ConcretePowderBlock(Block pConcrete, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.concrete = pConcrete.defaultBlockState();
   }

   public void onLand(World p_176502_1_, BlockPos p_176502_2_, BlockState p_176502_3_, BlockState p_176502_4_, FallingBlockEntity p_176502_5_) {
      if (shouldSolidify(p_176502_1_, p_176502_2_, p_176502_4_)) {
         p_176502_1_.setBlock(p_176502_2_, this.concrete, 3);
      }

   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      IBlockReader iblockreader = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = iblockreader.getBlockState(blockpos);
      return shouldSolidify(iblockreader, blockpos, blockstate) ? this.concrete : super.getStateForPlacement(pContext);
   }

   private static boolean shouldSolidify(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return canSolidify(pState) || touchesLiquid(pLevel, pPos);
   }

   private static boolean touchesLiquid(IBlockReader pLevel, BlockPos pPos) {
      boolean flag = false;
      BlockPos.Mutable blockpos$mutable = pPos.mutable();

      for(Direction direction : Direction.values()) {
         BlockState blockstate = pLevel.getBlockState(blockpos$mutable);
         if (direction != Direction.DOWN || canSolidify(blockstate)) {
            blockpos$mutable.setWithOffset(pPos, direction);
            blockstate = pLevel.getBlockState(blockpos$mutable);
            if (canSolidify(blockstate) && !blockstate.isFaceSturdy(pLevel, pPos, direction.getOpposite())) {
               flag = true;
               break;
            }
         }
      }

      return flag;
   }

   private static boolean canSolidify(BlockState pState) {
      return pState.getFluidState().is(FluidTags.WATER);
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return touchesLiquid(pLevel, pCurrentPos) ? this.concrete : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   @OnlyIn(Dist.CLIENT)
   public int getDustColor(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return pState.getMapColor(pLevel, pPos).col;
   }
}