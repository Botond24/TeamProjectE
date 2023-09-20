package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.IC0Transformer;

public enum StartRiverLayer implements IC0Transformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pValue) {
      return LayerUtil.isShallowOcean(pValue) ? pValue : pContext.nextRandom(299999) + 2;
   }
}