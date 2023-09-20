package net.minecraft.util.math.vector;

import java.util.EnumSet;
import net.minecraft.dispenser.IPosition;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Vector3d implements IPosition {
   public static final Vector3d ZERO = new Vector3d(0.0D, 0.0D, 0.0D);
   public final double x;
   public final double y;
   public final double z;

   @OnlyIn(Dist.CLIENT)
   public static Vector3d fromRGB24(int pPacked) {
      double d0 = (double)(pPacked >> 16 & 255) / 255.0D;
      double d1 = (double)(pPacked >> 8 & 255) / 255.0D;
      double d2 = (double)(pPacked & 255) / 255.0D;
      return new Vector3d(d0, d1, d2);
   }

   /**
    * Copies the coordinates of an Int vector and centers them.
    */
   public static Vector3d atCenterOf(Vector3i pToCopy) {
      return new Vector3d((double)pToCopy.getX() + 0.5D, (double)pToCopy.getY() + 0.5D, (double)pToCopy.getZ() + 0.5D);
   }

   /**
    * Copies the coordinates of an int vector exactly.
    */
   public static Vector3d atLowerCornerOf(Vector3i pToCopy) {
      return new Vector3d((double)pToCopy.getX(), (double)pToCopy.getY(), (double)pToCopy.getZ());
   }

   /**
    * Copies the coordinates of an int vector and centers them horizontally (x and z)
    */
   public static Vector3d atBottomCenterOf(Vector3i pToCopy) {
      return new Vector3d((double)pToCopy.getX() + 0.5D, (double)pToCopy.getY(), (double)pToCopy.getZ() + 0.5D);
   }

   /**
    * Copies the coordinates of an int vector and centers them horizontally and applies a vertical offset.
    */
   public static Vector3d upFromBottomCenterOf(Vector3i pToCopy, double pVerticalOffset) {
      return new Vector3d((double)pToCopy.getX() + 0.5D, (double)pToCopy.getY() + pVerticalOffset, (double)pToCopy.getZ() + 0.5D);
   }

   public Vector3d(double pX, double pY, double pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }

   public Vector3d(Vector3f pFloatVector) {
      this((double)pFloatVector.x(), (double)pFloatVector.y(), (double)pFloatVector.z());
   }

   /**
    * Returns a new vector with the result of the specified vector minus this.
    */
   public Vector3d vectorTo(Vector3d pVec) {
      return new Vector3d(pVec.x - this.x, pVec.y - this.y, pVec.z - this.z);
   }

   /**
    * Normalizes the vector to a length of 1 (except if it is the zero vector)
    */
   public Vector3d normalize() {
      double d0 = (double)MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
      return d0 < 1.0E-4D ? ZERO : new Vector3d(this.x / d0, this.y / d0, this.z / d0);
   }

   public double dot(Vector3d pVec) {
      return this.x * pVec.x + this.y * pVec.y + this.z * pVec.z;
   }

   /**
    * Returns a new vector with the result of this vector x the specified vector.
    */
   public Vector3d cross(Vector3d pVec) {
      return new Vector3d(this.y * pVec.z - this.z * pVec.y, this.z * pVec.x - this.x * pVec.z, this.x * pVec.y - this.y * pVec.x);
   }

   public Vector3d subtract(Vector3d pVec) {
      return this.subtract(pVec.x, pVec.y, pVec.z);
   }

   public Vector3d subtract(double pX, double pY, double pZ) {
      return this.add(-pX, -pY, -pZ);
   }

   public Vector3d add(Vector3d pVec) {
      return this.add(pVec.x, pVec.y, pVec.z);
   }

   /**
    * Adds the specified x,y,z vector components to this vector and returns the resulting vector. Does not change this
    * vector.
    */
   public Vector3d add(double pX, double pY, double pZ) {
      return new Vector3d(this.x + pX, this.y + pY, this.z + pZ);
   }

   /**
    * Checks if a position is within a certain distance of the coordinates.
    */
   public boolean closerThan(IPosition pPos, double pDistance) {
      return this.distanceToSqr(pPos.x(), pPos.y(), pPos.z()) < pDistance * pDistance;
   }

   /**
    * Euclidean distance between this and the specified vector, returned as double.
    */
   public double distanceTo(Vector3d pVec) {
      double d0 = pVec.x - this.x;
      double d1 = pVec.y - this.y;
      double d2 = pVec.z - this.z;
      return (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
   }

   /**
    * The square of the Euclidean distance between this and the specified vector.
    */
   public double distanceToSqr(Vector3d pVec) {
      double d0 = pVec.x - this.x;
      double d1 = pVec.y - this.y;
      double d2 = pVec.z - this.z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public double distanceToSqr(double pX, double pY, double pZ) {
      double d0 = pX - this.x;
      double d1 = pY - this.y;
      double d2 = pZ - this.z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public Vector3d scale(double pFactor) {
      return this.multiply(pFactor, pFactor, pFactor);
   }

   public Vector3d reverse() {
      return this.scale(-1.0D);
   }

   public Vector3d multiply(Vector3d pVec) {
      return this.multiply(pVec.x, pVec.y, pVec.z);
   }

   public Vector3d multiply(double pFactorX, double pFactorY, double pFactorZ) {
      return new Vector3d(this.x * pFactorX, this.y * pFactorY, this.z * pFactorZ);
   }

   /**
    * Returns the length of the vector.
    */
   public double length() {
      return (double)MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public double lengthSqr() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof Vector3d)) {
         return false;
      } else {
         Vector3d vector3d = (Vector3d)p_equals_1_;
         if (Double.compare(vector3d.x, this.x) != 0) {
            return false;
         } else if (Double.compare(vector3d.y, this.y) != 0) {
            return false;
         } else {
            return Double.compare(vector3d.z, this.z) == 0;
         }
      }
   }

   public int hashCode() {
      long j = Double.doubleToLongBits(this.x);
      int i = (int)(j ^ j >>> 32);
      j = Double.doubleToLongBits(this.y);
      i = 31 * i + (int)(j ^ j >>> 32);
      j = Double.doubleToLongBits(this.z);
      return 31 * i + (int)(j ^ j >>> 32);
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }

   public Vector3d xRot(float pPitch) {
      float f = MathHelper.cos(pPitch);
      float f1 = MathHelper.sin(pPitch);
      double d0 = this.x;
      double d1 = this.y * (double)f + this.z * (double)f1;
      double d2 = this.z * (double)f - this.y * (double)f1;
      return new Vector3d(d0, d1, d2);
   }

   public Vector3d yRot(float pYaw) {
      float f = MathHelper.cos(pYaw);
      float f1 = MathHelper.sin(pYaw);
      double d0 = this.x * (double)f + this.z * (double)f1;
      double d1 = this.y;
      double d2 = this.z * (double)f - this.x * (double)f1;
      return new Vector3d(d0, d1, d2);
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d zRot(float pRoll) {
      float f = MathHelper.cos(pRoll);
      float f1 = MathHelper.sin(pRoll);
      double d0 = this.x * (double)f + this.y * (double)f1;
      double d1 = this.y * (double)f - this.x * (double)f1;
      double d2 = this.z;
      return new Vector3d(d0, d1, d2);
   }

   /**
    * returns a Vec3d from given pitch and yaw degrees as Vec2f
    */
   public static Vector3d directionFromRotation(Vector2f pVec) {
      return directionFromRotation(pVec.x, pVec.y);
   }

   /**
    * returns a Vec3d from given pitch and yaw degrees
    */
   public static Vector3d directionFromRotation(float pPitch, float pYaw) {
      float f = MathHelper.cos(-pYaw * ((float)Math.PI / 180F) - (float)Math.PI);
      float f1 = MathHelper.sin(-pYaw * ((float)Math.PI / 180F) - (float)Math.PI);
      float f2 = -MathHelper.cos(-pPitch * ((float)Math.PI / 180F));
      float f3 = MathHelper.sin(-pPitch * ((float)Math.PI / 180F));
      return new Vector3d((double)(f1 * f2), (double)f3, (double)(f * f2));
   }

   public Vector3d align(EnumSet<Direction.Axis> pAxes) {
      double d0 = pAxes.contains(Direction.Axis.X) ? (double)MathHelper.floor(this.x) : this.x;
      double d1 = pAxes.contains(Direction.Axis.Y) ? (double)MathHelper.floor(this.y) : this.y;
      double d2 = pAxes.contains(Direction.Axis.Z) ? (double)MathHelper.floor(this.z) : this.z;
      return new Vector3d(d0, d1, d2);
   }

   public double get(Direction.Axis pAxis) {
      return pAxis.choose(this.x, this.y, this.z);
   }

   public final double x() {
      return this.x;
   }

   public final double y() {
      return this.y;
   }

   public final double z() {
      return this.z;
   }
}