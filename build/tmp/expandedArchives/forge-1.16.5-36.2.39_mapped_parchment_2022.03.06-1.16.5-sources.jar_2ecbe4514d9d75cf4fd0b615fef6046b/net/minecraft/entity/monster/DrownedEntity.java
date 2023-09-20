package net.minecraft.entity.monster;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class DrownedEntity extends ZombieEntity implements IRangedAttackMob {
   private boolean searchingForLand;
   protected final SwimmerPathNavigator waterNavigation;
   protected final GroundPathNavigator groundNavigation;

   public DrownedEntity(EntityType<? extends DrownedEntity> p_i50212_1_, World p_i50212_2_) {
      super(p_i50212_1_, p_i50212_2_);
      this.maxUpStep = 1.0F;
      this.moveControl = new DrownedEntity.MoveHelperController(this);
      this.setPathfindingMalus(PathNodeType.WATER, 0.0F);
      this.waterNavigation = new SwimmerPathNavigator(this, p_i50212_2_);
      this.groundNavigation = new GroundPathNavigator(this, p_i50212_2_);
   }

   protected void addBehaviourGoals() {
      this.goalSelector.addGoal(1, new DrownedEntity.GoToWaterGoal(this, 1.0D));
      this.goalSelector.addGoal(2, new DrownedEntity.TridentAttackGoal(this, 1.0D, 40, 10.0F));
      this.goalSelector.addGoal(2, new DrownedEntity.AttackGoal(this, 1.0D, false));
      this.goalSelector.addGoal(5, new DrownedEntity.GoToBeachGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new DrownedEntity.SwimUpGoal(this, 1.0D, this.level.getSeaLevel()));
      this.goalSelector.addGoal(7, new RandomWalkingGoal(this, 1.0D));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, DrownedEntity.class)).setAlertOthers(ZombifiedPiglinEntity.class));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::okTarget));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_ON_LAND_SELECTOR));
   }

   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      if (this.getItemBySlot(EquipmentSlotType.OFFHAND).isEmpty() && this.random.nextFloat() < 0.03F) {
         this.setItemSlot(EquipmentSlotType.OFFHAND, new ItemStack(Items.NAUTILUS_SHELL));
         this.handDropChances[EquipmentSlotType.OFFHAND.getIndex()] = 2.0F;
      }

      return pSpawnData;
   }

   public static boolean checkDrownedSpawnRules(EntityType<DrownedEntity> pDrowned, IServerWorld pServerLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      Optional<RegistryKey<Biome>> optional = pServerLevel.getBiomeName(pPos);
      boolean flag = pServerLevel.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(pServerLevel, pPos, pRandom) && (pSpawnType == SpawnReason.SPAWNER || pServerLevel.getFluidState(pPos).is(FluidTags.WATER));
      if (!Objects.equals(optional, Optional.of(Biomes.RIVER)) && !Objects.equals(optional, Optional.of(Biomes.FROZEN_RIVER))) {
         return pRandom.nextInt(40) == 0 && isDeepEnoughToSpawn(pServerLevel, pPos) && flag;
      } else {
         return pRandom.nextInt(15) == 0 && flag;
      }
   }

   private static boolean isDeepEnoughToSpawn(IWorld pLevel, BlockPos pPos) {
      return pPos.getY() < pLevel.getSeaLevel() - 5;
   }

   protected boolean supportsBreakDoorGoal() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return this.isInWater() ? SoundEvents.DROWNED_AMBIENT_WATER : SoundEvents.DROWNED_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return this.isInWater() ? SoundEvents.DROWNED_HURT_WATER : SoundEvents.DROWNED_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.isInWater() ? SoundEvents.DROWNED_DEATH_WATER : SoundEvents.DROWNED_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.DROWNED_STEP;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.DROWNED_SWIM;
   }

   protected ItemStack getSkull() {
      return ItemStack.EMPTY;
   }

   /**
    * Gives armor or weapon for entity based on given DifficultyInstance
    */
   protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
      if ((double)this.random.nextFloat() > 0.9D) {
         int i = this.random.nextInt(16);
         if (i < 10) {
            this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.TRIDENT));
         } else {
            this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.FISHING_ROD));
         }
      }

   }

   protected boolean canReplaceCurrentItem(ItemStack pCandidate, ItemStack pExisting) {
      if (pExisting.getItem() == Items.NAUTILUS_SHELL) {
         return false;
      } else if (pExisting.getItem() == Items.TRIDENT) {
         if (pCandidate.getItem() == Items.TRIDENT) {
            return pCandidate.getDamageValue() < pExisting.getDamageValue();
         } else {
            return false;
         }
      } else {
         return pCandidate.getItem() == Items.TRIDENT ? true : super.canReplaceCurrentItem(pCandidate, pExisting);
      }
   }

   protected boolean convertsInWater() {
      return false;
   }

   public boolean checkSpawnObstruction(IWorldReader pLevel) {
      return pLevel.isUnobstructed(this);
   }

   public boolean okTarget(@Nullable LivingEntity p_204714_1_) {
      if (p_204714_1_ != null) {
         return !this.level.isDay() || p_204714_1_.isInWater();
      } else {
         return false;
      }
   }

   public boolean isPushedByFluid() {
      return !this.isSwimming();
   }

   private boolean wantsToSwim() {
      if (this.searchingForLand) {
         return true;
      } else {
         LivingEntity livingentity = this.getTarget();
         return livingentity != null && livingentity.isInWater();
      }
   }

   public void travel(Vector3d pTravelVector) {
      if (this.isEffectiveAi() && this.isInWater() && this.wantsToSwim()) {
         this.moveRelative(0.01F, pTravelVector);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
      } else {
         super.travel(pTravelVector);
      }

   }

   public void updateSwimming() {
      if (!this.level.isClientSide) {
         if (this.isEffectiveAi() && this.isInWater() && this.wantsToSwim()) {
            this.navigation = this.waterNavigation;
            this.setSwimming(true);
         } else {
            this.navigation = this.groundNavigation;
            this.setSwimming(false);
         }
      }

   }

   protected boolean closeToNextPos() {
      Path path = this.getNavigation().getPath();
      if (path != null) {
         BlockPos blockpos = path.getTarget();
         if (blockpos != null) {
            double d0 = this.distanceToSqr((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
            if (d0 < 4.0D) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Attack the specified entity using a ranged attack.
    */
   public void performRangedAttack(LivingEntity pTarget, float pVelocity) {
      TridentEntity tridententity = new TridentEntity(this.level, this, new ItemStack(Items.TRIDENT));
      double d0 = pTarget.getX() - this.getX();
      double d1 = pTarget.getY(0.3333333333333333D) - tridententity.getY();
      double d2 = pTarget.getZ() - this.getZ();
      double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
      tridententity.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
      this.playSound(SoundEvents.DROWNED_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level.addFreshEntity(tridententity);
   }

   public void setSearchingForLand(boolean pSearchingForLand) {
      this.searchingForLand = pSearchingForLand;
   }

   static class AttackGoal extends ZombieAttackGoal {
      private final DrownedEntity drowned;

      public AttackGoal(DrownedEntity pDrowned, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
         super(pDrowned, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
         this.drowned = pDrowned;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && this.drowned.okTarget(this.drowned.getTarget());
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse() && this.drowned.okTarget(this.drowned.getTarget());
      }
   }

   static class GoToBeachGoal extends MoveToBlockGoal {
      private final DrownedEntity drowned;

      public GoToBeachGoal(DrownedEntity pDrowned, double pSpeedModifier) {
         super(pDrowned, pSpeedModifier, 8, 2);
         this.drowned = pDrowned;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && !this.drowned.level.isDay() && this.drowned.isInWater() && this.drowned.getY() >= (double)(this.drowned.level.getSeaLevel() - 3);
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse();
      }

      /**
       * Return true to set given position as destination
       */
      protected boolean isValidTarget(IWorldReader pLevel, BlockPos pPos) {
         BlockPos blockpos = pPos.above();
         return pLevel.isEmptyBlock(blockpos) && pLevel.isEmptyBlock(blockpos.above()) ? pLevel.getBlockState(pPos).entityCanStandOn(pLevel, pPos, this.drowned) : false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.drowned.setSearchingForLand(false);
         this.drowned.navigation = this.drowned.groundNavigation;
         super.start();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         super.stop();
      }
   }

   static class GoToWaterGoal extends Goal {
      private final CreatureEntity mob;
      private double wantedX;
      private double wantedY;
      private double wantedZ;
      private final double speedModifier;
      private final World level;

      public GoToWaterGoal(CreatureEntity pMob, double pSpeedModifier) {
         this.mob = pMob;
         this.speedModifier = pSpeedModifier;
         this.level = pMob.level;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (!this.level.isDay()) {
            return false;
         } else if (this.mob.isInWater()) {
            return false;
         } else {
            Vector3d vector3d = this.getWaterPos();
            if (vector3d == null) {
               return false;
            } else {
               this.wantedX = vector3d.x;
               this.wantedY = vector3d.y;
               this.wantedZ = vector3d.z;
               return true;
            }
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return !this.mob.getNavigation().isDone();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
      }

      @Nullable
      private Vector3d getWaterPos() {
         Random random = this.mob.getRandom();
         BlockPos blockpos = this.mob.blockPosition();

         for(int i = 0; i < 10; ++i) {
            BlockPos blockpos1 = blockpos.offset(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);
            if (this.level.getBlockState(blockpos1).is(Blocks.WATER)) {
               return Vector3d.atBottomCenterOf(blockpos1);
            }
         }

         return null;
      }
   }

   static class MoveHelperController extends MovementController {
      private final DrownedEntity drowned;

      public MoveHelperController(DrownedEntity pDrowned) {
         super(pDrowned);
         this.drowned = pDrowned;
      }

      public void tick() {
         LivingEntity livingentity = this.drowned.getTarget();
         if (this.drowned.wantsToSwim() && this.drowned.isInWater()) {
            if (livingentity != null && livingentity.getY() > this.drowned.getY() || this.drowned.searchingForLand) {
               this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0D, 0.002D, 0.0D));
            }

            if (this.operation != MovementController.Action.MOVE_TO || this.drowned.getNavigation().isDone()) {
               this.drowned.setSpeed(0.0F);
               return;
            }

            double d0 = this.wantedX - this.drowned.getX();
            double d1 = this.wantedY - this.drowned.getY();
            double d2 = this.wantedZ - this.drowned.getZ();
            double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            d1 = d1 / d3;
            float f = (float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
            this.drowned.yRot = this.rotlerp(this.drowned.yRot, f, 90.0F);
            this.drowned.yBodyRot = this.drowned.yRot;
            float f1 = (float)(this.speedModifier * this.drowned.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float f2 = MathHelper.lerp(0.125F, this.drowned.getSpeed(), f1);
            this.drowned.setSpeed(f2);
            this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add((double)f2 * d0 * 0.005D, (double)f2 * d1 * 0.1D, (double)f2 * d2 * 0.005D));
         } else {
            if (!this.drowned.onGround) {
               this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0D, -0.008D, 0.0D));
            }

            super.tick();
         }

      }
   }

   static class SwimUpGoal extends Goal {
      private final DrownedEntity drowned;
      private final double speedModifier;
      private final int seaLevel;
      private boolean stuck;

      public SwimUpGoal(DrownedEntity pDrowned, double pSpeedModifier, int pSeaLevel) {
         this.drowned = pDrowned;
         this.speedModifier = pSpeedModifier;
         this.seaLevel = pSeaLevel;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !this.drowned.level.isDay() && this.drowned.isInWater() && this.drowned.getY() < (double)(this.seaLevel - 2);
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.canUse() && !this.stuck;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.drowned.getY() < (double)(this.seaLevel - 1) && (this.drowned.getNavigation().isDone() || this.drowned.closeToNextPos())) {
            Vector3d vector3d = RandomPositionGenerator.getPosTowards(this.drowned, 4, 8, new Vector3d(this.drowned.getX(), (double)(this.seaLevel - 1), this.drowned.getZ()));
            if (vector3d == null) {
               this.stuck = true;
               return;
            }

            this.drowned.getNavigation().moveTo(vector3d.x, vector3d.y, vector3d.z, this.speedModifier);
         }

      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.drowned.setSearchingForLand(true);
         this.stuck = false;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.drowned.setSearchingForLand(false);
      }
   }

   static class TridentAttackGoal extends RangedAttackGoal {
      private final DrownedEntity drowned;

      public TridentAttackGoal(IRangedAttackMob p_i48907_1_, double p_i48907_2_, int p_i48907_4_, float p_i48907_5_) {
         super(p_i48907_1_, p_i48907_2_, p_i48907_4_, p_i48907_5_);
         this.drowned = (DrownedEntity)p_i48907_1_;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && this.drowned.getMainHandItem().getItem() == Items.TRIDENT;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.drowned.setAggressive(true);
         this.drowned.startUsingItem(Hand.MAIN_HAND);
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         super.stop();
         this.drowned.stopUsingItem();
         this.drowned.setAggressive(false);
      }
   }
}