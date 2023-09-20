package net.minecraft.util;

import java.util.Random;

public class SharedSeedRandom extends Random {
   private int count;

   public SharedSeedRandom() {
   }

   public SharedSeedRandom(long pSeed) {
      super(pSeed);
   }

   public void consumeCount(int p_202423_1_) {
      for(int i = 0; i < p_202423_1_; ++i) {
         this.next(1);
      }

   }

   protected int next(int p_next_1_) {
      ++this.count;
      return super.next(p_next_1_);
   }

   public long setBaseChunkSeed(int pX, int pZ) {
      long i = (long)pX * 341873128712L + (long)pZ * 132897987541L;
      this.setSeed(i);
      return i;
   }

   public long setDecorationSeed(long pBaseSeed, int pX, int pZ) {
      this.setSeed(pBaseSeed);
      long i = this.nextLong() | 1L;
      long j = this.nextLong() | 1L;
      long k = (long)pX * i + (long)pZ * j ^ pBaseSeed;
      this.setSeed(k);
      return k;
   }

   public long setFeatureSeed(long pBaseSeed, int pX, int pZ) {
      long i = pBaseSeed + (long)pX + (long)(10000 * pZ);
      this.setSeed(i);
      return i;
   }

   public long setLargeFeatureSeed(long pSeed, int pX, int pZ) {
      this.setSeed(pSeed);
      long i = this.nextLong();
      long j = this.nextLong();
      long k = (long)pX * i ^ (long)pZ * j ^ pSeed;
      this.setSeed(k);
      return k;
   }

   public long setLargeFeatureWithSalt(long pBaseSeed, int pX, int pZ, int pModifier) {
      long i = (long)pX * 341873128712L + (long)pZ * 132897987541L + pBaseSeed + (long)pModifier;
      this.setSeed(i);
      return i;
   }

   public static Random seedSlimeChunk(int pChunkX, int pChunkZ, long pSeed, long p_205190_4_) {
      return new Random(pSeed + (long)(pChunkX * pChunkX * 4987142) + (long)(pChunkX * 5947611) + (long)(pChunkZ * pChunkZ) * 4392871L + (long)(pChunkZ * 389711) ^ p_205190_4_);
   }
}