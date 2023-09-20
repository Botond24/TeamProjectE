package net.minecraft.stats;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StatisticsManager {
   protected final Object2IntMap<Stat<?>> stats = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());

   public StatisticsManager() {
      this.stats.defaultReturnValue(0);
   }

   public void increment(PlayerEntity pPlayer, Stat<?> pStat, int pAmount) {
      int i = (int)Math.min((long)this.getValue(pStat) + (long)pAmount, 2147483647L);
      this.setValue(pPlayer, pStat, i);
   }

   /**
    * Triggers the logging of an achievement and attempts to announce to server
    */
   public void setValue(PlayerEntity pPlayer, Stat<?> pStat, int pValue) {
      this.stats.put(pStat, pValue);
   }

   @OnlyIn(Dist.CLIENT)
   public <T> int getValue(StatType<T> pType, T p_199060_2_) {
      return pType.contains(p_199060_2_) ? this.getValue(pType.get(p_199060_2_)) : 0;
   }

   /**
    * Reads the given stat and returns its value as an int.
    */
   public int getValue(Stat<?> pStat) {
      return this.stats.getInt(pStat);
   }
}