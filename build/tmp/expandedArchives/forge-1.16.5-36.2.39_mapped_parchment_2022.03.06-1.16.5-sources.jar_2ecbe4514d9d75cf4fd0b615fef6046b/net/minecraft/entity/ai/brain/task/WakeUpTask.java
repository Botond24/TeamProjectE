package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.world.server.ServerWorld;

public class WakeUpTask extends Task<LivingEntity> {
   public WakeUpTask() {
      super(ImmutableMap.of());
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, LivingEntity pOwner) {
      return !pOwner.getBrain().isActive(Activity.REST) && pOwner.isSleeping();
   }

   protected void start(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      pEntity.stopSleeping();
   }
}