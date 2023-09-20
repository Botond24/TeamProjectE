package net.minecraft.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.level.ColorResolver;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeColors {
   public static final ColorResolver GRASS_COLOR_RESOLVER = Biome::getGrassColor;
   public static final ColorResolver FOLIAGE_COLOR_RESOLVER = (p_228362_0_, p_228362_1_, p_228362_3_) -> {
      return p_228362_0_.getFoliageColor();
   };
   public static final ColorResolver WATER_COLOR_RESOLVER = (p_228360_0_, p_228360_1_, p_228360_3_) -> {
      return p_228360_0_.getWaterColor();
   };

   private static int getAverageColor(IBlockDisplayReader pLevel, BlockPos pBlockPos, ColorResolver pColorResolver) {
      return pLevel.getBlockTint(pBlockPos, pColorResolver);
   }

   public static int getAverageGrassColor(IBlockDisplayReader pLevel, BlockPos pBlockPos) {
      return getAverageColor(pLevel, pBlockPos, GRASS_COLOR_RESOLVER);
   }

   public static int getAverageFoliageColor(IBlockDisplayReader pLevel, BlockPos pBlockPos) {
      return getAverageColor(pLevel, pBlockPos, FOLIAGE_COLOR_RESOLVER);
   }

   public static int getAverageWaterColor(IBlockDisplayReader pLevel, BlockPos pBlockPos) {
      return getAverageColor(pLevel, pBlockPos, WATER_COLOR_RESOLVER);
   }
}