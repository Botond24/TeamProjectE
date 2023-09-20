package net.minecraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SandBlock extends FallingBlock {
   private final int dustColor;

   public SandBlock(int pDustColor, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.dustColor = pDustColor;
   }

   @OnlyIn(Dist.CLIENT)
   public int getDustColor(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return this.dustColor;
   }
}