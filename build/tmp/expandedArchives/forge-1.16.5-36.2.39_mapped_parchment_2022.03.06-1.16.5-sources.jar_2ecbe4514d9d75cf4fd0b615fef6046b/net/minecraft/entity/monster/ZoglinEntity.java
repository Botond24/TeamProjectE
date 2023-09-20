package net.minecraft.entity.monster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.AttackTargetTask;
import net.minecraft.entity.ai.brain.task.DummyTask;
import net.minecraft.entity.ai.brain.task.FindNewAttackTargetTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.RunSometimesTask;
import net.minecraft.entity.ai.brain.task.SupplementedTask;
import net.minecraft.entity.ai.brain.task.WalkRandomlyTask;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ZoglinEntity extends MonsterEntity implements IMob, IFlinging {
   private static final DataParameter<Boolean> DATA_BABY_ID = EntityDataManager.defineId(ZoglinEntity.class, DataSerializers.BOOLEAN);
   private int attackAnimationRemainingTicks;
   protected static final ImmutableList<? extends SensorType<? extends Sensor<? super ZoglinEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
   protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);

   public ZoglinEntity(EntityType<? extends ZoglinEntity> p_i231566_1_, World p_i231566_2_) {
      super(p_i231566_1_, p_i231566_2_);
      this.xpReward = 5;
   }

   protected Brain.BrainCodec<ZoglinEntity> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      Brain<ZoglinEntity> brain = this.brainProvider().makeBrain(pDynamic);
      initCoreActivity(brain);
      initIdleActivity(brain);
      initFightActivity(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.useDefaultActivity();
      return brain;
   }

   private static void initCoreActivity(Brain<ZoglinEntity> pBrain) {
      pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookTask(45, 90), new WalkToTargetTask()));
   }

   private static void initIdleActivity(Brain<ZoglinEntity> pBrain) {
      pBrain.addActivity(Activity.IDLE, 10, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super ZoglinEntity>>of(new ForgetAttackTargetTask<>(ZoglinEntity::findNearestValidAttackTarget), new RunSometimesTask(new LookAtEntityTask(8.0F), RangedInteger.of(30, 60)), new FirstShuffledTask(ImmutableList.of(Pair.of(new WalkRandomlyTask(0.4F), 2), Pair.of(new WalkTowardsLookTargetTask(0.4F, 3), 2), Pair.of(new DummyTask(30, 60), 1)))));
   }

   private static void initFightActivity(Brain<ZoglinEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super ZoglinEntity>>of(new MoveToTargetTask(1.0F), new SupplementedTask<>(ZoglinEntity::isAdult, new AttackTargetTask(40)), new SupplementedTask<>(ZoglinEntity::isBaby, new AttackTargetTask(15)), new FindNewAttackTargetTask()), MemoryModuleType.ATTACK_TARGET);
   }

   private Optional<? extends LivingEntity> findNearestValidAttackTarget() {
      return this.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of()).stream().filter(ZoglinEntity::isTargetable).findFirst();
   }

   private static boolean isTargetable(LivingEntity p_234337_0_) {
      EntityType<?> entitytype = p_234337_0_.getType();
      return entitytype != EntityType.ZOGLIN && entitytype != EntityType.CREEPER && EntityPredicates.ATTACK_ALLOWED.test(p_234337_0_);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BABY_ID, false);
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_BABY_ID.equals(pKey)) {
         this.refreshDimensions();
      }

   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.KNOCKBACK_RESISTANCE, (double)0.6F).add(Attributes.ATTACK_KNOCKBACK, 1.0D).add(Attributes.ATTACK_DAMAGE, 6.0D);
   }

   public boolean isAdult() {
      return !this.isBaby();
   }

   public boolean doHurtTarget(Entity pEntity) {
      if (!(pEntity instanceof LivingEntity)) {
         return false;
      } else {
         this.attackAnimationRemainingTicks = 10;
         this.level.broadcastEntityEvent(this, (byte)4);
         this.playSound(SoundEvents.ZOGLIN_ATTACK, 1.0F, this.getVoicePitch());
         return IFlinging.hurtAndThrowTarget(this, (LivingEntity)pEntity);
      }
   }

   public boolean canBeLeashed(PlayerEntity pPlayer) {
      return !this.isLeashed();
   }

   protected void blockedByShield(LivingEntity pDefender) {
      if (!this.isBaby()) {
         IFlinging.throwTarget(this, pDefender);
      }

   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return (double)this.getBbHeight() - (this.isBaby() ? 0.2D : 0.15D);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      boolean flag = super.hurt(pSource, pAmount);
      if (this.level.isClientSide) {
         return false;
      } else if (flag && pSource.getEntity() instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)pSource.getEntity();
         if (EntityPredicates.ATTACK_ALLOWED.test(livingentity) && !BrainUtil.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(this, livingentity, 4.0D)) {
            this.setAttackTarget(livingentity);
         }

         return flag;
      } else {
         return flag;
      }
   }

   private void setAttackTarget(LivingEntity pTarget) {
      this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      this.brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, pTarget, 200L);
   }

   public Brain<ZoglinEntity> getBrain() {
      return (Brain<ZoglinEntity>)super.getBrain();
   }

   protected void updateActivity() {
      Activity activity = this.brain.getActiveNonCoreActivity().orElse((Activity)null);
      this.brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity activity1 = this.brain.getActiveNonCoreActivity().orElse((Activity)null);
      if (activity1 == Activity.FIGHT && activity != Activity.FIGHT) {
         this.playAngrySound();
      }

      this.setAggressive(this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   protected void customServerAiStep() {
      this.level.getProfiler().push("zoglinBrain");
      this.getBrain().tick((ServerWorld)this.level, this);
      this.level.getProfiler().pop();
      this.updateActivity();
   }

   /**
    * Set whether this zombie is a child.
    */
   public void setBaby(boolean pChildZombie) {
      this.getEntityData().set(DATA_BABY_ID, pChildZombie);
      if (!this.level.isClientSide && pChildZombie) {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5D);
      }

   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return this.getEntityData().get(DATA_BABY_ID);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.attackAnimationRemainingTicks > 0) {
         --this.attackAnimationRemainingTicks;
      }

      super.aiStep();
   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 4) {
         this.attackAnimationRemainingTicks = 10;
         this.playSound(SoundEvents.ZOGLIN_ATTACK, 1.0F, this.getVoicePitch());
      } else {
         super.handleEntityEvent(pId);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public int getAttackAnimationRemainingTicks() {
      return this.attackAnimationRemainingTicks;
   }

   protected SoundEvent getAmbientSound() {
      if (this.level.isClientSide) {
         return null;
      } else {
         return this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? SoundEvents.ZOGLIN_ANGRY : SoundEvents.ZOGLIN_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ZOGLIN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ZOGLIN_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.ZOGLIN_STEP, 0.15F, 1.0F);
   }

   protected void playAngrySound() {
      this.playSound(SoundEvents.ZOGLIN_ANGRY, 1.0F, this.getVoicePitch());
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPacketSender.sendEntityBrain(this);
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.UNDEAD;
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.isBaby()) {
         pCompound.putBoolean("IsBaby", true);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.getBoolean("IsBaby")) {
         this.setBaby(true);
      }

   }
}