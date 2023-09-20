package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.server.ServerWorld;

public class WalkToPOITask extends Task<VillagerEntity> {
   private final float speedModifier;
   private final int closeEnoughDistance;

   public WalkToPOITask(float p_i51557_1_, int p_i51557_2_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
      this.speedModifier = p_i51557_1_;
      this.closeEnoughDistance = p_i51557_2_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      return !pLevel.isVillage(pOwner.blockPosition());
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      PointOfInterestManager pointofinterestmanager = pLevel.getPoiManager();
      int i = pointofinterestmanager.sectionsToVillage(SectionPos.of(pEntity.blockPosition()));
      Vector3d vector3d = null;

      for(int j = 0; j < 5; ++j) {
         Vector3d vector3d1 = RandomPositionGenerator.getLandPos(pEntity, 15, 7, (p_225444_1_) -> {
            return (double)(-pLevel.sectionsToVillage(SectionPos.of(p_225444_1_)));
         });
         if (vector3d1 != null) {
            int k = pointofinterestmanager.sectionsToVillage(SectionPos.of(new BlockPos(vector3d1)));
            if (k < i) {
               vector3d = vector3d1;
               break;
            }

            if (k == i) {
               vector3d = vector3d1;
            }
         }
      }

      if (vector3d != null) {
         pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vector3d, this.speedModifier, this.closeEnoughDistance));
      }

   }
}