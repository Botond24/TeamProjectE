package net.minecraft.util;

public class FastRandom {
   public static long next(long pLeft, long pRight) {
      pLeft = pLeft * (pLeft * 6364136223846793005L + 1442695040888963407L);
      return pLeft + pRight;
   }
}