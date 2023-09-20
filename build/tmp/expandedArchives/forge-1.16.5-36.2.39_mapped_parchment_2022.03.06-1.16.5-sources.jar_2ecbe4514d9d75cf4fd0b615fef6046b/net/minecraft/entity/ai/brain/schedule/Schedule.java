package net.minecraft.entity.ai.brain.schedule;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.util.registry.Registry;

public class Schedule extends net.minecraftforge.registries.ForgeRegistryEntry<Schedule> {
   public static final Schedule EMPTY = register("empty").changeActivityAt(0, Activity.IDLE).build();
   public static final Schedule SIMPLE = register("simple").changeActivityAt(5000, Activity.WORK).changeActivityAt(11000, Activity.REST).build();
   public static final Schedule VILLAGER_BABY = register("villager_baby").changeActivityAt(10, Activity.IDLE).changeActivityAt(3000, Activity.PLAY).changeActivityAt(6000, Activity.IDLE).changeActivityAt(10000, Activity.PLAY).changeActivityAt(12000, Activity.REST).build();
   public static final Schedule VILLAGER_DEFAULT = register("villager_default").changeActivityAt(10, Activity.IDLE).changeActivityAt(2000, Activity.WORK).changeActivityAt(9000, Activity.MEET).changeActivityAt(11000, Activity.IDLE).changeActivityAt(12000, Activity.REST).build();
   private final Map<Activity, ScheduleDuties> timelines = Maps.newHashMap();

   protected static ScheduleBuilder register(String pKey) {
      Schedule schedule = Registry.register(Registry.SCHEDULE, pKey, new Schedule());
      return new ScheduleBuilder(schedule);
   }

   protected void ensureTimelineExistsFor(Activity pActivity) {
      if (!this.timelines.containsKey(pActivity)) {
         this.timelines.put(pActivity, new ScheduleDuties());
      }

   }

   protected ScheduleDuties getTimelineFor(Activity pActivity) {
      return this.timelines.get(pActivity);
   }

   protected List<ScheduleDuties> getAllTimelinesExceptFor(Activity pActivity) {
      return this.timelines.entrySet().stream().filter((p_221378_1_) -> {
         return p_221378_1_.getKey() != pActivity;
      }).map(Entry::getValue).collect(Collectors.toList());
   }

   public Activity getActivityAt(int pDayTime) {
      return this.timelines.entrySet().stream().max(Comparator.comparingDouble((p_221376_1_) -> {
         return (double)p_221376_1_.getValue().getValueAt(pDayTime);
      })).map(Entry::getKey).orElse(Activity.IDLE);
   }
}
