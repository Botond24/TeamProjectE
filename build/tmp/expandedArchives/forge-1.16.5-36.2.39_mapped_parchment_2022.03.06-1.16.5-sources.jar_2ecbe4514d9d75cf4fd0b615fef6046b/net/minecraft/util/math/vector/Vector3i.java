package net.minecraft.util.math.vector;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;
import net.minecraft.dispenser.IPosition;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Immutable
public class Vector3i implements Comparable<Vector3i> {
   public static final Codec<Vector3i> CODEC = Codec.INT_STREAM.comapFlatMap((p_239783_0_) -> {
      return Util.fixedSize(p_239783_0_, 3).map((p_239784_0_) -> {
         return new Vector3i(p_239784_0_[0], p_239784_0_[1], p_239784_0_[2]);
      });
   }, (p_239782_0_) -> {
      return IntStream.of(p_239782_0_.getX(), p_239782_0_.getY(), p_239782_0_.getZ());
   });
   /** An immutable vector with zero as all coordinates. */
   public static final Vector3i ZERO = new Vector3i(0, 0, 0);
   private int x;
   private int y;
   private int z;

   public Vector3i(int pX, int pY, int pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }

   public Vector3i(double pX, double pY, double pZ) {
      this(MathHelper.floor(pX), MathHelper.floor(pY), MathHelper.floor(pZ));
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof Vector3i)) {
         return false;
      } else {
         Vector3i vector3i = (Vector3i)p_equals_1_;
         if (this.getX() != vector3i.getX()) {
            return false;
         } else if (this.getY() != vector3i.getY()) {
            return false;
         } else {
            return this.getZ() == vector3i.getZ();
         }
      }
   }

   public int hashCode() {
      return (this.getY() + this.getZ() * 31) * 31 + this.getX();
   }

   public int compareTo(Vector3i p_compareTo_1_) {
      if (this.getY() == p_compareTo_1_.getY()) {
         return this.getZ() == p_compareTo_1_.getZ() ? this.getX() - p_compareTo_1_.getX() : this.getZ() - p_compareTo_1_.getZ();
      } else {
         return this.getY() - p_compareTo_1_.getY();
      }
   }

   /**
    * Gets the X coordinate.
    */
   public int getX() {
      return this.x;
   }

   /**
    * Gets the Y coordinate.
    */
   public int getY() {
      return this.y;
   }

   /**
    * Gets the Z coordinate.
    */
   public int getZ() {
      return this.z;
   }

   protected void setX(int p_223471_1_) {
      this.x = p_223471_1_;
   }

   protected void setY(int p_185336_1_) {
      this.y = p_185336_1_;
   }

   protected void setZ(int p_223472_1_) {
      this.z = p_223472_1_;
   }

   /**
    * Offset this vector 1 unit up
    */
   public Vector3i above() {
      return this.above(1);
   }

   /**
    * Offset this vector upwards by the given distance.
    */
   public Vector3i above(int pDistance) {
      return this.relative(Direction.UP, pDistance);
   }

   /**
    * Offset this vector 1 unit down
    */
   public Vector3i below() {
      return this.below(1);
   }

   /**
    * Offset this vector downwards by the given distance.
    */
   public Vector3i below(int pDistance) {
      return this.relative(Direction.DOWN, pDistance);
   }

   /**
    * Offsets this Vector by the given distance in the specified direction.
    */
   public Vector3i relative(Direction pDirection, int pDistance) {
      return pDistance == 0 ? this : new Vector3i(this.getX() + pDirection.getStepX() * pDistance, this.getY() + pDirection.getStepY() * pDistance, this.getZ() + pDirection.getStepZ() * pDistance);
   }

   /**
    * Calculate the cross product of this and the given Vector
    */
   public Vector3i cross(Vector3i pVector) {
      return new Vector3i(this.getY() * pVector.getZ() - this.getZ() * pVector.getY(), this.getZ() * pVector.getX() - this.getX() * pVector.getZ(), this.getX() * pVector.getY() - this.getY() * pVector.getX());
   }

   public boolean closerThan(Vector3i pVector, double pDistance) {
      return this.distSqr((double)pVector.getX(), (double)pVector.getY(), (double)pVector.getZ(), false) < pDistance * pDistance;
   }

   public boolean closerThan(IPosition pPosition, double pDistance) {
      return this.distSqr(pPosition.x(), pPosition.y(), pPosition.z(), true) < pDistance * pDistance;
   }

   /**
    * Calculate squared distance to the given Vector
    */
   public double distSqr(Vector3i pVector) {
      return this.distSqr((double)pVector.getX(), (double)pVector.getY(), (double)pVector.getZ(), true);
   }

   public double distSqr(IPosition pPosition, boolean pUseCenter) {
      return this.distSqr(pPosition.x(), pPosition.y(), pPosition.z(), pUseCenter);
   }

   public double distSqr(double pX, double pY, double pZ, boolean pUseCenter) {
      double d0 = pUseCenter ? 0.5D : 0.0D;
      double d1 = (double)this.getX() + d0 - pX;
      double d2 = (double)this.getY() + d0 - pY;
      double d3 = (double)this.getZ() + d0 - pZ;
      return d1 * d1 + d2 * d2 + d3 * d3;
   }

   public int distManhattan(Vector3i pVector) {
      float f = (float)Math.abs(pVector.getX() - this.getX());
      float f1 = (float)Math.abs(pVector.getY() - this.getY());
      float f2 = (float)Math.abs(pVector.getZ() - this.getZ());
      return (int)(f + f1 + f2);
   }

   public int get(Direction.Axis pAxis) {
      return pAxis.choose(this.x, this.y, this.z);
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
   }

   @OnlyIn(Dist.CLIENT)
   public String toShortString() {
      return "" + this.getX() + ", " + this.getY() + ", " + this.getZ();
   }
}