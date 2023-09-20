package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;

public class AttackTargetTask extends Task<MobEntity> {
   private final int cooldownBetweenAttacks;

   public AttackTargetTask(int p_i231523_1_) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleStatus.VALUE_ABSENT));
      this.cooldownBetweenAttacks = p_i231523_1_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, MobEntity pOwner) {
      LivingEntity livingentity = this.getAttackTarget(pOwner);
      return !this.isHoldingUsableProjectileWeapon(pOwner) && BrainUtil.canSee(pOwner, livingentity) && BrainUtil.isWithinMeleeAttackRange(pOwner, livingentity);
   }

   private boolean isHoldingUsableProjectileWeapon(MobEntity pMob) {
      return pMob.isHolding((p_233922_1_) -> {
         return p_233922_1_ instanceof ShootableItem && pMob.canFireProjectileWeapon((ShootableItem)p_233922_1_);
      });
   }

   protected void start(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      LivingEntity livingentity = this.getAttackTarget(pEntity);
      BrainUtil.lookAtEntity(pEntity, livingentity);
      pEntity.swing(Hand.MAIN_HAND);
      pEntity.doHurtTarget(livingentity);
      pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)this.cooldownBetweenAttacks);
   }

   private LivingEntity getAttackTarget(MobEntity pMob) {
      return pMob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }
}