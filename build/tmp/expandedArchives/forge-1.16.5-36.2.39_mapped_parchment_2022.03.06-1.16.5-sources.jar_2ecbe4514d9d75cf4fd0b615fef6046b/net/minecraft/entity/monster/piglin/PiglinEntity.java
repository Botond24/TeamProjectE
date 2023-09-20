package net.minecraft.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PiglinEntity extends AbstractPiglinEntity implements ICrossbowUser {
   private static final DataParameter<Boolean> DATA_BABY_ID = EntityDataManager.defineId(PiglinEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Boolean> DATA_IS_CHARGING_CROSSBOW = EntityDataManager.defineId(PiglinEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Boolean> DATA_IS_DANCING = EntityDataManager.defineId(PiglinEntity.class, DataSerializers.BOOLEAN);
   private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
   private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_UUID, "Baby speed boost", (double)0.2F, AttributeModifier.Operation.MULTIPLY_BASE);
   private final Inventory inventory = new Inventory(8);
   private boolean cannotHunt = false;
   protected static final ImmutableList<SensorType<? extends Sensor<? super PiglinEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
   protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.NEAREST_REPELLENT);

   public PiglinEntity(EntityType<? extends AbstractPiglinEntity> p_i231570_1_, World p_i231570_2_) {
      super(p_i231570_1_, p_i231570_2_);
      this.xpReward = 5;
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.isBaby()) {
         pCompound.putBoolean("IsBaby", true);
      }

      if (this.cannotHunt) {
         pCompound.putBoolean("CannotHunt", true);
      }

      pCompound.put("Inventory", this.inventory.createTag());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setBaby(pCompound.getBoolean("IsBaby"));
      this.setCannotHunt(pCompound.getBoolean("CannotHunt"));
      this.inventory.fromTag(pCompound.getList("Inventory", 10));
   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
      this.inventory.removeAllItems().forEach(this::spawnAtLocation);
   }

   protected ItemStack addToInventory(ItemStack pStack) {
      return this.inventory.addItem(pStack);
   }

   protected boolean canAddToInventory(ItemStack pStack) {
      return this.inventory.canAddItem(pStack);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BABY_ID, false);
      this.entityData.define(DATA_IS_CHARGING_CROSSBOW, false);
      this.entityData.define(DATA_IS_DANCING, false);
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_BABY_ID.equals(pKey)) {
         this.refreshDimensions();
      }

   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0D).add(Attributes.MOVEMENT_SPEED, (double)0.35F).add(Attributes.ATTACK_DAMAGE, 5.0D);
   }

   public static boolean checkPiglinSpawnRules(EntityType<PiglinEntity> pPiglin, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      return !pLevel.getBlockState(pPos.below()).is(Blocks.NETHER_WART_BLOCK);
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      if (pReason != SpawnReason.STRUCTURE) {
         if (pLevel.getRandom().nextFloat() < 0.2F) {
            this.setBaby(true);
         } else if (this.isAdult()) {
            this.setItemSlot(EquipmentSlotType.MAINHAND, this.createSpawnWeapon());
         }
      }

      PiglinTasks.initMemories(this);
      this.populateDefaultEquipmentSlots(pDifficulty);
      this.populateDefaultEquipmentEnchantments(pDifficulty);
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   protected boolean shouldDespawnInPeaceful() {
      return false;
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return !this.isPersistenceRequired();
   }

   /**
    * Gives armor or weapon for entity based on given DifficultyInstance
    */
   protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
      if (this.isAdult()) {
         this.maybeWearArmor(EquipmentSlotType.HEAD, new ItemStack(Items.GOLDEN_HELMET));
         this.maybeWearArmor(EquipmentSlotType.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
         this.maybeWearArmor(EquipmentSlotType.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
         this.maybeWearArmor(EquipmentSlotType.FEET, new ItemStack(Items.GOLDEN_BOOTS));
      }

   }

   private void maybeWearArmor(EquipmentSlotType pSlot, ItemStack pStack) {
      if (this.level.random.nextFloat() < 0.1F) {
         this.setItemSlot(pSlot, pStack);
      }

   }

   protected Brain.BrainCodec<PiglinEntity> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      return PiglinTasks.makeBrain(this, this.brainProvider().makeBrain(pDynamic));
   }

   public Brain<PiglinEntity> getBrain() {
      return (Brain<PiglinEntity>)super.getBrain();
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ActionResultType actionresulttype = super.mobInteract(pPlayer, pHand);
      if (actionresulttype.consumesAction()) {
         return actionresulttype;
      } else if (!this.level.isClientSide) {
         return PiglinTasks.mobInteract(this, pPlayer, pHand);
      } else {
         boolean flag = PiglinTasks.canAdmire(this, pPlayer.getItemInHand(pHand)) && this.getArmPose() != PiglinAction.ADMIRING_ITEM;
         return flag ? ActionResultType.SUCCESS : ActionResultType.PASS;
      }
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return this.isBaby() ? 0.93F : 1.74F;
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return (double)this.getBbHeight() * 0.92D;
   }

   /**
    * Set whether this zombie is a child.
    */
   public void setBaby(boolean pChildZombie) {
      this.getEntityData().set(DATA_BABY_ID, pChildZombie);
      if (!this.level.isClientSide) {
         ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
         modifiableattributeinstance.removeModifier(SPEED_MODIFIER_BABY);
         if (pChildZombie) {
            modifiableattributeinstance.addTransientModifier(SPEED_MODIFIER_BABY);
         }
      }

   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return this.getEntityData().get(DATA_BABY_ID);
   }

   private void setCannotHunt(boolean pCannotHunt) {
      this.cannotHunt = pCannotHunt;
   }

   protected boolean canHunt() {
      return !this.cannotHunt;
   }

   protected void customServerAiStep() {
      this.level.getProfiler().push("piglinBrain");
      this.getBrain().tick((ServerWorld)this.level, this);
      this.level.getProfiler().pop();
      PiglinTasks.updateActivity(this);
      super.customServerAiStep();
   }

   /**
    * Get the experience points the entity currently has.
    */
   protected int getExperienceReward(PlayerEntity pPlayer) {
      return this.xpReward;
   }

   protected void finishConversion(ServerWorld pServerLevel) {
      PiglinTasks.cancelAdmiring(this);
      this.inventory.removeAllItems().forEach(this::spawnAtLocation);
      super.finishConversion(pServerLevel);
   }

   private ItemStack createSpawnWeapon() {
      return (double)this.random.nextFloat() < 0.5D ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD);
   }

   private boolean isChargingCrossbow() {
      return this.entityData.get(DATA_IS_CHARGING_CROSSBOW);
   }

   public void setChargingCrossbow(boolean pIsCharging) {
      this.entityData.set(DATA_IS_CHARGING_CROSSBOW, pIsCharging);
   }

   public void onCrossbowAttackPerformed() {
      this.noActionTime = 0;
   }

   public PiglinAction getArmPose() {
      if (this.isDancing()) {
         return PiglinAction.DANCING;
      } else if (PiglinTasks.isLovedItem(this.getOffhandItem().getItem())) {
         return PiglinAction.ADMIRING_ITEM;
      } else if (this.isAggressive() && this.isHoldingMeleeWeapon()) {
         return PiglinAction.ATTACKING_WITH_MELEE_WEAPON;
      } else if (this.isChargingCrossbow()) {
         return PiglinAction.CROSSBOW_CHARGE;
      } else {
         return this.isAggressive() && this.isHolding(item -> item instanceof net.minecraft.item.CrossbowItem) ? PiglinAction.CROSSBOW_HOLD : PiglinAction.DEFAULT;
      }
   }

   public boolean isDancing() {
      return this.entityData.get(DATA_IS_DANCING);
   }

   public void setDancing(boolean pDancing) {
      this.entityData.set(DATA_IS_DANCING, pDancing);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      boolean flag = super.hurt(pSource, pAmount);
      if (this.level.isClientSide) {
         return false;
      } else {
         if (flag && pSource.getEntity() instanceof LivingEntity) {
            PiglinTasks.wasHurtBy(this, (LivingEntity)pSource.getEntity());
         }

         return flag;
      }
   }

   /**
    * Attack the specified entity using a ranged attack.
    */
   public void performRangedAttack(LivingEntity pTarget, float pVelocity) {
      this.performCrossbowAttack(this, 1.6F);
   }

   public void shootCrossbowProjectile(LivingEntity pTarget, ItemStack pCrossbowStack, ProjectileEntity pProjectile, float pProjectileAngle) {
      this.shootCrossbowProjectile(this, pTarget, pProjectile, pProjectileAngle, 1.6F);
   }

   public boolean canFireProjectileWeapon(ShootableItem pProjectileWeapon) {
      return pProjectileWeapon == Items.CROSSBOW;
   }

   protected void holdInMainHand(ItemStack pStack) {
      this.setItemSlotAndDropWhenKilled(EquipmentSlotType.MAINHAND, pStack);
   }

   protected void holdInOffHand(ItemStack pStack) {
      if (pStack.isPiglinCurrency()) {
         this.setItemSlot(EquipmentSlotType.OFFHAND, pStack);
         this.setGuaranteedDrop(EquipmentSlotType.OFFHAND);
      } else {
         this.setItemSlotAndDropWhenKilled(EquipmentSlotType.OFFHAND, pStack);
      }

   }

   public boolean wantsToPickUp(ItemStack pStack) {
      return net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this) && this.canPickUpLoot() && PiglinTasks.wantsToPickup(this, pStack);
   }

   protected boolean canReplaceCurrentItem(ItemStack pCandidate) {
      EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(pCandidate);
      ItemStack itemstack = this.getItemBySlot(equipmentslottype);
      return this.canReplaceCurrentItem(pCandidate, itemstack);
   }

   protected boolean canReplaceCurrentItem(ItemStack pCandidate, ItemStack pExisting) {
      if (EnchantmentHelper.hasBindingCurse(pExisting)) {
         return false;
      } else {
         boolean flag = PiglinTasks.isLovedItem(pCandidate.getItem()) || pCandidate.getItem() == Items.CROSSBOW;
         boolean flag1 = PiglinTasks.isLovedItem(pExisting.getItem()) || pExisting.getItem() == Items.CROSSBOW;
         if (flag && !flag1) {
            return true;
         } else if (!flag && flag1) {
            return false;
         } else {
            return this.isAdult() && pCandidate.getItem() != Items.CROSSBOW && pExisting.getItem() == Items.CROSSBOW ? false : super.canReplaceCurrentItem(pCandidate, pExisting);
         }
      }
   }

   /**
    * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
    * better.
    */
   protected void pickUpItem(ItemEntity pItemEntity) {
      this.onItemPickup(pItemEntity);
      PiglinTasks.pickUpItem(this, pItemEntity);
   }

   public boolean startRiding(Entity pEntity, boolean pForce) {
      if (this.isBaby() && pEntity.getType() == EntityType.HOGLIN) {
         pEntity = this.getTopPassenger(pEntity, 3);
      }

      return super.startRiding(pEntity, pForce);
   }

   private Entity getTopPassenger(Entity pVehicle, int pMaxPosition) {
      List<Entity> list = pVehicle.getPassengers();
      return pMaxPosition != 1 && !list.isEmpty() ? this.getTopPassenger(list.get(0), pMaxPosition - 1) : pVehicle;
   }

   protected SoundEvent getAmbientSound() {
      return this.level.isClientSide ? null : PiglinTasks.getSoundForCurrentActivity(this).orElse((SoundEvent)null);
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.PIGLIN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PIGLIN_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.PIGLIN_STEP, 0.15F, 1.0F);
   }

   protected void playSound(SoundEvent pSound) {
      this.playSound(pSound, this.getSoundVolume(), this.getVoicePitch());
   }

   protected void playConvertedSound() {
      this.playSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
   }
}
