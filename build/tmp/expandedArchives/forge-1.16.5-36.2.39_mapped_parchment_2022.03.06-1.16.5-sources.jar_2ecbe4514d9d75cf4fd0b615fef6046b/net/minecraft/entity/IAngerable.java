package net.minecraft.entity;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityPredicates;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public interface IAngerable {
   int getRemainingPersistentAngerTime();

   void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime);

   @Nullable
   UUID getPersistentAngerTarget();

   void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget);

   void startPersistentAngerTimer();

   default void addPersistentAngerSaveData(CompoundNBT pNbt) {
      pNbt.putInt("AngerTime", this.getRemainingPersistentAngerTime());
      if (this.getPersistentAngerTarget() != null) {
         pNbt.putUUID("AngryAt", this.getPersistentAngerTarget());
      }

   }

   default void readPersistentAngerSaveData(ServerWorld pServerLevel, CompoundNBT pCompound) {
      this.setRemainingPersistentAngerTime(pCompound.getInt("AngerTime"));
      if (!pCompound.hasUUID("AngryAt")) {
         this.setPersistentAngerTarget((UUID)null);
      } else {
         UUID uuid = pCompound.getUUID("AngryAt");
         this.setPersistentAngerTarget(uuid);
         Entity entity = pServerLevel.getEntity(uuid);
         if (entity != null) {
            if (entity instanceof MobEntity) {
               this.setLastHurtByMob((MobEntity)entity);
            }

            if (entity.getType() == EntityType.PLAYER) {
               this.setLastHurtByPlayer((PlayerEntity)entity);
            }

         }
      }
   }

   default void updatePersistentAnger(ServerWorld pServerLevel, boolean pUpdateAnger) {
      LivingEntity livingentity = this.getTarget();
      UUID uuid = this.getPersistentAngerTarget();
      if ((livingentity == null || livingentity.isDeadOrDying()) && uuid != null && pServerLevel.getEntity(uuid) instanceof MobEntity) {
         this.stopBeingAngry();
      } else {
         if (livingentity != null && !Objects.equals(uuid, livingentity.getUUID())) {
            this.setPersistentAngerTarget(livingentity.getUUID());
            this.startPersistentAngerTimer();
         }

         if (this.getRemainingPersistentAngerTime() > 0 && (livingentity == null || livingentity.getType() != EntityType.PLAYER || !pUpdateAnger)) {
            this.setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
            if (this.getRemainingPersistentAngerTime() == 0) {
               this.stopBeingAngry();
            }
         }

      }
   }

   default boolean isAngryAt(LivingEntity pTarget) {
      if (!EntityPredicates.ATTACK_ALLOWED.test(pTarget)) {
         return false;
      } else {
         return pTarget.getType() == EntityType.PLAYER && this.isAngryAtAllPlayers(pTarget.level) ? true : pTarget.getUUID().equals(this.getPersistentAngerTarget());
      }
   }

   default boolean isAngryAtAllPlayers(World pLevel) {
      return pLevel.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.isAngry() && this.getPersistentAngerTarget() == null;
   }

   default boolean isAngry() {
      return this.getRemainingPersistentAngerTime() > 0;
   }

   default void playerDied(PlayerEntity pPlayer) {
      if (pPlayer.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
         if (pPlayer.getUUID().equals(this.getPersistentAngerTarget())) {
            this.stopBeingAngry();
         }
      }
   }

   default void forgetCurrentTargetAndRefreshUniversalAnger() {
      this.stopBeingAngry();
      this.startPersistentAngerTimer();
   }

   default void stopBeingAngry() {
      this.setLastHurtByMob((LivingEntity)null);
      this.setPersistentAngerTarget((UUID)null);
      this.setTarget((LivingEntity)null);
      this.setRemainingPersistentAngerTime(0);
   }

   /**
    * Hint to AI tasks that we were attacked by the passed EntityLivingBase and should retaliate. Is not guaranteed to
    * change our actual active target (for example if we are currently busy attacking someone else)
    */
   void setLastHurtByMob(@Nullable LivingEntity pLivingEntity);

   void setLastHurtByPlayer(@Nullable PlayerEntity pPlayer);

   /**
    * Sets the active target the Task system uses for tracking
    */
   void setTarget(@Nullable LivingEntity pLivingEntity);

   /**
    * Gets the active target the Task system uses for tracking
    */
   @Nullable
   LivingEntity getTarget();
}