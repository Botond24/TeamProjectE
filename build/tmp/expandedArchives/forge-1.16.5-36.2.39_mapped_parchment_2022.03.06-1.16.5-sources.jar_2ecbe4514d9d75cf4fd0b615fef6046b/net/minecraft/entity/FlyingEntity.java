package net.minecraft.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class FlyingEntity extends MobEntity {
   protected FlyingEntity(EntityType<? extends FlyingEntity> p_i48578_1_, World p_i48578_2_) {
      super(p_i48578_1_, p_i48578_2_);
   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      return false;
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
   }

   public void travel(Vector3d pTravelVector) {
      if (this.isInWater()) {
         this.moveRelative(0.02F, pTravelVector);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale((double)0.8F));
      } else if (this.isInLava()) {
         this.moveRelative(0.02F, pTravelVector);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
      } else {
         BlockPos ground = new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ());
         float f = 0.91F;
         if (this.onGround) {
            f = this.level.getBlockState(ground).getSlipperiness(this.level, ground, this) * 0.91F;
         }

         float f1 = 0.16277137F / (f * f * f);
         f = 0.91F;
         if (this.onGround) {
            f = this.level.getBlockState(ground).getSlipperiness(this.level, ground, this) * 0.91F;
         }

         this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, pTravelVector);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale((double)f));
      }

      this.calculateEntityAnimation(this, false);
   }

   /**
    * Returns true if this entity should move as if it were on a ladder (either because it's actually on a ladder, or
    * for AI reasons)
    */
   public boolean onClimbable() {
      return false;
   }
}
