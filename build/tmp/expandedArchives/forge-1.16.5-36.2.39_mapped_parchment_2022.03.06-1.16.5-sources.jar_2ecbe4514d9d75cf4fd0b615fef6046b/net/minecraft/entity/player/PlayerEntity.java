package net.minecraft.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffers;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.JigsawTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.FoodStats;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PlayerEntity extends LivingEntity {
   public static final String PERSISTED_NBT_TAG = "PlayerPersisted";
   public static final EntitySize STANDING_DIMENSIONS = EntitySize.scalable(0.6F, 1.8F);
   private static final Map<Pose, EntitySize> POSES = ImmutableMap.<Pose, EntitySize>builder().put(Pose.STANDING, STANDING_DIMENSIONS).put(Pose.SLEEPING, SLEEPING_DIMENSIONS).put(Pose.FALL_FLYING, EntitySize.scalable(0.6F, 0.6F)).put(Pose.SWIMMING, EntitySize.scalable(0.6F, 0.6F)).put(Pose.SPIN_ATTACK, EntitySize.scalable(0.6F, 0.6F)).put(Pose.CROUCHING, EntitySize.scalable(0.6F, 1.5F)).put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F)).build();
   private static final DataParameter<Float> DATA_PLAYER_ABSORPTION_ID = EntityDataManager.defineId(PlayerEntity.class, DataSerializers.FLOAT);
   private static final DataParameter<Integer> DATA_SCORE_ID = EntityDataManager.defineId(PlayerEntity.class, DataSerializers.INT);
   protected static final DataParameter<Byte> DATA_PLAYER_MODE_CUSTOMISATION = EntityDataManager.defineId(PlayerEntity.class, DataSerializers.BYTE);
   protected static final DataParameter<Byte> DATA_PLAYER_MAIN_HAND = EntityDataManager.defineId(PlayerEntity.class, DataSerializers.BYTE);
   protected static final DataParameter<CompoundNBT> DATA_SHOULDER_LEFT = EntityDataManager.defineId(PlayerEntity.class, DataSerializers.COMPOUND_TAG);
   protected static final DataParameter<CompoundNBT> DATA_SHOULDER_RIGHT = EntityDataManager.defineId(PlayerEntity.class, DataSerializers.COMPOUND_TAG);
   private long timeEntitySatOnShoulder;
   public final PlayerInventory inventory = new PlayerInventory(this);
   protected EnderChestInventory enderChestInventory = new EnderChestInventory();
   public final PlayerContainer inventoryMenu;
   public Container containerMenu;
   protected FoodStats foodData = new FoodStats();
   protected int jumpTriggerTime;
   public float oBob;
   public float bob;
   public int takeXpDelay;
   public double xCloakO;
   public double yCloakO;
   public double zCloakO;
   public double xCloak;
   public double yCloak;
   public double zCloak;
   private int sleepCounter;
   protected boolean wasUnderwater;
   public final PlayerAbilities abilities = new PlayerAbilities();
   public int experienceLevel;
   public int totalExperience;
   public float experienceProgress;
   protected int enchantmentSeed;
   protected final float defaultFlySpeed = 0.02F;
   private int lastLevelUpTime;
   /** The player's unique game profile */
   private final GameProfile gameProfile;
   @OnlyIn(Dist.CLIENT)
   private boolean reducedDebugInfo;
   private ItemStack lastItemInMainHand = ItemStack.EMPTY;
   private final CooldownTracker cooldowns = this.createItemCooldowns();
   @Nullable
   public FishingBobberEntity fishing;
   private final java.util.Collection<IFormattableTextComponent> prefixes = new java.util.LinkedList<>();
   private final java.util.Collection<IFormattableTextComponent> suffixes = new java.util.LinkedList<>();
   @Nullable private Pose forcedPose;

   public PlayerEntity(World pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
      super(EntityType.PLAYER, pLevel);
      this.setUUID(createPlayerUUID(pGameProfile));
      this.gameProfile = pGameProfile;
      this.inventoryMenu = new PlayerContainer(this.inventory, !pLevel.isClientSide, this);
      this.containerMenu = this.inventoryMenu;
      this.moveTo((double)pPos.getX() + 0.5D, (double)(pPos.getY() + 1), (double)pPos.getZ() + 0.5D, pYRot, 0.0F);
      this.rotOffs = 180.0F;
   }

   public boolean blockActionRestricted(World pLevel, BlockPos pPos, GameType pGameMode) {
      if (!pGameMode.isBlockPlacingRestricted()) {
         return false;
      } else if (pGameMode == GameType.SPECTATOR) {
         return true;
      } else if (this.mayBuild()) {
         return false;
      } else {
         ItemStack itemstack = this.getMainHandItem();
         return itemstack.isEmpty() || !itemstack.hasAdventureModeBreakTagForBlock(pLevel.getTagManager(), new CachedBlockInfo(pLevel, pPos, false));
      }
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.MOVEMENT_SPEED, (double)0.1F).add(Attributes.ATTACK_SPEED).add(Attributes.LUCK).add(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).add(Attributes.ATTACK_KNOCKBACK);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_PLAYER_ABSORPTION_ID, 0.0F);
      this.entityData.define(DATA_SCORE_ID, 0);
      this.entityData.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte)0);
      this.entityData.define(DATA_PLAYER_MAIN_HAND, (byte)1);
      this.entityData.define(DATA_SHOULDER_LEFT, new CompoundNBT());
      this.entityData.define(DATA_SHOULDER_RIGHT, new CompoundNBT());
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      net.minecraftforge.fml.hooks.BasicEventHooks.onPlayerPreTick(this);
      this.noPhysics = this.isSpectator();
      if (this.isSpectator()) {
         this.onGround = false;
      }

      if (this.takeXpDelay > 0) {
         --this.takeXpDelay;
      }

      if (this.isSleeping()) {
         ++this.sleepCounter;
         if (this.sleepCounter > 100) {
            this.sleepCounter = 100;
         }

         if (!this.level.isClientSide && !net.minecraftforge.event.ForgeEventFactory.fireSleepingTimeCheck(this, getSleepingPos())) {
            this.stopSleepInBed(false, true);
         }
      } else if (this.sleepCounter > 0) {
         ++this.sleepCounter;
         if (this.sleepCounter >= 110) {
            this.sleepCounter = 0;
         }
      }

      this.updateIsUnderwater();
      super.tick();
      if (!this.level.isClientSide && this.containerMenu != null && !this.containerMenu.stillValid(this)) {
         this.closeContainer();
         this.containerMenu = this.inventoryMenu;
      }

      this.moveCloak();
      if (!this.level.isClientSide) {
         this.foodData.tick(this);
         this.awardStat(Stats.PLAY_ONE_MINUTE);
         if (this.isAlive()) {
            this.awardStat(Stats.TIME_SINCE_DEATH);
         }

         if (this.isDiscrete()) {
            this.awardStat(Stats.CROUCH_TIME);
         }

         if (!this.isSleeping()) {
            this.awardStat(Stats.TIME_SINCE_REST);
         }
      }

      int i = 29999999;
      double d0 = MathHelper.clamp(this.getX(), -2.9999999E7D, 2.9999999E7D);
      double d1 = MathHelper.clamp(this.getZ(), -2.9999999E7D, 2.9999999E7D);
      if (d0 != this.getX() || d1 != this.getZ()) {
         this.setPos(d0, this.getY(), d1);
      }

      ++this.attackStrengthTicker;
      ItemStack itemstack = this.getMainHandItem();
      if (!ItemStack.matches(this.lastItemInMainHand, itemstack)) {
         if (!ItemStack.isSameIgnoreDurability(this.lastItemInMainHand, itemstack)) {
            this.resetAttackStrengthTicker();
         }

         this.lastItemInMainHand = itemstack.copy();
      }

      this.turtleHelmetTick();
      this.cooldowns.tick();
      this.updatePlayerPose();
      net.minecraftforge.fml.hooks.BasicEventHooks.onPlayerPostTick(this);
   }

   public boolean isSecondaryUseActive() {
      return this.isShiftKeyDown();
   }

   protected boolean wantsToStopRiding() {
      return this.isShiftKeyDown();
   }

   protected boolean isStayingOnGroundSurface() {
      return this.isShiftKeyDown();
   }

   protected boolean updateIsUnderwater() {
      this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
      return this.wasUnderwater;
   }

   private void turtleHelmetTick() {
      ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.HEAD);
      if (itemstack.getItem() == Items.TURTLE_HELMET && !this.isEyeInFluid(FluidTags.WATER)) {
         this.addEffect(new EffectInstance(Effects.WATER_BREATHING, 200, 0, false, false, true));
      }

   }

   protected CooldownTracker createItemCooldowns() {
      return new CooldownTracker();
   }

   private void moveCloak() {
      this.xCloakO = this.xCloak;
      this.yCloakO = this.yCloak;
      this.zCloakO = this.zCloak;
      double d0 = this.getX() - this.xCloak;
      double d1 = this.getY() - this.yCloak;
      double d2 = this.getZ() - this.zCloak;
      double d3 = 10.0D;
      if (d0 > 10.0D) {
         this.xCloak = this.getX();
         this.xCloakO = this.xCloak;
      }

      if (d2 > 10.0D) {
         this.zCloak = this.getZ();
         this.zCloakO = this.zCloak;
      }

      if (d1 > 10.0D) {
         this.yCloak = this.getY();
         this.yCloakO = this.yCloak;
      }

      if (d0 < -10.0D) {
         this.xCloak = this.getX();
         this.xCloakO = this.xCloak;
      }

      if (d2 < -10.0D) {
         this.zCloak = this.getZ();
         this.zCloakO = this.zCloak;
      }

      if (d1 < -10.0D) {
         this.yCloak = this.getY();
         this.yCloakO = this.yCloak;
      }

      this.xCloak += d0 * 0.25D;
      this.zCloak += d2 * 0.25D;
      this.yCloak += d1 * 0.25D;
   }

   protected void updatePlayerPose() {
      if(forcedPose != null) {
         this.setPose(forcedPose);
         return;
      }
      if (this.canEnterPose(Pose.SWIMMING)) {
         Pose pose;
         if (this.isFallFlying()) {
            pose = Pose.FALL_FLYING;
         } else if (this.isSleeping()) {
            pose = Pose.SLEEPING;
         } else if (this.isSwimming()) {
            pose = Pose.SWIMMING;
         } else if (this.isAutoSpinAttack()) {
            pose = Pose.SPIN_ATTACK;
         } else if (this.isShiftKeyDown() && !this.abilities.flying) {
            pose = Pose.CROUCHING;
         } else {
            pose = Pose.STANDING;
         }

         Pose pose1;
         if (!this.isSpectator() && !this.isPassenger() && !this.canEnterPose(pose)) {
            if (this.canEnterPose(Pose.CROUCHING)) {
               pose1 = Pose.CROUCHING;
            } else {
               pose1 = Pose.SWIMMING;
            }
         } else {
            pose1 = pose;
         }

         this.setPose(pose1);
      }
   }

   /**
    * Return the amount of time this entity should stay in a portal before being transported.
    */
   public int getPortalWaitTime() {
      return this.abilities.invulnerable ? 1 : 80;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.PLAYER_SWIM;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.PLAYER_SPLASH;
   }

   protected SoundEvent getSwimHighSpeedSplashSound() {
      return SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
   }

   /**
    * Return the amount of cooldown before this entity can use a portal again.
    */
   public int getDimensionChangingDelay() {
      return 10;
   }

   public void playSound(SoundEvent pSound, float pVolume, float pPitch) {
      this.level.playSound(this, this.getX(), this.getY(), this.getZ(), pSound, this.getSoundSource(), pVolume, pPitch);
   }

   public void playNotifySound(SoundEvent pSound, SoundCategory pSource, float pVolume, float pPitch) {
   }

   public SoundCategory getSoundSource() {
      return SoundCategory.PLAYERS;
   }

   protected int getFireImmuneTicks() {
      return 20;
   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 9) {
         this.completeUsingItem();
      } else if (pId == 23) {
         this.reducedDebugInfo = false;
      } else if (pId == 22) {
         this.reducedDebugInfo = true;
      } else if (pId == 43) {
         this.addParticlesAroundSelf(ParticleTypes.CLOUD);
      } else {
         super.handleEntityEvent(pId);
      }

   }

   @OnlyIn(Dist.CLIENT)
   private void addParticlesAroundSelf(IParticleData pParticleOption) {
      for(int i = 0; i < 5; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level.addParticle(pParticleOption, this.getRandomX(1.0D), this.getRandomY() + 1.0D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   /**
    * set current crafting inventory back to the 2x2 square
    */
   public void closeContainer() {
      this.containerMenu = this.inventoryMenu;
   }

   /**
    * Handles updating while riding another entity
    */
   public void rideTick() {
      if (this.wantsToStopRiding() && this.isPassenger()) {
         this.stopRiding();
         this.setShiftKeyDown(false);
      } else {
         double d0 = this.getX();
         double d1 = this.getY();
         double d2 = this.getZ();
         super.rideTick();
         this.oBob = this.bob;
         this.bob = 0.0F;
         this.checkRidingStatistics(this.getX() - d0, this.getY() - d1, this.getZ() - d2);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void resetPos() {
      this.setPose(Pose.STANDING);
      super.resetPos();
      this.setHealth(this.getMaxHealth());
      this.deathTime = 0;
   }

   protected void serverAiStep() {
      super.serverAiStep();
      this.updateSwingTime();
      this.yHeadRot = this.yRot;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.jumpTriggerTime > 0) {
         --this.jumpTriggerTime;
      }

      if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
         if (this.getHealth() < this.getMaxHealth() && this.tickCount % 20 == 0) {
            this.heal(1.0F);
         }

         if (this.foodData.needsFood() && this.tickCount % 10 == 0) {
            this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
         }
      }

      this.inventory.tick();
      this.oBob = this.bob;
      super.aiStep();
      this.flyingSpeed = 0.02F;
      if (this.isSprinting()) {
         this.flyingSpeed = (float)((double)this.flyingSpeed + 0.005999999865889549D);
      }

      this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
      float f;
      if (this.onGround && !this.isDeadOrDying() && !this.isSwimming()) {
         f = Math.min(0.1F, MathHelper.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement())));
      } else {
         f = 0.0F;
      }

      this.bob += (f - this.bob) * 0.4F;
      if (this.getHealth() > 0.0F && !this.isSpectator()) {
         AxisAlignedBB axisalignedbb;
         if (this.isPassenger() && !this.getVehicle().removed) {
            axisalignedbb = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0D, 0.0D, 1.0D);
         } else {
            axisalignedbb = this.getBoundingBox().inflate(1.0D, 0.5D, 1.0D);
         }

         List<Entity> list = this.level.getEntities(this, axisalignedbb);

         for(int i = 0; i < list.size(); ++i) {
            Entity entity = list.get(i);
            if (!entity.removed) {
               this.touch(entity);
            }
         }
      }

      this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
      this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
      if (!this.level.isClientSide && (this.fallDistance > 0.5F || this.isInWater()) || this.abilities.flying || this.isSleeping()) {
         this.removeEntitiesOnShoulder();
      }

   }

   private void playShoulderEntityAmbientSound(@Nullable CompoundNBT pEntityCompound) {
      if (pEntityCompound != null && (!pEntityCompound.contains("Silent") || !pEntityCompound.getBoolean("Silent")) && this.level.random.nextInt(200) == 0) {
         String s = pEntityCompound.getString("id");
         EntityType.byString(s).filter((p_213830_0_) -> {
            return p_213830_0_ == EntityType.PARROT;
         }).ifPresent((p_213834_1_) -> {
            if (!ParrotEntity.imitateNearbyMobs(this.level, this)) {
               this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), ParrotEntity.getAmbient(this.level, this.level.random), this.getSoundSource(), 1.0F, ParrotEntity.getPitch(this.level.random));
            }

         });
      }

   }

   private void touch(Entity pEntity) {
      pEntity.playerTouch(this);
   }

   public int getScore() {
      return this.entityData.get(DATA_SCORE_ID);
   }

   /**
    * Set player's score
    */
   public void setScore(int pScore) {
      this.entityData.set(DATA_SCORE_ID, pScore);
   }

   /**
    * Add to player's score
    */
   public void increaseScore(int pScore) {
      int i = this.getScore();
      this.entityData.set(DATA_SCORE_ID, i + pScore);
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      if (net.minecraftforge.common.ForgeHooks.onLivingDeath(this,  pCause)) return;
      super.die(pCause);
      this.reapplyPosition();
      if (!this.isSpectator()) {
         this.dropAllDeathLoot(pCause);
      }

      if (pCause != null) {
         this.setDeltaMovement((double)(-MathHelper.cos((this.hurtDir + this.yRot) * ((float)Math.PI / 180F)) * 0.1F), (double)0.1F, (double)(-MathHelper.sin((this.hurtDir + this.yRot) * ((float)Math.PI / 180F)) * 0.1F));
      } else {
         this.setDeltaMovement(0.0D, 0.1D, 0.0D);
      }

      this.awardStat(Stats.DEATHS);
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
      this.clearFire();
      this.setSharedFlag(0, false);
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (!this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
         this.destroyVanishingCursedItems();
         this.inventory.dropAll();
      }

   }

   protected void destroyVanishingCursedItems() {
      for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
         ItemStack itemstack = this.inventory.getItem(i);
         if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
            this.inventory.removeItemNoUpdate(i);
         }
      }

   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      if (pDamageSource == DamageSource.ON_FIRE) {
         return SoundEvents.PLAYER_HURT_ON_FIRE;
      } else if (pDamageSource == DamageSource.DROWN) {
         return SoundEvents.PLAYER_HURT_DROWN;
      } else {
         return pDamageSource == DamageSource.SWEET_BERRY_BUSH ? SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH : SoundEvents.PLAYER_HURT;
      }
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PLAYER_DEATH;
   }

   public boolean drop(boolean pHasControlDown) {
      ItemStack stack = inventory.getSelected();
      if (stack.isEmpty() || !stack.onDroppedByPlayer(this)) return false;
      return net.minecraftforge.common.ForgeHooks.onPlayerTossEvent(this, this.inventory.removeItem(this.inventory.selected, pHasControlDown && !this.inventory.getSelected().isEmpty() ? this.inventory.getSelected().getCount() : 1), true) != null;
   }

   /**
    * Drops an item into the world.
    */
   @Nullable
   public ItemEntity drop(ItemStack pItemStack, boolean pIncludeThrowerName) {
      return net.minecraftforge.common.ForgeHooks.onPlayerTossEvent(this, pItemStack, pIncludeThrowerName);
   }

   /**
    * Creates and drops the provided item. Depending on the dropAround, it will drop teh item around the player, instead
    * of dropping the item from where the player is pointing at. Likewise, if traceItem is true, the dropped item entity
    * will have the thrower set as the player.
    */
   @Nullable
   public ItemEntity drop(ItemStack pDroppedItem, boolean pDropAround, boolean pTraceItem) {
      if (pDroppedItem.isEmpty()) {
         return null;
      } else {
         if (this.level.isClientSide) {
            this.swing(Hand.MAIN_HAND);
         }

         double d0 = this.getEyeY() - (double)0.3F;
         ItemEntity itementity = new ItemEntity(this.level, this.getX(), d0, this.getZ(), pDroppedItem);
         itementity.setPickUpDelay(40);
         if (pTraceItem) {
            itementity.setThrower(this.getUUID());
         }

         if (pDropAround) {
            float f = this.random.nextFloat() * 0.5F;
            float f1 = this.random.nextFloat() * ((float)Math.PI * 2F);
            itementity.setDeltaMovement((double)(-MathHelper.sin(f1) * f), (double)0.2F, (double)(MathHelper.cos(f1) * f));
         } else {
            float f7 = 0.3F;
            float f8 = MathHelper.sin(this.xRot * ((float)Math.PI / 180F));
            float f2 = MathHelper.cos(this.xRot * ((float)Math.PI / 180F));
            float f3 = MathHelper.sin(this.yRot * ((float)Math.PI / 180F));
            float f4 = MathHelper.cos(this.yRot * ((float)Math.PI / 180F));
            float f5 = this.random.nextFloat() * ((float)Math.PI * 2F);
            float f6 = 0.02F * this.random.nextFloat();
            itementity.setDeltaMovement((double)(-f3 * f2 * 0.3F) + Math.cos((double)f5) * (double)f6, (double)(-f8 * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double)(f4 * f2 * 0.3F) + Math.sin((double)f5) * (double)f6);
         }

         return itementity;
      }
   }

   @Deprecated //Use location sensitive version below
   public float getDestroySpeed(BlockState pState) {
      return getDigSpeed(pState, null);
   }

   public float getDigSpeed(BlockState pState, @Nullable BlockPos pos) {
      float f = this.inventory.getDestroySpeed(pState);
      if (f > 1.0F) {
         int i = EnchantmentHelper.getBlockEfficiency(this);
         ItemStack itemstack = this.getMainHandItem();
         if (i > 0 && !itemstack.isEmpty()) {
            f += (float)(i * i + 1);
         }
      }

      if (EffectUtils.hasDigSpeed(this)) {
         f *= 1.0F + (float)(EffectUtils.getDigSpeedAmplification(this) + 1) * 0.2F;
      }

      if (this.hasEffect(Effects.DIG_SLOWDOWN)) {
         float f1;
         switch(this.getEffect(Effects.DIG_SLOWDOWN).getAmplifier()) {
         case 0:
            f1 = 0.3F;
            break;
         case 1:
            f1 = 0.09F;
            break;
         case 2:
            f1 = 0.0027F;
            break;
         case 3:
         default:
            f1 = 8.1E-4F;
         }

         f *= f1;
      }

      if (this.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
         f /= 5.0F;
      }

      if (!this.onGround) {
         f /= 5.0F;
      }

      f = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed(this, pState, f, pos);
      return f;
   }

   public boolean hasCorrectToolForDrops(BlockState pState) {
      return net.minecraftforge.event.ForgeEventFactory.doPlayerHarvestCheck(this, pState, !pState.requiresCorrectToolForDrops() || this.inventory.getSelected().isCorrectToolForDrops(pState));
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setUUID(createPlayerUUID(this.gameProfile));
      ListNBT listnbt = pCompound.getList("Inventory", 10);
      this.inventory.load(listnbt);
      this.inventory.selected = pCompound.getInt("SelectedItemSlot");
      this.sleepCounter = pCompound.getShort("SleepTimer");
      this.experienceProgress = pCompound.getFloat("XpP");
      this.experienceLevel = pCompound.getInt("XpLevel");
      this.totalExperience = pCompound.getInt("XpTotal");
      this.enchantmentSeed = pCompound.getInt("XpSeed");
      if (this.enchantmentSeed == 0) {
         this.enchantmentSeed = this.random.nextInt();
      }

      this.setScore(pCompound.getInt("Score"));
      this.foodData.readAdditionalSaveData(pCompound);
      this.abilities.loadSaveData(pCompound);
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)this.abilities.getWalkingSpeed());
      if (pCompound.contains("EnderItems", 9)) {
         this.enderChestInventory.fromTag(pCompound.getList("EnderItems", 10));
      }

      if (pCompound.contains("ShoulderEntityLeft", 10)) {
         this.setShoulderEntityLeft(pCompound.getCompound("ShoulderEntityLeft"));
      }

      if (pCompound.contains("ShoulderEntityRight", 10)) {
         this.setShoulderEntityRight(pCompound.getCompound("ShoulderEntityRight"));
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
      pCompound.put("Inventory", this.inventory.save(new ListNBT()));
      pCompound.putInt("SelectedItemSlot", this.inventory.selected);
      pCompound.putShort("SleepTimer", (short)this.sleepCounter);
      pCompound.putFloat("XpP", this.experienceProgress);
      pCompound.putInt("XpLevel", this.experienceLevel);
      pCompound.putInt("XpTotal", this.totalExperience);
      pCompound.putInt("XpSeed", this.enchantmentSeed);
      pCompound.putInt("Score", this.getScore());
      this.foodData.addAdditionalSaveData(pCompound);
      this.abilities.addSaveData(pCompound);
      pCompound.put("EnderItems", this.enderChestInventory.createTag());
      if (!this.getShoulderEntityLeft().isEmpty()) {
         pCompound.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
      }

      if (!this.getShoulderEntityRight().isEmpty()) {
         pCompound.put("ShoulderEntityRight", this.getShoulderEntityRight());
      }

   }

   /**
    * Returns whether this Entity is invulnerable to the given DamageSource.
    */
   public boolean isInvulnerableTo(DamageSource pDamageSource) {
      if (super.isInvulnerableTo(pDamageSource)) {
         return true;
      } else if (pDamageSource == DamageSource.DROWN) {
         return !this.level.getGameRules().getBoolean(GameRules.RULE_DROWNING_DAMAGE);
      } else if (pDamageSource == DamageSource.FALL) {
         return !this.level.getGameRules().getBoolean(GameRules.RULE_FALL_DAMAGE);
      } else if (pDamageSource.isFire()) {
         return !this.level.getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE);
      } else {
         return false;
      }
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (!net.minecraftforge.common.ForgeHooks.onPlayerAttack(this, pSource, pAmount)) return false;
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (this.abilities.invulnerable && !pSource.isBypassInvul()) {
         return false;
      } else {
         this.noActionTime = 0;
         if (this.isDeadOrDying()) {
            return false;
         } else {
            this.removeEntitiesOnShoulder();
            if (pSource.scalesWithDifficulty()) {
               if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                  pAmount = 0.0F;
               }

               if (this.level.getDifficulty() == Difficulty.EASY) {
                  pAmount = Math.min(pAmount / 2.0F + 1.0F, pAmount);
               }

               if (this.level.getDifficulty() == Difficulty.HARD) {
                  pAmount = pAmount * 3.0F / 2.0F;
               }
            }

            return pAmount == 0.0F ? false : super.hurt(pSource, pAmount);
         }
      }
   }

   protected void blockUsingShield(LivingEntity pAttacker) {
      super.blockUsingShield(pAttacker);
      if (pAttacker.getMainHandItem().canDisableShield(this.useItem, this, pAttacker)) {
         this.disableShield(true);
      }

   }

   public boolean canHarmPlayer(PlayerEntity pOther) {
      Team team = this.getTeam();
      Team team1 = pOther.getTeam();
      if (team == null) {
         return true;
      } else {
         return !team.isAlliedTo(team1) ? true : team.isAllowFriendlyFire();
      }
   }

   protected void hurtArmor(DamageSource pDamageSource, float pDamageAmount) {
      this.inventory.hurtArmor(pDamageSource, pDamageAmount);
   }

   protected void hurtCurrentlyUsedShield(float pDamageAmount) {
      if (this.useItem.isShield(this)) {
         if (!this.level.isClientSide) {
            this.awardStat(Stats.ITEM_USED.get(this.useItem.getItem()));
         }

         if (pDamageAmount >= 3.0F) {
            int i = 1 + MathHelper.floor(pDamageAmount);
            Hand hand = this.getUsedItemHand();
            this.useItem.hurtAndBreak(i, this, (p_213833_1_) -> {
               p_213833_1_.broadcastBreakEvent(hand);
               net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(this, this.useItem, hand);
            });
            if (this.useItem.isEmpty()) {
               if (hand == Hand.MAIN_HAND) {
                  this.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
               } else {
                  this.setItemSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
               }

               this.useItem = ItemStack.EMPTY;
               this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
            }
         }

      }
   }

   /**
    * Deals damage to the entity. This will take the armor of the entity into consideration before damaging the health
    * bar.
    */
   protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
      if (!this.isInvulnerableTo(pDamageSource)) {
         pDamageAmount = net.minecraftforge.common.ForgeHooks.onLivingHurt(this, pDamageSource, pDamageAmount);
         if (pDamageAmount <= 0) return;
         pDamageAmount = this.getDamageAfterArmorAbsorb(pDamageSource, pDamageAmount);
         pDamageAmount = this.getDamageAfterMagicAbsorb(pDamageSource, pDamageAmount);
         float f2 = Math.max(pDamageAmount - this.getAbsorptionAmount(), 0.0F);
         this.setAbsorptionAmount(this.getAbsorptionAmount() - (pDamageAmount - f2));
         f2 = net.minecraftforge.common.ForgeHooks.onLivingDamage(this, pDamageSource, f2);
         float f = pDamageAmount - f2;
         if (f > 0.0F && f < 3.4028235E37F) {
            this.awardStat(Stats.DAMAGE_ABSORBED, Math.round(f * 10.0F));
         }

         if (f2 != 0.0F) {
            this.causeFoodExhaustion(pDamageSource.getFoodExhaustion());
            float f1 = this.getHealth();
            this.getCombatTracker().recordDamage(pDamageSource, f1, f2);
            this.setHealth(f1 - f2); // Forge: moved to fix MC-121048
            if (f2 < 3.4028235E37F) {
               this.awardStat(Stats.DAMAGE_TAKEN, Math.round(f2 * 10.0F));
            }

         }
      }
   }

   protected boolean onSoulSpeedBlock() {
      return !this.abilities.flying && super.onSoulSpeedBlock();
   }

   public void openTextEdit(SignTileEntity pSignTile) {
   }

   public void openMinecartCommandBlock(CommandBlockLogic pCommandBlock) {
   }

   public void openCommandBlock(CommandBlockTileEntity pCommandBlock) {
   }

   public void openStructureBlock(StructureBlockTileEntity pStructure) {
   }

   public void openJigsawBlock(JigsawTileEntity pJigsawBlockEntity) {
   }

   public void openHorseInventory(AbstractHorseEntity pHorse, IInventory pInventory) {
   }

   public OptionalInt openMenu(@Nullable INamedContainerProvider pMenu) {
      return OptionalInt.empty();
   }

   public void sendMerchantOffers(int pContainerId, MerchantOffers pOffers, int pLevel, int pXp, boolean pShowProgress, boolean pCanRestock) {
   }

   public void openItemGui(ItemStack pStack, Hand pHand) {
   }

   public ActionResultType interactOn(Entity pEntityToInteractOn, Hand pHand) {
      if (this.isSpectator()) {
         if (pEntityToInteractOn instanceof INamedContainerProvider) {
            this.openMenu((INamedContainerProvider)pEntityToInteractOn);
         }

         return ActionResultType.PASS;
      } else {
         ActionResultType cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntity(this, pEntityToInteractOn, pHand);
         if (cancelResult != null) return cancelResult;
         ItemStack itemstack = this.getItemInHand(pHand);
         ItemStack itemstack1 = itemstack.copy();
         ActionResultType actionresulttype = pEntityToInteractOn.interact(this, pHand);
         if (actionresulttype.consumesAction()) {
            if (this.abilities.instabuild && itemstack == this.getItemInHand(pHand) && itemstack.getCount() < itemstack1.getCount()) {
               itemstack.setCount(itemstack1.getCount());
            }

            if (!this.abilities.instabuild && itemstack.isEmpty()) {
               net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(this, itemstack1, pHand);
            }
            return actionresulttype;
         } else {
            if (!itemstack.isEmpty() && pEntityToInteractOn instanceof LivingEntity) {
               if (this.abilities.instabuild) {
                  itemstack = itemstack1;
               }

               ActionResultType actionresulttype1 = itemstack.interactLivingEntity(this, (LivingEntity)pEntityToInteractOn, pHand);
               if (actionresulttype1.consumesAction()) {
                  if (itemstack.isEmpty() && !this.abilities.instabuild) {
                     net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(this, itemstack1, pHand);
                     this.setItemInHand(pHand, ItemStack.EMPTY);
                  }

                  return actionresulttype1;
               }
            }

            return ActionResultType.PASS;
         }
      }
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return -0.35D;
   }

   public void removeVehicle() {
      super.removeVehicle();
      this.boardingCooldown = 0;
   }

   /**
    * Dead and sleeping entities cannot move
    */
   protected boolean isImmobile() {
      return super.isImmobile() || this.isSleeping();
   }

   public boolean isAffectedByFluids() {
      return !this.abilities.flying;
   }

   protected Vector3d maybeBackOffFromEdge(Vector3d pVec, MoverType pMover) {
      if (!this.abilities.flying && (pMover == MoverType.SELF || pMover == MoverType.PLAYER) && this.isStayingOnGroundSurface() && this.isAboveGround()) {
         double d0 = pVec.x;
         double d1 = pVec.z;
         double d2 = 0.05D;

         while(d0 != 0.0D && this.level.noCollision(this, this.getBoundingBox().move(d0, (double)(-this.maxUpStep), 0.0D))) {
            if (d0 < 0.05D && d0 >= -0.05D) {
               d0 = 0.0D;
            } else if (d0 > 0.0D) {
               d0 -= 0.05D;
            } else {
               d0 += 0.05D;
            }
         }

         while(d1 != 0.0D && this.level.noCollision(this, this.getBoundingBox().move(0.0D, (double)(-this.maxUpStep), d1))) {
            if (d1 < 0.05D && d1 >= -0.05D) {
               d1 = 0.0D;
            } else if (d1 > 0.0D) {
               d1 -= 0.05D;
            } else {
               d1 += 0.05D;
            }
         }

         while(d0 != 0.0D && d1 != 0.0D && this.level.noCollision(this, this.getBoundingBox().move(d0, (double)(-this.maxUpStep), d1))) {
            if (d0 < 0.05D && d0 >= -0.05D) {
               d0 = 0.0D;
            } else if (d0 > 0.0D) {
               d0 -= 0.05D;
            } else {
               d0 += 0.05D;
            }

            if (d1 < 0.05D && d1 >= -0.05D) {
               d1 = 0.0D;
            } else if (d1 > 0.0D) {
               d1 -= 0.05D;
            } else {
               d1 += 0.05D;
            }
         }

         pVec = new Vector3d(d0, pVec.y, d1);
      }

      return pVec;
   }

   private boolean isAboveGround() {
      return this.onGround || this.fallDistance < this.maxUpStep && !this.level.noCollision(this, this.getBoundingBox().move(0.0D, (double)(this.fallDistance - this.maxUpStep), 0.0D));
   }

   /**
    * Attacks for the player the targeted entity with the currently equipped item.  The equipped item has hitEntity
    * called on it. Args: targetEntity
    */
   public void attack(Entity pTargetEntity) {
      if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(this, pTargetEntity)) return;
      if (pTargetEntity.isAttackable()) {
         if (!pTargetEntity.skipAttackInteraction(this)) {
            float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float f1;
            if (pTargetEntity instanceof LivingEntity) {
               f1 = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)pTargetEntity).getMobType());
            } else {
               f1 = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), CreatureAttribute.UNDEFINED);
            }

            float f2 = this.getAttackStrengthScale(0.5F);
            f = f * (0.2F + f2 * f2 * 0.8F);
            f1 = f1 * f2;
            this.resetAttackStrengthTicker();
            if (f > 0.0F || f1 > 0.0F) {
               boolean flag = f2 > 0.9F;
               boolean flag1 = false;
               float i = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK); // Forge: Initialize this value to the attack knockback attribute of the player, which is by default 0
               i = i + EnchantmentHelper.getKnockbackBonus(this);
               if (this.isSprinting() && flag) {
                  this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, this.getSoundSource(), 1.0F, 1.0F);
                  ++i;
                  flag1 = true;
               }

               boolean flag2 = flag && this.fallDistance > 0.0F && !this.onGround && !this.onClimbable() && !this.isInWater() && !this.hasEffect(Effects.BLINDNESS) && !this.isPassenger() && pTargetEntity instanceof LivingEntity;
               flag2 = flag2 && !this.isSprinting();
               net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit(this, pTargetEntity, flag2, flag2 ? 1.5F : 1.0F);
               flag2 = hitResult != null;
               if (flag2) {
                  f *= hitResult.getDamageModifier();
               }

               f = f + f1;
               boolean flag3 = false;
               double d0 = (double)(this.walkDist - this.walkDistO);
               if (flag && !flag2 && !flag1 && this.onGround && d0 < (double)this.getSpeed()) {
                  ItemStack itemstack = this.getItemInHand(Hand.MAIN_HAND);
                  if (itemstack.getItem() instanceof SwordItem) {
                     flag3 = true;
                  }
               }

               float f4 = 0.0F;
               boolean flag4 = false;
               int j = EnchantmentHelper.getFireAspect(this);
               if (pTargetEntity instanceof LivingEntity) {
                  f4 = ((LivingEntity)pTargetEntity).getHealth();
                  if (j > 0 && !pTargetEntity.isOnFire()) {
                     flag4 = true;
                     pTargetEntity.setSecondsOnFire(1);
                  }
               }

               Vector3d vector3d = pTargetEntity.getDeltaMovement();
               boolean flag5 = pTargetEntity.hurt(DamageSource.playerAttack(this), f);
               if (flag5) {
                  if (i > 0) {
                     if (pTargetEntity instanceof LivingEntity) {
                        ((LivingEntity)pTargetEntity).knockback((float)i * 0.5F, (double)MathHelper.sin(this.yRot * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(this.yRot * ((float)Math.PI / 180F))));
                     } else {
                        pTargetEntity.push((double)(-MathHelper.sin(this.yRot * ((float)Math.PI / 180F)) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.yRot * ((float)Math.PI / 180F)) * (float)i * 0.5F));
                     }

                     this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                     this.setSprinting(false);
                  }

                  if (flag3) {
                     float f3 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(this) * f;

                     for(LivingEntity livingentity : this.level.getEntitiesOfClass(LivingEntity.class, pTargetEntity.getBoundingBox().inflate(1.0D, 0.25D, 1.0D))) {
                        if (livingentity != this && livingentity != pTargetEntity && !this.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity)livingentity).isMarker()) && this.distanceToSqr(livingentity) < 9.0D) {
                           livingentity.knockback(0.4F, (double)MathHelper.sin(this.yRot * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(this.yRot * ((float)Math.PI / 180F))));
                           livingentity.hurt(DamageSource.playerAttack(this), f3);
                        }
                     }

                     this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0F, 1.0F);
                     this.sweepAttack();
                  }

                  if (pTargetEntity instanceof ServerPlayerEntity && pTargetEntity.hurtMarked) {
                     ((ServerPlayerEntity)pTargetEntity).connection.send(new SEntityVelocityPacket(pTargetEntity));
                     pTargetEntity.hurtMarked = false;
                     pTargetEntity.setDeltaMovement(vector3d);
                  }

                  if (flag2) {
                     this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
                     this.crit(pTargetEntity);
                  }

                  if (!flag2 && !flag3) {
                     if (flag) {
                        this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
                     } else {
                        this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
                     }
                  }

                  if (f1 > 0.0F) {
                     this.magicCrit(pTargetEntity);
                  }

                  this.setLastHurtMob(pTargetEntity);
                  if (pTargetEntity instanceof LivingEntity) {
                     EnchantmentHelper.doPostHurtEffects((LivingEntity)pTargetEntity, this);
                  }

                  EnchantmentHelper.doPostDamageEffects(this, pTargetEntity);
                  ItemStack itemstack1 = this.getMainHandItem();
                  Entity entity = pTargetEntity;
                  if (pTargetEntity instanceof net.minecraftforge.entity.PartEntity) {
                     entity = ((net.minecraftforge.entity.PartEntity<?>) pTargetEntity).getParent();
                  }

                  if (!this.level.isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                     ItemStack copy = itemstack1.copy();
                     itemstack1.hurtEnemy((LivingEntity)entity, this);
                     if (itemstack1.isEmpty()) {
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(this, copy, Hand.MAIN_HAND);
                        this.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                     }
                  }

                  if (pTargetEntity instanceof LivingEntity) {
                     float f5 = f4 - ((LivingEntity)pTargetEntity).getHealth();
                     this.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                     if (j > 0) {
                        pTargetEntity.setSecondsOnFire(j * 4);
                     }

                     if (this.level instanceof ServerWorld && f5 > 2.0F) {
                        int k = (int)((double)f5 * 0.5D);
                        ((ServerWorld)this.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, pTargetEntity.getX(), pTargetEntity.getY(0.5D), pTargetEntity.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                     }
                  }

                  this.causeFoodExhaustion(0.1F);
               } else {
                  this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
                  if (flag4) {
                     pTargetEntity.clearFire();
                  }
               }
            }

         }
      }
   }

   protected void doAutoAttackOnTouch(LivingEntity pTarget) {
      this.attack(pTarget);
   }

   public void disableShield(boolean pBecauseOfAxe) {
      float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
      if (pBecauseOfAxe) {
         f += 0.75F;
      }

      if (this.random.nextFloat() < f) {
         this.getCooldowns().addCooldown(this.getUseItem().getItem(), 100);
         this.stopUsingItem();
         this.level.broadcastEntityEvent(this, (byte)30);
      }

   }

   /**
    * Called when the entity is dealt a critical hit.
    */
   public void crit(Entity pEntityHit) {
   }

   public void magicCrit(Entity pEntityHit) {
   }

   public void sweepAttack() {
      double d0 = (double)(-MathHelper.sin(this.yRot * ((float)Math.PI / 180F)));
      double d1 = (double)MathHelper.cos(this.yRot * ((float)Math.PI / 180F));
      if (this.level instanceof ServerWorld) {
         ((ServerWorld)this.level).sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d0, this.getY(0.5D), this.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public void respawn() {
   }

   @Override
   public void remove(boolean keepData) {
      super.remove(keepData);
      this.inventoryMenu.removed(this);
      if (this.containerMenu != null) {
         this.containerMenu.removed(this);
      }

   }

   /**
    * returns true if this is an EntityPlayerSP, or the logged in player.
    */
   public boolean isLocalPlayer() {
      return false;
   }

   /**
    * Returns the GameProfile for this player
    */
   public GameProfile getGameProfile() {
      return this.gameProfile;
   }

   public Either<PlayerEntity.SleepResult, Unit> startSleepInBed(BlockPos pAt) {
      this.startSleeping(pAt);
      this.sleepCounter = 0;
      return Either.right(Unit.INSTANCE);
   }

   public void stopSleepInBed(boolean pWakeImmediatly, boolean pUpdateLevelForSleepingPlayers) {
      net.minecraftforge.event.ForgeEventFactory.onPlayerWakeup(this, pWakeImmediatly, pUpdateLevelForSleepingPlayers);
      super.stopSleeping();
      if (this.level instanceof ServerWorld && pUpdateLevelForSleepingPlayers) {
         ((ServerWorld)this.level).updateSleepingPlayerList();
      }

      this.sleepCounter = pWakeImmediatly ? 0 : 100;
   }

   public void stopSleeping() {
      this.stopSleepInBed(true, true);
   }

   public static Optional<Vector3d> findRespawnPositionAndUseSpawnBlock(ServerWorld pServerLevel, BlockPos pSpawnBlockPos, float pPlayerOrientation, boolean pIsRespawnForced, boolean pRespawnAfterWinningTheGame) {
      BlockState blockstate = pServerLevel.getBlockState(pSpawnBlockPos);
      Block block = blockstate.getBlock();
      if (block instanceof RespawnAnchorBlock && blockstate.getValue(RespawnAnchorBlock.CHARGE) > 0 && RespawnAnchorBlock.canSetSpawn(pServerLevel)) {
         Optional<Vector3d> optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, pServerLevel, pSpawnBlockPos);
         if (!pRespawnAfterWinningTheGame && optional.isPresent()) {
            pServerLevel.setBlock(pSpawnBlockPos, blockstate.setValue(RespawnAnchorBlock.CHARGE, Integer.valueOf(blockstate.getValue(RespawnAnchorBlock.CHARGE) - 1)), 3);
         }

         return optional;
      } else if (block instanceof BedBlock && BedBlock.canSetSpawn(pServerLevel)) {
         return BedBlock.findStandUpPosition(EntityType.PLAYER, pServerLevel, pSpawnBlockPos, pPlayerOrientation);
      } else if (!pIsRespawnForced) {
         return blockstate.getRespawnPosition(EntityType.PLAYER, pServerLevel, pSpawnBlockPos, pPlayerOrientation, null);
      } else {
         boolean flag = block.isPossibleToRespawnInThis();
         boolean flag1 = pServerLevel.getBlockState(pSpawnBlockPos.above()).getBlock().isPossibleToRespawnInThis();
         return flag && flag1 ? Optional.of(new Vector3d((double)pSpawnBlockPos.getX() + 0.5D, (double)pSpawnBlockPos.getY() + 0.1D, (double)pSpawnBlockPos.getZ() + 0.5D)) : Optional.empty();
      }
   }

   /**
    * Returns whether or not the player is asleep and the screen has fully faded.
    */
   public boolean isSleepingLongEnough() {
      return this.isSleeping() && this.sleepCounter >= 100;
   }

   public int getSleepTimer() {
      return this.sleepCounter;
   }

   public void displayClientMessage(ITextComponent pChatComponent, boolean pActionBar) {
   }

   public void awardStat(ResourceLocation pStatKey) {
      this.awardStat(Stats.CUSTOM.get(pStatKey));
   }

   public void awardStat(ResourceLocation pStat, int pIncrement) {
      this.awardStat(Stats.CUSTOM.get(pStat), pIncrement);
   }

   /**
    * Add a stat once
    */
   public void awardStat(Stat<?> pStat) {
      this.awardStat(pStat, 1);
   }

   /**
    * Adds a value to a statistic field.
    */
   public void awardStat(Stat<?> pStat, int pAmount) {
   }

   public void resetStat(Stat<?> pStat) {
   }

   public int awardRecipes(Collection<IRecipe<?>> pRecipes) {
      return 0;
   }

   public void awardRecipesByKey(ResourceLocation[] pRecipesKeys) {
   }

   public int resetRecipes(Collection<IRecipe<?>> pRecipes) {
      return 0;
   }

   /**
    * Causes this entity to do an upwards motion (jumping).
    */
   public void jumpFromGround() {
      super.jumpFromGround();
      this.awardStat(Stats.JUMP);
      if (this.isSprinting()) {
         this.causeFoodExhaustion(0.2F);
      } else {
         this.causeFoodExhaustion(0.05F);
      }

   }

   public void travel(Vector3d pTravelVector) {
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      if (this.isSwimming() && !this.isPassenger()) {
         double d3 = this.getLookAngle().y;
         double d4 = d3 < -0.2D ? 0.085D : 0.06D;
         if (d3 <= 0.0D || this.jumping || !this.level.getBlockState(new BlockPos(this.getX(), this.getY() + 1.0D - 0.1D, this.getZ())).getFluidState().isEmpty()) {
            Vector3d vector3d1 = this.getDeltaMovement();
            this.setDeltaMovement(vector3d1.add(0.0D, (d3 - vector3d1.y) * d4, 0.0D));
         }
      }

      if (this.abilities.flying && !this.isPassenger()) {
         double d5 = this.getDeltaMovement().y;
         float f = this.flyingSpeed;
         this.flyingSpeed = this.abilities.getFlyingSpeed() * (float)(this.isSprinting() ? 2 : 1);
         super.travel(pTravelVector);
         Vector3d vector3d = this.getDeltaMovement();
         this.setDeltaMovement(vector3d.x, d5 * 0.6D, vector3d.z);
         this.flyingSpeed = f;
         this.fallDistance = 0.0F;
         this.setSharedFlag(7, false);
      } else {
         super.travel(pTravelVector);
      }

      this.checkMovementStatistics(this.getX() - d0, this.getY() - d1, this.getZ() - d2);
   }

   public void updateSwimming() {
      if (this.abilities.flying) {
         this.setSwimming(false);
      } else {
         super.updateSwimming();
      }

   }

   protected boolean freeAt(BlockPos pPos) {
      return !this.level.getBlockState(pPos).isSuffocating(this.level, pPos);
   }

   /**
    * the movespeed used for the new AI system
    */
   public float getSpeed() {
      return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
   }

   /**
    * Adds a value to a movement statistic field - like run, walk, swin or climb.
    */
   public void checkMovementStatistics(double pDistanceX, double pDistanceY, double pDistanceZ) {
      if (!this.isPassenger()) {
         if (this.isSwimming()) {
            int i = Math.round(MathHelper.sqrt(pDistanceX * pDistanceX + pDistanceY * pDistanceY + pDistanceZ * pDistanceZ) * 100.0F);
            if (i > 0) {
               this.awardStat(Stats.SWIM_ONE_CM, i);
               this.causeFoodExhaustion(0.01F * (float)i * 0.01F);
            }
         } else if (this.isEyeInFluid(FluidTags.WATER)) {
            int j = Math.round(MathHelper.sqrt(pDistanceX * pDistanceX + pDistanceY * pDistanceY + pDistanceZ * pDistanceZ) * 100.0F);
            if (j > 0) {
               this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, j);
               this.causeFoodExhaustion(0.01F * (float)j * 0.01F);
            }
         } else if (this.isInWater()) {
            int k = Math.round(MathHelper.sqrt(pDistanceX * pDistanceX + pDistanceZ * pDistanceZ) * 100.0F);
            if (k > 0) {
               this.awardStat(Stats.WALK_ON_WATER_ONE_CM, k);
               this.causeFoodExhaustion(0.01F * (float)k * 0.01F);
            }
         } else if (this.onClimbable()) {
            if (pDistanceY > 0.0D) {
               this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(pDistanceY * 100.0D));
            }
         } else if (this.onGround) {
            int l = Math.round(MathHelper.sqrt(pDistanceX * pDistanceX + pDistanceZ * pDistanceZ) * 100.0F);
            if (l > 0) {
               if (this.isSprinting()) {
                  this.awardStat(Stats.SPRINT_ONE_CM, l);
                  this.causeFoodExhaustion(0.1F * (float)l * 0.01F);
               } else if (this.isCrouching()) {
                  this.awardStat(Stats.CROUCH_ONE_CM, l);
                  this.causeFoodExhaustion(0.0F * (float)l * 0.01F);
               } else {
                  this.awardStat(Stats.WALK_ONE_CM, l);
                  this.causeFoodExhaustion(0.0F * (float)l * 0.01F);
               }
            }
         } else if (this.isFallFlying()) {
            int i1 = Math.round(MathHelper.sqrt(pDistanceX * pDistanceX + pDistanceY * pDistanceY + pDistanceZ * pDistanceZ) * 100.0F);
            this.awardStat(Stats.AVIATE_ONE_CM, i1);
         } else {
            int j1 = Math.round(MathHelper.sqrt(pDistanceX * pDistanceX + pDistanceZ * pDistanceZ) * 100.0F);
            if (j1 > 25) {
               this.awardStat(Stats.FLY_ONE_CM, j1);
            }
         }

      }
   }

   /**
    * Adds a value to a mounted movement statistic field - by minecart, boat, or pig.
    */
   private void checkRidingStatistics(double pDistanceX, double pDistanceY, double pDistanceZ) {
      if (this.isPassenger()) {
         int i = Math.round(MathHelper.sqrt(pDistanceX * pDistanceX + pDistanceY * pDistanceY + pDistanceZ * pDistanceZ) * 100.0F);
         if (i > 0) {
            Entity entity = this.getVehicle();
            if (entity instanceof AbstractMinecartEntity) {
               this.awardStat(Stats.MINECART_ONE_CM, i);
            } else if (entity instanceof BoatEntity) {
               this.awardStat(Stats.BOAT_ONE_CM, i);
            } else if (entity instanceof PigEntity) {
               this.awardStat(Stats.PIG_ONE_CM, i);
            } else if (entity instanceof AbstractHorseEntity) {
               this.awardStat(Stats.HORSE_ONE_CM, i);
            } else if (entity instanceof StriderEntity) {
               this.awardStat(Stats.STRIDER_ONE_CM, i);
            }
         }
      }

   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      if (this.abilities.mayfly) {
         net.minecraftforge.event.ForgeEventFactory.onPlayerFall(this, pFallDistance, pDamageMultiplier);
         return false;
      } else {
         if (pFallDistance >= 2.0F) {
            this.awardStat(Stats.FALL_ONE_CM, (int)Math.round((double)pFallDistance * 100.0D));
         }

         return super.causeFallDamage(pFallDistance, pDamageMultiplier);
      }
   }

   public boolean tryToStartFallFlying() {
      if (!this.onGround && !this.isFallFlying() && !this.isInWater() && !this.hasEffect(Effects.LEVITATION)) {
         ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.CHEST);
         if (itemstack.canElytraFly(this)) {
            this.startFallFlying();
            return true;
         }
      }

      return false;
   }

   public void startFallFlying() {
      this.setSharedFlag(7, true);
   }

   public void stopFallFlying() {
      this.setSharedFlag(7, true);
      this.setSharedFlag(7, false);
   }

   /**
    * Plays the {@link #getSplashSound() splash sound}, and the {@link ParticleType#WATER_BUBBLE} and {@link
    * ParticleType#WATER_SPLASH} particles.
    */
   protected void doWaterSplashEffect() {
      if (!this.isSpectator()) {
         super.doWaterSplashEffect();
      }

   }

   protected SoundEvent getFallDamageSound(int pHeight) {
      return pHeight > 4 ? SoundEvents.PLAYER_BIG_FALL : SoundEvents.PLAYER_SMALL_FALL;
   }

   public void killed(ServerWorld pLevel, LivingEntity pKilledEntity) {
      this.awardStat(Stats.ENTITY_KILLED.get(pKilledEntity.getType()));
   }

   public void makeStuckInBlock(BlockState pState, Vector3d pMotionMultiplier) {
      if (!this.abilities.flying) {
         super.makeStuckInBlock(pState, pMotionMultiplier);
      }

   }

   public void giveExperiencePoints(int pXpPoints) {
      net.minecraftforge.event.entity.player.PlayerXpEvent.XpChange event = new net.minecraftforge.event.entity.player.PlayerXpEvent.XpChange(this, pXpPoints);
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return;
      pXpPoints = event.getAmount();

      this.increaseScore(pXpPoints);
      this.experienceProgress += (float)pXpPoints / (float)this.getXpNeededForNextLevel();
      this.totalExperience = MathHelper.clamp(this.totalExperience + pXpPoints, 0, Integer.MAX_VALUE);

      while(this.experienceProgress < 0.0F) {
         float f = this.experienceProgress * (float)this.getXpNeededForNextLevel();
         if (this.experienceLevel > 0) {
            this.giveExperienceLevels(-1);
            this.experienceProgress = 1.0F + f / (float)this.getXpNeededForNextLevel();
         } else {
            this.giveExperienceLevels(-1);
            this.experienceProgress = 0.0F;
         }
      }

      while(this.experienceProgress >= 1.0F) {
         this.experienceProgress = (this.experienceProgress - 1.0F) * (float)this.getXpNeededForNextLevel();
         this.giveExperienceLevels(1);
         this.experienceProgress /= (float)this.getXpNeededForNextLevel();
      }

   }

   public int getEnchantmentSeed() {
      return this.enchantmentSeed;
   }

   public void onEnchantmentPerformed(ItemStack pEnchantedItem, int pCost) {
      giveExperienceLevels(-pCost);
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experienceProgress = 0.0F;
         this.totalExperience = 0;
      }

      this.enchantmentSeed = this.random.nextInt();
   }

   /**
    * Add experience levels to this player.
    */
   public void giveExperienceLevels(int pLevels) {
      net.minecraftforge.event.entity.player.PlayerXpEvent.LevelChange event = new net.minecraftforge.event.entity.player.PlayerXpEvent.LevelChange(this, pLevels);
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return;
      pLevels = event.getLevels();

      this.experienceLevel += pLevels;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experienceProgress = 0.0F;
         this.totalExperience = 0;
      }

      if (pLevels > 0 && this.experienceLevel % 5 == 0 && (float)this.lastLevelUpTime < (float)this.tickCount - 100.0F) {
         float f = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
         this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), f * 0.75F, 1.0F);
         this.lastLevelUpTime = this.tickCount;
      }

   }

   /**
    * This method returns the cap amount of experience that the experience bar can hold. With each level, the experience
    * cap on the player's experience bar is raised by 10.
    */
   public int getXpNeededForNextLevel() {
      if (this.experienceLevel >= 30) {
         return 112 + (this.experienceLevel - 30) * 9;
      } else {
         return this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2;
      }
   }

   /**
    * increases exhaustion level by supplied amount
    */
   public void causeFoodExhaustion(float pExhaustion) {
      if (!this.abilities.invulnerable) {
         if (!this.level.isClientSide) {
            this.foodData.addExhaustion(pExhaustion);
         }

      }
   }

   /**
    * Returns the player's FoodStats object.
    */
   public FoodStats getFoodData() {
      return this.foodData;
   }

   public boolean canEat(boolean pCanAlwaysEat) {
      return this.abilities.invulnerable || pCanAlwaysEat || this.foodData.needsFood();
   }

   /**
    * Checks if the player's health is not full and not zero.
    */
   public boolean isHurt() {
      return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
   }

   public boolean mayBuild() {
      return this.abilities.mayBuild;
   }

   /**
    * Returns whether this player can modify the block at a certain location with the given stack.
    * <p>
    * The position being queried is {@code pos.offset(facing.getOpposite()))}.
    * 
    * @return Whether this player may modify the queried location in the current world
    * @see ItemStack#canPlaceOn(Block)
    * @see ItemStack#canEditBlocks()
    * @see PlayerCapabilities#allowEdit
    */
   public boolean mayUseItemAt(BlockPos pPos, Direction pFacing, ItemStack pStack) {
      if (this.abilities.mayBuild) {
         return true;
      } else {
         BlockPos blockpos = pPos.relative(pFacing.getOpposite());
         CachedBlockInfo cachedblockinfo = new CachedBlockInfo(this.level, blockpos, false);
         return pStack.hasAdventureModePlaceTagForBlock(this.level.getTagManager(), cachedblockinfo);
      }
   }

   /**
    * Get the experience points the entity currently has.
    */
   protected int getExperienceReward(PlayerEntity pPlayer) {
      if (!this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !this.isSpectator()) {
         int i = this.experienceLevel * 7;
         return i > 100 ? 100 : i;
      } else {
         return 0;
      }
   }

   /**
    * Only use is to identify if class is an instance of player for experience dropping
    */
   protected boolean isAlwaysExperienceDropper() {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldShowName() {
      return true;
   }

   protected boolean isMovementNoisy() {
      return !this.abilities.flying && (!this.onGround || !this.isDiscrete());
   }

   /**
    * Sends the player's abilities to the server (if there is one).
    */
   public void onUpdateAbilities() {
   }

   public void setGameMode(GameType pGameType) {
   }

   public ITextComponent getName() {
      return new StringTextComponent(this.gameProfile.getName());
   }

   /**
    * Returns the InventoryEnderChest of this player.
    */
   public EnderChestInventory getEnderChestInventory() {
      return this.enderChestInventory;
   }

   public ItemStack getItemBySlot(EquipmentSlotType pSlot) {
      if (pSlot == EquipmentSlotType.MAINHAND) {
         return this.inventory.getSelected();
      } else if (pSlot == EquipmentSlotType.OFFHAND) {
         return this.inventory.offhand.get(0);
      } else {
         return pSlot.getType() == EquipmentSlotType.Group.ARMOR ? this.inventory.armor.get(pSlot.getIndex()) : ItemStack.EMPTY;
      }
   }

   public void setItemSlot(EquipmentSlotType pSlot, ItemStack pStack) {
      if (pSlot == EquipmentSlotType.MAINHAND) {
         this.playEquipSound(pStack);
         this.inventory.items.set(this.inventory.selected, pStack);
      } else if (pSlot == EquipmentSlotType.OFFHAND) {
         this.playEquipSound(pStack);
         this.inventory.offhand.set(0, pStack);
      } else if (pSlot.getType() == EquipmentSlotType.Group.ARMOR) {
         this.playEquipSound(pStack);
         this.inventory.armor.set(pSlot.getIndex(), pStack);
      }

   }

   public boolean addItem(ItemStack pStack) {
      this.playEquipSound(pStack);
      return this.inventory.add(pStack);
   }

   public Iterable<ItemStack> getHandSlots() {
      return Lists.newArrayList(this.getMainHandItem(), this.getOffhandItem());
   }

   public Iterable<ItemStack> getArmorSlots() {
      return this.inventory.armor;
   }

   public boolean setEntityOnShoulder(CompoundNBT pEntityCompound) {
      if (!this.isPassenger() && this.onGround && !this.isInWater()) {
         if (this.getShoulderEntityLeft().isEmpty()) {
            this.setShoulderEntityLeft(pEntityCompound);
            this.timeEntitySatOnShoulder = this.level.getGameTime();
            return true;
         } else if (this.getShoulderEntityRight().isEmpty()) {
            this.setShoulderEntityRight(pEntityCompound);
            this.timeEntitySatOnShoulder = this.level.getGameTime();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected void removeEntitiesOnShoulder() {
      if (this.timeEntitySatOnShoulder + 20L < this.level.getGameTime()) {
         this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
         this.setShoulderEntityLeft(new CompoundNBT());
         this.respawnEntityOnShoulder(this.getShoulderEntityRight());
         this.setShoulderEntityRight(new CompoundNBT());
      }

   }

   private void respawnEntityOnShoulder(CompoundNBT pEntityCompound) {
      if (!this.level.isClientSide && !pEntityCompound.isEmpty()) {
         EntityType.create(pEntityCompound, this.level).ifPresent((p_226562_1_) -> {
            if (p_226562_1_ instanceof TameableEntity) {
               ((TameableEntity)p_226562_1_).setOwnerUUID(this.uuid);
            }

            p_226562_1_.setPos(this.getX(), this.getY() + (double)0.7F, this.getZ());
            ((ServerWorld)this.level).addWithUUID(p_226562_1_);
         });
      }

   }

   /**
    * Returns true if the player is in spectator mode.
    */
   public abstract boolean isSpectator();

   public boolean isSwimming() {
      return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
   }

   public abstract boolean isCreative();

   public boolean isPushedByFluid() {
      return !this.abilities.flying;
   }

   public Scoreboard getScoreboard() {
      return this.level.getScoreboard();
   }

   public ITextComponent getDisplayName() {
      if (this.displayname == null) this.displayname = net.minecraftforge.event.ForgeEventFactory.getPlayerDisplayName(this, this.getName());
      IFormattableTextComponent iformattabletextcomponent = new StringTextComponent("");
      iformattabletextcomponent = prefixes.stream().reduce(iformattabletextcomponent, IFormattableTextComponent::append);
      iformattabletextcomponent = iformattabletextcomponent.append(ScorePlayerTeam.formatNameForTeam(this.getTeam(), this.displayname));
      iformattabletextcomponent = suffixes.stream().reduce(iformattabletextcomponent, IFormattableTextComponent::append);
      return this.decorateDisplayNameComponent(iformattabletextcomponent);
   }

   private IFormattableTextComponent decorateDisplayNameComponent(IFormattableTextComponent pDisplayName) {
      String s = this.getGameProfile().getName();
      return pDisplayName.withStyle((p_234565_2_) -> {
         return p_234565_2_.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + s + " ")).withHoverEvent(this.createHoverEvent()).withInsertion(s);
      });
   }

   /**
    * Returns a String to use as this entity's name in the scoreboard/entity selector systems
    */
   public String getScoreboardName() {
      return this.getGameProfile().getName();
   }

   public float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      switch(pPose) {
      case SWIMMING:
      case FALL_FLYING:
      case SPIN_ATTACK:
         return 0.4F;
      case CROUCHING:
         return 1.27F;
      default:
         return 1.62F;
      }
   }

   public void setAbsorptionAmount(float pAbsorptionAmount) {
      if (pAbsorptionAmount < 0.0F) {
         pAbsorptionAmount = 0.0F;
      }

      this.getEntityData().set(DATA_PLAYER_ABSORPTION_ID, pAbsorptionAmount);
   }

   /**
    * Returns the amount of health added by the Absorption effect.
    */
   public float getAbsorptionAmount() {
      return this.getEntityData().get(DATA_PLAYER_ABSORPTION_ID);
   }

   /**
    * Gets a players UUID given their GameProfie
    */
   public static UUID createPlayerUUID(GameProfile pProfile) {
      UUID uuid = pProfile.getId();
      if (uuid == null) {
         uuid = createPlayerUUID(pProfile.getName());
      }

      return uuid;
   }

   public static UUID createPlayerUUID(String pUsername) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + pUsername).getBytes(StandardCharsets.UTF_8));
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isModelPartShown(PlayerModelPart pPart) {
      return (this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & pPart.getMask()) == pPart.getMask();
   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      if (pSlotIndex >= 0 && pSlotIndex < this.inventory.items.size()) {
         this.inventory.setItem(pSlotIndex, pStack);
         return true;
      } else {
         EquipmentSlotType equipmentslottype;
         if (pSlotIndex == 100 + EquipmentSlotType.HEAD.getIndex()) {
            equipmentslottype = EquipmentSlotType.HEAD;
         } else if (pSlotIndex == 100 + EquipmentSlotType.CHEST.getIndex()) {
            equipmentslottype = EquipmentSlotType.CHEST;
         } else if (pSlotIndex == 100 + EquipmentSlotType.LEGS.getIndex()) {
            equipmentslottype = EquipmentSlotType.LEGS;
         } else if (pSlotIndex == 100 + EquipmentSlotType.FEET.getIndex()) {
            equipmentslottype = EquipmentSlotType.FEET;
         } else {
            equipmentslottype = null;
         }

         if (pSlotIndex == 98) {
            this.setItemSlot(EquipmentSlotType.MAINHAND, pStack);
            return true;
         } else if (pSlotIndex == 99) {
            this.setItemSlot(EquipmentSlotType.OFFHAND, pStack);
            return true;
         } else if (equipmentslottype == null) {
            int i = pSlotIndex - 200;
            if (i >= 0 && i < this.enderChestInventory.getContainerSize()) {
               this.enderChestInventory.setItem(i, pStack);
               return true;
            } else {
               return false;
            }
         } else {
            if (!pStack.isEmpty()) {
               if (!(pStack.getItem() instanceof ArmorItem) && !(pStack.getItem() instanceof ElytraItem)) {
                  if (equipmentslottype != EquipmentSlotType.HEAD) {
                     return false;
                  }
               } else if (MobEntity.getEquipmentSlotForItem(pStack) != equipmentslottype) {
                  return false;
               }
            }

            this.inventory.setItem(equipmentslottype.getIndex() + this.inventory.items.size(), pStack);
            return true;
         }
      }
   }

   /**
    * Whether the "reducedDebugInfo" option is active for this player.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean isReducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   @OnlyIn(Dist.CLIENT)
   public void setReducedDebugInfo(boolean pReducedDebugInfo) {
      this.reducedDebugInfo = pReducedDebugInfo;
   }

   public void setRemainingFireTicks(int pRemainingFireTicks) {
      super.setRemainingFireTicks(this.abilities.invulnerable ? Math.min(pRemainingFireTicks, 1) : pRemainingFireTicks);
   }

   public HandSide getMainArm() {
      return this.entityData.get(DATA_PLAYER_MAIN_HAND) == 0 ? HandSide.LEFT : HandSide.RIGHT;
   }

   public void setMainArm(HandSide pHand) {
      this.entityData.set(DATA_PLAYER_MAIN_HAND, (byte)(pHand == HandSide.LEFT ? 0 : 1));
   }

   public CompoundNBT getShoulderEntityLeft() {
      return this.entityData.get(DATA_SHOULDER_LEFT);
   }

   protected void setShoulderEntityLeft(CompoundNBT pEntityCompound) {
      this.entityData.set(DATA_SHOULDER_LEFT, pEntityCompound);
   }

   public CompoundNBT getShoulderEntityRight() {
      return this.entityData.get(DATA_SHOULDER_RIGHT);
   }

   protected void setShoulderEntityRight(CompoundNBT pEntityCompound) {
      this.entityData.set(DATA_SHOULDER_RIGHT, pEntityCompound);
   }

   public float getCurrentItemAttackStrengthDelay() {
      return (float)(1.0D / this.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0D);
   }

   /**
    * Returns the percentage of attack power available based on the cooldown (zero to one).
    */
   public float getAttackStrengthScale(float pAdjustTicks) {
      return MathHelper.clamp(((float)this.attackStrengthTicker + pAdjustTicks) / this.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
   }

   public void resetAttackStrengthTicker() {
      this.attackStrengthTicker = 0;
   }

   public CooldownTracker getCooldowns() {
      return this.cooldowns;
   }

   protected float getBlockSpeedFactor() {
      return !this.abilities.flying && !this.isFallFlying() ? super.getBlockSpeedFactor() : 1.0F;
   }

   public float getLuck() {
      return (float)this.getAttributeValue(Attributes.LUCK);
   }

   public boolean canUseGameMasterBlocks() {
      return this.abilities.instabuild && this.getPermissionLevel() >= 2;
   }

   public boolean canTakeItem(ItemStack pItemstack) {
      EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(pItemstack);
      return this.getItemBySlot(equipmentslottype).isEmpty();
   }

   public EntitySize getDimensions(Pose pPose) {
      return POSES.getOrDefault(pPose, STANDING_DIMENSIONS);
   }

   public ImmutableList<Pose> getDismountPoses() {
      return ImmutableList.of(Pose.STANDING, Pose.CROUCHING, Pose.SWIMMING);
   }

   public ItemStack getProjectile(ItemStack pShootable) {
      if (!(pShootable.getItem() instanceof ShootableItem)) {
         return ItemStack.EMPTY;
      } else {
         Predicate<ItemStack> predicate = ((ShootableItem)pShootable.getItem()).getSupportedHeldProjectiles();
         ItemStack itemstack = ShootableItem.getHeldProjectile(this, predicate);
         if (!itemstack.isEmpty()) {
            return itemstack;
         } else {
            predicate = ((ShootableItem)pShootable.getItem()).getAllSupportedProjectiles();

            for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
               ItemStack itemstack1 = this.inventory.getItem(i);
               if (predicate.test(itemstack1)) {
                  return itemstack1;
               }
            }

            return this.abilities.instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
         }
      }
   }

   public ItemStack eat(World pLevel, ItemStack pFood) {
      this.getFoodData().eat(pFood.getItem(), pFood);
      this.awardStat(Stats.ITEM_USED.get(pFood.getItem()));
      pLevel.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.9F);
      if (this instanceof ServerPlayerEntity) {
         CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity)this, pFood);
      }

      return super.eat(pLevel, pFood);
   }

   protected boolean shouldRemoveSoulSpeed(BlockState pState) {
      return this.abilities.flying || super.shouldRemoveSoulSpeed(pState);
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getRopeHoldPosition(float pPartialTicks) {
      double d0 = 0.22D * (this.getMainArm() == HandSide.RIGHT ? -1.0D : 1.0D);
      float f = MathHelper.lerp(pPartialTicks * 0.5F, this.xRot, this.xRotO) * ((float)Math.PI / 180F);
      float f1 = MathHelper.lerp(pPartialTicks, this.yBodyRotO, this.yBodyRot) * ((float)Math.PI / 180F);
      if (!this.isFallFlying() && !this.isAutoSpinAttack()) {
         if (this.isVisuallySwimming()) {
            return this.getPosition(pPartialTicks).add((new Vector3d(d0, 0.2D, -0.15D)).xRot(-f).yRot(-f1));
         } else {
            double d5 = this.getBoundingBox().getYsize() - 1.0D;
            double d6 = this.isCrouching() ? -0.2D : 0.07D;
            return this.getPosition(pPartialTicks).add((new Vector3d(d0, d5, d6)).yRot(-f1));
         }
      } else {
         Vector3d vector3d = this.getViewVector(pPartialTicks);
         Vector3d vector3d1 = this.getDeltaMovement();
         double d1 = Entity.getHorizontalDistanceSqr(vector3d1);
         double d2 = Entity.getHorizontalDistanceSqr(vector3d);
         float f2;
         if (d1 > 0.0D && d2 > 0.0D) {
            double d3 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d1 * d2);
            double d4 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
            f2 = (float)(Math.signum(d4) * Math.acos(d3));
         } else {
            f2 = 0.0F;
         }

         return this.getPosition(pPartialTicks).add((new Vector3d(d0, -0.11D, 0.85D)).zRot(-f2).xRot(-f).yRot(-f1));
      }
   }

   public static enum SleepResult {
      NOT_POSSIBLE_HERE,
      NOT_POSSIBLE_NOW(new TranslationTextComponent("block.minecraft.bed.no_sleep")),
      TOO_FAR_AWAY(new TranslationTextComponent("block.minecraft.bed.too_far_away")),
      OBSTRUCTED(new TranslationTextComponent("block.minecraft.bed.obstructed")),
      OTHER_PROBLEM,
      NOT_SAFE(new TranslationTextComponent("block.minecraft.bed.not_safe"));

      @Nullable
      private final ITextComponent message;

      private SleepResult() {
         this.message = null;
      }

      private SleepResult(ITextComponent pMessage) {
         this.message = pMessage;
      }

      @Nullable
      public ITextComponent getMessage() {
         return this.message;
      }
   }

   // =========== FORGE START ==============//
   public Collection<IFormattableTextComponent> getPrefixes() {
       return this.prefixes;
   }

   public Collection<IFormattableTextComponent> getSuffixes() {
       return this.suffixes;
   }

   private ITextComponent displayname = null;
   /**
    * Force the displayed name to refresh, by firing {@link net.minecraftforge.event.entity.player.PlayerEvent.NameFormat}, using the real player name as event parameter.
    */
   public void refreshDisplayName() {
      this.displayname = net.minecraftforge.event.ForgeEventFactory.getPlayerDisplayName(this, this.getName());
   }

   private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>
         playerMainHandler = net.minecraftforge.common.util.LazyOptional.of(
               () -> new net.minecraftforge.items.wrapper.PlayerMainInvWrapper(inventory));

   private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>
         playerEquipmentHandler = net.minecraftforge.common.util.LazyOptional.of(
               () -> new net.minecraftforge.items.wrapper.CombinedInvWrapper(
                     new net.minecraftforge.items.wrapper.PlayerArmorInvWrapper(inventory),
                     new net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper(inventory)));

   private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>
         playerJoinedHandler = net.minecraftforge.common.util.LazyOptional.of(
               () -> new net.minecraftforge.items.wrapper.PlayerInvWrapper(inventory));

   @Override
   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable Direction facing) {
      if (this.isAlive() && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (facing == null) return playerJoinedHandler.cast();
         else if (facing.getAxis().isVertical()) return playerMainHandler.cast();
         else if (facing.getAxis().isHorizontal()) return playerEquipmentHandler.cast();
      }
      return super.getCapability(capability, facing);
   }

   /**
    * Force a pose for the player. If set, the vanilla pose determination and clearance check is skipped. Make sure the pose is clear yourself (e.g. in PlayerTick).
    * This has to be set just once, do not set it every tick.
    * Make sure to clear (null) the pose if not required anymore and only use if necessary.
    */
   public void setForcedPose(@Nullable Pose pose) {
      this.forcedPose = pose;
   }

   /**
    * @return The forced pose if set, null otherwise
    */
   @Nullable
   public Pose getForcedPose() {
      return this.forcedPose;
   }
}
