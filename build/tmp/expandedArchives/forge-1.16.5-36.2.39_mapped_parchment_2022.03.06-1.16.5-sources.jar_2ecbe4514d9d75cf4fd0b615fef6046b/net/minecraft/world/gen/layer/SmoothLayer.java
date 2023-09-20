package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.ICastleTransformer;

public enum SmoothLayer implements ICastleTransformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter) {
      boolean flag = pWest == pEast;
      boolean flag1 = pNorth == pSouth;
      if (flag == flag1) {
         if (flag) {
            return pContext.nextRandom(2) == 0 ? pEast : pNorth;
         } else {
            return pCenter;
         }
      } else {
         return flag ? pEast : pNorth;
      }
   }
}