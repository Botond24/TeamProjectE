package net.minecraft.profiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiler implements IResultableProfiler {
   private static final long WARNING_TIME_NANOS = Duration.ofMillis(100L).toNanos();
   private static final Logger LOGGER = LogManager.getLogger();
   private final List<String> paths = Lists.newArrayList();
   private final LongList startTimes = new LongArrayList();
   private final Map<String, Profiler.Section> entries = Maps.newHashMap();
   private final IntSupplier getTickTime;
   private final LongSupplier getRealTime;
   private final long startTimeNano;
   private final int startTimeTicks;
   private String path = "";
   private boolean started;
   @Nullable
   private Profiler.Section currentEntry;
   private final boolean warn;

   public Profiler(LongSupplier pStartTimeNano, IntSupplier pStartTimeTicks, boolean pWarn) {
      this.startTimeNano = pStartTimeNano.getAsLong();
      this.getRealTime = pStartTimeNano;
      this.startTimeTicks = pStartTimeTicks.getAsInt();
      this.getTickTime = pStartTimeTicks;
      this.warn = pWarn;
   }

   public void startTick() {
      if (this.started) {
         LOGGER.error("Profiler tick already started - missing endTick()?");
      } else {
         this.started = true;
         this.path = "";
         this.paths.clear();
         this.push("root");
      }
   }

   public void endTick() {
      if (!this.started) {
         LOGGER.error("Profiler tick already ended - missing startTick()?");
      } else {
         this.pop();
         this.started = false;
         if (!this.path.isEmpty()) {
            LOGGER.error("Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?", () -> {
               return IProfileResult.demanglePath(this.path);
            });
         }

      }
   }

   /**
    * Start section
    */
   public void push(String pName) {
      if (!this.started) {
         LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", (Object)pName);
      } else {
         if (!this.path.isEmpty()) {
            this.path = this.path + '\u001e';
         }

         this.path = this.path + pName;
         this.paths.add(this.path);
         this.startTimes.add(Util.getNanos());
         this.currentEntry = null;
      }
   }

   public void push(Supplier<String> pNameSupplier) {
      this.push(pNameSupplier.get());
   }

   /**
    * End section
    */
   public void pop() {
      if (!this.started) {
         LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
      } else if (this.startTimes.isEmpty()) {
         LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
      } else {
         long i = Util.getNanos();
         long j = this.startTimes.removeLong(this.startTimes.size() - 1);
         this.paths.remove(this.paths.size() - 1);
         long k = i - j;
         Profiler.Section profiler$section = this.getCurrentEntry();
         profiler$section.duration = profiler$section.duration + k;
         profiler$section.count = profiler$section.count + 1L;
         if (this.warn && k > WARNING_TIME_NANOS) {
            LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", () -> {
               return IProfileResult.demanglePath(this.path);
            }, () -> {
               return (double)k / 1000000.0D;
            });
         }

         this.path = this.paths.isEmpty() ? "" : this.paths.get(this.paths.size() - 1);
         this.currentEntry = null;
      }
   }

   public void popPush(String pName) {
      this.pop();
      this.push(pName);
   }

   @OnlyIn(Dist.CLIENT)
   public void popPush(Supplier<String> pNameSupplier) {
      this.pop();
      this.push(pNameSupplier);
   }

   private Profiler.Section getCurrentEntry() {
      if (this.currentEntry == null) {
         this.currentEntry = this.entries.computeIfAbsent(this.path, (p_230080_0_) -> {
            return new Profiler.Section();
         });
      }

      return this.currentEntry;
   }

   public void incrementCounter(String pEntryId) {
      this.getCurrentEntry().counters.addTo(pEntryId, 1L);
   }

   public void incrementCounter(Supplier<String> pEntryIdSupplier) {
      this.getCurrentEntry().counters.addTo(pEntryIdSupplier.get(), 1L);
   }

   public IProfileResult getResults() {
      return new FilledProfileResult(this.entries, this.startTimeNano, this.startTimeTicks, this.getRealTime.getAsLong(), this.getTickTime.getAsInt());
   }

   static class Section implements IProfilerSection {
      private long duration;
      private long count;
      private Object2LongOpenHashMap<String> counters = new Object2LongOpenHashMap<>();

      private Section() {
      }

      public long getDuration() {
         return this.duration;
      }

      public long getCount() {
         return this.count;
      }

      public Object2LongMap<String> getCounters() {
         return Object2LongMaps.unmodifiable(this.counters);
      }
   }
}