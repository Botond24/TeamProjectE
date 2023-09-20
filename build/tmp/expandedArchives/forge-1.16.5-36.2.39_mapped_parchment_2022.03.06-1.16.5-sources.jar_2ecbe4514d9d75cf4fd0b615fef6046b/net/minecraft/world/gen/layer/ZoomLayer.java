package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.area.IArea;
import net.minecraft.world.gen.layer.traits.IAreaTransformer1;

public enum ZoomLayer implements IAreaTransformer1 {
   NORMAL,
   FUZZY {
      protected int modeOrRandom(IExtendedNoiseRandom<?> pContext, int pFirst, int pSecond, int pThird, int pFourth) {
         return pContext.random(pFirst, pSecond, pThird, pFourth);
      }
   };

   private ZoomLayer() {
   }

   public int getParentX(int pX) {
      return pX >> 1;
   }

   public int getParentY(int pZ) {
      return pZ >> 1;
   }

   public int applyPixel(IExtendedNoiseRandom<?> pContext, IArea pArea, int pX, int pZ) {
      int i = pArea.get(this.getParentX(pX), this.getParentY(pZ));
      pContext.initRandom((long)(pX >> 1 << 1), (long)(pZ >> 1 << 1));
      int j = pX & 1;
      int k = pZ & 1;
      if (j == 0 && k == 0) {
         return i;
      } else {
         int l = pArea.get(this.getParentX(pX), this.getParentY(pZ + 1));
         int i1 = pContext.random(i, l);
         if (j == 0 && k == 1) {
            return i1;
         } else {
            int j1 = pArea.get(this.getParentX(pX + 1), this.getParentY(pZ));
            int k1 = pContext.random(i, j1);
            if (j == 1 && k == 0) {
               return k1;
            } else {
               int l1 = pArea.get(this.getParentX(pX + 1), this.getParentY(pZ + 1));
               return this.modeOrRandom(pContext, i, j1, l, l1);
            }
         }
      }
   }

   protected int modeOrRandom(IExtendedNoiseRandom<?> pContext, int pFirst, int pSecond, int pThird, int pFourth) {
      if (pSecond == pThird && pThird == pFourth) {
         return pSecond;
      } else if (pFirst == pSecond && pFirst == pThird) {
         return pFirst;
      } else if (pFirst == pSecond && pFirst == pFourth) {
         return pFirst;
      } else if (pFirst == pThird && pFirst == pFourth) {
         return pFirst;
      } else if (pFirst == pSecond && pThird != pFourth) {
         return pFirst;
      } else if (pFirst == pThird && pSecond != pFourth) {
         return pFirst;
      } else if (pFirst == pFourth && pSecond != pThird) {
         return pFirst;
      } else if (pSecond == pThird && pFirst != pFourth) {
         return pSecond;
      } else if (pSecond == pFourth && pFirst != pThird) {
         return pSecond;
      } else {
         return pThird == pFourth && pFirst != pSecond ? pThird : pContext.random(pFirst, pSecond, pThird, pFourth);
      }
   }
}