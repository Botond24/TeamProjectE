package net.minecraft.entity.monster;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveTowardsRaidGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractRaiderEntity extends PatrollerEntity {
   protected static final DataParameter<Boolean> IS_CELEBRATING = EntityDataManager.defineId(AbstractRaiderEntity.class, DataSerializers.BOOLEAN);
   private static final Predicate<ItemEntity> ALLOWED_ITEMS = (p_213647_0_) -> {
      return !p_213647_0_.hasPickUpDelay() && p_213647_0_.isAlive() && ItemStack.matches(p_213647_0_.getItem(), Raid.getLeaderBannerInstance());
   };
   @Nullable
   protected Raid raid;
   private int wave;
   private boolean canJoinRaid;
   private int ticksOutsideRaid;

   protected AbstractRaiderEntity(EntityType<? extends AbstractRaiderEntity> p_i50143_1_, World p_i50143_2_) {
      super(p_i50143_1_, p_i50143_2_);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new AbstractRaiderEntity.PromoteLeaderGoal<>(this));
      this.goalSelector.addGoal(3, new MoveTowardsRaidGoal<>(this));
      this.goalSelector.addGoal(4, new AbstractRaiderEntity.InvadeHomeGoal(this, (double)1.05F, 1));
      this.goalSelector.addGoal(5, new AbstractRaiderEntity.CelebrateRaidLossGoal(this));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(IS_CELEBRATING, false);
   }

   public abstract void applyRaidBuffs(int pWave, boolean pUnusedFalse);

   public boolean canJoinRaid() {
      return this.canJoinRaid;
   }

   public void setCanJoinRaid(boolean pCanJoinRaid) {
      this.canJoinRaid = pCanJoinRaid;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.level instanceof ServerWorld && this.isAlive()) {
         Raid raid = this.getCurrentRaid();
         if (this.canJoinRaid()) {
            if (raid == null) {
               if (this.level.getGameTime() % 20L == 0L) {
                  Raid raid1 = ((ServerWorld)this.level).getRaidAt(this.blockPosition());
                  if (raid1 != null && RaidManager.canJoinRaid(this, raid1)) {
                     raid1.joinRaid(raid1.getGroupsSpawned(), this, (BlockPos)null, true);
                  }
               }
            } else {
               LivingEntity livingentity = this.getTarget();
               if (livingentity != null && (livingentity.getType() == EntityType.PLAYER || livingentity.getType() == EntityType.IRON_GOLEM)) {
                  this.noActionTime = 0;
               }
            }
         }
      }

      super.aiStep();
   }

   protected void updateNoActionTime() {
      this.noActionTime += 2;
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      if (this.level instanceof ServerWorld) {
         Entity entity = pCause.getEntity();
         Raid raid = this.getCurrentRaid();
         if (raid != null) {
            if (this.isPatrolLeader()) {
               raid.removeLeader(this.getWave());
            }

            if (entity != null && entity.getType() == EntityType.PLAYER) {
               raid.addHeroOfTheVillage(entity);
            }

            raid.removeFromRaid(this, false);
         }

         if (this.isPatrolLeader() && raid == null && ((ServerWorld)this.level).getRaidAt(this.blockPosition()) == null) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.HEAD);
            PlayerEntity playerentity = null;
            if (entity instanceof PlayerEntity) {
               playerentity = (PlayerEntity)entity;
            } else if (entity instanceof WolfEntity) {
               WolfEntity wolfentity = (WolfEntity)entity;
               LivingEntity livingentity = wolfentity.getOwner();
               if (wolfentity.isTame() && livingentity instanceof PlayerEntity) {
                  playerentity = (PlayerEntity)livingentity;
               }
            }

            if (!itemstack.isEmpty() && ItemStack.matches(itemstack, Raid.getLeaderBannerInstance()) && playerentity != null) {
               EffectInstance effectinstance1 = playerentity.getEffect(Effects.BAD_OMEN);
               int i = 1;
               if (effectinstance1 != null) {
                  i += effectinstance1.getAmplifier();
                  playerentity.removeEffectNoUpdate(Effects.BAD_OMEN);
               } else {
                  --i;
               }

               i = MathHelper.clamp(i, 0, 4);
               EffectInstance effectinstance = new EffectInstance(Effects.BAD_OMEN, 120000, i, false, false, true);
               if (!this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                  playerentity.addEffect(effectinstance);
               }
            }
         }
      }

      super.die(pCause);
   }

   public boolean canJoinPatrol() {
      return !this.hasActiveRaid();
   }

   public void setCurrentRaid(@Nullable Raid pRaid) {
      this.raid = pRaid;
   }

   @Nullable
   public Raid getCurrentRaid() {
      return this.raid;
   }

   public boolean hasActiveRaid() {
      return this.getCurrentRaid() != null && this.getCurrentRaid().isActive();
   }

   public void setWave(int pWave) {
      this.wave = pWave;
   }

   public int getWave() {
      return this.wave;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isCelebrating() {
      return this.entityData.get(IS_CELEBRATING);
   }

   public void setCelebrating(boolean pCelebrating) {
      this.entityData.set(IS_CELEBRATING, pCelebrating);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Wave", this.wave);
      pCompound.putBoolean("CanJoinRaid", this.canJoinRaid);
      if (this.raid != null) {
         pCompound.putInt("RaidId", this.raid.getId());
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.wave = pCompound.getInt("Wave");
      this.canJoinRaid = pCompound.getBoolean("CanJoinRaid");
      if (pCompound.contains("RaidId", 3)) {
         if (this.level instanceof ServerWorld) {
            this.raid = ((ServerWorld)this.level).getRaids().get(pCompound.getInt("RaidId"));
         }

         if (this.raid != null) {
            this.raid.addWaveMob(this.wave, this, false);
            if (this.isPatrolLeader()) {
               this.raid.setLeader(this.wave, this);
            }
         }
      }

   }

   /**
    * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
    * better.
    */
   protected void pickUpItem(ItemEntity pItemEntity) {
      ItemStack itemstack = pItemEntity.getItem();
      boolean flag = this.hasActiveRaid() && this.getCurrentRaid().getLeader(this.getWave()) != null;
      if (this.hasActiveRaid() && !flag && ItemStack.matches(itemstack, Raid.getLeaderBannerInstance())) {
         EquipmentSlotType equipmentslottype = EquipmentSlotType.HEAD;
         ItemStack itemstack1 = this.getItemBySlot(equipmentslottype);
         double d0 = (double)this.getEquipmentDropChance(equipmentslottype);
         if (!itemstack1.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d0) {
            this.spawnAtLocation(itemstack1);
         }

         this.onItemPickup(pItemEntity);
         this.setItemSlot(equipmentslottype, itemstack);
         this.take(pItemEntity, itemstack.getCount());
         pItemEntity.remove();
         this.getCurrentRaid().setLeader(this.getWave(), this);
         this.setPatrolLeader(true);
      } else {
         super.pickUpItem(pItemEntity);
      }

   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return this.getCurrentRaid() == null ? super.removeWhenFarAway(pDistanceToClosestPlayer) : false;
   }

   public boolean requiresCustomPersistence() {
      return super.requiresCustomPersistence() || this.getCurrentRaid() != null;
   }

   public int getTicksOutsideRaid() {
      return this.ticksOutsideRaid;
   }

   public void setTicksOutsideRaid(int pTicksOutsideRaid) {
      this.ticksOutsideRaid = pTicksOutsideRaid;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.hasActiveRaid()) {
         this.getCurrentRaid().updateBossbar();
      }

      return super.hurt(pSource, pAmount);
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      this.setCanJoinRaid(this.getType() != EntityType.WITCH || pReason != SpawnReason.NATURAL);
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public abstract SoundEvent getCelebrateSound();

   public class CelebrateRaidLossGoal extends Goal {
      private final AbstractRaiderEntity mob;

      CelebrateRaidLossGoal(AbstractRaiderEntity pMob) {
         this.mob = pMob;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         Raid raid = this.mob.getCurrentRaid();
         return this.mob.isAlive() && this.mob.getTarget() == null && raid != null && raid.isLoss();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.mob.setCelebrating(true);
         super.start();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.mob.setCelebrating(false);
         super.stop();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (!this.mob.isSilent() && this.mob.random.nextInt(100) == 0) {
            AbstractRaiderEntity.this.playSound(AbstractRaiderEntity.this.getCelebrateSound(), AbstractRaiderEntity.this.getSoundVolume(), AbstractRaiderEntity.this.getVoicePitch());
         }

         if (!this.mob.isPassenger() && this.mob.random.nextInt(50) == 0) {
            this.mob.getJumpControl().jump();
         }

         super.tick();
      }
   }

   public class FindTargetGoal extends Goal {
      private final AbstractRaiderEntity mob;
      private final float hostileRadiusSqr;
      public final EntityPredicate shoutTargeting = (new EntityPredicate()).range(8.0D).allowNonAttackable().allowInvulnerable().allowSameTeam().allowUnseeable().ignoreInvisibilityTesting();

      public FindTargetGoal(AbstractIllagerEntity pMob, float pHostileRadiusSquare) {
         this.mob = pMob;
         this.hostileRadiusSqr = pHostileRadiusSquare * pHostileRadiusSquare;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         LivingEntity livingentity = this.mob.getLastHurtByMob();
         return this.mob.getCurrentRaid() == null && this.mob.isPatrolling() && this.mob.getTarget() != null && !this.mob.isAggressive() && (livingentity == null || livingentity.getType() != EntityType.PLAYER);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.mob.getNavigation().stop();

         for(AbstractRaiderEntity abstractraiderentity : this.mob.level.getNearbyEntities(AbstractRaiderEntity.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D))) {
            abstractraiderentity.setTarget(this.mob.getTarget());
         }

      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         super.stop();
         LivingEntity livingentity = this.mob.getTarget();
         if (livingentity != null) {
            for(AbstractRaiderEntity abstractraiderentity : this.mob.level.getNearbyEntities(AbstractRaiderEntity.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D))) {
               abstractraiderentity.setTarget(livingentity);
               abstractraiderentity.setAggressive(true);
            }

            this.mob.setAggressive(true);
         }

      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = this.mob.getTarget();
         if (livingentity != null) {
            if (this.mob.distanceToSqr(livingentity) > (double)this.hostileRadiusSqr) {
               this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
               if (this.mob.random.nextInt(50) == 0) {
                  this.mob.playAmbientSound();
               }
            } else {
               this.mob.setAggressive(true);
            }

            super.tick();
         }
      }
   }

   static class InvadeHomeGoal extends Goal {
      private final AbstractRaiderEntity raider;
      private final double speedModifier;
      private BlockPos poiPos;
      private final List<BlockPos> visited = Lists.newArrayList();
      private final int distanceToPoi;
      private boolean stuck;

      public InvadeHomeGoal(AbstractRaiderEntity pRaider, double pSpeedModifier, int pDistanceToPoi) {
         this.raider = pRaider;
         this.speedModifier = pSpeedModifier;
         this.distanceToPoi = pDistanceToPoi;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         this.updateVisited();
         return this.isValidRaid() && this.hasSuitablePoi() && this.raider.getTarget() == null;
      }

      private boolean isValidRaid() {
         return this.raider.hasActiveRaid() && !this.raider.getCurrentRaid().isOver();
      }

      private boolean hasSuitablePoi() {
         ServerWorld serverworld = (ServerWorld)this.raider.level;
         BlockPos blockpos = this.raider.blockPosition();
         Optional<BlockPos> optional = serverworld.getPoiManager().getRandom((p_220859_0_) -> {
            return p_220859_0_ == PointOfInterestType.HOME;
         }, this::hasNotVisited, PointOfInterestManager.Status.ANY, blockpos, 48, this.raider.random);
         if (!optional.isPresent()) {
            return false;
         } else {
            this.poiPos = optional.get().immutable();
            return true;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         if (this.raider.getNavigation().isDone()) {
            return false;
         } else {
            return this.raider.getTarget() == null && !this.poiPos.closerThan(this.raider.position(), (double)(this.raider.getBbWidth() + (float)this.distanceToPoi)) && !this.stuck;
         }
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         if (this.poiPos.closerThan(this.raider.position(), (double)this.distanceToPoi)) {
            this.visited.add(this.poiPos);
         }

      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.raider.setNoActionTime(0);
         this.raider.getNavigation().moveTo((double)this.poiPos.getX(), (double)this.poiPos.getY(), (double)this.poiPos.getZ(), this.speedModifier);
         this.stuck = false;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.raider.getNavigation().isDone()) {
            Vector3d vector3d = Vector3d.atBottomCenterOf(this.poiPos);
            Vector3d vector3d1 = RandomPositionGenerator.getPosTowards(this.raider, 16, 7, vector3d, (double)((float)Math.PI / 10F));
            if (vector3d1 == null) {
               vector3d1 = RandomPositionGenerator.getPosTowards(this.raider, 8, 7, vector3d);
            }

            if (vector3d1 == null) {
               this.stuck = true;
               return;
            }

            this.raider.getNavigation().moveTo(vector3d1.x, vector3d1.y, vector3d1.z, this.speedModifier);
         }

      }

      private boolean hasNotVisited(BlockPos p_220860_1_) {
         for(BlockPos blockpos : this.visited) {
            if (Objects.equals(p_220860_1_, blockpos)) {
               return false;
            }
         }

         return true;
      }

      private void updateVisited() {
         if (this.visited.size() > 2) {
            this.visited.remove(0);
         }

      }
   }

   public class PromoteLeaderGoal<T extends AbstractRaiderEntity> extends Goal {
      private final T mob;

      public PromoteLeaderGoal(T pMob) {
         this.mob = pMob;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         Raid raid = this.mob.getCurrentRaid();
         if (this.mob.hasActiveRaid() && !this.mob.getCurrentRaid().isOver() && this.mob.canBeLeader() && !ItemStack.matches(this.mob.getItemBySlot(EquipmentSlotType.HEAD), Raid.getLeaderBannerInstance())) {
            AbstractRaiderEntity abstractraiderentity = raid.getLeader(this.mob.getWave());
            if (abstractraiderentity == null || !abstractraiderentity.isAlive()) {
               List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(16.0D, 8.0D, 16.0D), AbstractRaiderEntity.ALLOWED_ITEMS);
               if (!list.isEmpty()) {
                  return this.mob.getNavigation().moveTo(list.get(0), (double)1.15F);
               }
            }

            return false;
         } else {
            return false;
         }
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.mob.getNavigation().getTargetPos().closerThan(this.mob.position(), 1.414D)) {
            List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(4.0D, 4.0D, 4.0D), AbstractRaiderEntity.ALLOWED_ITEMS);
            if (!list.isEmpty()) {
               this.mob.pickUpItem(list.get(0));
            }
         }

      }
   }
}