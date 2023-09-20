package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ChickenEntity extends AnimalEntity {
   private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
   public float flap;
   public float flapSpeed;
   public float oFlapSpeed;
   public float oFlap;
   public float flapping = 1.0F;
   public int eggTime = this.random.nextInt(6000) + 6000;
   public boolean isChickenJockey;

   public ChickenEntity(EntityType<? extends ChickenEntity> p_i50282_1_, World p_i50282_2_) {
      super(p_i50282_1_, p_i50282_2_);
      this.setPathfindingMalus(PathNodeType.WATER, 0.0F);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new SwimGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, false, FOOD_ITEMS));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return this.isBaby() ? pSize.height * 0.85F : pSize.height * 0.92F;
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0D).add(Attributes.MOVEMENT_SPEED, 0.25D);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      this.oFlap = this.flap;
      this.oFlapSpeed = this.flapSpeed;
      this.flapSpeed = (float)((double)this.flapSpeed + (double)(this.onGround ? -1 : 4) * 0.3D);
      this.flapSpeed = MathHelper.clamp(this.flapSpeed, 0.0F, 1.0F);
      if (!this.onGround && this.flapping < 1.0F) {
         this.flapping = 1.0F;
      }

      this.flapping = (float)((double)this.flapping * 0.9D);
      Vector3d vector3d = this.getDeltaMovement();
      if (!this.onGround && vector3d.y < 0.0D) {
         this.setDeltaMovement(vector3d.multiply(1.0D, 0.6D, 1.0D));
      }

      this.flap += this.flapping * 2.0F;
      if (!this.level.isClientSide && this.isAlive() && !this.isBaby() && !this.isChickenJockey() && --this.eggTime <= 0) {
         this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         this.spawnAtLocation(Items.EGG);
         this.eggTime = this.random.nextInt(6000) + 6000;
      }

   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.CHICKEN_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.CHICKEN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.CHICKEN_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
   }

   public ChickenEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      return EntityType.CHICKEN.create(pServerLevel);
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return FOOD_ITEMS.test(pStack);
   }

   /**
    * Get the experience points the entity currently has.
    */
   protected int getExperienceReward(PlayerEntity pPlayer) {
      return this.isChickenJockey() ? 10 : super.getExperienceReward(pPlayer);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.isChickenJockey = pCompound.getBoolean("IsChickenJockey");
      if (pCompound.contains("EggLayTime")) {
         this.eggTime = pCompound.getInt("EggLayTime");
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("IsChickenJockey", this.isChickenJockey);
      pCompound.putInt("EggLayTime", this.eggTime);
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return this.isChickenJockey();
   }

   public void positionRider(Entity pPassenger) {
      super.positionRider(pPassenger);
      float f = MathHelper.sin(this.yBodyRot * ((float)Math.PI / 180F));
      float f1 = MathHelper.cos(this.yBodyRot * ((float)Math.PI / 180F));
      float f2 = 0.1F;
      float f3 = 0.0F;
      pPassenger.setPos(this.getX() + (double)(0.1F * f), this.getY(0.5D) + pPassenger.getMyRidingOffset() + 0.0D, this.getZ() - (double)(0.1F * f1));
      if (pPassenger instanceof LivingEntity) {
         ((LivingEntity)pPassenger).yBodyRot = this.yBodyRot;
      }

   }

   /**
    * Determines if this chicken is a jokey with a zombie riding it.
    */
   public boolean isChickenJockey() {
      return this.isChickenJockey;
   }

   /**
    * Sets whether this chicken is a jockey or not.
    */
   public void setChickenJockey(boolean pIsChickenJockey) {
      this.isChickenJockey = pIsChickenJockey;
   }
}