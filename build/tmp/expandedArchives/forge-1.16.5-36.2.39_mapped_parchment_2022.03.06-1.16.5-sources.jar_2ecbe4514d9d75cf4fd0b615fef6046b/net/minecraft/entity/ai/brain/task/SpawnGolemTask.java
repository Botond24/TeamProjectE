package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class SpawnGolemTask extends Task<VillagerEntity> {
   private long lastCheck;

   public SpawnGolemTask() {
      super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      if (pLevel.getGameTime() - this.lastCheck < 300L) {
         return false;
      } else if (pLevel.random.nextInt(2) != 0) {
         return false;
      } else {
         this.lastCheck = pLevel.getGameTime();
         GlobalPos globalpos = pOwner.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
         return globalpos.dimension() == pLevel.dimension() && globalpos.pos().closerThan(pOwner.position(), 1.73D);
      }
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      Brain<VillagerEntity> brain = pEntity.getBrain();
      brain.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, pGameTime);
      brain.getMemory(MemoryModuleType.JOB_SITE).ifPresent((p_225460_1_) -> {
         brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(p_225460_1_.pos()));
      });
      pEntity.playWorkSound();
      this.useWorkstation(pLevel, pEntity);
      if (pEntity.shouldRestock()) {
         pEntity.restock();
      }

   }

   protected void useWorkstation(ServerWorld pLevel, VillagerEntity pVillager) {
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      Optional<GlobalPos> optional = pEntity.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      if (!optional.isPresent()) {
         return false;
      } else {
         GlobalPos globalpos = optional.get();
         return globalpos.dimension() == pLevel.dimension() && globalpos.pos().closerThan(pEntity.position(), 1.73D);
      }
   }
}