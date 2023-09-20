package net.minecraft.entity.ai.brain.task;

import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public abstract class Task<E extends LivingEntity> {
   protected final Map<MemoryModuleType<?>, MemoryModuleStatus> entryCondition;
   private Task.Status status = Task.Status.STOPPED;
   private long endTimestamp;
   private final int minDuration;
   private final int maxDuration;

   public Task(Map<MemoryModuleType<?>, MemoryModuleStatus> p_i51504_1_) {
      this(p_i51504_1_, 60);
   }

   public Task(Map<MemoryModuleType<?>, MemoryModuleStatus> p_i51505_1_, int p_i51505_2_) {
      this(p_i51505_1_, p_i51505_2_, p_i51505_2_);
   }

   public Task(Map<MemoryModuleType<?>, MemoryModuleStatus> p_i51506_1_, int p_i51506_2_, int p_i51506_3_) {
      this.minDuration = p_i51506_2_;
      this.maxDuration = p_i51506_3_;
      this.entryCondition = p_i51506_1_;
   }

   public Task.Status getStatus() {
      return this.status;
   }

   public final boolean tryStart(ServerWorld pLevel, E pOwner, long pGameTime) {
      if (this.hasRequiredMemories(pOwner) && this.checkExtraStartConditions(pLevel, pOwner)) {
         this.status = Task.Status.RUNNING;
         int i = this.minDuration + pLevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
         this.endTimestamp = pGameTime + (long)i;
         this.start(pLevel, pOwner, pGameTime);
         return true;
      } else {
         return false;
      }
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
   }

   public final void tickOrStop(ServerWorld pLevel, E pEntity, long pGameTime) {
      if (!this.timedOut(pGameTime) && this.canStillUse(pLevel, pEntity, pGameTime)) {
         this.tick(pLevel, pEntity, pGameTime);
      } else {
         this.doStop(pLevel, pEntity, pGameTime);
      }

   }

   protected void tick(ServerWorld pLevel, E pOwner, long pGameTime) {
   }

   public final void doStop(ServerWorld pLevel, E pEntity, long pGameTime) {
      this.status = Task.Status.STOPPED;
      this.stop(pLevel, pEntity, pGameTime);
   }

   protected void stop(ServerWorld pLevel, E pEntity, long pGameTime) {
   }

   protected boolean canStillUse(ServerWorld pLevel, E pEntity, long pGameTime) {
      return false;
   }

   protected boolean timedOut(long pGameTime) {
      return pGameTime > this.endTimestamp;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      return true;
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }

   private boolean hasRequiredMemories(E pOwner) {
      for(Entry<MemoryModuleType<?>, MemoryModuleStatus> entry : this.entryCondition.entrySet()) {
         MemoryModuleType<?> memorymoduletype = entry.getKey();
         MemoryModuleStatus memorymodulestatus = entry.getValue();
         if (!pOwner.getBrain().checkMemory(memorymoduletype, memorymodulestatus)) {
            return false;
         }
      }

      return true;
   }

   public static enum Status {
      STOPPED,
      RUNNING;
   }
}