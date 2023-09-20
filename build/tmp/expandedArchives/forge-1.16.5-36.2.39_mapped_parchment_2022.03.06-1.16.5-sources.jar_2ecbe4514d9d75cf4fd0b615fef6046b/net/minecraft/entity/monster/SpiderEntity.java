package net.minecraft.entity.monster;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

public class SpiderEntity extends MonsterEntity {
   private static final DataParameter<Byte> DATA_FLAGS_ID = EntityDataManager.defineId(SpiderEntity.class, DataSerializers.BYTE);

   public SpiderEntity(EntityType<? extends SpiderEntity> p_i48550_1_, World p_i48550_2_) {
      super(p_i48550_1_, p_i48550_2_);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new SwimGoal(this));
      this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(4, new SpiderEntity.AttackGoal(this));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 0.8D));
      this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new SpiderEntity.TargetGoal<>(this, PlayerEntity.class));
      this.targetSelector.addGoal(3, new SpiderEntity.TargetGoal<>(this, IronGolemEntity.class));
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return (double)(this.getBbHeight() * 0.5F);
   }

   /**
    * Returns new PathNavigateGround instance
    */
   protected PathNavigator createNavigation(World pLevel) {
      return new ClimberPathNavigator(this, pLevel);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_FLAGS_ID, (byte)0);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (!this.level.isClientSide) {
         this.setClimbing(this.horizontalCollision);
      }

   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SPIDER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.SPIDER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SPIDER_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
   }

   /**
    * Returns true if this entity should move as if it were on a ladder (either because it's actually on a ladder, or
    * for AI reasons)
    */
   public boolean onClimbable() {
      return this.isClimbing();
   }

   public void makeStuckInBlock(BlockState pState, Vector3d pMotionMultiplier) {
      if (!pState.is(Blocks.COBWEB)) {
         super.makeStuckInBlock(pState, pMotionMultiplier);
      }

   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.ARTHROPOD;
   }

   public boolean canBeAffected(EffectInstance pEffectInstance) {
      if (pEffectInstance.getEffect() == Effects.POISON) {
         net.minecraftforge.event.entity.living.PotionEvent.PotionApplicableEvent event = new net.minecraftforge.event.entity.living.PotionEvent.PotionApplicableEvent(this, pEffectInstance);
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
         return event.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW;
      }
      return super.canBeAffected(pEffectInstance);
   }

   /**
    * Returns true if the WatchableObject (Byte) is 0x01 otherwise returns false. The WatchableObject is updated using
    * setBesideClimableBlock.
    */
   public boolean isClimbing() {
      return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
   }

   /**
    * Updates the WatchableObject (Byte) created in entityInit(), setting it to 0x01 if par1 is true or 0x00 if it is
    * false.
    */
   public void setClimbing(boolean pClimbing) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (pClimbing) {
         b0 = (byte)(b0 | 1);
      } else {
         b0 = (byte)(b0 & -2);
      }

      this.entityData.set(DATA_FLAGS_ID, b0);
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      if (pLevel.getRandom().nextInt(100) == 0) {
         SkeletonEntity skeletonentity = EntityType.SKELETON.create(this.level);
         skeletonentity.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
         skeletonentity.finalizeSpawn(pLevel, pDifficulty, pReason, (ILivingEntityData)null, (CompoundNBT)null);
         skeletonentity.startRiding(this);
      }

      if (pSpawnData == null) {
         pSpawnData = new SpiderEntity.GroupData();
         if (pLevel.getDifficulty() == Difficulty.HARD && pLevel.getRandom().nextFloat() < 0.1F * pDifficulty.getSpecialMultiplier()) {
            ((SpiderEntity.GroupData)pSpawnData).setRandomEffect(pLevel.getRandom());
         }
      }

      if (pSpawnData instanceof SpiderEntity.GroupData) {
         Effect effect = ((SpiderEntity.GroupData)pSpawnData).effect;
         if (effect != null) {
            this.addEffect(new EffectInstance(effect, Integer.MAX_VALUE));
         }
      }

      return pSpawnData;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return 0.65F;
   }

   static class AttackGoal extends MeleeAttackGoal {
      public AttackGoal(SpiderEntity pSpider) {
         super(pSpider, 1.0D, true);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && !this.mob.isVehicle();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         float f = this.mob.getBrightness();
         if (f >= 0.5F && this.mob.getRandom().nextInt(100) == 0) {
            this.mob.setTarget((LivingEntity)null);
            return false;
         } else {
            return super.canContinueToUse();
         }
      }

      protected double getAttackReachSqr(LivingEntity pAttackTarget) {
         return (double)(4.0F + pAttackTarget.getBbWidth());
      }
   }

   public static class GroupData implements ILivingEntityData {
      public Effect effect;

      public void setRandomEffect(Random pRand) {
         int i = pRand.nextInt(5);
         if (i <= 1) {
            this.effect = Effects.MOVEMENT_SPEED;
         } else if (i <= 2) {
            this.effect = Effects.DAMAGE_BOOST;
         } else if (i <= 3) {
            this.effect = Effects.REGENERATION;
         } else if (i <= 4) {
            this.effect = Effects.INVISIBILITY;
         }

      }
   }

   static class TargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
      public TargetGoal(SpiderEntity pSpider, Class<T> pEntityTypeToTarget) {
         super(pSpider, pEntityTypeToTarget, true);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         float f = this.mob.getBrightness();
         return f >= 0.5F ? false : super.canUse();
      }
   }
}
