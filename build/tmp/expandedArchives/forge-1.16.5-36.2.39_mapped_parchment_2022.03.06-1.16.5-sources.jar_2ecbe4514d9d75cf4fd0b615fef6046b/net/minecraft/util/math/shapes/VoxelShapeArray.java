package net.minecraft.util.math.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;

public final class VoxelShapeArray extends VoxelShape {
   private final DoubleList xs;
   private final DoubleList ys;
   private final DoubleList zs;

   protected VoxelShapeArray(VoxelShapePart pShape, double[] pXs, double[] pYs, double[] pZs) {
      this(pShape, (DoubleList)DoubleArrayList.wrap(Arrays.copyOf(pXs, pShape.getXSize() + 1)), (DoubleList)DoubleArrayList.wrap(Arrays.copyOf(pYs, pShape.getYSize() + 1)), (DoubleList)DoubleArrayList.wrap(Arrays.copyOf(pZs, pShape.getZSize() + 1)));
   }

   VoxelShapeArray(VoxelShapePart pShape, DoubleList pXs, DoubleList pYs, DoubleList pZs) {
      super(pShape);
      int i = pShape.getXSize() + 1;
      int j = pShape.getYSize() + 1;
      int k = pShape.getZSize() + 1;
      if (i == pXs.size() && j == pYs.size() && k == pZs.size()) {
         this.xs = pXs;
         this.ys = pYs;
         this.zs = pZs;
      } else {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Lengths of point arrays must be consistent with the size of the VoxelShape."));
      }
   }

   protected DoubleList getCoords(Direction.Axis pAxis) {
      switch(pAxis) {
      case X:
         return this.xs;
      case Y:
         return this.ys;
      case Z:
         return this.zs;
      default:
         throw new IllegalArgumentException();
      }
   }
}