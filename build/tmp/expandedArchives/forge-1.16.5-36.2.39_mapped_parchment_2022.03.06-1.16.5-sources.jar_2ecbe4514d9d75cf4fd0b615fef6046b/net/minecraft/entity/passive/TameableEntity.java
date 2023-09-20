package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TameableEntity extends AnimalEntity {
   protected static final DataParameter<Byte> DATA_FLAGS_ID = EntityDataManager.defineId(TameableEntity.class, DataSerializers.BYTE);
   protected static final DataParameter<Optional<UUID>> DATA_OWNERUUID_ID = EntityDataManager.defineId(TameableEntity.class, DataSerializers.OPTIONAL_UUID);
   private boolean orderedToSit;

   protected TameableEntity(EntityType<? extends TameableEntity> p_i48574_1_, World p_i48574_2_) {
      super(p_i48574_1_, p_i48574_2_);
      this.reassessTameGoals();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_FLAGS_ID, (byte)0);
      this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.getOwnerUUID() != null) {
         pCompound.putUUID("Owner", this.getOwnerUUID());
      }

      pCompound.putBoolean("Sitting", this.orderedToSit);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      UUID uuid;
      if (pCompound.hasUUID("Owner")) {
         uuid = pCompound.getUUID("Owner");
      } else {
         String s = pCompound.getString("Owner");
         uuid = PreYggdrasilConverter.convertMobOwnerIfNecessary(this.getServer(), s);
      }

      if (uuid != null) {
         try {
            this.setOwnerUUID(uuid);
            this.setTame(true);
         } catch (Throwable throwable) {
            this.setTame(false);
         }
      }

      this.orderedToSit = pCompound.getBoolean("Sitting");
      this.setInSittingPose(this.orderedToSit);
   }

   public boolean canBeLeashed(PlayerEntity pPlayer) {
      return !this.isLeashed();
   }

   /**
    * Play the taming effect, will either be hearts or smoke depending on status
    */
   @OnlyIn(Dist.CLIENT)
   protected void spawnTamingParticles(boolean pTamed) {
      IParticleData iparticledata = ParticleTypes.HEART;
      if (!pTamed) {
         iparticledata = ParticleTypes.SMOKE;
      }

      for(int i = 0; i < 7; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level.addParticle(iparticledata, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 7) {
         this.spawnTamingParticles(true);
      } else if (pId == 6) {
         this.spawnTamingParticles(false);
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public boolean isTame() {
      return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
   }

   public void setTame(boolean pTamed) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (pTamed) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 4));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -5));
      }

      this.reassessTameGoals();
   }

   protected void reassessTameGoals() {
   }

   public boolean isInSittingPose() {
      return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
   }

   public void setInSittingPose(boolean pSitting) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (pSitting) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
      }

   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.entityData.get(DATA_OWNERUUID_ID).orElse((UUID)null);
   }

   public void setOwnerUUID(@Nullable UUID pUuid) {
      this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(pUuid));
   }

   public void tame(PlayerEntity pPlayer) {
      this.setTame(true);
      this.setOwnerUUID(pPlayer.getUUID());
      if (pPlayer instanceof ServerPlayerEntity) {
         CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayerEntity)pPlayer, this);
      }

   }

   @Nullable
   public LivingEntity getOwner() {
      try {
         UUID uuid = this.getOwnerUUID();
         return uuid == null ? null : this.level.getPlayerByUUID(uuid);
      } catch (IllegalArgumentException illegalargumentexception) {
         return null;
      }
   }

   public boolean canAttack(LivingEntity pTarget) {
      return this.isOwnedBy(pTarget) ? false : super.canAttack(pTarget);
   }

   public boolean isOwnedBy(LivingEntity pEntity) {
      return pEntity == this.getOwner();
   }

   public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
      return true;
   }

   public Team getTeam() {
      if (this.isTame()) {
         LivingEntity livingentity = this.getOwner();
         if (livingentity != null) {
            return livingentity.getTeam();
         }
      }

      return super.getTeam();
   }

   /**
    * Returns whether this Entity is on the same team as the given Entity.
    */
   public boolean isAlliedTo(Entity pEntity) {
      if (this.isTame()) {
         LivingEntity livingentity = this.getOwner();
         if (pEntity == livingentity) {
            return true;
         }

         if (livingentity != null) {
            return livingentity.isAlliedTo(pEntity);
         }
      }

      return super.isAlliedTo(pEntity);
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      if (!this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
         this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage(), Util.NIL_UUID);
      }

      super.die(pCause);
   }

   public boolean isOrderedToSit() {
      return this.orderedToSit;
   }

   public void setOrderedToSit(boolean pOrderedToSit) {
      this.orderedToSit = pOrderedToSit;
   }
}