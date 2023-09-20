package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.vector.Vector3d;

public class HoverPhase extends Phase {
   private Vector3d targetLocation;

   public HoverPhase(EnderDragonEntity p_i46790_1_) {
      super(p_i46790_1_);
   }

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   public void doServerTick() {
      if (this.targetLocation == null) {
         this.targetLocation = this.dragon.position();
      }

   }

   public boolean isSitting() {
      return true;
   }

   /**
    * Called when this phase is set to active
    */
   public void begin() {
      this.targetLocation = null;
   }

   /**
    * Returns the maximum amount dragon may rise or fall during this phase
    */
   public float getFlySpeed() {
      return 1.0F;
   }

   /**
    * Returns the location the dragon is flying toward
    */
   @Nullable
   public Vector3d getFlyTargetLocation() {
      return this.targetLocation;
   }

   public PhaseType<HoverPhase> getPhase() {
      return PhaseType.HOVERING;
   }
}