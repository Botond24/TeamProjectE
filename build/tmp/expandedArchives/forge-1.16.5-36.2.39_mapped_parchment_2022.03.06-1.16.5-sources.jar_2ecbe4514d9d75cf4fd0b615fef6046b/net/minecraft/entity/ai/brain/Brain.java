package net.minecraft.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Brain<E extends LivingEntity> {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Supplier<Codec<Brain<E>>> codec;
   private final Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> memories = Maps.newHashMap();
   private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
   private final Map<Integer, Map<Activity, Set<Task<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
   private Schedule schedule = Schedule.EMPTY;
   private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>>> activityRequirements = Maps.newHashMap();
   private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.newHashMap();
   private Set<Activity> coreActivities = Sets.newHashSet();
   private final Set<Activity> activeActivities = Sets.newHashSet();
   private Activity defaultActivity = Activity.IDLE;
   private long lastScheduleUpdate = -9999L;

   public static <E extends LivingEntity> Brain.BrainCodec<E> provider(Collection<? extends MemoryModuleType<?>> pMemoryTypes, Collection<? extends SensorType<? extends Sensor<? super E>>> pSensorTypes) {
      return new Brain.BrainCodec<>(pMemoryTypes, pSensorTypes);
   }

   public static <E extends LivingEntity> Codec<Brain<E>> codec(final Collection<? extends MemoryModuleType<?>> pMemoryTypes, final Collection<? extends SensorType<? extends Sensor<? super E>>> pSensorTypes) {
      final MutableObject<Codec<Brain<E>>> mutableobject = new MutableObject<>();
      mutableobject.setValue((new MapCodec<Brain<E>>() {
         public <T> Stream<T> keys(DynamicOps<T> p_keys_1_) {
            return pMemoryTypes.stream().flatMap((p_233734_0_) -> {
               return Util.toStream(p_233734_0_.getCodec().map((p_233727_1_) -> {
                  return Registry.MEMORY_MODULE_TYPE.getKey(p_233734_0_);
               }));
            }).map((p_233733_1_) -> {
               return p_keys_1_.createString(p_233733_1_.toString());
            });
         }

         public <T> DataResult<Brain<E>> decode(DynamicOps<T> p_decode_1_, MapLike<T> p_decode_2_) {
            MutableObject<DataResult<Builder<Brain.MemoryCodec<?>>>> mutableobject1 = new MutableObject<>(DataResult.success(ImmutableList.builder()));
            p_decode_2_.entries().forEach((p_233732_3_) -> {
               DataResult<MemoryModuleType<?>> dataresult = Registry.MEMORY_MODULE_TYPE.parse(p_decode_1_, p_233732_3_.getFirst());
               DataResult<? extends Brain.MemoryCodec<?>> dataresult1 = dataresult.flatMap((p_233729_3_) -> {
                  return this.captureRead(p_233729_3_, p_decode_1_, (T)p_233732_3_.getSecond());
               });
               mutableobject1.setValue(mutableobject1.getValue().apply2(Builder::add, dataresult1));
            });
            ImmutableList<Brain.MemoryCodec<?>> immutablelist = mutableobject1.getValue().resultOrPartial(Brain.LOGGER::error).map(Builder::build).orElseGet(ImmutableList::of);
            return DataResult.success(new Brain<>(pMemoryTypes, pSensorTypes, immutablelist, mutableobject::getValue));
         }

         private <T, U> DataResult<Brain.MemoryCodec<U>> captureRead(MemoryModuleType<U> p_233728_1_, DynamicOps<T> p_233728_2_, T p_233728_3_) {
            return p_233728_1_.getCodec().map(DataResult::success).orElseGet(() -> {
               return DataResult.error("No codec for memory: " + p_233728_1_);
            }).flatMap((p_233731_2_) -> {
               return p_233731_2_.parse(p_233728_2_, p_233728_3_);
            }).map((p_233726_1_) -> {
               return new Brain.MemoryCodec<>(p_233728_1_, Optional.of(p_233726_1_));
            });
         }

         public <T> RecordBuilder<T> encode(Brain<E> p_encode_1_, DynamicOps<T> p_encode_2_, RecordBuilder<T> p_encode_3_) {
            p_encode_1_.memories().forEach((p_233730_2_) -> {
               p_233730_2_.serialize(p_encode_2_, p_encode_3_);
            });
            return p_encode_3_;
         }
      }).fieldOf("memories").codec());
      return mutableobject.getValue();
   }

   public Brain(Collection<? extends MemoryModuleType<?>> p_i231494_1_, Collection<? extends SensorType<? extends Sensor<? super E>>> p_i231494_2_, ImmutableList<Brain.MemoryCodec<?>> p_i231494_3_, Supplier<Codec<Brain<E>>> p_i231494_4_) {
      this.codec = p_i231494_4_;

      for(MemoryModuleType<?> memorymoduletype : p_i231494_1_) {
         this.memories.put(memorymoduletype, Optional.empty());
      }

      for(SensorType<? extends Sensor<? super E>> sensortype : p_i231494_2_) {
         this.sensors.put(sensortype, sensortype.create());
      }

      for(Sensor<? super E> sensor : this.sensors.values()) {
         for(MemoryModuleType<?> memorymoduletype1 : sensor.requires()) {
            this.memories.put(memorymoduletype1, Optional.empty());
         }
      }

      for(Brain.MemoryCodec<?> memorycodec : p_i231494_3_) {
         memorycodec.setMemoryInternal(this);
      }

   }

   public <T> DataResult<T> serializeStart(DynamicOps<T> pOps) {
      return this.codec.get().encodeStart(pOps, this);
   }

   private Stream<Brain.MemoryCodec<?>> memories() {
      return this.memories.entrySet().stream().map((p_233707_0_) -> {
         return Brain.MemoryCodec.createUnchecked(p_233707_0_.getKey(), p_233707_0_.getValue());
      });
   }

   public boolean hasMemoryValue(MemoryModuleType<?> pType) {
      return this.checkMemory(pType, MemoryModuleStatus.VALUE_PRESENT);
   }

   public <U> void eraseMemory(MemoryModuleType<U> pType) {
      this.setMemory(pType, Optional.empty());
   }

   public <U> void setMemory(MemoryModuleType<U> pMemoryType, @Nullable U pMemory) {
      this.setMemory(pMemoryType, Optional.ofNullable(pMemory));
   }

   public <U> void setMemoryWithExpiry(MemoryModuleType<U> pMemoryType, U pMemory, long pTimesToLive) {
      this.setMemoryInternal(pMemoryType, Optional.of(Memory.of(pMemory, pTimesToLive)));
   }

   public <U> void setMemory(MemoryModuleType<U> pMemoryType, Optional<? extends U> pMemory) {
      this.setMemoryInternal(pMemoryType, pMemory.map(Memory::of));
   }

   private <U> void setMemoryInternal(MemoryModuleType<U> pMemoryType, Optional<? extends Memory<?>> pMemory) {
      if (this.memories.containsKey(pMemoryType)) {
         if (pMemory.isPresent() && this.isEmptyCollection(pMemory.get().getValue())) {
            this.eraseMemory(pMemoryType);
         } else {
            this.memories.put(pMemoryType, pMemory);
         }
      }

   }

   public <U> Optional<U> getMemory(MemoryModuleType<U> pType) {
      return (Optional<U>) this.memories.get(pType).map(Memory::getValue);
   }

   public <U> boolean isMemoryValue(MemoryModuleType<U> pMemoryType, U pMemory) {
      return !this.hasMemoryValue(pMemoryType) ? false : this.getMemory(pMemoryType).filter((p_233704_1_) -> {
         return p_233704_1_.equals(pMemory);
      }).isPresent();
   }

   public boolean checkMemory(MemoryModuleType<?> pMemoryType, MemoryModuleStatus pMemoryStatus) {
      Optional<? extends Memory<?>> optional = this.memories.get(pMemoryType);
      if (optional == null) {
         return false;
      } else {
         return pMemoryStatus == MemoryModuleStatus.REGISTERED || pMemoryStatus == MemoryModuleStatus.VALUE_PRESENT && optional.isPresent() || pMemoryStatus == MemoryModuleStatus.VALUE_ABSENT && !optional.isPresent();
      }
   }

   public Schedule getSchedule() {
      return this.schedule;
   }

   public void setSchedule(Schedule pNewSchedule) {
      this.schedule = pNewSchedule;
   }

   public void setCoreActivities(Set<Activity> pNewActivities) {
      this.coreActivities = pNewActivities;
   }

   @Deprecated
   public List<Task<? super E>> getRunningBehaviors() {
      List<Task<? super E>> list = new ObjectArrayList<>();

      for(Map<Activity, Set<Task<? super E>>> map : this.availableBehaviorsByPriority.values()) {
         for(Set<Task<? super E>> set : map.values()) {
            for(Task<? super E> task : set) {
               if (task.getStatus() == Task.Status.RUNNING) {
                  list.add(task);
               }
            }
         }
      }

      return list;
   }

   public void useDefaultActivity() {
      this.setActiveActivity(this.defaultActivity);
   }

   public Optional<Activity> getActiveNonCoreActivity() {
      for(Activity activity : this.activeActivities) {
         if (!this.coreActivities.contains(activity)) {
            return Optional.of(activity);
         }
      }

      return Optional.empty();
   }

   public void setActiveActivityIfPossible(Activity pActivity) {
      if (this.activityRequirementsAreMet(pActivity)) {
         this.setActiveActivity(pActivity);
      } else {
         this.useDefaultActivity();
      }

   }

   private void setActiveActivity(Activity pActivity) {
      if (!this.isActive(pActivity)) {
         this.eraseMemoriesForOtherActivitesThan(pActivity);
         this.activeActivities.clear();
         this.activeActivities.addAll(this.coreActivities);
         this.activeActivities.add(pActivity);
      }
   }

   private void eraseMemoriesForOtherActivitesThan(Activity pActivity) {
      for(Activity activity : this.activeActivities) {
         if (activity != pActivity) {
            Set<MemoryModuleType<?>> set = this.activityMemoriesToEraseWhenStopped.get(activity);
            if (set != null) {
               for(MemoryModuleType<?> memorymoduletype : set) {
                  this.eraseMemory(memorymoduletype);
               }
            }
         }
      }

   }

   public void updateActivityFromSchedule(long pDayTime, long pGameTime) {
      if (pGameTime - this.lastScheduleUpdate > 20L) {
         this.lastScheduleUpdate = pGameTime;
         Activity activity = this.getSchedule().getActivityAt((int)(pDayTime % 24000L));
         if (!this.activeActivities.contains(activity)) {
            this.setActiveActivityIfPossible(activity);
         }
      }

   }

   public void setActiveActivityToFirstValid(List<Activity> pActivities) {
      for(Activity activity : pActivities) {
         if (this.activityRequirementsAreMet(activity)) {
            this.setActiveActivity(activity);
            break;
         }
      }

   }

   public void setDefaultActivity(Activity pNewFallbackActivity) {
      this.defaultActivity = pNewFallbackActivity;
   }

   public void addActivity(Activity pActivity, int pPriorityStart, ImmutableList<? extends Task<? super E>> pTasks) {
      this.addActivity(pActivity, this.createPriorityPairs(pPriorityStart, pTasks));
   }

   public void addActivityAndRemoveMemoryWhenStopped(Activity pActivity, int pPriorityStart, ImmutableList<? extends Task<? super E>> pTasks, MemoryModuleType<?> pMemoryType) {
      Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> set = ImmutableSet.of(Pair.of(pMemoryType, MemoryModuleStatus.VALUE_PRESENT));
      Set<MemoryModuleType<?>> set1 = ImmutableSet.of(pMemoryType);
      this.addActivityAndRemoveMemoriesWhenStopped(pActivity, this.createPriorityPairs(pPriorityStart, pTasks), set, set1);
   }

   public void addActivity(Activity pActivity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> pTasks) {
      this.addActivityAndRemoveMemoriesWhenStopped(pActivity, pTasks, ImmutableSet.of(), Sets.newHashSet());
   }

   public void addActivityWithConditions(Activity pActivity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> pTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> pMemoryStatuses) {
      this.addActivityAndRemoveMemoriesWhenStopped(pActivity, pTasks, pMemoryStatuses, Sets.newHashSet());
   }

   private void addActivityAndRemoveMemoriesWhenStopped(Activity pActivity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> pTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> pMemorieStatuses, Set<MemoryModuleType<?>> pMemoryTypes) {
      this.activityRequirements.put(pActivity, pMemorieStatuses);
      if (!pMemoryTypes.isEmpty()) {
         this.activityMemoriesToEraseWhenStopped.put(pActivity, pMemoryTypes);
      }

      for(Pair<Integer, ? extends Task<? super E>> pair : pTasks) {
         this.availableBehaviorsByPriority.computeIfAbsent(pair.getFirst(), (p_233703_0_) -> {
            return Maps.newHashMap();
         }).computeIfAbsent(pActivity, (p_233717_0_) -> {
            return Sets.newLinkedHashSet();
         }).add(pair.getSecond());
      }

   }

   public boolean isActive(Activity pActivity) {
      return this.activeActivities.contains(pActivity);
   }

   public Brain<E> copyWithoutBehaviors() {
      Brain<E> brain = new Brain<>(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codec);

      for(Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>> entry : this.memories.entrySet()) {
         MemoryModuleType<?> memorymoduletype = entry.getKey();
         if (entry.getValue().isPresent()) {
            brain.memories.put(memorymoduletype, entry.getValue());
         }
      }

      return brain;
   }

   public void tick(ServerWorld pLevel, E pEntity) {
      this.forgetOutdatedMemories();
      this.tickSensors(pLevel, pEntity);
      this.startEachNonRunningBehavior(pLevel, pEntity);
      this.tickEachRunningBehavior(pLevel, pEntity);
   }

   private void tickSensors(ServerWorld pLevel, E pBrainHolder) {
      for(Sensor<? super E> sensor : this.sensors.values()) {
         sensor.tick(pLevel, pBrainHolder);
      }

   }

   private void forgetOutdatedMemories() {
      for(Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>> entry : this.memories.entrySet()) {
         if (entry.getValue().isPresent()) {
            Memory<?> memory = entry.getValue().get();
            memory.tick();
            if (memory.hasExpired()) {
               this.eraseMemory(entry.getKey());
            }
         }
      }

   }

   public void stopAll(ServerWorld pLevel, E pOwner) {
      long i = pOwner.level.getGameTime();

      for(Task<? super E> task : this.getRunningBehaviors()) {
         task.doStop(pLevel, pOwner, i);
      }

   }

   private void startEachNonRunningBehavior(ServerWorld pLevel, E pEntity) {
      long i = pLevel.getGameTime();

      for(Map<Activity, Set<Task<? super E>>> map : this.availableBehaviorsByPriority.values()) {
         for(Entry<Activity, Set<Task<? super E>>> entry : map.entrySet()) {
            Activity activity = entry.getKey();
            if (this.activeActivities.contains(activity)) {
               for(Task<? super E> task : entry.getValue()) {
                  if (task.getStatus() == Task.Status.STOPPED) {
                     task.tryStart(pLevel, pEntity, i);
                  }
               }
            }
         }
      }

   }

   private void tickEachRunningBehavior(ServerWorld pLevel, E pEntity) {
      long i = pLevel.getGameTime();

      for(Task<? super E> task : this.getRunningBehaviors()) {
         task.tickOrStop(pLevel, pEntity, i);
      }

   }

   private boolean activityRequirementsAreMet(Activity pActivity) {
      if (!this.activityRequirements.containsKey(pActivity)) {
         return false;
      } else {
         for(Pair<MemoryModuleType<?>, MemoryModuleStatus> pair : this.activityRequirements.get(pActivity)) {
            MemoryModuleType<?> memorymoduletype = pair.getFirst();
            MemoryModuleStatus memorymodulestatus = pair.getSecond();
            if (!this.checkMemory(memorymoduletype, memorymodulestatus)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean isEmptyCollection(Object pCollection) {
      return pCollection instanceof Collection && ((Collection)pCollection).isEmpty();
   }

   ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> createPriorityPairs(int pPriorityStart, ImmutableList<? extends Task<? super E>> pTasks) {
      int i = pPriorityStart;
      Builder<Pair<Integer, ? extends Task<? super E>>> builder = ImmutableList.builder();

      for(Task<? super E> task : pTasks) {
         builder.add(Pair.of(i++, task));
      }

      return builder.build();
   }

   public static final class BrainCodec<E extends LivingEntity> {
      private final Collection<? extends MemoryModuleType<?>> memoryTypes;
      private final Collection<? extends SensorType<? extends Sensor<? super E>>> sensorTypes;
      private final Codec<Brain<E>> codec;

      private BrainCodec(Collection<? extends MemoryModuleType<?>> p_i231498_1_, Collection<? extends SensorType<? extends Sensor<? super E>>> p_i231498_2_) {
         this.memoryTypes = p_i231498_1_;
         this.sensorTypes = p_i231498_2_;
         this.codec = Brain.codec(p_i231498_1_, p_i231498_2_);
      }

      public Brain<E> makeBrain(Dynamic<?> pOps) {
         return this.codec.parse(pOps).resultOrPartial(Brain.LOGGER::error).orElseGet(() -> {
            return new Brain<>(this.memoryTypes, this.sensorTypes, ImmutableList.of(), () -> {
               return this.codec;
            });
         });
      }
   }

   static final class MemoryCodec<U> {
      private final MemoryModuleType<U> type;
      private final Optional<? extends Memory<U>> value;

      private static <U> Brain.MemoryCodec<U> createUnchecked(MemoryModuleType<U> pMemoryType, Optional<? extends Memory<?>> pMemory) {
         return new Brain.MemoryCodec<>(pMemoryType, (Optional<? extends Memory<U>>) pMemory);
      }

      private MemoryCodec(MemoryModuleType<U> p_i231496_1_, Optional<? extends Memory<U>> p_i231496_2_) {
         this.type = p_i231496_1_;
         this.value = p_i231496_2_;
      }

      private void setMemoryInternal(Brain<?> pBrain) {
         pBrain.setMemoryInternal(this.type, this.value);
      }

      public <T> void serialize(DynamicOps<T> pOps, RecordBuilder<T> pBuilder) {
         this.type.getCodec().ifPresent((p_233741_3_) -> {
            this.value.ifPresent((p_233742_4_) -> {
               pBuilder.add(Registry.MEMORY_MODULE_TYPE.encodeStart(pOps, this.type), p_233741_3_.encodeStart(pOps, p_233742_4_));
            });
         });
      }
   }
}