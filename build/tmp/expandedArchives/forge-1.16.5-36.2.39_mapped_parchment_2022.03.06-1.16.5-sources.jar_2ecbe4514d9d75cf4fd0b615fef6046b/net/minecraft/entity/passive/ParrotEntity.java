package net.minecraft.entity.passive;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.goal.FollowMobGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ParrotEntity extends ShoulderRidingEntity implements IFlyingAnimal {
   private static final DataParameter<Integer> DATA_VARIANT_ID = EntityDataManager.defineId(ParrotEntity.class, DataSerializers.INT);
   private static final Predicate<MobEntity> NOT_PARROT_PREDICATE = new Predicate<MobEntity>() {
      public boolean test(@Nullable MobEntity p_test_1_) {
         return p_test_1_ != null && ParrotEntity.MOB_SOUND_MAP.containsKey(p_test_1_.getType());
      }
   };
   private static final Item POISONOUS_FOOD = Items.COOKIE;
   private static final Set<Item> TAME_FOOD = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
   private static final Map<EntityType<?>, SoundEvent> MOB_SOUND_MAP = Util.make(Maps.newHashMap(), (p_200609_0_) -> {
      p_200609_0_.put(EntityType.BLAZE, SoundEvents.PARROT_IMITATE_BLAZE);
      p_200609_0_.put(EntityType.CAVE_SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
      p_200609_0_.put(EntityType.CREEPER, SoundEvents.PARROT_IMITATE_CREEPER);
      p_200609_0_.put(EntityType.DROWNED, SoundEvents.PARROT_IMITATE_DROWNED);
      p_200609_0_.put(EntityType.ELDER_GUARDIAN, SoundEvents.PARROT_IMITATE_ELDER_GUARDIAN);
      p_200609_0_.put(EntityType.ENDER_DRAGON, SoundEvents.PARROT_IMITATE_ENDER_DRAGON);
      p_200609_0_.put(EntityType.ENDERMITE, SoundEvents.PARROT_IMITATE_ENDERMITE);
      p_200609_0_.put(EntityType.EVOKER, SoundEvents.PARROT_IMITATE_EVOKER);
      p_200609_0_.put(EntityType.GHAST, SoundEvents.PARROT_IMITATE_GHAST);
      p_200609_0_.put(EntityType.GUARDIAN, SoundEvents.PARROT_IMITATE_GUARDIAN);
      p_200609_0_.put(EntityType.HOGLIN, SoundEvents.PARROT_IMITATE_HOGLIN);
      p_200609_0_.put(EntityType.HUSK, SoundEvents.PARROT_IMITATE_HUSK);
      p_200609_0_.put(EntityType.ILLUSIONER, SoundEvents.PARROT_IMITATE_ILLUSIONER);
      p_200609_0_.put(EntityType.MAGMA_CUBE, SoundEvents.PARROT_IMITATE_MAGMA_CUBE);
      p_200609_0_.put(EntityType.PHANTOM, SoundEvents.PARROT_IMITATE_PHANTOM);
      p_200609_0_.put(EntityType.PIGLIN, SoundEvents.PARROT_IMITATE_PIGLIN);
      p_200609_0_.put(EntityType.PIGLIN_BRUTE, SoundEvents.PARROT_IMITATE_PIGLIN_BRUTE);
      p_200609_0_.put(EntityType.PILLAGER, SoundEvents.PARROT_IMITATE_PILLAGER);
      p_200609_0_.put(EntityType.RAVAGER, SoundEvents.PARROT_IMITATE_RAVAGER);
      p_200609_0_.put(EntityType.SHULKER, SoundEvents.PARROT_IMITATE_SHULKER);
      p_200609_0_.put(EntityType.SILVERFISH, SoundEvents.PARROT_IMITATE_SILVERFISH);
      p_200609_0_.put(EntityType.SKELETON, SoundEvents.PARROT_IMITATE_SKELETON);
      p_200609_0_.put(EntityType.SLIME, SoundEvents.PARROT_IMITATE_SLIME);
      p_200609_0_.put(EntityType.SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
      p_200609_0_.put(EntityType.STRAY, SoundEvents.PARROT_IMITATE_STRAY);
      p_200609_0_.put(EntityType.VEX, SoundEvents.PARROT_IMITATE_VEX);
      p_200609_0_.put(EntityType.VINDICATOR, SoundEvents.PARROT_IMITATE_VINDICATOR);
      p_200609_0_.put(EntityType.WITCH, SoundEvents.PARROT_IMITATE_WITCH);
      p_200609_0_.put(EntityType.WITHER, SoundEvents.PARROT_IMITATE_WITHER);
      p_200609_0_.put(EntityType.WITHER_SKELETON, SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
      p_200609_0_.put(EntityType.ZOGLIN, SoundEvents.PARROT_IMITATE_ZOGLIN);
      p_200609_0_.put(EntityType.ZOMBIE, SoundEvents.PARROT_IMITATE_ZOMBIE);
      p_200609_0_.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.PARROT_IMITATE_ZOMBIE_VILLAGER);
   });
   public float flap;
   public float flapSpeed;
   public float oFlapSpeed;
   public float oFlap;
   private float flapping = 1.0F;
   private boolean partyParrot;
   private BlockPos jukebox;

   public ParrotEntity(EntityType<? extends ParrotEntity> p_i50251_1_, World p_i50251_2_) {
      super(p_i50251_1_, p_i50251_2_);
      this.moveControl = new FlyingMovementController(this, 10, false);
      this.setPathfindingMalus(PathNodeType.DANGER_FIRE, -1.0F);
      this.setPathfindingMalus(PathNodeType.DAMAGE_FIRE, -1.0F);
      this.setPathfindingMalus(PathNodeType.COCOA, -1.0F);
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      this.setVariant(this.random.nextInt(5));
      if (pSpawnData == null) {
         pSpawnData = new AgeableEntity.AgeableData(false);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return false;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new PanicGoal(this, 1.25D));
      this.goalSelector.addGoal(0, new SwimGoal(this));
      this.goalSelector.addGoal(1, new LookAtGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.addGoal(2, new SitGoal(this));
      this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D, 5.0F, 1.0F, true));
      this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
      this.goalSelector.addGoal(3, new FollowMobGoal(this, 1.0D, 3.0F, 7.0F));
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0D).add(Attributes.FLYING_SPEED, (double)0.4F).add(Attributes.MOVEMENT_SPEED, (double)0.2F);
   }

   /**
    * Returns new PathNavigateGround instance
    */
   protected PathNavigator createNavigation(World pLevel) {
      FlyingPathNavigator flyingpathnavigator = new FlyingPathNavigator(this, pLevel);
      flyingpathnavigator.setCanOpenDoors(false);
      flyingpathnavigator.setCanFloat(true);
      flyingpathnavigator.setCanPassDoors(true);
      return flyingpathnavigator;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return pSize.height * 0.6F;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.jukebox == null || !this.jukebox.closerThan(this.position(), 3.46D) || !this.level.getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
         this.partyParrot = false;
         this.jukebox = null;
      }

      if (this.level.random.nextInt(400) == 0) {
         imitateNearbyMobs(this.level, this);
      }

      super.aiStep();
      this.calculateFlapping();
   }

   /**
    * Called when a record starts or stops playing. Used to make parrots start or stop partying.
    */
   @OnlyIn(Dist.CLIENT)
   public void setRecordPlayingNearby(BlockPos pPos, boolean pIsPartying) {
      this.jukebox = pPos;
      this.partyParrot = pIsPartying;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isPartyParrot() {
      return this.partyParrot;
   }

   private void calculateFlapping() {
      this.oFlap = this.flap;
      this.oFlapSpeed = this.flapSpeed;
      this.flapSpeed = (float)((double)this.flapSpeed + (double)(!this.onGround && !this.isPassenger() ? 4 : -1) * 0.3D);
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
   }

   public static boolean imitateNearbyMobs(World pLevel, Entity pParrot) {
      if (pParrot.isAlive() && !pParrot.isSilent() && pLevel.random.nextInt(2) == 0) {
         List<MobEntity> list = pLevel.getEntitiesOfClass(MobEntity.class, pParrot.getBoundingBox().inflate(20.0D), NOT_PARROT_PREDICATE);
         if (!list.isEmpty()) {
            MobEntity mobentity = list.get(pLevel.random.nextInt(list.size()));
            if (!mobentity.isSilent()) {
               SoundEvent soundevent = getImitatedSound(mobentity.getType());
               pLevel.playSound((PlayerEntity)null, pParrot.getX(), pParrot.getY(), pParrot.getZ(), soundevent, pParrot.getSoundSource(), 0.7F, getPitch(pLevel.random));
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!this.isTame() && TAME_FOOD.contains(itemstack.getItem())) {
         if (!pPlayer.abilities.instabuild) {
            itemstack.shrink(1);
         }

         if (!this.isSilent()) {
            this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PARROT_EAT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }

         if (!this.level.isClientSide) {
            if (this.random.nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
               this.tame(pPlayer);
               this.level.broadcastEntityEvent(this, (byte)7);
            } else {
               this.level.broadcastEntityEvent(this, (byte)6);
            }
         }

         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else if (itemstack.getItem() == POISONOUS_FOOD) {
         if (!pPlayer.abilities.instabuild) {
            itemstack.shrink(1);
         }

         this.addEffect(new EffectInstance(Effects.POISON, 900));
         if (pPlayer.isCreative() || !this.isInvulnerable()) {
            this.hurt(DamageSource.playerAttack(pPlayer), Float.MAX_VALUE);
         }

         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else if (!this.isFlying() && this.isTame() && this.isOwnedBy(pPlayer)) {
         if (!this.level.isClientSide) {
            this.setOrderedToSit(!this.isOrderedToSit());
         }

         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return false;
   }

   public static boolean checkParrotSpawnRules(EntityType<ParrotEntity> pParrot, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      return (blockstate.is(BlockTags.LEAVES) || blockstate.is(Blocks.GRASS_BLOCK) || blockstate.is(BlockTags.LOGS) || blockstate.is(Blocks.AIR)) && pLevel.getRawBrightness(pPos, 0) > 8;
   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      return false;
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
   }

   /**
    * Returns true if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(AnimalEntity pOtherAnimal) {
      return false;
   }

   @Nullable
   public AgeableEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      return null;
   }

   public boolean doHurtTarget(Entity pEntity) {
      return pEntity.hurt(DamageSource.mobAttack(this), 3.0F);
   }

   @Nullable
   public SoundEvent getAmbientSound() {
      return getAmbient(this.level, this.level.random);
   }

   public static SoundEvent getAmbient(World pLevel, Random pRandom) {
      if (pLevel.getDifficulty() != Difficulty.PEACEFUL && pRandom.nextInt(1000) == 0) {
         List<EntityType<?>> list = Lists.newArrayList(MOB_SOUND_MAP.keySet());
         return getImitatedSound(list.get(pRandom.nextInt(list.size())));
      } else {
         return SoundEvents.PARROT_AMBIENT;
      }
   }

   private static SoundEvent getImitatedSound(EntityType<?> pType) {
      return MOB_SOUND_MAP.getOrDefault(pType, SoundEvents.PARROT_AMBIENT);
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.PARROT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PARROT_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.PARROT_STEP, 0.15F, 1.0F);
   }

   protected float playFlySound(float pVolume) {
      this.playSound(SoundEvents.PARROT_FLY, 0.15F, 1.0F);
      return pVolume + this.flapSpeed / 2.0F;
   }

   protected boolean makeFlySound() {
      return true;
   }

   /**
    * Gets the pitch of living sounds in living entities.
    */
   protected float getVoicePitch() {
      return getPitch(this.random);
   }

   public static float getPitch(Random pRandom) {
      return (pRandom.nextFloat() - pRandom.nextFloat()) * 0.2F + 1.0F;
   }

   public SoundCategory getSoundSource() {
      return SoundCategory.NEUTRAL;
   }

   /**
    * Returns true if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return true;
   }

   protected void doPush(Entity pEntity) {
      if (!(pEntity instanceof PlayerEntity)) {
         super.doPush(pEntity);
      }
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         this.setOrderedToSit(false);
         return super.hurt(pSource, pAmount);
      }
   }

   public int getVariant() {
      return MathHelper.clamp(this.entityData.get(DATA_VARIANT_ID), 0, 4);
   }

   public void setVariant(int pVariant) {
      this.entityData.set(DATA_VARIANT_ID, pVariant);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_VARIANT_ID, 0);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Variant", this.getVariant());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setVariant(pCompound.getInt("Variant"));
   }

   public boolean isFlying() {
      return !this.onGround;
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getLeashOffset() {
      return new Vector3d(0.0D, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }
}
