package net.minecraft.util;

import net.minecraft.util.math.SectionPos;
import net.minecraft.world.lighting.LevelBasedGraph;

public abstract class SectionDistanceGraph extends LevelBasedGraph {
   protected SectionDistanceGraph(int p_i50706_1_, int p_i50706_2_, int p_i50706_3_) {
      super(p_i50706_1_, p_i50706_2_, p_i50706_3_);
   }

   protected boolean isSource(long pPos) {
      return pPos == Long.MAX_VALUE;
   }

   protected void checkNeighborsAfterUpdate(long pPos, int pLevel, boolean pIsDecreasing) {
      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            for(int k = -1; k <= 1; ++k) {
               long l = SectionPos.offset(pPos, i, j, k);
               if (l != pPos) {
                  this.checkNeighbor(pPos, l, pLevel, pIsDecreasing);
               }
            }
         }
      }

   }

   /**
    * Computes level propagated from neighbors of specified position with given existing level, excluding the given
    * source position.
    */
   protected int getComputedLevel(long pPos, long pExcludedSourcePos, int pLevel) {
      int i = pLevel;

      for(int j = -1; j <= 1; ++j) {
         for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
               long i1 = SectionPos.offset(pPos, j, k, l);
               if (i1 == pPos) {
                  i1 = Long.MAX_VALUE;
               }

               if (i1 != pExcludedSourcePos) {
                  int j1 = this.computeLevelFromNeighbor(i1, pPos, this.getLevel(i1));
                  if (i > j1) {
                     i = j1;
                  }

                  if (i == 0) {
                     return i;
                  }
               }
            }
         }
      }

      return i;
   }

   /**
    * Returns level propagated from start position with specified level to the neighboring end position.
    */
   protected int computeLevelFromNeighbor(long pStartPos, long pEndPos, int pStartLevel) {
      return pStartPos == Long.MAX_VALUE ? this.getLevelFromSource(pEndPos) : pStartLevel + 1;
   }

   protected abstract int getLevelFromSource(long pPos);

   public void update(long pPos, int pLevel, boolean pIsDecreasing) {
      this.checkEdge(Long.MAX_VALUE, pPos, pLevel, pIsDecreasing);
   }
}