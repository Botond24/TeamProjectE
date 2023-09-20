package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TradeWithPlayerGoal extends Goal {
   private final AbstractVillagerEntity mob;

   public TradeWithPlayerGoal(AbstractVillagerEntity p_i50320_1_) {
      this.mob = p_i50320_1_;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (!this.mob.isAlive()) {
         return false;
      } else if (this.mob.isInWater()) {
         return false;
      } else if (!this.mob.isOnGround()) {
         return false;
      } else if (this.mob.hurtMarked) {
         return false;
      } else {
         PlayerEntity playerentity = this.mob.getTradingPlayer();
         if (playerentity == null) {
            return false;
         } else if (this.mob.distanceToSqr(playerentity) > 16.0D) {
            return false;
         } else {
            return playerentity.containerMenu != null;
         }
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.getNavigation().stop();
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.mob.setTradingPlayer((PlayerEntity)null);
   }
}