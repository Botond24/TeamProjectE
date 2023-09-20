package net.minecraft.util;

public class IntArray implements IIntArray {
   private final int[] ints;

   public IntArray(int pSize) {
      this.ints = new int[pSize];
   }

   public int get(int pIndex) {
      return this.ints[pIndex];
   }

   public void set(int pIndex, int pValue) {
      this.ints[pIndex] = pValue;
   }

   public int getCount() {
      return this.ints.length;
   }
}