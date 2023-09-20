package net.minecraft.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GuardianEntity extends MonsterEntity {
   private static final DataParameter<Boolean> DATA_ID_MOVING = EntityDataManager.defineId(GuardianEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Integer> DATA_ID_ATTACK_TARGET = EntityDataManager.defineId(GuardianEntity.class, DataSerializers.INT);
   private float clientSideTailAnimation;
   private float clientSideTailAnimationO;
   private float clientSideTailAnimationSpeed;
   private float clientSideSpikesAnimation;
   private float clientSideSpikesAnimationO;
   private LivingEntity clientSideCachedAttackTarget;
   private int clientSideAttackTime;
   private boolean clientSideTouchedGround;
   protected RandomWalkingGoal randomStrollGoal;

   public GuardianEntity(EntityType<? extends GuardianEntity> p_i48554_1_, World p_i48554_2_) {
      super(p_i48554_1_, p_i48554_2_);
      this.xpReward = 10;
      this.setPathfindingMalus(PathNodeType.WATER, 0.0F);
      this.moveControl = new GuardianEntity.MoveHelperController(this);
      this.clientSideTailAnimation = this.random.nextFloat();
      this.clientSideTailAnimationO = this.clientSideTailAnimation;
   }

   protected void registerGoals() {
      MoveTowardsRestrictionGoal movetowardsrestrictiongoal = new MoveTowardsRestrictionGoal(this, 1.0D);
      this.randomStrollGoal = new RandomWalkingGoal(this, 1.0D, 80);
      this.goalSelector.addGoal(4, new GuardianEntity.AttackGoal(this));
      this.goalSelector.addGoal(5, movetowardsrestrictiongoal);
      this.goalSelector.addGoal(7, this.randomStrollGoal);
      this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.addGoal(8, new LookAtGoal(this, GuardianEntity.class, 12.0F, 0.01F));
      this.goalSelector.addGoal(9, new LookRandomlyGoal(this));
      this.randomStrollGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      movetowardsrestrictiongoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, new GuardianEntity.TargetPredicate(this)));
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.MAX_HEALTH, 30.0D);
   }

   /**
    * Returns new PathNavigateGround instance
    */
   protected PathNavigator createNavigation(World pLevel) {
      return new SwimmerPathNavigator(this, pLevel);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_MOVING, false);
      this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.WATER;
   }

   public boolean isMoving() {
      return this.entityData.get(DATA_ID_MOVING);
   }

   private void setMoving(boolean pMoving) {
      this.entityData.set(DATA_ID_MOVING, pMoving);
   }

   public int getAttackDuration() {
      return 80;
   }

   private void setActiveAttackTarget(int pActiveAttackTargetId) {
      this.entityData.set(DATA_ID_ATTACK_TARGET, pActiveAttackTargetId);
   }

   public boolean hasActiveAttackTarget() {
      return this.entityData.get(DATA_ID_ATTACK_TARGET) != 0;
   }

   @Nullable
   public LivingEntity getActiveAttackTarget() {
      if (!this.hasActiveAttackTarget()) {
         return null;
      } else if (this.level.isClientSide) {
         if (this.clientSideCachedAttackTarget != null) {
            return this.clientSideCachedAttackTarget;
         } else {
            Entity entity = this.level.getEntity(this.entityData.get(DATA_ID_ATTACK_TARGET));
            if (entity instanceof LivingEntity) {
               this.clientSideCachedAttackTarget = (LivingEntity)entity;
               return this.clientSideCachedAttackTarget;
            } else {
               return null;
            }
         }
      } else {
         return this.getTarget();
      }
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_ID_ATTACK_TARGET.equals(pKey)) {
         this.clientSideAttackTime = 0;
         this.clientSideCachedAttackTarget = null;
      }

   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 160;
   }

   protected SoundEvent getAmbientSound() {
      return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_AMBIENT : SoundEvents.GUARDIAN_AMBIENT_LAND;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_HURT : SoundEvents.GUARDIAN_HURT_LAND;
   }

   protected SoundEvent getDeathSound() {
      return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_DEATH : SoundEvents.GUARDIAN_DEATH_LAND;
   }

   protected boolean isMovementNoisy() {
      return false;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return pSize.height * 0.5F;
   }

   public float getWalkTargetValue(BlockPos pPos, IWorldReader pLevel) {
      return pLevel.getFluidState(pPos).is(FluidTags.WATER) ? 10.0F + pLevel.getBrightness(pPos) - 0.5F : super.getWalkTargetValue(pPos, pLevel);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.isAlive()) {
         if (this.level.isClientSide) {
            this.clientSideTailAnimationO = this.clientSideTailAnimation;
            if (!this.isInWater()) {
               this.clientSideTailAnimationSpeed = 2.0F;
               Vector3d vector3d = this.getDeltaMovement();
               if (vector3d.y > 0.0D && this.clientSideTouchedGround && !this.isSilent()) {
                  this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundSource(), 1.0F, 1.0F, false);
               }

               this.clientSideTouchedGround = vector3d.y < 0.0D && this.level.loadedAndEntityCanStandOn(this.blockPosition().below(), this);
            } else if (this.isMoving()) {
               if (this.clientSideTailAnimationSpeed < 0.5F) {
                  this.clientSideTailAnimationSpeed = 4.0F;
               } else {
                  this.clientSideTailAnimationSpeed += (0.5F - this.clientSideTailAnimationSpeed) * 0.1F;
               }
            } else {
               this.clientSideTailAnimationSpeed += (0.125F - this.clientSideTailAnimationSpeed) * 0.2F;
            }

            this.clientSideTailAnimation += this.clientSideTailAnimationSpeed;
            this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
            if (!this.isInWaterOrBubble()) {
               this.clientSideSpikesAnimation = this.random.nextFloat();
            } else if (this.isMoving()) {
               this.clientSideSpikesAnimation += (0.0F - this.clientSideSpikesAnimation) * 0.25F;
            } else {
               this.clientSideSpikesAnimation += (1.0F - this.clientSideSpikesAnimation) * 0.06F;
            }

            if (this.isMoving() && this.isInWater()) {
               Vector3d vector3d1 = this.getViewVector(0.0F);

               for(int i = 0; i < 2; ++i) {
                  this.level.addParticle(ParticleTypes.BUBBLE, this.getRandomX(0.5D) - vector3d1.x * 1.5D, this.getRandomY() - vector3d1.y * 1.5D, this.getRandomZ(0.5D) - vector3d1.z * 1.5D, 0.0D, 0.0D, 0.0D);
               }
            }

            if (this.hasActiveAttackTarget()) {
               if (this.clientSideAttackTime < this.getAttackDuration()) {
                  ++this.clientSideAttackTime;
               }

               LivingEntity livingentity = this.getActiveAttackTarget();
               if (livingentity != null) {
                  this.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
                  this.getLookControl().tick();
                  double d5 = (double)this.getAttackAnimationScale(0.0F);
                  double d0 = livingentity.getX() - this.getX();
                  double d1 = livingentity.getY(0.5D) - this.getEyeY();
                  double d2 = livingentity.getZ() - this.getZ();
                  double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                  d0 = d0 / d3;
                  d1 = d1 / d3;
                  d2 = d2 / d3;
                  double d4 = this.random.nextDouble();

                  while(d4 < d3) {
                     d4 += 1.8D - d5 + this.random.nextDouble() * (1.7D - d5);
                     this.level.addParticle(ParticleTypes.BUBBLE, this.getX() + d0 * d4, this.getEyeY() + d1 * d4, this.getZ() + d2 * d4, 0.0D, 0.0D, 0.0D);
                  }
               }
            }
         }

         if (this.isInWaterOrBubble()) {
            this.setAirSupply(300);
         } else if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F), 0.5D, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F)));
            this.yRot = this.random.nextFloat() * 360.0F;
            this.onGround = false;
            this.hasImpulse = true;
         }

         if (this.hasActiveAttackTarget()) {
            this.yRot = this.yHeadRot;
         }
      }

      super.aiStep();
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.GUARDIAN_FLOP;
   }

   @OnlyIn(Dist.CLIENT)
   public float getTailAnimation(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.clientSideTailAnimationO, this.clientSideTailAnimation);
   }

   @OnlyIn(Dist.CLIENT)
   public float getSpikesAnimation(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.clientSideSpikesAnimationO, this.clientSideSpikesAnimation);
   }

   public float getAttackAnimationScale(float pPartialTick) {
      return ((float)this.clientSideAttackTime + pPartialTick) / (float)this.getAttackDuration();
   }

   public boolean checkSpawnObstruction(IWorldReader pLevel) {
      return pLevel.isUnobstructed(this);
   }

   public static boolean checkGuardianSpawnRules(EntityType<? extends GuardianEntity> pGuardian, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      return (pRandom.nextInt(20) == 0 || !pLevel.canSeeSkyFromBelowWater(pPos)) && pLevel.getDifficulty() != Difficulty.PEACEFUL && (pSpawnType == SpawnReason.SPAWNER || pLevel.getFluidState(pPos).is(FluidTags.WATER));
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (!this.isMoving() && !pSource.isMagic() && pSource.getDirectEntity() instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)pSource.getDirectEntity();
         if (!pSource.isExplosion()) {
            livingentity.hurt(DamageSource.thorns(this), 2.0F);
         }
      }

      if (this.randomStrollGoal != null) {
         this.randomStrollGoal.trigger();
      }

      return super.hurt(pSource, pAmount);
   }

   /**
    * The speed it takes to move the entityliving's head rotation through the faceEntity method.
    */
   public int getMaxHeadXRot() {
      return 180;
   }

   public void travel(Vector3d pTravelVector) {
      if (this.isEffectiveAi() && this.isInWater()) {
         this.moveRelative(0.1F, pTravelVector);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
         if (!this.isMoving() && this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
         }
      } else {
         super.travel(pTravelVector);
      }

   }

   static class AttackGoal extends Goal {
      private final GuardianEntity guardian;
      private int attackTime;
      private final boolean elder;

      public AttackGoal(GuardianEntity pGuardian) {
         this.guardian = pGuardian;
         this.elder = pGuardian instanceof ElderGuardianEntity;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         LivingEntity livingentity = this.guardian.getTarget();
         return livingentity != null && livingentity.isAlive();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse() && (this.elder || this.guardian.distanceToSqr(this.guardian.getTarget()) > 9.0D);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.attackTime = -10;
         this.guardian.getNavigation().stop();
         this.guardian.getLookControl().setLookAt(this.guardian.getTarget(), 90.0F, 90.0F);
         this.guardian.hasImpulse = true;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.guardian.setActiveAttackTarget(0);
         this.guardian.setTarget((LivingEntity)null);
         this.guardian.randomStrollGoal.trigger();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = this.guardian.getTarget();
         this.guardian.getNavigation().stop();
         this.guardian.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
         if (!this.guardian.canSee(livingentity)) {
            this.guardian.setTarget((LivingEntity)null);
         } else {
            ++this.attackTime;
            if (this.attackTime == 0) {
               this.guardian.setActiveAttackTarget(this.guardian.getTarget().getId());
               if (!this.guardian.isSilent()) {
                  this.guardian.level.broadcastEntityEvent(this.guardian, (byte)21);
               }
            } else if (this.attackTime >= this.guardian.getAttackDuration()) {
               float f = 1.0F;
               if (this.guardian.level.getDifficulty() == Difficulty.HARD) {
                  f += 2.0F;
               }

               if (this.elder) {
                  f += 2.0F;
               }

               livingentity.hurt(DamageSource.indirectMagic(this.guardian, this.guardian), f);
               livingentity.hurt(DamageSource.mobAttack(this.guardian), (float)this.guardian.getAttributeValue(Attributes.ATTACK_DAMAGE));
               this.guardian.setTarget((LivingEntity)null);
            }

            super.tick();
         }
      }
   }

   static class MoveHelperController extends MovementController {
      private final GuardianEntity guardian;

      public MoveHelperController(GuardianEntity pGuardian) {
         super(pGuardian);
         this.guardian = pGuardian;
      }

      public void tick() {
         if (this.operation == MovementController.Action.MOVE_TO && !this.guardian.getNavigation().isDone()) {
            Vector3d vector3d = new Vector3d(this.wantedX - this.guardian.getX(), this.wantedY - this.guardian.getY(), this.wantedZ - this.guardian.getZ());
            double d0 = vector3d.length();
            double d1 = vector3d.x / d0;
            double d2 = vector3d.y / d0;
            double d3 = vector3d.z / d0;
            float f = (float)(MathHelper.atan2(vector3d.z, vector3d.x) * (double)(180F / (float)Math.PI)) - 90.0F;
            this.guardian.yRot = this.rotlerp(this.guardian.yRot, f, 90.0F);
            this.guardian.yBodyRot = this.guardian.yRot;
            float f1 = (float)(this.speedModifier * this.guardian.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float f2 = MathHelper.lerp(0.125F, this.guardian.getSpeed(), f1);
            this.guardian.setSpeed(f2);
            double d4 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.5D) * 0.05D;
            double d5 = Math.cos((double)(this.guardian.yRot * ((float)Math.PI / 180F)));
            double d6 = Math.sin((double)(this.guardian.yRot * ((float)Math.PI / 180F)));
            double d7 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.75D) * 0.05D;
            this.guardian.setDeltaMovement(this.guardian.getDeltaMovement().add(d4 * d5, d7 * (d6 + d5) * 0.25D + (double)f2 * d2 * 0.1D, d4 * d6));
            LookController lookcontroller = this.guardian.getLookControl();
            double d8 = this.guardian.getX() + d1 * 2.0D;
            double d9 = this.guardian.getEyeY() + d2 / d0;
            double d10 = this.guardian.getZ() + d3 * 2.0D;
            double d11 = lookcontroller.getWantedX();
            double d12 = lookcontroller.getWantedY();
            double d13 = lookcontroller.getWantedZ();
            if (!lookcontroller.isHasWanted()) {
               d11 = d8;
               d12 = d9;
               d13 = d10;
            }

            this.guardian.getLookControl().setLookAt(MathHelper.lerp(0.125D, d11, d8), MathHelper.lerp(0.125D, d12, d9), MathHelper.lerp(0.125D, d13, d10), 10.0F, 40.0F);
            this.guardian.setMoving(true);
         } else {
            this.guardian.setSpeed(0.0F);
            this.guardian.setMoving(false);
         }
      }
   }

   static class TargetPredicate implements Predicate<LivingEntity> {
      private final GuardianEntity guardian;

      public TargetPredicate(GuardianEntity pGuardian) {
         this.guardian = pGuardian;
      }

      public boolean test(@Nullable LivingEntity p_test_1_) {
         return (p_test_1_ instanceof PlayerEntity || p_test_1_ instanceof SquidEntity) && p_test_1_.distanceToSqr(this.guardian) > 9.0D;
      }
   }
}