package net.minecraft.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.AttackStrafingTask;
import net.minecraft.entity.ai.brain.task.AttackTargetTask;
import net.minecraft.entity.ai.brain.task.DummyTask;
import net.minecraft.entity.ai.brain.task.EndAttackTask;
import net.minecraft.entity.ai.brain.task.FindInteractionAndLookTargetTask;
import net.minecraft.entity.ai.brain.task.FindNewAttackTargetTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.GetAngryTask;
import net.minecraft.entity.ai.brain.task.HuntCelebrationTask;
import net.minecraft.entity.ai.brain.task.InteractWithDoorTask;
import net.minecraft.entity.ai.brain.task.InteractWithEntityTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.PickupWantedItemTask;
import net.minecraft.entity.ai.brain.task.PiglinIdleActivityTask;
import net.minecraft.entity.ai.brain.task.PredicateTask;
import net.minecraft.entity.ai.brain.task.RideEntityTask;
import net.minecraft.entity.ai.brain.task.RunAwayTask;
import net.minecraft.entity.ai.brain.task.RunSometimesTask;
import net.minecraft.entity.ai.brain.task.ShootTargetTask;
import net.minecraft.entity.ai.brain.task.StopRidingEntityTask;
import net.minecraft.entity.ai.brain.task.SupplementedTask;
import net.minecraft.entity.ai.brain.task.WalkRandomlyTask;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TickRangeConverter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public class PiglinTasks {
   public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
   private static final RangedInteger TIME_BETWEEN_HUNTS = TickRangeConverter.rangeOfSeconds(30, 120);
   private static final RangedInteger RIDE_START_INTERVAL = TickRangeConverter.rangeOfSeconds(10, 40);
   private static final RangedInteger RIDE_DURATION = TickRangeConverter.rangeOfSeconds(10, 30);
   private static final RangedInteger RETREAT_DURATION = TickRangeConverter.rangeOfSeconds(5, 20);
   private static final RangedInteger AVOID_ZOMBIFIED_DURATION = TickRangeConverter.rangeOfSeconds(5, 7);
   private static final RangedInteger BABY_AVOID_NEMESIS_DURATION = TickRangeConverter.rangeOfSeconds(5, 7);
   private static final Set<Item> FOOD_ITEMS = ImmutableSet.of(Items.PORKCHOP, Items.COOKED_PORKCHOP);

   protected static Brain<?> makeBrain(PiglinEntity pPiglin, Brain<PiglinEntity> pBrain) {
      initCoreActivity(pBrain);
      initIdleActivity(pBrain);
      initAdmireItemActivity(pBrain);
      initFightActivity(pPiglin, pBrain);
      initCelebrateActivity(pBrain);
      initRetreatActivity(pBrain);
      initRideHoglinActivity(pBrain);
      pBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      pBrain.setDefaultActivity(Activity.IDLE);
      pBrain.useDefaultActivity();
      return pBrain;
   }

   protected static void initMemories(PiglinEntity pPiglin) {
      int i = TIME_BETWEEN_HUNTS.randomValue(pPiglin.level.random);
      pPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)i);
   }

   private static void initCoreActivity(Brain<PiglinEntity> pBrain) {
      pBrain.addActivity(Activity.CORE, 0, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super PiglinEntity>>of(new LookTask(45, 90), new WalkToTargetTask(), new InteractWithDoorTask(), babyAvoidNemesis(), avoidZombified(), new StartAdmiringItemTask(), new AdmireItemTask(120), new EndAttackTask(300, PiglinTasks::wantsToDance), new GetAngryTask()));
   }

   private static void initIdleActivity(Brain<PiglinEntity> pBrain) {
      pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(new LookAtEntityTask(PiglinTasks::isPlayerHoldingLovedItem, 14.0F), new ForgetAttackTargetTask<>(AbstractPiglinEntity::isAdult, PiglinTasks::findNearestValidAttackTarget), new SupplementedTask<>(PiglinEntity::canHunt, new StartHuntTask<>()), avoidRepellent(), babySometimesRideBabyHoglin(), createIdleLookBehaviors(), createIdleMovementBehaviors(), new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)));
   }

   private static void initFightActivity(PiglinEntity pPiglin, Brain<PiglinEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super PiglinEntity>>of(new FindNewAttackTargetTask<>((p_234523_1_) -> {
         return !isNearestValidAttackTarget(pPiglin, p_234523_1_);
      }), new SupplementedTask<>(PiglinTasks::hasCrossbow, new AttackStrafingTask<>(5, 0.75F)), new MoveToTargetTask(1.0F), new AttackTargetTask(20), new ShootTargetTask(), new FinishedHuntTask(), new PredicateTask<>(PiglinTasks::isNearZombified, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
   }

   private static void initCelebrateActivity(Brain<PiglinEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.CELEBRATE, 10, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super PiglinEntity>>of(avoidRepellent(), new LookAtEntityTask(PiglinTasks::isPlayerHoldingLovedItem, 14.0F), new ForgetAttackTargetTask<PiglinEntity>(AbstractPiglinEntity::isAdult, PiglinTasks::findNearestValidAttackTarget), new SupplementedTask<PiglinEntity>((p_234457_0_) -> {
         return !p_234457_0_.isDancing();
      }, new HuntCelebrationTask<>(2, 1.0F)), new SupplementedTask<PiglinEntity>(PiglinEntity::isDancing, new HuntCelebrationTask<>(4, 0.6F)), new FirstShuffledTask(ImmutableList.of(Pair.of(new LookAtEntityTask(EntityType.PIGLIN, 8.0F), 1), Pair.of(new WalkRandomlyTask(0.6F, 2, 1), 1), Pair.of(new DummyTask(10, 20), 1)))), MemoryModuleType.CELEBRATE_LOCATION);
   }

   private static void initAdmireItemActivity(Brain<PiglinEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super PiglinEntity>>of(new PickupWantedItemTask<>(PiglinTasks::isNotHoldingLovedItemInOffHand, 1.0F, true, 9), new ForgetAdmiredItemTask(9), new StopReachingItemTask(200, 200)), MemoryModuleType.ADMIRING_ITEM);
   }

   private static void initRetreatActivity(Brain<PiglinEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(RunAwayTask.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true), createIdleLookBehaviors(), createIdleMovementBehaviors(), new PredicateTask<PiglinEntity>(PiglinTasks::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
   }

   private static void initRideHoglinActivity(Brain<PiglinEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, ImmutableList.of(new RideEntityTask<>(0.8F), new LookAtEntityTask(PiglinTasks::isPlayerHoldingLovedItem, 8.0F), new SupplementedTask<>(Entity::isPassenger, createIdleLookBehaviors()), new StopRidingEntityTask<>(8, PiglinTasks::wantsToStopRiding)), MemoryModuleType.RIDE_TARGET);
   }

   private static FirstShuffledTask<PiglinEntity> createIdleLookBehaviors() {
      return new FirstShuffledTask<>(ImmutableList.of(Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 1), Pair.of(new LookAtEntityTask(EntityType.PIGLIN, 8.0F), 1), Pair.of(new LookAtEntityTask(8.0F), 1), Pair.of(new DummyTask(30, 60), 1)));
   }

   private static FirstShuffledTask<PiglinEntity> createIdleMovementBehaviors() {
      return new FirstShuffledTask<>(ImmutableList.of(Pair.of(new WalkRandomlyTask(0.6F), 2), Pair.of(InteractWithEntityTask.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(new SupplementedTask<>(PiglinTasks::doesntSeeAnyPlayerHoldingLovedItem, new WalkTowardsLookTargetTask(0.6F, 3)), 2), Pair.of(new DummyTask(30, 60), 1)));
   }

   private static RunAwayTask<BlockPos> avoidRepellent() {
      return RunAwayTask.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
   }

   private static PiglinIdleActivityTask<PiglinEntity, LivingEntity> babyAvoidNemesis() {
      return new PiglinIdleActivityTask<>(PiglinEntity::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
   }

   private static PiglinIdleActivityTask<PiglinEntity, LivingEntity> avoidZombified() {
      return new PiglinIdleActivityTask<>(PiglinTasks::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION);
   }

   protected static void updateActivity(PiglinEntity pPiglin) {
      Brain<PiglinEntity> brain = pPiglin.getBrain();
      Activity activity = brain.getActiveNonCoreActivity().orElse((Activity)null);
      brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
      Activity activity1 = brain.getActiveNonCoreActivity().orElse((Activity)null);
      if (activity != activity1) {
         getSoundForCurrentActivity(pPiglin).ifPresent(pPiglin::playSound);
      }

      pPiglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
      if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(pPiglin)) {
         pPiglin.stopRiding();
      }

      if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
         brain.eraseMemory(MemoryModuleType.DANCING);
      }

      pPiglin.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
   }

   private static boolean isBabyRidingBaby(PiglinEntity pPassenger) {
      if (!pPassenger.isBaby()) {
         return false;
      } else {
         Entity entity = pPassenger.getVehicle();
         return entity instanceof PiglinEntity && ((PiglinEntity)entity).isBaby() || entity instanceof HoglinEntity && ((HoglinEntity)entity).isBaby();
      }
   }

   protected static void pickUpItem(PiglinEntity pPiglin, ItemEntity pItemEntity) {
      stopWalking(pPiglin);
      ItemStack itemstack;
      if (pItemEntity.getItem().getItem() == Items.GOLD_NUGGET) {
         pPiglin.take(pItemEntity, pItemEntity.getItem().getCount());
         itemstack = pItemEntity.getItem();
         pItemEntity.remove();
      } else {
         pPiglin.take(pItemEntity, 1);
         itemstack = removeOneItemFromItemEntity(pItemEntity);
      }

      Item item = itemstack.getItem();
      if (isLovedItem(item)) {
         pPiglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
         holdInOffhand(pPiglin, itemstack);
         admireGoldItem(pPiglin);
      } else if (isFood(item) && !hasEatenRecently(pPiglin)) {
         eat(pPiglin);
      } else {
         boolean flag = pPiglin.equipItemIfPossible(itemstack);
         if (!flag) {
            putInInventory(pPiglin, itemstack);
         }
      }
   }

   private static void holdInOffhand(PiglinEntity pPiglin, ItemStack pStack) {
      if (isHoldingItemInOffHand(pPiglin)) {
         pPiglin.spawnAtLocation(pPiglin.getItemInHand(Hand.OFF_HAND));
      }

      pPiglin.holdInOffHand(pStack);
   }

   private static ItemStack removeOneItemFromItemEntity(ItemEntity pItemEntity) {
      ItemStack itemstack = pItemEntity.getItem();
      ItemStack itemstack1 = itemstack.split(1);
      if (itemstack.isEmpty()) {
         pItemEntity.remove();
      } else {
         pItemEntity.setItem(itemstack);
      }

      return itemstack1;
   }

   protected static void stopHoldingOffHandItem(PiglinEntity pPiglin, boolean pShouldBarter) {
      ItemStack itemstack = pPiglin.getItemInHand(Hand.OFF_HAND);
      pPiglin.setItemInHand(Hand.OFF_HAND, ItemStack.EMPTY);
      if (pPiglin.isAdult()) {
         boolean flag = itemstack.isPiglinCurrency();
         if (pShouldBarter && flag) {
            throwItems(pPiglin, getBarterResponseItems(pPiglin));
         } else if (!flag) {
            boolean flag1 = pPiglin.equipItemIfPossible(itemstack);
            if (!flag1) {
               putInInventory(pPiglin, itemstack);
            }
         }
      } else {
         boolean flag2 = pPiglin.equipItemIfPossible(itemstack);
         if (!flag2) {
            ItemStack itemstack1 = pPiglin.getMainHandItem();
            if (isLovedItem(itemstack1.getItem())) {
               putInInventory(pPiglin, itemstack1);
            } else {
               throwItems(pPiglin, Collections.singletonList(itemstack1));
            }

            pPiglin.holdInMainHand(itemstack);
         }
      }

   }

   protected static void cancelAdmiring(PiglinEntity pPiglin) {
      if (isAdmiringItem(pPiglin) && !pPiglin.getOffhandItem().isEmpty()) {
         pPiglin.spawnAtLocation(pPiglin.getOffhandItem());
         pPiglin.setItemInHand(Hand.OFF_HAND, ItemStack.EMPTY);
      }

   }

   private static void putInInventory(PiglinEntity pPiglin, ItemStack pStack) {
      ItemStack itemstack = pPiglin.addToInventory(pStack);
      throwItemsTowardRandomPos(pPiglin, Collections.singletonList(itemstack));
   }

   private static void throwItems(PiglinEntity pPilgin, List<ItemStack> pStacks) {
      Optional<PlayerEntity> optional = pPilgin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
      if (optional.isPresent()) {
         throwItemsTowardPlayer(pPilgin, optional.get(), pStacks);
      } else {
         throwItemsTowardRandomPos(pPilgin, pStacks);
      }

   }

   private static void throwItemsTowardRandomPos(PiglinEntity pPiglin, List<ItemStack> pStacks) {
      throwItemsTowardPos(pPiglin, pStacks, getRandomNearbyPos(pPiglin));
   }

   private static void throwItemsTowardPlayer(PiglinEntity pPiglin, PlayerEntity pPlayer, List<ItemStack> pStacks) {
      throwItemsTowardPos(pPiglin, pStacks, pPlayer.position());
   }

   private static void throwItemsTowardPos(PiglinEntity pPiglin, List<ItemStack> pStacks, Vector3d pPos) {
      if (!pStacks.isEmpty()) {
         pPiglin.swing(Hand.OFF_HAND);

         for(ItemStack itemstack : pStacks) {
            BrainUtil.throwItem(pPiglin, itemstack, pPos.add(0.0D, 1.0D, 0.0D));
         }
      }

   }

   private static List<ItemStack> getBarterResponseItems(PiglinEntity pPiglin) {
      LootTable loottable = pPiglin.level.getServer().getLootTables().get(LootTables.PIGLIN_BARTERING);
      return loottable.getRandomItems((new LootContext.Builder((ServerWorld)pPiglin.level)).withParameter(LootParameters.THIS_ENTITY, pPiglin).withRandom(pPiglin.level.random).create(LootParameterSets.PIGLIN_BARTER));
   }

   private static boolean wantsToDance(LivingEntity p_234461_0_, LivingEntity p_234461_1_) {
      if (p_234461_1_.getType() != EntityType.HOGLIN) {
         return false;
      } else {
         return (new Random(p_234461_0_.level.getGameTime())).nextFloat() < 0.1F;
      }
   }

   protected static boolean wantsToPickup(PiglinEntity pPiglin, ItemStack pStack) {
      Item item = pStack.getItem();
      if (item.is(ItemTags.PIGLIN_REPELLENTS)) {
         return false;
      } else if (isAdmiringDisabled(pPiglin) && pPiglin.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
         return false;
      } else if (pStack.isPiglinCurrency()) {
         return isNotHoldingLovedItemInOffHand(pPiglin);
      } else {
         boolean flag = pPiglin.canAddToInventory(pStack);
         if (item == Items.GOLD_NUGGET) {
            return flag;
         } else if (isFood(item)) {
            return !hasEatenRecently(pPiglin) && flag;
         } else if (!isLovedItem(item)) {
            return pPiglin.canReplaceCurrentItem(pStack);
         } else {
            return isNotHoldingLovedItemInOffHand(pPiglin) && flag;
         }
      }
   }

   protected static boolean isLovedItem(Item p_234480_0_) {
      return p_234480_0_.is(ItemTags.PIGLIN_LOVED);
   }

   private static boolean wantsToStopRiding(PiglinEntity p_234467_0_, Entity p_234467_1_) {
      if (!(p_234467_1_ instanceof MobEntity)) {
         return false;
      } else {
         MobEntity mobentity = (MobEntity)p_234467_1_;
         return !mobentity.isBaby() || !mobentity.isAlive() || wasHurtRecently(p_234467_0_) || wasHurtRecently(mobentity) || mobentity instanceof PiglinEntity && mobentity.getVehicle() == null;
      }
   }

   private static boolean isNearestValidAttackTarget(PiglinEntity pPiglin, LivingEntity pTarget) {
      return findNearestValidAttackTarget(pPiglin).filter((p_234483_1_) -> {
         return p_234483_1_ == pTarget;
      }).isPresent();
   }

   private static boolean isNearZombified(PiglinEntity p_234525_0_) {
      Brain<PiglinEntity> brain = p_234525_0_.getBrain();
      if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
         LivingEntity livingentity = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
         return p_234525_0_.closerThan(livingentity, 6.0D);
      } else {
         return false;
      }
   }

   private static Optional<? extends LivingEntity> findNearestValidAttackTarget(PiglinEntity p_234526_0_) {
      Brain<PiglinEntity> brain = p_234526_0_.getBrain();
      if (isNearZombified(p_234526_0_)) {
         return Optional.empty();
      } else {
         Optional<LivingEntity> optional = BrainUtil.getLivingEntityFromUUIDMemory(p_234526_0_, MemoryModuleType.ANGRY_AT);
         if (optional.isPresent() && isAttackAllowed(optional.get())) {
            return optional;
         } else {
            if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
               Optional<PlayerEntity> optional1 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
               if (optional1.isPresent()) {
                  return optional1;
               }
            }

            Optional<MobEntity> optional3 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
            if (optional3.isPresent()) {
               return optional3;
            } else {
               Optional<PlayerEntity> optional2 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
               return optional2.isPresent() && isAttackAllowed(optional2.get()) ? optional2 : Optional.empty();
            }
         }
      }
   }

   public static void angerNearbyPiglins(PlayerEntity pPlayer, boolean pAngerOnlyIfCanSee) {
      List<PiglinEntity> list = pPlayer.level.getEntitiesOfClass(PiglinEntity.class, pPlayer.getBoundingBox().inflate(16.0D));
      list.stream().filter(PiglinTasks::isIdle).filter((p_234491_2_) -> {
         return !pAngerOnlyIfCanSee || BrainUtil.canSee(p_234491_2_, pPlayer);
      }).forEach((p_234479_1_) -> {
         if (p_234479_1_.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            setAngerTargetToNearestTargetablePlayerIfFound(p_234479_1_, pPlayer);
         } else {
            setAngerTarget(p_234479_1_, pPlayer);
         }

      });
   }

   public static ActionResultType mobInteract(PiglinEntity pPiglin, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (canAdmire(pPiglin, itemstack)) {
         ItemStack itemstack1 = itemstack.split(1);
         holdInOffhand(pPiglin, itemstack1);
         admireGoldItem(pPiglin);
         stopWalking(pPiglin);
         return ActionResultType.CONSUME;
      } else {
         return ActionResultType.PASS;
      }
   }

   protected static boolean canAdmire(PiglinEntity pPiglin, ItemStack pStack) {
      return !isAdmiringDisabled(pPiglin) && !isAdmiringItem(pPiglin) && pPiglin.isAdult() && pStack.isPiglinCurrency();
   }

   protected static void wasHurtBy(PiglinEntity pPiglin, LivingEntity pTarget) {
      if (!(pTarget instanceof PiglinEntity)) {
         if (isHoldingItemInOffHand(pPiglin)) {
            stopHoldingOffHandItem(pPiglin, false);
         }

         Brain<PiglinEntity> brain = pPiglin.getBrain();
         brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
         brain.eraseMemory(MemoryModuleType.DANCING);
         brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
         if (pTarget instanceof PlayerEntity) {
            brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
         }

         getAvoidTarget(pPiglin).ifPresent((p_234462_2_) -> {
            if (p_234462_2_.getType() != pTarget.getType()) {
               brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
            }

         });
         if (pPiglin.isBaby()) {
            brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, pTarget, 100L);
            if (isAttackAllowed(pTarget)) {
               broadcastAngerTarget(pPiglin, pTarget);
            }

         } else if (pTarget.getType() == EntityType.HOGLIN && hoglinsOutnumberPiglins(pPiglin)) {
            setAvoidTargetAndDontHuntForAWhile(pPiglin, pTarget);
            broadcastRetreat(pPiglin, pTarget);
         } else {
            maybeRetaliate(pPiglin, pTarget);
         }
      }
   }

   protected static void maybeRetaliate(AbstractPiglinEntity pPiglin, LivingEntity pTarget) {
      if (!pPiglin.getBrain().isActive(Activity.AVOID)) {
         if (isAttackAllowed(pTarget)) {
            if (!BrainUtil.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(pPiglin, pTarget, 4.0D)) {
               if (pTarget.getType() == EntityType.PLAYER && pPiglin.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                  setAngerTargetToNearestTargetablePlayerIfFound(pPiglin, pTarget);
                  broadcastUniversalAnger(pPiglin);
               } else {
                  setAngerTarget(pPiglin, pTarget);
                  broadcastAngerTarget(pPiglin, pTarget);
               }

            }
         }
      }
   }

   public static Optional<SoundEvent> getSoundForCurrentActivity(PiglinEntity pPiglin) {
      return pPiglin.getBrain().getActiveNonCoreActivity().map((p_241426_1_) -> {
         return getSoundForActivity(pPiglin, p_241426_1_);
      });
   }

   private static SoundEvent getSoundForActivity(PiglinEntity pPiglin, Activity pActivity) {
      if (pActivity == Activity.FIGHT) {
         return SoundEvents.PIGLIN_ANGRY;
      } else if (pPiglin.isConverting()) {
         return SoundEvents.PIGLIN_RETREAT;
      } else if (pActivity == Activity.AVOID && isNearAvoidTarget(pPiglin)) {
         return SoundEvents.PIGLIN_RETREAT;
      } else if (pActivity == Activity.ADMIRE_ITEM) {
         return SoundEvents.PIGLIN_ADMIRING_ITEM;
      } else if (pActivity == Activity.CELEBRATE) {
         return SoundEvents.PIGLIN_CELEBRATE;
      } else if (seesPlayerHoldingLovedItem(pPiglin)) {
         return SoundEvents.PIGLIN_JEALOUS;
      } else {
         return isNearRepellent(pPiglin) ? SoundEvents.PIGLIN_RETREAT : SoundEvents.PIGLIN_AMBIENT;
      }
   }

   private static boolean isNearAvoidTarget(PiglinEntity pPiglin) {
      Brain<PiglinEntity> brain = pPiglin.getBrain();
      return !brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? false : brain.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(pPiglin, 12.0D);
   }

   protected static boolean hasAnyoneNearbyHuntedRecently(PiglinEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY) || getVisibleAdultPiglins(pPiglin).stream().anyMatch((p_234456_0_) -> {
         return p_234456_0_.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY);
      });
   }

   private static List<AbstractPiglinEntity> getVisibleAdultPiglins(PiglinEntity pPiglin) {
      return pPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
   }

   private static List<AbstractPiglinEntity> getAdultPiglins(AbstractPiglinEntity pPiglin) {
      return pPiglin.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
   }

   public static boolean isWearingGold(LivingEntity pLivingEntity) {
      for(ItemStack itemstack : pLivingEntity.getArmorSlots()) {
         Item item = itemstack.getItem();
         if (itemstack.makesPiglinsNeutral(pLivingEntity)) {
            return true;
         }
      }

      return false;
   }

   private static void stopWalking(PiglinEntity pPiglin) {
      pPiglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pPiglin.getNavigation().stop();
   }

   private static RunSometimesTask<PiglinEntity> babySometimesRideBabyHoglin() {
      return new RunSometimesTask<>(new PiglinIdleActivityTask<>(PiglinEntity::isBaby, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION), RIDE_START_INTERVAL);
   }

   protected static void broadcastAngerTarget(AbstractPiglinEntity pPiglin, LivingEntity pTarget) {
      getAdultPiglins(pPiglin).forEach((p_234484_1_) -> {
         if (pTarget.getType() != EntityType.HOGLIN || p_234484_1_.canHunt() && ((HoglinEntity)pTarget).canBeHunted()) {
            setAngerTargetIfCloserThanCurrent(p_234484_1_, pTarget);
         }
      });
   }

   protected static void broadcastUniversalAnger(AbstractPiglinEntity pPiglin) {
      getAdultPiglins(pPiglin).forEach((p_241419_0_) -> {
         getNearestVisibleTargetablePlayer(p_241419_0_).ifPresent((p_241421_1_) -> {
            setAngerTarget(p_241419_0_, p_241421_1_);
         });
      });
   }

   protected static void broadcastDontKillAnyMoreHoglinsForAWhile(PiglinEntity pPiglin) {
      getVisibleAdultPiglins(pPiglin).forEach(PiglinTasks::dontKillAnyMoreHoglinsForAWhile);
   }

   protected static void setAngerTarget(AbstractPiglinEntity pPiglin, LivingEntity pTarget) {
      if (isAttackAllowed(pTarget)) {
         pPiglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         pPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, pTarget.getUUID(), 600L);
         if (pTarget.getType() == EntityType.HOGLIN && pPiglin.canHunt()) {
            dontKillAnyMoreHoglinsForAWhile(pPiglin);
         }

         if (pTarget.getType() == EntityType.PLAYER && pPiglin.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            pPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
         }

      }
   }

   private static void setAngerTargetToNearestTargetablePlayerIfFound(AbstractPiglinEntity pPiglin, LivingEntity pCurrentTarget) {
      Optional<PlayerEntity> optional = getNearestVisibleTargetablePlayer(pPiglin);
      if (optional.isPresent()) {
         setAngerTarget(pPiglin, optional.get());
      } else {
         setAngerTarget(pPiglin, pCurrentTarget);
      }

   }

   private static void setAngerTargetIfCloserThanCurrent(AbstractPiglinEntity pPiglin, LivingEntity pCurrentTarget) {
      Optional<LivingEntity> optional = getAngerTarget(pPiglin);
      LivingEntity livingentity = BrainUtil.getNearestTarget(pPiglin, optional, pCurrentTarget);
      if (!optional.isPresent() || optional.get() != livingentity) {
         setAngerTarget(pPiglin, livingentity);
      }
   }

   private static Optional<LivingEntity> getAngerTarget(AbstractPiglinEntity pPiglin) {
      return BrainUtil.getLivingEntityFromUUIDMemory(pPiglin, MemoryModuleType.ANGRY_AT);
   }

   public static Optional<LivingEntity> getAvoidTarget(PiglinEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? pPiglin.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
   }

   public static Optional<PlayerEntity> getNearestVisibleTargetablePlayer(AbstractPiglinEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER) ? pPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER) : Optional.empty();
   }

   private static void broadcastRetreat(PiglinEntity pPiglin, LivingEntity pTarget) {
      getVisibleAdultPiglins(pPiglin).stream().filter((p_242341_0_) -> {
         return p_242341_0_ instanceof PiglinEntity;
      }).forEach((p_234463_1_) -> {
         retreatFromNearestTarget((PiglinEntity)p_234463_1_, pTarget);
      });
   }

   private static void retreatFromNearestTarget(PiglinEntity pPiglin, LivingEntity pTarget) {
      Brain<PiglinEntity> brain = pPiglin.getBrain();
      LivingEntity lvt_3_1_ = BrainUtil.getNearestTarget(pPiglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), pTarget);
      lvt_3_1_ = BrainUtil.getNearestTarget(pPiglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), lvt_3_1_);
      setAvoidTargetAndDontHuntForAWhile(pPiglin, lvt_3_1_);
   }

   private static boolean wantsToStopFleeing(PiglinEntity p_234533_0_) {
      Brain<PiglinEntity> brain = p_234533_0_.getBrain();
      if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
         return true;
      } else {
         LivingEntity livingentity = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
         EntityType<?> entitytype = livingentity.getType();
         if (entitytype == EntityType.HOGLIN) {
            return piglinsEqualOrOutnumberHoglins(p_234533_0_);
         } else if (isZombified(entitytype)) {
            return !brain.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, livingentity);
         } else {
            return false;
         }
      }
   }

   private static boolean piglinsEqualOrOutnumberHoglins(PiglinEntity pPiglin) {
      return !hoglinsOutnumberPiglins(pPiglin);
   }

   private static boolean hoglinsOutnumberPiglins(PiglinEntity pPiglin) {
      int i = pPiglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
      int j = pPiglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
      return j > i;
   }

   private static void setAvoidTargetAndDontHuntForAWhile(PiglinEntity pPiglin, LivingEntity pTarget) {
      pPiglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
      pPiglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      pPiglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, pTarget, (long)RETREAT_DURATION.randomValue(pPiglin.level.random));
      dontKillAnyMoreHoglinsForAWhile(pPiglin);
   }

   protected static void dontKillAnyMoreHoglinsForAWhile(AbstractPiglinEntity p_234518_0_) {
      p_234518_0_.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)TIME_BETWEEN_HUNTS.randomValue(p_234518_0_.level.random));
   }

   private static void eat(PiglinEntity pPiglin) {
      pPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
   }

   private static Vector3d getRandomNearbyPos(PiglinEntity pPiglin) {
      Vector3d vector3d = RandomPositionGenerator.getLandPos(pPiglin, 4, 2);
      return vector3d == null ? pPiglin.position() : vector3d;
   }

   private static boolean hasEatenRecently(PiglinEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
   }

   protected static boolean isIdle(AbstractPiglinEntity p_234520_0_) {
      return p_234520_0_.getBrain().isActive(Activity.IDLE);
   }

   private static boolean hasCrossbow(LivingEntity p_234494_0_) {
      return p_234494_0_.isHolding(item -> item instanceof net.minecraft.item.CrossbowItem);
   }

   private static void admireGoldItem(LivingEntity pPiglin) {
      pPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
   }

   private static boolean isAdmiringItem(PiglinEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
   }

   private static boolean isBarterCurrency(Item pItem) {
      return pItem == BARTERING_ITEM;
   }

   private static boolean isFood(Item pItem) {
      return FOOD_ITEMS.contains(pItem);
   }

   private static boolean isAttackAllowed(LivingEntity pTarget) {
      return EntityPredicates.ATTACK_ALLOWED.test(pTarget);
   }

   private static boolean isNearRepellent(PiglinEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
   }

   private static boolean seesPlayerHoldingLovedItem(LivingEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
   }

   private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity p_234514_0_) {
      return !seesPlayerHoldingLovedItem(p_234514_0_);
   }

   public static boolean isPlayerHoldingLovedItem(LivingEntity p_234482_0_) {
      return p_234482_0_.getType() == EntityType.PLAYER && p_234482_0_.isHolding(PiglinTasks::isLovedItem);
   }

   private static boolean isAdmiringDisabled(PiglinEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
   }

   private static boolean wasHurtRecently(LivingEntity pPiglin) {
      return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
   }

   private static boolean isHoldingItemInOffHand(PiglinEntity pPiglin) {
      return !pPiglin.getOffhandItem().isEmpty();
   }

   private static boolean isNotHoldingLovedItemInOffHand(PiglinEntity p_234455_0_) {
      return p_234455_0_.getOffhandItem().isEmpty() || !isLovedItem(p_234455_0_.getOffhandItem().getItem());
   }

   public static boolean isZombified(EntityType pEntityType) {
      return pEntityType == EntityType.ZOMBIFIED_PIGLIN || pEntityType == EntityType.ZOGLIN;
   }
}
