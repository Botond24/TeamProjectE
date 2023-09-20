package net.minecraft.entity.ai.goal;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.passive.fish.AbstractGroupFishEntity;

public class FollowSchoolLeaderGoal extends Goal {
   private final AbstractGroupFishEntity mob;
   private int timeToRecalcPath;
   private int nextStartTick;

   public FollowSchoolLeaderGoal(AbstractGroupFishEntity p_i49857_1_) {
      this.mob = p_i49857_1_;
      this.nextStartTick = this.nextStartTick(p_i49857_1_);
   }

   protected int nextStartTick(AbstractGroupFishEntity pTaskOwner) {
      return 200 + pTaskOwner.getRandom().nextInt(200) % 20;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.mob.hasFollowers()) {
         return false;
      } else if (this.mob.isFollower()) {
         return true;
      } else if (this.nextStartTick > 0) {
         --this.nextStartTick;
         return false;
      } else {
         this.nextStartTick = this.nextStartTick(this.mob);
         Predicate<AbstractGroupFishEntity> predicate = (p_212824_0_) -> {
            return p_212824_0_.canBeFollowed() || !p_212824_0_.isFollower();
         };
         List<AbstractGroupFishEntity> list = this.mob.level.getEntitiesOfClass(this.mob.getClass(), this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), predicate);
         AbstractGroupFishEntity abstractgroupfishentity = list.stream().filter(AbstractGroupFishEntity::canBeFollowed).findAny().orElse(this.mob);
         abstractgroupfishentity.addFollowers(list.stream().filter((p_212823_0_) -> {
            return !p_212823_0_.isFollower();
         }));
         return this.mob.isFollower();
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.mob.isFollower() && this.mob.inRangeOfLeader();
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
      this.mob.stopFollowing();
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = 10;
         this.mob.pathToLeader();
      }
   }
}