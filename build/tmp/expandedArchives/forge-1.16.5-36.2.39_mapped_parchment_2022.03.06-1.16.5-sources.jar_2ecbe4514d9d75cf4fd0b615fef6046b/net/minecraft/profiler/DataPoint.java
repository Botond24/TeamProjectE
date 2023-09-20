package net.minecraft.profiler;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class DataPoint implements Comparable<DataPoint> {
   public final double percentage;
   public final double globalPercentage;
   public final long count;
   public final String name;

   public DataPoint(String pName, double pPercentage, double pGlobalPercentage, long pCount) {
      this.name = pName;
      this.percentage = pPercentage;
      this.globalPercentage = pGlobalPercentage;
      this.count = pCount;
   }

   public int compareTo(DataPoint p_compareTo_1_) {
      if (p_compareTo_1_.percentage < this.percentage) {
         return -1;
      } else {
         return p_compareTo_1_.percentage > this.percentage ? 1 : p_compareTo_1_.name.compareTo(this.name);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public int getColor() {
      return (this.name.hashCode() & 11184810) + 4473924;
   }
}