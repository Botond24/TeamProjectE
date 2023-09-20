package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.ICastleTransformer;

public enum RemoveTooMuchOceanLayer implements ICastleTransformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter) {
      return LayerUtil.isShallowOcean(pCenter) && LayerUtil.isShallowOcean(pNorth) && LayerUtil.isShallowOcean(pWest) && LayerUtil.isShallowOcean(pEast) && LayerUtil.isShallowOcean(pSouth) && pContext.nextRandom(2) == 0 ? 1 : pCenter;
   }
}