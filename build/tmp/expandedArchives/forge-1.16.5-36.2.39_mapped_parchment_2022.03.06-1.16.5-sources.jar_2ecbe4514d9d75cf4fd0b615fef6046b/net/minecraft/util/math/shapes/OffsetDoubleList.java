package net.minecraft.util.math.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class OffsetDoubleList extends AbstractDoubleList {
   private final DoubleList delegate;
   private final double offset;

   public OffsetDoubleList(DoubleList pDelegate, double pOffset) {
      this.delegate = pDelegate;
      this.offset = pOffset;
   }

   public double getDouble(int p_getDouble_1_) {
      return this.delegate.getDouble(p_getDouble_1_) + this.offset;
   }

   public int size() {
      return this.delegate.size();
   }
}