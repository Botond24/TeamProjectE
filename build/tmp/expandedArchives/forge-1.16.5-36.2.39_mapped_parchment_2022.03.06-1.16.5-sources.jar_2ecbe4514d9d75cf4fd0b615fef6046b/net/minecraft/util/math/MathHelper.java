package net.minecraft.util.math;

import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.math.NumberUtils;

public class MathHelper {
   public static final float SQRT_OF_TWO = sqrt(2.0F);
   private static final float[] SIN = Util.make(new float[65536], (p_203445_0_) -> {
      for(int i = 0; i < p_203445_0_.length; ++i) {
         p_203445_0_[i] = (float)Math.sin((double)i * Math.PI * 2.0D / 65536.0D);
      }

   });
   private static final Random RANDOM = new Random();
   /**
    * Though it looks like an array, this is really more like a mapping. Key (index of this array) is the upper 5 bits
    * of the result of multiplying a 32-bit unsigned integer by the B(2, 5) De Bruijn sequence 0x077CB531. Value (value
    * stored in the array) is the unique index (from the right) of the leftmo
    */
   private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
   private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
   private static final double[] ASIN_TAB = new double[257];
   private static final double[] COS_TAB = new double[257];

   /**
    * sin looked up in a table
    */
   public static float sin(float pValue) {
      return SIN[(int)(pValue * 10430.378F) & '\uffff'];
   }

   /**
    * cos looked up in the sin table with the appropriate offset
    */
   public static float cos(float pValue) {
      return SIN[(int)(pValue * 10430.378F + 16384.0F) & '\uffff'];
   }

   public static float sqrt(float pValue) {
      return (float)Math.sqrt((double)pValue);
   }

   public static float sqrt(double p_76133_0_) {
      return (float)Math.sqrt(p_76133_0_);
   }

   /**
    * Returns the greatest integer less than or equal to the float argument
    */
   public static int floor(float pValue) {
      int i = (int)pValue;
      return pValue < (float)i ? i - 1 : i;
   }

   /**
    * returns par0 cast as an int, and no greater than Integer.MAX_VALUE-1024
    */
   @OnlyIn(Dist.CLIENT)
   public static int fastFloor(double pValue) {
      return (int)(pValue + 1024.0D) - 1024;
   }

   /**
    * Returns the greatest integer less than or equal to the double argument
    */
   public static int floor(double pValue) {
      int i = (int)pValue;
      return pValue < (double)i ? i - 1 : i;
   }

   /**
    * Long version of floor()
    */
   public static long lfloor(double pValue) {
      long i = (long)pValue;
      return pValue < (double)i ? i - 1L : i;
   }

   public static float abs(float pValue) {
      return Math.abs(pValue);
   }

   /**
    * Returns the unsigned value of an int.
    */
   public static int abs(int pValue) {
      return Math.abs(pValue);
   }

   public static int ceil(float pValue) {
      int i = (int)pValue;
      return pValue > (float)i ? i + 1 : i;
   }

   public static int ceil(double pValue) {
      int i = (int)pValue;
      return pValue > (double)i ? i + 1 : i;
   }

   /**
    * Returns the given value if between the lower and the upper bound. If the value is less than the lower bound,
    * returns the lower bound. If the value is greater than the upper bound, returns the upper bound.
    * @param pValue The value that is clamped.
    * @param pMin The lower bound for the clamp.
    * @param pMax The upper bound for the clamp.
    */
   public static int clamp(int pValue, int pMin, int pMax) {
      if (pValue < pMin) {
         return pMin;
      } else {
         return pValue > pMax ? pMax : pValue;
      }
   }

   /**
    * Returns the given value if between the lower and the upper bound. If the value is less than the lower bound,
    * returns the lower bound. If the value is greater than the upper bound, returns the upper bound.
    * @param pValue The value that is clamped.
    * @param pMin The lower bound for the clamp.
    * @param pMax The upper bound for the clamp.
    */
   @OnlyIn(Dist.CLIENT)
   public static long clamp(long pValue, long pMin, long pMax) {
      if (pValue < pMin) {
         return pMin;
      } else {
         return pValue > pMax ? pMax : pValue;
      }
   }

   /**
    * Returns the given value if between the lower and the upper bound. If the value is less than the lower bound,
    * returns the lower bound. If the value is greater than the upper bound, returns the upper bound.
    * @param pValue The value that is clamped.
    * @param pMin The lower bound for the clamp.
    * @param pMax The upper bound for the clamp.
    */
   public static float clamp(float pValue, float pMin, float pMax) {
      if (pValue < pMin) {
         return pMin;
      } else {
         return pValue > pMax ? pMax : pValue;
      }
   }

   /**
    * Returns the given value if between the lower and the upper bound. If the value is less than the lower bound,
    * returns the lower bound. If the value is greater than the upper bound, returns the upper bound.
    * @param pValue The value that is clamped.
    * @param pMin The lower bound for the clamp.
    * @param pMax The upper bound for the clamp.
    */
   public static double clamp(double pValue, double pMin, double pMax) {
      if (pValue < pMin) {
         return pMin;
      } else {
         return pValue > pMax ? pMax : pValue;
      }
   }

   /**
    * Method for linear interpolation of doubles.
    * @param pStart Start value for the lerp.
    * @param pEnd End value for the lerp.
    * @param pDelta A value between 0 and 1 that indicates the percentage of the lerp. (0 will give the start value and
    * 1 will give the end value) If the value is not between 0 and 1, it is clamped.
    */
   public static double clampedLerp(double pStart, double pEnd, double pDelta) {
      if (pDelta < 0.0D) {
         return pStart;
      } else {
         return pDelta > 1.0D ? pEnd : lerp(pDelta, pStart, pEnd);
      }
   }

   /**
    * Maximum of the absolute value of two numbers.
    */
   public static double absMax(double pX, double pY) {
      if (pX < 0.0D) {
         pX = -pX;
      }

      if (pY < 0.0D) {
         pY = -pY;
      }

      return pX > pY ? pX : pY;
   }

   /**
    * Buckets an integer with specifed bucket sizes.
    */
   public static int intFloorDiv(int pX, int pY) {
      return Math.floorDiv(pX, pY);
   }

   public static int nextInt(Random pRandom, int pMinimum, int pMaximum) {
      return pMinimum >= pMaximum ? pMinimum : pRandom.nextInt(pMaximum - pMinimum + 1) + pMinimum;
   }

   public static float nextFloat(Random pRandom, float pMinimum, float pMaximum) {
      return pMinimum >= pMaximum ? pMinimum : pRandom.nextFloat() * (pMaximum - pMinimum) + pMinimum;
   }

   public static double nextDouble(Random pRandom, double pMinimum, double pMaximum) {
      return pMinimum >= pMaximum ? pMinimum : pRandom.nextDouble() * (pMaximum - pMinimum) + pMinimum;
   }

   public static double average(long[] pValues) {
      long i = 0L;

      for(long j : pValues) {
         i += j;
      }

      return (double)i / (double)pValues.length;
   }

   @OnlyIn(Dist.CLIENT)
   public static boolean equal(float pX, float pY) {
      return Math.abs(pY - pX) < 1.0E-5F;
   }

   public static boolean equal(double pX, double pY) {
      return Math.abs(pY - pX) < (double)1.0E-5F;
   }

   public static int positiveModulo(int pX, int pY) {
      return Math.floorMod(pX, pY);
   }

   @OnlyIn(Dist.CLIENT)
   public static float positiveModulo(float pNumerator, float pDenominator) {
      return (pNumerator % pDenominator + pDenominator) % pDenominator;
   }

   @OnlyIn(Dist.CLIENT)
   public static double positiveModulo(double pNumerator, double pDenominator) {
      return (pNumerator % pDenominator + pDenominator) % pDenominator;
   }

   /**
    * Adjust the angle so that his value is in range [-180180[
    */
   @OnlyIn(Dist.CLIENT)
   public static int wrapDegrees(int pAngle) {
      int i = pAngle % 360;
      if (i >= 180) {
         i -= 360;
      }

      if (i < -180) {
         i += 360;
      }

      return i;
   }

   /**
    * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
    */
   public static float wrapDegrees(float pValue) {
      float f = pValue % 360.0F;
      if (f >= 180.0F) {
         f -= 360.0F;
      }

      if (f < -180.0F) {
         f += 360.0F;
      }

      return f;
   }

   /**
    * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
    */
   public static double wrapDegrees(double pValue) {
      double d0 = pValue % 360.0D;
      if (d0 >= 180.0D) {
         d0 -= 360.0D;
      }

      if (d0 < -180.0D) {
         d0 += 360.0D;
      }

      return d0;
   }

   public static float degreesDifference(float p_203302_0_, float p_203302_1_) {
      return wrapDegrees(p_203302_1_ - p_203302_0_);
   }

   public static float degreesDifferenceAbs(float p_203301_0_, float p_203301_1_) {
      return abs(degreesDifference(p_203301_0_, p_203301_1_));
   }

   public static float rotateIfNecessary(float p_219800_0_, float p_219800_1_, float p_219800_2_) {
      float f = degreesDifference(p_219800_0_, p_219800_1_);
      float f1 = clamp(f, -p_219800_2_, p_219800_2_);
      return p_219800_1_ - f1;
   }

   public static float approach(float p_203300_0_, float p_203300_1_, float p_203300_2_) {
      p_203300_2_ = abs(p_203300_2_);
      return p_203300_0_ < p_203300_1_ ? clamp(p_203300_0_ + p_203300_2_, p_203300_0_, p_203300_1_) : clamp(p_203300_0_ - p_203300_2_, p_203300_1_, p_203300_0_);
   }

   public static float approachDegrees(float p_203303_0_, float p_203303_1_, float p_203303_2_) {
      float f = degreesDifference(p_203303_0_, p_203303_1_);
      return approach(p_203303_0_, p_203303_0_ + f, p_203303_2_);
   }

   /**
    * parses the string as integer or returns the second parameter if it fails
    */
   @OnlyIn(Dist.CLIENT)
   public static int getInt(String pValue, int pDefaultValue) {
      return NumberUtils.toInt(pValue, pDefaultValue);
   }

   /**
    * Returns the input value rounded up to the next highest power of two.
    */
   public static int smallestEncompassingPowerOfTwo(int pValue) {
      int i = pValue - 1;
      i = i | i >> 1;
      i = i | i >> 2;
      i = i | i >> 4;
      i = i | i >> 8;
      i = i | i >> 16;
      return i + 1;
   }

   /**
    * Is the given value a power of two?  (1, 2, 4, 8, 16, ...)
    */
   public static boolean isPowerOfTwo(int pValue) {
      return pValue != 0 && (pValue & pValue - 1) == 0;
   }

   /**
    * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given value.
    * Optimized for cases where the input value is a power-of-two. If the input value is not a power-of-two, then
    * subtract 1 from the return value.
    */
   public static int ceillog2(int pValue) {
      pValue = isPowerOfTwo(pValue) ? pValue : smallestEncompassingPowerOfTwo(pValue);
      return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)pValue * 125613361L >> 27) & 31];
   }

   /**
    * Efficiently calculates the floor of the base-2 log of an integer value.  This is effectively the index of the
    * highest bit that is set.  For example, if the number in binary is 0...100101, this will return 5.
    */
   public static int log2(int pValue) {
      return ceillog2(pValue) - (isPowerOfTwo(pValue) ? 0 : 1);
   }

   public static int roundUp(int p_154354_0_, int p_154354_1_) {
      if (p_154354_1_ == 0) {
         return 0;
      } else if (p_154354_0_ == 0) {
         return p_154354_1_;
      } else {
         if (p_154354_0_ < 0) {
            p_154354_1_ *= -1;
         }

         int i = p_154354_0_ % p_154354_1_;
         return i == 0 ? p_154354_0_ : p_154354_0_ + p_154354_1_ - i;
      }
   }

   /**
    * Makes an integer color from the given red, green, and blue float values
    */
   @OnlyIn(Dist.CLIENT)
   public static int color(float pR, float pG, float pB) {
      return color(floor(pR * 255.0F), floor(pG * 255.0F), floor(pB * 255.0F));
   }

   /**
    * Makes a single int color with the given red, green, and blue values.
    */
   @OnlyIn(Dist.CLIENT)
   public static int color(int pR, int pG, int pB) {
      int lvt_3_1_ = (pR << 8) + pG;
      return (lvt_3_1_ << 8) + pB;
   }

   public static float frac(float pNumber) {
      return pNumber - (float)floor(pNumber);
   }

   /**
    * Gets the decimal portion of the given double. For instance, {@code frac(5.5)} returns {@code .5}.
    */
   public static double frac(double pNumber) {
      return pNumber - (double)lfloor(pNumber);
   }

   public static long getSeed(Vector3i pPos) {
      return getSeed(pPos.getX(), pPos.getY(), pPos.getZ());
   }

   public static long getSeed(int pX, int pY, int pZ) {
      long i = (long)(pX * 3129871) ^ (long)pZ * 116129781L ^ (long)pY;
      i = i * i * 42317861L + i * 11L;
      return i >> 16;
   }

   public static UUID createInsecureUUID(Random pRand) {
      long i = pRand.nextLong() & -61441L | 16384L;
      long j = pRand.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
      return new UUID(i, j);
   }

   /**
    * Generates a random UUID using the shared random
    */
   public static UUID createInsecureUUID() {
      return createInsecureUUID(RANDOM);
   }

   public static double inverseLerp(double p_233020_0_, double p_233020_2_, double p_233020_4_) {
      return (p_233020_0_ - p_233020_2_) / (p_233020_4_ - p_233020_2_);
   }

   public static double atan2(double p_181159_0_, double p_181159_2_) {
      double d0 = p_181159_2_ * p_181159_2_ + p_181159_0_ * p_181159_0_;
      if (Double.isNaN(d0)) {
         return Double.NaN;
      } else {
         boolean flag = p_181159_0_ < 0.0D;
         if (flag) {
            p_181159_0_ = -p_181159_0_;
         }

         boolean flag1 = p_181159_2_ < 0.0D;
         if (flag1) {
            p_181159_2_ = -p_181159_2_;
         }

         boolean flag2 = p_181159_0_ > p_181159_2_;
         if (flag2) {
            double d1 = p_181159_2_;
            p_181159_2_ = p_181159_0_;
            p_181159_0_ = d1;
         }

         double d9 = fastInvSqrt(d0);
         p_181159_2_ = p_181159_2_ * d9;
         p_181159_0_ = p_181159_0_ * d9;
         double d2 = FRAC_BIAS + p_181159_0_;
         int i = (int)Double.doubleToRawLongBits(d2);
         double d3 = ASIN_TAB[i];
         double d4 = COS_TAB[i];
         double d5 = d2 - FRAC_BIAS;
         double d6 = p_181159_0_ * d4 - p_181159_2_ * d5;
         double d7 = (6.0D + d6 * d6) * d6 * 0.16666666666666666D;
         double d8 = d3 + d7;
         if (flag2) {
            d8 = (Math.PI / 2D) - d8;
         }

         if (flag1) {
            d8 = Math.PI - d8;
         }

         if (flag) {
            d8 = -d8;
         }

         return d8;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static float fastInvSqrt(float pNumber) {
      float f = 0.5F * pNumber;
      int i = Float.floatToIntBits(pNumber);
      i = 1597463007 - (i >> 1);
      pNumber = Float.intBitsToFloat(i);
      return pNumber * (1.5F - f * pNumber * pNumber);
   }

   /**
    * Computes 1/sqrt(n) using <a href="https://en.wikipedia.org/wiki/Fast_inverse_square_root">the fast inverse square
    * root</a> with a constant of 0x5FE6EB50C7B537AA.
    */
   public static double fastInvSqrt(double pNumber) {
      double d0 = 0.5D * pNumber;
      long i = Double.doubleToRawLongBits(pNumber);
      i = 6910469410427058090L - (i >> 1);
      pNumber = Double.longBitsToDouble(i);
      return pNumber * (1.5D - d0 * pNumber * pNumber);
   }

   @OnlyIn(Dist.CLIENT)
   public static float fastInvCubeRoot(float pNumber) {
      int i = Float.floatToIntBits(pNumber);
      i = 1419967116 - i / 3;
      float f = Float.intBitsToFloat(i);
      f = 0.6666667F * f + 1.0F / (3.0F * f * f * pNumber);
      return 0.6666667F * f + 1.0F / (3.0F * f * f * pNumber);
   }

   public static int hsvToRgb(float pHue, float pSaturation, float pValue) {
      int i = (int)(pHue * 6.0F) % 6;
      float f = pHue * 6.0F - (float)i;
      float f1 = pValue * (1.0F - pSaturation);
      float f2 = pValue * (1.0F - f * pSaturation);
      float f3 = pValue * (1.0F - (1.0F - f) * pSaturation);
      float f4;
      float f5;
      float f6;
      switch(i) {
      case 0:
         f4 = pValue;
         f5 = f3;
         f6 = f1;
         break;
      case 1:
         f4 = f2;
         f5 = pValue;
         f6 = f1;
         break;
      case 2:
         f4 = f1;
         f5 = pValue;
         f6 = f3;
         break;
      case 3:
         f4 = f1;
         f5 = f2;
         f6 = pValue;
         break;
      case 4:
         f4 = f3;
         f5 = f1;
         f6 = pValue;
         break;
      case 5:
         f4 = pValue;
         f5 = f1;
         f6 = f2;
         break;
      default:
         throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + pHue + ", " + pSaturation + ", " + pValue);
      }

      int j = clamp((int)(f4 * 255.0F), 0, 255);
      int k = clamp((int)(f5 * 255.0F), 0, 255);
      int l = clamp((int)(f6 * 255.0F), 0, 255);
      return j << 16 | k << 8 | l;
   }

   public static int murmurHash3Mixer(int p_188208_0_) {
      p_188208_0_ = p_188208_0_ ^ p_188208_0_ >>> 16;
      p_188208_0_ = p_188208_0_ * -2048144789;
      p_188208_0_ = p_188208_0_ ^ p_188208_0_ >>> 13;
      p_188208_0_ = p_188208_0_ * -1028477387;
      return p_188208_0_ ^ p_188208_0_ >>> 16;
   }

   public static int binarySearch(int pMin, int pMax, IntPredicate pIsTargetBeforeOrAt) {
      int i = pMax - pMin;

      while(i > 0) {
         int j = i / 2;
         int k = pMin + j;
         if (pIsTargetBeforeOrAt.test(k)) {
            i = j;
         } else {
            pMin = k + 1;
            i -= j + 1;
         }
      }

      return pMin;
   }

   /**
    * Method for linear interpolation of floats
    * @param pDelta A value usually between 0 and 1 that indicates the percentage of the lerp. (0 will give the start
    * value and 1 will give the end value)
    * @param pStart Start value for the lerp
    * @param pEnd End value for the lerp
    */
   public static float lerp(float pDelta, float pStart, float pEnd) {
      return pStart + pDelta * (pEnd - pStart);
   }

   /**
    * Method for linear interpolation of doubles
    * @param pDelta A value usually between 0 and 1 that indicates the percentage of the lerp. (0 will give the start
    * value and 1 will give the end value)
    * @param pStart Start value for the lerp
    * @param pEnd End value for the lerp
    */
   public static double lerp(double pDelta, double pStart, double pEnd) {
      return pStart + pDelta * (pEnd - pStart);
   }

   public static double lerp2(double p_219804_0_, double p_219804_2_, double p_219804_4_, double p_219804_6_, double p_219804_8_, double p_219804_10_) {
      return lerp(p_219804_2_, lerp(p_219804_0_, p_219804_4_, p_219804_6_), lerp(p_219804_0_, p_219804_8_, p_219804_10_));
   }

   public static double lerp3(double p_219807_0_, double p_219807_2_, double p_219807_4_, double p_219807_6_, double p_219807_8_, double p_219807_10_, double p_219807_12_, double p_219807_14_, double p_219807_16_, double p_219807_18_, double p_219807_20_) {
      return lerp(p_219807_4_, lerp2(p_219807_0_, p_219807_2_, p_219807_6_, p_219807_8_, p_219807_10_, p_219807_12_), lerp2(p_219807_0_, p_219807_2_, p_219807_14_, p_219807_16_, p_219807_18_, p_219807_20_));
   }

   public static double smoothstep(double p_219801_0_) {
      return p_219801_0_ * p_219801_0_ * p_219801_0_ * (p_219801_0_ * (p_219801_0_ * 6.0D - 15.0D) + 10.0D);
   }

   public static int sign(double pX) {
      if (pX == 0.0D) {
         return 0;
      } else {
         return pX > 0.0D ? 1 : -1;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static float rotLerp(float p_219805_0_, float p_219805_1_, float p_219805_2_) {
      return p_219805_1_ + p_219805_0_ * wrapDegrees(p_219805_2_ - p_219805_1_);
   }

   @Deprecated
   public static float rotlerp(float p_226167_0_, float p_226167_1_, float p_226167_2_) {
      float f;
      for(f = p_226167_1_ - p_226167_0_; f < -180.0F; f += 360.0F) {
      }

      while(f >= 180.0F) {
         f -= 360.0F;
      }

      return p_226167_0_ + p_226167_2_ * f;
   }

   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public static float rotWrap(double p_226168_0_) {
      while(p_226168_0_ >= 180.0D) {
         p_226168_0_ -= 360.0D;
      }

      while(p_226168_0_ < -180.0D) {
         p_226168_0_ += 360.0D;
      }

      return (float)p_226168_0_;
   }

   @OnlyIn(Dist.CLIENT)
   public static float triangleWave(float p_233021_0_, float p_233021_1_) {
      return (Math.abs(p_233021_0_ % p_233021_1_ - p_233021_1_ * 0.5F) - p_233021_1_ * 0.25F) / (p_233021_1_ * 0.25F);
   }

   public static float square(float pValue) {
      return pValue * pValue;
   }

   static {
      for(int i = 0; i < 257; ++i) {
         double d0 = (double)i / 256.0D;
         double d1 = Math.asin(d0);
         COS_TAB[i] = Math.cos(d1);
         ASIN_TAB[i] = d1;
      }

   }
}