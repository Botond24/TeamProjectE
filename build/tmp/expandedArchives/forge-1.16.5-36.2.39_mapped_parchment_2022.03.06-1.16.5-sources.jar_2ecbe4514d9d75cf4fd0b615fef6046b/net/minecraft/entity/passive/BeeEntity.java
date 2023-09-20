package net.minecraft.entity.passive;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.ResetAngerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TickRangeConverter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BeeEntity extends AnimalEntity implements IAngerable, IFlyingAnimal {
   private static final DataParameter<Byte> DATA_FLAGS_ID = EntityDataManager.defineId(BeeEntity.class, DataSerializers.BYTE);
   private static final DataParameter<Integer> DATA_REMAINING_ANGER_TIME = EntityDataManager.defineId(BeeEntity.class, DataSerializers.INT);
   private static final RangedInteger PERSISTENT_ANGER_TIME = TickRangeConverter.rangeOfSeconds(20, 39);
   private UUID persistentAngerTarget;
   private float rollAmount;
   private float rollAmountO;
   private int timeSinceSting;
   private int ticksWithoutNectarSinceExitingHive;
   private int stayOutOfHiveCountdown;
   private int numCropsGrownSincePollination;
   private int remainingCooldownBeforeLocatingNewHive = 0;
   private int remainingCooldownBeforeLocatingNewFlower = 0;
   @Nullable
   private BlockPos savedFlowerPos = null;
   @Nullable
   private BlockPos hivePos = null;
   private BeeEntity.PollinateGoal beePollinateGoal;
   private BeeEntity.FindBeehiveGoal goToHiveGoal;
   private BeeEntity.FindFlowerGoal goToKnownFlowerGoal;
   private int underWaterTicks;

   public BeeEntity(EntityType<? extends BeeEntity> p_i225714_1_, World p_i225714_2_) {
      super(p_i225714_1_, p_i225714_2_);
      this.moveControl = new FlyingMovementController(this, 20, true);
      this.lookControl = new BeeEntity.BeeLookController(this);
      this.setPathfindingMalus(PathNodeType.DANGER_FIRE, -1.0F);
      this.setPathfindingMalus(PathNodeType.WATER, -1.0F);
      this.setPathfindingMalus(PathNodeType.WATER_BORDER, 16.0F);
      this.setPathfindingMalus(PathNodeType.COCOA, -1.0F);
      this.setPathfindingMalus(PathNodeType.FENCE, -1.0F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_FLAGS_ID, (byte)0);
      this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
   }

   public float getWalkTargetValue(BlockPos pPos, IWorldReader pLevel) {
      return pLevel.getBlockState(pPos).isAir() ? 10.0F : 0.0F;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new BeeEntity.StingGoal(this, (double)1.4F, true));
      this.goalSelector.addGoal(1, new BeeEntity.EnterBeehiveGoal());
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(ItemTags.FLOWERS), false));
      this.beePollinateGoal = new BeeEntity.PollinateGoal();
      this.goalSelector.addGoal(4, this.beePollinateGoal);
      this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
      this.goalSelector.addGoal(5, new BeeEntity.UpdateBeehiveGoal());
      this.goToHiveGoal = new BeeEntity.FindBeehiveGoal();
      this.goalSelector.addGoal(5, this.goToHiveGoal);
      this.goToKnownFlowerGoal = new BeeEntity.FindFlowerGoal();
      this.goalSelector.addGoal(6, this.goToKnownFlowerGoal);
      this.goalSelector.addGoal(7, new BeeEntity.FindPollinationTargetGoal());
      this.goalSelector.addGoal(8, new BeeEntity.WanderGoal());
      this.goalSelector.addGoal(9, new SwimGoal(this));
      this.targetSelector.addGoal(1, (new BeeEntity.AngerGoal(this)).setAlertOthers(new Class[0]));
      this.targetSelector.addGoal(2, new BeeEntity.AttackPlayerGoal(this));
      this.targetSelector.addGoal(3, new ResetAngerGoal<>(this, true));
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.hasHive()) {
         pCompound.put("HivePos", NBTUtil.writeBlockPos(this.getHivePos()));
      }

      if (this.hasSavedFlowerPos()) {
         pCompound.put("FlowerPos", NBTUtil.writeBlockPos(this.getSavedFlowerPos()));
      }

      pCompound.putBoolean("HasNectar", this.hasNectar());
      pCompound.putBoolean("HasStung", this.hasStung());
      pCompound.putInt("TicksSincePollination", this.ticksWithoutNectarSinceExitingHive);
      pCompound.putInt("CannotEnterHiveTicks", this.stayOutOfHiveCountdown);
      pCompound.putInt("CropsGrownSincePollination", this.numCropsGrownSincePollination);
      this.addPersistentAngerSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      this.hivePos = null;
      if (pCompound.contains("HivePos")) {
         this.hivePos = NBTUtil.readBlockPos(pCompound.getCompound("HivePos"));
      }

      this.savedFlowerPos = null;
      if (pCompound.contains("FlowerPos")) {
         this.savedFlowerPos = NBTUtil.readBlockPos(pCompound.getCompound("FlowerPos"));
      }

      super.readAdditionalSaveData(pCompound);
      this.setHasNectar(pCompound.getBoolean("HasNectar"));
      this.setHasStung(pCompound.getBoolean("HasStung"));
      this.ticksWithoutNectarSinceExitingHive = pCompound.getInt("TicksSincePollination");
      this.stayOutOfHiveCountdown = pCompound.getInt("CannotEnterHiveTicks");
      this.numCropsGrownSincePollination = pCompound.getInt("CropsGrownSincePollination");
      if(!level.isClientSide) //FORGE: allow this entity to be read from nbt on client. (Fixes MC-189565)
      this.readPersistentAngerSaveData((ServerWorld)this.level, pCompound);
   }

   public boolean doHurtTarget(Entity pEntity) {
      boolean flag = pEntity.hurt(DamageSource.sting(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
      if (flag) {
         this.doEnchantDamageEffects(this, pEntity);
         if (pEntity instanceof LivingEntity) {
            ((LivingEntity)pEntity).setStingerCount(((LivingEntity)pEntity).getStingerCount() + 1);
            int i = 0;
            if (this.level.getDifficulty() == Difficulty.NORMAL) {
               i = 10;
            } else if (this.level.getDifficulty() == Difficulty.HARD) {
               i = 18;
            }

            if (i > 0) {
               ((LivingEntity)pEntity).addEffect(new EffectInstance(Effects.POISON, i * 20, 0));
            }
         }

         this.setHasStung(true);
         this.stopBeingAngry();
         this.playSound(SoundEvents.BEE_STING, 1.0F, 1.0F);
      }

      return flag;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.random.nextFloat() < 0.05F) {
         for(int i = 0; i < this.random.nextInt(2) + 1; ++i) {
            this.spawnFluidParticle(this.level, this.getX() - (double)0.3F, this.getX() + (double)0.3F, this.getZ() - (double)0.3F, this.getZ() + (double)0.3F, this.getY(0.5D), ParticleTypes.FALLING_NECTAR);
         }
      }

      this.updateRollAmount();
   }

   private void spawnFluidParticle(World pLevel, double pStartX, double pEndX, double pStartZ, double pEndZ, double pPosY, IParticleData pParticleOption) {
      pLevel.addParticle(pParticleOption, MathHelper.lerp(pLevel.random.nextDouble(), pStartX, pEndX), pPosY, MathHelper.lerp(pLevel.random.nextDouble(), pStartZ, pEndZ), 0.0D, 0.0D, 0.0D);
   }

   private void pathfindRandomlyTowards(BlockPos pPos) {
      Vector3d vector3d = Vector3d.atBottomCenterOf(pPos);
      int i = 0;
      BlockPos blockpos = this.blockPosition();
      int j = (int)vector3d.y - blockpos.getY();
      if (j > 2) {
         i = 4;
      } else if (j < -2) {
         i = -4;
      }

      int k = 6;
      int l = 8;
      int i1 = blockpos.distManhattan(pPos);
      if (i1 < 15) {
         k = i1 / 2;
         l = i1 / 2;
      }

      Vector3d vector3d1 = RandomPositionGenerator.getAirPosTowards(this, k, l, i, vector3d, (double)((float)Math.PI / 10F));
      if (vector3d1 != null) {
         this.navigation.setMaxVisitedNodesMultiplier(0.5F);
         this.navigation.moveTo(vector3d1.x, vector3d1.y, vector3d1.z, 1.0D);
      }
   }

   @Nullable
   public BlockPos getSavedFlowerPos() {
      return this.savedFlowerPos;
   }

   public boolean hasSavedFlowerPos() {
      return this.savedFlowerPos != null;
   }

   public void setSavedFlowerPos(BlockPos pSavedFlowerPos) {
      this.savedFlowerPos = pSavedFlowerPos;
   }

   private boolean isTiredOfLookingForNectar() {
      return this.ticksWithoutNectarSinceExitingHive > 3600;
   }

   private boolean wantsToEnterHive() {
      if (this.stayOutOfHiveCountdown <= 0 && !this.beePollinateGoal.isPollinating() && !this.hasStung() && this.getTarget() == null) {
         boolean flag = this.isTiredOfLookingForNectar() || this.level.isRaining() || this.level.isNight() || this.hasNectar();
         return flag && !this.isHiveNearFire();
      } else {
         return false;
      }
   }

   public void setStayOutOfHiveCountdown(int pStayOutOfHiveCountdown) {
      this.stayOutOfHiveCountdown = pStayOutOfHiveCountdown;
   }

   @OnlyIn(Dist.CLIENT)
   public float getRollAmount(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.rollAmountO, this.rollAmount);
   }

   private void updateRollAmount() {
      this.rollAmountO = this.rollAmount;
      if (this.isRolling()) {
         this.rollAmount = Math.min(1.0F, this.rollAmount + 0.2F);
      } else {
         this.rollAmount = Math.max(0.0F, this.rollAmount - 0.24F);
      }

   }

   protected void customServerAiStep() {
      boolean flag = this.hasStung();
      if (this.isInWaterOrBubble()) {
         ++this.underWaterTicks;
      } else {
         this.underWaterTicks = 0;
      }

      if (this.underWaterTicks > 20) {
         this.hurt(DamageSource.DROWN, 1.0F);
      }

      if (flag) {
         ++this.timeSinceSting;
         if (this.timeSinceSting % 5 == 0 && this.random.nextInt(MathHelper.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
            this.hurt(DamageSource.GENERIC, this.getHealth());
         }
      }

      if (!this.hasNectar()) {
         ++this.ticksWithoutNectarSinceExitingHive;
      }

      if (!this.level.isClientSide) {
         this.updatePersistentAnger((ServerWorld)this.level, false);
      }

   }

   public void resetTicksWithoutNectarSinceExitingHive() {
      this.ticksWithoutNectarSinceExitingHive = 0;
   }

   private boolean isHiveNearFire() {
      if (this.hivePos == null) {
         return false;
      } else {
         TileEntity tileentity = this.level.getBlockEntity(this.hivePos);
         return tileentity instanceof BeehiveTileEntity && ((BeehiveTileEntity)tileentity).isFireNearby();
      }
   }

   public int getRemainingPersistentAngerTime() {
      return this.entityData.get(DATA_REMAINING_ANGER_TIME);
   }

   public void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime) {
      this.entityData.set(DATA_REMAINING_ANGER_TIME, pRemainingPersistentAngerTime);
   }

   public UUID getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   public void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget) {
      this.persistentAngerTarget = pPersistentAngerTarget;
   }

   public void startPersistentAngerTimer() {
      this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
   }

   private boolean doesHiveHaveSpace(BlockPos pHivePos) {
      TileEntity tileentity = this.level.getBlockEntity(pHivePos);
      if (tileentity instanceof BeehiveTileEntity) {
         return !((BeehiveTileEntity)tileentity).isFull();
      } else {
         return false;
      }
   }

   public boolean hasHive() {
      return this.hivePos != null;
   }

   @Nullable
   public BlockPos getHivePos() {
      return this.hivePos;
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPacketSender.sendBeeInfo(this);
   }

   private int getCropsGrownSincePollination() {
      return this.numCropsGrownSincePollination;
   }

   private void resetNumCropsGrownSincePollination() {
      this.numCropsGrownSincePollination = 0;
   }

   private void incrementNumCropsGrownSincePollination() {
      ++this.numCropsGrownSincePollination;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (!this.level.isClientSide) {
         if (this.stayOutOfHiveCountdown > 0) {
            --this.stayOutOfHiveCountdown;
         }

         if (this.remainingCooldownBeforeLocatingNewHive > 0) {
            --this.remainingCooldownBeforeLocatingNewHive;
         }

         if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
            --this.remainingCooldownBeforeLocatingNewFlower;
         }

         boolean flag = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0D;
         this.setRolling(flag);
         if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
            this.hivePos = null;
         }
      }

   }

   private boolean isHiveValid() {
      if (!this.hasHive()) {
         return false;
      } else {
         TileEntity tileentity = this.level.getBlockEntity(this.hivePos);
         return tileentity instanceof BeehiveTileEntity;
      }
   }

   public boolean hasNectar() {
      return this.getFlag(8);
   }

   private void setHasNectar(boolean pHasNectar) {
      if (pHasNectar) {
         this.resetTicksWithoutNectarSinceExitingHive();
      }

      this.setFlag(8, pHasNectar);
   }

   public boolean hasStung() {
      return this.getFlag(4);
   }

   private void setHasStung(boolean pHasStung) {
      this.setFlag(4, pHasStung);
   }

   private boolean isRolling() {
      return this.getFlag(2);
   }

   private void setRolling(boolean pIsRolling) {
      this.setFlag(2, pIsRolling);
   }

   private boolean isTooFarAway(BlockPos pPos) {
      return !this.closerThan(pPos, 32);
   }

   private void setFlag(int pFlagId, boolean pValue) {
      if (pValue) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | pFlagId));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~pFlagId));
      }

   }

   private boolean getFlag(int pFlagId) {
      return (this.entityData.get(DATA_FLAGS_ID) & pFlagId) != 0;
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FLYING_SPEED, (double)0.6F).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.ATTACK_DAMAGE, 2.0D).add(Attributes.FOLLOW_RANGE, 48.0D);
   }

   /**
    * Returns new PathNavigateGround instance
    */
   protected PathNavigator createNavigation(World pLevel) {
      FlyingPathNavigator flyingpathnavigator = new FlyingPathNavigator(this, pLevel) {
         public boolean isStableDestination(BlockPos pPos) {
            return !this.level.getBlockState(pPos.below()).isAir();
         }

         public void tick() {
            if (!BeeEntity.this.beePollinateGoal.isPollinating()) {
               super.tick();
            }
         }
      };
      flyingpathnavigator.setCanOpenDoors(false);
      flyingpathnavigator.setCanFloat(false);
      flyingpathnavigator.setCanPassDoors(true);
      return flyingpathnavigator;
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return pStack.getItem().is(ItemTags.FLOWERS);
   }

   private boolean isFlowerValid(BlockPos pPos) {
      return this.level.isLoaded(pPos) && this.level.getBlockState(pPos).getBlock().is(BlockTags.FLOWERS);
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
   }

   protected SoundEvent getAmbientSound() {
      return null;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.BEE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.BEE_DEATH;
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 0.4F;
   }

   public BeeEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      return EntityType.BEE.create(pServerLevel);
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return this.isBaby() ? pSize.height * 0.5F : pSize.height * 0.5F;
   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      return false;
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
   }

   protected boolean makeFlySound() {
      return true;
   }

   public void dropOffNectar() {
      this.setHasNectar(false);
      this.resetNumCropsGrownSincePollination();
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         Entity entity = pSource.getEntity();
         if (!this.level.isClientSide) {
            this.beePollinateGoal.stopPollinating();
         }

         return super.hurt(pSource, pAmount);
      }
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.ARTHROPOD;
   }

   protected void jumpInLiquid(ITag<Fluid> pFluidTag) {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getLeashOffset() {
      return new Vector3d(0.0D, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.2F));
   }

   private boolean closerThan(BlockPos pPos, int pDistance) {
      return pPos.closerThan(this.blockPosition(), (double)pDistance);
   }

   class AngerGoal extends HurtByTargetGoal {
      AngerGoal(BeeEntity pMob) {
         super(pMob);
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return BeeEntity.this.isAngry() && super.canContinueToUse();
      }

      protected void alertOther(MobEntity pMob, LivingEntity pTarget) {
         if (pMob instanceof BeeEntity && this.mob.canSee(pTarget)) {
            pMob.setTarget(pTarget);
         }

      }
   }

   static class AttackPlayerGoal extends NearestAttackableTargetGoal<PlayerEntity> {
      AttackPlayerGoal(BeeEntity pMob) {
         super(pMob, PlayerEntity.class, 10, true, false, pMob::isAngryAt);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.beeCanTarget() && super.canUse();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         boolean flag = this.beeCanTarget();
         if (flag && this.mob.getTarget() != null) {
            return super.canContinueToUse();
         } else {
            this.targetMob = null;
            return false;
         }
      }

      private boolean beeCanTarget() {
         BeeEntity beeentity = (BeeEntity)this.mob;
         return beeentity.isAngry() && !beeentity.hasStung();
      }
   }

   class BeeLookController extends LookController {
      BeeLookController(MobEntity pMob) {
         super(pMob);
      }

      /**
       * Updates look
       */
      public void tick() {
         if (!BeeEntity.this.isAngry()) {
            super.tick();
         }
      }

      protected boolean resetXRotOnTick() {
         return !BeeEntity.this.beePollinateGoal.isPollinating();
      }
   }

   class EnterBeehiveGoal extends BeeEntity.PassiveGoal {
      private EnterBeehiveGoal() {
      }

      public boolean canBeeUse() {
         if (BeeEntity.this.hasHive() && BeeEntity.this.wantsToEnterHive() && BeeEntity.this.hivePos.closerThan(BeeEntity.this.position(), 2.0D)) {
            TileEntity tileentity = BeeEntity.this.level.getBlockEntity(BeeEntity.this.hivePos);
            if (tileentity instanceof BeehiveTileEntity) {
               BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;
               if (!beehivetileentity.isFull()) {
                  return true;
               }

               BeeEntity.this.hivePos = null;
            }
         }

         return false;
      }

      public boolean canBeeContinueToUse() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         TileEntity tileentity = BeeEntity.this.level.getBlockEntity(BeeEntity.this.hivePos);
         if (tileentity instanceof BeehiveTileEntity) {
            BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;
            beehivetileentity.addOccupant(BeeEntity.this, BeeEntity.this.hasNectar());
         }

      }
   }

   public class FindBeehiveGoal extends BeeEntity.PassiveGoal {
      private int travellingTicks = BeeEntity.this.level.random.nextInt(10);
      private List<BlockPos> blacklistedTargets = Lists.newArrayList();
      @Nullable
      private Path lastPath = null;
      private int ticksStuck;

      FindBeehiveGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      public boolean canBeeUse() {
         return BeeEntity.this.hivePos != null && !BeeEntity.this.hasRestriction() && BeeEntity.this.wantsToEnterHive() && !this.hasReachedTarget(BeeEntity.this.hivePos) && BeeEntity.this.level.getBlockState(BeeEntity.this.hivePos).is(BlockTags.BEEHIVES);
      }

      public boolean canBeeContinueToUse() {
         return this.canBeeUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.travellingTicks = 0;
         this.ticksStuck = 0;
         super.start();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.travellingTicks = 0;
         this.ticksStuck = 0;
         BeeEntity.this.navigation.stop();
         BeeEntity.this.navigation.resetMaxVisitedNodesMultiplier();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (BeeEntity.this.hivePos != null) {
            ++this.travellingTicks;
            if (this.travellingTicks > 600) {
               this.dropAndBlacklistHive();
            } else if (!BeeEntity.this.navigation.isInProgress()) {
               if (!BeeEntity.this.closerThan(BeeEntity.this.hivePos, 16)) {
                  if (BeeEntity.this.isTooFarAway(BeeEntity.this.hivePos)) {
                     this.dropHive();
                  } else {
                     BeeEntity.this.pathfindRandomlyTowards(BeeEntity.this.hivePos);
                  }
               } else {
                  boolean flag = this.pathfindDirectlyTowards(BeeEntity.this.hivePos);
                  if (!flag) {
                     this.dropAndBlacklistHive();
                  } else if (this.lastPath != null && BeeEntity.this.navigation.getPath().sameAs(this.lastPath)) {
                     ++this.ticksStuck;
                     if (this.ticksStuck > 60) {
                        this.dropHive();
                        this.ticksStuck = 0;
                     }
                  } else {
                     this.lastPath = BeeEntity.this.navigation.getPath();
                  }

               }
            }
         }
      }

      private boolean pathfindDirectlyTowards(BlockPos pPos) {
         BeeEntity.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
         BeeEntity.this.navigation.moveTo((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), 1.0D);
         return BeeEntity.this.navigation.getPath() != null && BeeEntity.this.navigation.getPath().canReach();
      }

      private boolean isTargetBlacklisted(BlockPos pPos) {
         return this.blacklistedTargets.contains(pPos);
      }

      private void blacklistTarget(BlockPos pPos) {
         this.blacklistedTargets.add(pPos);

         while(this.blacklistedTargets.size() > 3) {
            this.blacklistedTargets.remove(0);
         }

      }

      private void clearBlacklist() {
         this.blacklistedTargets.clear();
      }

      private void dropAndBlacklistHive() {
         if (BeeEntity.this.hivePos != null) {
            this.blacklistTarget(BeeEntity.this.hivePos);
         }

         this.dropHive();
      }

      private void dropHive() {
         BeeEntity.this.hivePos = null;
         BeeEntity.this.remainingCooldownBeforeLocatingNewHive = 200;
      }

      private boolean hasReachedTarget(BlockPos pPos) {
         if (BeeEntity.this.closerThan(pPos, 2)) {
            return true;
         } else {
            Path path = BeeEntity.this.navigation.getPath();
            return path != null && path.getTarget().equals(pPos) && path.canReach() && path.isDone();
         }
      }
   }

   public class FindFlowerGoal extends BeeEntity.PassiveGoal {
      private int travellingTicks = BeeEntity.this.level.random.nextInt(10);

      FindFlowerGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      public boolean canBeeUse() {
         return BeeEntity.this.savedFlowerPos != null && !BeeEntity.this.hasRestriction() && this.wantsToGoToKnownFlower() && BeeEntity.this.isFlowerValid(BeeEntity.this.savedFlowerPos) && !BeeEntity.this.closerThan(BeeEntity.this.savedFlowerPos, 2);
      }

      public boolean canBeeContinueToUse() {
         return this.canBeeUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.travellingTicks = 0;
         super.start();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.travellingTicks = 0;
         BeeEntity.this.navigation.stop();
         BeeEntity.this.navigation.resetMaxVisitedNodesMultiplier();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (BeeEntity.this.savedFlowerPos != null) {
            ++this.travellingTicks;
            if (this.travellingTicks > 600) {
               BeeEntity.this.savedFlowerPos = null;
            } else if (!BeeEntity.this.navigation.isInProgress()) {
               if (BeeEntity.this.isTooFarAway(BeeEntity.this.savedFlowerPos)) {
                  BeeEntity.this.savedFlowerPos = null;
               } else {
                  BeeEntity.this.pathfindRandomlyTowards(BeeEntity.this.savedFlowerPos);
               }
            }
         }
      }

      private boolean wantsToGoToKnownFlower() {
         return BeeEntity.this.ticksWithoutNectarSinceExitingHive > 2400;
      }
   }

   class FindPollinationTargetGoal extends BeeEntity.PassiveGoal {
      private FindPollinationTargetGoal() {
      }

      public boolean canBeeUse() {
         if (BeeEntity.this.getCropsGrownSincePollination() >= 10) {
            return false;
         } else if (BeeEntity.this.random.nextFloat() < 0.3F) {
            return false;
         } else {
            return BeeEntity.this.hasNectar() && BeeEntity.this.isHiveValid();
         }
      }

      public boolean canBeeContinueToUse() {
         return this.canBeeUse();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (BeeEntity.this.random.nextInt(30) == 0) {
            for(int i = 1; i <= 2; ++i) {
               BlockPos blockpos = BeeEntity.this.blockPosition().below(i);
               BlockState blockstate = BeeEntity.this.level.getBlockState(blockpos);
               Block block = blockstate.getBlock();
               boolean flag = false;
               IntegerProperty integerproperty = null;
               if (block.is(BlockTags.BEE_GROWABLES)) {
                  if (block instanceof CropsBlock) {
                     CropsBlock cropsblock = (CropsBlock)block;
                     if (!cropsblock.isMaxAge(blockstate)) {
                        flag = true;
                        integerproperty = cropsblock.getAgeProperty();
                     }
                  } else if (block instanceof StemBlock) {
                     int j = blockstate.getValue(StemBlock.AGE);
                     if (j < 7) {
                        flag = true;
                        integerproperty = StemBlock.AGE;
                     }
                  } else if (block == Blocks.SWEET_BERRY_BUSH) {
                     int k = blockstate.getValue(SweetBerryBushBlock.AGE);
                     if (k < 3) {
                        flag = true;
                        integerproperty = SweetBerryBushBlock.AGE;
                     }
                  }

                  if (flag) {
                     BeeEntity.this.level.levelEvent(2005, blockpos, 0);
                     BeeEntity.this.level.setBlockAndUpdate(blockpos, blockstate.setValue(integerproperty, Integer.valueOf(blockstate.getValue(integerproperty) + 1)));
                     BeeEntity.this.incrementNumCropsGrownSincePollination();
                  }
               }
            }

         }
      }
   }

   abstract class PassiveGoal extends Goal {
      private PassiveGoal() {
      }

      public abstract boolean canBeeUse();

      public abstract boolean canBeeContinueToUse();

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.canBeeUse() && !BeeEntity.this.isAngry();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.canBeeContinueToUse() && !BeeEntity.this.isAngry();
      }
   }

   class PollinateGoal extends BeeEntity.PassiveGoal {
      private final Predicate<BlockState> VALID_POLLINATION_BLOCKS = (p_226499_0_) -> {
         if (p_226499_0_.is(BlockTags.TALL_FLOWERS)) {
            if (p_226499_0_.is(Blocks.SUNFLOWER)) {
               return p_226499_0_.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
            } else {
               return true;
            }
         } else {
            return p_226499_0_.is(BlockTags.SMALL_FLOWERS);
         }
      };
      private int successfulPollinatingTicks = 0;
      private int lastSoundPlayedTick = 0;
      private boolean pollinating;
      private Vector3d hoverPos;
      private int pollinatingTicks = 0;

      PollinateGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      public boolean canBeeUse() {
         if (BeeEntity.this.remainingCooldownBeforeLocatingNewFlower > 0) {
            return false;
         } else if (BeeEntity.this.hasNectar()) {
            return false;
         } else if (BeeEntity.this.level.isRaining()) {
            return false;
         } else if (BeeEntity.this.random.nextFloat() < 0.7F) {
            return false;
         } else {
            Optional<BlockPos> optional = this.findNearbyFlower();
            if (optional.isPresent()) {
               BeeEntity.this.savedFlowerPos = optional.get();
               BeeEntity.this.navigation.moveTo((double)BeeEntity.this.savedFlowerPos.getX() + 0.5D, (double)BeeEntity.this.savedFlowerPos.getY() + 0.5D, (double)BeeEntity.this.savedFlowerPos.getZ() + 0.5D, (double)1.2F);
               return true;
            } else {
               return false;
            }
         }
      }

      public boolean canBeeContinueToUse() {
         if (!this.pollinating) {
            return false;
         } else if (!BeeEntity.this.hasSavedFlowerPos()) {
            return false;
         } else if (BeeEntity.this.level.isRaining()) {
            return false;
         } else if (this.hasPollinatedLongEnough()) {
            return BeeEntity.this.random.nextFloat() < 0.2F;
         } else if (BeeEntity.this.tickCount % 20 == 0 && !BeeEntity.this.isFlowerValid(BeeEntity.this.savedFlowerPos)) {
            BeeEntity.this.savedFlowerPos = null;
            return false;
         } else {
            return true;
         }
      }

      private boolean hasPollinatedLongEnough() {
         return this.successfulPollinatingTicks > 400;
      }

      private boolean isPollinating() {
         return this.pollinating;
      }

      private void stopPollinating() {
         this.pollinating = false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.successfulPollinatingTicks = 0;
         this.pollinatingTicks = 0;
         this.lastSoundPlayedTick = 0;
         this.pollinating = true;
         BeeEntity.this.resetTicksWithoutNectarSinceExitingHive();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         if (this.hasPollinatedLongEnough()) {
            BeeEntity.this.setHasNectar(true);
         }

         this.pollinating = false;
         BeeEntity.this.navigation.stop();
         BeeEntity.this.remainingCooldownBeforeLocatingNewFlower = 200;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         ++this.pollinatingTicks;
         if (this.pollinatingTicks > 600) {
            BeeEntity.this.savedFlowerPos = null;
         } else {
            Vector3d vector3d = Vector3d.atBottomCenterOf(BeeEntity.this.savedFlowerPos).add(0.0D, (double)0.6F, 0.0D);
            if (vector3d.distanceTo(BeeEntity.this.position()) > 1.0D) {
               this.hoverPos = vector3d;
               this.setWantedPos();
            } else {
               if (this.hoverPos == null) {
                  this.hoverPos = vector3d;
               }

               boolean flag = BeeEntity.this.position().distanceTo(this.hoverPos) <= 0.1D;
               boolean flag1 = true;
               if (!flag && this.pollinatingTicks > 600) {
                  BeeEntity.this.savedFlowerPos = null;
               } else {
                  if (flag) {
                     boolean flag2 = BeeEntity.this.random.nextInt(25) == 0;
                     if (flag2) {
                        this.hoverPos = new Vector3d(vector3d.x() + (double)this.getOffset(), vector3d.y(), vector3d.z() + (double)this.getOffset());
                        BeeEntity.this.navigation.stop();
                     } else {
                        flag1 = false;
                     }

                     BeeEntity.this.getLookControl().setLookAt(vector3d.x(), vector3d.y(), vector3d.z());
                  }

                  if (flag1) {
                     this.setWantedPos();
                  }

                  ++this.successfulPollinatingTicks;
                  if (BeeEntity.this.random.nextFloat() < 0.05F && this.successfulPollinatingTicks > this.lastSoundPlayedTick + 60) {
                     this.lastSoundPlayedTick = this.successfulPollinatingTicks;
                     BeeEntity.this.playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
                  }

               }
            }
         }
      }

      private void setWantedPos() {
         BeeEntity.this.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), (double)0.35F);
      }

      private float getOffset() {
         return (BeeEntity.this.random.nextFloat() * 2.0F - 1.0F) * 0.33333334F;
      }

      private Optional<BlockPos> findNearbyFlower() {
         return this.findNearestBlock(this.VALID_POLLINATION_BLOCKS, 5.0D);
      }

      private Optional<BlockPos> findNearestBlock(Predicate<BlockState> pPredicate, double pDistance) {
         BlockPos blockpos = BeeEntity.this.blockPosition();
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

         for(int i = 0; (double)i <= pDistance; i = i > 0 ? -i : 1 - i) {
            for(int j = 0; (double)j < pDistance; ++j) {
               for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                  for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                     blockpos$mutable.setWithOffset(blockpos, k, i - 1, l);
                     if (blockpos.closerThan(blockpos$mutable, pDistance) && pPredicate.test(BeeEntity.this.level.getBlockState(blockpos$mutable))) {
                        return Optional.of(blockpos$mutable);
                     }
                  }
               }
            }
         }

         return Optional.empty();
      }
   }

   class StingGoal extends MeleeAttackGoal {
      StingGoal(CreatureEntity pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeed) {
         super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeed);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && BeeEntity.this.isAngry() && !BeeEntity.this.hasStung();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse() && BeeEntity.this.isAngry() && !BeeEntity.this.hasStung();
      }
   }

   class UpdateBeehiveGoal extends BeeEntity.PassiveGoal {
      private UpdateBeehiveGoal() {
      }

      public boolean canBeeUse() {
         return BeeEntity.this.remainingCooldownBeforeLocatingNewHive == 0 && !BeeEntity.this.hasHive() && BeeEntity.this.wantsToEnterHive();
      }

      public boolean canBeeContinueToUse() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         BeeEntity.this.remainingCooldownBeforeLocatingNewHive = 200;
         List<BlockPos> list = this.findNearbyHivesWithSpace();
         if (!list.isEmpty()) {
            for(BlockPos blockpos : list) {
               if (!BeeEntity.this.goToHiveGoal.isTargetBlacklisted(blockpos)) {
                  BeeEntity.this.hivePos = blockpos;
                  return;
               }
            }

            BeeEntity.this.goToHiveGoal.clearBlacklist();
            BeeEntity.this.hivePos = list.get(0);
         }
      }

      private List<BlockPos> findNearbyHivesWithSpace() {
         BlockPos blockpos = BeeEntity.this.blockPosition();
         PointOfInterestManager pointofinterestmanager = ((ServerWorld)BeeEntity.this.level).getPoiManager();
         Stream<PointOfInterest> stream = pointofinterestmanager.getInRange((p_226486_0_) -> {
            return p_226486_0_ == PointOfInterestType.BEEHIVE || p_226486_0_ == PointOfInterestType.BEE_NEST;
         }, blockpos, 20, PointOfInterestManager.Status.ANY);
         return stream.map(PointOfInterest::getPos).filter((p_226487_1_) -> {
            return BeeEntity.this.doesHiveHaveSpace(p_226487_1_);
         }).sorted(Comparator.comparingDouble((p_226488_1_) -> {
            return p_226488_1_.distSqr(blockpos);
         })).collect(Collectors.toList());
      }
   }

   class WanderGoal extends Goal {
      WanderGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return BeeEntity.this.navigation.isDone() && BeeEntity.this.random.nextInt(10) == 0;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return BeeEntity.this.navigation.isInProgress();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Vector3d vector3d = this.findPos();
         if (vector3d != null) {
            BeeEntity.this.navigation.moveTo(BeeEntity.this.navigation.createPath(new BlockPos(vector3d), 1), 1.0D);
         }

      }

      @Nullable
      private Vector3d findPos() {
         Vector3d vector3d;
         if (BeeEntity.this.isHiveValid() && !BeeEntity.this.closerThan(BeeEntity.this.hivePos, 22)) {
            Vector3d vector3d1 = Vector3d.atCenterOf(BeeEntity.this.hivePos);
            vector3d = vector3d1.subtract(BeeEntity.this.position()).normalize();
         } else {
            vector3d = BeeEntity.this.getViewVector(0.0F);
         }

         int i = 8;
         Vector3d vector3d2 = RandomPositionGenerator.getAboveLandPos(BeeEntity.this, 8, 7, vector3d, ((float)Math.PI / 2F), 2, 1);
         return vector3d2 != null ? vector3d2 : RandomPositionGenerator.getAirPos(BeeEntity.this, 8, 4, -2, vector3d, (double)((float)Math.PI / 2F));
      }
   }
}
