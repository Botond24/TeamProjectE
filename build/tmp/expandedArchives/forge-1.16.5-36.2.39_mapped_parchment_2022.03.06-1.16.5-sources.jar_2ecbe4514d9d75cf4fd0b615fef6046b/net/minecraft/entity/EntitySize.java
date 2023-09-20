package net.minecraft.entity;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class EntitySize {
   public final float width;
   public final float height;
   public final boolean fixed;

   public EntitySize(float pWidth, float pHeight, boolean pFixed) {
      this.width = pWidth;
      this.height = pHeight;
      this.fixed = pFixed;
   }

   public AxisAlignedBB makeBoundingBox(Vector3d pPos) {
      return this.makeBoundingBox(pPos.x, pPos.y, pPos.z);
   }

   public AxisAlignedBB makeBoundingBox(double pX, double pY, double pZ) {
      float f = this.width / 2.0F;
      float f1 = this.height;
      return new AxisAlignedBB(pX - (double)f, pY, pZ - (double)f, pX + (double)f, pY + (double)f1, pZ + (double)f);
   }

   public EntitySize scale(float pFactor) {
      return this.scale(pFactor, pFactor);
   }

   public EntitySize scale(float pWidthFactor, float pHeightFactor) {
      return !this.fixed && (pWidthFactor != 1.0F || pHeightFactor != 1.0F) ? scalable(this.width * pWidthFactor, this.height * pHeightFactor) : this;
   }

   public static EntitySize scalable(float pWidth, float pHeight) {
      return new EntitySize(pWidth, pHeight, false);
   }

   public static EntitySize fixed(float pWidth, float pHeight) {
      return new EntitySize(pWidth, pHeight, true);
   }

   public String toString() {
      return "EntityDimensions w=" + this.width + ", h=" + this.height + ", fixed=" + this.fixed;
   }
}