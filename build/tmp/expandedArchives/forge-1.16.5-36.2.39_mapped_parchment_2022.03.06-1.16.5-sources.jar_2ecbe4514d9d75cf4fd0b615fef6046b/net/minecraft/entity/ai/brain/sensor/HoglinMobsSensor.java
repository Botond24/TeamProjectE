package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class HoglinMobsSensor extends Sensor<HoglinEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT);
   }

   protected void doTick(ServerWorld pLevel, HoglinEntity pEntity) {
      Brain<?> brain = pEntity.getBrain();
      brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent(pLevel, pEntity));
      Optional<PiglinEntity> optional = Optional.empty();
      int i = 0;
      List<HoglinEntity> list = Lists.newArrayList();

      for(LivingEntity livingentity : brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList())) {
         if (livingentity instanceof PiglinEntity && !livingentity.isBaby()) {
            ++i;
            if (!optional.isPresent()) {
               optional = Optional.of((PiglinEntity)livingentity);
            }
         }

         if (livingentity instanceof HoglinEntity && !livingentity.isBaby()) {
            list.add((HoglinEntity)livingentity);
         }
      }

      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, list);
      brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, i);
      brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, list.size());
   }

   private Optional<BlockPos> findNearestRepellent(ServerWorld pLevel, HoglinEntity pHoglin) {
      return BlockPos.findClosestMatch(pHoglin.blockPosition(), 8, 4, (p_234119_1_) -> {
         return pLevel.getBlockState(p_234119_1_).is(BlockTags.HOGLIN_REPELLENTS);
      });
   }
}