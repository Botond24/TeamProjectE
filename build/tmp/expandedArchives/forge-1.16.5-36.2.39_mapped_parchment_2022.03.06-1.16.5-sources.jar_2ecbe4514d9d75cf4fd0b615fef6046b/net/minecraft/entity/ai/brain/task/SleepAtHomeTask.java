package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class SleepAtHomeTask extends Task<LivingEntity> {
   private long nextOkStartTime;

   public SleepAtHomeTask() {
      super(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryModuleStatus.REGISTERED));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, LivingEntity pOwner) {
      if (pOwner.isPassenger()) {
         return false;
      } else {
         Brain<?> brain = pOwner.getBrain();
         GlobalPos globalpos = brain.getMemory(MemoryModuleType.HOME).get();
         if (pLevel.dimension() != globalpos.dimension()) {
            return false;
         } else {
            Optional<Long> optional = brain.getMemory(MemoryModuleType.LAST_WOKEN);
            if (optional.isPresent()) {
               long i = pLevel.getGameTime() - optional.get();
               if (i > 0L && i < 100L) {
                  return false;
               }
            }

            BlockState blockstate = pLevel.getBlockState(globalpos.pos());
            return globalpos.pos().closerThan(pOwner.position(), 2.0D) && blockstate.getBlock().is(BlockTags.BEDS) && !blockstate.getValue(BedBlock.OCCUPIED);
         }
      }
   }

   protected boolean canStillUse(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      Optional<GlobalPos> optional = pEntity.getBrain().getMemory(MemoryModuleType.HOME);
      if (!optional.isPresent()) {
         return false;
      } else {
         BlockPos blockpos = optional.get().pos();
         return pEntity.getBrain().isActive(Activity.REST) && pEntity.getY() > (double)blockpos.getY() + 0.4D && blockpos.closerThan(pEntity.position(), 1.14D);
      }
   }

   protected void start(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      if (pGameTime > this.nextOkStartTime) {
         InteractWithDoorTask.closeDoorsThatIHaveOpenedOrPassedThrough(pLevel, pEntity, (PathPoint)null, (PathPoint)null);
         pEntity.startSleeping(pEntity.getBrain().getMemory(MemoryModuleType.HOME).get().pos());
      }

   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   protected void stop(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      if (pEntity.isSleeping()) {
         pEntity.stopSleeping();
         this.nextOkStartTime = pGameTime + 40L;
      }

   }
}