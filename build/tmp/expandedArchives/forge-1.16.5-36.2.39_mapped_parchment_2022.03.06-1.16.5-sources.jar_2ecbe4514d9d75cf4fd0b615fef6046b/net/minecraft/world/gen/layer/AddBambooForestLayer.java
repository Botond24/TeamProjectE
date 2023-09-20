package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.IC1Transformer;

public enum AddBambooForestLayer implements IC1Transformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pValue) {
      return pContext.nextRandom(10) == 0 && pValue == 21 ? 168 : pValue;
   }
}