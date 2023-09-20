package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;

public class PrioritizedGoal extends Goal {
   private final Goal goal;
   private final int priority;
   private boolean isRunning;

   public PrioritizedGoal(int p_i50318_1_, Goal p_i50318_2_) {
      this.priority = p_i50318_1_;
      this.goal = p_i50318_2_;
   }

   public boolean canBeReplacedBy(PrioritizedGoal pOther) {
      return this.isInterruptable() && pOther.getPriority() < this.getPriority();
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.goal.canUse();
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.goal.canContinueToUse();
   }

   public boolean isInterruptable() {
      return this.goal.isInterruptable();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      if (!this.isRunning) {
         this.isRunning = true;
         this.goal.start();
      }
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      if (this.isRunning) {
         this.isRunning = false;
         this.goal.stop();
      }
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      this.goal.tick();
   }

   public void setFlags(EnumSet<Goal.Flag> pFlagSet) {
      this.goal.setFlags(pFlagSet);
   }

   public EnumSet<Goal.Flag> getFlags() {
      return this.goal.getFlags();
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public int getPriority() {
      return this.priority;
   }

   /**
    * Gets the private goal enclosed by this PrioritizedGoal. Call this rather than use an access transformer"
    */
   public Goal getGoal() {
      return this.goal;
   }

   public boolean equals(@Nullable Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ != null && this.getClass() == p_equals_1_.getClass() ? this.goal.equals(((PrioritizedGoal)p_equals_1_).goal) : false;
      }
   }

   public int hashCode() {
      return this.goal.hashCode();
   }
}