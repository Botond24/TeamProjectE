package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.IC0Transformer;
import net.minecraft.world.gen.layer.traits.ICastleTransformer;

public class EdgeLayer {
   public static enum CoolWarm implements ICastleTransformer {
      INSTANCE;

      public int apply(INoiseRandom pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter) {
         return pCenter != 1 || pNorth != 3 && pWest != 3 && pEast != 3 && pSouth != 3 && pNorth != 4 && pWest != 4 && pEast != 4 && pSouth != 4 ? pCenter : 2;
      }
   }

   public static enum HeatIce implements ICastleTransformer {
      INSTANCE;

      public int apply(INoiseRandom pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter) {
         return pCenter != 4 || pNorth != 1 && pWest != 1 && pEast != 1 && pSouth != 1 && pNorth != 2 && pWest != 2 && pEast != 2 && pSouth != 2 ? pCenter : 3;
      }
   }

   public static enum Special implements IC0Transformer {
      INSTANCE;

      public int apply(INoiseRandom pContext, int pValue) {
         if (!LayerUtil.isShallowOcean(pValue) && pContext.nextRandom(13) == 0) {
            pValue |= 1 + pContext.nextRandom(15) << 8 & 3840;
         }

         return pValue;
      }
   }
}