package net.minecraft.world;

import net.minecraft.util.math.BlockPos;

public interface ITickList<T> {
   boolean hasScheduledTick(BlockPos pPos, T pItem);

   default void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime) {
      this.scheduleTick(pPos, pItem, pScheduledTime, TickPriority.NORMAL);
   }

   void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority);

   /**
    * Checks if this position/item is scheduled to be updated this tick
    */
   boolean willTickThisTick(BlockPos pPos, T pObj);
}