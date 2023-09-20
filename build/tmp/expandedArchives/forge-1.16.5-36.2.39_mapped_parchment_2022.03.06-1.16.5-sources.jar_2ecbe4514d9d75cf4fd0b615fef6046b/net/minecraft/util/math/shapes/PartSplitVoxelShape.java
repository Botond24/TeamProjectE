package net.minecraft.util.math.shapes;

import net.minecraft.util.Direction;

public final class PartSplitVoxelShape extends VoxelShapePart {
   private final VoxelShapePart parent;
   private final int startX;
   private final int startY;
   private final int startZ;
   private final int endX;
   private final int endY;
   private final int endZ;

   protected PartSplitVoxelShape(VoxelShapePart pParent, int pStartX, int pStartY, int pStartZ, int pEndX, int pEndY, int pEndZ) {
      super(pEndX - pStartX, pEndY - pStartY, pEndZ - pStartZ);
      this.parent = pParent;
      this.startX = pStartX;
      this.startY = pStartY;
      this.startZ = pStartZ;
      this.endX = pEndX;
      this.endY = pEndY;
      this.endZ = pEndZ;
   }

   public boolean isFull(int pX, int pY, int pZ) {
      return this.parent.isFull(this.startX + pX, this.startY + pY, this.startZ + pZ);
   }

   public void setFull(int p_199625_1_, int p_199625_2_, int p_199625_3_, boolean p_199625_4_, boolean p_199625_5_) {
      this.parent.setFull(this.startX + p_199625_1_, this.startY + p_199625_2_, this.startZ + p_199625_3_, p_199625_4_, p_199625_5_);
   }

   public int firstFull(Direction.Axis pAxis) {
      return Math.max(0, this.parent.firstFull(pAxis) - pAxis.choose(this.startX, this.startY, this.startZ));
   }

   public int lastFull(Direction.Axis pAxis) {
      return Math.min(pAxis.choose(this.endX, this.endY, this.endZ), this.parent.lastFull(pAxis) - pAxis.choose(this.startX, this.startY, this.startZ));
   }
}