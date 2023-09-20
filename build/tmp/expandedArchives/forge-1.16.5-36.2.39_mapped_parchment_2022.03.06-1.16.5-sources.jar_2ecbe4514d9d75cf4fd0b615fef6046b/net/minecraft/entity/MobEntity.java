package net.minecraft.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.BodyController;
import net.minecraft.entity.ai.controller.JumpController;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.LeashKnotEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SMountEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class MobEntity extends LivingEntity {
   private static final DataParameter<Byte> DATA_MOB_FLAGS_ID = EntityDataManager.defineId(MobEntity.class, DataSerializers.BYTE);
   public int ambientSoundTime;
   protected int xpReward;
   protected LookController lookControl;
   protected MovementController moveControl;
   protected JumpController jumpControl;
   private final BodyController bodyRotationControl;
   protected PathNavigator navigation;
   public final GoalSelector goalSelector;
   public final GoalSelector targetSelector;
   private LivingEntity target;
   private final EntitySenses sensing;
   private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
   protected final float[] handDropChances = new float[2];
   private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
   protected final float[] armorDropChances = new float[4];
   private boolean canPickUpLoot;
   private boolean persistenceRequired;
   private final Map<PathNodeType, Float> pathfindingMalus = Maps.newEnumMap(PathNodeType.class);
   private ResourceLocation lootTable;
   private long lootTableSeed;
   @Nullable
   private Entity leashHolder;
   private int delayedLeashHolderId;
   @Nullable
   private CompoundNBT leashInfoTag;
   private BlockPos restrictCenter = BlockPos.ZERO;
   private float restrictRadius = -1.0F;

   protected MobEntity(EntityType<? extends MobEntity> p_i48576_1_, World p_i48576_2_) {
      super(p_i48576_1_, p_i48576_2_);
      this.goalSelector = new GoalSelector(p_i48576_2_.getProfilerSupplier());
      this.targetSelector = new GoalSelector(p_i48576_2_.getProfilerSupplier());
      this.lookControl = new LookController(this);
      this.moveControl = new MovementController(this);
      this.jumpControl = new JumpController(this);
      this.bodyRotationControl = this.createBodyControl();
      this.navigation = this.createNavigation(p_i48576_2_);
      this.sensing = new EntitySenses(this);
      Arrays.fill(this.armorDropChances, 0.085F);
      Arrays.fill(this.handDropChances, 0.085F);
      if (p_i48576_2_ != null && !p_i48576_2_.isClientSide) {
         this.registerGoals();
      }

   }

   protected void registerGoals() {
   }

   public static AttributeModifierMap.MutableAttribute createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.ATTACK_KNOCKBACK);
   }

   /**
    * Returns new PathNavigateGround instance
    */
   protected PathNavigator createNavigation(World pLevel) {
      return new GroundPathNavigator(this, pLevel);
   }

   protected boolean shouldPassengersInheritMalus() {
      return false;
   }

   public float getPathfindingMalus(PathNodeType pNodeType) {
      MobEntity mobentity;
      if (this.getVehicle() instanceof MobEntity && ((MobEntity)this.getVehicle()).shouldPassengersInheritMalus()) {
         mobentity = (MobEntity)this.getVehicle();
      } else {
         mobentity = this;
      }

      Float f = mobentity.pathfindingMalus.get(pNodeType);
      return f == null ? pNodeType.getMalus() : f;
   }

   public void setPathfindingMalus(PathNodeType pNodeType, float pMalus) {
      this.pathfindingMalus.put(pNodeType, pMalus);
   }

   public boolean canCutCorner(PathNodeType pPathType) {
      return pPathType != PathNodeType.DANGER_FIRE && pPathType != PathNodeType.DANGER_CACTUS && pPathType != PathNodeType.DANGER_OTHER && pPathType != PathNodeType.WALKABLE_DOOR;
   }

   protected BodyController createBodyControl() {
      return new BodyController(this);
   }

   public LookController getLookControl() {
      return this.lookControl;
   }

   public MovementController getMoveControl() {
      if (this.isPassenger() && this.getVehicle() instanceof MobEntity) {
         MobEntity mobentity = (MobEntity)this.getVehicle();
         return mobentity.getMoveControl();
      } else {
         return this.moveControl;
      }
   }

   public JumpController getJumpControl() {
      return this.jumpControl;
   }

   public PathNavigator getNavigation() {
      if (this.isPassenger() && this.getVehicle() instanceof MobEntity) {
         MobEntity mobentity = (MobEntity)this.getVehicle();
         return mobentity.getNavigation();
      } else {
         return this.navigation;
      }
   }

   public EntitySenses getSensing() {
      return this.sensing;
   }

   /**
    * Gets the active target the Task system uses for tracking
    */
   @Nullable
   public LivingEntity getTarget() {
      return this.target;
   }

   /**
    * Sets the active target the Task system uses for tracking
    */
   public void setTarget(@Nullable LivingEntity pLivingEntity) {
      this.target = pLivingEntity;
      net.minecraftforge.common.ForgeHooks.onLivingSetAttackTarget(this, pLivingEntity);
   }

   public boolean canAttackType(EntityType<?> pType) {
      return pType != EntityType.GHAST;
   }

   public boolean canFireProjectileWeapon(ShootableItem pProjectileWeapon) {
      return false;
   }

   /**
    * Applies the benefits of growing back wool and faster growing up to the acting entity. This function is used in the
    * {@code EatBlockGoal}.
    */
   public void ate() {
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_MOB_FLAGS_ID, (byte)0);
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 80;
   }

   /**
    * Plays living's sound at its position
    */
   public void playAmbientSound() {
      SoundEvent soundevent = this.getAmbientSound();
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   /**
    * Gets called every tick from main Entity class
    */
   public void baseTick() {
      super.baseTick();
      this.level.getProfiler().push("mobBaseTick");
      if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
         this.resetAmbientSoundTime();
         this.playAmbientSound();
      }

      this.level.getProfiler().pop();
   }

   protected void playHurtSound(DamageSource pSource) {
      this.resetAmbientSoundTime();
      super.playHurtSound(pSource);
   }

   private void resetAmbientSoundTime() {
      this.ambientSoundTime = -this.getAmbientSoundInterval();
   }

   /**
    * Get the experience points the entity currently has.
    */
   protected int getExperienceReward(PlayerEntity pPlayer) {
      if (this.xpReward > 0) {
         int i = this.xpReward;

         for(int j = 0; j < this.armorItems.size(); ++j) {
            if (!this.armorItems.get(j).isEmpty() && this.armorDropChances[j] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         for(int k = 0; k < this.handItems.size(); ++k) {
            if (!this.handItems.get(k).isEmpty() && this.handDropChances[k] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         return i;
      } else {
         return this.xpReward;
      }
   }

   /**
    * Spawns an explosion particle around the Entity's location
    */
   public void spawnAnim() {
      if (this.level.isClientSide) {
         for(int i = 0; i < 20; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            double d3 = 10.0D;
            this.level.addParticle(ParticleTypes.POOF, this.getX(1.0D) - d0 * 10.0D, this.getRandomY() - d1 * 10.0D, this.getRandomZ(1.0D) - d2 * 10.0D, d0, d1, d2);
         }
      } else {
         this.level.broadcastEntityEvent(this, (byte)20);
      }

   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 20) {
         this.spawnAnim();
      } else {
         super.handleEntityEvent(pId);
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (!this.level.isClientSide) {
         this.tickLeash();
         if (this.tickCount % 5 == 0) {
            this.updateControlFlags();
         }
      }

   }

   /**
    * Sets MOVE, JUMP, LOOK Goal.Flags depending if entity is riding or been controlled
    */
   protected void updateControlFlags() {
      boolean flag = !(this.getControllingPassenger() instanceof MobEntity);
      boolean flag1 = !(this.getVehicle() instanceof BoatEntity);
      this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
      this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
      this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
   }

   protected float tickHeadTurn(float pYRot, float pAnimStep) {
      this.bodyRotationControl.clientTick();
      return pAnimStep;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("CanPickUpLoot", this.canPickUpLoot());
      pCompound.putBoolean("PersistenceRequired", this.persistenceRequired);
      ListNBT listnbt = new ListNBT();

      for(ItemStack itemstack : this.armorItems) {
         CompoundNBT compoundnbt = new CompoundNBT();
         if (!itemstack.isEmpty()) {
            itemstack.save(compoundnbt);
         }

         listnbt.add(compoundnbt);
      }

      pCompound.put("ArmorItems", listnbt);
      ListNBT listnbt1 = new ListNBT();

      for(ItemStack itemstack1 : this.handItems) {
         CompoundNBT compoundnbt1 = new CompoundNBT();
         if (!itemstack1.isEmpty()) {
            itemstack1.save(compoundnbt1);
         }

         listnbt1.add(compoundnbt1);
      }

      pCompound.put("HandItems", listnbt1);
      ListNBT listnbt2 = new ListNBT();

      for(float f : this.armorDropChances) {
         listnbt2.add(FloatNBT.valueOf(f));
      }

      pCompound.put("ArmorDropChances", listnbt2);
      ListNBT listnbt3 = new ListNBT();

      for(float f1 : this.handDropChances) {
         listnbt3.add(FloatNBT.valueOf(f1));
      }

      pCompound.put("HandDropChances", listnbt3);
      if (this.leashHolder != null) {
         CompoundNBT compoundnbt2 = new CompoundNBT();
         if (this.leashHolder instanceof LivingEntity) {
            UUID uuid = this.leashHolder.getUUID();
            compoundnbt2.putUUID("UUID", uuid);
         } else if (this.leashHolder instanceof HangingEntity) {
            BlockPos blockpos = ((HangingEntity)this.leashHolder).getPos();
            compoundnbt2.putInt("X", blockpos.getX());
            compoundnbt2.putInt("Y", blockpos.getY());
            compoundnbt2.putInt("Z", blockpos.getZ());
         }

         pCompound.put("Leash", compoundnbt2);
      } else if (this.leashInfoTag != null) {
         pCompound.put("Leash", this.leashInfoTag.copy());
      }

      pCompound.putBoolean("LeftHanded", this.isLeftHanded());
      if (this.lootTable != null) {
         pCompound.putString("DeathLootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            pCompound.putLong("DeathLootTableSeed", this.lootTableSeed);
         }
      }

      if (this.isNoAi()) {
         pCompound.putBoolean("NoAI", this.isNoAi());
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("CanPickUpLoot", 1)) {
         this.setCanPickUpLoot(pCompound.getBoolean("CanPickUpLoot"));
      }

      this.persistenceRequired = pCompound.getBoolean("PersistenceRequired");
      if (pCompound.contains("ArmorItems", 9)) {
         ListNBT listnbt = pCompound.getList("ArmorItems", 10);

         for(int i = 0; i < this.armorItems.size(); ++i) {
            this.armorItems.set(i, ItemStack.of(listnbt.getCompound(i)));
         }
      }

      if (pCompound.contains("HandItems", 9)) {
         ListNBT listnbt1 = pCompound.getList("HandItems", 10);

         for(int j = 0; j < this.handItems.size(); ++j) {
            this.handItems.set(j, ItemStack.of(listnbt1.getCompound(j)));
         }
      }

      if (pCompound.contains("ArmorDropChances", 9)) {
         ListNBT listnbt2 = pCompound.getList("ArmorDropChances", 5);

         for(int k = 0; k < listnbt2.size(); ++k) {
            this.armorDropChances[k] = listnbt2.getFloat(k);
         }
      }

      if (pCompound.contains("HandDropChances", 9)) {
         ListNBT listnbt3 = pCompound.getList("HandDropChances", 5);

         for(int l = 0; l < listnbt3.size(); ++l) {
            this.handDropChances[l] = listnbt3.getFloat(l);
         }
      }

      if (pCompound.contains("Leash", 10)) {
         this.leashInfoTag = pCompound.getCompound("Leash");
      }

      this.setLeftHanded(pCompound.getBoolean("LeftHanded"));
      if (pCompound.contains("DeathLootTable", 8)) {
         this.lootTable = new ResourceLocation(pCompound.getString("DeathLootTable"));
         this.lootTableSeed = pCompound.getLong("DeathLootTableSeed");
      }

      this.setNoAi(pCompound.getBoolean("NoAI"));
   }

   protected void dropFromLootTable(DamageSource pDamageSource, boolean pAttackedRecently) {
      super.dropFromLootTable(pDamageSource, pAttackedRecently);
      this.lootTable = null;
   }

   protected LootContext.Builder createLootContext(boolean pAttackedRecently, DamageSource pDamageSource) {
      return super.createLootContext(pAttackedRecently, pDamageSource).withOptionalRandomSeed(this.lootTableSeed, this.random);
   }

   public final ResourceLocation getLootTable() {
      return this.lootTable == null ? this.getDefaultLootTable() : this.lootTable;
   }

   protected ResourceLocation getDefaultLootTable() {
      return super.getLootTable();
   }

   public void setZza(float pAmount) {
      this.zza = pAmount;
   }

   public void setYya(float pAmount) {
      this.yya = pAmount;
   }

   public void setXxa(float pAmount) {
      this.xxa = pAmount;
   }

   /**
    * set the movespeed used for the new AI system
    */
   public void setSpeed(float pSpeed) {
      super.setSpeed(pSpeed);
      this.setZza(pSpeed);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      this.level.getProfiler().push("looting");
      if (!this.level.isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
         for(ItemEntity itementity : this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D))) {
            if (!itementity.removed && !itementity.getItem().isEmpty() && !itementity.hasPickUpDelay() && this.wantsToPickUp(itementity.getItem())) {
               this.pickUpItem(itementity);
            }
         }
      }

      this.level.getProfiler().pop();
   }

   /**
    * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
    * better.
    */
   protected void pickUpItem(ItemEntity pItemEntity) {
      ItemStack itemstack = pItemEntity.getItem();
      if (this.equipItemIfPossible(itemstack)) {
         this.onItemPickup(pItemEntity);
         this.take(pItemEntity, itemstack.getCount());
         pItemEntity.remove();
      }

   }

   public boolean equipItemIfPossible(ItemStack pStack) {
      EquipmentSlotType equipmentslottype = getEquipmentSlotForItem(pStack);
      ItemStack itemstack = this.getItemBySlot(equipmentslottype);
      boolean flag = this.canReplaceCurrentItem(pStack, itemstack);
      if (flag && this.canHoldItem(pStack)) {
         double d0 = (double)this.getEquipmentDropChance(equipmentslottype);
         if (!itemstack.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d0) {
            this.spawnAtLocation(itemstack);
         }

         this.setItemSlotAndDropWhenKilled(equipmentslottype, pStack);
         this.playEquipSound(pStack);
         return true;
      } else {
         return false;
      }
   }

   protected void setItemSlotAndDropWhenKilled(EquipmentSlotType pSlot, ItemStack pStack) {
      this.setItemSlot(pSlot, pStack);
      this.setGuaranteedDrop(pSlot);
      this.persistenceRequired = true;
   }

   public void setGuaranteedDrop(EquipmentSlotType pSlot) {
      switch(pSlot.getType()) {
      case HAND:
         this.handDropChances[pSlot.getIndex()] = 2.0F;
         break;
      case ARMOR:
         this.armorDropChances[pSlot.getIndex()] = 2.0F;
      }

   }

   protected boolean canReplaceCurrentItem(ItemStack pCandidate, ItemStack pExisting) {
      if (pExisting.isEmpty()) {
         return true;
      } else if (pCandidate.getItem() instanceof SwordItem) {
         if (!(pExisting.getItem() instanceof SwordItem)) {
            return true;
         } else {
            SwordItem sworditem = (SwordItem)pCandidate.getItem();
            SwordItem sworditem1 = (SwordItem)pExisting.getItem();
            if (sworditem.getDamage() != sworditem1.getDamage()) {
               return sworditem.getDamage() > sworditem1.getDamage();
            } else {
               return this.canReplaceEqualItem(pCandidate, pExisting);
            }
         }
      } else if (pCandidate.getItem() instanceof BowItem && pExisting.getItem() instanceof BowItem) {
         return this.canReplaceEqualItem(pCandidate, pExisting);
      } else if (pCandidate.getItem() instanceof CrossbowItem && pExisting.getItem() instanceof CrossbowItem) {
         return this.canReplaceEqualItem(pCandidate, pExisting);
      } else if (pCandidate.getItem() instanceof ArmorItem) {
         if (EnchantmentHelper.hasBindingCurse(pExisting)) {
            return false;
         } else if (!(pExisting.getItem() instanceof ArmorItem)) {
            return true;
         } else {
            ArmorItem armoritem = (ArmorItem)pCandidate.getItem();
            ArmorItem armoritem1 = (ArmorItem)pExisting.getItem();
            if (armoritem.getDefense() != armoritem1.getDefense()) {
               return armoritem.getDefense() > armoritem1.getDefense();
            } else if (armoritem.getToughness() != armoritem1.getToughness()) {
               return armoritem.getToughness() > armoritem1.getToughness();
            } else {
               return this.canReplaceEqualItem(pCandidate, pExisting);
            }
         }
      } else {
         if (pCandidate.getItem() instanceof ToolItem) {
            if (pExisting.getItem() instanceof BlockItem) {
               return true;
            }

            if (pExisting.getItem() instanceof ToolItem) {
               ToolItem toolitem = (ToolItem)pCandidate.getItem();
               ToolItem toolitem1 = (ToolItem)pExisting.getItem();
               if (toolitem.getAttackDamage() != toolitem1.getAttackDamage()) {
                  return toolitem.getAttackDamage() > toolitem1.getAttackDamage();
               }

               return this.canReplaceEqualItem(pCandidate, pExisting);
            }
         }

         return false;
      }
   }

   public boolean canReplaceEqualItem(ItemStack pCandidate, ItemStack pExisting) {
      if (pCandidate.getDamageValue() >= pExisting.getDamageValue() && (!pCandidate.hasTag() || pExisting.hasTag())) {
         if (pCandidate.hasTag() && pExisting.hasTag()) {
            return pCandidate.getTag().getAllKeys().stream().anyMatch((p_233664_0_) -> {
               return !p_233664_0_.equals("Damage");
            }) && !pExisting.getTag().getAllKeys().stream().anyMatch((p_233662_0_) -> {
               return !p_233662_0_.equals("Damage");
            });
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean canHoldItem(ItemStack pStack) {
      return true;
   }

   public boolean wantsToPickUp(ItemStack pStack) {
      return this.canHoldItem(pStack);
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return true;
   }

   public boolean requiresCustomPersistence() {
      return this.isPassenger();
   }

   protected boolean shouldDespawnInPeaceful() {
      return false;
   }

   /**
    * Makes the entity despawn if requirements are reached
    */
   public void checkDespawn() {
      if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
         this.remove();
      } else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()) {
         Entity entity = this.level.getNearestPlayer(this, -1.0D);
         net.minecraftforge.eventbus.api.Event.Result result = net.minecraftforge.event.ForgeEventFactory.canEntityDespawn(this);
         if (result == net.minecraftforge.eventbus.api.Event.Result.DENY) {
            noActionTime = 0;
            entity = null;
         } else if (result == net.minecraftforge.eventbus.api.Event.Result.ALLOW) {
            this.remove();
            entity = null;
         }
         if (entity != null) {
            double d0 = entity.distanceToSqr(this);
            int i = this.getType().getCategory().getDespawnDistance();
            int j = i * i;
            if (d0 > (double)j && this.removeWhenFarAway(d0)) {
               this.remove();
            }

            int k = this.getType().getCategory().getNoDespawnDistance();
            int l = k * k;
            if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.removeWhenFarAway(d0)) {
               this.remove();
            } else if (d0 < (double)l) {
               this.noActionTime = 0;
            }
         }

      } else {
         this.noActionTime = 0;
      }
   }

   protected final void serverAiStep() {
      ++this.noActionTime;
      this.level.getProfiler().push("sensing");
      this.sensing.tick();
      this.level.getProfiler().pop();
      this.level.getProfiler().push("targetSelector");
      this.targetSelector.tick();
      this.level.getProfiler().pop();
      this.level.getProfiler().push("goalSelector");
      this.goalSelector.tick();
      this.level.getProfiler().pop();
      this.level.getProfiler().push("navigation");
      this.navigation.tick();
      this.level.getProfiler().pop();
      this.level.getProfiler().push("mob tick");
      this.customServerAiStep();
      this.level.getProfiler().pop();
      this.level.getProfiler().push("controls");
      this.level.getProfiler().push("move");
      this.moveControl.tick();
      this.level.getProfiler().popPush("look");
      this.lookControl.tick();
      this.level.getProfiler().popPush("jump");
      this.jumpControl.tick();
      this.level.getProfiler().pop();
      this.level.getProfiler().pop();
      this.sendDebugPackets();
   }

   protected void sendDebugPackets() {
      DebugPacketSender.sendGoalSelector(this.level, this, this.goalSelector);
   }

   protected void customServerAiStep() {
   }

   /**
    * The speed it takes to move the entityliving's head rotation through the faceEntity method.
    */
   public int getMaxHeadXRot() {
      return 40;
   }

   public int getMaxHeadYRot() {
      return 75;
   }

   public int getHeadRotSpeed() {
      return 10;
   }

   /**
    * Changes the X and Y rotation so that this entity is facing the given entity.
    */
   public void lookAt(Entity pEntity, float pMaxYRotIncrease, float pMaxXRotIncrease) {
      double d0 = pEntity.getX() - this.getX();
      double d2 = pEntity.getZ() - this.getZ();
      double d1;
      if (pEntity instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)pEntity;
         d1 = livingentity.getEyeY() - this.getEyeY();
      } else {
         d1 = (pEntity.getBoundingBox().minY + pEntity.getBoundingBox().maxY) / 2.0D - this.getEyeY();
      }

      double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
      float f = (float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
      float f1 = (float)(-(MathHelper.atan2(d1, d3) * (double)(180F / (float)Math.PI)));
      this.xRot = this.rotlerp(this.xRot, f1, pMaxXRotIncrease);
      this.yRot = this.rotlerp(this.yRot, f, pMaxYRotIncrease);
   }

   /**
    * Arguments: current rotation, intended rotation, max increment.
    */
   private float rotlerp(float pAngle, float pTargetAngle, float pMaxIncrease) {
      float f = MathHelper.wrapDegrees(pTargetAngle - pAngle);
      if (f > pMaxIncrease) {
         f = pMaxIncrease;
      }

      if (f < -pMaxIncrease) {
         f = -pMaxIncrease;
      }

      return pAngle + f;
   }

   /**
    * Returns true if entity is spawned from spawner or if entity can spawn on given BlockPos
    */
   public static boolean checkMobSpawnRules(EntityType<? extends MobEntity> pType, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      BlockPos blockpos = pPos.below();
      return pSpawnType == SpawnReason.SPAWNER || pLevel.getBlockState(blockpos).isValidSpawn(pLevel, blockpos, pType);
   }

   public boolean checkSpawnRules(IWorld pLevel, SpawnReason pReason) {
      return true;
   }

   public boolean checkSpawnObstruction(IWorldReader pLevel) {
      return !pLevel.containsAnyLiquid(this.getBoundingBox()) && pLevel.isUnobstructed(this);
   }

   /**
    * Will return how many at most can spawn in a chunk at once.
    */
   public int getMaxSpawnClusterSize() {
      return 4;
   }

   public boolean isMaxGroupSizeReached(int pSize) {
      return false;
   }

   /**
    * The maximum height from where the entity is alowed to jump (used in pathfinder)
    */
   public int getMaxFallDistance() {
      if (this.getTarget() == null) {
         return 3;
      } else {
         int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
         i = i - (3 - this.level.getDifficulty().getId()) * 4;
         if (i < 0) {
            i = 0;
         }

         return i + 3;
      }
   }

   public Iterable<ItemStack> getHandSlots() {
      return this.handItems;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return this.armorItems;
   }

   public ItemStack getItemBySlot(EquipmentSlotType pSlot) {
      switch(pSlot.getType()) {
      case HAND:
         return this.handItems.get(pSlot.getIndex());
      case ARMOR:
         return this.armorItems.get(pSlot.getIndex());
      default:
         return ItemStack.EMPTY;
      }
   }

   public void setItemSlot(EquipmentSlotType pSlot, ItemStack pStack) {
      switch(pSlot.getType()) {
      case HAND:
         this.handItems.set(pSlot.getIndex(), pStack);
         break;
      case ARMOR:
         this.armorItems.set(pSlot.getIndex(), pStack);
      }

   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);

      for(EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
         ItemStack itemstack = this.getItemBySlot(equipmentslottype);
         float f = this.getEquipmentDropChance(equipmentslottype);
         boolean flag = f > 1.0F;
         if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && (pRecentlyHit || flag) && Math.max(this.random.nextFloat() - (float)pLooting * 0.01F, 0.0F) < f) {
            if (!flag && itemstack.isDamageableItem()) {
               itemstack.setDamageValue(itemstack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
            }

            this.spawnAtLocation(itemstack);
            this.setItemSlot(equipmentslottype, ItemStack.EMPTY);
         }
      }

   }

   protected float getEquipmentDropChance(EquipmentSlotType pSlot) {
      float f;
      switch(pSlot.getType()) {
      case HAND:
         f = this.handDropChances[pSlot.getIndex()];
         break;
      case ARMOR:
         f = this.armorDropChances[pSlot.getIndex()];
         break;
      default:
         f = 0.0F;
      }

      return f;
   }

   /**
    * Gives armor or weapon for entity based on given DifficultyInstance
    */
   protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
      if (this.random.nextFloat() < 0.15F * pDifficulty.getSpecialMultiplier()) {
         int i = this.random.nextInt(2);
         float f = this.level.getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
         if (this.random.nextFloat() < 0.095F) {
            ++i;
         }

         if (this.random.nextFloat() < 0.095F) {
            ++i;
         }

         if (this.random.nextFloat() < 0.095F) {
            ++i;
         }

         boolean flag = true;

         for(EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
            if (equipmentslottype.getType() == EquipmentSlotType.Group.ARMOR) {
               ItemStack itemstack = this.getItemBySlot(equipmentslottype);
               if (!flag && this.random.nextFloat() < f) {
                  break;
               }

               flag = false;
               if (itemstack.isEmpty()) {
                  Item item = getEquipmentForSlot(equipmentslottype, i);
                  if (item != null) {
                     this.setItemSlot(equipmentslottype, new ItemStack(item));
                  }
               }
            }
         }
      }

   }

   public static EquipmentSlotType getEquipmentSlotForItem(ItemStack pStack) {
      final EquipmentSlotType slot = pStack.getEquipmentSlot();
      if (slot != null) return slot; // FORGE: Allow modders to set a non-default equipment slot for a stack; e.g. a non-armor chestplate-slot item
      Item item = pStack.getItem();
      if (item != Blocks.CARVED_PUMPKIN.asItem() && (!(item instanceof BlockItem) || !(((BlockItem)item).getBlock() instanceof AbstractSkullBlock))) {
         if (item instanceof ArmorItem) {
            return ((ArmorItem)item).getSlot();
         } else if (item == Items.ELYTRA) {
            return EquipmentSlotType.CHEST;
         } else {
            return pStack.isShield(null) ? EquipmentSlotType.OFFHAND : EquipmentSlotType.MAINHAND;
         }
      } else {
         return EquipmentSlotType.HEAD;
      }
   }

   @Nullable
   public static Item getEquipmentForSlot(EquipmentSlotType pSlot, int pChance) {
      switch(pSlot) {
      case HEAD:
         if (pChance == 0) {
            return Items.LEATHER_HELMET;
         } else if (pChance == 1) {
            return Items.GOLDEN_HELMET;
         } else if (pChance == 2) {
            return Items.CHAINMAIL_HELMET;
         } else if (pChance == 3) {
            return Items.IRON_HELMET;
         } else if (pChance == 4) {
            return Items.DIAMOND_HELMET;
         }
      case CHEST:
         if (pChance == 0) {
            return Items.LEATHER_CHESTPLATE;
         } else if (pChance == 1) {
            return Items.GOLDEN_CHESTPLATE;
         } else if (pChance == 2) {
            return Items.CHAINMAIL_CHESTPLATE;
         } else if (pChance == 3) {
            return Items.IRON_CHESTPLATE;
         } else if (pChance == 4) {
            return Items.DIAMOND_CHESTPLATE;
         }
      case LEGS:
         if (pChance == 0) {
            return Items.LEATHER_LEGGINGS;
         } else if (pChance == 1) {
            return Items.GOLDEN_LEGGINGS;
         } else if (pChance == 2) {
            return Items.CHAINMAIL_LEGGINGS;
         } else if (pChance == 3) {
            return Items.IRON_LEGGINGS;
         } else if (pChance == 4) {
            return Items.DIAMOND_LEGGINGS;
         }
      case FEET:
         if (pChance == 0) {
            return Items.LEATHER_BOOTS;
         } else if (pChance == 1) {
            return Items.GOLDEN_BOOTS;
         } else if (pChance == 2) {
            return Items.CHAINMAIL_BOOTS;
         } else if (pChance == 3) {
            return Items.IRON_BOOTS;
         } else if (pChance == 4) {
            return Items.DIAMOND_BOOTS;
         }
      default:
         return null;
      }
   }

   /**
    * Enchants Entity's current equipments based on given DifficultyInstance
    */
   protected void populateDefaultEquipmentEnchantments(DifficultyInstance pDifficulty) {
      float f = pDifficulty.getSpecialMultiplier();
      this.enchantSpawnedWeapon(f);

      for(EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
         if (equipmentslottype.getType() == EquipmentSlotType.Group.ARMOR) {
            this.enchantSpawnedArmor(f, equipmentslottype);
         }
      }

   }

   protected void enchantSpawnedWeapon(float pChanceMultiplier) {
      if (!this.getMainHandItem().isEmpty() && this.random.nextFloat() < 0.25F * pChanceMultiplier) {
         this.setItemSlot(EquipmentSlotType.MAINHAND, EnchantmentHelper.enchantItem(this.random, this.getMainHandItem(), (int)(5.0F + pChanceMultiplier * (float)this.random.nextInt(18)), false));
      }

   }

   protected void enchantSpawnedArmor(float pChanceMultiplier, EquipmentSlotType pSlot) {
      ItemStack itemstack = this.getItemBySlot(pSlot);
      if (!itemstack.isEmpty() && this.random.nextFloat() < 0.5F * pChanceMultiplier) {
         this.setItemSlot(pSlot, EnchantmentHelper.enchantItem(this.random, itemstack, (int)(5.0F + pChanceMultiplier * (float)this.random.nextInt(18)), false));
      }

   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05D, AttributeModifier.Operation.MULTIPLY_BASE));
      if (this.random.nextFloat() < 0.05F) {
         this.setLeftHanded(true);
      } else {
         this.setLeftHanded(false);
      }

      return pSpawnData;
   }

   /**
    * @return true if all the conditions for steering the entity are met. For pigs, this is true if it is being ridden
    * by a player and the player is holding a carrot-on-a-stick
    */
   public boolean canBeControlledByRider() {
      return false;
   }

   /**
    * Enable the Entity persistence
    */
   public void setPersistenceRequired() {
      this.persistenceRequired = true;
   }

   public void setDropChance(EquipmentSlotType pSlot, float pChance) {
      switch(pSlot.getType()) {
      case HAND:
         this.handDropChances[pSlot.getIndex()] = pChance;
         break;
      case ARMOR:
         this.armorDropChances[pSlot.getIndex()] = pChance;
      }

   }

   public boolean canPickUpLoot() {
      return this.canPickUpLoot;
   }

   public void setCanPickUpLoot(boolean pCanPickUpLoot) {
      this.canPickUpLoot = pCanPickUpLoot;
   }

   public boolean canTakeItem(ItemStack pItemstack) {
      EquipmentSlotType equipmentslottype = getEquipmentSlotForItem(pItemstack);
      return this.getItemBySlot(equipmentslottype).isEmpty() && this.canPickUpLoot();
   }

   /**
    * @return {@code true} if this entity may not naturally despawn.
    */
   public boolean isPersistenceRequired() {
      return this.persistenceRequired;
   }

   public final ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      if (!this.isAlive()) {
         return ActionResultType.PASS;
      } else if (this.getLeashHolder() == pPlayer) {
         this.dropLeash(true, !pPlayer.abilities.instabuild);
         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else {
         ActionResultType actionresulttype = this.checkAndHandleImportantInteractions(pPlayer, pHand);
         if (actionresulttype.consumesAction()) {
            return actionresulttype;
         } else {
            actionresulttype = this.mobInteract(pPlayer, pHand);
            return actionresulttype.consumesAction() ? actionresulttype : super.interact(pPlayer, pHand);
         }
      }
   }

   private ActionResultType checkAndHandleImportantInteractions(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getItem() == Items.LEAD && this.canBeLeashed(pPlayer)) {
         this.setLeashedTo(pPlayer, true);
         itemstack.shrink(1);
         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else {
         if (itemstack.getItem() == Items.NAME_TAG) {
            ActionResultType actionresulttype = itemstack.interactLivingEntity(pPlayer, this, pHand);
            if (actionresulttype.consumesAction()) {
               return actionresulttype;
            }
         }

         if (itemstack.getItem() instanceof SpawnEggItem) {
            if (this.level instanceof ServerWorld) {
               SpawnEggItem spawneggitem = (SpawnEggItem)itemstack.getItem();
               Optional<MobEntity> optional = spawneggitem.spawnOffspringFromSpawnEgg(pPlayer, this, (EntityType)this.getType(), (ServerWorld)this.level, this.position(), itemstack);
               optional.ifPresent((p_233658_2_) -> {
                  this.onOffspringSpawnedFromEgg(pPlayer, p_233658_2_);
               });
               return optional.isPresent() ? ActionResultType.SUCCESS : ActionResultType.PASS;
            } else {
               return ActionResultType.CONSUME;
            }
         } else {
            return ActionResultType.PASS;
         }
      }
   }

   protected void onOffspringSpawnedFromEgg(PlayerEntity pPlayer, MobEntity pChild) {
   }

   protected ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      return ActionResultType.PASS;
   }

   public boolean isWithinRestriction() {
      return this.isWithinRestriction(this.blockPosition());
   }

   public boolean isWithinRestriction(BlockPos pPos) {
      if (this.restrictRadius == -1.0F) {
         return true;
      } else {
         return this.restrictCenter.distSqr(pPos) < (double)(this.restrictRadius * this.restrictRadius);
      }
   }

   public void restrictTo(BlockPos pPos, int pDistance) {
      this.restrictCenter = pPos;
      this.restrictRadius = (float)pDistance;
   }

   public BlockPos getRestrictCenter() {
      return this.restrictCenter;
   }

   public float getRestrictRadius() {
      return this.restrictRadius;
   }

   public boolean hasRestriction() {
      return this.restrictRadius != -1.0F;
   }

   @Nullable
   public <T extends MobEntity> T convertTo(EntityType<T> pEntityType, boolean pTransferInventory) {
      if (this.removed) {
         return (T)null;
      } else {
         T t = pEntityType.create(this.level);
         t.copyPosition(this);
         t.setBaby(this.isBaby());
         t.setNoAi(this.isNoAi());
         if (this.hasCustomName()) {
            t.setCustomName(this.getCustomName());
            t.setCustomNameVisible(this.isCustomNameVisible());
         }

         if (this.isPersistenceRequired()) {
            t.setPersistenceRequired();
         }

         t.setInvulnerable(this.isInvulnerable());
         if (pTransferInventory) {
            t.setCanPickUpLoot(this.canPickUpLoot());

            for(EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
               ItemStack itemstack = this.getItemBySlot(equipmentslottype);
               if (!itemstack.isEmpty()) {
                  t.setItemSlot(equipmentslottype, itemstack.copy());
                  t.setDropChance(equipmentslottype, this.getEquipmentDropChance(equipmentslottype));
                  itemstack.setCount(0);
               }
            }
         }

         this.level.addFreshEntity(t);
         if (this.isPassenger()) {
            Entity entity = this.getVehicle();
            this.stopRiding();
            t.startRiding(entity, true);
         }

         this.remove();
         return t;
      }
   }

   /**
    * Applies logic related to leashes, for example dragging the entity or breaking the leash.
    */
   protected void tickLeash() {
      if (this.leashInfoTag != null) {
         this.restoreLeashFromSave();
      }

      if (this.leashHolder != null) {
         if (!this.isAlive() || !this.leashHolder.isAlive()) {
            this.dropLeash(true, true);
         }

      }
   }

   /**
    * Removes the leash from this entity
    */
   public void dropLeash(boolean pBroadcastPacket, boolean pDropLeash) {
      if (this.leashHolder != null) {
         this.forcedLoading = false;
         if (!(this.leashHolder instanceof PlayerEntity)) {
            this.leashHolder.forcedLoading = false;
         }

         this.leashHolder = null;
         this.leashInfoTag = null;
         if (!this.level.isClientSide && pDropLeash) {
            this.spawnAtLocation(Items.LEAD);
         }

         if (!this.level.isClientSide && pBroadcastPacket && this.level instanceof ServerWorld) {
            ((ServerWorld)this.level).getChunkSource().broadcast(this, new SMountEntityPacket(this, (Entity)null));
         }
      }

   }

   public boolean canBeLeashed(PlayerEntity pPlayer) {
      return !this.isLeashed() && !(this instanceof IMob);
   }

   public boolean isLeashed() {
      return this.leashHolder != null;
   }

   @Nullable
   public Entity getLeashHolder() {
      if (this.leashHolder == null && this.delayedLeashHolderId != 0 && this.level.isClientSide) {
         this.leashHolder = this.level.getEntity(this.delayedLeashHolderId);
      }

      return this.leashHolder;
   }

   /**
    * Sets the entity to be leashed to.
    */
   public void setLeashedTo(Entity pLeashHolder, boolean pBroadcastPacket) {
      this.leashHolder = pLeashHolder;
      this.leashInfoTag = null;
      this.forcedLoading = true;
      if (!(this.leashHolder instanceof PlayerEntity)) {
         this.leashHolder.forcedLoading = true;
      }

      if (!this.level.isClientSide && pBroadcastPacket && this.level instanceof ServerWorld) {
         ((ServerWorld)this.level).getChunkSource().broadcast(this, new SMountEntityPacket(this, this.leashHolder));
      }

      if (this.isPassenger()) {
         this.stopRiding();
      }

   }

   @OnlyIn(Dist.CLIENT)
   public void setDelayedLeashHolderId(int pLeashHolderID) {
      this.delayedLeashHolderId = pLeashHolderID;
      this.dropLeash(false, false);
   }

   public boolean startRiding(Entity pEntity, boolean pForce) {
      boolean flag = super.startRiding(pEntity, pForce);
      if (flag && this.isLeashed()) {
         this.dropLeash(true, true);
      }

      return flag;
   }

   private void restoreLeashFromSave() {
      if (this.leashInfoTag != null && this.level instanceof ServerWorld) {
         if (this.leashInfoTag.hasUUID("UUID")) {
            UUID uuid = this.leashInfoTag.getUUID("UUID");
            Entity entity = ((ServerWorld)this.level).getEntity(uuid);
            if (entity != null) {
               this.setLeashedTo(entity, true);
               return;
            }
         } else if (this.leashInfoTag.contains("X", 99) && this.leashInfoTag.contains("Y", 99) && this.leashInfoTag.contains("Z", 99)) {
            BlockPos blockpos = new BlockPos(this.leashInfoTag.getInt("X"), this.leashInfoTag.getInt("Y"), this.leashInfoTag.getInt("Z"));
            this.setLeashedTo(LeashKnotEntity.getOrCreateKnot(this.level, blockpos), true);
            return;
         }

         if (this.tickCount > 100) {
            this.spawnAtLocation(Items.LEAD);
            this.leashInfoTag = null;
         }
      }

   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      EquipmentSlotType equipmentslottype;
      if (pSlotIndex == 98) {
         equipmentslottype = EquipmentSlotType.MAINHAND;
      } else if (pSlotIndex == 99) {
         equipmentslottype = EquipmentSlotType.OFFHAND;
      } else if (pSlotIndex == 100 + EquipmentSlotType.HEAD.getIndex()) {
         equipmentslottype = EquipmentSlotType.HEAD;
      } else if (pSlotIndex == 100 + EquipmentSlotType.CHEST.getIndex()) {
         equipmentslottype = EquipmentSlotType.CHEST;
      } else if (pSlotIndex == 100 + EquipmentSlotType.LEGS.getIndex()) {
         equipmentslottype = EquipmentSlotType.LEGS;
      } else {
         if (pSlotIndex != 100 + EquipmentSlotType.FEET.getIndex()) {
            return false;
         }

         equipmentslottype = EquipmentSlotType.FEET;
      }

      if (!pStack.isEmpty() && !isValidSlotForItem(equipmentslottype, pStack) && equipmentslottype != EquipmentSlotType.HEAD) {
         return false;
      } else {
         this.setItemSlot(equipmentslottype, pStack);
         return true;
      }
   }

   public boolean isControlledByLocalInstance() {
      return this.canBeControlledByRider() && super.isControlledByLocalInstance();
   }

   public static boolean isValidSlotForItem(EquipmentSlotType pSlot, ItemStack pStack) {
      EquipmentSlotType equipmentslottype = getEquipmentSlotForItem(pStack);
      return equipmentslottype == pSlot || equipmentslottype == EquipmentSlotType.MAINHAND && pSlot == EquipmentSlotType.OFFHAND || equipmentslottype == EquipmentSlotType.OFFHAND && pSlot == EquipmentSlotType.MAINHAND;
   }

   /**
    * Returns whether the entity is in a server world
    */
   public boolean isEffectiveAi() {
      return super.isEffectiveAi() && !this.isNoAi();
   }

   /**
    * Set whether this Entity's AI is disabled
    */
   public void setNoAi(boolean pNoAi) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, pNoAi ? (byte)(b0 | 1) : (byte)(b0 & -2));
   }

   public void setLeftHanded(boolean pLeftHanded) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, pLeftHanded ? (byte)(b0 | 2) : (byte)(b0 & -3));
   }

   public void setAggressive(boolean pAggressive) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, pAggressive ? (byte)(b0 | 4) : (byte)(b0 & -5));
   }

   /**
    * Get whether this Entity's AI is disabled
    */
   public boolean isNoAi() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
   }

   public boolean isLeftHanded() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
   }

   public boolean isAggressive() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
   }

   /**
    * Set whether this zombie is a child.
    */
   public void setBaby(boolean pChildZombie) {
   }

   public HandSide getMainArm() {
      return this.isLeftHanded() ? HandSide.LEFT : HandSide.RIGHT;
   }

   public boolean canAttack(LivingEntity pTarget) {
      return pTarget.getType() == EntityType.PLAYER && ((PlayerEntity)pTarget).abilities.invulnerable ? false : super.canAttack(pTarget);
   }

   public boolean doHurtTarget(Entity pEntity) {
      float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      if (pEntity instanceof LivingEntity) {
         f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)pEntity).getMobType());
         f1 += (float)EnchantmentHelper.getKnockbackBonus(this);
      }

      int i = EnchantmentHelper.getFireAspect(this);
      if (i > 0) {
         pEntity.setSecondsOnFire(i * 4);
      }

      boolean flag = pEntity.hurt(DamageSource.mobAttack(this), f);
      if (flag) {
         if (f1 > 0.0F && pEntity instanceof LivingEntity) {
            ((LivingEntity)pEntity).knockback(f1 * 0.5F, (double)MathHelper.sin(this.yRot * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(this.yRot * ((float)Math.PI / 180F))));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
         }

         if (pEntity instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity)pEntity;
            this.maybeDisableShield(playerentity, this.getMainHandItem(), playerentity.isUsingItem() ? playerentity.getUseItem() : ItemStack.EMPTY);
         }

         this.doEnchantDamageEffects(this, pEntity);
         this.setLastHurtMob(pEntity);
      }

      return flag;
   }

   private void maybeDisableShield(PlayerEntity pPlayer, ItemStack pMobItemStack, ItemStack pPlayerItemStack) {
      if (!pMobItemStack.isEmpty() && !pPlayerItemStack.isEmpty() && pMobItemStack.getItem() instanceof AxeItem && pPlayerItemStack.getItem() == Items.SHIELD) {
         float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
         if (this.random.nextFloat() < f) {
            pPlayer.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.level.broadcastEntityEvent(pPlayer, (byte)30);
         }
      }

   }

   protected boolean isSunBurnTick() {
      if (this.level.isDay() && !this.level.isClientSide) {
         float f = this.getBrightness();
         BlockPos blockpos = this.getVehicle() instanceof BoatEntity ? (new BlockPos(this.getX(), (double)Math.round(this.getY()), this.getZ())).above() : new BlockPos(this.getX(), (double)Math.round(this.getY()), this.getZ());
         if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.level.canSeeSky(blockpos)) {
            return true;
         }
      }

      return false;
   }

   protected void jumpInLiquid(ITag<Fluid> pFluidTag) {
      if (this.getNavigation().canFloat()) {
         super.jumpInLiquid(pFluidTag);
      } else {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.3D, 0.0D));
      }

   }

   protected void removeAfterChangingDimensions() {
      super.removeAfterChangingDimensions();
      this.dropLeash(true, false);
   }
}
