package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public class ForgetAttackTargetTask<E extends MobEntity> extends Task<E> {
   private final Predicate<E> canAttackPredicate;
   private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

   public ForgetAttackTargetTask(Predicate<E> p_i231537_1_, Function<E, Optional<? extends LivingEntity>> p_i231537_2_) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleStatus.REGISTERED));
      this.canAttackPredicate = p_i231537_1_;
      this.targetFinderFunction = p_i231537_2_;
   }

   public ForgetAttackTargetTask(Function<E, Optional<? extends LivingEntity>> p_i231536_1_) {
      this((p_233975_0_) -> {
         return true;
      }, p_i231536_1_);
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      if (!this.canAttackPredicate.test(pOwner)) {
         return false;
      } else {
         Optional<? extends LivingEntity> optional = this.targetFinderFunction.apply(pOwner);
         return optional.isPresent() && optional.get().isAlive();
      }
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      this.targetFinderFunction.apply(pEntity).ifPresent((p_233977_2_) -> {
         this.setAttackTarget(pEntity, p_233977_2_);
      });
   }

   private void setAttackTarget(E p_233976_1_, LivingEntity p_233976_2_) {
      p_233976_1_.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, p_233976_2_);
      p_233976_1_.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
   }
}