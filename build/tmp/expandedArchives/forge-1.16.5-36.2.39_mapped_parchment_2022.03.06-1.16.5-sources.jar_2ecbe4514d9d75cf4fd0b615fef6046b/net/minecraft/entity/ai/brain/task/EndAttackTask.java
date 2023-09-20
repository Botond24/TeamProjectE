package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.function.BiPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public class EndAttackTask extends Task<LivingEntity> {
   private final int celebrateDuration;
   private final BiPredicate<LivingEntity, LivingEntity> dancePredicate;

   public EndAttackTask(int p_i231538_1_, BiPredicate<LivingEntity, LivingEntity> p_i231538_2_) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.ANGRY_AT, MemoryModuleStatus.REGISTERED, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.DANCING, MemoryModuleStatus.REGISTERED));
      this.celebrateDuration = p_i231538_1_;
      this.dancePredicate = p_i231538_2_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, LivingEntity pOwner) {
      return this.getAttackTarget(pOwner).isDeadOrDying();
   }

   protected void start(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      LivingEntity livingentity = this.getAttackTarget(pEntity);
      if (this.dancePredicate.test(pEntity, livingentity)) {
         pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.DANCING, true, (long)this.celebrateDuration);
      }

      pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.CELEBRATE_LOCATION, livingentity.blockPosition(), (long)this.celebrateDuration);
      if (livingentity.getType() != EntityType.PLAYER || pLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
         pEntity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
         pEntity.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
      }

   }

   private LivingEntity getAttackTarget(LivingEntity pLivingEntity) {
      return pLivingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }
}