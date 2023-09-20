package net.minecraft.stats;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Stat<T> extends ScoreCriteria {
   private final IStatFormatter formatter;
   private final T value;
   private final StatType<T> type;

   protected Stat(StatType<T> pType, T pValue, IStatFormatter pFormatter) {
      super(buildName(pType, pValue));
      this.type = pType;
      this.formatter = pFormatter;
      this.value = pValue;
   }

   public static <T> String buildName(StatType<T> pType, T pValue) {
      return locationToKey(Registry.STAT_TYPE.getKey(pType)) + ":" + locationToKey(pType.getRegistry().getKey(pValue));
   }

   private static <T> String locationToKey(@Nullable ResourceLocation pId) {
      return pId.toString().replace(':', '.');
   }

   public StatType<T> getType() {
      return this.type;
   }

   public T getValue() {
      return this.value;
   }

   @OnlyIn(Dist.CLIENT)
   public String format(int pNumber) {
      return this.formatter.format(pNumber);
   }

   public boolean equals(Object p_equals_1_) {
      return this == p_equals_1_ || p_equals_1_ instanceof Stat && Objects.equals(this.getName(), ((Stat)p_equals_1_).getName());
   }

   public int hashCode() {
      return this.getName().hashCode();
   }

   public String toString() {
      return "Stat{name=" + this.getName() + ", formatter=" + this.formatter + '}';
   }
}