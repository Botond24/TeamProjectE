package net.minecraft.util;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.Difficulty;

public final class EntityPredicates {
   public static final Predicate<Entity> ENTITY_STILL_ALIVE = Entity::isAlive;
   public static final Predicate<LivingEntity> LIVING_ENTITY_STILL_ALIVE = LivingEntity::isAlive;
   /** Selects only entities which are neither ridden by anything nor ride on anything */
   public static final Predicate<Entity> ENTITY_NOT_BEING_RIDDEN = (p_200821_0_) -> {
      return p_200821_0_.isAlive() && !p_200821_0_.isVehicle() && !p_200821_0_.isPassenger();
   };
   public static final Predicate<Entity> CONTAINER_ENTITY_SELECTOR = (p_200822_0_) -> {
      return p_200822_0_ instanceof IInventory && p_200822_0_.isAlive();
   };
   public static final Predicate<Entity> NO_CREATIVE_OR_SPECTATOR = (p_200824_0_) -> {
      return !(p_200824_0_ instanceof PlayerEntity) || !p_200824_0_.isSpectator() && !((PlayerEntity)p_200824_0_).isCreative();
   };
   public static final Predicate<Entity> ATTACK_ALLOWED = (p_200818_0_) -> {
      return !(p_200818_0_ instanceof PlayerEntity) || !p_200818_0_.isSpectator() && !((PlayerEntity)p_200818_0_).isCreative() && p_200818_0_.level.getDifficulty() != Difficulty.PEACEFUL;
   };
   /** Selects entities which are either not players or players that are not spectating */
   public static final Predicate<Entity> NO_SPECTATORS = (p_233587_0_) -> {
      return !p_233587_0_.isSpectator();
   };

   public static Predicate<Entity> withinDistance(double pX, double pY, double pZ, double pRange) {
      double d0 = pRange * pRange;
      return (p_233584_8_) -> {
         return p_233584_8_ != null && p_233584_8_.distanceToSqr(pX, pY, pZ) <= d0;
      };
   }

   public static Predicate<Entity> pushableBy(Entity pEntity) {
      Team team = pEntity.getTeam();
      Team.CollisionRule team$collisionrule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
      return (Predicate<Entity>)(team$collisionrule == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : NO_SPECTATORS.and((p_233586_3_) -> {
         if (!p_233586_3_.isPushable()) {
            return false;
         } else if (!pEntity.level.isClientSide || p_233586_3_ instanceof PlayerEntity && ((PlayerEntity)p_233586_3_).isLocalPlayer()) {
            Team team1 = p_233586_3_.getTeam();
            Team.CollisionRule team$collisionrule1 = team1 == null ? Team.CollisionRule.ALWAYS : team1.getCollisionRule();
            if (team$collisionrule1 == Team.CollisionRule.NEVER) {
               return false;
            } else {
               boolean flag = team != null && team.isAlliedTo(team1);
               if ((team$collisionrule == Team.CollisionRule.PUSH_OWN_TEAM || team$collisionrule1 == Team.CollisionRule.PUSH_OWN_TEAM) && flag) {
                  return false;
               } else {
                  return team$collisionrule != Team.CollisionRule.PUSH_OTHER_TEAMS && team$collisionrule1 != Team.CollisionRule.PUSH_OTHER_TEAMS || flag;
               }
            }
         } else {
            return false;
         }
      }));
   }

   public static Predicate<Entity> notRiding(Entity pEntity) {
      return (p_233585_1_) -> {
         while(true) {
            if (p_233585_1_.isPassenger()) {
               p_233585_1_ = p_233585_1_.getVehicle();
               if (p_233585_1_ != pEntity) {
                  continue;
               }

               return false;
            }

            return true;
         }
      };
   }

   public static class ArmoredMob implements Predicate<Entity> {
      private final ItemStack itemStack;

      public ArmoredMob(ItemStack pStack) {
         this.itemStack = pStack;
      }

      public boolean test(@Nullable Entity p_test_1_) {
         if (!p_test_1_.isAlive()) {
            return false;
         } else if (!(p_test_1_ instanceof LivingEntity)) {
            return false;
         } else {
            LivingEntity livingentity = (LivingEntity)p_test_1_;
            return livingentity.canTakeItem(this.itemStack);
         }
      }
   }
}