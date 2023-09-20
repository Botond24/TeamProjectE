package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.ICastleTransformer;

public enum EdgeBiomeLayer implements ICastleTransformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter) {
      int[] aint = new int[1];
      if (!this.checkEdge(aint, pCenter) && !this.checkEdgeStrict(aint, pNorth, pWest, pSouth, pEast, pCenter, 38, 37) && !this.checkEdgeStrict(aint, pNorth, pWest, pSouth, pEast, pCenter, 39, 37) && !this.checkEdgeStrict(aint, pNorth, pWest, pSouth, pEast, pCenter, 32, 5)) {
         if (pCenter != 2 || pNorth != 12 && pWest != 12 && pEast != 12 && pSouth != 12) {
            if (pCenter == 6) {
               if (pNorth == 2 || pWest == 2 || pEast == 2 || pSouth == 2 || pNorth == 30 || pWest == 30 || pEast == 30 || pSouth == 30 || pNorth == 12 || pWest == 12 || pEast == 12 || pSouth == 12) {
                  return 1;
               }

               if (pNorth == 21 || pSouth == 21 || pWest == 21 || pEast == 21 || pNorth == 168 || pSouth == 168 || pWest == 168 || pEast == 168) {
                  return 23;
               }
            }

            return pCenter;
         } else {
            return 34;
         }
      } else {
         return aint[0];
      }
   }

   private boolean checkEdge(int[] p_242935_1_, int p_242935_2_) {
      if (!LayerUtil.isSame(p_242935_2_, 3)) {
         return false;
      } else {
         p_242935_1_[0] = p_242935_2_;
         return true;
      }
   }

   /**
    * Creates a border around a biome.
    */
   private boolean checkEdgeStrict(int[] p_151635_1_, int p_151635_2_, int p_151635_3_, int p_151635_4_, int p_151635_5_, int p_151635_6_, int p_151635_7_, int p_151635_8_) {
      if (p_151635_6_ != p_151635_7_) {
         return false;
      } else {
         if (LayerUtil.isSame(p_151635_2_, p_151635_7_) && LayerUtil.isSame(p_151635_3_, p_151635_7_) && LayerUtil.isSame(p_151635_5_, p_151635_7_) && LayerUtil.isSame(p_151635_4_, p_151635_7_)) {
            p_151635_1_[0] = p_151635_6_;
         } else {
            p_151635_1_[0] = p_151635_8_;
         }

         return true;
      }
   }
}