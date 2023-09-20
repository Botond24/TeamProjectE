package net.minecraft.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.Items;
import net.minecraft.world.server.ServerWorld;

public class StartAdmiringItemTask<E extends PiglinEntity> extends Task<E> {
   public StartAdmiringItemTask() {
      super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryModuleStatus.VALUE_ABSENT));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      return !pOwner.getOffhandItem().isEmpty() && !pOwner.getOffhandItem().isShield(pOwner);
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      PiglinTasks.stopHoldingOffHandItem(pEntity, true);
   }
}
