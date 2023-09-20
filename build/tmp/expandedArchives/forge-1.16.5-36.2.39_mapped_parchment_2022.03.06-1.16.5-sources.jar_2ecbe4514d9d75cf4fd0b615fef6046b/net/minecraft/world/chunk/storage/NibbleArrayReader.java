package net.minecraft.world.chunk.storage;

public class NibbleArrayReader {
   public final byte[] data;
   private final int depthBits;
   private final int depthBitsPlusFour;

   public NibbleArrayReader(byte[] pData, int pDepthBits) {
      this.data = pData;
      this.depthBits = pDepthBits;
      this.depthBitsPlusFour = pDepthBits + 4;
   }

   public int get(int pX, int pY, int pZ) {
      int i = pX << this.depthBitsPlusFour | pZ << this.depthBits | pY;
      int j = i >> 1;
      int k = i & 1;
      return k == 0 ? this.data[j] & 15 : this.data[j] >> 4 & 15;
   }
}