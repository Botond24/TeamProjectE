package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public class PredicateTask<E extends LivingEntity> extends Task<E> {
   private final Predicate<E> predicate;
   private final MemoryModuleType<?> memoryType;

   public PredicateTask(Predicate<E> p_i231517_1_, MemoryModuleType<?> p_i231517_2_) {
      super(ImmutableMap.of(p_i231517_2_, MemoryModuleStatus.VALUE_PRESENT));
      this.predicate = p_i231517_1_;
      this.memoryType = p_i231517_2_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      return this.predicate.test(pOwner);
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(this.memoryType);
   }
}