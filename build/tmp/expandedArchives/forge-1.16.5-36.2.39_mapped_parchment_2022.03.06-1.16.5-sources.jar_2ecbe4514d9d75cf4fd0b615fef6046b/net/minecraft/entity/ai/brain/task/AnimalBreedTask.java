package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.server.ServerWorld;

public class AnimalBreedTask extends Task<AnimalEntity> {
   private final EntityType<? extends AnimalEntity> partnerType;
   private final float speedModifier;
   private long spawnChildAtTime;

   public AnimalBreedTask(EntityType<? extends AnimalEntity> p_i231506_1_, float p_i231506_2_) {
      super(ImmutableMap.of(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.BREED_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED), 325);
      this.partnerType = p_i231506_1_;
      this.speedModifier = p_i231506_2_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, AnimalEntity pOwner) {
      return pOwner.isInLove() && this.findValidBreedPartner(pOwner).isPresent();
   }

   protected void start(ServerWorld pLevel, AnimalEntity pEntity, long pGameTime) {
      AnimalEntity animalentity = this.findValidBreedPartner(pEntity).get();
      pEntity.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animalentity);
      animalentity.getBrain().setMemory(MemoryModuleType.BREED_TARGET, pEntity);
      BrainUtil.lockGazeAndWalkToEachOther(pEntity, animalentity, this.speedModifier);
      int i = 275 + pEntity.getRandom().nextInt(50);
      this.spawnChildAtTime = pGameTime + (long)i;
   }

   protected boolean canStillUse(ServerWorld pLevel, AnimalEntity pEntity, long pGameTime) {
      if (!this.hasBreedTargetOfRightType(pEntity)) {
         return false;
      } else {
         AnimalEntity animalentity = this.getBreedTarget(pEntity);
         return animalentity.isAlive() && pEntity.canMate(animalentity) && BrainUtil.entityIsVisible(pEntity.getBrain(), animalentity) && pGameTime <= this.spawnChildAtTime;
      }
   }

   protected void tick(ServerWorld pLevel, AnimalEntity pOwner, long pGameTime) {
      AnimalEntity animalentity = this.getBreedTarget(pOwner);
      BrainUtil.lockGazeAndWalkToEachOther(pOwner, animalentity, this.speedModifier);
      if (pOwner.closerThan(animalentity, 3.0D)) {
         if (pGameTime >= this.spawnChildAtTime) {
            pOwner.spawnChildFromBreeding(pLevel, animalentity);
            pOwner.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            animalentity.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
         }

      }
   }

   protected void stop(ServerWorld pLevel, AnimalEntity pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      this.spawnChildAtTime = 0L;
   }

   private AnimalEntity getBreedTarget(AnimalEntity pAnimal) {
      return (AnimalEntity)pAnimal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
   }

   private boolean hasBreedTargetOfRightType(AnimalEntity pAnimal) {
      Brain<?> brain = pAnimal.getBrain();
      return brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) && brain.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
   }

   private Optional<? extends AnimalEntity> findValidBreedPartner(AnimalEntity pAnimal) {
      return pAnimal.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().stream().filter((p_233847_1_) -> {
         return p_233847_1_.getType() == this.partnerType;
      }).map((p_233845_0_) -> {
         return (AnimalEntity)p_233845_0_;
      }).filter(pAnimal::canMate).findFirst();
   }
}