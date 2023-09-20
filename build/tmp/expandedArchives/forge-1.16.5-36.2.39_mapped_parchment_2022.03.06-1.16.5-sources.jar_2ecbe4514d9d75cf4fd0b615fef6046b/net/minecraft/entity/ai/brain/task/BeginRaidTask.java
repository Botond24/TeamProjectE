package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;

public class BeginRaidTask extends Task<LivingEntity> {
   public BeginRaidTask() {
      super(ImmutableMap.of());
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, LivingEntity pOwner) {
      return pLevel.random.nextInt(20) == 0;
   }

   protected void start(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      Raid raid = pLevel.getRaidAt(pEntity.blockPosition());
      if (raid != null) {
         if (raid.hasFirstWaveSpawned() && !raid.isBetweenWaves()) {
            brain.setDefaultActivity(Activity.RAID);
            brain.setActiveActivityIfPossible(Activity.RAID);
         } else {
            brain.setDefaultActivity(Activity.PRE_RAID);
            brain.setActiveActivityIfPossible(Activity.PRE_RAID);
         }
      }

   }
}