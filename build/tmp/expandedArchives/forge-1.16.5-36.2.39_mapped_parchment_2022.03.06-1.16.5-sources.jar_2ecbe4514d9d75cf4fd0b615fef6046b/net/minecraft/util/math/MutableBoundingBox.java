package net.minecraft.util.math;

import com.google.common.base.MoreObjects;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3i;

/**
 * A simple three dimensional mutable integer bounding box.
 * Note that this box is both mutable, and has an implementation of {@code hashCode()} and {@code equals()}.
 * This can be used as {@code HashMap} keys for example, if the user can ensure the instances themselves are not
 * modified.
 */
public class MutableBoundingBox {
   public int x0;
   public int y0;
   public int z0;
   public int x1;
   public int y1;
   public int z1;

   public MutableBoundingBox() {
   }

   public MutableBoundingBox(int[] p_i43000_1_) {
      if (p_i43000_1_.length == 6) {
         this.x0 = p_i43000_1_[0];
         this.y0 = p_i43000_1_[1];
         this.z0 = p_i43000_1_[2];
         this.x1 = p_i43000_1_[3];
         this.y1 = p_i43000_1_[4];
         this.z1 = p_i43000_1_[5];
      }

   }

   public static MutableBoundingBox getUnknownBox() {
      return new MutableBoundingBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   public static MutableBoundingBox infinite() {
      return new MutableBoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   /**
    * Create a bounding box with the specified dimensions and rotate it. Used to project a possible new component
    * Bounding Box - to check if it would cut anything already spawned.
    */
   public static MutableBoundingBox orientBox(int pStructureMinX, int pStructureMinY, int pStructureMinZ, int pXMin, int pYMin, int pZMin, int pXMax, int pYMax, int pZMax, Direction pFacing) {
      switch(pFacing) {
      case NORTH:
         return new MutableBoundingBox(pStructureMinX + pXMin, pStructureMinY + pYMin, pStructureMinZ - pZMax + 1 + pZMin, pStructureMinX + pXMax - 1 + pXMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pZMin);
      case SOUTH:
         return new MutableBoundingBox(pStructureMinX + pXMin, pStructureMinY + pYMin, pStructureMinZ + pZMin, pStructureMinX + pXMax - 1 + pXMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pZMax - 1 + pZMin);
      case WEST:
         return new MutableBoundingBox(pStructureMinX - pZMax + 1 + pZMin, pStructureMinY + pYMin, pStructureMinZ + pXMin, pStructureMinX + pZMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pXMax - 1 + pXMin);
      case EAST:
         return new MutableBoundingBox(pStructureMinX + pZMin, pStructureMinY + pYMin, pStructureMinZ + pXMin, pStructureMinX + pZMax - 1 + pZMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pXMax - 1 + pXMin);
      default:
         return new MutableBoundingBox(pStructureMinX + pXMin, pStructureMinY + pYMin, pStructureMinZ + pZMin, pStructureMinX + pXMax - 1 + pXMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pZMax - 1 + pZMin);
      }
   }

   public static MutableBoundingBox createProper(int p_175899_0_, int p_175899_1_, int p_175899_2_, int p_175899_3_, int p_175899_4_, int p_175899_5_) {
      return new MutableBoundingBox(Math.min(p_175899_0_, p_175899_3_), Math.min(p_175899_1_, p_175899_4_), Math.min(p_175899_2_, p_175899_5_), Math.max(p_175899_0_, p_175899_3_), Math.max(p_175899_1_, p_175899_4_), Math.max(p_175899_2_, p_175899_5_));
   }

   public MutableBoundingBox(MutableBoundingBox p_i2031_1_) {
      this.x0 = p_i2031_1_.x0;
      this.y0 = p_i2031_1_.y0;
      this.z0 = p_i2031_1_.z0;
      this.x1 = p_i2031_1_.x1;
      this.y1 = p_i2031_1_.y1;
      this.z1 = p_i2031_1_.z1;
   }

   public MutableBoundingBox(int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      this.x0 = pMinX;
      this.y0 = pMinY;
      this.z0 = pMinZ;
      this.x1 = pMaxX;
      this.y1 = pMaxY;
      this.z1 = pMaxZ;
   }

   public MutableBoundingBox(Vector3i p_i45626_1_, Vector3i p_i45626_2_) {
      this.x0 = Math.min(p_i45626_1_.getX(), p_i45626_2_.getX());
      this.y0 = Math.min(p_i45626_1_.getY(), p_i45626_2_.getY());
      this.z0 = Math.min(p_i45626_1_.getZ(), p_i45626_2_.getZ());
      this.x1 = Math.max(p_i45626_1_.getX(), p_i45626_2_.getX());
      this.y1 = Math.max(p_i45626_1_.getY(), p_i45626_2_.getY());
      this.z1 = Math.max(p_i45626_1_.getZ(), p_i45626_2_.getZ());
   }

   public MutableBoundingBox(int p_i2033_1_, int p_i2033_2_, int p_i2033_3_, int p_i2033_4_) {
      this.x0 = p_i2033_1_;
      this.z0 = p_i2033_2_;
      this.x1 = p_i2033_3_;
      this.z1 = p_i2033_4_;
      this.y0 = 1;
      this.y1 = 512;
   }

   /**
    * @return {@code true} if {@code box} intersects this box.
    */
   public boolean intersects(MutableBoundingBox pBox) {
      return this.x1 >= pBox.x0 && this.x0 <= pBox.x1 && this.z1 >= pBox.z0 && this.z0 <= pBox.z1 && this.y1 >= pBox.y0 && this.y0 <= pBox.y1;
   }

   /**
    * @return {@code true} if this bounding box intersects the horizontal x/z region described by the min and max
    * parameters.
    */
   public boolean intersects(int pMinX, int pMinZ, int pMaxX, int pMaxZ) {
      return this.x1 >= pMinX && this.x0 <= pMaxX && this.z1 >= pMinZ && this.z0 <= pMaxZ;
   }

   public void expand(MutableBoundingBox p_78888_1_) {
      this.x0 = Math.min(this.x0, p_78888_1_.x0);
      this.y0 = Math.min(this.y0, p_78888_1_.y0);
      this.z0 = Math.min(this.z0, p_78888_1_.z0);
      this.x1 = Math.max(this.x1, p_78888_1_.x1);
      this.y1 = Math.max(this.y1, p_78888_1_.y1);
      this.z1 = Math.max(this.z1, p_78888_1_.z1);
   }

   public void move(int p_78886_1_, int p_78886_2_, int p_78886_3_) {
      this.x0 += p_78886_1_;
      this.y0 += p_78886_2_;
      this.z0 += p_78886_3_;
      this.x1 += p_78886_1_;
      this.y1 += p_78886_2_;
      this.z1 += p_78886_3_;
   }

   /**
    * @return A new bounding box equal to this box, translated by the given coordinates.
    */
   public MutableBoundingBox moved(int pX, int pY, int pZ) {
      return new MutableBoundingBox(this.x0 + pX, this.y0 + pY, this.z0 + pZ, this.x1 + pX, this.y1 + pY, this.z1 + pZ);
   }

   public void move(Vector3i p_236989_1_) {
      this.move(p_236989_1_.getX(), p_236989_1_.getY(), p_236989_1_.getZ());
   }

   /**
    * @return {@code true} if the bounding box contains the {@code vector}.
    */
   public boolean isInside(Vector3i pVector) {
      return pVector.getX() >= this.x0 && pVector.getX() <= this.x1 && pVector.getZ() >= this.z0 && pVector.getZ() <= this.z1 && pVector.getY() >= this.y0 && pVector.getY() <= this.y1;
   }

   /**
    * Returns a vector describing the dimensions of this bounding box.
    * Note that unlike {@code getXSpan()}, {@code getYSpan()}, and {@code getZSpan()}, the length is interpreted here as
    * the difference in coordinates. So a box over a 1x1x1 area, which still contains a single point, will report length
    * zero.
    */
   public Vector3i getLength() {
      return new Vector3i(this.x1 - this.x0, this.y1 - this.y0, this.z1 - this.z0);
   }

   /**
    * @return The length of this bounding box along the x axis.
    */
   public int getXSpan() {
      return this.x1 - this.x0 + 1;
   }

   /**
    * @return The length of this bounding box along the y axis.
    */
   public int getYSpan() {
      return this.y1 - this.y0 + 1;
   }

   /**
    * @return The length of this bounding box along the z axis.
    */
   public int getZSpan() {
      return this.z1 - this.z0 + 1;
   }

   public Vector3i getCenter() {
      return new BlockPos(this.x0 + (this.x1 - this.x0 + 1) / 2, this.y0 + (this.y1 - this.y0 + 1) / 2, this.z0 + (this.z1 - this.z0 + 1) / 2);
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("x0", this.x0).add("y0", this.y0).add("z0", this.z0).add("x1", this.x1).add("y1", this.y1).add("z1", this.z1).toString();
   }

   public IntArrayNBT createTag() {
      return new IntArrayNBT(new int[]{this.x0, this.y0, this.z0, this.x1, this.y1, this.z1});
   }
}