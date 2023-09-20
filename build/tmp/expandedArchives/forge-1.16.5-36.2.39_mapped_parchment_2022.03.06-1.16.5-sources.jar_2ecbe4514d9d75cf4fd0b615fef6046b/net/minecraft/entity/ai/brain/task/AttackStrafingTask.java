package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public class AttackStrafingTask<E extends MobEntity> extends Task<E> {
   private final int tooCloseDistance;
   private final float strafeSpeed;

   public AttackStrafingTask(int p_i231509_1_, float p_i231509_2_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT));
      this.tooCloseDistance = p_i231509_1_;
      this.strafeSpeed = p_i231509_2_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      return this.isTargetVisible(pOwner) && this.isTargetTooClose(pOwner);
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(this.getTarget(pEntity), true));
      pEntity.getMoveControl().strafe(-this.strafeSpeed, 0.0F);
      pEntity.yRot = MathHelper.rotateIfNecessary(pEntity.yRot, pEntity.yHeadRot, 0.0F);
   }

   private boolean isTargetVisible(E pMob) {
      return pMob.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().contains(this.getTarget(pMob));
   }

   private boolean isTargetTooClose(E pMob) {
      return this.getTarget(pMob).closerThan(pMob, (double)this.tooCloseDistance);
   }

   private LivingEntity getTarget(E pMob) {
      return pMob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }
}