package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.world.server.ServerWorld;

public class FindInteractionAndLookTargetTask extends Task<LivingEntity> {
   private final EntityType<?> type;
   private final int interactionRangeSqr;
   private final Predicate<LivingEntity> targetFilter;
   private final Predicate<LivingEntity> selfFilter;

   public FindInteractionAndLookTargetTask(EntityType<?> p_i50347_1_, int p_i50347_2_, Predicate<LivingEntity> p_i50347_3_, Predicate<LivingEntity> p_i50347_4_) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT));
      this.type = p_i50347_1_;
      this.interactionRangeSqr = p_i50347_2_ * p_i50347_2_;
      this.targetFilter = p_i50347_4_;
      this.selfFilter = p_i50347_3_;
   }

   public FindInteractionAndLookTargetTask(EntityType<?> p_i50348_1_, int p_i50348_2_) {
      this(p_i50348_1_, p_i50348_2_, (p_220528_0_) -> {
         return true;
      }, (p_220531_0_) -> {
         return true;
      });
   }

   public boolean checkExtraStartConditions(ServerWorld pLevel, LivingEntity pOwner) {
      return this.selfFilter.test(pOwner) && this.getVisibleEntities(pOwner).stream().anyMatch(this::isMatchingTarget);
   }

   public void start(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      super.start(pLevel, pEntity, pGameTime);
      Brain<?> brain = pEntity.getBrain();
      brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).ifPresent((p_220526_3_) -> {
         p_220526_3_.stream().filter((p_220529_2_) -> {
            return p_220529_2_.distanceToSqr(pEntity) <= (double)this.interactionRangeSqr;
         }).filter(this::isMatchingTarget).findFirst().ifPresent((p_220527_1_) -> {
            brain.setMemory(MemoryModuleType.INTERACTION_TARGET, p_220527_1_);
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(p_220527_1_, true));
         });
      });
   }

   private boolean isMatchingTarget(LivingEntity p_220532_1_) {
      return this.type.equals(p_220532_1_.getType()) && this.targetFilter.test(p_220532_1_);
   }

   private List<LivingEntity> getVisibleEntities(LivingEntity pLivingEntity) {
      return pLivingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get();
   }
}