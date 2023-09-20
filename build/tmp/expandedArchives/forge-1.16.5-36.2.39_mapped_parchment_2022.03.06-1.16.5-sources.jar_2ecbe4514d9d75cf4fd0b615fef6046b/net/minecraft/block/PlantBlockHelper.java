package net.minecraft.block;

import java.util.Random;

public class PlantBlockHelper {
   public static boolean isValidGrowthState(BlockState pState) {
      return pState.isAir();
   }

   public static int getBlocksToGrowWhenBonemealed(Random pRandom) {
      double d0 = 1.0D;

      int i;
      for(i = 0; pRandom.nextDouble() < d0; ++i) {
         d0 *= 0.826D;
      }

      return i;
   }
}