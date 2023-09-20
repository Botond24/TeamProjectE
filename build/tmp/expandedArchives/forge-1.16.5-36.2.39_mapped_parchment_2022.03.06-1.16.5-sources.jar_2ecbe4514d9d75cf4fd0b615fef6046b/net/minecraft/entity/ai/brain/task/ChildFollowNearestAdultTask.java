package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.RangedInteger;
import net.minecraft.world.server.ServerWorld;

public class ChildFollowNearestAdultTask<E extends AgeableEntity> extends Task<E> {
   private final RangedInteger followRange;
   private final float speedModifier;

   public ChildFollowNearestAdultTask(RangedInteger p_i231508_1_, float p_i231508_2_) {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
      this.followRange = p_i231508_1_;
      this.speedModifier = p_i231508_2_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      if (!pOwner.isBaby()) {
         return false;
      } else {
         AgeableEntity ageableentity = this.getNearestAdult(pOwner);
         return pOwner.closerThan(ageableentity, (double)(this.followRange.getMaxInclusive() + 1)) && !pOwner.closerThan(ageableentity, (double)this.followRange.getMinInclusive());
      }
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      BrainUtil.setWalkAndLookTargetMemories(pEntity, this.getNearestAdult(pEntity), this.speedModifier, this.followRange.getMinInclusive() - 1);
   }

   private AgeableEntity getNearestAdult(E p_233852_1_) {
      return p_233852_1_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT).get();
   }
}