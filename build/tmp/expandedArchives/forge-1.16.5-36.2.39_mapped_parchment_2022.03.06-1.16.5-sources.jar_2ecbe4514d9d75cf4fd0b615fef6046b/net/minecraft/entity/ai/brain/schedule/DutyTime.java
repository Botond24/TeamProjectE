package net.minecraft.entity.ai.brain.schedule;

public class DutyTime {
   private final int timeStamp;
   private final float value;

   public DutyTime(int pTimeStamp, float pValue) {
      this.timeStamp = pTimeStamp;
      this.value = pValue;
   }

   public int getTimeStamp() {
      return this.timeStamp;
   }

   public float getValue() {
      return this.value;
   }
}