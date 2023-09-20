package net.minecraft.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;

public abstract class TargetGoal extends Goal {
   /** The entity that this goal belongs to */
   protected final MobEntity mob;
   protected final boolean mustSee;
   private final boolean mustReach;
   private int reachCache;
   private int reachCacheTime;
   private int unseenTicks;
   protected LivingEntity targetMob;
   protected int unseenMemoryTicks = 60;

   public TargetGoal(MobEntity p_i50308_1_, boolean p_i50308_2_) {
      this(p_i50308_1_, p_i50308_2_, false);
   }

   public TargetGoal(MobEntity p_i50309_1_, boolean p_i50309_2_, boolean p_i50309_3_) {
      this.mob = p_i50309_1_;
      this.mustSee = p_i50309_2_;
      this.mustReach = p_i50309_3_;
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      LivingEntity livingentity = this.mob.getTarget();
      if (livingentity == null) {
         livingentity = this.targetMob;
      }

      if (livingentity == null) {
         return false;
      } else if (!livingentity.isAlive()) {
         return false;
      } else {
         Team team = this.mob.getTeam();
         Team team1 = livingentity.getTeam();
         if (team != null && team1 == team) {
            return false;
         } else {
            double d0 = this.getFollowDistance();
            if (this.mob.distanceToSqr(livingentity) > d0 * d0) {
               return false;
            } else {
               if (this.mustSee) {
                  if (this.mob.getSensing().canSee(livingentity)) {
                     this.unseenTicks = 0;
                  } else if (++this.unseenTicks > this.unseenMemoryTicks) {
                     return false;
                  }
               }

               if (livingentity instanceof PlayerEntity && ((PlayerEntity)livingentity).abilities.invulnerable) {
                  return false;
               } else {
                  this.mob.setTarget(livingentity);
                  return true;
               }
            }
         }
      }
   }

   protected double getFollowDistance() {
      return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.reachCache = 0;
      this.reachCacheTime = 0;
      this.unseenTicks = 0;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.mob.setTarget((LivingEntity)null);
      this.targetMob = null;
   }

   /**
    * checks if is is a suitable target
    */
   protected boolean canAttack(@Nullable LivingEntity pPotentialTarget, EntityPredicate pTargetPredicate) {
      if (pPotentialTarget == null) {
         return false;
      } else if (!pTargetPredicate.test(this.mob, pPotentialTarget)) {
         return false;
      } else if (!this.mob.isWithinRestriction(pPotentialTarget.blockPosition())) {
         return false;
      } else {
         if (this.mustReach) {
            if (--this.reachCacheTime <= 0) {
               this.reachCache = 0;
            }

            if (this.reachCache == 0) {
               this.reachCache = this.canReach(pPotentialTarget) ? 1 : 2;
            }

            if (this.reachCache == 2) {
               return false;
            }
         }

         return true;
      }
   }

   /**
    * Checks to see if this entity can find a short path to the given target.
    */
   private boolean canReach(LivingEntity pTarget) {
      this.reachCacheTime = 10 + this.mob.getRandom().nextInt(5);
      Path path = this.mob.getNavigation().createPath(pTarget, 0);
      if (path == null) {
         return false;
      } else {
         PathPoint pathpoint = path.getEndNode();
         if (pathpoint == null) {
            return false;
         } else {
            int i = pathpoint.x - MathHelper.floor(pTarget.getX());
            int j = pathpoint.z - MathHelper.floor(pTarget.getZ());
            return (double)(i * i + j * j) <= 2.25D;
         }
      }
   }

   public TargetGoal setUnseenMemoryTicks(int pUnseenMemoryTicks) {
      this.unseenMemoryTicks = pUnseenMemoryTicks;
      return this;
   }
}