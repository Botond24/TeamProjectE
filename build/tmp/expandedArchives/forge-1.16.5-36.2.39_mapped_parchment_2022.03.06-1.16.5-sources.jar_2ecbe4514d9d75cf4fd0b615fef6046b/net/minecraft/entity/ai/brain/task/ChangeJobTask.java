package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.world.server.ServerWorld;

public class ChangeJobTask extends Task<VillagerEntity> {
   public ChangeJobTask() {
      super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_ABSENT));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      VillagerData villagerdata = pOwner.getVillagerData();
      return villagerdata.getProfession() != VillagerProfession.NONE && villagerdata.getProfession() != VillagerProfession.NITWIT && pOwner.getVillagerXp() == 0 && villagerdata.getLevel() <= 1;
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      pEntity.setVillagerData(pEntity.getVillagerData().setProfession(VillagerProfession.NONE));
      pEntity.refreshBrain(pLevel);
   }
}