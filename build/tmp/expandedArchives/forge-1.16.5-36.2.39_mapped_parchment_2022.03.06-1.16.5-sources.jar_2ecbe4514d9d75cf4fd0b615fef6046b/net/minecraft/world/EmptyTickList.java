package net.minecraft.world;

import net.minecraft.util.math.BlockPos;

public class EmptyTickList<T> implements ITickList<T> {
   private static final EmptyTickList<Object> INSTANCE = new EmptyTickList<>();

   public static <T> EmptyTickList<T> empty() {
      return (EmptyTickList<T>) INSTANCE;
   }

   public boolean hasScheduledTick(BlockPos pPos, T pItem) {
      return false;
   }

   public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime) {
   }

   public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority) {
   }

   /**
    * Checks if this position/item is scheduled to be updated this tick
    */
   public boolean willTickThisTick(BlockPos pPos, T pObj) {
      return false;
   }
}