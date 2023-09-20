package net.minecraft.entity.passive.horse;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TraderLlamaEntity extends LlamaEntity {
   private int despawnDelay = 47999;

   public TraderLlamaEntity(EntityType<? extends TraderLlamaEntity> p_i50234_1_, World p_i50234_2_) {
      super(p_i50234_1_, p_i50234_2_);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isTraderLlama() {
      return true;
   }

   protected LlamaEntity makeBabyLlama() {
      return EntityType.TRADER_LLAMA.create(this.level);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("DespawnDelay", this.despawnDelay);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("DespawnDelay", 99)) {
         this.despawnDelay = pCompound.getInt("DespawnDelay");
      }

   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
      this.targetSelector.addGoal(1, new TraderLlamaEntity.FollowTraderGoal(this));
   }

   protected void doPlayerRide(PlayerEntity pPlayer) {
      Entity entity = this.getLeashHolder();
      if (!(entity instanceof WanderingTraderEntity)) {
         super.doPlayerRide(pPlayer);
      }
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (!this.level.isClientSide) {
         this.maybeDespawn();
      }

   }

   private void maybeDespawn() {
      if (this.canDespawn()) {
         this.despawnDelay = this.isLeashedToWanderingTrader() ? ((WanderingTraderEntity)this.getLeashHolder()).getDespawnDelay() - 1 : this.despawnDelay - 1;
         if (this.despawnDelay <= 0) {
            this.dropLeash(true, false);
            this.remove();
         }

      }
   }

   private boolean canDespawn() {
      return !this.isTamed() && !this.isLeashedToSomethingOtherThanTheWanderingTrader() && !this.hasOnePlayerPassenger();
   }

   private boolean isLeashedToWanderingTrader() {
      return this.getLeashHolder() instanceof WanderingTraderEntity;
   }

   private boolean isLeashedToSomethingOtherThanTheWanderingTrader() {
      return this.isLeashed() && !this.isLeashedToWanderingTrader();
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      if (pReason == SpawnReason.EVENT) {
         this.setAge(0);
      }

      if (pSpawnData == null) {
         pSpawnData = new AgeableEntity.AgeableData(false);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public class FollowTraderGoal extends TargetGoal {
      private final LlamaEntity llama;
      private LivingEntity ownerLastHurtBy;
      private int timestamp;

      public FollowTraderGoal(LlamaEntity pLlama) {
         super(pLlama, false);
         this.llama = pLlama;
         this.setFlags(EnumSet.of(Goal.Flag.TARGET));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (!this.llama.isLeashed()) {
            return false;
         } else {
            Entity entity = this.llama.getLeashHolder();
            if (!(entity instanceof WanderingTraderEntity)) {
               return false;
            } else {
               WanderingTraderEntity wanderingtraderentity = (WanderingTraderEntity)entity;
               this.ownerLastHurtBy = wanderingtraderentity.getLastHurtByMob();
               int i = wanderingtraderentity.getLastHurtByMobTimestamp();
               return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, EntityPredicate.DEFAULT);
            }
         }
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.mob.setTarget(this.ownerLastHurtBy);
         Entity entity = this.llama.getLeashHolder();
         if (entity instanceof WanderingTraderEntity) {
            this.timestamp = ((WanderingTraderEntity)entity).getLastHurtByMobTimestamp();
         }

         super.start();
      }
   }
}