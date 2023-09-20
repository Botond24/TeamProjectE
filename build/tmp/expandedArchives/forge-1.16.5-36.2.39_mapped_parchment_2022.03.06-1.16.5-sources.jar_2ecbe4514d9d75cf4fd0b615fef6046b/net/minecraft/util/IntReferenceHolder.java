package net.minecraft.util;

public abstract class IntReferenceHolder {
   private int prevValue;

   public static IntReferenceHolder forContainer(final IIntArray pData, final int pIdx) {
      return new IntReferenceHolder() {
         public int get() {
            return pData.get(pIdx);
         }

         public void set(int pValue) {
            pData.set(pIdx, pValue);
         }
      };
   }

   public static IntReferenceHolder shared(final int[] pData, final int pIdx) {
      return new IntReferenceHolder() {
         public int get() {
            return pData[pIdx];
         }

         public void set(int pValue) {
            pData[pIdx] = pValue;
         }
      };
   }

   public static IntReferenceHolder standalone() {
      return new IntReferenceHolder() {
         private int value;

         public int get() {
            return this.value;
         }

         public void set(int pValue) {
            this.value = pValue;
         }
      };
   }

   public abstract int get();

   public abstract void set(int pValue);

   public boolean checkAndClearUpdateFlag() {
      int i = this.get();
      boolean flag = i != this.prevValue;
      this.prevValue = i;
      return flag;
   }
}