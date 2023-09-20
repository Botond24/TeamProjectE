package net.minecraft.world;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IBlockDisplayReader extends IBlockReader {
   @OnlyIn(Dist.CLIENT)
   float getShade(Direction pDirection, boolean pIsShade);

   WorldLightManager getLightEngine();

   @OnlyIn(Dist.CLIENT)
   int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver);

   default int getBrightness(LightType pLightType, BlockPos pBlockPos) {
      return this.getLightEngine().getLayerListener(pLightType).getLightValue(pBlockPos);
   }

   default int getRawBrightness(BlockPos pBlockPos, int pAmount) {
      return this.getLightEngine().getRawBrightness(pBlockPos, pAmount);
   }

   default boolean canSeeSky(BlockPos pBlockPos) {
      return this.getBrightness(LightType.SKY, pBlockPos) >= this.getMaxLightLevel();
   }
}