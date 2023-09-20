package net.minecraft.client.resources.data;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationFrame {
   private final int index;
   private final int time;

   public AnimationFrame(int pIndex) {
      this(pIndex, -1);
   }

   public AnimationFrame(int pIndex, int pTime) {
      this.index = pIndex;
      this.time = pTime;
   }

   public boolean isTimeUnknown() {
      return this.time == -1;
   }

   public int getTime() {
      return this.time;
   }

   public int getIndex() {
      return this.index;
   }
}