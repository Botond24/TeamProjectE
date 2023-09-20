package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.IC1Transformer;

public enum AddSnowLayer implements IC1Transformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pValue) {
      if (LayerUtil.isShallowOcean(pValue)) {
         return pValue;
      } else {
         int i = pContext.nextRandom(6);
         if (i == 0) {
            return 4;
         } else {
            return i == 1 ? 3 : 1;
         }
      }
   }
}