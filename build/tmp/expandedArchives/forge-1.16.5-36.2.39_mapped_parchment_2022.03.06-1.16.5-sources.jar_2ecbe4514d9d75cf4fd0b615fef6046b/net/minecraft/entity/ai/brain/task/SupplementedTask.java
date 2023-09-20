package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public class SupplementedTask<E extends LivingEntity> extends Task<E> {
   private final Predicate<E> predicate;
   private final Task<? super E> wrappedBehavior;
   private final boolean checkWhileRunningAlso;

   public SupplementedTask(Map<MemoryModuleType<?>, MemoryModuleStatus> p_i231528_1_, Predicate<E> p_i231528_2_, Task<? super E> p_i231528_3_, boolean p_i231528_4_) {
      super(mergeMaps(p_i231528_1_, p_i231528_3_.entryCondition));
      this.predicate = p_i231528_2_;
      this.wrappedBehavior = p_i231528_3_;
      this.checkWhileRunningAlso = p_i231528_4_;
   }

   private static Map<MemoryModuleType<?>, MemoryModuleStatus> mergeMaps(Map<MemoryModuleType<?>, MemoryModuleStatus> p_233943_0_, Map<MemoryModuleType<?>, MemoryModuleStatus> p_233943_1_) {
      Map<MemoryModuleType<?>, MemoryModuleStatus> map = Maps.newHashMap();
      map.putAll(p_233943_0_);
      map.putAll(p_233943_1_);
      return map;
   }

   public SupplementedTask(Predicate<E> p_i231529_1_, Task<? super E> p_i231529_2_) {
      this(ImmutableMap.of(), p_i231529_1_, p_i231529_2_, false);
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      return this.predicate.test(pOwner) && this.wrappedBehavior.checkExtraStartConditions(pLevel, pOwner);
   }

   protected boolean canStillUse(ServerWorld pLevel, E pEntity, long pGameTime) {
      return this.checkWhileRunningAlso && this.predicate.test(pEntity) && this.wrappedBehavior.canStillUse(pLevel, pEntity, pGameTime);
   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      this.wrappedBehavior.start(pLevel, pEntity, pGameTime);
   }

   protected void tick(ServerWorld pLevel, E pOwner, long pGameTime) {
      this.wrappedBehavior.tick(pLevel, pOwner, pGameTime);
   }

   protected void stop(ServerWorld pLevel, E pEntity, long pGameTime) {
      this.wrappedBehavior.stop(pLevel, pEntity, pGameTime);
   }

   public String toString() {
      return "RunIf: " + this.wrappedBehavior;
   }
}