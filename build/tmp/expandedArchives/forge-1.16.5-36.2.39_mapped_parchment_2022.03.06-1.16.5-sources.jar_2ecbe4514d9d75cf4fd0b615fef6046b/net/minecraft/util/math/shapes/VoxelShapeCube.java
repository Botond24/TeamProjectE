package net.minecraft.util.math.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public final class VoxelShapeCube extends VoxelShape {
   protected VoxelShapeCube(VoxelShapePart p_i48182_1_) {
      super(p_i48182_1_);
   }

   protected DoubleList getCoords(Direction.Axis pAxis) {
      return new DoubleRangeList(this.shape.getSize(pAxis));
   }

   protected int findIndex(Direction.Axis pAxis, double pPosition) {
      int i = this.shape.getSize(pAxis);
      return MathHelper.clamp(MathHelper.floor(pPosition * (double)i), -1, i);
   }
}