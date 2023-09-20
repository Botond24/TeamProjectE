package net.minecraft.entity.ai.goal;

import java.util.List;
import net.minecraft.entity.passive.AnimalEntity;

public class FollowParentGoal extends Goal {
   private final AnimalEntity animal;
   private AnimalEntity parent;
   private final double speedModifier;
   private int timeToRecalcPath;

   public FollowParentGoal(AnimalEntity p_i1626_1_, double p_i1626_2_) {
      this.animal = p_i1626_1_;
      this.speedModifier = p_i1626_2_;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.animal.getAge() >= 0) {
         return false;
      } else {
         List<AnimalEntity> list = this.animal.level.getEntitiesOfClass(this.animal.getClass(), this.animal.getBoundingBox().inflate(8.0D, 4.0D, 8.0D));
         AnimalEntity animalentity = null;
         double d0 = Double.MAX_VALUE;

         for(AnimalEntity animalentity1 : list) {
            if (animalentity1.getAge() >= 0) {
               double d1 = this.animal.distanceToSqr(animalentity1);
               if (!(d1 > d0)) {
                  d0 = d1;
                  animalentity = animalentity1;
               }
            }
         }

         if (animalentity == null) {
            return false;
         } else if (d0 < 9.0D) {
            return false;
         } else {
            this.parent = animalentity;
            return true;
         }
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      if (this.animal.getAge() >= 0) {
         return false;
      } else if (!this.parent.isAlive()) {
         return false;
      } else {
         double d0 = this.animal.distanceToSqr(this.parent);
         return !(d0 < 9.0D) && !(d0 > 256.0D);
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.timeToRecalcPath = 0;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.parent = null;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = 10;
         this.animal.getNavigation().moveTo(this.parent, this.speedModifier);
      }
   }
}