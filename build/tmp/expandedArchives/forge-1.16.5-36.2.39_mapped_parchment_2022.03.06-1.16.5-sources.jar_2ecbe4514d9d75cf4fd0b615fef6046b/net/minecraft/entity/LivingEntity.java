package net.minecraft.entity;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.network.play.server.SCollectItemPacket;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SSpawnMobPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.CombatRules;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class LivingEntity extends Entity {
   private static final UUID SPEED_MODIFIER_SPRINTING_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
   private static final UUID SPEED_MODIFIER_SOUL_SPEED_UUID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");
   private static final UUID SLOW_FALLING_ID = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA");
   private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(SPEED_MODIFIER_SPRINTING_UUID, "Sprinting speed boost", (double)0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL);
   private static final AttributeModifier SLOW_FALLING = new AttributeModifier(SLOW_FALLING_ID, "Slow falling acceleration reduction", -0.07, AttributeModifier.Operation.ADDITION); // Add -0.07 to 0.08 so we get the vanilla default of 0.01
   protected static final DataParameter<Byte> DATA_LIVING_ENTITY_FLAGS = EntityDataManager.defineId(LivingEntity.class, DataSerializers.BYTE);
   private static final DataParameter<Float> DATA_HEALTH_ID = EntityDataManager.defineId(LivingEntity.class, DataSerializers.FLOAT);
   private static final DataParameter<Integer> DATA_EFFECT_COLOR_ID = EntityDataManager.defineId(LivingEntity.class, DataSerializers.INT);
   private static final DataParameter<Boolean> DATA_EFFECT_AMBIENCE_ID = EntityDataManager.defineId(LivingEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Integer> DATA_ARROW_COUNT_ID = EntityDataManager.defineId(LivingEntity.class, DataSerializers.INT);
   private static final DataParameter<Integer> DATA_STINGER_COUNT_ID = EntityDataManager.defineId(LivingEntity.class, DataSerializers.INT);
   private static final DataParameter<Optional<BlockPos>> SLEEPING_POS_ID = EntityDataManager.defineId(LivingEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
   protected static final EntitySize SLEEPING_DIMENSIONS = EntitySize.fixed(0.2F, 0.2F);
   private final AttributeModifierManager attributes;
   private final CombatTracker combatTracker = new CombatTracker(this);
   private final Map<Effect, EffectInstance> activeEffects = Maps.newHashMap();
   private final NonNullList<ItemStack> lastHandItemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
   private final NonNullList<ItemStack> lastArmorItemStacks = NonNullList.withSize(4, ItemStack.EMPTY);
   public boolean swinging;
   public Hand swingingArm;
   public int swingTime;
   public int removeArrowTime;
   public int removeStingerTime;
   public int hurtTime;
   public int hurtDuration;
   public float hurtDir;
   public int deathTime;
   public float oAttackAnim;
   public float attackAnim;
   protected int attackStrengthTicker;
   public float animationSpeedOld;
   public float animationSpeed;
   public float animationPosition;
   public final int invulnerableDuration = 20;
   public final float timeOffs;
   public final float rotA;
   public float yBodyRot;
   public float yBodyRotO;
   public float yHeadRot;
   public float yHeadRotO;
   public float flyingSpeed = 0.02F;
   @Nullable
   protected PlayerEntity lastHurtByPlayer;
   protected int lastHurtByPlayerTime;
   protected boolean dead;
   protected int noActionTime;
   protected float oRun;
   protected float run;
   protected float animStep;
   protected float animStepO;
   protected float rotOffs;
   protected int deathScore;
   /** Damage taken in the last hit. Mobs are resistant to damage less than this for a short time after taking damage. */
   protected float lastHurt;
   protected boolean jumping;
   public float xxa;
   public float yya;
   public float zza;
   protected int lerpSteps;
   protected double lerpX;
   protected double lerpY;
   protected double lerpZ;
   protected double lerpYRot;
   protected double lerpXRot;
   protected double lyHeadRot;
   protected int lerpHeadSteps;
   private boolean effectsDirty = true;
   @Nullable
   private LivingEntity lastHurtByMob;
   private int lastHurtByMobTimestamp;
   private LivingEntity lastHurtMob;
   /** Holds the value of ticksExisted when setLastAttacker was last called. */
   private int lastHurtMobTimestamp;
   private float speed;
   private int noJumpDelay;
   private float absorptionAmount;
   protected ItemStack useItem = ItemStack.EMPTY;
   protected int useItemRemaining;
   protected int fallFlyTicks;
   private BlockPos lastPos;
   private Optional<BlockPos> lastClimbablePos = Optional.empty();
   private DamageSource lastDamageSource;
   private long lastDamageStamp;
   protected int autoSpinAttackTicks;
   private float swimAmount;
   private float swimAmountO;
   protected Brain<?> brain;

   protected LivingEntity(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
      super(p_i48577_1_, p_i48577_2_);
      this.attributes = new AttributeModifierManager(GlobalEntityTypeAttributes.getSupplier(p_i48577_1_));
      this.setHealth(this.getMaxHealth());
      this.blocksBuilding = true;
      this.rotA = (float)((Math.random() + 1.0D) * (double)0.01F);
      this.reapplyPosition();
      this.timeOffs = (float)Math.random() * 12398.0F;
      this.yRot = (float)(Math.random() * (double)((float)Math.PI * 2F));
      this.yHeadRot = this.yRot;
      this.maxUpStep = 0.6F;
      NBTDynamicOps nbtdynamicops = NBTDynamicOps.INSTANCE;
      this.brain = this.makeBrain(new Dynamic<>(nbtdynamicops, nbtdynamicops.createMap(ImmutableMap.of(nbtdynamicops.createString("memories"), nbtdynamicops.emptyMap()))));
   }

   public Brain<?> getBrain() {
      return this.brain;
   }

   protected Brain.BrainCodec<?> brainProvider() {
      return Brain.provider(ImmutableList.of(), ImmutableList.of());
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      return this.brainProvider().makeBrain(pDynamic);
   }

   /**
    * Called by the /kill command.
    */
   public void kill() {
      this.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
   }

   public boolean canAttackType(EntityType<?> pType) {
      return true;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_LIVING_ENTITY_FLAGS, (byte)0);
      this.entityData.define(DATA_EFFECT_COLOR_ID, 0);
      this.entityData.define(DATA_EFFECT_AMBIENCE_ID, false);
      this.entityData.define(DATA_ARROW_COUNT_ID, 0);
      this.entityData.define(DATA_STINGER_COUNT_ID, 0);
      this.entityData.define(DATA_HEALTH_ID, 1.0F);
      this.entityData.define(SLEEPING_POS_ID, Optional.empty());
   }

   public static AttributeModifierMap.MutableAttribute createLivingAttributes() {
      return AttributeModifierMap.builder().add(Attributes.MAX_HEALTH).add(Attributes.KNOCKBACK_RESISTANCE).add(Attributes.MOVEMENT_SPEED).add(Attributes.ARMOR).add(Attributes.ARMOR_TOUGHNESS).add(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).add(net.minecraftforge.common.ForgeMod.NAMETAG_DISTANCE.get()).add(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
      if (!this.isInWater()) {
         this.updateInWaterStateAndDoWaterCurrentPushing();
      }

      if (!this.level.isClientSide && pOnGround && this.fallDistance > 0.0F) {
         this.removeSoulSpeed();
         this.tryAddSoulSpeed();
      }

      if (!this.level.isClientSide && this.fallDistance > 3.0F && pOnGround) {
         float f = (float)MathHelper.ceil(this.fallDistance - 3.0F);
         if (!pState.isAir(level, pPos)) {
            double d0 = Math.min((double)(0.2F + f / 15.0F), 2.5D);
            int i = (int)(150.0D * d0);
            if (!pState.addLandingEffects((ServerWorld)this.level, pPos, pState, this, i))
            ((ServerWorld)this.level).sendParticles(new BlockParticleData(ParticleTypes.BLOCK, pState).setPos(pPos), this.getX(), this.getY(), this.getZ(), i, 0.0D, 0.0D, 0.0D, (double)0.15F);
         }
      }

      super.checkFallDamage(pY, pOnGround, pState, pPos);
   }

   public boolean canBreatheUnderwater() {
      return this.getMobType() == CreatureAttribute.UNDEAD;
   }

   @OnlyIn(Dist.CLIENT)
   public float getSwimAmount(float pPartialTicks) {
      return MathHelper.lerp(pPartialTicks, this.swimAmountO, this.swimAmount);
   }

   /**
    * Gets called every tick from main Entity class
    */
   public void baseTick() {
      this.oAttackAnim = this.attackAnim;
      if (this.firstTick) {
         this.getSleepingPos().ifPresent(this::setPosToBed);
      }

      if (this.canSpawnSoulSpeedParticle()) {
         this.spawnSoulSpeedParticle();
      }

      super.baseTick();
      this.level.getProfiler().push("livingEntityBaseTick");
      boolean flag = this instanceof PlayerEntity;
      if (this.isAlive()) {
         if (this.isInWall()) {
            this.hurt(DamageSource.IN_WALL, 1.0F);
         } else if (flag && !this.level.getWorldBorder().isWithinBounds(this.getBoundingBox())) {
            double d0 = this.level.getWorldBorder().getDistanceToBorder(this) + this.level.getWorldBorder().getDamageSafeZone();
            if (d0 < 0.0D) {
               double d1 = this.level.getWorldBorder().getDamagePerBlock();
               if (d1 > 0.0D) {
                  this.hurt(DamageSource.IN_WALL, (float)Math.max(1, MathHelper.floor(-d0 * d1)));
               }
            }
         }
      }

      if (this.fireImmune() || this.level.isClientSide) {
         this.clearFire();
      }

      boolean flag1 = flag && ((PlayerEntity)this).abilities.invulnerable;
      if (this.isAlive()) {
         if (this.isEyeInFluid(FluidTags.WATER) && !this.level.getBlockState(new BlockPos(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
            if (!this.canBreatheUnderwater() && !EffectUtils.hasWaterBreathing(this) && !flag1) {
               this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
               if (this.getAirSupply() == -20) {
                  this.setAirSupply(0);
                  Vector3d vector3d = this.getDeltaMovement();

                  for(int i = 0; i < 8; ++i) {
                     double d2 = this.random.nextDouble() - this.random.nextDouble();
                     double d3 = this.random.nextDouble() - this.random.nextDouble();
                     double d4 = this.random.nextDouble() - this.random.nextDouble();
                     this.level.addParticle(ParticleTypes.BUBBLE, this.getX() + d2, this.getY() + d3, this.getZ() + d4, vector3d.x, vector3d.y, vector3d.z);
                  }

                  this.hurt(DamageSource.DROWN, 2.0F);
               }
            }

            if (!this.level.isClientSide && this.isPassenger() && this.getVehicle() != null && !this.getVehicle().canBeRiddenInWater(this)) {
               this.stopRiding();
            }
         } else if (this.getAirSupply() < this.getMaxAirSupply()) {
            this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
         }

         if (!this.level.isClientSide) {
            BlockPos blockpos = this.blockPosition();
            if (!Objects.equal(this.lastPos, blockpos)) {
               this.lastPos = blockpos;
               this.onChangedBlock(blockpos);
            }
         }
      }

      if (this.isAlive() && this.isInWaterRainOrBubble()) {
         this.clearFire();
      }

      if (this.hurtTime > 0) {
         --this.hurtTime;
      }

      if (this.invulnerableTime > 0 && !(this instanceof ServerPlayerEntity)) {
         --this.invulnerableTime;
      }

      if (this.isDeadOrDying()) {
         this.tickDeath();
      }

      if (this.lastHurtByPlayerTime > 0) {
         --this.lastHurtByPlayerTime;
      } else {
         this.lastHurtByPlayer = null;
      }

      if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
         this.lastHurtMob = null;
      }

      if (this.lastHurtByMob != null) {
         if (!this.lastHurtByMob.isAlive()) {
            this.setLastHurtByMob((LivingEntity)null);
         } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
            this.setLastHurtByMob((LivingEntity)null);
         }
      }

      this.tickEffects();
      this.animStepO = this.animStep;
      this.yBodyRotO = this.yBodyRot;
      this.yHeadRotO = this.yHeadRot;
      this.yRotO = this.yRot;
      this.xRotO = this.xRot;
      this.level.getProfiler().pop();
   }

   public boolean canSpawnSoulSpeedParticle() {
      return this.tickCount % 5 == 0 && this.getDeltaMovement().x != 0.0D && this.getDeltaMovement().z != 0.0D && !this.isSpectator() && EnchantmentHelper.hasSoulSpeed(this) && this.onSoulSpeedBlock();
   }

   protected void spawnSoulSpeedParticle() {
      Vector3d vector3d = this.getDeltaMovement();
      this.level.addParticle(ParticleTypes.SOUL, this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth(), this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth(), vector3d.x * -0.2D, 0.1D, vector3d.z * -0.2D);
      float f = this.random.nextFloat() * 0.4F + this.random.nextFloat() > 0.9F ? 0.6F : 0.0F;
      this.playSound(SoundEvents.SOUL_ESCAPE, f, 0.6F + this.random.nextFloat() * 0.4F);
   }

   protected boolean onSoulSpeedBlock() {
      return this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.SOUL_SPEED_BLOCKS);
   }

   protected float getBlockSpeedFactor() {
      return this.onSoulSpeedBlock() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, this) > 0 ? 1.0F : super.getBlockSpeedFactor();
   }

   protected boolean shouldRemoveSoulSpeed(BlockState pState) {
      return !pState.isAir() || this.isFallFlying();
   }

   protected void removeSoulSpeed() {
      ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (modifiableattributeinstance != null) {
         if (modifiableattributeinstance.getModifier(SPEED_MODIFIER_SOUL_SPEED_UUID) != null) {
            modifiableattributeinstance.removeModifier(SPEED_MODIFIER_SOUL_SPEED_UUID);
         }

      }
   }

   protected void tryAddSoulSpeed() {
      if (!this.getBlockStateOn().isAir()) {
         int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, this);
         if (i > 0 && this.onSoulSpeedBlock()) {
            ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (modifiableattributeinstance == null) {
               return;
            }

            modifiableattributeinstance.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_SOUL_SPEED_UUID, "Soul speed boost", (double)(0.03F * (1.0F + (float)i * 0.35F)), AttributeModifier.Operation.ADDITION));
            if (this.getRandom().nextFloat() < 0.04F) {
               ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.FEET);
               itemstack.hurtAndBreak(1, this, (p_233654_0_) -> {
                  p_233654_0_.broadcastBreakEvent(EquipmentSlotType.FEET);
               });
            }
         }
      }

   }

   protected void onChangedBlock(BlockPos pPos) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FROST_WALKER, this);
      if (i > 0) {
         FrostWalkerEnchantment.onEntityMoved(this, this.level, pPos, i);
      }

      if (this.shouldRemoveSoulSpeed(this.getBlockStateOn())) {
         this.removeSoulSpeed();
      }

      this.tryAddSoulSpeed();
   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return false;
   }

   public float getScale() {
      return this.isBaby() ? 0.5F : 1.0F;
   }

   protected boolean isAffectedByFluids() {
      return true;
   }

   public boolean rideableUnderWater() {
      return false;
   }

   /**
    * handles entity death timer, experience orb and particle creation
    */
   protected void tickDeath() {
      ++this.deathTime;
      if (this.deathTime == 20) {
         this.remove(this instanceof net.minecraft.entity.player.ServerPlayerEntity); //Forge keep data until we revive player

         for(int i = 0; i < 20; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(ParticleTypes.POOF, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), d0, d1, d2);
         }
      }

   }

   /**
    * Entity won't drop items or experience points if this returns false
    */
   protected boolean shouldDropExperience() {
      return !this.isBaby();
   }

   /**
    * Entity won't drop items if this returns false
    */
   protected boolean shouldDropLoot() {
      return !this.isBaby();
   }

   /**
    * Decrements the entity's air supply when underwater
    */
   protected int decreaseAirSupply(int pCurrentAir) {
      int i = EnchantmentHelper.getRespiration(this);
      return i > 0 && this.random.nextInt(i + 1) > 0 ? pCurrentAir : pCurrentAir - 1;
   }

   protected int increaseAirSupply(int pCurrentAir) {
      return Math.min(pCurrentAir + 4, this.getMaxAirSupply());
   }

   /**
    * Get the experience points the entity currently has.
    */
   protected int getExperienceReward(PlayerEntity pPlayer) {
      return 0;
   }

   /**
    * Only use is to identify if class is an instance of player for experience dropping
    */
   protected boolean isAlwaysExperienceDropper() {
      return false;
   }

   public Random getRandom() {
      return this.random;
   }

   @Nullable
   public LivingEntity getLastHurtByMob() {
      return this.lastHurtByMob;
   }

   public int getLastHurtByMobTimestamp() {
      return this.lastHurtByMobTimestamp;
   }

   public void setLastHurtByPlayer(@Nullable PlayerEntity pPlayer) {
      this.lastHurtByPlayer = pPlayer;
      this.lastHurtByPlayerTime = this.tickCount;
   }

   /**
    * Hint to AI tasks that we were attacked by the passed EntityLivingBase and should retaliate. Is not guaranteed to
    * change our actual active target (for example if we are currently busy attacking someone else)
    */
   public void setLastHurtByMob(@Nullable LivingEntity pLivingEntity) {
      this.lastHurtByMob = pLivingEntity;
      this.lastHurtByMobTimestamp = this.tickCount;
   }

   @Nullable
   public LivingEntity getLastHurtMob() {
      return this.lastHurtMob;
   }

   public int getLastHurtMobTimestamp() {
      return this.lastHurtMobTimestamp;
   }

   public void setLastHurtMob(Entity pEntity) {
      if (pEntity instanceof LivingEntity) {
         this.lastHurtMob = (LivingEntity)pEntity;
      } else {
         this.lastHurtMob = null;
      }

      this.lastHurtMobTimestamp = this.tickCount;
   }

   public int getNoActionTime() {
      return this.noActionTime;
   }

   public void setNoActionTime(int pIdleTime) {
      this.noActionTime = pIdleTime;
   }

   protected void playEquipSound(ItemStack pStack) {
      if (!pStack.isEmpty()) {
         SoundEvent soundevent = SoundEvents.ARMOR_EQUIP_GENERIC;
         Item item = pStack.getItem();
         if (item instanceof ArmorItem) {
            soundevent = ((ArmorItem)item).getMaterial().getEquipSound();
         } else if (item == Items.ELYTRA) {
            soundevent = SoundEvents.ARMOR_EQUIP_ELYTRA;
         }

         this.playSound(soundevent, 1.0F, 1.0F);
      }
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      pCompound.putFloat("Health", this.getHealth());
      pCompound.putShort("HurtTime", (short)this.hurtTime);
      pCompound.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
      pCompound.putShort("DeathTime", (short)this.deathTime);
      pCompound.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
      pCompound.put("Attributes", this.getAttributes().save());
      if (!this.activeEffects.isEmpty()) {
         ListNBT listnbt = new ListNBT();

         for(EffectInstance effectinstance : this.activeEffects.values()) {
            listnbt.add(effectinstance.save(new CompoundNBT()));
         }

         pCompound.put("ActiveEffects", listnbt);
      }

      pCompound.putBoolean("FallFlying", this.isFallFlying());
      this.getSleepingPos().ifPresent((p_213338_1_) -> {
         pCompound.putInt("SleepingX", p_213338_1_.getX());
         pCompound.putInt("SleepingY", p_213338_1_.getY());
         pCompound.putInt("SleepingZ", p_213338_1_.getZ());
      });
      DataResult<INBT> dataresult = this.brain.serializeStart(NBTDynamicOps.INSTANCE);
      dataresult.resultOrPartial(LOGGER::error).ifPresent((p_233636_1_) -> {
         pCompound.put("Brain", p_233636_1_);
      });
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      this.setAbsorptionAmount(pCompound.getFloat("AbsorptionAmount"));
      if (pCompound.contains("Attributes", 9) && this.level != null && !this.level.isClientSide) {
         this.getAttributes().load(pCompound.getList("Attributes", 10));
      }

      if (pCompound.contains("ActiveEffects", 9)) {
         ListNBT listnbt = pCompound.getList("ActiveEffects", 10);

         for(int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            EffectInstance effectinstance = EffectInstance.load(compoundnbt);
            if (effectinstance != null) {
               this.activeEffects.put(effectinstance.getEffect(), effectinstance);
            }
         }
      }

      if (pCompound.contains("Health", 99)) {
         this.setHealth(pCompound.getFloat("Health"));
      }

      this.hurtTime = pCompound.getShort("HurtTime");
      this.deathTime = pCompound.getShort("DeathTime");
      this.lastHurtByMobTimestamp = pCompound.getInt("HurtByTimestamp");
      if (pCompound.contains("Team", 8)) {
         String s = pCompound.getString("Team");
         ScorePlayerTeam scoreplayerteam = this.level.getScoreboard().getPlayerTeam(s);
         boolean flag = scoreplayerteam != null && this.level.getScoreboard().addPlayerToTeam(this.getStringUUID(), scoreplayerteam);
         if (!flag) {
            LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", (Object)s);
         }
      }

      if (pCompound.getBoolean("FallFlying")) {
         this.setSharedFlag(7, true);
      }

      if (pCompound.contains("SleepingX", 99) && pCompound.contains("SleepingY", 99) && pCompound.contains("SleepingZ", 99)) {
         BlockPos blockpos = new BlockPos(pCompound.getInt("SleepingX"), pCompound.getInt("SleepingY"), pCompound.getInt("SleepingZ"));
         this.setSleepingPos(blockpos);
         this.entityData.set(DATA_POSE, Pose.SLEEPING);
         if (!this.firstTick) {
            this.setPosToBed(blockpos);
         }
      }

      if (pCompound.contains("Brain", 10)) {
         this.brain = this.makeBrain(new Dynamic<>(NBTDynamicOps.INSTANCE, pCompound.get("Brain")));
      }

   }

   protected void tickEffects() {
      Iterator<Effect> iterator = this.activeEffects.keySet().iterator();

      try {
         while(iterator.hasNext()) {
            Effect effect = iterator.next();
            EffectInstance effectinstance = this.activeEffects.get(effect);
            if (!effectinstance.tick(this, () -> {
               this.onEffectUpdated(effectinstance, true);
            })) {
               if (!this.level.isClientSide && !net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.PotionEvent.PotionExpiryEvent(this, effectinstance))) {
                  iterator.remove();
                  this.onEffectRemoved(effectinstance);
               }
            } else if (effectinstance.getDuration() % 600 == 0) {
               this.onEffectUpdated(effectinstance, false);
            }
         }
      } catch (ConcurrentModificationException concurrentmodificationexception) {
      }

      if (this.effectsDirty) {
         if (!this.level.isClientSide) {
            this.updateInvisibilityStatus();
         }

         this.effectsDirty = false;
      }

      int i = this.entityData.get(DATA_EFFECT_COLOR_ID);
      boolean flag1 = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
      if (i > 0) {
         boolean flag;
         if (this.isInvisible()) {
            flag = this.random.nextInt(15) == 0;
         } else {
            flag = this.random.nextBoolean();
         }

         if (flag1) {
            flag &= this.random.nextInt(5) == 0;
         }

         if (flag && i > 0) {
            double d0 = (double)(i >> 16 & 255) / 255.0D;
            double d1 = (double)(i >> 8 & 255) / 255.0D;
            double d2 = (double)(i >> 0 & 255) / 255.0D;
            this.level.addParticle(flag1 ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), d0, d1, d2);
         }
      }

   }

   /**
    * Clears potion metadata values if the entity has no potion effects. Otherwise, updates potion effect color,
    * ambience, and invisibility metadata values
    */
   protected void updateInvisibilityStatus() {
      if (this.activeEffects.isEmpty()) {
         this.removeEffectParticles();
         this.setInvisible(false);
      } else {
         Collection<EffectInstance> collection = this.activeEffects.values();
         net.minecraftforge.event.entity.living.PotionColorCalculationEvent event = new net.minecraftforge.event.entity.living.PotionColorCalculationEvent(this, PotionUtils.getColor(collection), areAllEffectsAmbient(collection), collection);
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
         this.entityData.set(DATA_EFFECT_AMBIENCE_ID, event.areParticlesHidden());
         this.entityData.set(DATA_EFFECT_COLOR_ID, event.getColor());
         this.setInvisible(this.hasEffect(Effects.INVISIBILITY));
      }

   }

   public double getVisibilityPercent(@Nullable Entity pLookingEntity) {
      double d0 = 1.0D;
      if (this.isDiscrete()) {
         d0 *= 0.8D;
      }

      if (this.isInvisible()) {
         float f = this.getArmorCoverPercentage();
         if (f < 0.1F) {
            f = 0.1F;
         }

         d0 *= 0.7D * (double)f;
      }

      if (pLookingEntity != null) {
         ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.HEAD);
         Item item = itemstack.getItem();
         EntityType<?> entitytype = pLookingEntity.getType();
         if (entitytype == EntityType.SKELETON && item == Items.SKELETON_SKULL || entitytype == EntityType.ZOMBIE && item == Items.ZOMBIE_HEAD || entitytype == EntityType.CREEPER && item == Items.CREEPER_HEAD) {
            d0 *= 0.5D;
         }
      }
      d0 = net.minecraftforge.common.ForgeHooks.getEntityVisibilityMultiplier(this, pLookingEntity, d0);
      return d0;
   }

   public boolean canAttack(LivingEntity pTarget) {
      return true;
   }

   public boolean canAttack(LivingEntity pLivingentity, EntityPredicate pCondition) {
      return pCondition.test(this, pLivingentity);
   }

   /**
    * Returns true if all of the potion effects in the specified collection are ambient.
    */
   public static boolean areAllEffectsAmbient(Collection<EffectInstance> pPotionEffects) {
      for(EffectInstance effectinstance : pPotionEffects) {
         if (!effectinstance.isAmbient()) {
            return false;
         }
      }

      return true;
   }

   /**
    * Resets the potion effect color and ambience metadata values
    */
   protected void removeEffectParticles() {
      this.entityData.set(DATA_EFFECT_AMBIENCE_ID, false);
      this.entityData.set(DATA_EFFECT_COLOR_ID, 0);
   }

   public boolean removeAllEffects() {
      if (this.level.isClientSide) {
         return false;
      } else {
         Iterator<EffectInstance> iterator = this.activeEffects.values().iterator();

         boolean flag;
         for(flag = false; iterator.hasNext(); flag = true) {
            EffectInstance effect = iterator.next();
            if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent(this, effect))) continue;
            this.onEffectRemoved(effect);
            iterator.remove();
         }

         return flag;
      }
   }

   public Collection<EffectInstance> getActiveEffects() {
      return this.activeEffects.values();
   }

   public Map<Effect, EffectInstance> getActiveEffectsMap() {
      return this.activeEffects;
   }

   public boolean hasEffect(Effect pEffect) {
      return this.activeEffects.containsKey(pEffect);
   }

   /**
    * returns the PotionEffect for the supplied Potion if it is active, null otherwise.
    */
   @Nullable
   public EffectInstance getEffect(Effect pEffect) {
      return this.activeEffects.get(pEffect);
   }

   public boolean addEffect(EffectInstance pEffectInstance) {
      if (!this.canBeAffected(pEffectInstance)) {
         return false;
      } else {
         EffectInstance effectinstance = this.activeEffects.get(pEffectInstance.getEffect());
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent(this, effectinstance, pEffectInstance));
         if (effectinstance == null) {
            this.activeEffects.put(pEffectInstance.getEffect(), pEffectInstance);
            this.onEffectAdded(pEffectInstance);
            return true;
         } else if (effectinstance.update(pEffectInstance)) {
            this.onEffectUpdated(effectinstance, true);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean canBeAffected(EffectInstance pEffectInstance) {
      net.minecraftforge.event.entity.living.PotionEvent.PotionApplicableEvent event = new net.minecraftforge.event.entity.living.PotionEvent.PotionApplicableEvent(this, pEffectInstance);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
      if (event.getResult() != net.minecraftforge.eventbus.api.Event.Result.DEFAULT) return event.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW;
      if (this.getMobType() == CreatureAttribute.UNDEAD) {
         Effect effect = pEffectInstance.getEffect();
         if (effect == Effects.REGENERATION || effect == Effects.POISON) {
            return false;
         }
      }

      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public void forceAddEffect(EffectInstance pEffectInstance) {
      if (this.canBeAffected(pEffectInstance)) {
         EffectInstance effectinstance = this.activeEffects.put(pEffectInstance.getEffect(), pEffectInstance);
         if (effectinstance == null) {
            this.onEffectAdded(pEffectInstance);
         } else {
            this.onEffectUpdated(pEffectInstance, true);
         }

      }
   }

   /**
    * Returns true if this entity is undead.
    */
   public boolean isInvertedHealAndHarm() {
      return this.getMobType() == CreatureAttribute.UNDEAD;
   }

   /**
    * Removes the given potion effect from the active potion map and returns it. Does not call cleanup callbacks for the
    * end of the potion effect.
    */
   @Nullable
   public EffectInstance removeEffectNoUpdate(@Nullable Effect pEffect) {
      return this.activeEffects.remove(pEffect);
   }

   public boolean removeEffect(Effect pEffect) {
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent(this, pEffect))) return false;
      EffectInstance effectinstance = this.removeEffectNoUpdate(pEffect);
      if (effectinstance != null) {
         this.onEffectRemoved(effectinstance);
         return true;
      } else {
         return false;
      }
   }

   protected void onEffectAdded(EffectInstance pEffectInstancee) {
      this.effectsDirty = true;
      if (!this.level.isClientSide) {
         pEffectInstancee.getEffect().addAttributeModifiers(this, this.getAttributes(), pEffectInstancee.getAmplifier());
      }

   }

   protected void onEffectUpdated(EffectInstance pEffectInstance, boolean pResetAttributes) {
      this.effectsDirty = true;
      if (pResetAttributes && !this.level.isClientSide) {
         Effect effect = pEffectInstance.getEffect();
         effect.removeAttributeModifiers(this, this.getAttributes(), pEffectInstance.getAmplifier());
         effect.addAttributeModifiers(this, this.getAttributes(), pEffectInstance.getAmplifier());
      }

   }

   protected void onEffectRemoved(EffectInstance pEffectInstance) {
      this.effectsDirty = true;
      if (!this.level.isClientSide) {
         pEffectInstance.getEffect().removeAttributeModifiers(this, this.getAttributes(), pEffectInstance.getAmplifier());
      }

   }

   /**
    * Heal living entity (param: amount of half-hearts)
    */
   public void heal(float pHealAmount) {
      pHealAmount = net.minecraftforge.event.ForgeEventFactory.onLivingHeal(this, pHealAmount);
      if (pHealAmount <= 0) return;
      float f = this.getHealth();
      if (f > 0.0F) {
         this.setHealth(f + pHealAmount);
      }

   }

   public float getHealth() {
      return this.entityData.get(DATA_HEALTH_ID);
   }

   public void setHealth(float pHealth) {
      this.entityData.set(DATA_HEALTH_ID, MathHelper.clamp(pHealth, 0.0F, this.getMaxHealth()));
   }

   public boolean isDeadOrDying() {
      return this.getHealth() <= 0.0F;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(this, pSource, pAmount)) return false;
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (this.level.isClientSide) {
         return false;
      } else if (this.isDeadOrDying()) {
         return false;
      } else if (pSource.isFire() && this.hasEffect(Effects.FIRE_RESISTANCE)) {
         return false;
      } else {
         if (this.isSleeping() && !this.level.isClientSide) {
            this.stopSleeping();
         }

         this.noActionTime = 0;
         float f = pAmount;
         if ((pSource == DamageSource.ANVIL || pSource == DamageSource.FALLING_BLOCK) && !this.getItemBySlot(EquipmentSlotType.HEAD).isEmpty()) {
            this.getItemBySlot(EquipmentSlotType.HEAD).hurtAndBreak((int)(pAmount * 4.0F + this.random.nextFloat() * pAmount * 2.0F), this, (p_233653_0_) -> {
               p_233653_0_.broadcastBreakEvent(EquipmentSlotType.HEAD);
            });
            pAmount *= 0.75F;
         }

         boolean flag = false;
         float f1 = 0.0F;
         if (pAmount > 0.0F && this.isDamageSourceBlocked(pSource)) {
            this.hurtCurrentlyUsedShield(pAmount);
            f1 = pAmount;
            pAmount = 0.0F;
            if (!pSource.isProjectile()) {
               Entity entity = pSource.getDirectEntity();
               if (entity instanceof LivingEntity) {
                  this.blockUsingShield((LivingEntity)entity);
               }
            }

            flag = true;
         }

         this.animationSpeed = 1.5F;
         boolean flag1 = true;
         if ((float)this.invulnerableTime > 10.0F) {
            if (pAmount <= this.lastHurt) {
               return false;
            }

            this.actuallyHurt(pSource, pAmount - this.lastHurt);
            this.lastHurt = pAmount;
            flag1 = false;
         } else {
            this.lastHurt = pAmount;
            this.invulnerableTime = 20;
            this.actuallyHurt(pSource, pAmount);
            this.hurtDuration = 10;
            this.hurtTime = this.hurtDuration;
         }

         this.hurtDir = 0.0F;
         Entity entity1 = pSource.getEntity();
         if (entity1 != null) {
            if (entity1 instanceof LivingEntity) {
               this.setLastHurtByMob((LivingEntity)entity1);
            }

            if (entity1 instanceof PlayerEntity) {
               this.lastHurtByPlayerTime = 100;
               this.lastHurtByPlayer = (PlayerEntity)entity1;
            } else if (entity1 instanceof net.minecraft.entity.passive.TameableEntity) {
               net.minecraft.entity.passive.TameableEntity wolfentity = (net.minecraft.entity.passive.TameableEntity)entity1;
               if (wolfentity.isTame()) {
                  this.lastHurtByPlayerTime = 100;
                  LivingEntity livingentity = wolfentity.getOwner();
                  if (livingentity != null && livingentity.getType() == EntityType.PLAYER) {
                     this.lastHurtByPlayer = (PlayerEntity)livingentity;
                  } else {
                     this.lastHurtByPlayer = null;
                  }
               }
            }
         }

         if (flag1) {
            if (flag) {
               this.level.broadcastEntityEvent(this, (byte)29);
            } else if (pSource instanceof EntityDamageSource && ((EntityDamageSource)pSource).isThorns()) {
               this.level.broadcastEntityEvent(this, (byte)33);
            } else {
               byte b0;
               if (pSource == DamageSource.DROWN) {
                  b0 = 36;
               } else if (pSource.isFire()) {
                  b0 = 37;
               } else if (pSource == DamageSource.SWEET_BERRY_BUSH) {
                  b0 = 44;
               } else {
                  b0 = 2;
               }

               this.level.broadcastEntityEvent(this, b0);
            }

            if (pSource != DamageSource.DROWN && (!flag || pAmount > 0.0F)) {
               this.markHurt();
            }

            if (entity1 != null) {
               double d1 = entity1.getX() - this.getX();

               double d0;
               for(d0 = entity1.getZ() - this.getZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                  d1 = (Math.random() - Math.random()) * 0.01D;
               }

               this.hurtDir = (float)(MathHelper.atan2(d0, d1) * (double)(180F / (float)Math.PI) - (double)this.yRot);
               this.knockback(0.4F, d1, d0);
            } else {
               this.hurtDir = (float)((int)(Math.random() * 2.0D) * 180);
            }
         }

         if (this.isDeadOrDying()) {
            if (!this.checkTotemDeathProtection(pSource)) {
               SoundEvent soundevent = this.getDeathSound();
               if (flag1 && soundevent != null) {
                  this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
               }

               this.die(pSource);
            }
         } else if (flag1) {
            this.playHurtSound(pSource);
         }

         boolean flag2 = !flag || pAmount > 0.0F;
         if (flag2) {
            this.lastDamageSource = pSource;
            this.lastDamageStamp = this.level.getGameTime();
         }

         if (this instanceof ServerPlayerEntity) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity)this, pSource, f, pAmount, flag);
            if (f1 > 0.0F && f1 < 3.4028235E37F) {
               ((ServerPlayerEntity)this).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(f1 * 10.0F));
            }
         }

         if (entity1 instanceof ServerPlayerEntity) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity)entity1, this, pSource, f, pAmount, flag);
         }

         return flag2;
      }
   }

   protected void blockUsingShield(LivingEntity pAttacker) {
      pAttacker.blockedByShield(this);
   }

   protected void blockedByShield(LivingEntity pDefender) {
      pDefender.knockback(0.5F, pDefender.getX() - this.getX(), pDefender.getZ() - this.getZ());
   }

   private boolean checkTotemDeathProtection(DamageSource pDamageSource) {
      if (pDamageSource.isBypassInvul()) {
         return false;
      } else {
         ItemStack itemstack = null;

         for(Hand hand : Hand.values()) {
            ItemStack itemstack1 = this.getItemInHand(hand);
            if (itemstack1.getItem() == Items.TOTEM_OF_UNDYING) {
               itemstack = itemstack1.copy();
               itemstack1.shrink(1);
               break;
            }
         }

         if (itemstack != null) {
            if (this instanceof ServerPlayerEntity) {
               ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)this;
               serverplayerentity.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
               CriteriaTriggers.USED_TOTEM.trigger(serverplayerentity, itemstack);
            }

            this.setHealth(1.0F);
            this.removeAllEffects();
            this.addEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
            this.addEffect(new EffectInstance(Effects.ABSORPTION, 100, 1));
            this.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 800, 0));
            this.level.broadcastEntityEvent(this, (byte)35);
         }

         return itemstack != null;
      }
   }

   @Nullable
   public DamageSource getLastDamageSource() {
      if (this.level.getGameTime() - this.lastDamageStamp > 40L) {
         this.lastDamageSource = null;
      }

      return this.lastDamageSource;
   }

   protected void playHurtSound(DamageSource pSource) {
      SoundEvent soundevent = this.getHurtSound(pSource);
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   /**
    * Determines whether the entity can block the damage source based on the damage source's location, whether the
    * damage source is blockable, and whether the entity is blocking.
    */
   private boolean isDamageSourceBlocked(DamageSource pDamageSource) {
      Entity entity = pDamageSource.getDirectEntity();
      boolean flag = false;
      if (entity instanceof AbstractArrowEntity) {
         AbstractArrowEntity abstractarrowentity = (AbstractArrowEntity)entity;
         if (abstractarrowentity.getPierceLevel() > 0) {
            flag = true;
         }
      }

      if (!pDamageSource.isBypassArmor() && this.isBlocking() && !flag) {
         Vector3d vector3d2 = pDamageSource.getSourcePosition();
         if (vector3d2 != null) {
            Vector3d vector3d = this.getViewVector(1.0F);
            Vector3d vector3d1 = vector3d2.vectorTo(this.position()).normalize();
            vector3d1 = new Vector3d(vector3d1.x, 0.0D, vector3d1.z);
            if (vector3d1.dot(vector3d) < 0.0D) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Renders broken item particles using the given ItemStack
    */
   @OnlyIn(Dist.CLIENT)
   private void breakItem(ItemStack pStack) {
      if (!pStack.isEmpty()) {
         if (!this.isSilent()) {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_BREAK, this.getSoundSource(), 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F, false);
         }

         this.spawnItemParticles(pStack, 5);
      }

   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      if (net.minecraftforge.common.ForgeHooks.onLivingDeath(this, pCause)) return;
      if (!this.removed && !this.dead) {
         Entity entity = pCause.getEntity();
         LivingEntity livingentity = this.getKillCredit();
         if (this.deathScore >= 0 && livingentity != null) {
            livingentity.awardKillScore(this, this.deathScore, pCause);
         }

         if (this.isSleeping()) {
            this.stopSleeping();
         }

         this.dead = true;
         this.getCombatTracker().recheckStatus();
         if (this.level instanceof ServerWorld) {
            if (entity != null) {
               entity.killed((ServerWorld)this.level, this);
            }

            this.dropAllDeathLoot(pCause);
            this.createWitherRose(livingentity);
         }

         this.level.broadcastEntityEvent(this, (byte)3);
         this.setPose(Pose.DYING);
      }
   }

   protected void createWitherRose(@Nullable LivingEntity pEntitySource) {
      if (!this.level.isClientSide) {
         boolean flag = false;
         if (pEntitySource instanceof WitherEntity) {
            if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
               BlockPos blockpos = this.blockPosition();
               BlockState blockstate = Blocks.WITHER_ROSE.defaultBlockState();
               if (this.level.isEmptyBlock(blockpos) && blockstate.canSurvive(this.level, blockpos)) {
                  this.level.setBlock(blockpos, blockstate, 3);
                  flag = true;
               }
            }

            if (!flag) {
               ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
               this.level.addFreshEntity(itementity);
            }
         }

      }
   }

   protected void dropAllDeathLoot(DamageSource pDamageSource) {
      Entity entity = pDamageSource.getEntity();

      int i = net.minecraftforge.common.ForgeHooks.getLootingLevel(this, entity, pDamageSource);
      this.captureDrops(new java.util.ArrayList<>());

      boolean flag = this.lastHurtByPlayerTime > 0;
      if (this.shouldDropLoot() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         this.dropFromLootTable(pDamageSource, flag);
         this.dropCustomDeathLoot(pDamageSource, i, flag);
      }

      this.dropEquipment();
      this.dropExperience();

      Collection<ItemEntity> drops = captureDrops(null);
      if (!net.minecraftforge.common.ForgeHooks.onLivingDrops(this, pDamageSource, drops, i, lastHurtByPlayerTime > 0))
         drops.forEach(e -> level.addFreshEntity(e));
   }

   protected void dropEquipment() {
   }

   protected void dropExperience() {
      if (!this.level.isClientSide && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
         int i = this.getExperienceReward(this.lastHurtByPlayer);

         i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.lastHurtByPlayer, i);
         while(i > 0) {
            int j = ExperienceOrbEntity.getExperienceValue(i);
            i -= j;
            this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY(), this.getZ(), j));
         }
      }

   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
   }

   public ResourceLocation getLootTable() {
      return this.getType().getDefaultLootTable();
   }

   protected void dropFromLootTable(DamageSource pDamageSource, boolean pAttackedRecently) {
      ResourceLocation resourcelocation = this.getLootTable();
      LootTable loottable = this.level.getServer().getLootTables().get(resourcelocation);
      LootContext.Builder lootcontext$builder = this.createLootContext(pAttackedRecently, pDamageSource);
      LootContext ctx = lootcontext$builder.create(LootParameterSets.ENTITY);
      loottable.getRandomItems(ctx).forEach(this::spawnAtLocation);
   }

   protected LootContext.Builder createLootContext(boolean pAttackedRecently, DamageSource pDamageSource) {
      LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.level)).withRandom(this.random).withParameter(LootParameters.THIS_ENTITY, this).withParameter(LootParameters.ORIGIN, this.position()).withParameter(LootParameters.DAMAGE_SOURCE, pDamageSource).withOptionalParameter(LootParameters.KILLER_ENTITY, pDamageSource.getEntity()).withOptionalParameter(LootParameters.DIRECT_KILLER_ENTITY, pDamageSource.getDirectEntity());
      if (pAttackedRecently && this.lastHurtByPlayer != null) {
         lootcontext$builder = lootcontext$builder.withParameter(LootParameters.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
      }

      return lootcontext$builder;
   }

   public void knockback(float pStrength, double pRatioX, double pRatioZ) {
      net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(this, pStrength, pRatioX, pRatioZ);
      if(event.isCanceled()) return;
      pStrength = event.getStrength();
      pRatioX = event.getRatioX();
      pRatioZ = event.getRatioZ();
      pStrength = (float)((double)pStrength * (1.0D - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
      if (!(pStrength <= 0.0F)) {
         this.hasImpulse = true;
         Vector3d vector3d = this.getDeltaMovement();
         Vector3d vector3d1 = (new Vector3d(pRatioX, 0.0D, pRatioZ)).normalize().scale((double)pStrength);
         this.setDeltaMovement(vector3d.x / 2.0D - vector3d1.x, this.onGround ? Math.min(0.4D, vector3d.y / 2.0D + (double)pStrength) : vector3d.y, vector3d.z / 2.0D - vector3d1.z);
      }
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.GENERIC_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.GENERIC_DEATH;
   }

   protected SoundEvent getFallDamageSound(int pHeight) {
      return pHeight > 4 ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL;
   }

   protected SoundEvent getDrinkingSound(ItemStack pStack) {
      return pStack.getDrinkingSound();
   }

   public SoundEvent getEatingSound(ItemStack pStack) {
      return pStack.getEatingSound();
   }

   public void setOnGround(boolean pOnGround) {
      super.setOnGround(pOnGround);
      if (pOnGround) {
         this.lastClimbablePos = Optional.empty();
      }

   }

   public Optional<BlockPos> getLastClimbablePos() {
      return this.lastClimbablePos;
   }

   /**
    * Returns true if this entity should move as if it were on a ladder (either because it's actually on a ladder, or
    * for AI reasons)
    */
   public boolean onClimbable() {
      if (this.isSpectator()) {
         return false;
      } else {
         BlockPos blockpos = this.blockPosition();
         BlockState blockstate = this.getFeetBlockState();
         Optional<BlockPos> ladderPos = net.minecraftforge.common.ForgeHooks.isLivingOnLadderPos(blockstate, level, blockpos, this);
         if (ladderPos.isPresent()) this.lastClimbablePos = ladderPos;
         return ladderPos.isPresent();
      }
   }

   public BlockState getFeetBlockState() {
      return this.level.getBlockState(this.blockPosition());
   }

   private boolean trapdoorUsableAsLadder(BlockPos pPos, BlockState pState) {
      if (pState.getValue(TrapDoorBlock.OPEN)) {
         BlockState blockstate = this.level.getBlockState(pPos.below());
         if (blockstate.is(Blocks.LADDER) && blockstate.getValue(LadderBlock.FACING) == pState.getValue(TrapDoorBlock.FACING)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Returns true if the entity has not been {@link #removed}.
    */
   public boolean isAlive() {
      return !this.removed && this.getHealth() > 0.0F;
   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      float[] ret = net.minecraftforge.common.ForgeHooks.onLivingFall(this, pFallDistance, pDamageMultiplier);
      if (ret == null) return false;
      pFallDistance = ret[0];
      pDamageMultiplier = ret[1];

      boolean flag = super.causeFallDamage(pFallDistance, pDamageMultiplier);
      int i = this.calculateFallDamage(pFallDistance, pDamageMultiplier);
      if (i > 0) {
         this.playSound(this.getFallDamageSound(i), 1.0F, 1.0F);
         this.playBlockFallSound();
         this.hurt(DamageSource.FALL, (float)i);
         return true;
      } else {
         return flag;
      }
   }

   protected int calculateFallDamage(float pDistance, float pDamageMultiplier) {
      EffectInstance effectinstance = this.getEffect(Effects.JUMP);
      float f = effectinstance == null ? 0.0F : (float)(effectinstance.getAmplifier() + 1);
      return MathHelper.ceil((pDistance - 3.0F - f) * pDamageMultiplier);
   }

   /**
    * Plays the fall sound for the block landed on
    */
   protected void playBlockFallSound() {
      if (!this.isSilent()) {
         int i = MathHelper.floor(this.getX());
         int j = MathHelper.floor(this.getY() - (double)0.2F);
         int k = MathHelper.floor(this.getZ());
         BlockPos pos = new BlockPos(i, j, k);
         BlockState blockstate = this.level.getBlockState(pos);
         if (!blockstate.isAir(this.level, pos)) {
            SoundType soundtype = blockstate.getSoundType(level, pos, this);
            this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
         }

      }
   }

   /**
    * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
    */
   @OnlyIn(Dist.CLIENT)
   public void animateHurt() {
      this.hurtDuration = 10;
      this.hurtTime = this.hurtDuration;
      this.hurtDir = 0.0F;
   }

   /**
    * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
    */
   public int getArmorValue() {
      return MathHelper.floor(this.getAttributeValue(Attributes.ARMOR));
   }

   protected void hurtArmor(DamageSource pDamageSource, float pDamageAmount) {
   }

   protected void hurtCurrentlyUsedShield(float pDamageAmount) {
   }

   /**
    * Reduces damage, depending on armor
    */
   protected float getDamageAfterArmorAbsorb(DamageSource pDamageSource, float pDamageAmount) {
      if (!pDamageSource.isBypassArmor()) {
         this.hurtArmor(pDamageSource, pDamageAmount);
         pDamageAmount = CombatRules.getDamageAfterAbsorb(pDamageAmount, (float)this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
      }

      return pDamageAmount;
   }

   /**
    * Reduces damage, depending on potions
    */
   protected float getDamageAfterMagicAbsorb(DamageSource pDamageSource, float pDamageAmount) {
      if (pDamageSource.isBypassMagic()) {
         return pDamageAmount;
      } else {
         if (this.hasEffect(Effects.DAMAGE_RESISTANCE) && pDamageSource != DamageSource.OUT_OF_WORLD) {
            int i = (this.getEffect(Effects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = pDamageAmount * (float)j;
            float f1 = pDamageAmount;
            pDamageAmount = Math.max(f / 25.0F, 0.0F);
            float f2 = f1 - pDamageAmount;
            if (f2 > 0.0F && f2 < 3.4028235E37F) {
               if (this instanceof ServerPlayerEntity) {
                  ((ServerPlayerEntity)this).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
               } else if (pDamageSource.getEntity() instanceof ServerPlayerEntity) {
                  ((ServerPlayerEntity)pDamageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
               }
            }
         }

         if (pDamageAmount <= 0.0F) {
            return 0.0F;
         } else {
            int k = EnchantmentHelper.getDamageProtection(this.getArmorSlots(), pDamageSource);
            if (k > 0) {
               pDamageAmount = CombatRules.getDamageAfterMagicAbsorb(pDamageAmount, (float)k);
            }

            return pDamageAmount;
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
         float f = pDamageAmount - f2;
         if (f > 0.0F && f < 3.4028235E37F && pDamageSource.getEntity() instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)pDamageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
         }

         f2 = net.minecraftforge.common.ForgeHooks.onLivingDamage(this, pDamageSource, f2);
         if (f2 != 0.0F) {
            float f1 = this.getHealth();
            this.getCombatTracker().recordDamage(pDamageSource, f1, f2);
            this.setHealth(f1 - f2); // Forge: moved to fix MC-121048
            this.setAbsorptionAmount(this.getAbsorptionAmount() - f2);
         }
      }
   }

   /**
    * 1.8.9
    */
   public CombatTracker getCombatTracker() {
      return this.combatTracker;
   }

   @Nullable
   public LivingEntity getKillCredit() {
      if (this.combatTracker.getKiller() != null) {
         return this.combatTracker.getKiller();
      } else if (this.lastHurtByPlayer != null) {
         return this.lastHurtByPlayer;
      } else {
         return this.lastHurtByMob != null ? this.lastHurtByMob : null;
      }
   }

   /**
    * Returns the maximum health of the entity (what it is able to regenerate up to, what it spawned with, etc)
    */
   public final float getMaxHealth() {
      return (float)this.getAttributeValue(Attributes.MAX_HEALTH);
   }

   /**
    * counts the amount of arrows stuck in the entity. getting hit by arrows increases this, used in rendering
    */
   public final int getArrowCount() {
      return this.entityData.get(DATA_ARROW_COUNT_ID);
   }

   /**
    * sets the amount of arrows stuck in the entity. used for rendering those
    */
   public final void setArrowCount(int pCount) {
      this.entityData.set(DATA_ARROW_COUNT_ID, pCount);
   }

   public final int getStingerCount() {
      return this.entityData.get(DATA_STINGER_COUNT_ID);
   }

   public final void setStingerCount(int pCount) {
      this.entityData.set(DATA_STINGER_COUNT_ID, pCount);
   }

   /**
    * Returns an integer indicating the end point of the swing animation, used by {@link #swingProgress} to provide a
    * progress indicator. Takes dig speed enchantments into account.
    */
   private int getCurrentSwingDuration() {
      if (EffectUtils.hasDigSpeed(this)) {
         return 6 - (1 + EffectUtils.getDigSpeedAmplification(this));
      } else {
         return this.hasEffect(Effects.DIG_SLOWDOWN) ? 6 + (1 + this.getEffect(Effects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
      }
   }

   public void swing(Hand pHand) {
      this.swing(pHand, false);
   }

   public void swing(Hand pHand, boolean pUpdateSelf) {
      ItemStack stack = this.getItemInHand(pHand);
      if (!stack.isEmpty() && stack.onEntitySwing(this)) return;
      if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
         this.swingTime = -1;
         this.swinging = true;
         this.swingingArm = pHand;
         if (this.level instanceof ServerWorld) {
            SAnimateHandPacket sanimatehandpacket = new SAnimateHandPacket(this, pHand == Hand.MAIN_HAND ? 0 : 3);
            ServerChunkProvider serverchunkprovider = ((ServerWorld)this.level).getChunkSource();
            if (pUpdateSelf) {
               serverchunkprovider.broadcastAndSend(this, sanimatehandpacket);
            } else {
               serverchunkprovider.broadcast(this, sanimatehandpacket);
            }
         }
      }

   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      switch(pId) {
      case 2:
      case 33:
      case 36:
      case 37:
      case 44:
         boolean flag1 = pId == 33;
         boolean flag2 = pId == 36;
         boolean flag3 = pId == 37;
         boolean flag = pId == 44;
         this.animationSpeed = 1.5F;
         this.invulnerableTime = 20;
         this.hurtDuration = 10;
         this.hurtTime = this.hurtDuration;
         this.hurtDir = 0.0F;
         if (flag1) {
            this.playSound(SoundEvents.THORNS_HIT, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         }

         DamageSource damagesource;
         if (flag3) {
            damagesource = DamageSource.ON_FIRE;
         } else if (flag2) {
            damagesource = DamageSource.DROWN;
         } else if (flag) {
            damagesource = DamageSource.SWEET_BERRY_BUSH;
         } else {
            damagesource = DamageSource.GENERIC;
         }

         SoundEvent soundevent1 = this.getHurtSound(damagesource);
         if (soundevent1 != null) {
            this.playSound(soundevent1, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         }

         this.hurt(DamageSource.GENERIC, 0.0F);
         break;
      case 3:
         SoundEvent soundevent = this.getDeathSound();
         if (soundevent != null) {
            this.playSound(soundevent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         }

         if (!(this instanceof PlayerEntity)) {
            this.setHealth(0.0F);
            this.die(DamageSource.GENERIC);
         }
         break;
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 31:
      case 32:
      case 34:
      case 35:
      case 38:
      case 39:
      case 40:
      case 41:
      case 42:
      case 43:
      case 45:
      case 53:
      default:
         super.handleEntityEvent(pId);
         break;
      case 29:
         this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + this.level.random.nextFloat() * 0.4F);
         break;
      case 30:
         this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
         break;
      case 46:
         int i = 128;

         for(int j = 0; j < 128; ++j) {
            double d0 = (double)j / 127.0D;
            float f = (this.random.nextFloat() - 0.5F) * 0.2F;
            float f1 = (this.random.nextFloat() - 0.5F) * 0.2F;
            float f2 = (this.random.nextFloat() - 0.5F) * 0.2F;
            double d1 = MathHelper.lerp(d0, this.xo, this.getX()) + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth() * 2.0D;
            double d2 = MathHelper.lerp(d0, this.yo, this.getY()) + this.random.nextDouble() * (double)this.getBbHeight();
            double d3 = MathHelper.lerp(d0, this.zo, this.getZ()) + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth() * 2.0D;
            this.level.addParticle(ParticleTypes.PORTAL, d1, d2, d3, (double)f, (double)f1, (double)f2);
         }
         break;
      case 47:
         this.breakItem(this.getItemBySlot(EquipmentSlotType.MAINHAND));
         break;
      case 48:
         this.breakItem(this.getItemBySlot(EquipmentSlotType.OFFHAND));
         break;
      case 49:
         this.breakItem(this.getItemBySlot(EquipmentSlotType.HEAD));
         break;
      case 50:
         this.breakItem(this.getItemBySlot(EquipmentSlotType.CHEST));
         break;
      case 51:
         this.breakItem(this.getItemBySlot(EquipmentSlotType.LEGS));
         break;
      case 52:
         this.breakItem(this.getItemBySlot(EquipmentSlotType.FEET));
         break;
      case 54:
         HoneyBlock.showJumpParticles(this);
         break;
      case 55:
         this.swapHandItems();
      }

   }

   @OnlyIn(Dist.CLIENT)
   private void swapHandItems() {
      ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.OFFHAND);
      this.setItemSlot(EquipmentSlotType.OFFHAND, this.getItemBySlot(EquipmentSlotType.MAINHAND));
      this.setItemSlot(EquipmentSlotType.MAINHAND, itemstack);
   }

   /**
    * sets the dead flag. Used when you fall off the bottom of the world.
    */
   protected void outOfWorld() {
      this.hurt(DamageSource.OUT_OF_WORLD, 4.0F);
   }

   /**
    * Updates the arm swing progress counters and animation progress
    */
   protected void updateSwingTime() {
      int i = this.getCurrentSwingDuration();
      if (this.swinging) {
         ++this.swingTime;
         if (this.swingTime >= i) {
            this.swingTime = 0;
            this.swinging = false;
         }
      } else {
         this.swingTime = 0;
      }

      this.attackAnim = (float)this.swingTime / (float)i;
   }

   @Nullable
   public ModifiableAttributeInstance getAttribute(Attribute pAttribute) {
      return this.getAttributes().getInstance(pAttribute);
   }

   public double getAttributeValue(Attribute pAttribute) {
      return this.getAttributes().getValue(pAttribute);
   }

   public double getAttributeBaseValue(Attribute pAttribute) {
      return this.getAttributes().getBaseValue(pAttribute);
   }

   public AttributeModifierManager getAttributes() {
      return this.attributes;
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.UNDEFINED;
   }

   public ItemStack getMainHandItem() {
      return this.getItemBySlot(EquipmentSlotType.MAINHAND);
   }

   public ItemStack getOffhandItem() {
      return this.getItemBySlot(EquipmentSlotType.OFFHAND);
   }

   public boolean isHolding(Item pItem) {
      return this.isHolding((p_233632_1_) -> {
         return p_233632_1_ == pItem;
      });
   }

   public boolean isHolding(Predicate<Item> pPredicate) {
      return pPredicate.test(this.getMainHandItem().getItem()) || pPredicate.test(this.getOffhandItem().getItem());
   }

   public ItemStack getItemInHand(Hand pHand) {
      if (pHand == Hand.MAIN_HAND) {
         return this.getItemBySlot(EquipmentSlotType.MAINHAND);
      } else if (pHand == Hand.OFF_HAND) {
         return this.getItemBySlot(EquipmentSlotType.OFFHAND);
      } else {
         throw new IllegalArgumentException("Invalid hand " + pHand);
      }
   }

   public void setItemInHand(Hand pHand, ItemStack pStack) {
      if (pHand == Hand.MAIN_HAND) {
         this.setItemSlot(EquipmentSlotType.MAINHAND, pStack);
      } else {
         if (pHand != Hand.OFF_HAND) {
            throw new IllegalArgumentException("Invalid hand " + pHand);
         }

         this.setItemSlot(EquipmentSlotType.OFFHAND, pStack);
      }

   }

   public boolean hasItemInSlot(EquipmentSlotType pSlot) {
      return !this.getItemBySlot(pSlot).isEmpty();
   }

   public abstract Iterable<ItemStack> getArmorSlots();

   public abstract ItemStack getItemBySlot(EquipmentSlotType pSlot);

   public abstract void setItemSlot(EquipmentSlotType pSlot, ItemStack pStack);

   public float getArmorCoverPercentage() {
      Iterable<ItemStack> iterable = this.getArmorSlots();
      int i = 0;
      int j = 0;

      for(ItemStack itemstack : iterable) {
         if (!itemstack.isEmpty()) {
            ++j;
         }

         ++i;
      }

      return i > 0 ? (float)j / (float)i : 0.0F;
   }

   /**
    * Set sprinting switch for Entity.
    */
   public void setSprinting(boolean pSprinting) {
      super.setSprinting(pSprinting);
      ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (modifiableattributeinstance.getModifier(SPEED_MODIFIER_SPRINTING_UUID) != null) {
         modifiableattributeinstance.removeModifier(SPEED_MODIFIER_SPRINTING);
      }

      if (pSprinting) {
         modifiableattributeinstance.addTransientModifier(SPEED_MODIFIER_SPRINTING);
      }

   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 1.0F;
   }

   /**
    * Gets the pitch of living sounds in living entities.
    */
   protected float getVoicePitch() {
      return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
   }

   /**
    * Dead and sleeping entities cannot move
    */
   protected boolean isImmobile() {
      return this.isDeadOrDying();
   }

   /**
    * Applies a velocity to the entities, to push them away from eachother.
    */
   public void push(Entity pEntity) {
      if (!this.isSleeping()) {
         super.push(pEntity);
      }

   }

   private void dismountVehicle(Entity pVehicle) {
      Vector3d vector3d;
      if (!pVehicle.removed && !this.level.getBlockState(pVehicle.blockPosition()).getBlock().is(BlockTags.PORTALS)) {
         vector3d = pVehicle.getDismountLocationForPassenger(this);
      } else {
         vector3d = new Vector3d(pVehicle.getX(), pVehicle.getY() + (double)pVehicle.getBbHeight(), pVehicle.getZ());
      }

      this.teleportTo(vector3d.x, vector3d.y, vector3d.z);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldShowName() {
      return this.isCustomNameVisible();
   }

   protected float getJumpPower() {
      return 0.42F * this.getBlockJumpFactor();
   }

   /**
    * Causes this entity to do an upwards motion (jumping).
    */
   protected void jumpFromGround() {
      float f = this.getJumpPower();
      if (this.hasEffect(Effects.JUMP)) {
         f += 0.1F * (float)(this.getEffect(Effects.JUMP).getAmplifier() + 1);
      }

      Vector3d vector3d = this.getDeltaMovement();
      this.setDeltaMovement(vector3d.x, (double)f, vector3d.z);
      if (this.isSprinting()) {
         float f1 = this.yRot * ((float)Math.PI / 180F);
         this.setDeltaMovement(this.getDeltaMovement().add((double)(-MathHelper.sin(f1) * 0.2F), 0.0D, (double)(MathHelper.cos(f1) * 0.2F)));
      }

      this.hasImpulse = true;
      net.minecraftforge.common.ForgeHooks.onLivingJump(this);
   }

   @OnlyIn(Dist.CLIENT)
   protected void goDownInWater() {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)-0.04F * this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue(), 0.0D));
   }

   protected void jumpInLiquid(ITag<Fluid> pFluidTag) {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)0.04F * this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue(), 0.0D));
   }

   protected float getWaterSlowDown() {
      return 0.8F;
   }

   public boolean canStandOnFluid(Fluid pFluid) {
      return false;
   }

   public void travel(Vector3d pTravelVector) {
      if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
         double d0 = 0.08D;
         ModifiableAttributeInstance gravity = this.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
         boolean flag = this.getDeltaMovement().y <= 0.0D;
         if (flag && this.hasEffect(Effects.SLOW_FALLING)) {
            if (!gravity.hasModifier(SLOW_FALLING)) gravity.addTransientModifier(SLOW_FALLING);
            this.fallDistance = 0.0F;
         } else if (gravity.hasModifier(SLOW_FALLING)) {
            gravity.removeModifier(SLOW_FALLING);
         }
         d0 = gravity.getValue();

         FluidState fluidstate = this.level.getFluidState(this.blockPosition());
         if (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType())) {
            double d8 = this.getY();
            float f5 = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
            float f6 = 0.02F;
            float f7 = (float)EnchantmentHelper.getDepthStrider(this);
            if (f7 > 3.0F) {
               f7 = 3.0F;
            }

            if (!this.onGround) {
               f7 *= 0.5F;
            }

            if (f7 > 0.0F) {
               f5 += (0.54600006F - f5) * f7 / 3.0F;
               f6 += (this.getSpeed() - f6) * f7 / 3.0F;
            }

            if (this.hasEffect(Effects.DOLPHINS_GRACE)) {
               f5 = 0.96F;
            }

            f6 *= (float)this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
            this.moveRelative(f6, pTravelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vector3d vector3d6 = this.getDeltaMovement();
            if (this.horizontalCollision && this.onClimbable()) {
               vector3d6 = new Vector3d(vector3d6.x, 0.2D, vector3d6.z);
            }

            this.setDeltaMovement(vector3d6.multiply((double)f5, (double)0.8F, (double)f5));
            Vector3d vector3d2 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
            this.setDeltaMovement(vector3d2);
            if (this.horizontalCollision && this.isFree(vector3d2.x, vector3d2.y + (double)0.6F - this.getY() + d8, vector3d2.z)) {
               this.setDeltaMovement(vector3d2.x, (double)0.3F, vector3d2.z);
            }
         } else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType())) {
            double d7 = this.getY();
            this.moveRelative(0.02F, pTravelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, (double)0.8F, 0.5D));
               Vector3d vector3d3 = this.getFluidFallingAdjustedMovement(d0, flag, this.getDeltaMovement());
               this.setDeltaMovement(vector3d3);
            } else {
               this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            }

            if (!this.isNoGravity()) {
               this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
            }

            Vector3d vector3d4 = this.getDeltaMovement();
            if (this.horizontalCollision && this.isFree(vector3d4.x, vector3d4.y + (double)0.6F - this.getY() + d7, vector3d4.z)) {
               this.setDeltaMovement(vector3d4.x, (double)0.3F, vector3d4.z);
            }
         } else if (this.isFallFlying()) {
            Vector3d vector3d = this.getDeltaMovement();
            if (vector3d.y > -0.5D) {
               this.fallDistance = 1.0F;
            }

            Vector3d vector3d1 = this.getLookAngle();
            float f = this.xRot * ((float)Math.PI / 180F);
            double d1 = Math.sqrt(vector3d1.x * vector3d1.x + vector3d1.z * vector3d1.z);
            double d3 = Math.sqrt(getHorizontalDistanceSqr(vector3d));
            double d4 = vector3d1.length();
            float f1 = MathHelper.cos(f);
            f1 = (float)((double)f1 * (double)f1 * Math.min(1.0D, d4 / 0.4D));
            vector3d = this.getDeltaMovement().add(0.0D, d0 * (-1.0D + (double)f1 * 0.75D), 0.0D);
            if (vector3d.y < 0.0D && d1 > 0.0D) {
               double d5 = vector3d.y * -0.1D * (double)f1;
               vector3d = vector3d.add(vector3d1.x * d5 / d1, d5, vector3d1.z * d5 / d1);
            }

            if (f < 0.0F && d1 > 0.0D) {
               double d9 = d3 * (double)(-MathHelper.sin(f)) * 0.04D;
               vector3d = vector3d.add(-vector3d1.x * d9 / d1, d9 * 3.2D, -vector3d1.z * d9 / d1);
            }

            if (d1 > 0.0D) {
               vector3d = vector3d.add((vector3d1.x / d1 * d3 - vector3d.x) * 0.1D, 0.0D, (vector3d1.z / d1 * d3 - vector3d.z) * 0.1D);
            }

            this.setDeltaMovement(vector3d.multiply((double)0.99F, (double)0.98F, (double)0.99F));
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.horizontalCollision && !this.level.isClientSide) {
               double d10 = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
               double d6 = d3 - d10;
               float f2 = (float)(d6 * 10.0D - 3.0D);
               if (f2 > 0.0F) {
                  this.playSound(this.getFallDamageSound((int)f2), 1.0F, 1.0F);
                  this.hurt(DamageSource.FLY_INTO_WALL, f2);
               }
            }

            if (this.onGround && !this.level.isClientSide) {
               this.setSharedFlag(7, false);
            }
         } else {
            BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
            float f3 = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getSlipperiness(level, this.getBlockPosBelowThatAffectsMyMovement(), this);
            float f4 = this.onGround ? f3 * 0.91F : 0.91F;
            Vector3d vector3d5 = this.handleRelativeFrictionAndCalculateMovement(pTravelVector, f3);
            double d2 = vector3d5.y;
            if (this.hasEffect(Effects.LEVITATION)) {
               d2 += (0.05D * (double)(this.getEffect(Effects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
               this.fallDistance = 0.0F;
            } else if (this.level.isClientSide && !this.level.hasChunkAt(blockpos)) {
               if (this.getY() > 0.0D) {
                  d2 = -0.1D;
               } else {
                  d2 = 0.0D;
               }
            } else if (!this.isNoGravity()) {
               d2 -= d0;
            }

            this.setDeltaMovement(vector3d5.x * (double)f4, d2 * (double)0.98F, vector3d5.z * (double)f4);
         }
      }

      this.calculateEntityAnimation(this, this instanceof IFlyingAnimal);
   }

   public void calculateEntityAnimation(LivingEntity pLivingEntity, boolean pIsFlying) {
      pLivingEntity.animationSpeedOld = pLivingEntity.animationSpeed;
      double d0 = pLivingEntity.getX() - pLivingEntity.xo;
      double d1 = pIsFlying ? pLivingEntity.getY() - pLivingEntity.yo : 0.0D;
      double d2 = pLivingEntity.getZ() - pLivingEntity.zo;
      float f = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 4.0F;
      if (f > 1.0F) {
         f = 1.0F;
      }

      pLivingEntity.animationSpeed += (f - pLivingEntity.animationSpeed) * 0.4F;
      pLivingEntity.animationPosition += pLivingEntity.animationSpeed;
   }

   public Vector3d handleRelativeFrictionAndCalculateMovement(Vector3d pDeltaMovement, float pFriction) {
      this.moveRelative(this.getFrictionInfluencedSpeed(pFriction), pDeltaMovement);
      this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
      this.move(MoverType.SELF, this.getDeltaMovement());
      Vector3d vector3d = this.getDeltaMovement();
      if ((this.horizontalCollision || this.jumping) && this.onClimbable()) {
         vector3d = new Vector3d(vector3d.x, 0.2D, vector3d.z);
      }

      return vector3d;
   }

   public Vector3d getFluidFallingAdjustedMovement(double pGravity, boolean pIsFalling, Vector3d pDeltaMovement) {
      if (!this.isNoGravity() && !this.isSprinting()) {
         double d0;
         if (pIsFalling && Math.abs(pDeltaMovement.y - 0.005D) >= 0.003D && Math.abs(pDeltaMovement.y - pGravity / 16.0D) < 0.003D) {
            d0 = -0.003D;
         } else {
            d0 = pDeltaMovement.y - pGravity / 16.0D;
         }

         return new Vector3d(pDeltaMovement.x, d0, pDeltaMovement.z);
      } else {
         return pDeltaMovement;
      }
   }

   private Vector3d handleOnClimbable(Vector3d pDeltaMovement) {
      if (this.onClimbable()) {
         this.fallDistance = 0.0F;
         float f = 0.15F;
         double d0 = MathHelper.clamp(pDeltaMovement.x, (double)-0.15F, (double)0.15F);
         double d1 = MathHelper.clamp(pDeltaMovement.z, (double)-0.15F, (double)0.15F);
         double d2 = Math.max(pDeltaMovement.y, (double)-0.15F);
         if (d2 < 0.0D && !this.getFeetBlockState().isScaffolding(this) && this.isSuppressingSlidingDownLadder() && this instanceof PlayerEntity) {
            d2 = 0.0D;
         }

         pDeltaMovement = new Vector3d(d0, d2, d1);
      }

      return pDeltaMovement;
   }

   private float getFrictionInfluencedSpeed(float pFriction) {
      return this.onGround ? this.getSpeed() * (0.21600002F / (pFriction * pFriction * pFriction)) : this.flyingSpeed;
   }

   /**
    * the movespeed used for the new AI system
    */
   public float getSpeed() {
      return this.speed;
   }

   /**
    * set the movespeed used for the new AI system
    */
   public void setSpeed(float pSpeed) {
      this.speed = pSpeed;
   }

   public boolean doHurtTarget(Entity pEntity) {
      this.setLastHurtMob(pEntity);
      return false;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (net.minecraftforge.common.ForgeHooks.onLivingUpdate(this)) return;
      super.tick();
      this.updatingUsingItem();
      this.updateSwimAmount();
      if (!this.level.isClientSide) {
         int i = this.getArrowCount();
         if (i > 0) {
            if (this.removeArrowTime <= 0) {
               this.removeArrowTime = 20 * (30 - i);
            }

            --this.removeArrowTime;
            if (this.removeArrowTime <= 0) {
               this.setArrowCount(i - 1);
            }
         }

         int j = this.getStingerCount();
         if (j > 0) {
            if (this.removeStingerTime <= 0) {
               this.removeStingerTime = 20 * (30 - j);
            }

            --this.removeStingerTime;
            if (this.removeStingerTime <= 0) {
               this.setStingerCount(j - 1);
            }
         }

         this.detectEquipmentUpdates();
         if (this.tickCount % 20 == 0) {
            this.getCombatTracker().recheckStatus();
         }

         if (!this.glowing) {
            boolean flag = this.hasEffect(Effects.GLOWING);
            if (this.getSharedFlag(6) != flag) {
               this.setSharedFlag(6, flag);
            }
         }

         if (this.isSleeping() && !this.checkBedExists()) {
            this.stopSleeping();
         }
      }

      this.aiStep();
      double d0 = this.getX() - this.xo;
      double d1 = this.getZ() - this.zo;
      float f = (float)(d0 * d0 + d1 * d1);
      float f1 = this.yBodyRot;
      float f2 = 0.0F;
      this.oRun = this.run;
      float f3 = 0.0F;
      if (f > 0.0025000002F) {
         f3 = 1.0F;
         f2 = (float)Math.sqrt((double)f) * 3.0F;
         float f4 = (float)MathHelper.atan2(d1, d0) * (180F / (float)Math.PI) - 90.0F;
         float f5 = MathHelper.abs(MathHelper.wrapDegrees(this.yRot) - f4);
         if (95.0F < f5 && f5 < 265.0F) {
            f1 = f4 - 180.0F;
         } else {
            f1 = f4;
         }
      }

      if (this.attackAnim > 0.0F) {
         f1 = this.yRot;
      }

      if (!this.onGround) {
         f3 = 0.0F;
      }

      this.run += (f3 - this.run) * 0.3F;
      this.level.getProfiler().push("headTurn");
      f2 = this.tickHeadTurn(f1, f2);
      this.level.getProfiler().pop();
      this.level.getProfiler().push("rangeChecks");

      while(this.yRot - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }

      while(this.yRot - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }

      while(this.yBodyRot - this.yBodyRotO < -180.0F) {
         this.yBodyRotO -= 360.0F;
      }

      while(this.yBodyRot - this.yBodyRotO >= 180.0F) {
         this.yBodyRotO += 360.0F;
      }

      while(this.xRot - this.xRotO < -180.0F) {
         this.xRotO -= 360.0F;
      }

      while(this.xRot - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }

      while(this.yHeadRot - this.yHeadRotO < -180.0F) {
         this.yHeadRotO -= 360.0F;
      }

      while(this.yHeadRot - this.yHeadRotO >= 180.0F) {
         this.yHeadRotO += 360.0F;
      }

      this.level.getProfiler().pop();
      this.animStep += f2;
      if (this.isFallFlying()) {
         ++this.fallFlyTicks;
      } else {
         this.fallFlyTicks = 0;
      }

      if (this.isSleeping()) {
         this.xRot = 0.0F;
      }

   }

   private void detectEquipmentUpdates() {
      Map<EquipmentSlotType, ItemStack> map = this.collectEquipmentChanges();
      if (map != null) {
         this.handleHandSwap(map);
         if (!map.isEmpty()) {
            this.handleEquipmentChanges(map);
         }
      }

   }

   @Nullable
   private Map<EquipmentSlotType, ItemStack> collectEquipmentChanges() {
      Map<EquipmentSlotType, ItemStack> map = null;

      for(EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
         ItemStack itemstack;
         switch(equipmentslottype.getType()) {
         case HAND:
            itemstack = this.getLastHandItem(equipmentslottype);
            break;
         case ARMOR:
            itemstack = this.getLastArmorItem(equipmentslottype);
            break;
         default:
            continue;
         }

         ItemStack itemstack1 = this.getItemBySlot(equipmentslottype);
         if (!ItemStack.matches(itemstack1, itemstack)) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent(this, equipmentslottype, itemstack, itemstack1));
            if (map == null) {
               map = Maps.newEnumMap(EquipmentSlotType.class);
            }

            map.put(equipmentslottype, itemstack1);
            if (!itemstack.isEmpty()) {
               this.getAttributes().removeAttributeModifiers(itemstack.getAttributeModifiers(equipmentslottype));
            }

            if (!itemstack1.isEmpty()) {
               this.getAttributes().addTransientAttributeModifiers(itemstack1.getAttributeModifiers(equipmentslottype));
            }
         }
      }

      return map;
   }

   private void handleHandSwap(Map<EquipmentSlotType, ItemStack> pHands) {
      ItemStack itemstack = pHands.get(EquipmentSlotType.MAINHAND);
      ItemStack itemstack1 = pHands.get(EquipmentSlotType.OFFHAND);
      if (itemstack != null && itemstack1 != null && ItemStack.matches(itemstack, this.getLastHandItem(EquipmentSlotType.OFFHAND)) && ItemStack.matches(itemstack1, this.getLastHandItem(EquipmentSlotType.MAINHAND))) {
         ((ServerWorld)this.level).getChunkSource().broadcast(this, new SEntityStatusPacket(this, (byte)55));
         pHands.remove(EquipmentSlotType.MAINHAND);
         pHands.remove(EquipmentSlotType.OFFHAND);
         this.setLastHandItem(EquipmentSlotType.MAINHAND, itemstack.copy());
         this.setLastHandItem(EquipmentSlotType.OFFHAND, itemstack1.copy());
      }

   }

   private void handleEquipmentChanges(Map<EquipmentSlotType, ItemStack> pEquipments) {
      List<Pair<EquipmentSlotType, ItemStack>> list = Lists.newArrayListWithCapacity(pEquipments.size());
      pEquipments.forEach((p_241341_2_, p_241341_3_) -> {
         ItemStack itemstack = p_241341_3_.copy();
         list.add(Pair.of(p_241341_2_, itemstack));
         switch(p_241341_2_.getType()) {
         case HAND:
            this.setLastHandItem(p_241341_2_, itemstack);
            break;
         case ARMOR:
            this.setLastArmorItem(p_241341_2_, itemstack);
         }

      });
      ((ServerWorld)this.level).getChunkSource().broadcast(this, new SEntityEquipmentPacket(this.getId(), list));
   }

   private ItemStack getLastArmorItem(EquipmentSlotType pSlot) {
      return this.lastArmorItemStacks.get(pSlot.getIndex());
   }

   private void setLastArmorItem(EquipmentSlotType pSlot, ItemStack pStack) {
      this.lastArmorItemStacks.set(pSlot.getIndex(), pStack);
   }

   private ItemStack getLastHandItem(EquipmentSlotType pSlot) {
      return this.lastHandItemStacks.get(pSlot.getIndex());
   }

   private void setLastHandItem(EquipmentSlotType pSlot, ItemStack pStack) {
      this.lastHandItemStacks.set(pSlot.getIndex(), pStack);
   }

   protected float tickHeadTurn(float pYRot, float pAnimStep) {
      float f = MathHelper.wrapDegrees(pYRot - this.yBodyRot);
      this.yBodyRot += f * 0.3F;
      float f1 = MathHelper.wrapDegrees(this.yRot - this.yBodyRot);
      boolean flag = f1 < -90.0F || f1 >= 90.0F;
      if (f1 < -75.0F) {
         f1 = -75.0F;
      }

      if (f1 >= 75.0F) {
         f1 = 75.0F;
      }

      this.yBodyRot = this.yRot - f1;
      if (f1 * f1 > 2500.0F) {
         this.yBodyRot += f1 * 0.2F;
      }

      if (flag) {
         pAnimStep *= -1.0F;
      }

      return pAnimStep;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.noJumpDelay > 0) {
         --this.noJumpDelay;
      }

      if (this.isControlledByLocalInstance()) {
         this.lerpSteps = 0;
         this.setPacketCoordinates(this.getX(), this.getY(), this.getZ());
      }

      if (this.lerpSteps > 0) {
         double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
         double d2 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
         double d4 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
         double d6 = MathHelper.wrapDegrees(this.lerpYRot - (double)this.yRot);
         this.yRot = (float)((double)this.yRot + d6 / (double)this.lerpSteps);
         this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
         --this.lerpSteps;
         this.setPos(d0, d2, d4);
         this.setRot(this.yRot, this.xRot);
      } else if (!this.isEffectiveAi()) {
         this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
      }

      if (this.lerpHeadSteps > 0) {
         this.yHeadRot = (float)((double)this.yHeadRot + MathHelper.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
         --this.lerpHeadSteps;
      }

      Vector3d vector3d = this.getDeltaMovement();
      double d1 = vector3d.x;
      double d3 = vector3d.y;
      double d5 = vector3d.z;
      if (Math.abs(vector3d.x) < 0.003D) {
         d1 = 0.0D;
      }

      if (Math.abs(vector3d.y) < 0.003D) {
         d3 = 0.0D;
      }

      if (Math.abs(vector3d.z) < 0.003D) {
         d5 = 0.0D;
      }

      this.setDeltaMovement(d1, d3, d5);
      this.level.getProfiler().push("ai");
      if (this.isImmobile()) {
         this.jumping = false;
         this.xxa = 0.0F;
         this.zza = 0.0F;
      } else if (this.isEffectiveAi()) {
         this.level.getProfiler().push("newAi");
         this.serverAiStep();
         this.level.getProfiler().pop();
      }

      this.level.getProfiler().pop();
      this.level.getProfiler().push("jump");
      if (this.jumping && this.isAffectedByFluids()) {
         double d7;
         if (this.isInLava()) {
            d7 = this.getFluidHeight(FluidTags.LAVA);
         } else {
            d7 = this.getFluidHeight(FluidTags.WATER);
         }

         boolean flag = this.isInWater() && d7 > 0.0D;
         double d8 = this.getFluidJumpThreshold();
         if (!flag || this.onGround && !(d7 > d8)) {
            if (!this.isInLava() || this.onGround && !(d7 > d8)) {
               if ((this.onGround || flag && d7 <= d8) && this.noJumpDelay == 0) {
                  this.jumpFromGround();
                  this.noJumpDelay = 10;
               }
            } else {
               this.jumpInLiquid(FluidTags.LAVA);
            }
         } else {
            this.jumpInLiquid(FluidTags.WATER);
         }
      } else {
         this.noJumpDelay = 0;
      }

      this.level.getProfiler().pop();
      this.level.getProfiler().push("travel");
      this.xxa *= 0.98F;
      this.zza *= 0.98F;
      this.updateFallFlying();
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      this.travel(new Vector3d((double)this.xxa, (double)this.yya, (double)this.zza));
      this.level.getProfiler().pop();
      this.level.getProfiler().push("push");
      if (this.autoSpinAttackTicks > 0) {
         --this.autoSpinAttackTicks;
         this.checkAutoSpinAttack(axisalignedbb, this.getBoundingBox());
      }

      this.pushEntities();
      this.level.getProfiler().pop();
      if (!this.level.isClientSide && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
         this.hurt(DamageSource.DROWN, 1.0F);
      }

   }

   public boolean isSensitiveToWater() {
      return false;
   }

   /**
    * Called each tick. Updates state for the elytra.
    */
   private void updateFallFlying() {
      boolean flag = this.getSharedFlag(7);
      if (flag && !this.onGround && !this.isPassenger() && !this.hasEffect(Effects.LEVITATION)) {
         ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.CHEST);
         flag = itemstack.canElytraFly(this) && itemstack.elytraFlightTick(this, this.fallFlyTicks);
         if (false) //Forge: Moved to ElytraItem
         if (itemstack.getItem() == Items.ELYTRA && ElytraItem.isFlyEnabled(itemstack)) {
            flag = true;
            if (!this.level.isClientSide && (this.fallFlyTicks + 1) % 20 == 0) {
               itemstack.hurtAndBreak(1, this, (p_233652_0_) -> {
                  p_233652_0_.broadcastBreakEvent(EquipmentSlotType.CHEST);
               });
            }
         } else {
            flag = false;
         }
      } else {
         flag = false;
      }

      if (!this.level.isClientSide) {
         this.setSharedFlag(7, flag);
      }

   }

   protected void serverAiStep() {
   }

   protected void pushEntities() {
      List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), EntityPredicates.pushableBy(this));
      if (!list.isEmpty()) {
         int i = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
         if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
            int j = 0;

            for(int k = 0; k < list.size(); ++k) {
               if (!list.get(k).isPassenger()) {
                  ++j;
               }
            }

            if (j > i - 1) {
               this.hurt(DamageSource.CRAMMING, 6.0F);
            }
         }

         for(int l = 0; l < list.size(); ++l) {
            Entity entity = list.get(l);
            this.doPush(entity);
         }
      }

   }

   protected void checkAutoSpinAttack(AxisAlignedBB pBoundingBoxBeforeSpin, AxisAlignedBB pBoundingBoxAfterSpin) {
      AxisAlignedBB axisalignedbb = pBoundingBoxBeforeSpin.minmax(pBoundingBoxAfterSpin);
      List<Entity> list = this.level.getEntities(this, axisalignedbb);
      if (!list.isEmpty()) {
         for(int i = 0; i < list.size(); ++i) {
            Entity entity = list.get(i);
            if (entity instanceof LivingEntity) {
               this.doAutoAttackOnTouch((LivingEntity)entity);
               this.autoSpinAttackTicks = 0;
               this.setDeltaMovement(this.getDeltaMovement().scale(-0.2D));
               break;
            }
         }
      } else if (this.horizontalCollision) {
         this.autoSpinAttackTicks = 0;
      }

      if (!this.level.isClientSide && this.autoSpinAttackTicks <= 0) {
         this.setLivingEntityFlag(4, false);
      }

   }

   protected void doPush(Entity pEntity) {
      pEntity.push(this);
   }

   protected void doAutoAttackOnTouch(LivingEntity pTarget) {
   }

   public void startAutoSpinAttack(int pAutoSpinAttackTicks) {
      this.autoSpinAttackTicks = pAutoSpinAttackTicks;
      if (!this.level.isClientSide) {
         this.setLivingEntityFlag(4, true);
      }

   }

   public boolean isAutoSpinAttack() {
      return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
   }

   /**
    * Dismounts this entity from the entity it is riding.
    */
   public void stopRiding() {
      Entity entity = this.getVehicle();
      super.stopRiding();
      if (entity != null && entity != this.getVehicle() && !this.level.isClientSide) {
         this.dismountVehicle(entity);
      }

   }

   /**
    * Handles updating while riding another entity
    */
   public void rideTick() {
      super.rideTick();
      this.oRun = this.run;
      this.run = 0.0F;
      this.fallDistance = 0.0F;
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pLerpSteps, boolean pTeleport) {
      this.lerpX = pX;
      this.lerpY = pY;
      this.lerpZ = pZ;
      this.lerpYRot = (double)pYRot;
      this.lerpXRot = (double)pXRot;
      this.lerpSteps = pLerpSteps;
   }

   @OnlyIn(Dist.CLIENT)
   public void lerpHeadTo(float pYaw, int pPitch) {
      this.lyHeadRot = (double)pYaw;
      this.lerpHeadSteps = pPitch;
   }

   public void setJumping(boolean pJumping) {
      this.jumping = pJumping;
   }

   public void onItemPickup(ItemEntity pItemEntity) {
      PlayerEntity playerentity = pItemEntity.getThrower() != null ? this.level.getPlayerByUUID(pItemEntity.getThrower()) : null;
      if (playerentity instanceof ServerPlayerEntity) {
         CriteriaTriggers.ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayerEntity)playerentity, pItemEntity.getItem(), this);
      }

   }

   /**
    * Called when the entity picks up an item.
    */
   public void take(Entity pEntity, int pAmount) {
      if (!pEntity.removed && !this.level.isClientSide && (pEntity instanceof ItemEntity || pEntity instanceof AbstractArrowEntity || pEntity instanceof ExperienceOrbEntity)) {
         ((ServerWorld)this.level).getChunkSource().broadcast(pEntity, new SCollectItemPacket(pEntity.getId(), this.getId(), pAmount));
      }

   }

   public boolean canSee(Entity pEntity) {
      Vector3d vector3d = new Vector3d(this.getX(), this.getEyeY(), this.getZ());
      Vector3d vector3d1 = new Vector3d(pEntity.getX(), pEntity.getEyeY(), pEntity.getZ());
      if (pEntity.level != this.level || vector3d1.distanceToSqr(vector3d) > 128.0D * 128.0D) return false; //Forge Backport MC-209819
      return this.level.clip(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this)).getType() == RayTraceResult.Type.MISS;
   }

   /**
    * Returns the current Y rotation of the entity.
    */
   public float getViewYRot(float pPartialTick) {
      return pPartialTick == 1.0F ? this.yHeadRot : MathHelper.lerp(pPartialTick, this.yHeadRotO, this.yHeadRot);
   }

   /**
    * Gets the progression of the swing animation, ranges from 0.0 to 1.0.
    */
   @OnlyIn(Dist.CLIENT)
   public float getAttackAnim(float pPartialTick) {
      float f = this.attackAnim - this.oAttackAnim;
      if (f < 0.0F) {
         ++f;
      }

      return this.oAttackAnim + f * pPartialTick;
   }

   /**
    * Returns whether the entity is in a server world
    */
   public boolean isEffectiveAi() {
      return !this.level.isClientSide;
   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return !this.removed;
   }

   /**
    * Returns true if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return this.isAlive() && !this.isSpectator() && !this.onClimbable();
   }

   /**
    * Marks this entity's velocity as changed, so that it can be re-synced with the client later
    */
   protected void markHurt() {
      this.hurtMarked = this.random.nextDouble() >= this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
   }

   public float getYHeadRot() {
      return this.yHeadRot;
   }

   /**
    * Sets the head's Y rotation of the entity.
    */
   public void setYHeadRot(float pYHeadRot) {
      this.yHeadRot = pYHeadRot;
   }

   /**
    * Set the body Y rotation of the entity.
    */
   public void setYBodyRot(float pYBodyRot) {
      this.yBodyRot = pYBodyRot;
   }

   protected Vector3d getRelativePortalPosition(Direction.Axis pAxis, TeleportationRepositioner.Result pPortal) {
      return resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(pAxis, pPortal));
   }

   public static Vector3d resetForwardDirectionOfRelativePortalPosition(Vector3d pRelativePortalPosition) {
      return new Vector3d(pRelativePortalPosition.x, pRelativePortalPosition.y, 0.0D);
   }

   /**
    * Returns the amount of health added by the Absorption effect.
    */
   public float getAbsorptionAmount() {
      return this.absorptionAmount;
   }

   public void setAbsorptionAmount(float pAbsorptionAmount) {
      if (pAbsorptionAmount < 0.0F) {
         pAbsorptionAmount = 0.0F;
      }

      this.absorptionAmount = pAbsorptionAmount;
   }

   /**
    * Sends an ENTER_COMBAT packet to the client
    */
   public void onEnterCombat() {
   }

   /**
    * Sends an END_COMBAT packet to the client
    */
   public void onLeaveCombat() {
   }

   protected void updateEffectVisibility() {
      this.effectsDirty = true;
   }

   public abstract HandSide getMainArm();

   public boolean isUsingItem() {
      return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
   }

   public Hand getUsedItemHand() {
      return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
   }

   private void updatingUsingItem() {
      if (this.isUsingItem()) {
         ItemStack itemStack = this.getItemInHand(this.getUsedItemHand());
         if (net.minecraftforge.common.ForgeHooks.canContinueUsing(this.useItem, itemStack)) this.useItem = itemStack;
         if (itemStack == this.useItem) {

            if (!this.useItem.isEmpty()) {
              useItemRemaining = net.minecraftforge.event.ForgeEventFactory.onItemUseTick(this, useItem, useItemRemaining);
              if (useItemRemaining > 0)
                 useItem.onUsingTick(this, useItemRemaining);
            }

            this.useItem.onUseTick(this.level, this, this.getUseItemRemainingTicks());
            if (this.shouldTriggerItemUseEffects()) {
               this.triggerItemUseEffects(this.useItem, 5);
            }

            if (--this.useItemRemaining == 0 && !this.level.isClientSide && !this.useItem.useOnRelease()) {
               this.completeUsingItem();
            }
         } else {
            this.stopUsingItem();
         }
      }

   }

   private boolean shouldTriggerItemUseEffects() {
      int i = this.getUseItemRemainingTicks();
      Food food = this.useItem.getItem().getFoodProperties();
      boolean flag = food != null && food.isFastFood();
      flag = flag | i <= this.useItem.getUseDuration() - 7;
      return flag && i % 4 == 0;
   }

   private void updateSwimAmount() {
      this.swimAmountO = this.swimAmount;
      if (this.isVisuallySwimming()) {
         this.swimAmount = Math.min(1.0F, this.swimAmount + 0.09F);
      } else {
         this.swimAmount = Math.max(0.0F, this.swimAmount - 0.09F);
      }

   }

   protected void setLivingEntityFlag(int pKey, boolean pValue) {
      int i = this.entityData.get(DATA_LIVING_ENTITY_FLAGS);
      if (pValue) {
         i = i | pKey;
      } else {
         i = i & ~pKey;
      }

      this.entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte)i);
   }

   public void startUsingItem(Hand pHand) {
      ItemStack itemstack = this.getItemInHand(pHand);
      if (!itemstack.isEmpty() && !this.isUsingItem()) {
         int duration = net.minecraftforge.event.ForgeEventFactory.onItemUseStart(this, itemstack, itemstack.getUseDuration());
         if (duration <= 0) return;
         this.useItem = itemstack;
         this.useItemRemaining = duration;
         if (!this.level.isClientSide) {
            this.setLivingEntityFlag(1, true);
            this.setLivingEntityFlag(2, pHand == Hand.OFF_HAND);
         }

      }
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (SLEEPING_POS_ID.equals(pKey)) {
         if (this.level.isClientSide) {
            this.getSleepingPos().ifPresent(this::setPosToBed);
         }
      } else if (DATA_LIVING_ENTITY_FLAGS.equals(pKey) && this.level.isClientSide) {
         if (this.isUsingItem() && this.useItem.isEmpty()) {
            this.useItem = this.getItemInHand(this.getUsedItemHand());
            if (!this.useItem.isEmpty()) {
               this.useItemRemaining = this.useItem.getUseDuration();
            }
         } else if (!this.isUsingItem() && !this.useItem.isEmpty()) {
            this.useItem = ItemStack.EMPTY;
            this.useItemRemaining = 0;
         }
      }

   }

   public void lookAt(EntityAnchorArgument.Type pAnchor, Vector3d pTarget) {
      super.lookAt(pAnchor, pTarget);
      this.yHeadRotO = this.yHeadRot;
      this.yBodyRot = this.yHeadRot;
      this.yBodyRotO = this.yBodyRot;
   }

   protected void triggerItemUseEffects(ItemStack pStack, int pAmount) {
      if (!pStack.isEmpty() && this.isUsingItem()) {
         if (pStack.getUseAnimation() == UseAction.DRINK) {
            this.playSound(this.getDrinkingSound(pStack), 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
         }

         if (pStack.getUseAnimation() == UseAction.EAT) {
            this.spawnItemParticles(pStack, pAmount);
            this.playSound(this.getEatingSound(pStack), 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         }

      }
   }

   private void spawnItemParticles(ItemStack pStack, int pAmount) {
      for(int i = 0; i < pAmount; ++i) {
         Vector3d vector3d = new Vector3d(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
         vector3d = vector3d.xRot(-this.xRot * ((float)Math.PI / 180F));
         vector3d = vector3d.yRot(-this.yRot * ((float)Math.PI / 180F));
         double d0 = (double)(-this.random.nextFloat()) * 0.6D - 0.3D;
         Vector3d vector3d1 = new Vector3d(((double)this.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
         vector3d1 = vector3d1.xRot(-this.xRot * ((float)Math.PI / 180F));
         vector3d1 = vector3d1.yRot(-this.yRot * ((float)Math.PI / 180F));
         vector3d1 = vector3d1.add(this.getX(), this.getEyeY(), this.getZ());
         if (this.level instanceof ServerWorld) //Forge: Fix MC-2518 spawnParticle is nooped on server, need to use server specific variant
             ((ServerWorld)this.level).sendParticles(new ItemParticleData(ParticleTypes.ITEM, pStack), vector3d1.x, vector3d1.y, vector3d1.z, 1, vector3d.x, vector3d.y + 0.05D, vector3d.z, 0.0D);
         else
         this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, pStack), vector3d1.x, vector3d1.y, vector3d1.z, vector3d.x, vector3d.y + 0.05D, vector3d.z);
      }

   }

   /**
    * Used for when item use count runs out, ie: eating completed
    */
   protected void completeUsingItem() {
      Hand hand = this.getUsedItemHand();
      if (!this.useItem.equals(this.getItemInHand(hand))) {
         this.releaseUsingItem();
      } else {
         if (!this.useItem.isEmpty() && this.isUsingItem()) {
            this.triggerItemUseEffects(this.useItem, 16);
            ItemStack copy = this.useItem.copy();
            ItemStack itemstack = net.minecraftforge.event.ForgeEventFactory.onItemUseFinish(this, copy, getUseItemRemainingTicks(), this.useItem.finishUsingItem(this.level, this));
            if (itemstack != this.useItem) {
               this.setItemInHand(hand, itemstack);
            }

            this.stopUsingItem();
         }

      }
   }

   public ItemStack getUseItem() {
      return this.useItem;
   }

   public int getUseItemRemainingTicks() {
      return this.useItemRemaining;
   }

   public int getTicksUsingItem() {
      return this.isUsingItem() ? this.useItem.getUseDuration() - this.getUseItemRemainingTicks() : 0;
   }

   public void releaseUsingItem() {
      if (!this.useItem.isEmpty()) {
         if (!net.minecraftforge.event.ForgeEventFactory.onUseItemStop(this, useItem, this.getUseItemRemainingTicks())) {
            ItemStack copy = this instanceof PlayerEntity ? useItem.copy() : null;
         this.useItem.releaseUsing(this.level, this, this.getUseItemRemainingTicks());
           if (copy != null && useItem.isEmpty()) net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem((PlayerEntity)this, copy, getUsedItemHand());
         }
         if (this.useItem.useOnRelease()) {
            this.updatingUsingItem();
         }
      }

      this.stopUsingItem();
   }

   public void stopUsingItem() {
      if (!this.level.isClientSide) {
         this.setLivingEntityFlag(1, false);
      }

      this.useItem = ItemStack.EMPTY;
      this.useItemRemaining = 0;
   }

   public boolean isBlocking() {
      if (this.isUsingItem() && !this.useItem.isEmpty()) {
         Item item = this.useItem.getItem();
         if (item.getUseAnimation(this.useItem) != UseAction.BLOCK) {
            return false;
         } else {
            return item.getUseDuration(this.useItem) - this.useItemRemaining >= 5;
         }
      } else {
         return false;
      }
   }

   public boolean isSuppressingSlidingDownLadder() {
      return this.isShiftKeyDown();
   }

   public boolean isFallFlying() {
      return this.getSharedFlag(7);
   }

   public boolean isVisuallySwimming() {
      return super.isVisuallySwimming() || !this.isFallFlying() && this.getPose() == Pose.FALL_FLYING;
   }

   @OnlyIn(Dist.CLIENT)
   public int getFallFlyingTicks() {
      return this.fallFlyTicks;
   }

   public boolean randomTeleport(double pX, double pY, double pZ, boolean pBroadcastTeleport) {
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      double d3 = pY;
      boolean flag = false;
      BlockPos blockpos = new BlockPos(pX, pY, pZ);
      World world = this.level;
      if (world.hasChunkAt(blockpos)) {
         boolean flag1 = false;

         while(!flag1 && blockpos.getY() > 0) {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = world.getBlockState(blockpos1);
            if (blockstate.getMaterial().blocksMotion()) {
               flag1 = true;
            } else {
               --d3;
               blockpos = blockpos1;
            }
         }

         if (flag1) {
            this.teleportTo(pX, d3, pZ);
            if (world.noCollision(this) && !world.containsAnyLiquid(this.getBoundingBox())) {
               flag = true;
            }
         }
      }

      if (!flag) {
         this.teleportTo(d0, d1, d2);
         return false;
      } else {
         if (pBroadcastTeleport) {
            world.broadcastEntityEvent(this, (byte)46);
         }

         if (this instanceof CreatureEntity) {
            ((CreatureEntity)this).getNavigation().stop();
         }

         return true;
      }
   }

   /**
    * Returns false if the entity is an armor stand. Returns true for all other entity living bases.
    */
   public boolean isAffectedByPotions() {
      return true;
   }

   public boolean attackable() {
      return true;
   }

   /**
    * Called when a record starts or stops playing. Used to make parrots start or stop partying.
    */
   @OnlyIn(Dist.CLIENT)
   public void setRecordPlayingNearby(BlockPos pPos, boolean pIsPartying) {
   }

   public boolean canTakeItem(ItemStack pItemstack) {
      return false;
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnMobPacket(this);
   }

   public EntitySize getDimensions(Pose pPose) {
      return pPose == Pose.SLEEPING ? SLEEPING_DIMENSIONS : super.getDimensions(pPose).scale(this.getScale());
   }

   public ImmutableList<Pose> getDismountPoses() {
      return ImmutableList.of(Pose.STANDING);
   }

   public AxisAlignedBB getLocalBoundsForPose(Pose pPose) {
      EntitySize entitysize = this.getDimensions(pPose);
      return new AxisAlignedBB((double)(-entitysize.width / 2.0F), 0.0D, (double)(-entitysize.width / 2.0F), (double)(entitysize.width / 2.0F), (double)entitysize.height, (double)(entitysize.width / 2.0F));
   }

   public Optional<BlockPos> getSleepingPos() {
      return this.entityData.get(SLEEPING_POS_ID);
   }

   public void setSleepingPos(BlockPos pPos) {
      this.entityData.set(SLEEPING_POS_ID, Optional.of(pPos));
   }

   public void clearSleepingPos() {
      this.entityData.set(SLEEPING_POS_ID, Optional.empty());
   }

   /**
    * Returns whether player is sleeping or not
    */
   public boolean isSleeping() {
      return this.getSleepingPos().isPresent();
   }

   public void startSleeping(BlockPos pPos) {
      if (this.isPassenger()) {
         this.stopRiding();
      }

      BlockState blockstate = this.level.getBlockState(pPos);
      if (blockstate.isBed(level, pPos, this)) {
         blockstate.setBedOccupied(level, pPos, this, true);
      }

      this.setPose(Pose.SLEEPING);
      this.setPosToBed(pPos);
      this.setSleepingPos(pPos);
      this.setDeltaMovement(Vector3d.ZERO);
      this.hasImpulse = true;
   }

   /**
    * Sets entity position to a supplied BlockPos plus a little offset
    */
   private void setPosToBed(BlockPos p_213370_1_) {
      this.setPos((double)p_213370_1_.getX() + 0.5D, (double)p_213370_1_.getY() + 0.6875D, (double)p_213370_1_.getZ() + 0.5D);
   }

   private boolean checkBedExists() {
      return this.getSleepingPos().map((p_241350_1_) -> {
         return net.minecraftforge.event.ForgeEventFactory.fireSleepingLocationCheck(this, p_241350_1_);
      }).orElse(false);
   }

   public void stopSleeping() {
      this.getSleepingPos().filter(this.level::hasChunkAt).ifPresent((p_241348_1_) -> {
         BlockState blockstate = this.level.getBlockState(p_241348_1_);
         if (blockstate.isBed(level, p_241348_1_, this)) {
            blockstate.setBedOccupied(level, p_241348_1_, this, false);
            Vector3d vector3d1 = BedBlock.findStandUpPosition(this.getType(), this.level, p_241348_1_, this.yRot).orElseGet(() -> {
               BlockPos blockpos = p_241348_1_.above();
               return new Vector3d((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.1D, (double)blockpos.getZ() + 0.5D);
            });
            Vector3d vector3d2 = Vector3d.atBottomCenterOf(p_241348_1_).subtract(vector3d1).normalize();
            float f = (float)MathHelper.wrapDegrees(MathHelper.atan2(vector3d2.z, vector3d2.x) * (double)(180F / (float)Math.PI) - 90.0D);
            this.setPos(vector3d1.x, vector3d1.y, vector3d1.z);
            this.yRot = f;
            this.xRot = 0.0F;
         }

      });
      Vector3d vector3d = this.position();
      this.setPose(Pose.STANDING);
      this.setPos(vector3d.x, vector3d.y, vector3d.z);
      this.clearSleepingPos();
   }

   /**
    * gets the Direction for the camera if this entity is sleeping
    */
   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Direction getBedOrientation() {
      BlockPos blockpos = this.getSleepingPos().orElse((BlockPos)null);
      if (blockpos == null) return Direction.UP;
      BlockState state = this.level.getBlockState(blockpos);
      return !state.isBed(level, blockpos, this) ? Direction.UP : state.getBedDirection(level, blockpos);
   }

   /**
    * Checks if this entity is inside of an opaque block
    */
   public boolean isInWall() {
      return !this.isSleeping() && super.isInWall();
   }

   protected final float getEyeHeight(Pose pPose, EntitySize pSize) {
      return pPose == Pose.SLEEPING ? 0.2F : this.getStandingEyeHeight(pPose, pSize);
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return super.getEyeHeight(pPose, pSize);
   }

   public ItemStack getProjectile(ItemStack pShootable) {
      return ItemStack.EMPTY;
   }

   public ItemStack eat(World pLevel, ItemStack pFood) {
      if (pFood.isEdible()) {
         pLevel.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(pFood), SoundCategory.NEUTRAL, 1.0F, 1.0F + (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.4F);
         this.addEatEffect(pFood, pLevel, this);
         if (!(this instanceof PlayerEntity) || !((PlayerEntity)this).abilities.instabuild) {
            pFood.shrink(1);
         }
      }

      return pFood;
   }

   private void addEatEffect(ItemStack pFood, World pLevel, LivingEntity pLivingEntity) {
      Item item = pFood.getItem();
      if (item.isEdible()) {
         for(Pair<EffectInstance, Float> pair : item.getFoodProperties().getEffects()) {
            if (!pLevel.isClientSide && pair.getFirst() != null && pLevel.random.nextFloat() < pair.getSecond()) {
               pLivingEntity.addEffect(new EffectInstance(pair.getFirst()));
            }
         }
      }

   }

   private static byte entityEventForEquipmentBreak(EquipmentSlotType pSlot) {
      switch(pSlot) {
      case MAINHAND:
         return 47;
      case OFFHAND:
         return 48;
      case HEAD:
         return 49;
      case CHEST:
         return 50;
      case FEET:
         return 52;
      case LEGS:
         return 51;
      default:
         return 47;
      }
   }

   public void broadcastBreakEvent(EquipmentSlotType pSlot) {
      this.level.broadcastEntityEvent(this, entityEventForEquipmentBreak(pSlot));
   }

   public void broadcastBreakEvent(Hand pHand) {
      this.broadcastBreakEvent(pHand == Hand.MAIN_HAND ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND);
   }

   /* ==== FORGE START ==== */
   /***
    * Removes all potion effects that have curativeItem as a curative item for its effect
    * @param curativeItem The itemstack we are using to cure potion effects
    */
   public boolean curePotionEffects(ItemStack curativeItem) {
      if (this.level.isClientSide)
         return false;
      boolean ret = false;
      Iterator<EffectInstance> itr = this.activeEffects.values().iterator();
      while (itr.hasNext()) {
         EffectInstance effect = itr.next();
         if (effect.isCurativeItem(curativeItem) && !net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent(this, effect))) {
            this.onEffectRemoved(effect);
            itr.remove();
            ret = true;
            this.effectsDirty = true;
         }
      }
      return ret;
   }

   /**
    * Returns true if the entity's rider (EntityPlayer) should face forward when mounted.
    * currently only used in vanilla code by pigs.
    *
    * @param player The player who is riding the entity.
    * @return If the player should orient the same direction as this entity.
    */
   public boolean shouldRiderFaceForward(PlayerEntity player) {
      return this instanceof net.minecraft.entity.passive.PigEntity;
   }

   private final net.minecraftforge.common.util.LazyOptional<?>[] handlers = net.minecraftforge.items.wrapper.EntityEquipmentInvWrapper.create(this);

   @Override
   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable Direction facing) {
      if (this.isAlive() && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (facing == null) return handlers[2].cast();
         else if (facing.getAxis().isVertical()) return handlers[0].cast();
         else if (facing.getAxis().isHorizontal()) return handlers[1].cast();
      }
      return super.getCapability(capability, facing);
   }

   @Override
   protected void invalidateCaps() {
      super.invalidateCaps();
      for (int x = 0; x < handlers.length; x++)
         handlers[x].invalidate();
   }

   /**
    * Gets the bounding box of this Entity, adjusted to take auxiliary entities into account (e.g. the tile contained by
    * a minecart, such as a command block).
    */
   @OnlyIn(Dist.CLIENT)
   public AxisAlignedBB getBoundingBoxForCulling() {
      if (this.getItemBySlot(EquipmentSlotType.HEAD).getItem() == Items.DRAGON_HEAD) {
         float f = 0.5F;
         return this.getBoundingBox().inflate(0.5D, 0.5D, 0.5D);
      } else {
         return super.getBoundingBoxForCulling();
      }
   }
}
