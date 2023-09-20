package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.server.ServerWorld;

public class ClearHurtTask extends Task<VillagerEntity> {
   public ClearHurtTask() {
      super(ImmutableMap.of());
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      boolean flag = PanicTask.isHurt(pEntity) || PanicTask.hasHostile(pEntity) || isCloseToEntityThatHurtMe(pEntity);
      if (!flag) {
         pEntity.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
         pEntity.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
         pEntity.getBrain().updateActivityFromSchedule(pLevel.getDayTime(), pLevel.getGameTime());
      }

   }

   private static boolean isCloseToEntityThatHurtMe(VillagerEntity pVillager) {
      return pVillager.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).filter((p_223523_1_) -> {
         return p_223523_1_.distanceToSqr(pVillager) <= 36.0D;
      }).isPresent();
   }
}