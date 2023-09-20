package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public interface IEntityReader {
   /**
    * Gets all entities within the specified AABB excluding the one passed into it.
    */
   List<Entity> getEntities(@Nullable Entity pEntity, AxisAlignedBB pArea, @Nullable Predicate<? super Entity> pPredicate);

   <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> pClazz, AxisAlignedBB pArea, @Nullable Predicate<? super T> pFilter);

   default <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, @Nullable Predicate<? super T> p_225316_3_) {
      return this.getEntitiesOfClass(p_225316_1_, p_225316_2_, p_225316_3_);
   }

   List<? extends PlayerEntity> players();

   /**
    * Will get all entities within the specified AABB excluding the one passed into it. Args: entityToExclude, aabb
    */
   default List<Entity> getEntities(@Nullable Entity pEntity, AxisAlignedBB pArea) {
      return this.getEntities(pEntity, pArea, EntityPredicates.NO_SPECTATORS);
   }

   default boolean isUnobstructed(@Nullable Entity pEntity, VoxelShape pShape) {
      if (pShape.isEmpty()) {
         return true;
      } else {
         for(Entity entity : this.getEntities(pEntity, pShape.bounds())) {
            if (!entity.removed && entity.blocksBuilding && (pEntity == null || !entity.isPassengerOfSameVehicle(pEntity)) && VoxelShapes.joinIsNotEmpty(pShape, VoxelShapes.create(entity.getBoundingBox()), IBooleanFunction.AND)) {
               return false;
            }
         }

         return true;
      }
   }

   default <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> pEntityClass, AxisAlignedBB pArea) {
      return this.getEntitiesOfClass(pEntityClass, pArea, EntityPredicates.NO_SPECTATORS);
   }

   default <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> p_225317_1_, AxisAlignedBB p_225317_2_) {
      return this.getLoadedEntitiesOfClass(p_225317_1_, p_225317_2_, EntityPredicates.NO_SPECTATORS);
   }

   default Stream<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AxisAlignedBB pArea, Predicate<Entity> pFilter) {
      if (pArea.getSize() < 1.0E-7D) {
         return Stream.empty();
      } else {
         AxisAlignedBB axisalignedbb = pArea.inflate(1.0E-7D);
         return this.getEntities(pEntity, axisalignedbb, pFilter.and((p_234892_2_) -> {
            if (p_234892_2_.getBoundingBox().intersects(axisalignedbb)) {
               if (pEntity == null) {
                  if (p_234892_2_.canBeCollidedWith()) {
                     return true;
                  }
               } else if (pEntity.canCollideWith(p_234892_2_)) {
                  return true;
               }
            }

            return false;
         })).stream().map(Entity::getBoundingBox).map(VoxelShapes::create);
      }
   }

   @Nullable
   default PlayerEntity getNearestPlayer(double pX, double pY, double pZ, double pDistance, @Nullable Predicate<Entity> pPredicate) {
      double d0 = -1.0D;
      PlayerEntity playerentity = null;

      for(PlayerEntity playerentity1 : this.players()) {
         if (pPredicate == null || pPredicate.test(playerentity1)) {
            double d1 = playerentity1.distanceToSqr(pX, pY, pZ);
            if ((pDistance < 0.0D || d1 < pDistance * pDistance) && (d0 == -1.0D || d1 < d0)) {
               d0 = d1;
               playerentity = playerentity1;
            }
         }
      }

      return playerentity;
   }

   @Nullable
   default PlayerEntity getNearestPlayer(Entity pEntity, double pDistance) {
      return this.getNearestPlayer(pEntity.getX(), pEntity.getY(), pEntity.getZ(), pDistance, false);
   }

   @Nullable
   default PlayerEntity getNearestPlayer(double pX, double pY, double pZ, double pDistance, boolean pCreativePlayers) {
      Predicate<Entity> predicate = pCreativePlayers ? EntityPredicates.NO_CREATIVE_OR_SPECTATOR : EntityPredicates.NO_SPECTATORS;
      return this.getNearestPlayer(pX, pY, pZ, pDistance, predicate);
   }

   default boolean hasNearbyAlivePlayer(double pX, double pY, double pZ, double pDistance) {
      for(PlayerEntity playerentity : this.players()) {
         if (EntityPredicates.NO_SPECTATORS.test(playerentity) && EntityPredicates.LIVING_ENTITY_STILL_ALIVE.test(playerentity)) {
            double d0 = playerentity.distanceToSqr(pX, pY, pZ);
            if (pDistance < 0.0D || d0 < pDistance * pDistance) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   default PlayerEntity getNearestPlayer(EntityPredicate pPredicate, LivingEntity pTarget) {
      return this.getNearestEntity(this.players(), pPredicate, pTarget, pTarget.getX(), pTarget.getY(), pTarget.getZ());
   }

   @Nullable
   default PlayerEntity getNearestPlayer(EntityPredicate pPredicate, LivingEntity pTarget, double pX, double pY, double pZ) {
      return this.getNearestEntity(this.players(), pPredicate, pTarget, pX, pY, pZ);
   }

   @Nullable
   default PlayerEntity getNearestPlayer(EntityPredicate pPredicate, double pX, double pY, double pZ) {
      return this.getNearestEntity(this.players(), pPredicate, (LivingEntity)null, pX, pY, pZ);
   }

   @Nullable
   default <T extends LivingEntity> T getNearestEntity(Class<? extends T> pEntityClazz, EntityPredicate pConditions, @Nullable LivingEntity pTarget, double pX, double pY, double pZ, AxisAlignedBB pBoundingBox) {
      return this.getNearestEntity(this.getEntitiesOfClass(pEntityClazz, pBoundingBox, (Predicate<? super T>)null), pConditions, pTarget, pX, pY, pZ);
   }

   @Nullable
   default <T extends LivingEntity> T getNearestLoadedEntity(Class<? extends T> p_225318_1_, EntityPredicate p_225318_2_, @Nullable LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, AxisAlignedBB p_225318_10_) {
      return this.getNearestEntity(this.getLoadedEntitiesOfClass(p_225318_1_, p_225318_10_, (Predicate<? super T>)null), p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_);
   }

   @Nullable
   default <T extends LivingEntity> T getNearestEntity(List<? extends T> pEntities, EntityPredicate pPredicate, @Nullable LivingEntity pTarget, double pX, double pY, double pZ) {
      double d0 = -1.0D;
      T t = null;

      for(T t1 : pEntities) {
         if (pPredicate.test(pTarget, t1)) {
            double d1 = t1.distanceToSqr(pX, pY, pZ);
            if (d0 == -1.0D || d1 < d0) {
               d0 = d1;
               t = t1;
            }
         }
      }

      return t;
   }

   default List<PlayerEntity> getNearbyPlayers(EntityPredicate pPredicate, LivingEntity pTarget, AxisAlignedBB pArea) {
      List<PlayerEntity> list = Lists.newArrayList();

      for(PlayerEntity playerentity : this.players()) {
         if (pArea.contains(playerentity.getX(), playerentity.getY(), playerentity.getZ()) && pPredicate.test(pTarget, playerentity)) {
            list.add(playerentity);
         }
      }

      return list;
   }

   default <T extends LivingEntity> List<T> getNearbyEntities(Class<? extends T> pEntityClazz, EntityPredicate pEntityPredicate, LivingEntity pEntity, AxisAlignedBB pArea) {
      List<T> list = this.getEntitiesOfClass(pEntityClazz, pArea, (Predicate<? super T>)null);
      List<T> list1 = Lists.newArrayList();

      for(T t : list) {
         if (pEntityPredicate.test(pEntity, t)) {
            list1.add(t);
         }
      }

      return list1;
   }

   @Nullable
   default PlayerEntity getPlayerByUUID(UUID pUniqueId) {
      for(int i = 0; i < this.players().size(); ++i) {
         PlayerEntity playerentity = this.players().get(i);
         if (pUniqueId.equals(playerentity.getUUID())) {
            return playerentity;
         }
      }

      return null;
   }
}