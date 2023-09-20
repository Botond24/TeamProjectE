package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.RangedInteger;
import net.minecraft.world.server.ServerWorld;

public class RunSometimesTask<E extends LivingEntity> extends Task<E> {
   private boolean resetTicks;
   private boolean wasRunning;
   private final RangedInteger interval;
   private final Task<? super E> wrappedBehavior;
   private int ticksUntilNextStart;

   public RunSometimesTask(Task<? super E> p_i231530_1_, RangedInteger p_i231530_2_) {
      this(p_i231530_1_, false, p_i231530_2_);
   }

   public RunSometimesTask(Task<? super E> p_i231531_1_, boolean p_i231531_2_, RangedInteger p_i231531_3_) {
      super(p_i231531_1_.entryCondition);
      this.wrappedBehavior = p_i231531_1_;
      this.resetTicks = !p_i231531_2_;
      this.interval = p_i231531_3_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      if (!this.wrappedBehavior.checkExtraStartConditions(pLevel, pOwner)) {
         return false;
      } else {
         if (this.resetTicks) {
            this.resetTicksUntilNextStart(pLevel);
            this.resetTicks = false;
         }

         if (this.ticksUntilNextStart > 0) {
            --this.ticksUntilNextStart;
         }

         return !this.wasRunning && this.ticksUntilNextStart == 0;
      }
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      this.wrappedBehavior.start(pLevel, pEntity, pGameTime);
   }

   protected boolean canStillUse(ServerWorld pLevel, E pEntity, long pGameTime) {
      return this.wrappedBehavior.canStillUse(pLevel, pEntity, pGameTime);
   }

   protected void tick(ServerWorld pLevel, E pOwner, long pGameTime) {
      this.wrappedBehavior.tick(pLevel, pOwner, pGameTime);
      this.wasRunning = this.wrappedBehavior.getStatus() == Task.Status.RUNNING;
   }

   protected void stop(ServerWorld pLevel, E pEntity, long pGameTime) {
      this.resetTicksUntilNextStart(pLevel);
      this.wrappedBehavior.stop(pLevel, pEntity, pGameTime);
   }

   private void resetTicksUntilNextStart(ServerWorld p_233949_1_) {
      this.ticksUntilNextStart = this.interval.randomValue(p_233949_1_.random);
   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   public String toString() {
      return "RunSometimes: " + this.wrappedBehavior;
   }
}