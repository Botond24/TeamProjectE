package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.IBishopTransformer;

public enum AddIslandLayer implements IBishopTransformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pX, int pSouthEast, int p_202792_4_, int p_202792_5_, int p_202792_6_) {
      if (!LayerUtil.isShallowOcean(p_202792_6_) || LayerUtil.isShallowOcean(p_202792_5_) && LayerUtil.isShallowOcean(p_202792_4_) && LayerUtil.isShallowOcean(pX) && LayerUtil.isShallowOcean(pSouthEast)) {
         if (!LayerUtil.isShallowOcean(p_202792_6_) && (LayerUtil.isShallowOcean(p_202792_5_) || LayerUtil.isShallowOcean(pX) || LayerUtil.isShallowOcean(p_202792_4_) || LayerUtil.isShallowOcean(pSouthEast)) && pContext.nextRandom(5) == 0) {
            if (LayerUtil.isShallowOcean(p_202792_5_)) {
               return p_202792_6_ == 4 ? 4 : p_202792_5_;
            }

            if (LayerUtil.isShallowOcean(pX)) {
               return p_202792_6_ == 4 ? 4 : pX;
            }

            if (LayerUtil.isShallowOcean(p_202792_4_)) {
               return p_202792_6_ == 4 ? 4 : p_202792_4_;
            }

            if (LayerUtil.isShallowOcean(pSouthEast)) {
               return p_202792_6_ == 4 ? 4 : pSouthEast;
            }
         }

         return p_202792_6_;
      } else {
         int i = 1;
         int j = 1;
         if (!LayerUtil.isShallowOcean(p_202792_5_) && pContext.nextRandom(i++) == 0) {
            j = p_202792_5_;
         }

         if (!LayerUtil.isShallowOcean(p_202792_4_) && pContext.nextRandom(i++) == 0) {
            j = p_202792_4_;
         }

         if (!LayerUtil.isShallowOcean(pX) && pContext.nextRandom(i++) == 0) {
            j = pX;
         }

         if (!LayerUtil.isShallowOcean(pSouthEast) && pContext.nextRandom(i++) == 0) {
            j = pSouthEast;
         }

         if (pContext.nextRandom(3) == 0) {
            return j;
         } else {
            return j == 4 ? 4 : p_202792_6_;
         }
      }
   }
}