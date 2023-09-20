package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public class RandomlyStopAttackingTask extends Task<LivingEntity> {
   private final int pacifyDuration;

   public RandomlyStopAttackingTask(MemoryModuleType<?> p_i231510_1_, int p_i231510_2_) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.PACIFIED, MemoryModuleStatus.VALUE_ABSENT, p_i231510_1_, MemoryModuleStatus.VALUE_PRESENT));
      this.pacifyDuration = p_i231510_2_;
   }

   protected void start(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.PACIFIED, true, (long)this.pacifyDuration);
      pEntity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
   }
}