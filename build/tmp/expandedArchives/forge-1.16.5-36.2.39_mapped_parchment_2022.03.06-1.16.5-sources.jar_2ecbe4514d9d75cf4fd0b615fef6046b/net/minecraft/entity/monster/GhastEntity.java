package net.minecraft.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GhastEntity extends FlyingEntity implements IMob {
   private static final DataParameter<Boolean> DATA_IS_CHARGING = EntityDataManager.defineId(GhastEntity.class, DataSerializers.BOOLEAN);
   private int explosionPower = 1;

   public GhastEntity(EntityType<? extends GhastEntity> p_i50206_1_, World p_i50206_2_) {
      super(p_i50206_1_, p_i50206_2_);
      this.xpReward = 5;
      this.moveControl = new GhastEntity.MoveHelperController(this);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(5, new GhastEntity.RandomFlyGoal(this));
      this.goalSelector.addGoal(7, new GhastEntity.LookAroundGoal(this));
      this.goalSelector.addGoal(7, new GhastEntity.FireballAttackGoal(this));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, (p_213812_1_) -> {
         return Math.abs(p_213812_1_.getY() - this.getY()) <= 4.0D;
      }));
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isCharging() {
      return this.entityData.get(DATA_IS_CHARGING);
   }

   public void setCharging(boolean pCharging) {
      this.entityData.set(DATA_IS_CHARGING, pCharging);
   }

   public int getExplosionPower() {
      return this.explosionPower;
   }

   protected boolean shouldDespawnInPeaceful() {
      return true;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (pSource.getDirectEntity() instanceof FireballEntity && pSource.getEntity() instanceof PlayerEntity) {
         super.hurt(pSource, 1000.0F);
         return true;
      } else {
         return super.hurt(pSource, pAmount);
      }
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_IS_CHARGING, false);
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FOLLOW_RANGE, 100.0D);
   }

   public SoundCategory getSoundSource() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.GHAST_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.GHAST_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.GHAST_DEATH;
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 5.0F;
   }

   public static boolean checkGhastSpawnRules(EntityType<GhastEntity> pGhast, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      return pLevel.getDifficulty() != Difficulty.PEACEFUL && pRandom.nextInt(20) == 0 && checkMobSpawnRules(pGhast, pLevel, pSpawnType, pPos, pRandom);
   }

   /**
    * Will return how many at most can spawn in a chunk at once.
    */
   public int getMaxSpawnClusterSize() {
      return 1;
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("ExplosionPower", this.explosionPower);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("ExplosionPower", 99)) {
         this.explosionPower = pCompound.getInt("ExplosionPower");
      }

   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return 2.6F;
   }

   static class FireballAttackGoal extends Goal {
      private final GhastEntity ghast;
      public int chargeTime;

      public FireballAttackGoal(GhastEntity pGhast) {
         this.ghast = pGhast;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.ghast.getTarget() != null;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.chargeTime = 0;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.ghast.setCharging(false);
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = this.ghast.getTarget();
         double d0 = 64.0D;
         if (livingentity.distanceToSqr(this.ghast) < 4096.0D && this.ghast.canSee(livingentity)) {
            World world = this.ghast.level;
            ++this.chargeTime;
            if (this.chargeTime == 10 && !this.ghast.isSilent()) {
               world.levelEvent((PlayerEntity)null, 1015, this.ghast.blockPosition(), 0);
            }

            if (this.chargeTime == 20) {
               double d1 = 4.0D;
               Vector3d vector3d = this.ghast.getViewVector(1.0F);
               double d2 = livingentity.getX() - (this.ghast.getX() + vector3d.x * 4.0D);
               double d3 = livingentity.getY(0.5D) - (0.5D + this.ghast.getY(0.5D));
               double d4 = livingentity.getZ() - (this.ghast.getZ() + vector3d.z * 4.0D);
               if (!this.ghast.isSilent()) {
                  world.levelEvent((PlayerEntity)null, 1016, this.ghast.blockPosition(), 0);
               }

               FireballEntity fireballentity = new FireballEntity(world, this.ghast, d2, d3, d4);
               fireballentity.explosionPower = this.ghast.getExplosionPower();
               fireballentity.setPos(this.ghast.getX() + vector3d.x * 4.0D, this.ghast.getY(0.5D) + 0.5D, fireballentity.getZ() + vector3d.z * 4.0D);
               world.addFreshEntity(fireballentity);
               this.chargeTime = -40;
            }
         } else if (this.chargeTime > 0) {
            --this.chargeTime;
         }

         this.ghast.setCharging(this.chargeTime > 10);
      }
   }

   static class LookAroundGoal extends Goal {
      private final GhastEntity ghast;

      public LookAroundGoal(GhastEntity pGhast) {
         this.ghast = pGhast;
         this.setFlags(EnumSet.of(Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.ghast.getTarget() == null) {
            Vector3d vector3d = this.ghast.getDeltaMovement();
            this.ghast.yRot = -((float)MathHelper.atan2(vector3d.x, vector3d.z)) * (180F / (float)Math.PI);
            this.ghast.yBodyRot = this.ghast.yRot;
         } else {
            LivingEntity livingentity = this.ghast.getTarget();
            double d0 = 64.0D;
            if (livingentity.distanceToSqr(this.ghast) < 4096.0D) {
               double d1 = livingentity.getX() - this.ghast.getX();
               double d2 = livingentity.getZ() - this.ghast.getZ();
               this.ghast.yRot = -((float)MathHelper.atan2(d1, d2)) * (180F / (float)Math.PI);
               this.ghast.yBodyRot = this.ghast.yRot;
            }
         }

      }
   }

   static class MoveHelperController extends MovementController {
      private final GhastEntity ghast;
      private int floatDuration;

      public MoveHelperController(GhastEntity pGhast) {
         super(pGhast);
         this.ghast = pGhast;
      }

      public void tick() {
         if (this.operation == MovementController.Action.MOVE_TO) {
            if (this.floatDuration-- <= 0) {
               this.floatDuration += this.ghast.getRandom().nextInt(5) + 2;
               Vector3d vector3d = new Vector3d(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());
               double d0 = vector3d.length();
               vector3d = vector3d.normalize();
               if (this.canReach(vector3d, MathHelper.ceil(d0))) {
                  this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add(vector3d.scale(0.1D)));
               } else {
                  this.operation = MovementController.Action.WAIT;
               }
            }

         }
      }

      private boolean canReach(Vector3d pPos, int pLength) {
         AxisAlignedBB axisalignedbb = this.ghast.getBoundingBox();

         for(int i = 1; i < pLength; ++i) {
            axisalignedbb = axisalignedbb.move(pPos);
            if (!this.ghast.level.noCollision(this.ghast, axisalignedbb)) {
               return false;
            }
         }

         return true;
      }
   }

   static class RandomFlyGoal extends Goal {
      private final GhastEntity ghast;

      public RandomFlyGoal(GhastEntity pGhast) {
         this.ghast = pGhast;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         MovementController movementcontroller = this.ghast.getMoveControl();
         if (!movementcontroller.hasWanted()) {
            return true;
         } else {
            double d0 = movementcontroller.getWantedX() - this.ghast.getX();
            double d1 = movementcontroller.getWantedY() - this.ghast.getY();
            double d2 = movementcontroller.getWantedZ() - this.ghast.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            return d3 < 1.0D || d3 > 3600.0D;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Random random = this.ghast.getRandom();
         double d0 = this.ghast.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double d1 = this.ghast.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double d2 = this.ghast.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         this.ghast.getMoveControl().setWantedPosition(d0, d1, d2, 1.0D);
      }
   }
}