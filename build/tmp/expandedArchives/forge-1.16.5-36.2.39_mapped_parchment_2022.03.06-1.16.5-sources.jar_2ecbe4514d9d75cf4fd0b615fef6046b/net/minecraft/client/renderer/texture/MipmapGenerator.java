package net.minecraft.client.renderer.texture;

import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MipmapGenerator {
   private static final float[] POW22 = Util.make(new float[256], (p_229174_0_) -> {
      for(int i = 0; i < p_229174_0_.length; ++i) {
         p_229174_0_[i] = (float)Math.pow((double)((float)i / 255.0F), 2.2D);
      }

   });

   public static NativeImage[] generateMipLevels(NativeImage pImage, int pMipmapLevels) {
      NativeImage[] anativeimage = new NativeImage[pMipmapLevels + 1];
      anativeimage[0] = pImage;
      if (pMipmapLevels > 0) {
         boolean flag = false;

         label51:
         for(int i = 0; i < pImage.getWidth(); ++i) {
            for(int j = 0; j < pImage.getHeight(); ++j) {
               if (pImage.getPixelRGBA(i, j) >> 24 == 0) {
                  flag = true;
                  break label51;
               }
            }
         }

         for(int k1 = 1; k1 <= pMipmapLevels; ++k1) {
            NativeImage nativeimage1 = anativeimage[k1 - 1];
            NativeImage nativeimage = new NativeImage(nativeimage1.getWidth() >> 1, nativeimage1.getHeight() >> 1, false);
            int k = nativeimage.getWidth();
            int l = nativeimage.getHeight();

            for(int i1 = 0; i1 < k; ++i1) {
               for(int j1 = 0; j1 < l; ++j1) {
                  nativeimage.setPixelRGBA(i1, j1, alphaBlend(nativeimage1.getPixelRGBA(i1 * 2 + 0, j1 * 2 + 0), nativeimage1.getPixelRGBA(i1 * 2 + 1, j1 * 2 + 0), nativeimage1.getPixelRGBA(i1 * 2 + 0, j1 * 2 + 1), nativeimage1.getPixelRGBA(i1 * 2 + 1, j1 * 2 + 1), flag));
               }
            }

            anativeimage[k1] = nativeimage;
         }
      }

      return anativeimage;
   }

   private static int alphaBlend(int pCol1, int pCol2, int pCol3, int pCol4, boolean pTransparent) {
      if (pTransparent) {
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         float f3 = 0.0F;
         if (pCol1 >> 24 != 0) {
            f += getPow22(pCol1 >> 24);
            f1 += getPow22(pCol1 >> 16);
            f2 += getPow22(pCol1 >> 8);
            f3 += getPow22(pCol1 >> 0);
         }

         if (pCol2 >> 24 != 0) {
            f += getPow22(pCol2 >> 24);
            f1 += getPow22(pCol2 >> 16);
            f2 += getPow22(pCol2 >> 8);
            f3 += getPow22(pCol2 >> 0);
         }

         if (pCol3 >> 24 != 0) {
            f += getPow22(pCol3 >> 24);
            f1 += getPow22(pCol3 >> 16);
            f2 += getPow22(pCol3 >> 8);
            f3 += getPow22(pCol3 >> 0);
         }

         if (pCol4 >> 24 != 0) {
            f += getPow22(pCol4 >> 24);
            f1 += getPow22(pCol4 >> 16);
            f2 += getPow22(pCol4 >> 8);
            f3 += getPow22(pCol4 >> 0);
         }

         f = f / 4.0F;
         f1 = f1 / 4.0F;
         f2 = f2 / 4.0F;
         f3 = f3 / 4.0F;
         int i1 = (int)(Math.pow((double)f, 0.45454545454545453D) * 255.0D);
         int j1 = (int)(Math.pow((double)f1, 0.45454545454545453D) * 255.0D);
         int k1 = (int)(Math.pow((double)f2, 0.45454545454545453D) * 255.0D);
         int l1 = (int)(Math.pow((double)f3, 0.45454545454545453D) * 255.0D);
         if (i1 < 96) {
            i1 = 0;
         }

         return i1 << 24 | j1 << 16 | k1 << 8 | l1;
      } else {
         int i = gammaBlend(pCol1, pCol2, pCol3, pCol4, 24);
         int j = gammaBlend(pCol1, pCol2, pCol3, pCol4, 16);
         int k = gammaBlend(pCol1, pCol2, pCol3, pCol4, 8);
         int l = gammaBlend(pCol1, pCol2, pCol3, pCol4, 0);
         return i << 24 | j << 16 | k << 8 | l;
      }
   }

   private static int gammaBlend(int pCol1, int pCol2, int pCol3, int pCol4, int pBitOffset) {
      float f = getPow22(pCol1 >> pBitOffset);
      float f1 = getPow22(pCol2 >> pBitOffset);
      float f2 = getPow22(pCol3 >> pBitOffset);
      float f3 = getPow22(pCol4 >> pBitOffset);
      float f4 = (float)((double)((float)Math.pow((double)(f + f1 + f2 + f3) * 0.25D, 0.45454545454545453D)));
      return (int)((double)f4 * 255.0D);
   }

   private static float getPow22(int pVal) {
      return POW22[pVal & 255];
   }
}