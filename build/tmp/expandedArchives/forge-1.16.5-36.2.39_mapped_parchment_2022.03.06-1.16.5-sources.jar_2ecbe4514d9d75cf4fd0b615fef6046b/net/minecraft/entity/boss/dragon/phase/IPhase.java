package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public interface IPhase {
   boolean isSitting();

   /**
    * Generates particle effects appropriate to the phase (or sometimes sounds).
    * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
    */
   void doClientTick();

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   void doServerTick();

   void onCrystalDestroyed(EnderCrystalEntity pCrystal, BlockPos pPos, DamageSource pDmgSrc, @Nullable PlayerEntity pPlyr);

   /**
    * Called when this phase is set to active
    */
   void begin();

   void end();

   /**
    * Returns the maximum amount dragon may rise or fall during this phase
    */
   float getFlySpeed();

   float getTurnSpeed();

   PhaseType<? extends IPhase> getPhase();

   /**
    * Returns the location the dragon is flying toward
    */
   @Nullable
   Vector3d getFlyTargetLocation();

   float onHurt(DamageSource pDamageSource, float pAmount);
}