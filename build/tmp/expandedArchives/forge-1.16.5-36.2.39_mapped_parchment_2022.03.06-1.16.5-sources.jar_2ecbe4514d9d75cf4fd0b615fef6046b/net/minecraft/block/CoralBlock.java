package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;

public class CoralBlock extends Block {
   private final Block deadBlock;

   public CoralBlock(Block p_i48893_1_, AbstractBlock.Properties p_i48893_2_) {
      super(p_i48893_2_);
      this.deadBlock = p_i48893_1_;
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (!this.scanForWater(pLevel, pPos)) {
         pLevel.setBlock(pPos, this.deadBlock.defaultBlockState(), 2);
      }

   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (!this.scanForWater(pLevel, pCurrentPos)) {
         pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 60 + pLevel.getRandom().nextInt(40));
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   protected boolean scanForWater(IBlockReader pLevel, BlockPos pPos) {
      for(Direction direction : Direction.values()) {
         FluidState fluidstate = pLevel.getFluidState(pPos.relative(direction));
         if (fluidstate.is(FluidTags.WATER)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      if (!this.scanForWater(pContext.getLevel(), pContext.getClickedPos())) {
         pContext.getLevel().getBlockTicks().scheduleTick(pContext.getClickedPos(), this, 60 + pContext.getLevel().getRandom().nextInt(40));
      }

      return this.defaultBlockState();
   }
}