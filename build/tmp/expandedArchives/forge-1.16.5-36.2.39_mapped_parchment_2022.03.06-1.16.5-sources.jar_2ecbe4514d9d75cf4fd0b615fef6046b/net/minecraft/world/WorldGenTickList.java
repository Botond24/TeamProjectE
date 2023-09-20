package net.minecraft.world;

import java.util.function.Function;
import net.minecraft.util.math.BlockPos;

public class WorldGenTickList<T> implements ITickList<T> {
   private final Function<BlockPos, ITickList<T>> index;

   public WorldGenTickList(Function<BlockPos, ITickList<T>> p_i48981_1_) {
      this.index = p_i48981_1_;
   }

   public boolean hasScheduledTick(BlockPos pPos, T pItem) {
      return this.index.apply(pPos).hasScheduledTick(pPos, pItem);
   }

   public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority) {
      this.index.apply(pPos).scheduleTick(pPos, pItem, pScheduledTime, pPriority);
   }

   /**
    * Checks if this position/item is scheduled to be updated this tick
    */
   public boolean willTickThisTick(BlockPos pPos, T pObj) {
      return false;
   }
}