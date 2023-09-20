package net.minecraft.entity.ai.controller;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LookController {
   protected final MobEntity mob;
   protected float yMaxRotSpeed;
   protected float xMaxRotAngle;
   protected boolean hasWanted;
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;

   public LookController(MobEntity p_i1613_1_) {
      this.mob = p_i1613_1_;
   }

   public void setLookAt(Vector3d pLookVector) {
      this.setLookAt(pLookVector.x, pLookVector.y, pLookVector.z);
   }

   /**
    * Sets position to look at using entity
    */
   public void setLookAt(Entity pEntity, float pDeltaYaw, float pDeltaPitch) {
      this.setLookAt(pEntity.getX(), getWantedY(pEntity), pEntity.getZ(), pDeltaYaw, pDeltaPitch);
   }

   public void setLookAt(double pX, double pY, double pZ) {
      this.setLookAt(pX, pY, pZ, (float)this.mob.getHeadRotSpeed(), (float)this.mob.getMaxHeadXRot());
   }

   /**
    * Sets position to look at
    */
   public void setLookAt(double pX, double pY, double pZ, float pDeltaYaw, float pDeltaPitch) {
      this.wantedX = pX;
      this.wantedY = pY;
      this.wantedZ = pZ;
      this.yMaxRotSpeed = pDeltaYaw;
      this.xMaxRotAngle = pDeltaPitch;
      this.hasWanted = true;
   }

   /**
    * Updates look
    */
   public void tick() {
      if (this.resetXRotOnTick()) {
         this.mob.xRot = 0.0F;
      }

      if (this.hasWanted) {
         this.hasWanted = false;
         this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.getYRotD(), this.yMaxRotSpeed);
         this.mob.xRot = this.rotateTowards(this.mob.xRot, this.getXRotD(), this.xMaxRotAngle);
      } else {
         this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
      }

      if (!this.mob.getNavigation().isDone()) {
         this.mob.yHeadRot = MathHelper.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float)this.mob.getMaxHeadYRot());
      }

   }

   protected boolean resetXRotOnTick() {
      return true;
   }

   public boolean isHasWanted() {
      return this.hasWanted;
   }

   public double getWantedX() {
      return this.wantedX;
   }

   public double getWantedY() {
      return this.wantedY;
   }

   public double getWantedZ() {
      return this.wantedZ;
   }

   protected float getXRotD() {
      double d0 = this.wantedX - this.mob.getX();
      double d1 = this.wantedY - this.mob.getEyeY();
      double d2 = this.wantedZ - this.mob.getZ();
      double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
      return (float)(-(MathHelper.atan2(d1, d3) * (double)(180F / (float)Math.PI)));
   }

   protected float getYRotD() {
      double d0 = this.wantedX - this.mob.getX();
      double d1 = this.wantedZ - this.mob.getZ();
      return (float)(MathHelper.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
   }

   /**
    * Rotate as much as possible from {@code from} to {@code to} within the bounds of {@code maxDelta}
    */
   protected float rotateTowards(float pFrom, float pTo, float pMaxDelta) {
      float f = MathHelper.degreesDifference(pFrom, pTo);
      float f1 = MathHelper.clamp(f, -pMaxDelta, pMaxDelta);
      return pFrom + f1;
   }

   private static double getWantedY(Entity pEntity) {
      return pEntity instanceof LivingEntity ? pEntity.getEyeY() : (pEntity.getBoundingBox().minY + pEntity.getBoundingBox().maxY) / 2.0D;
   }
}