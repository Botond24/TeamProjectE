package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import net.minecraft.util.Util;

public class NibbleArray {
   @Nullable
   protected byte[] data;

   public NibbleArray() {
   }

   public NibbleArray(byte[] pData) {
      this.data = pData;
      if (pData.length != 2048) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + pData.length));
      }
   }

   protected NibbleArray(int pSize) {
      this.data = new byte[pSize];
   }

   /**
    * Returns the nibble of data corresponding to the passed in x, y, z. y is at most 6 bits, z is at most 4.
    */
   public int get(int pX, int pY, int pZ) {
      return this.get(this.getIndex(pX, pY, pZ));
   }

   /**
    * Arguments are x, y, z, val. Sets the nibble of data at x << 11 | z << 7 | y to val.
    */
   public void set(int pX, int pY, int pZ, int pValue) {
      this.set(this.getIndex(pX, pY, pZ), pValue);
   }

   protected int getIndex(int pX, int pY, int pZ) {
      return pY << 8 | pZ << 4 | pX;
   }

   private int get(int pIndex) {
      if (this.data == null) {
         return 0;
      } else {
         int i = this.getPosition(pIndex);
         return this.isFirst(pIndex) ? this.data[i] & 15 : this.data[i] >> 4 & 15;
      }
   }

   private void set(int pIndex, int pValue) {
      if (this.data == null) {
         this.data = new byte[2048];
      }

      int i = this.getPosition(pIndex);
      if (this.isFirst(pIndex)) {
         this.data[i] = (byte)(this.data[i] & 240 | pValue & 15);
      } else {
         this.data[i] = (byte)(this.data[i] & 15 | (pValue & 15) << 4);
      }

   }

   private boolean isFirst(int pIndex) {
      return (pIndex & 1) == 0;
   }

   private int getPosition(int pIndex) {
      return pIndex >> 1;
   }

   public byte[] getData() {
      if (this.data == null) {
         this.data = new byte[2048];
      }

      return this.data;
   }

   public NibbleArray copy() {
      return this.data == null ? new NibbleArray() : new NibbleArray((byte[])this.data.clone());
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();

      for(int i = 0; i < 4096; ++i) {
         stringbuilder.append(Integer.toHexString(this.get(i)));
         if ((i & 15) == 15) {
            stringbuilder.append("\n");
         }

         if ((i & 255) == 255) {
            stringbuilder.append("\n");
         }
      }

      return stringbuilder.toString();
   }

   public boolean isEmpty() {
      return this.data == null;
   }
}