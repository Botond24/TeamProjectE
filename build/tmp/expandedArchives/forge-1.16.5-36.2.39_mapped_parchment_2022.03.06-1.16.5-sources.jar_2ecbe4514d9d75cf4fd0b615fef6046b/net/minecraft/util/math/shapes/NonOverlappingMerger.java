package net.minecraft.util.math.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class NonOverlappingMerger extends AbstractDoubleList implements IDoubleListMerger {
   private final DoubleList lower;
   private final DoubleList upper;
   private final boolean swap;

   public NonOverlappingMerger(DoubleList pLower, DoubleList pUpper, boolean pSwap) {
      this.lower = pLower;
      this.upper = pUpper;
      this.swap = pSwap;
   }

   public int size() {
      return this.lower.size() + this.upper.size();
   }

   public boolean forMergedIndexes(IDoubleListMerger.IConsumer pConsumer) {
      return this.swap ? this.forNonSwappedIndexes((p_199636_1_, p_199636_2_, p_199636_3_) -> {
         return pConsumer.merge(p_199636_2_, p_199636_1_, p_199636_3_);
      }) : this.forNonSwappedIndexes(pConsumer);
   }

   private boolean forNonSwappedIndexes(IDoubleListMerger.IConsumer pConsumer) {
      int i = this.lower.size() - 1;

      for(int j = 0; j < i; ++j) {
         if (!pConsumer.merge(j, -1, j)) {
            return false;
         }
      }

      if (!pConsumer.merge(i, -1, i)) {
         return false;
      } else {
         for(int k = 0; k < this.upper.size(); ++k) {
            if (!pConsumer.merge(i, k, i + 1 + k)) {
               return false;
            }
         }

         return true;
      }
   }

   public double getDouble(int p_getDouble_1_) {
      return p_getDouble_1_ < this.lower.size() ? this.lower.getDouble(p_getDouble_1_) : this.upper.getDouble(p_getDouble_1_ - this.lower.size());
   }

   public DoubleList getList() {
      return this;
   }
}