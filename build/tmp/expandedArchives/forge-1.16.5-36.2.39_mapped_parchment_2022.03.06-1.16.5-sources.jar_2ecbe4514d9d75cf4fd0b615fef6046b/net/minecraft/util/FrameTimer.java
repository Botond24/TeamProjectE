package net.minecraft.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FrameTimer {
   /** An array with the last 240 frames */
   private final long[] loggedTimes = new long[240];
   /** The last index used when 240 frames have been set */
   private int logStart;
   /** A counter */
   private int logLength;
   /** The next index to use in the array */
   private int logEnd;

   /**
    * Add a frame at the next index in the array frames
    */
   public void logFrameDuration(long pRunningTime) {
      this.loggedTimes[this.logEnd] = pRunningTime;
      ++this.logEnd;
      if (this.logEnd == 240) {
         this.logEnd = 0;
      }

      if (this.logLength < 240) {
         this.logStart = 0;
         ++this.logLength;
      } else {
         this.logStart = this.wrapIndex(this.logEnd + 1);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public int scaleSampleTo(long pValue, int pScale, int pDivisor) {
      double d0 = (double)pValue / (double)(1000000000L / (long)pDivisor);
      return (int)(d0 * (double)pScale);
   }

   /**
    * Return the last index used when 240 frames have been set
    */
   @OnlyIn(Dist.CLIENT)
   public int getLogStart() {
      return this.logStart;
   }

   /**
    * Return the index of the next frame in the array
    */
   @OnlyIn(Dist.CLIENT)
   public int getLogEnd() {
      return this.logEnd;
   }

   /**
    * Change 240 to 0
    */
   public int wrapIndex(int pRawIndex) {
      return pRawIndex % 240;
   }

   /**
    * Return the array of frames
    */
   @OnlyIn(Dist.CLIENT)
   public long[] getLog() {
      return this.loggedTimes;
   }
}