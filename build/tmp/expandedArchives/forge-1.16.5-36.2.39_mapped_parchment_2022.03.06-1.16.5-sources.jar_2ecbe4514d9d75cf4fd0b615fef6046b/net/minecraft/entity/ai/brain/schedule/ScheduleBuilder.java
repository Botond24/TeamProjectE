package net.minecraft.entity.ai.brain.schedule;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleBuilder {
   private final Schedule schedule;
   private final List<ScheduleBuilder.ActivityEntry> transitions = Lists.newArrayList();

   public ScheduleBuilder(Schedule pSchedule) {
      this.schedule = pSchedule;
   }

   public ScheduleBuilder changeActivityAt(int pDuration, Activity pActivity) {
      this.transitions.add(new ScheduleBuilder.ActivityEntry(pDuration, pActivity));
      return this;
   }

   public Schedule build() {
      this.transitions.stream().map(ScheduleBuilder.ActivityEntry::getActivity).collect(Collectors.toSet()).forEach(this.schedule::ensureTimelineExistsFor);
      this.transitions.forEach((p_221405_1_) -> {
         Activity activity = p_221405_1_.getActivity();
         this.schedule.getAllTimelinesExceptFor(activity).forEach((p_221403_1_) -> {
            p_221403_1_.addKeyframe(p_221405_1_.getTime(), 0.0F);
         });
         this.schedule.getTimelineFor(activity).addKeyframe(p_221405_1_.getTime(), 1.0F);
      });
      return this.schedule;
   }

   static class ActivityEntry {
      private final int time;
      private final Activity activity;

      public ActivityEntry(int pTime, Activity pActivity) {
         this.time = pTime;
         this.activity = pActivity;
      }

      public int getTime() {
         return this.time;
      }

      public Activity getActivity() {
         return this.activity;
      }
   }
}