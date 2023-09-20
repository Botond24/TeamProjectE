package net.minecraft.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.world.server.ServerWorld;

public class StartHuntTask<E extends PiglinEntity> extends Task<E> {
   public StartHuntTask() {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.ANGRY_AT, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleStatus.REGISTERED));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, PiglinEntity pOwner) {
      return !pOwner.isBaby() && !PiglinTasks.hasAnyoneNearbyHuntedRecently(pOwner);
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      HoglinEntity hoglinentity = pEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN).get();
      PiglinTasks.setAngerTarget(pEntity, hoglinentity);
      PiglinTasks.dontKillAnyMoreHoglinsForAWhile(pEntity);
      PiglinTasks.broadcastAngerTarget(pEntity, hoglinentity);
      PiglinTasks.broadcastDontKillAnyMoreHoglinsForAWhile(pEntity);
   }
}