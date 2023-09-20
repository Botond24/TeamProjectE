package net.minecraft.block;

import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BreakableBlock extends Block {
   public BreakableBlock(AbstractBlock.Properties p_i48382_1_) {
      super(p_i48382_1_);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
      return pAdjacentBlockState.is(this) ? true : super.skipRendering(pState, pAdjacentBlockState, pSide);
   }
}