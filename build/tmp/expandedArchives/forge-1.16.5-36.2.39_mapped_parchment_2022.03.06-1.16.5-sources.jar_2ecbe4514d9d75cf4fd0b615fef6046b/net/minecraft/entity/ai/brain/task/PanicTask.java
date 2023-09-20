package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.server.ServerWorld;

public class PanicTask extends Task<VillagerEntity> {
   public PanicTask() {
      super(ImmutableMap.of());
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return isHurt(pEntity) || hasHostile(pEntity);
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      if (isHurt(pEntity) || hasHostile(pEntity)) {
         Brain<?> brain = pEntity.getBrain();
         if (!brain.isActive(Activity.PANIC)) {
            brain.eraseMemory(MemoryModuleType.PATH);
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
            brain.eraseMemory(MemoryModuleType.BREED_TARGET);
            brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
         }

         brain.setActiveActivityIfPossible(Activity.PANIC);
      }

   }

   protected void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      if (pGameTime % 100L == 0L) {
         pOwner.spawnGolemIfNeeded(pLevel, pGameTime, 3);
      }

   }

   public static boolean hasHostile(LivingEntity pEntity) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE);
   }

   public static boolean isHurt(LivingEntity pEntity) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
   }
}