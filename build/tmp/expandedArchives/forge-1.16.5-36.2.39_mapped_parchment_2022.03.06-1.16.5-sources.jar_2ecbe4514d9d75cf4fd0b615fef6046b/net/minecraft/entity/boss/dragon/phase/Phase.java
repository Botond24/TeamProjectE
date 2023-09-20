package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public abstract class Phase implements IPhase {
   protected final EnderDragonEntity dragon;

   public Phase(EnderDragonEntity pDragon) {
      this.dragon = pDragon;
   }

   public boolean isSitting() {
      return false;
   }

   /**
    * Generates particle effects appropriate to the phase (or sometimes sounds).
    * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
    */
   public void doClientTick() {
   }

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   public void doServerTick() {
   }

   public void onCrystalDestroyed(EnderCrystalEntity pCrystal, BlockPos pPos, DamageSource pDmgSrc, @Nullable PlayerEntity pPlyr) {
   }

   /**
    * Called when this phase is set to active
    */
   public void begin() {
   }

   public void end() {
   }

   /**
    * Returns the maximum amount dragon may rise or fall during this phase
    */
   public float getFlySpeed() {
      return 0.6F;
   }

   /**
    * Returns the location the dragon is flying toward
    */
   @Nullable
   public Vector3d getFlyTargetLocation() {
      return null;
   }

   public float onHurt(DamageSource pDamageSource, float pAmount) {
      return pAmount;
   }

   public float getTurnSpeed() {
      float f = MathHelper.sqrt(Entity.getHorizontalDistanceSqr(this.dragon.getDeltaMovement())) + 1.0F;
      float f1 = Math.min(f, 40.0F);
      return 0.7F / f1 / f;
   }
}