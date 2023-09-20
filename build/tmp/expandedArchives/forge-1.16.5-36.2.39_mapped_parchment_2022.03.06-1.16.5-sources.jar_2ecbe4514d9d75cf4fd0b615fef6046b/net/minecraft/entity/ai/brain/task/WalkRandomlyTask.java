package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class WalkRandomlyTask extends Task<CreatureEntity> {
   private final float speedModifier;
   private final int maxHorizontalDistance;
   private final int maxVerticalDistance;

   public WalkRandomlyTask(float p_i231526_1_) {
      this(p_i231526_1_, 10, 7);
   }

   public WalkRandomlyTask(float p_i231527_1_, int p_i231527_2_, int p_i231527_3_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
      this.speedModifier = p_i231527_1_;
      this.maxHorizontalDistance = p_i231527_2_;
      this.maxVerticalDistance = p_i231527_3_;
   }

   protected void start(ServerWorld pLevel, CreatureEntity pEntity, long pGameTime) {
      Optional<Vector3d> optional = Optional.ofNullable(RandomPositionGenerator.getLandPos(pEntity, this.maxHorizontalDistance, this.maxVerticalDistance));
      pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map((p_233939_1_) -> {
         return new WalkTarget(p_233939_1_, this.speedModifier, 0);
      }));
   }
}