package net.minecraft.entity.merchant.villager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.sensor.GolemLastSeenSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.VillagerTasks;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.IReputationTracking;
import net.minecraft.entity.merchant.IReputationType;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.villager.IVillagerDataHolder;
import net.minecraft.entity.villager.VillagerType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.village.GossipManager;
import net.minecraft.village.GossipType;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VillagerEntity extends AbstractVillagerEntity implements IReputationTracking, IVillagerDataHolder {
   private static final DataParameter<VillagerData> DATA_VILLAGER_DATA = EntityDataManager.defineId(VillagerEntity.class, DataSerializers.VILLAGER_DATA);
   /** Mapping between valid food items and their respective efficiency values. */
   public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
   /** Defaults items a villager regardless of its profession can pick up. */
   private static final Set<Item> WANTED_ITEMS = ImmutableSet.of(Items.BREAD, Items.POTATO, Items.CARROT, Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT, Items.BEETROOT_SEEDS);
   private int updateMerchantTimer;
   private boolean increaseProfessionLevelOnUpdate;
   @Nullable
   private PlayerEntity lastTradedPlayer;
   private byte foodLevel;
   private final GossipManager gossips = new GossipManager();
   private long lastGossipTime;
   private long lastGossipDecayTime;
   private int villagerXp;
   private long lastRestockGameTime;
   private int numberOfRestocksToday;
   private long lastRestockCheckDayTime;
   private boolean assignProfessionWhenSpawned;
   private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY);
   private static final ImmutableList<SensorType<? extends Sensor<? super VillagerEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_DETECTED);
   public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<VillagerEntity, PointOfInterestType>> POI_MEMORIES = ImmutableMap.of(MemoryModuleType.HOME, (p_213769_0_, p_213769_1_) -> {
      return p_213769_1_ == PointOfInterestType.HOME;
   }, MemoryModuleType.JOB_SITE, (p_213771_0_, p_213771_1_) -> {
      return p_213771_0_.getVillagerData().getProfession().getJobPoiType() == p_213771_1_;
   }, MemoryModuleType.POTENTIAL_JOB_SITE, (p_213772_0_, p_213772_1_) -> {
      return PointOfInterestType.ALL_JOBS.test(p_213772_1_);
   }, MemoryModuleType.MEETING_POINT, (p_234546_0_, p_234546_1_) -> {
      return p_234546_1_ == PointOfInterestType.MEETING;
   });

   public VillagerEntity(EntityType<? extends VillagerEntity> p_i50182_1_, World p_i50182_2_) {
      this(p_i50182_1_, p_i50182_2_, VillagerType.PLAINS);
   }

   public VillagerEntity(EntityType<? extends VillagerEntity> pEntityType, World pLevel, VillagerType pVillagerType) {
      super(pEntityType, pLevel);
      ((GroundPathNavigator)this.getNavigation()).setCanOpenDoors(true);
      this.getNavigation().setCanFloat(true);
      this.setCanPickUpLoot(true);
      this.setVillagerData(this.getVillagerData().setType(pVillagerType).setProfession(VillagerProfession.NONE));
   }

   public Brain<VillagerEntity> getBrain() {
      return (Brain<VillagerEntity>)super.getBrain();
   }

   protected Brain.BrainCodec<VillagerEntity> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      Brain<VillagerEntity> brain = this.brainProvider().makeBrain(pDynamic);
      this.registerBrainGoals(brain);
      return brain;
   }

   public void refreshBrain(ServerWorld pServerLevel) {
      Brain<VillagerEntity> brain = this.getBrain();
      brain.stopAll(pServerLevel, this);
      this.brain = brain.copyWithoutBehaviors();
      this.registerBrainGoals(this.getBrain());
   }

   private void registerBrainGoals(Brain<VillagerEntity> pVillagerBrain) {
      VillagerProfession villagerprofession = this.getVillagerData().getProfession();
      if (this.isBaby()) {
         pVillagerBrain.setSchedule(Schedule.VILLAGER_BABY);
         pVillagerBrain.addActivity(Activity.PLAY, VillagerTasks.getPlayPackage(0.5F));
      } else {
         pVillagerBrain.setSchedule(Schedule.VILLAGER_DEFAULT);
         pVillagerBrain.addActivityWithConditions(Activity.WORK, VillagerTasks.getWorkPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_PRESENT)));
      }

      pVillagerBrain.addActivity(Activity.CORE, VillagerTasks.getCorePackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivityWithConditions(Activity.MEET, VillagerTasks.getMeetPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.VALUE_PRESENT)));
      pVillagerBrain.addActivity(Activity.REST, VillagerTasks.getRestPackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.IDLE, VillagerTasks.getIdlePackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.PANIC, VillagerTasks.getPanicPackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.PRE_RAID, VillagerTasks.getPreRaidPackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.RAID, VillagerTasks.getRaidPackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.HIDE, VillagerTasks.getHidePackage(villagerprofession, 0.5F));
      pVillagerBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      pVillagerBrain.setDefaultActivity(Activity.IDLE);
      pVillagerBrain.setActiveActivityIfPossible(Activity.IDLE);
      pVillagerBrain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());
   }

   protected void ageBoundaryReached() {
      super.ageBoundaryReached();
      if (this.level instanceof ServerWorld) {
         this.refreshBrain((ServerWorld)this.level);
      }

   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.FOLLOW_RANGE, 48.0D);
   }

   public boolean assignProfessionWhenSpawned() {
      return this.assignProfessionWhenSpawned;
   }

   protected void customServerAiStep() {
      this.level.getProfiler().push("villagerBrain");
      this.getBrain().tick((ServerWorld)this.level, this);
      this.level.getProfiler().pop();
      if (this.assignProfessionWhenSpawned) {
         this.assignProfessionWhenSpawned = false;
      }

      if (!this.isTrading() && this.updateMerchantTimer > 0) {
         --this.updateMerchantTimer;
         if (this.updateMerchantTimer <= 0) {
            if (this.increaseProfessionLevelOnUpdate) {
               this.increaseMerchantCareer();
               this.increaseProfessionLevelOnUpdate = false;
            }

            this.addEffect(new EffectInstance(Effects.REGENERATION, 200, 0));
         }
      }

      if (this.lastTradedPlayer != null && this.level instanceof ServerWorld) {
         ((ServerWorld)this.level).onReputationEvent(IReputationType.TRADE, this.lastTradedPlayer, this);
         this.level.broadcastEntityEvent(this, (byte)14);
         this.lastTradedPlayer = null;
      }

      if (!this.isNoAi() && this.random.nextInt(100) == 0) {
         Raid raid = ((ServerWorld)this.level).getRaidAt(this.blockPosition());
         if (raid != null && raid.isActive() && !raid.isOver()) {
            this.level.broadcastEntityEvent(this, (byte)42);
         }
      }

      if (this.getVillagerData().getProfession() == VillagerProfession.NONE && this.isTrading()) {
         this.stopTrading();
      }

      super.customServerAiStep();
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.getUnhappyCounter() > 0) {
         this.setUnhappyCounter(this.getUnhappyCounter() - 1);
      }

      this.maybeDecayGossip();
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.isTrading() && !this.isSleeping() && !pPlayer.isSecondaryUseActive()) {
         if (this.isBaby()) {
            this.setUnhappy();
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         } else {
            boolean flag = this.getOffers().isEmpty();
            if (pHand == Hand.MAIN_HAND) {
               if (flag && !this.level.isClientSide) {
                  this.setUnhappy();
               }

               pPlayer.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (flag) {
               return ActionResultType.sidedSuccess(this.level.isClientSide);
            } else {
               if (!this.level.isClientSide && !this.offers.isEmpty()) {
                  this.startTrading(pPlayer);
               }

               return ActionResultType.sidedSuccess(this.level.isClientSide);
            }
         }
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   private void setUnhappy() {
      this.setUnhappyCounter(40);
      if (!this.level.isClientSide()) {
         this.playSound(SoundEvents.VILLAGER_NO, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   private void startTrading(PlayerEntity pPlayer) {
      this.updateSpecialPrices(pPlayer);
      this.setTradingPlayer(pPlayer);
      this.openTradingScreen(pPlayer, this.getDisplayName(), this.getVillagerData().getLevel());
   }

   public void setTradingPlayer(@Nullable PlayerEntity pTradingPlayer) {
      boolean flag = this.getTradingPlayer() != null && pTradingPlayer == null;
      super.setTradingPlayer(pTradingPlayer);
      if (flag) {
         this.stopTrading();
      }

   }

   protected void stopTrading() {
      super.stopTrading();
      this.resetSpecialPrices();
   }

   private void resetSpecialPrices() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.resetSpecialPriceDiff();
      }

   }

   public boolean canRestock() {
      return true;
   }

   public void restock() {
      this.updateDemand();

      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.resetUses();
      }

      this.lastRestockGameTime = this.level.getGameTime();
      ++this.numberOfRestocksToday;
   }

   private boolean needsToRestock() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         if (merchantoffer.needsRestock()) {
            return true;
         }
      }

      return false;
   }

   private boolean allowedToRestock() {
      return this.numberOfRestocksToday == 0 || this.numberOfRestocksToday < 2 && this.level.getGameTime() > this.lastRestockGameTime + 2400L;
   }

   public boolean shouldRestock() {
      long i = this.lastRestockGameTime + 12000L;
      long j = this.level.getGameTime();
      boolean flag = j > i;
      long k = this.level.getDayTime();
      if (this.lastRestockCheckDayTime > 0L) {
         long l = this.lastRestockCheckDayTime / 24000L;
         long i1 = k / 24000L;
         flag |= i1 > l;
      }

      this.lastRestockCheckDayTime = k;
      if (flag) {
         this.lastRestockGameTime = j;
         this.resetNumberOfRestocks();
      }

      return this.allowedToRestock() && this.needsToRestock();
   }

   private void catchUpDemand() {
      int i = 2 - this.numberOfRestocksToday;
      if (i > 0) {
         for(MerchantOffer merchantoffer : this.getOffers()) {
            merchantoffer.resetUses();
         }
      }

      for(int j = 0; j < i; ++j) {
         this.updateDemand();
      }

   }

   private void updateDemand() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.updateDemand();
      }

   }

   private void updateSpecialPrices(PlayerEntity pPlayer) {
      int i = this.getPlayerReputation(pPlayer);
      if (i != 0) {
         for(MerchantOffer merchantoffer : this.getOffers()) {
            merchantoffer.addToSpecialPriceDiff(-MathHelper.floor((float)i * merchantoffer.getPriceMultiplier()));
         }
      }

      if (pPlayer.hasEffect(Effects.HERO_OF_THE_VILLAGE)) {
         EffectInstance effectinstance = pPlayer.getEffect(Effects.HERO_OF_THE_VILLAGE);
         int k = effectinstance.getAmplifier();

         for(MerchantOffer merchantoffer1 : this.getOffers()) {
            double d0 = 0.3D + 0.0625D * (double)k;
            int j = (int)Math.floor(d0 * (double)merchantoffer1.getBaseCostA().getCount());
            merchantoffer1.addToSpecialPriceDiff(-Math.max(j, 1));
         }
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      VillagerData.CODEC.encodeStart(NBTDynamicOps.INSTANCE, this.getVillagerData()).resultOrPartial(LOGGER::error).ifPresent((p_234547_1_) -> {
         pCompound.put("VillagerData", p_234547_1_);
      });
      pCompound.putByte("FoodLevel", this.foodLevel);
      pCompound.put("Gossips", this.gossips.store(NBTDynamicOps.INSTANCE).getValue());
      pCompound.putInt("Xp", this.villagerXp);
      pCompound.putLong("LastRestock", this.lastRestockGameTime);
      pCompound.putLong("LastGossipDecay", this.lastGossipDecayTime);
      pCompound.putInt("RestocksToday", this.numberOfRestocksToday);
      if (this.assignProfessionWhenSpawned) {
         pCompound.putBoolean("AssignProfessionWhenSpawned", true);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("VillagerData", 10)) {
         DataResult<VillagerData> dataresult = VillagerData.CODEC.parse(new Dynamic<>(NBTDynamicOps.INSTANCE, pCompound.get("VillagerData")));
         dataresult.resultOrPartial(LOGGER::error).ifPresent(this::setVillagerData);
      }

      if (pCompound.contains("Offers", 10)) {
         this.offers = new MerchantOffers(pCompound.getCompound("Offers"));
      }

      if (pCompound.contains("FoodLevel", 1)) {
         this.foodLevel = pCompound.getByte("FoodLevel");
      }

      ListNBT listnbt = pCompound.getList("Gossips", 10);
      this.gossips.update(new Dynamic<>(NBTDynamicOps.INSTANCE, listnbt));
      if (pCompound.contains("Xp", 3)) {
         this.villagerXp = pCompound.getInt("Xp");
      }

      this.lastRestockGameTime = pCompound.getLong("LastRestock");
      this.lastGossipDecayTime = pCompound.getLong("LastGossipDecay");
      this.setCanPickUpLoot(true);
      if (this.level instanceof ServerWorld) {
         this.refreshBrain((ServerWorld)this.level);
      }

      this.numberOfRestocksToday = pCompound.getInt("RestocksToday");
      if (pCompound.contains("AssignProfessionWhenSpawned")) {
         this.assignProfessionWhenSpawned = pCompound.getBoolean("AssignProfessionWhenSpawned");
      }

   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return false;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isSleeping()) {
         return null;
      } else {
         return this.isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.VILLAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.VILLAGER_DEATH;
   }

   public void playWorkSound() {
      SoundEvent soundevent = this.getVillagerData().getProfession().getWorkSound();
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public void setVillagerData(VillagerData p_213753_1_) {
      VillagerData villagerdata = this.getVillagerData();
      if (villagerdata.getProfession() != p_213753_1_.getProfession()) {
         this.offers = null;
      }

      this.entityData.set(DATA_VILLAGER_DATA, p_213753_1_);
   }

   public VillagerData getVillagerData() {
      return this.entityData.get(DATA_VILLAGER_DATA);
   }

   protected void rewardTradeXp(MerchantOffer pOffer) {
      int i = 3 + this.random.nextInt(4);
      this.villagerXp += pOffer.getXp();
      this.lastTradedPlayer = this.getTradingPlayer();
      if (this.shouldIncreaseLevel()) {
         this.updateMerchantTimer = 40;
         this.increaseProfessionLevelOnUpdate = true;
         i += 5;
      }

      if (pOffer.shouldRewardExp()) {
         this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));
      }

   }

   /**
    * Hint to AI tasks that we were attacked by the passed EntityLivingBase and should retaliate. Is not guaranteed to
    * change our actual active target (for example if we are currently busy attacking someone else)
    */
   public void setLastHurtByMob(@Nullable LivingEntity pLivingEntity) {
      if (pLivingEntity != null && this.level instanceof ServerWorld) {
         ((ServerWorld)this.level).onReputationEvent(IReputationType.VILLAGER_HURT, pLivingEntity, this);
         if (this.isAlive() && pLivingEntity instanceof PlayerEntity) {
            this.level.broadcastEntityEvent(this, (byte)13);
         }
      }

      super.setLastHurtByMob(pLivingEntity);
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      LOGGER.info("Villager {} died, message: '{}'", this, pCause.getLocalizedDeathMessage(this).getString());
      Entity entity = pCause.getEntity();
      if (entity != null) {
         this.tellWitnessesThatIWasMurdered(entity);
      }

      this.releaseAllPois();
      super.die(pCause);
   }

   private void releaseAllPois() {
      this.releasePoi(MemoryModuleType.HOME);
      this.releasePoi(MemoryModuleType.JOB_SITE);
      this.releasePoi(MemoryModuleType.POTENTIAL_JOB_SITE);
      this.releasePoi(MemoryModuleType.MEETING_POINT);
   }

   private void tellWitnessesThatIWasMurdered(Entity pMurderer) {
      if (this.level instanceof ServerWorld) {
         Optional<List<LivingEntity>> optional = this.brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
         if (optional.isPresent()) {
            ServerWorld serverworld = (ServerWorld)this.level;
            optional.get().stream().filter((p_223349_0_) -> {
               return p_223349_0_ instanceof IReputationTracking;
            }).forEach((p_223342_2_) -> {
               serverworld.onReputationEvent(IReputationType.VILLAGER_KILLED, pMurderer, (IReputationTracking)p_223342_2_);
            });
         }
      }
   }

   public void releasePoi(MemoryModuleType<GlobalPos> pModuleType) {
      if (this.level instanceof ServerWorld) {
         MinecraftServer minecraftserver = ((ServerWorld)this.level).getServer();
         this.brain.getMemory(pModuleType).ifPresent((p_213752_3_) -> {
            ServerWorld serverworld = minecraftserver.getLevel(p_213752_3_.dimension());
            if (serverworld != null) {
               PointOfInterestManager pointofinterestmanager = serverworld.getPoiManager();
               Optional<PointOfInterestType> optional = pointofinterestmanager.getType(p_213752_3_.pos());
               BiPredicate<VillagerEntity, PointOfInterestType> bipredicate = POI_MEMORIES.get(pModuleType);
               if (optional.isPresent() && bipredicate.test(this, optional.get())) {
                  pointofinterestmanager.release(p_213752_3_.pos());
                  DebugPacketSender.sendPoiTicketCountPacket(serverworld, p_213752_3_.pos());
               }

            }
         });
      }
   }

   public boolean canBreed() {
      return this.foodLevel + this.countFoodPointsInInventory() >= 12 && this.getAge() == 0;
   }

   private boolean hungry() {
      return this.foodLevel < 12;
   }

   private void eatUntilFull() {
      if (this.hungry() && this.countFoodPointsInInventory() != 0) {
         for(int i = 0; i < this.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = this.getInventory().getItem(i);
            if (!itemstack.isEmpty()) {
               Integer integer = FOOD_POINTS.get(itemstack.getItem());
               if (integer != null) {
                  int j = itemstack.getCount();

                  for(int k = j; k > 0; --k) {
                     this.foodLevel = (byte)(this.foodLevel + integer);
                     this.getInventory().removeItem(i, 1);
                     if (!this.hungry()) {
                        return;
                     }
                  }
               }
            }
         }

      }
   }

   public int getPlayerReputation(PlayerEntity pPlayer) {
      return this.gossips.getReputation(pPlayer.getUUID(), (p_223103_0_) -> {
         return true;
      });
   }

   private void digestFood(int pQty) {
      this.foodLevel = (byte)(this.foodLevel - pQty);
   }

   public void eatAndDigestFood() {
      this.eatUntilFull();
      this.digestFood(12);
   }

   public void setOffers(MerchantOffers pOffers) {
      this.offers = pOffers;
   }

   private boolean shouldIncreaseLevel() {
      int i = this.getVillagerData().getLevel();
      return VillagerData.canLevelUp(i) && this.villagerXp >= VillagerData.getMaxXpPerLevel(i);
   }

   private void increaseMerchantCareer() {
      this.setVillagerData(this.getVillagerData().setLevel(this.getVillagerData().getLevel() + 1));
      this.updateTrades();
   }

   protected ITextComponent getTypeName() {
      net.minecraft.util.ResourceLocation profName = this.getVillagerData().getProfession().getRegistryName();
      return new TranslationTextComponent(this.getType().getDescriptionId() + '.' + (!"minecraft".equals(profName.getNamespace()) ? profName.getNamespace() + '.' : "") + profName.getPath());
   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 12) {
         this.addParticlesAroundSelf(ParticleTypes.HEART);
      } else if (pId == 13) {
         this.addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);
      } else if (pId == 14) {
         this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
      } else if (pId == 42) {
         this.addParticlesAroundSelf(ParticleTypes.SPLASH);
      } else {
         super.handleEntityEvent(pId);
      }

   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      if (pReason == SpawnReason.BREEDING) {
         this.setVillagerData(this.getVillagerData().setProfession(VillagerProfession.NONE));
      }

      if (pReason == SpawnReason.COMMAND || pReason == SpawnReason.SPAWN_EGG || pReason == SpawnReason.SPAWNER || pReason == SpawnReason.DISPENSER) {
         this.setVillagerData(this.getVillagerData().setType(VillagerType.byBiome(pLevel.getBiomeName(this.blockPosition()))));
      }

      if (pReason == SpawnReason.STRUCTURE) {
         this.assignProfessionWhenSpawned = true;
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public VillagerEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      double d0 = this.random.nextDouble();
      VillagerType villagertype;
      if (d0 < 0.5D) {
         villagertype = VillagerType.byBiome(pServerLevel.getBiomeName(this.blockPosition()));
      } else if (d0 < 0.75D) {
         villagertype = this.getVillagerData().getType();
      } else {
         villagertype = ((VillagerEntity)pMate).getVillagerData().getType();
      }

      VillagerEntity villagerentity = new VillagerEntity(EntityType.VILLAGER, pServerLevel, villagertype);
      villagerentity.finalizeSpawn(pServerLevel, pServerLevel.getCurrentDifficultyAt(villagerentity.blockPosition()), SpawnReason.BREEDING, (ILivingEntityData)null, (CompoundNBT)null);
      return villagerentity;
   }

   public void thunderHit(ServerWorld pLevel, LightningBoltEntity pLightning) {
      if (pLevel.getDifficulty() != Difficulty.PEACEFUL && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(this, EntityType.WITCH, (timer) -> {})) {
         LOGGER.info("Villager {} was struck by lightning {}.", this, pLightning);
         WitchEntity witchentity = EntityType.WITCH.create(pLevel);
         witchentity.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
         witchentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(witchentity.blockPosition()), SpawnReason.CONVERSION, (ILivingEntityData)null, (CompoundNBT)null);
         witchentity.setNoAi(this.isNoAi());
         if (this.hasCustomName()) {
            witchentity.setCustomName(this.getCustomName());
            witchentity.setCustomNameVisible(this.isCustomNameVisible());
         }

         witchentity.setPersistenceRequired();
         net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, witchentity);
         pLevel.addFreshEntityWithPassengers(witchentity);
         this.releaseAllPois();
         this.remove();
      } else {
         super.thunderHit(pLevel, pLightning);
      }

   }

   /**
    * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
    * better.
    */
   protected void pickUpItem(ItemEntity pItemEntity) {
      ItemStack itemstack = pItemEntity.getItem();
      if (this.wantsToPickUp(itemstack)) {
         Inventory inventory = this.getInventory();
         boolean flag = inventory.canAddItem(itemstack);
         if (!flag) {
            return;
         }

         this.onItemPickup(pItemEntity);
         this.take(pItemEntity, itemstack.getCount());
         ItemStack itemstack1 = inventory.addItem(itemstack);
         if (itemstack1.isEmpty()) {
            pItemEntity.remove();
         } else {
            itemstack.setCount(itemstack1.getCount());
         }
      }

   }

   public boolean wantsToPickUp(ItemStack pStack) {
      Item item = pStack.getItem();
      return (WANTED_ITEMS.contains(item) || this.getVillagerData().getProfession().getRequestedItems().contains(item)) && this.getInventory().canAddItem(pStack);
   }

   /**
    * Used by {@link net.minecraft.entity.ai.EntityAIVillagerInteract EntityAIVillagerInteract} to check if the villager
    * can give some items from an inventory to another villager.
    */
   public boolean hasExcessFood() {
      return this.countFoodPointsInInventory() >= 24;
   }

   public boolean wantsMoreFood() {
      return this.countFoodPointsInInventory() < 12;
   }

   /**
    * @return calculated food value from item stacks in this villager's inventory
    */
   private int countFoodPointsInInventory() {
      Inventory inventory = this.getInventory();
      return FOOD_POINTS.entrySet().stream().mapToInt((p_226553_1_) -> {
         return inventory.countItem(p_226553_1_.getKey()) * p_226553_1_.getValue();
      }).sum();
   }

   /**
    * Returns true if villager has seeds, potatoes or carrots in inventory
    */
   public boolean hasFarmSeeds() {
      return this.getInventory().hasAnyOf(ImmutableSet.of(Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS));
   }

   protected void updateTrades() {
      VillagerData villagerdata = this.getVillagerData();
      Int2ObjectMap<VillagerTrades.ITrade[]> int2objectmap = VillagerTrades.TRADES.get(villagerdata.getProfession());
      if (int2objectmap != null && !int2objectmap.isEmpty()) {
         VillagerTrades.ITrade[] avillagertrades$itrade = int2objectmap.get(villagerdata.getLevel());
         if (avillagertrades$itrade != null) {
            MerchantOffers merchantoffers = this.getOffers();
            this.addOffersFromItemListings(merchantoffers, avillagertrades$itrade, 2);
         }
      }
   }

   public void gossip(ServerWorld pServerLevel, VillagerEntity pTarget, long pGameTime) {
      if ((pGameTime < this.lastGossipTime || pGameTime >= this.lastGossipTime + 1200L) && (pGameTime < pTarget.lastGossipTime || pGameTime >= pTarget.lastGossipTime + 1200L)) {
         this.gossips.transferFrom(pTarget.gossips, this.random, 10);
         this.lastGossipTime = pGameTime;
         pTarget.lastGossipTime = pGameTime;
         this.spawnGolemIfNeeded(pServerLevel, pGameTime, 5);
      }
   }

   private void maybeDecayGossip() {
      long i = this.level.getGameTime();
      if (this.lastGossipDecayTime == 0L) {
         this.lastGossipDecayTime = i;
      } else if (i >= this.lastGossipDecayTime + 24000L) {
         this.gossips.decay();
         this.lastGossipDecayTime = i;
      }
   }

   public void spawnGolemIfNeeded(ServerWorld pServerLevel, long pGameTime, int pMinVillagerAmount) {
      if (this.wantsToSpawnGolem(pGameTime)) {
         AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(10.0D, 10.0D, 10.0D);
         List<VillagerEntity> list = pServerLevel.getEntitiesOfClass(VillagerEntity.class, axisalignedbb);
         List<VillagerEntity> list1 = list.stream().filter((p_226554_2_) -> {
            return p_226554_2_.wantsToSpawnGolem(pGameTime);
         }).limit(5L).collect(Collectors.toList());
         if (list1.size() >= pMinVillagerAmount) {
            IronGolemEntity irongolementity = this.trySpawnGolem(pServerLevel);
            if (irongolementity != null) {
               list.forEach(GolemLastSeenSensor::golemDetected);
            }
         }
      }
   }

   public boolean wantsToSpawnGolem(long pGameTime) {
      if (!this.golemSpawnConditionsMet(this.level.getGameTime())) {
         return false;
      } else {
         return !this.brain.hasMemoryValue(MemoryModuleType.GOLEM_DETECTED_RECENTLY);
      }
   }

   @Nullable
   private IronGolemEntity trySpawnGolem(ServerWorld pServerLevel) {
      BlockPos blockpos = this.blockPosition();

      for(int i = 0; i < 10; ++i) {
         double d0 = (double)(pServerLevel.random.nextInt(16) - 8);
         double d1 = (double)(pServerLevel.random.nextInt(16) - 8);
         BlockPos blockpos1 = this.findSpawnPositionForGolemInColumn(blockpos, d0, d1);
         if (blockpos1 != null) {
            IronGolemEntity irongolementity = EntityType.IRON_GOLEM.create(pServerLevel, (CompoundNBT)null, (ITextComponent)null, (PlayerEntity)null, blockpos1, SpawnReason.MOB_SUMMONED, false, false);
            if (irongolementity != null) {
               if (irongolementity.checkSpawnRules(pServerLevel, SpawnReason.MOB_SUMMONED) && irongolementity.checkSpawnObstruction(pServerLevel)) {
                  pServerLevel.addFreshEntityWithPassengers(irongolementity);
                  return irongolementity;
               }

               irongolementity.remove();
            }
         }
      }

      return null;
   }

   @Nullable
   private BlockPos findSpawnPositionForGolemInColumn(BlockPos pVillagerPos, double pOffsetX, double pOffsetZ) {
      int i = 6;
      BlockPos blockpos = pVillagerPos.offset(pOffsetX, 6.0D, pOffsetZ);
      BlockState blockstate = this.level.getBlockState(blockpos);

      for(int j = 6; j >= -6; --j) {
         BlockPos blockpos1 = blockpos;
         BlockState blockstate1 = blockstate;
         blockpos = blockpos.below();
         blockstate = this.level.getBlockState(blockpos);
         if ((blockstate1.isAir() || blockstate1.getMaterial().isLiquid()) && blockstate.getMaterial().isSolidBlocking()) {
            return blockpos1;
         }
      }

      return null;
   }

   public void onReputationEventFrom(IReputationType pType, Entity pTarget) {
      if (pType == IReputationType.ZOMBIE_VILLAGER_CURED) {
         this.gossips.add(pTarget.getUUID(), GossipType.MAJOR_POSITIVE, 20);
         this.gossips.add(pTarget.getUUID(), GossipType.MINOR_POSITIVE, 25);
      } else if (pType == IReputationType.TRADE) {
         this.gossips.add(pTarget.getUUID(), GossipType.TRADING, 2);
      } else if (pType == IReputationType.VILLAGER_HURT) {
         this.gossips.add(pTarget.getUUID(), GossipType.MINOR_NEGATIVE, 25);
      } else if (pType == IReputationType.VILLAGER_KILLED) {
         this.gossips.add(pTarget.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
      }

   }

   public int getVillagerXp() {
      return this.villagerXp;
   }

   public void setVillagerXp(int pVillagerXp) {
      this.villagerXp = pVillagerXp;
   }

   private void resetNumberOfRestocks() {
      this.catchUpDemand();
      this.numberOfRestocksToday = 0;
   }

   public GossipManager getGossips() {
      return this.gossips;
   }

   public void setGossips(INBT pGossip) {
      this.gossips.update(new Dynamic<>(NBTDynamicOps.INSTANCE, pGossip));
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPacketSender.sendEntityBrain(this);
   }

   public void startSleeping(BlockPos pPos) {
      super.startSleeping(pPos);
      this.brain.setMemory(MemoryModuleType.LAST_SLEPT, this.level.getGameTime());
      this.brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
   }

   public void stopSleeping() {
      super.stopSleeping();
      this.brain.setMemory(MemoryModuleType.LAST_WOKEN, this.level.getGameTime());
   }

   private boolean golemSpawnConditionsMet(long pGameTime) {
      Optional<Long> optional = this.brain.getMemory(MemoryModuleType.LAST_SLEPT);
      if (optional.isPresent()) {
         return pGameTime - optional.get() < 24000L;
      } else {
         return false;
      }
   }
}
