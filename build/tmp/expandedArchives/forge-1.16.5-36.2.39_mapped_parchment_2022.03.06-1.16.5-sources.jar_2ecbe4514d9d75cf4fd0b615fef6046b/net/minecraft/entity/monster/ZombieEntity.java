package net.minecraft.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.BreakBlockGoal;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.GroundPathHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;

public class ZombieEntity extends MonsterEntity {
   private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
   private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);
   private static final DataParameter<Boolean> DATA_BABY_ID = EntityDataManager.defineId(ZombieEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Integer> DATA_SPECIAL_TYPE_ID = EntityDataManager.defineId(ZombieEntity.class, DataSerializers.INT);
   private static final DataParameter<Boolean> DATA_DROWNED_CONVERSION_ID = EntityDataManager.defineId(ZombieEntity.class, DataSerializers.BOOLEAN);
   private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = (p_213697_0_) -> {
      return p_213697_0_ == Difficulty.HARD;
   };
   private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE);
   private boolean canBreakDoors;
   private int inWaterTime;
   private int conversionTime;

   public ZombieEntity(EntityType<? extends ZombieEntity> p_i48549_1_, World p_i48549_2_) {
      super(p_i48549_1_, p_i48549_2_);
   }

   public ZombieEntity(World pLevel) {
      this(EntityType.ZOMBIE, pLevel);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(4, new ZombieEntity.AttackTurtleEggGoal(this, 1.0D, 3));
      this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
      this.addBehaviourGoals();
   }

   protected void addBehaviourGoals() {
      this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
      this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglinEntity.class));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_ON_LAND_SELECTOR));
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0D).add(Attributes.MOVEMENT_SPEED, (double)0.23F).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.ARMOR, 2.0D).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_BABY_ID, false);
      this.getEntityData().define(DATA_SPECIAL_TYPE_ID, 0);
      this.getEntityData().define(DATA_DROWNED_CONVERSION_ID, false);
   }

   public boolean isUnderWaterConverting() {
      return this.getEntityData().get(DATA_DROWNED_CONVERSION_ID);
   }

   public boolean canBreakDoors() {
      return this.canBreakDoors;
   }

   /**
    * Sets or removes EntityAIBreakDoor task
    */
   public void setCanBreakDoors(boolean pCanBreakDoors) {
      if (this.supportsBreakDoorGoal() && GroundPathHelper.hasGroundPathNavigation(this)) {
         if (this.canBreakDoors != pCanBreakDoors) {
            this.canBreakDoors = pCanBreakDoors;
            ((GroundPathNavigator)this.getNavigation()).setCanOpenDoors(pCanBreakDoors);
            if (pCanBreakDoors) {
               this.goalSelector.addGoal(1, this.breakDoorGoal);
            } else {
               this.goalSelector.removeGoal(this.breakDoorGoal);
            }
         }
      } else if (this.canBreakDoors) {
         this.goalSelector.removeGoal(this.breakDoorGoal);
         this.canBreakDoors = false;
      }

   }

   protected boolean supportsBreakDoorGoal() {
      return true;
   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return this.getEntityData().get(DATA_BABY_ID);
   }

   /**
    * Get the experience points the entity currently has.
    */
   protected int getExperienceReward(PlayerEntity pPlayer) {
      if (this.isBaby()) {
         this.xpReward = (int)((float)this.xpReward * 2.5F);
      }

      return super.getExperienceReward(pPlayer);
   }

   /**
    * Set whether this zombie is a child.
    */
   public void setBaby(boolean pChildZombie) {
      this.getEntityData().set(DATA_BABY_ID, pChildZombie);
      if (this.level != null && !this.level.isClientSide) {
         ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
         modifiableattributeinstance.removeModifier(SPEED_MODIFIER_BABY);
         if (pChildZombie) {
            modifiableattributeinstance.addTransientModifier(SPEED_MODIFIER_BABY);
         }
      }

   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (DATA_BABY_ID.equals(pKey)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(pKey);
   }

   protected boolean convertsInWater() {
      return true;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (!this.level.isClientSide && this.isAlive() && !this.isNoAi()) {
         if (this.isUnderWaterConverting()) {
            --this.conversionTime;

            if (this.conversionTime < 0 && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(this, EntityType.DROWNED, (timer) -> this.conversionTime = timer)) {
               this.doUnderWaterConversion();
            }
         } else if (this.convertsInWater()) {
            if (this.isEyeInFluid(FluidTags.WATER)) {
               ++this.inWaterTime;
               if (this.inWaterTime >= 600) {
                  this.startUnderWaterConversion(300);
               }
            } else {
               this.inWaterTime = -1;
            }
         }
      }

      super.tick();
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.isAlive()) {
         boolean flag = this.isSunSensitive() && this.isSunBurnTick();
         if (flag) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.HEAD);
            if (!itemstack.isEmpty()) {
               if (itemstack.isDamageableItem()) {
                  itemstack.setDamageValue(itemstack.getDamageValue() + this.random.nextInt(2));
                  if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                     this.broadcastBreakEvent(EquipmentSlotType.HEAD);
                     this.setItemSlot(EquipmentSlotType.HEAD, ItemStack.EMPTY);
                  }
               }

               flag = false;
            }

            if (flag) {
               this.setSecondsOnFire(8);
            }
         }
      }

      super.aiStep();
   }

   private void startUnderWaterConversion(int pConversionTime) {
      this.conversionTime = pConversionTime;
      this.getEntityData().set(DATA_DROWNED_CONVERSION_ID, true);
   }

   protected void doUnderWaterConversion() {
      this.convertToZombieType(EntityType.DROWNED);
      if (!this.isSilent()) {
         this.level.levelEvent((PlayerEntity)null, 1040, this.blockPosition(), 0);
      }

   }

   protected void convertToZombieType(EntityType<? extends ZombieEntity> pEntityType) {
      ZombieEntity zombieentity = this.convertTo(pEntityType, true);
      if (zombieentity != null) {
         zombieentity.handleAttributes(zombieentity.level.getCurrentDifficultyAt(zombieentity.blockPosition()).getSpecialMultiplier());
         zombieentity.setCanBreakDoors(zombieentity.supportsBreakDoorGoal() && this.canBreakDoors());
         net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, zombieentity);
      }

   }

   protected boolean isSunSensitive() {
      return true;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (!super.hurt(pSource, pAmount)) {
         return false;
      } else if (!(this.level instanceof ServerWorld)) {
         return false;
      } else {
         ServerWorld serverworld = (ServerWorld)this.level;
         LivingEntity livingentity = this.getTarget();
         if (livingentity == null && pSource.getEntity() instanceof LivingEntity) {
            livingentity = (LivingEntity)pSource.getEntity();
         }

            int i = MathHelper.floor(this.getX());
            int j = MathHelper.floor(this.getY());
            int k = MathHelper.floor(this.getZ());

         net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent event = net.minecraftforge.event.ForgeEventFactory.fireZombieSummonAid(this, level, i, j, k, livingentity, this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).getValue());
         if (event.getResult() == net.minecraftforge.eventbus.api.Event.Result.DENY) return true;
         if (event.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW  ||
            livingentity != null && this.level.getDifficulty() == Difficulty.HARD && (double)this.random.nextFloat() < this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).getValue() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            ZombieEntity zombieentity = event.getCustomSummonedAid() != null && event.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW ? event.getCustomSummonedAid() : EntityType.ZOMBIE.create(this.level);

            for(int l = 0; l < 50; ++l) {
               int i1 = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               int j1 = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               int k1 = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               BlockPos blockpos = new BlockPos(i1, j1, k1);
               EntityType<?> entitytype = zombieentity.getType();
               EntitySpawnPlacementRegistry.PlacementType entityspawnplacementregistry$placementtype = EntitySpawnPlacementRegistry.getPlacementType(entitytype);
               if (WorldEntitySpawner.isSpawnPositionOk(entityspawnplacementregistry$placementtype, this.level, blockpos, entitytype) && EntitySpawnPlacementRegistry.checkSpawnRules(entitytype, serverworld, SpawnReason.REINFORCEMENT, blockpos, this.level.random)) {
                  zombieentity.setPos((double)i1, (double)j1, (double)k1);
                  if (!this.level.hasNearbyAlivePlayer((double)i1, (double)j1, (double)k1, 7.0D) && this.level.isUnobstructed(zombieentity) && this.level.noCollision(zombieentity) && !this.level.containsAnyLiquid(zombieentity.getBoundingBox())) {
                     if (livingentity != null)
                     zombieentity.setTarget(livingentity);
                     zombieentity.finalizeSpawn(serverworld, this.level.getCurrentDifficultyAt(zombieentity.blockPosition()), SpawnReason.REINFORCEMENT, (ILivingEntityData)null, (CompoundNBT)null);
                     serverworld.addFreshEntityWithPassengers(zombieentity);
                     this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Zombie reinforcement caller charge", (double)-0.05F, AttributeModifier.Operation.ADDITION));
                     zombieentity.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Zombie reinforcement callee charge", (double)-0.05F, AttributeModifier.Operation.ADDITION));
                     break;
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean doHurtTarget(Entity pEntity) {
      boolean flag = super.doHurtTarget(pEntity);
      if (flag) {
         float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
         if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
            pEntity.setSecondsOnFire(2 * (int)f);
         }
      }

      return flag;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ZOMBIE_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ZOMBIE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ZOMBIE_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.UNDEAD;
   }

   /**
    * Gives armor or weapon for entity based on given DifficultyInstance
    */
   protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
      super.populateDefaultEquipmentSlots(pDifficulty);
      if (this.random.nextFloat() < (this.level.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
         int i = this.random.nextInt(3);
         if (i == 0) {
            this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_SWORD));
         } else {
            this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
         }
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("IsBaby", this.isBaby());
      pCompound.putBoolean("CanBreakDoors", this.canBreakDoors());
      pCompound.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
      pCompound.putInt("DrownedConversionTime", this.isUnderWaterConverting() ? this.conversionTime : -1);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setBaby(pCompound.getBoolean("IsBaby"));
      this.setCanBreakDoors(pCompound.getBoolean("CanBreakDoors"));
      this.inWaterTime = pCompound.getInt("InWaterTime");
      if (pCompound.contains("DrownedConversionTime", 99) && pCompound.getInt("DrownedConversionTime") > -1) {
         this.startUnderWaterConversion(pCompound.getInt("DrownedConversionTime"));
      }

   }

   public void killed(ServerWorld pLevel, LivingEntity pKilledEntity) {
      super.killed(pLevel, pKilledEntity);
      if ((pLevel.getDifficulty() == Difficulty.NORMAL || pLevel.getDifficulty() == Difficulty.HARD) && pKilledEntity instanceof VillagerEntity && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(pKilledEntity, EntityType.ZOMBIE_VILLAGER, (timer) -> {})) {
         if (pLevel.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
            return;
         }

         VillagerEntity villagerentity = (VillagerEntity)pKilledEntity;
         ZombieVillagerEntity zombievillagerentity = villagerentity.convertTo(EntityType.ZOMBIE_VILLAGER, false);
         zombievillagerentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(zombievillagerentity.blockPosition()), SpawnReason.CONVERSION, new ZombieEntity.GroupData(false, true), (CompoundNBT)null);
         zombievillagerentity.setVillagerData(villagerentity.getVillagerData());
         zombievillagerentity.setGossips(villagerentity.getGossips().store(NBTDynamicOps.INSTANCE).getValue());
         zombievillagerentity.setTradeOffers(villagerentity.getOffers().createTag());
         zombievillagerentity.setVillagerXp(villagerentity.getVillagerXp());
         net.minecraftforge.event.ForgeEventFactory.onLivingConvert(pKilledEntity, zombievillagerentity);
         if (!this.isSilent()) {
            pLevel.levelEvent((PlayerEntity)null, 1026, this.blockPosition(), 0);
         }
      }

   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return this.isBaby() ? 0.93F : 1.74F;
   }

   public boolean canHoldItem(ItemStack pStack) {
      return pStack.getItem() == Items.EGG && this.isBaby() && this.isPassenger() ? false : super.canHoldItem(pStack);
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      float f = pDifficulty.getSpecialMultiplier();
      this.setCanPickUpLoot(this.random.nextFloat() < 0.55F * f);
      if (pSpawnData == null) {
         pSpawnData = new ZombieEntity.GroupData(getSpawnAsBabyOdds(pLevel.getRandom()), true);
      }

      if (pSpawnData instanceof ZombieEntity.GroupData) {
         ZombieEntity.GroupData zombieentity$groupdata = (ZombieEntity.GroupData)pSpawnData;
         if (zombieentity$groupdata.isBaby) {
            this.setBaby(true);
            if (zombieentity$groupdata.canSpawnJockey) {
               if ((double)pLevel.getRandom().nextFloat() < 0.05D) {
                  List<ChickenEntity> list = pLevel.getEntitiesOfClass(ChickenEntity.class, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D), EntityPredicates.ENTITY_NOT_BEING_RIDDEN);
                  if (!list.isEmpty()) {
                     ChickenEntity chickenentity = list.get(0);
                     chickenentity.setChickenJockey(true);
                     this.startRiding(chickenentity);
                  }
               } else if ((double)pLevel.getRandom().nextFloat() < 0.05D) {
                  ChickenEntity chickenentity1 = EntityType.CHICKEN.create(this.level);
                  chickenentity1.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
                  chickenentity1.finalizeSpawn(pLevel, pDifficulty, SpawnReason.JOCKEY, (ILivingEntityData)null, (CompoundNBT)null);
                  chickenentity1.setChickenJockey(true);
                  this.startRiding(chickenentity1);
                  pLevel.addFreshEntity(chickenentity1);
               }
            }
         }

         this.setCanBreakDoors(this.supportsBreakDoorGoal() && this.random.nextFloat() < f * 0.1F);
         this.populateDefaultEquipmentSlots(pDifficulty);
         this.populateDefaultEquipmentEnchantments(pDifficulty);
      }

      if (this.getItemBySlot(EquipmentSlotType.HEAD).isEmpty()) {
         LocalDate localdate = LocalDate.now();
         int i = localdate.get(ChronoField.DAY_OF_MONTH);
         int j = localdate.get(ChronoField.MONTH_OF_YEAR);
         if (j == 10 && i == 31 && this.random.nextFloat() < 0.25F) {
            this.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
            this.armorDropChances[EquipmentSlotType.HEAD.getIndex()] = 0.0F;
         }
      }

      this.handleAttributes(f);
      return pSpawnData;
   }

   public static boolean getSpawnAsBabyOdds(Random pRandom) {
      return pRandom.nextFloat() < net.minecraftforge.common.ForgeConfig.SERVER.zombieBabyChance.get();
   }

   protected void handleAttributes(float pDifficulty) {
      this.randomizeReinforcementsChance();
      this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier("Random spawn bonus", this.random.nextDouble() * (double)0.05F, AttributeModifier.Operation.ADDITION));
      double d0 = this.random.nextDouble() * 1.5D * (double)pDifficulty;
      if (d0 > 1.0D) {
         this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random zombie-spawn bonus", d0, AttributeModifier.Operation.MULTIPLY_TOTAL));
      }

      if (this.random.nextFloat() < pDifficulty * 0.05F) {
         this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25D + 0.5D, AttributeModifier.Operation.ADDITION));
         this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0D + 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
         this.setCanBreakDoors(this.supportsBreakDoorGoal());
      }

   }

   protected void randomizeReinforcementsChance() {
      this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.random.nextDouble() * net.minecraftforge.common.ForgeConfig.SERVER.zombieBaseSummonChance.get());
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return this.isBaby() ? 0.0D : -0.45D;
   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
      Entity entity = pSource.getEntity();
      if (entity instanceof CreeperEntity) {
         CreeperEntity creeperentity = (CreeperEntity)entity;
         if (creeperentity.canDropMobsSkull()) {
            ItemStack itemstack = this.getSkull();
            if (!itemstack.isEmpty()) {
               creeperentity.increaseDroppedSkulls();
               this.spawnAtLocation(itemstack);
            }
         }
      }

   }

   protected ItemStack getSkull() {
      return new ItemStack(Items.ZOMBIE_HEAD);
   }

   class AttackTurtleEggGoal extends BreakBlockGoal {
      AttackTurtleEggGoal(CreatureEntity pMob, double pSpeedModifier, int pVerticalSearchRange) {
         super(Blocks.TURTLE_EGG, pMob, pSpeedModifier, pVerticalSearchRange);
      }

      public void playDestroyProgressSound(IWorld pLevel, BlockPos pPos) {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5F, 0.9F + ZombieEntity.this.random.nextFloat() * 0.2F);
      }

      public void playBreakSound(World pLevel, BlockPos pPos) {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + pLevel.random.nextFloat() * 0.2F);
      }

      public double acceptedDistance() {
         return 1.14D;
      }
   }

   public static class GroupData implements ILivingEntityData {
      public final boolean isBaby;
      public final boolean canSpawnJockey;

      public GroupData(boolean pIsBaby, boolean pCanSpawnJockey) {
         this.isBaby = pIsBaby;
         this.canSpawnJockey = pCanSpawnJockey;
      }
   }
}
