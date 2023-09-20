package net.minecraft.util.math.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class SimpleDoubleMerger implements IDoubleListMerger {
   private final DoubleList coords;

   public SimpleDoubleMerger(DoubleList pCoords) {
      this.coords = pCoords;
   }

   public boolean forMergedIndexes(IDoubleListMerger.IConsumer pConsumer) {
      for(int i = 0; i <= this.coords.size(); ++i) {
         if (!pConsumer.merge(i, i, i)) {
            return false;
         }
      }

      return true;
   }

   public DoubleList getList() {
      return this.coords;
   }
}