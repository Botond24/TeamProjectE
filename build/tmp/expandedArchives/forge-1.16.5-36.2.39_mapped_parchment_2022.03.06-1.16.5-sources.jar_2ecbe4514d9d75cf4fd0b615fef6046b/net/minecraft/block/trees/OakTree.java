package net.minecraft.block.trees;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;

public class OakTree extends Tree {
   /**
    * Get a {@link net.minecraft.world.gen.feature.ConfiguredFeature} of tree
    */
   @Nullable
   protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random pRandom, boolean pLargeHive) {
      if (pRandom.nextInt(10) == 0) {
         return pLargeHive ? Features.FANCY_OAK_BEES_005 : Features.FANCY_OAK;
      } else {
         return pLargeHive ? Features.OAK_BEES_005 : Features.OAK;
      }
   }
}