package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.server.ServerWorld;

public class SwimTask extends Task<MobEntity> {
   private final float chance;

   public SwimTask(float p_i231540_1_) {
      super(ImmutableMap.of());
      this.chance = p_i231540_1_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, MobEntity pOwner) {
      return pOwner.isInWater() && pOwner.getFluidHeight(FluidTags.WATER) > pOwner.getFluidJumpThreshold() || pOwner.isInLava();
   }

   protected boolean canStillUse(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      return this.checkExtraStartConditions(pLevel, pEntity);
   }

   protected void tick(ServerWorld pLevel, MobEntity pOwner, long pGameTime) {
      if (pOwner.getRandom().nextFloat() < this.chance) {
         pOwner.getJumpControl().jump();
      }

   }
}