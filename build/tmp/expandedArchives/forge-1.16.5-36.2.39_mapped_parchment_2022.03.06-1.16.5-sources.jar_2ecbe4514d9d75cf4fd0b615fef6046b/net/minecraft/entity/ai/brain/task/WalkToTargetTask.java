package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class WalkToTargetTask extends Task<MobEntity> {
   private int remainingCooldown;
   @Nullable
   private Path path;
   @Nullable
   private BlockPos lastTargetPos;
   private float speedModifier;

   public WalkToTargetTask() {
      this(150, 250);
   }

   public WalkToTargetTask(int p_i241908_1_, int p_i241908_2_) {
      super(ImmutableMap.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleStatus.REGISTERED, MemoryModuleType.PATH, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_PRESENT), p_i241908_1_, p_i241908_2_);
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, MobEntity pOwner) {
      if (this.remainingCooldown > 0) {
         --this.remainingCooldown;
         return false;
      } else {
         Brain<?> brain = pOwner.getBrain();
         WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
         boolean flag = this.reachedTarget(pOwner, walktarget);
         if (!flag && this.tryComputePath(pOwner, walktarget, pLevel.getGameTime())) {
            this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
            return true;
         } else {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            if (flag) {
               brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            }

            return false;
         }
      }
   }

   protected boolean canStillUse(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      if (this.path != null && this.lastTargetPos != null) {
         Optional<WalkTarget> optional = pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
         PathNavigator pathnavigator = pEntity.getNavigation();
         return !pathnavigator.isDone() && optional.isPresent() && !this.reachedTarget(pEntity, optional.get());
      } else {
         return false;
      }
   }

   protected void stop(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      if (pEntity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && !this.reachedTarget(pEntity, pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get()) && pEntity.getNavigation().isStuck()) {
         this.remainingCooldown = pLevel.getRandom().nextInt(40);
      }

      pEntity.getNavigation().stop();
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.PATH);
      this.path = null;
   }

   protected void start(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      pEntity.getBrain().setMemory(MemoryModuleType.PATH, this.path);
      pEntity.getNavigation().moveTo(this.path, (double)this.speedModifier);
   }

   protected void tick(ServerWorld pLevel, MobEntity pOwner, long pGameTime) {
      Path path = pOwner.getNavigation().getPath();
      Brain<?> brain = pOwner.getBrain();
      if (this.path != path) {
         this.path = path;
         brain.setMemory(MemoryModuleType.PATH, path);
      }

      if (path != null && this.lastTargetPos != null) {
         WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
         if (walktarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0D && this.tryComputePath(pOwner, walktarget, pLevel.getGameTime())) {
            this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
            this.start(pLevel, pOwner, pGameTime);
         }

      }
   }

   private boolean tryComputePath(MobEntity p_220487_1_, WalkTarget p_220487_2_, long p_220487_3_) {
      BlockPos blockpos = p_220487_2_.getTarget().currentBlockPosition();
      this.path = p_220487_1_.getNavigation().createPath(blockpos, 0);
      this.speedModifier = p_220487_2_.getSpeedModifier();
      Brain<?> brain = p_220487_1_.getBrain();
      if (this.reachedTarget(p_220487_1_, p_220487_2_)) {
         brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      } else {
         boolean flag = this.path != null && this.path.canReach();
         if (flag) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, p_220487_3_);
         }

         if (this.path != null) {
            return true;
         }

         Vector3d vector3d = RandomPositionGenerator.getPosTowards((CreatureEntity)p_220487_1_, 10, 7, Vector3d.atBottomCenterOf(blockpos));
         if (vector3d != null) {
            this.path = p_220487_1_.getNavigation().createPath(vector3d.x, vector3d.y, vector3d.z, 0);
            return this.path != null;
         }
      }

      return false;
   }

   private boolean reachedTarget(MobEntity p_220486_1_, WalkTarget p_220486_2_) {
      return p_220486_2_.getTarget().currentBlockPosition().distManhattan(p_220486_1_.blockPosition()) <= p_220486_2_.getCloseEnoughDist();
   }
}