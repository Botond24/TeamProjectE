package net.minecraft.util.math;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

public class ChunkPos {
   /** Value representing an absent or invalid chunkpos */
   public static final long INVALID_CHUNK_POS = asLong(1875016, 1875016);
   public final int x;
   public final int z;

   public ChunkPos(int pX, int pY) {
      this.x = pX;
      this.z = pY;
   }

   public ChunkPos(BlockPos pPos) {
      this.x = pPos.getX() >> 4;
      this.z = pPos.getZ() >> 4;
   }

   public ChunkPos(long p_i48713_1_) {
      this.x = (int)p_i48713_1_;
      this.z = (int)(p_i48713_1_ >> 32);
   }

   public long toLong() {
      return asLong(this.x, this.z);
   }

   /**
    * Converts the chunk coordinate pair to a long
    */
   public static long asLong(int pX, int pZ) {
      return (long)pX & 4294967295L | ((long)pZ & 4294967295L) << 32;
   }

   public static int getX(long pChunkAsLong) {
      return (int)(pChunkAsLong & 4294967295L);
   }

   public static int getZ(long pChunkAsLong) {
      return (int)(pChunkAsLong >>> 32 & 4294967295L);
   }

   public int hashCode() {
      int i = 1664525 * this.x + 1013904223;
      int j = 1664525 * (this.z ^ -559038737) + 1013904223;
      return i ^ j;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof ChunkPos)) {
         return false;
      } else {
         ChunkPos chunkpos = (ChunkPos)p_equals_1_;
         return this.x == chunkpos.x && this.z == chunkpos.z;
      }
   }

   /**
    * Get the first world X coordinate that belongs to this Chunk
    */
   public int getMinBlockX() {
      return this.x << 4;
   }

   /**
    * Get the first world Z coordinate that belongs to this Chunk
    */
   public int getMinBlockZ() {
      return this.z << 4;
   }

   /**
    * Get the last world X coordinate that belongs to this Chunk
    */
   public int getMaxBlockX() {
      return (this.x << 4) + 15;
   }

   /**
    * Get the last world Z coordinate that belongs to this Chunk
    */
   public int getMaxBlockZ() {
      return (this.z << 4) + 15;
   }

   /**
    * Gets the x-coordinate of the region file containing this chunk.
    */
   public int getRegionX() {
      return this.x >> 5;
   }

   /**
    * Gets the z-coordinate of the region file containing this chunk.
    */
   public int getRegionZ() {
      return this.z >> 5;
   }

   /**
    * Gets the x-coordinate of this chunk within the region file that contains it.
    */
   public int getRegionLocalX() {
      return this.x & 31;
   }

   /**
    * Gets the z-coordinate of this chunk within the region file that contains it.
    */
   public int getRegionLocalZ() {
      return this.z & 31;
   }

   public String toString() {
      return "[" + this.x + ", " + this.z + "]";
   }

   public BlockPos getWorldPosition() {
      return new BlockPos(this.getMinBlockX(), 0, this.getMinBlockZ());
   }

   public int getChessboardDistance(ChunkPos pChunkPos) {
      return Math.max(Math.abs(this.x - pChunkPos.x), Math.abs(this.z - pChunkPos.z));
   }

   public static Stream<ChunkPos> rangeClosed(ChunkPos pCenter, int pRadius) {
      return rangeClosed(new ChunkPos(pCenter.x - pRadius, pCenter.z - pRadius), new ChunkPos(pCenter.x + pRadius, pCenter.z + pRadius));
   }

   public static Stream<ChunkPos> rangeClosed(final ChunkPos pStart, final ChunkPos pEnd) {
      int i = Math.abs(pStart.x - pEnd.x) + 1;
      int j = Math.abs(pStart.z - pEnd.z) + 1;
      final int k = pStart.x < pEnd.x ? 1 : -1;
      final int l = pStart.z < pEnd.z ? 1 : -1;
      return StreamSupport.stream(new AbstractSpliterator<ChunkPos>((long)(i * j), 64) {
         @Nullable
         private ChunkPos pos;

         public boolean tryAdvance(Consumer<? super ChunkPos> p_tryAdvance_1_) {
            if (this.pos == null) {
               this.pos = pStart;
            } else {
               int i1 = this.pos.x;
               int j1 = this.pos.z;
               if (i1 == pEnd.x) {
                  if (j1 == pEnd.z) {
                     return false;
                  }

                  this.pos = new ChunkPos(pStart.x, j1 + l);
               } else {
                  this.pos = new ChunkPos(i1 + k, j1);
               }
            }

            p_tryAdvance_1_.accept(this.pos);
            return true;
         }
      }, false);
   }
}