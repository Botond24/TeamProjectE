package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.IBishopTransformer;

public enum AddMushroomIslandLayer implements IBishopTransformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pX, int pSouthEast, int p_202792_4_, int p_202792_5_, int p_202792_6_) {
      return LayerUtil.isShallowOcean(p_202792_6_) && LayerUtil.isShallowOcean(p_202792_5_) && LayerUtil.isShallowOcean(pX) && LayerUtil.isShallowOcean(p_202792_4_) && LayerUtil.isShallowOcean(pSouthEast) && pContext.nextRandom(100) == 0 ? 14 : p_202792_6_;
   }
}