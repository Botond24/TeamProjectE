package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class WorkTask extends Task<CreatureEntity> {
   private final MemoryModuleType<GlobalPos> memoryType;
   private long nextOkStartTime;
   private final int maxDistanceFromPoi;
   private float speedModifier;

   public WorkTask(MemoryModuleType<GlobalPos> p_i241909_1_, float p_i241909_2_, int p_i241909_3_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED, p_i241909_1_, MemoryModuleStatus.VALUE_PRESENT));
      this.memoryType = p_i241909_1_;
      this.speedModifier = p_i241909_2_;
      this.maxDistanceFromPoi = p_i241909_3_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, CreatureEntity pOwner) {
      Optional<GlobalPos> optional = pOwner.getBrain().getMemory(this.memoryType);
      return optional.isPresent() && pLevel.dimension() == optional.get().dimension() && optional.get().pos().closerThan(pOwner.position(), (double)this.maxDistanceFromPoi);
   }

   protected void start(ServerWorld pLevel, CreatureEntity pEntity, long pGameTime) {
      if (pGameTime > this.nextOkStartTime) {
         Optional<Vector3d> optional = Optional.ofNullable(RandomPositionGenerator.getLandPos(pEntity, 8, 6));
         pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map((p_220564_1_) -> {
            return new WalkTarget(p_220564_1_, this.speedModifier, 1);
         }));
         this.nextOkStartTime = pGameTime + 180L;
      }

   }
}