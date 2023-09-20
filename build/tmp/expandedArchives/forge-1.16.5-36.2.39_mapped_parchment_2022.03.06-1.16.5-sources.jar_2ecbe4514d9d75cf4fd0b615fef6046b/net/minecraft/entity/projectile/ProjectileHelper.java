package net.minecraft.entity.projectile;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class ProjectileHelper {
   public static RayTraceResult getHitResult(Entity pProjectile, Predicate<Entity> pFilter) {
      Vector3d vector3d = pProjectile.getDeltaMovement();
      World world = pProjectile.level;
      Vector3d vector3d1 = pProjectile.position();
      Vector3d vector3d2 = vector3d1.add(vector3d);
      RayTraceResult raytraceresult = world.clip(new RayTraceContext(vector3d1, vector3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, pProjectile));
      if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
         vector3d2 = raytraceresult.getLocation();
      }

      RayTraceResult raytraceresult1 = getEntityHitResult(world, pProjectile, vector3d1, vector3d2, pProjectile.getBoundingBox().expandTowards(pProjectile.getDeltaMovement()).inflate(1.0D), pFilter);
      if (raytraceresult1 != null) {
         raytraceresult = raytraceresult1;
      }

      return raytraceresult;
   }

   /**
    * Gets the EntityRayTraceResult representing the entity hit
    */
   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static EntityRayTraceResult getEntityHitResult(Entity pShooter, Vector3d pStartVec, Vector3d pEndVec, AxisAlignedBB pBoundingBox, Predicate<Entity> pFilter, double pDistance) {
      World world = pShooter.level;
      double d0 = pDistance;
      Entity entity = null;
      Vector3d vector3d = null;

      for(Entity entity1 : world.getEntities(pShooter, pBoundingBox, pFilter)) {
         AxisAlignedBB axisalignedbb = entity1.getBoundingBox().inflate((double)entity1.getPickRadius());
         Optional<Vector3d> optional = axisalignedbb.clip(pStartVec, pEndVec);
         if (axisalignedbb.contains(pStartVec)) {
            if (d0 >= 0.0D) {
               entity = entity1;
               vector3d = optional.orElse(pStartVec);
               d0 = 0.0D;
            }
         } else if (optional.isPresent()) {
            Vector3d vector3d1 = optional.get();
            double d1 = pStartVec.distanceToSqr(vector3d1);
            if (d1 < d0 || d0 == 0.0D) {
               if (entity1.getRootVehicle() == pShooter.getRootVehicle() && !entity1.canRiderInteract()) {
                  if (d0 == 0.0D) {
                     entity = entity1;
                     vector3d = vector3d1;
                  }
               } else {
                  entity = entity1;
                  vector3d = vector3d1;
                  d0 = d1;
               }
            }
         }
      }

      return entity == null ? null : new EntityRayTraceResult(entity, vector3d);
   }

   @Nullable
   public static EntityRayTraceResult getEntityHitResult(World p_221269_0_, Entity p_221269_1_, Vector3d p_221269_2_, Vector3d p_221269_3_, AxisAlignedBB p_221269_4_, Predicate<Entity> p_221269_5_) {
      double d0 = Double.MAX_VALUE;
      Entity entity = null;

      for(Entity entity1 : p_221269_0_.getEntities(p_221269_1_, p_221269_4_, p_221269_5_)) {
         AxisAlignedBB axisalignedbb = entity1.getBoundingBox().inflate((double)0.3F);
         Optional<Vector3d> optional = axisalignedbb.clip(p_221269_2_, p_221269_3_);
         if (optional.isPresent()) {
            double d1 = p_221269_2_.distanceToSqr(optional.get());
            if (d1 < d0) {
               entity = entity1;
               d0 = d1;
            }
         }
      }

      return entity == null ? null : new EntityRayTraceResult(entity);
   }

   public static final void rotateTowardsMovement(Entity pProjectile, float pRotationSpeed) {
      Vector3d vector3d = pProjectile.getDeltaMovement();
      if (vector3d.lengthSqr() != 0.0D) {
         float f = MathHelper.sqrt(Entity.getHorizontalDistanceSqr(vector3d));
         pProjectile.yRot = (float)(MathHelper.atan2(vector3d.z, vector3d.x) * (double)(180F / (float)Math.PI)) + 90.0F;

         for(pProjectile.xRot = (float)(MathHelper.atan2((double)f, vector3d.y) * (double)(180F / (float)Math.PI)) - 90.0F; pProjectile.xRot - pProjectile.xRotO < -180.0F; pProjectile.xRotO -= 360.0F) {
         }

         while(pProjectile.xRot - pProjectile.xRotO >= 180.0F) {
            pProjectile.xRotO += 360.0F;
         }

         while(pProjectile.yRot - pProjectile.yRotO < -180.0F) {
            pProjectile.yRotO -= 360.0F;
         }

         while(pProjectile.yRot - pProjectile.yRotO >= 180.0F) {
            pProjectile.yRotO += 360.0F;
         }

         pProjectile.xRot = MathHelper.lerp(pRotationSpeed, pProjectile.xRotO, pProjectile.xRot);
         pProjectile.yRot = MathHelper.lerp(pRotationSpeed, pProjectile.yRotO, pProjectile.yRot);
      }
   }

   @Deprecated // Forge: Use the version below that takes in a Predicate<Item> instead of an Item
   public static Hand getWeaponHoldingHand(LivingEntity pShooter, Item pWeapon) {
      return pShooter.getMainHandItem().getItem() == pWeapon ? Hand.MAIN_HAND : Hand.OFF_HAND;
   }

   public static Hand getWeaponHoldingHand(LivingEntity livingEntity, Predicate<Item> itemPredicate)
   {
      return itemPredicate.test(livingEntity.getMainHandItem().getItem()) ? Hand.MAIN_HAND : Hand.OFF_HAND;
   }

   public static AbstractArrowEntity getMobArrow(LivingEntity pShooter, ItemStack pArrowStack, float pVelocity) {
      ArrowItem arrowitem = (ArrowItem)(pArrowStack.getItem() instanceof ArrowItem ? pArrowStack.getItem() : Items.ARROW);
      AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(pShooter.level, pArrowStack, pShooter);
      abstractarrowentity.setEnchantmentEffectsFromEntity(pShooter, pVelocity);
      if (pArrowStack.getItem() == Items.TIPPED_ARROW && abstractarrowentity instanceof ArrowEntity) {
         ((ArrowEntity)abstractarrowentity).setEffectsFromItem(pArrowStack);
      }

      return abstractarrowentity;
   }
}
