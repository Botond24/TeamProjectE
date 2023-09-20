package net.minecraft.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ColorHelper {
   public static class PackedColor {
      @OnlyIn(Dist.CLIENT)
      public static int alpha(int pPackedColor) {
         return pPackedColor >>> 24;
      }

      public static int red(int pPackedColor) {
         return pPackedColor >> 16 & 255;
      }

      public static int green(int pPackedColor) {
         return pPackedColor >> 8 & 255;
      }

      public static int blue(int pPackedColor) {
         return pPackedColor & 255;
      }

      @OnlyIn(Dist.CLIENT)
      public static int color(int pAlpha, int pRed, int pGreen, int pBlue) {
         return pAlpha << 24 | pRed << 16 | pGreen << 8 | pBlue;
      }

      @OnlyIn(Dist.CLIENT)
      public static int multiply(int pPackedColourOne, int pPackedColorTwo) {
         return color(alpha(pPackedColourOne) * alpha(pPackedColorTwo) / 255, red(pPackedColourOne) * red(pPackedColorTwo) / 255, green(pPackedColourOne) * green(pPackedColorTwo) / 255, blue(pPackedColourOne) * blue(pPackedColorTwo) / 255);
      }
   }
}