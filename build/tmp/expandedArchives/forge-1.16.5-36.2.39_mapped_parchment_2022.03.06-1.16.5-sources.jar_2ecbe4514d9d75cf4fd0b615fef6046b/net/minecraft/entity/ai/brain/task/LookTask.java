package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public class LookTask extends Task<MobEntity> {
   public LookTask(int p_i50358_1_, int p_i50358_2_) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_PRESENT), p_i50358_1_, p_i50358_2_);
   }

   protected boolean canStillUse(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      return pEntity.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter((p_220485_1_) -> {
         return p_220485_1_.isVisibleBy(pEntity);
      }).isPresent();
   }

   protected void stop(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerWorld pLevel, MobEntity pOwner, long pGameTime) {
      pOwner.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((p_220484_1_) -> {
         pOwner.getLookControl().setLookAt(p_220484_1_.currentPosition());
      });
   }
}