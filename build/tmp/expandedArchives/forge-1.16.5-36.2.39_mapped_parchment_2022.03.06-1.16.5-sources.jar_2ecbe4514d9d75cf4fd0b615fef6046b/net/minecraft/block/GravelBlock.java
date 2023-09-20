package net.minecraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GravelBlock extends FallingBlock {
   public GravelBlock(AbstractBlock.Properties p_i48384_1_) {
      super(p_i48384_1_);
   }

   @OnlyIn(Dist.CLIENT)
   public int getDustColor(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return -8356741;
   }
}