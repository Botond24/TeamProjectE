package net.minecraft.entity.monster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.AnimalBreedTask;
import net.minecraft.entity.ai.brain.task.AttackTargetTask;
import net.minecraft.entity.ai.brain.task.ChildFollowNearestAdultTask;
import net.minecraft.entity.ai.brain.task.DummyTask;
import net.minecraft.entity.ai.brain.task.FindNewAttackTargetTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.PredicateTask;
import net.minecraft.entity.ai.brain.task.RandomlyStopAttackingTask;
import net.minecraft.entity.ai.brain.task.RunAwayTask;
import net.minecraft.entity.ai.brain.task.RunSometimesTask;
import net.minecraft.entity.ai.brain.task.SupplementedTask;
import net.minecraft.entity.ai.brain.task.WalkRandomlyTask;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TickRangeConverter;
import net.minecraft.util.math.BlockPos;

public class HoglinTasks {
   private static final RangedInteger RETREAT_DURATION = TickRangeConverter.rangeOfSeconds(5, 20);
   private static final RangedInteger ADULT_FOLLOW_RANGE = RangedInteger.of(5, 16);

   protected static Brain<?> makeBrain(Brain<HoglinEntity> pBrain) {
      initCoreActivity(pBrain);
      initIdleActivity(pBrain);
      initFightActivity(pBrain);
      initRetreatActivity(pBrain);
      pBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      pBrain.setDefaultActivity(Activity.IDLE);
      pBrain.useDefaultActivity();
      return pBrain;
   }

   private static void initCoreActivity(Brain<HoglinEntity> pBrain) {
      pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookTask(45, 90), new WalkToTargetTask()));
   }

   private static void initIdleActivity(Brain<HoglinEntity> pBrain) {
      pBrain.addActivity(Activity.IDLE, 10, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super HoglinEntity>>of(new RandomlyStopAttackingTask(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalBreedTask(EntityType.HOGLIN, 0.6F), RunAwayTask.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true), new ForgetAttackTargetTask<HoglinEntity>(HoglinTasks::findNearestValidAttackTarget), new SupplementedTask<HoglinEntity>(HoglinEntity::isAdult, RunAwayTask.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4F, 8, false)), new RunSometimesTask<LivingEntity>(new LookAtEntityTask(8.0F), RangedInteger.of(30, 60)), new ChildFollowNearestAdultTask(ADULT_FOLLOW_RANGE, 0.6F), createIdleMovementBehaviors()));
   }

   private static void initFightActivity(Brain<HoglinEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super HoglinEntity>>of(new RandomlyStopAttackingTask(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalBreedTask(EntityType.HOGLIN, 0.6F), new MoveToTargetTask(1.0F), new SupplementedTask<>(HoglinEntity::isAdult, new AttackTargetTask(40)), new SupplementedTask<>(AgeableEntity::isBaby, new AttackTargetTask(15)), new FindNewAttackTargetTask(), new PredicateTask<>(HoglinTasks::isBreeding, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
   }

   private static void initRetreatActivity(Brain<HoglinEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.<net.minecraft.entity.ai.brain.task.Task<? super HoglinEntity>>of(RunAwayTask.entity(MemoryModuleType.AVOID_TARGET, 1.3F, 15, false), createIdleMovementBehaviors(), new RunSometimesTask<LivingEntity>(new LookAtEntityTask(8.0F), RangedInteger.of(30, 60)), new PredicateTask<HoglinEntity>(HoglinTasks::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
   }

   private static FirstShuffledTask<HoglinEntity> createIdleMovementBehaviors() {
      return new FirstShuffledTask<>(ImmutableList.of(Pair.of(new WalkRandomlyTask(0.4F), 2), Pair.of(new WalkTowardsLookTargetTask(0.4F, 3), 2), Pair.of(new DummyTask(30, 60), 1)));
   }

   protected static void updateActivity(HoglinEntity pHoglin) {
      Brain<HoglinEntity> brain = pHoglin.getBrain();
      Activity activity = brain.getActiveNonCoreActivity().orElse((Activity)null);
      brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
      Activity activity1 = brain.getActiveNonCoreActivity().orElse((Activity)null);
      if (activity != activity1) {
         getSoundForCurrentActivity(pHoglin).ifPresent(pHoglin::playSound);
      }

      pHoglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   protected static void onHitTarget(HoglinEntity pHoglin, LivingEntity pTarget) {
      if (!pHoglin.isBaby()) {
         if (pTarget.getType() == EntityType.PIGLIN && piglinsOutnumberHoglins(pHoglin)) {
            setAvoidTarget(pHoglin, pTarget);
            broadcastRetreat(pHoglin, pTarget);
         } else {
            broadcastAttackTarget(pHoglin, pTarget);
         }
      }
   }

   private static void broadcastRetreat(HoglinEntity pHoglin, LivingEntity pTarget) {
      getVisibleAdultHoglins(pHoglin).forEach((p_234381_1_) -> {
         retreatFromNearestTarget(p_234381_1_, pTarget);
      });
   }

   private static void retreatFromNearestTarget(HoglinEntity pHoglin, LivingEntity pTarget) {
      Brain<HoglinEntity> brain = pHoglin.getBrain();
      LivingEntity lvt_2_1_ = BrainUtil.getNearestTarget(pHoglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), pTarget);
      lvt_2_1_ = BrainUtil.getNearestTarget(pHoglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), lvt_2_1_);
      setAvoidTarget(pHoglin, lvt_2_1_);
   }

   private static void setAvoidTarget(HoglinEntity pHoglin, LivingEntity pTarget) {
      pHoglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      pHoglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pHoglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, pTarget, (long)RETREAT_DURATION.randomValue(pHoglin.level.random));
   }

   private static Optional<? extends LivingEntity> findNearestValidAttackTarget(HoglinEntity p_234392_0_) {
      return !isPacified(p_234392_0_) && !isBreeding(p_234392_0_) ? p_234392_0_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER) : Optional.empty();
   }

   static boolean isPosNearNearestRepellent(HoglinEntity pHoglin, BlockPos pPos) {
      Optional<BlockPos> optional = pHoglin.getBrain().getMemory(MemoryModuleType.NEAREST_REPELLENT);
      return optional.isPresent() && optional.get().closerThan(pPos, 8.0D);
   }

   private static boolean wantsToStopFleeing(HoglinEntity p_234394_0_) {
      return p_234394_0_.isAdult() && !piglinsOutnumberHoglins(p_234394_0_);
   }

   private static boolean piglinsOutnumberHoglins(HoglinEntity pHoglin) {
      if (pHoglin.isBaby()) {
         return false;
      } else {
         int i = pHoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
         int j = pHoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
         return i > j;
      }
   }

   protected static void wasHurtBy(HoglinEntity pHoglin, LivingEntity pLivingEntity) {
      Brain<HoglinEntity> brain = pHoglin.getBrain();
      brain.eraseMemory(MemoryModuleType.PACIFIED);
      brain.eraseMemory(MemoryModuleType.BREED_TARGET);
      if (pHoglin.isBaby()) {
         retreatFromNearestTarget(pHoglin, pLivingEntity);
      } else {
         maybeRetaliate(pHoglin, pLivingEntity);
      }
   }

   private static void maybeRetaliate(HoglinEntity pHoglin, LivingEntity pLivingEntity) {
      if (!pHoglin.getBrain().isActive(Activity.AVOID) || pLivingEntity.getType() != EntityType.PIGLIN) {
         if (EntityPredicates.ATTACK_ALLOWED.test(pLivingEntity)) {
            if (pLivingEntity.getType() != EntityType.HOGLIN) {
               if (!BrainUtil.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(pHoglin, pLivingEntity, 4.0D)) {
                  setAttackTarget(pHoglin, pLivingEntity);
                  broadcastAttackTarget(pHoglin, pLivingEntity);
               }
            }
         }
      }
   }

   private static void setAttackTarget(HoglinEntity pHoglin, LivingEntity pTarget) {
      Brain<HoglinEntity> brain = pHoglin.getBrain();
      brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      brain.eraseMemory(MemoryModuleType.BREED_TARGET);
      brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, pTarget, 200L);
   }

   private static void broadcastAttackTarget(HoglinEntity pHoglin, LivingEntity pTarget) {
      getVisibleAdultHoglins(pHoglin).forEach((p_234375_1_) -> {
         setAttackTargetIfCloserThanCurrent(p_234375_1_, pTarget);
      });
   }

   private static void setAttackTargetIfCloserThanCurrent(HoglinEntity pHoglin, LivingEntity pTarget) {
      if (!isPacified(pHoglin)) {
         Optional<LivingEntity> optional = pHoglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
         LivingEntity livingentity = BrainUtil.getNearestTarget(pHoglin, optional, pTarget);
         setAttackTarget(pHoglin, livingentity);
      }
   }

   public static Optional<SoundEvent> getSoundForCurrentActivity(HoglinEntity pHoglin) {
      return pHoglin.getBrain().getActiveNonCoreActivity().map((p_234379_1_) -> {
         return getSoundForActivity(pHoglin, p_234379_1_);
      });
   }

   private static SoundEvent getSoundForActivity(HoglinEntity pHoglin, Activity pActivity) {
      if (pActivity != Activity.AVOID && !pHoglin.isConverting()) {
         if (pActivity == Activity.FIGHT) {
            return SoundEvents.HOGLIN_ANGRY;
         } else {
            return isNearRepellent(pHoglin) ? SoundEvents.HOGLIN_RETREAT : SoundEvents.HOGLIN_AMBIENT;
         }
      } else {
         return SoundEvents.HOGLIN_RETREAT;
      }
   }

   private static List<HoglinEntity> getVisibleAdultHoglins(HoglinEntity pHoglin) {
      return pHoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
   }

   private static boolean isNearRepellent(HoglinEntity pHoglin) {
      return pHoglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
   }

   private static boolean isBreeding(HoglinEntity p_234402_0_) {
      return p_234402_0_.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
   }

   protected static boolean isPacified(HoglinEntity pHoglin) {
      return pHoglin.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
   }
}