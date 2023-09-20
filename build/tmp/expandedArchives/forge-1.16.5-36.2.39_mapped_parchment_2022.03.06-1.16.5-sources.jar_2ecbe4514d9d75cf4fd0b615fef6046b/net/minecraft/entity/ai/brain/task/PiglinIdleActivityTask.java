package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.RangedInteger;
import net.minecraft.world.server.ServerWorld;

public class PiglinIdleActivityTask<E extends MobEntity, T> extends Task<E> {
   private final Predicate<E> predicate;
   private final MemoryModuleType<? extends T> sourceMemory;
   private final MemoryModuleType<T> targetMemory;
   private final RangedInteger durationOfCopy;

   public PiglinIdleActivityTask(Predicate<E> p_i231513_1_, MemoryModuleType<? extends T> p_i231513_2_, MemoryModuleType<T> p_i231513_3_, RangedInteger p_i231513_4_) {
      super(ImmutableMap.of(p_i231513_2_, MemoryModuleStatus.VALUE_PRESENT, p_i231513_3_, MemoryModuleStatus.VALUE_ABSENT));
      this.predicate = p_i231513_1_;
      this.sourceMemory = p_i231513_2_;
      this.targetMemory = p_i231513_3_;
      this.durationOfCopy = p_i231513_4_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      return this.predicate.test(pOwner);
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      brain.setMemoryWithExpiry(this.targetMemory, brain.getMemory(this.sourceMemory).get(), (long)this.durationOfCopy.randomValue(pLevel.random));
   }
}