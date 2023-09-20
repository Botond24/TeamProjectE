package net.minecraft.util.math.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class IndirectMerger implements IDoubleListMerger {
   private final DoubleArrayList result;
   private final IntArrayList firstIndices;
   private final IntArrayList secondIndices;

   protected IndirectMerger(DoubleList pLower, DoubleList pUpper, boolean p_i47685_3_, boolean p_i47685_4_) {
      int i = 0;
      int j = 0;
      double d0 = Double.NaN;
      int k = pLower.size();
      int l = pUpper.size();
      int i1 = k + l;
      this.result = new DoubleArrayList(i1);
      this.firstIndices = new IntArrayList(i1);
      this.secondIndices = new IntArrayList(i1);

      while(true) {
         boolean flag = i < k;
         boolean flag1 = j < l;
         if (!flag && !flag1) {
            if (this.result.isEmpty()) {
               this.result.add(Math.min(pLower.getDouble(k - 1), pUpper.getDouble(l - 1)));
            }

            return;
         }

         boolean flag2 = flag && (!flag1 || pLower.getDouble(i) < pUpper.getDouble(j) + 1.0E-7D);
         double d1 = flag2 ? pLower.getDouble(i++) : pUpper.getDouble(j++);
         if ((i != 0 && flag || flag2 || p_i47685_4_) && (j != 0 && flag1 || !flag2 || p_i47685_3_)) {
            if (!(d0 >= d1 - 1.0E-7D)) {
               this.firstIndices.add(i - 1);
               this.secondIndices.add(j - 1);
               this.result.add(d1);
               d0 = d1;
            } else if (!this.result.isEmpty()) {
               this.firstIndices.set(this.firstIndices.size() - 1, i - 1);
               this.secondIndices.set(this.secondIndices.size() - 1, j - 1);
            }
         }
      }
   }

   public boolean forMergedIndexes(IDoubleListMerger.IConsumer pConsumer) {
      for(int i = 0; i < this.result.size() - 1; ++i) {
         if (!pConsumer.merge(this.firstIndices.getInt(i), this.secondIndices.getInt(i), i)) {
            return false;
         }
      }

      return true;
   }

   public DoubleList getList() {
      return this.result;
   }
}