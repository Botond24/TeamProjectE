package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;

public class CreateBabyVillagerTask extends Task<VillagerEntity> {
   private long birthTimestamp;

   public CreateBabyVillagerTask() {
      super(ImmutableMap.of(MemoryModuleType.BREED_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT), 350, 350);
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      return this.isBreedingPossible(pOwner);
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return pGameTime <= this.birthTimestamp && this.isBreedingPossible(pEntity);
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      AgeableEntity ageableentity = pEntity.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
      BrainUtil.lockGazeAndWalkToEachOther(pEntity, ageableentity, 0.5F);
      pLevel.broadcastEntityEvent(ageableentity, (byte)18);
      pLevel.broadcastEntityEvent(pEntity, (byte)18);
      int i = 275 + pEntity.getRandom().nextInt(50);
      this.birthTimestamp = pGameTime + (long)i;
   }

   protected void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      VillagerEntity villagerentity = (VillagerEntity)pOwner.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
      if (!(pOwner.distanceToSqr(villagerentity) > 5.0D)) {
         BrainUtil.lockGazeAndWalkToEachOther(pOwner, villagerentity, 0.5F);
         if (pGameTime >= this.birthTimestamp) {
            pOwner.eatAndDigestFood();
            villagerentity.eatAndDigestFood();
            this.tryToGiveBirth(pLevel, pOwner, villagerentity);
         } else if (pOwner.getRandom().nextInt(35) == 0) {
            pLevel.broadcastEntityEvent(villagerentity, (byte)12);
            pLevel.broadcastEntityEvent(pOwner, (byte)12);
         }

      }
   }

   private void tryToGiveBirth(ServerWorld pLevel, VillagerEntity pParent, VillagerEntity pPartner) {
      Optional<BlockPos> optional = this.takeVacantBed(pLevel, pParent);
      if (!optional.isPresent()) {
         pLevel.broadcastEntityEvent(pPartner, (byte)13);
         pLevel.broadcastEntityEvent(pParent, (byte)13);
      } else {
         Optional<VillagerEntity> optional1 = this.breed(pLevel, pParent, pPartner);
         if (optional1.isPresent()) {
            this.giveBedToChild(pLevel, optional1.get(), optional.get());
         } else {
            pLevel.getPoiManager().release(optional.get());
            DebugPacketSender.sendPoiTicketCountPacket(pLevel, optional.get());
         }
      }

   }

   protected void stop(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
   }

   private boolean isBreedingPossible(VillagerEntity pVillager) {
      Brain<VillagerEntity> brain = pVillager.getBrain();
      Optional<AgeableEntity> optional = brain.getMemory(MemoryModuleType.BREED_TARGET).filter((p_233999_0_) -> {
         return p_233999_0_.getType() == EntityType.VILLAGER;
      });
      if (!optional.isPresent()) {
         return false;
      } else {
         return BrainUtil.targetIsValid(brain, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && pVillager.canBreed() && optional.get().canBreed();
      }
   }

   private Optional<BlockPos> takeVacantBed(ServerWorld pLevel, VillagerEntity pVillager) {
      return pLevel.getPoiManager().take(PointOfInterestType.HOME.getPredicate(), (p_220481_2_) -> {
         return this.canReach(pVillager, p_220481_2_);
      }, pVillager.blockPosition(), 48);
   }

   private boolean canReach(VillagerEntity pVillager, BlockPos pPos) {
      Path path = pVillager.getNavigation().createPath(pPos, PointOfInterestType.HOME.getValidRange());
      return path != null && path.canReach();
   }

   private Optional<VillagerEntity> breed(ServerWorld pLevel, VillagerEntity pParent, VillagerEntity pPartner) {
      VillagerEntity villagerentity = pParent.getBreedOffspring(pLevel, pPartner);
      if (villagerentity == null) {
         return Optional.empty();
      } else {
         pParent.setAge(6000);
         pPartner.setAge(6000);
         villagerentity.setAge(-24000);
         villagerentity.moveTo(pParent.getX(), pParent.getY(), pParent.getZ(), 0.0F, 0.0F);
         pLevel.addFreshEntityWithPassengers(villagerentity);
         pLevel.broadcastEntityEvent(villagerentity, (byte)12);
         return Optional.of(villagerentity);
      }
   }

   private void giveBedToChild(ServerWorld pLevel, VillagerEntity pVillager, BlockPos pPos) {
      GlobalPos globalpos = GlobalPos.of(pLevel.dimension(), pPos);
      pVillager.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
   }
}