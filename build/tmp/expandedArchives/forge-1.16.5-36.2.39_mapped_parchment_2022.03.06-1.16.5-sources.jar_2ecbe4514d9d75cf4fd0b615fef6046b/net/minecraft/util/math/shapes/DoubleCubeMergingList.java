package net.minecraft.util.math.shapes;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public final class DoubleCubeMergingList implements IDoubleListMerger {
   private final DoubleRangeList result;
   private final int firstSize;
   private final int secondSize;
   private final int gcd;

   DoubleCubeMergingList(int pAa, int pBb) {
      this.result = new DoubleRangeList((int)VoxelShapes.lcm(pAa, pBb));
      this.firstSize = pAa;
      this.secondSize = pBb;
      this.gcd = IntMath.gcd(pAa, pBb);
   }

   public boolean forMergedIndexes(IDoubleListMerger.IConsumer pConsumer) {
      int i = this.firstSize / this.gcd;
      int j = this.secondSize / this.gcd;

      for(int k = 0; k <= this.result.size(); ++k) {
         if (!pConsumer.merge(k / j, k / i, k)) {
            return false;
         }
      }

      return true;
   }

   public DoubleList getList() {
      return this.result;
   }
}