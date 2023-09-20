package net.minecraft.entity.ai.brain;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class BrainUtil {
   public static void lockGazeAndWalkToEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity, float pSpeed) {
      lookAtEachOther(pFirstEntity, pSecondEntity);
      setWalkAndLookTargetMemoriesToEachOther(pFirstEntity, pSecondEntity, pSpeed);
   }

   public static boolean entityIsVisible(Brain<?> pBrain, LivingEntity pTarget) {
      return pBrain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).filter((p_220614_1_) -> {
         return p_220614_1_.contains(pTarget);
      }).isPresent();
   }

   public static boolean targetIsValid(Brain<?> pBrains, MemoryModuleType<? extends LivingEntity> pMemorymodule, EntityType<?> pEntityType) {
      return targetIsValid(pBrains, pMemorymodule, (p_220622_1_) -> {
         return p_220622_1_.getType() == pEntityType;
      });
   }

   private static boolean targetIsValid(Brain<?> pBrain, MemoryModuleType<? extends LivingEntity> pMemoryType, Predicate<LivingEntity> pLivingPredicate) {
      return pBrain.getMemory(pMemoryType).filter(pLivingPredicate).filter(LivingEntity::isAlive).filter((p_220615_1_) -> {
         return entityIsVisible(pBrain, p_220615_1_);
      }).isPresent();
   }

   private static void lookAtEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity) {
      lookAtEntity(pFirstEntity, pSecondEntity);
      lookAtEntity(pSecondEntity, pFirstEntity);
   }

   public static void lookAtEntity(LivingEntity pEntity, LivingEntity pTarget) {
      pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(pTarget, true));
   }

   private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity, float pSpeed) {
      int i = 2;
      setWalkAndLookTargetMemories(pFirstEntity, pSecondEntity, pSpeed, 2);
      setWalkAndLookTargetMemories(pSecondEntity, pFirstEntity, pSpeed, 2);
   }

   public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, Entity pTarget, float pSpeed, int pDistance) {
      WalkTarget walktarget = new WalkTarget(new EntityPosWrapper(pTarget, false), pSpeed, pDistance);
      pLivingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(pTarget, true));
      pLivingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
   }

   public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, BlockPos pPos, float pSpeed, int pDistance) {
      WalkTarget walktarget = new WalkTarget(new BlockPosWrapper(pPos), pSpeed, pDistance);
      pLivingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(pPos));
      pLivingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
   }

   public static void throwItem(LivingEntity pLivingEntity, ItemStack pStack, Vector3d pOffset) {
      double d0 = pLivingEntity.getEyeY() - (double)0.3F;
      ItemEntity itementity = new ItemEntity(pLivingEntity.level, pLivingEntity.getX(), d0, pLivingEntity.getZ(), pStack);
      float f = 0.3F;
      Vector3d vector3d = pOffset.subtract(pLivingEntity.position());
      vector3d = vector3d.normalize().scale((double)0.3F);
      itementity.setDeltaMovement(vector3d);
      itementity.setDefaultPickUpDelay();
      pLivingEntity.level.addFreshEntity(itementity);
   }

   public static SectionPos findSectionClosestToVillage(ServerWorld pServerLevel, SectionPos pSectionPos, int pRadius) {
      int i = pServerLevel.sectionsToVillage(pSectionPos);
      return SectionPos.cube(pSectionPos, pRadius).filter((p_220620_2_) -> {
         return pServerLevel.sectionsToVillage(p_220620_2_) < i;
      }).min(Comparator.comparingInt(pServerLevel::sectionsToVillage)).orElse(pSectionPos);
   }

   public static boolean isWithinAttackRange(MobEntity pMob, LivingEntity pTarget, int pCooldown) {
      Item item = pMob.getMainHandItem().getItem();
      if (item instanceof ShootableItem && pMob.canFireProjectileWeapon((ShootableItem)item)) {
         int i = ((ShootableItem)item).getDefaultProjectileRange() - pCooldown;
         return pMob.closerThan(pTarget, (double)i);
      } else {
         return isWithinMeleeAttackRange(pMob, pTarget);
      }
   }

   public static boolean isWithinMeleeAttackRange(LivingEntity p_233874_0_, LivingEntity p_233874_1_) {
      double d0 = p_233874_0_.distanceToSqr(p_233874_1_.getX(), p_233874_1_.getY(), p_233874_1_.getZ());
      double d1 = (double)(p_233874_0_.getBbWidth() * 2.0F * p_233874_0_.getBbWidth() * 2.0F + p_233874_1_.getBbWidth());
      return d0 <= d1;
   }

   public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity pLivingEntity, LivingEntity pTarget, double pDistance) {
      Optional<LivingEntity> optional = pLivingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
      if (!optional.isPresent()) {
         return false;
      } else {
         double d0 = pLivingEntity.distanceToSqr(optional.get().position());
         double d1 = pLivingEntity.distanceToSqr(pTarget.position());
         return d1 > d0 + pDistance * pDistance;
      }
   }

   public static boolean canSee(LivingEntity pLivingEntity, LivingEntity pTarget) {
      Brain<?> brain = pLivingEntity.getBrain();
      return !brain.hasMemoryValue(MemoryModuleType.VISIBLE_LIVING_ENTITIES) ? false : brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().contains(pTarget);
   }

   public static LivingEntity getNearestTarget(LivingEntity pCenterEntity, Optional<LivingEntity> pOptionalEntity, LivingEntity pLivingEntity) {
      return !pOptionalEntity.isPresent() ? pLivingEntity : getTargetNearestMe(pCenterEntity, pOptionalEntity.get(), pLivingEntity);
   }

   public static LivingEntity getTargetNearestMe(LivingEntity pCenterEntity, LivingEntity pLivingEntity1, LivingEntity pLivingEntity2) {
      Vector3d vector3d = pLivingEntity1.position();
      Vector3d vector3d1 = pLivingEntity2.position();
      return pCenterEntity.distanceToSqr(vector3d) < pCenterEntity.distanceToSqr(vector3d1) ? pLivingEntity1 : pLivingEntity2;
   }

   public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity pLivingEntity, MemoryModuleType<UUID> pTargetMemory) {
      Optional<UUID> optional = pLivingEntity.getBrain().getMemory(pTargetMemory);
      return optional.map((p_233868_1_) -> {
         return (LivingEntity)((ServerWorld)pLivingEntity.level).getEntity(p_233868_1_);
      });
   }

   public static Stream<VillagerEntity> getNearbyVillagersWithCondition(VillagerEntity pVillager, Predicate<VillagerEntity> pVillagerPredicate) {
      return pVillager.getBrain().getMemory(MemoryModuleType.LIVING_ENTITIES).map((p_233873_2_) -> {
         return p_233873_2_.stream().filter((p_233871_1_) -> {
            return p_233871_1_ instanceof VillagerEntity && p_233871_1_ != pVillager;
         }).map((p_233859_0_) -> {
            return (VillagerEntity)p_233859_0_;
         }).filter(LivingEntity::isAlive).filter(pVillagerPredicate);
      }).orElseGet(Stream::empty);
   }
}