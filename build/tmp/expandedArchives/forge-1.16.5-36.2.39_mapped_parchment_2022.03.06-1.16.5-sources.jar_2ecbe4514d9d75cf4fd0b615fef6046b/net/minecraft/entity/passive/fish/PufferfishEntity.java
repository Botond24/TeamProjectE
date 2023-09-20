package net.minecraft.entity.passive.fish;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class PufferfishEntity extends AbstractFishEntity {
   private static final DataParameter<Integer> PUFF_STATE = EntityDataManager.defineId(PufferfishEntity.class, DataSerializers.INT);
   private int inflateCounter;
   private int deflateTimer;
   private static final Predicate<LivingEntity> NO_SPECTATORS_AND_NO_WATER_MOB = (p_210139_0_) -> {
      if (p_210139_0_ == null) {
         return false;
      } else if (!(p_210139_0_ instanceof PlayerEntity) || !p_210139_0_.isSpectator() && !((PlayerEntity)p_210139_0_).isCreative()) {
         return p_210139_0_.getMobType() != CreatureAttribute.WATER;
      } else {
         return false;
      }
   };

   public PufferfishEntity(EntityType<? extends PufferfishEntity> p_i50248_1_, World p_i50248_2_) {
      super(p_i50248_1_, p_i50248_2_);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(PUFF_STATE, 0);
   }

   public int getPuffState() {
      return this.entityData.get(PUFF_STATE);
   }

   public void setPuffState(int pPuffState) {
      this.entityData.set(PUFF_STATE, pPuffState);
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (PUFF_STATE.equals(pKey)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(pKey);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("PuffState", this.getPuffState());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setPuffState(pCompound.getInt("PuffState"));
   }

   protected ItemStack getBucketItemStack() {
      return new ItemStack(Items.PUFFERFISH_BUCKET);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new PufferfishEntity.PuffGoal(this));
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (!this.level.isClientSide && this.isAlive() && this.isEffectiveAi()) {
         if (this.inflateCounter > 0) {
            if (this.getPuffState() == 0) {
               this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getVoicePitch());
               this.setPuffState(1);
            } else if (this.inflateCounter > 40 && this.getPuffState() == 1) {
               this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getVoicePitch());
               this.setPuffState(2);
            }

            ++this.inflateCounter;
         } else if (this.getPuffState() != 0) {
            if (this.deflateTimer > 60 && this.getPuffState() == 2) {
               this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getVoicePitch());
               this.setPuffState(1);
            } else if (this.deflateTimer > 100 && this.getPuffState() == 1) {
               this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getVoicePitch());
               this.setPuffState(0);
            }

            ++this.deflateTimer;
         }
      }

      super.tick();
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.isAlive() && this.getPuffState() > 0) {
         for(MobEntity mobentity : this.level.getEntitiesOfClass(MobEntity.class, this.getBoundingBox().inflate(0.3D), NO_SPECTATORS_AND_NO_WATER_MOB)) {
            if (mobentity.isAlive()) {
               this.touch(mobentity);
            }
         }
      }

   }

   private void touch(MobEntity pMob) {
      int i = this.getPuffState();
      if (pMob.hurt(DamageSource.mobAttack(this), (float)(1 + i))) {
         pMob.addEffect(new EffectInstance(Effects.POISON, 60 * i, 0));
         this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
      }

   }

   /**
    * Called by a player entity when they collide with an entity
    */
   public void playerTouch(PlayerEntity pEntity) {
      int i = this.getPuffState();
      if (pEntity instanceof ServerPlayerEntity && i > 0 && pEntity.hurt(DamageSource.mobAttack(this), (float)(1 + i))) {
         if (!this.isSilent()) {
            ((ServerPlayerEntity)pEntity).connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.PUFFER_FISH_STING, 0.0F));
         }

         pEntity.addEffect(new EffectInstance(Effects.POISON, 60 * i, 0));
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.PUFFER_FISH_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PUFFER_FISH_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.PUFFER_FISH_HURT;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.PUFFER_FISH_FLOP;
   }

   public EntitySize getDimensions(Pose pPose) {
      return super.getDimensions(pPose).scale(getScale(this.getPuffState()));
   }

   private static float getScale(int pPuffState) {
      switch(pPuffState) {
      case 0:
         return 0.5F;
      case 1:
         return 0.7F;
      default:
         return 1.0F;
      }
   }

   static class PuffGoal extends Goal {
      private final PufferfishEntity fish;

      public PuffGoal(PufferfishEntity pFish) {
         this.fish = pFish;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         List<LivingEntity> list = this.fish.level.getEntitiesOfClass(LivingEntity.class, this.fish.getBoundingBox().inflate(2.0D), PufferfishEntity.NO_SPECTATORS_AND_NO_WATER_MOB);
         return !list.isEmpty();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.fish.inflateCounter = 1;
         this.fish.deflateTimer = 0;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.fish.inflateCounter = 0;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         List<LivingEntity> list = this.fish.level.getEntitiesOfClass(LivingEntity.class, this.fish.getBoundingBox().inflate(2.0D), PufferfishEntity.NO_SPECTATORS_AND_NO_WATER_MOB);
         return !list.isEmpty();
      }
   }
}