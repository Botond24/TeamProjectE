package net.minecraft.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.AttackTargetTask;
import net.minecraft.entity.ai.brain.task.DummyTask;
import net.minecraft.entity.ai.brain.task.FindInteractionAndLookTargetTask;
import net.minecraft.entity.ai.brain.task.FindNewAttackTargetTask;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.GetAngryTask;
import net.minecraft.entity.ai.brain.task.InteractWithDoorTask;
import net.minecraft.entity.ai.brain.task.InteractWithEntityTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.WalkRandomlyTask;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import net.minecraft.entity.ai.brain.task.WorkTask;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.GlobalPos;

public class PiglinBruteBrain {
   protected static Brain<?> makeBrain(PiglinBruteEntity pPiglinBrute, Brain<PiglinBruteEntity> pBrain) {
      initCoreActivity(pPiglinBrute, pBrain);
      initIdleActivity(pPiglinBrute, pBrain);
      initFightActivity(pPiglinBrute, pBrain);
      pBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      pBrain.setDefaultActivity(Activity.IDLE);
      pBrain.useDefaultActivity();
      return pBrain;
   }

   protected static void initMemories(PiglinBruteEntity pPiglinBrute) {
      GlobalPos globalpos = GlobalPos.of(pPiglinBrute.level.dimension(), pPiglinBrute.blockPosition());
      pPiglinBrute.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
   }

   private static void initCoreActivity(PiglinBruteEntity pPiglinBrute, Brain<PiglinBruteEntity> pBrain) {
      pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookTask(45, 90), new WalkToTargetTask(), new InteractWithDoorTask(), new GetAngryTask<>()));
   }

   private static void initIdleActivity(PiglinBruteEntity pPiglinBrute, Brain<PiglinBruteEntity> pBrain) {
      pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(new ForgetAttackTargetTask<>(PiglinBruteBrain::findNearestValidAttackTarget), createIdleLookBehaviors(), createIdleMovementBehaviors(), new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)));
   }

   private static void initFightActivity(PiglinBruteEntity pPiglinBrute, Brain<PiglinBruteEntity> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new FindNewAttackTargetTask<>((p_242361_1_) -> {
         return !isNearestValidAttackTarget(pPiglinBrute, p_242361_1_);
      }), new MoveToTargetTask(1.0F), new AttackTargetTask(20)), MemoryModuleType.ATTACK_TARGET);
   }

   private static FirstShuffledTask<PiglinBruteEntity> createIdleLookBehaviors() {
      return new FirstShuffledTask<>(ImmutableList.of(Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 1), Pair.of(new LookAtEntityTask(EntityType.PIGLIN, 8.0F), 1), Pair.of(new LookAtEntityTask(EntityType.PIGLIN_BRUTE, 8.0F), 1), Pair.of(new LookAtEntityTask(8.0F), 1), Pair.of(new DummyTask(30, 60), 1)));
   }

   private static FirstShuffledTask<PiglinBruteEntity> createIdleMovementBehaviors() {
      return new FirstShuffledTask<>(ImmutableList.of(Pair.of(new WalkRandomlyTask(0.6F), 2), Pair.of(InteractWithEntityTask.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(InteractWithEntityTask.of(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(new WalkTowardsPosTask(MemoryModuleType.HOME, 0.6F, 2, 100), 2), Pair.of(new WorkTask(MemoryModuleType.HOME, 0.6F, 5), 2), Pair.of(new DummyTask(30, 60), 1)));
   }

   protected static void updateActivity(PiglinBruteEntity pPiglinBrute) {
      Brain<PiglinBruteEntity> brain = pPiglinBrute.getBrain();
      Activity activity = brain.getActiveNonCoreActivity().orElse((Activity)null);
      brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity activity1 = brain.getActiveNonCoreActivity().orElse((Activity)null);
      if (activity != activity1) {
         playActivitySound(pPiglinBrute);
      }

      pPiglinBrute.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   private static boolean isNearestValidAttackTarget(AbstractPiglinEntity pPiglinBrute, LivingEntity pTarget) {
      return findNearestValidAttackTarget(pPiglinBrute).filter((p_242348_1_) -> {
         return p_242348_1_ == pTarget;
      }).isPresent();
   }

   private static Optional<? extends LivingEntity> findNearestValidAttackTarget(AbstractPiglinEntity p_242349_0_) {
      Optional<LivingEntity> optional = BrainUtil.getLivingEntityFromUUIDMemory(p_242349_0_, MemoryModuleType.ANGRY_AT);
      if (optional.isPresent() && isAttackAllowed(optional.get())) {
         return optional;
      } else {
         Optional<? extends LivingEntity> optional1 = getTargetIfWithinRange(p_242349_0_, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
         return optional1.isPresent() ? optional1 : p_242349_0_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
      }
   }

   private static boolean isAttackAllowed(LivingEntity pTarget) {
      return EntityPredicates.ATTACK_ALLOWED.test(pTarget);
   }

   private static Optional<? extends LivingEntity> getTargetIfWithinRange(AbstractPiglinEntity pPiglinBrute, MemoryModuleType<? extends LivingEntity> pMemoryType) {
      return pPiglinBrute.getBrain().getMemory(pMemoryType).filter((p_242357_1_) -> {
         return p_242357_1_.closerThan(pPiglinBrute, 12.0D);
      });
   }

   protected static void wasHurtBy(PiglinBruteEntity pPiglinBrute, LivingEntity pTarget) {
      if (!(pTarget instanceof AbstractPiglinEntity)) {
         PiglinTasks.maybeRetaliate(pPiglinBrute, pTarget);
      }
   }

   protected static void maybePlayActivitySound(PiglinBruteEntity pPiglinBrute) {
      if ((double)pPiglinBrute.level.random.nextFloat() < 0.0125D) {
         playActivitySound(pPiglinBrute);
      }

   }

   private static void playActivitySound(PiglinBruteEntity pPiglinBrute) {
      pPiglinBrute.getBrain().getActiveNonCoreActivity().ifPresent((p_242355_1_) -> {
         if (p_242355_1_ == Activity.FIGHT) {
            pPiglinBrute.playAngrySound();
         }

      });
   }
}