package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public class GolemLastSeenSensor extends Sensor<LivingEntity> {
   public GolemLastSeenSensor() {
      this(200);
   }

   public GolemLastSeenSensor(int p_i51525_1_) {
      super(p_i51525_1_);
   }

   protected void doTick(ServerWorld pLevel, LivingEntity pEntity) {
      checkForNearbyGolem(pEntity);
   }

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES);
   }

   public static void checkForNearbyGolem(LivingEntity pLivingEntity) {
      Optional<List<LivingEntity>> optional = pLivingEntity.getBrain().getMemory(MemoryModuleType.LIVING_ENTITIES);
      if (optional.isPresent()) {
         boolean flag = optional.get().stream().anyMatch((p_223546_0_) -> {
            return p_223546_0_.getType().equals(EntityType.IRON_GOLEM);
         });
         if (flag) {
            golemDetected(pLivingEntity);
         }

      }
   }

   public static void golemDetected(LivingEntity pLivingEntity) {
      pLivingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, 600L);
   }
}