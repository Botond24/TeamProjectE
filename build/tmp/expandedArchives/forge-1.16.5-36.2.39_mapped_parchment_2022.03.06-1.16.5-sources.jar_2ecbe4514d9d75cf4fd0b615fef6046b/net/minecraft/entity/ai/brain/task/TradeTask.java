package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.world.server.ServerWorld;

public class TradeTask extends Task<VillagerEntity> {
   private final float speedModifier;

   public TradeTask(float p_i50359_1_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED), Integer.MAX_VALUE);
      this.speedModifier = p_i50359_1_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      PlayerEntity playerentity = pOwner.getTradingPlayer();
      return pOwner.isAlive() && playerentity != null && !pOwner.isInWater() && !pOwner.hurtMarked && pOwner.distanceToSqr(playerentity) <= 16.0D && playerentity.containerMenu != null;
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return this.checkExtraStartConditions(pLevel, pEntity);
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      this.followPlayer(pEntity);
   }

   protected void stop(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      this.followPlayer(pOwner);
   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   private void followPlayer(VillagerEntity pOwner) {
      Brain<?> brain = pOwner.getBrain();
      brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityPosWrapper(pOwner.getTradingPlayer(), false), this.speedModifier, 2));
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(pOwner.getTradingPlayer(), true));
   }
}