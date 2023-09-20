package net.minecraft.entity;

import java.util.function.Predicate;
import javax.annotation.Nullable;

public class EntityPredicate {
   public static final EntityPredicate DEFAULT = new EntityPredicate();
   private double range = -1.0D;
   private boolean allowInvulnerable;
   private boolean allowSameTeam;
   private boolean allowUnseeable;
   private boolean allowNonAttackable;
   private boolean testInvisible = true;
   private Predicate<LivingEntity> selector;

   public EntityPredicate range(double pDistance) {
      this.range = pDistance;
      return this;
   }

   public EntityPredicate allowInvulnerable() {
      this.allowInvulnerable = true;
      return this;
   }

   public EntityPredicate allowSameTeam() {
      this.allowSameTeam = true;
      return this;
   }

   public EntityPredicate allowUnseeable() {
      this.allowUnseeable = true;
      return this;
   }

   public EntityPredicate allowNonAttackable() {
      this.allowNonAttackable = true;
      return this;
   }

   public EntityPredicate ignoreInvisibilityTesting() {
      this.testInvisible = false;
      return this;
   }

   public EntityPredicate selector(@Nullable Predicate<LivingEntity> pCustomPredicate) {
      this.selector = pCustomPredicate;
      return this;
   }

   public boolean test(@Nullable LivingEntity pAttacker, LivingEntity pTarget) {
      if (pAttacker == pTarget) {
         return false;
      } else if (pTarget.isSpectator()) {
         return false;
      } else if (!pTarget.isAlive()) {
         return false;
      } else if (!this.allowInvulnerable && pTarget.isInvulnerable()) {
         return false;
      } else if (this.selector != null && !this.selector.test(pTarget)) {
         return false;
      } else {
         if (pAttacker != null) {
            if (!this.allowNonAttackable) {
               if (!pAttacker.canAttack(pTarget)) {
                  return false;
               }

               if (!pAttacker.canAttackType(pTarget.getType())) {
                  return false;
               }
            }

            if (!this.allowSameTeam && pAttacker.isAlliedTo(pTarget)) {
               return false;
            }

            if (this.range > 0.0D) {
               double d0 = this.testInvisible ? pTarget.getVisibilityPercent(pAttacker) : 1.0D;
               double d1 = Math.max(this.range * d0, 2.0D);
               double d2 = pAttacker.distanceToSqr(pTarget.getX(), pTarget.getY(), pTarget.getZ());
               if (d2 > d1 * d1) {
                  return false;
               }
            }

            if (!this.allowUnseeable && pAttacker instanceof MobEntity && !((MobEntity)pAttacker).getSensing().canSee(pTarget)) {
               return false;
            }
         }

         return true;
      }
   }
}