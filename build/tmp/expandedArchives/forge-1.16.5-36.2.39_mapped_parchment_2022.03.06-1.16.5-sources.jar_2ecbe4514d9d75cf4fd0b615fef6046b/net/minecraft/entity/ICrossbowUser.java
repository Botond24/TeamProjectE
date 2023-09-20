package net.minecraft.entity;

import javax.annotation.Nullable;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public interface ICrossbowUser extends IRangedAttackMob {
   void setChargingCrossbow(boolean pIsCharging);

   void shootCrossbowProjectile(LivingEntity pTarget, ItemStack pCrossbowStack, ProjectileEntity pProjectile, float pProjectileAngle);

   /**
    * Gets the active target the Task system uses for tracking
    */
   @Nullable
   LivingEntity getTarget();

   void onCrossbowAttackPerformed();

   default void performCrossbowAttack(LivingEntity pUser, float pVelocity) {
      Hand hand = ProjectileHelper.getWeaponHoldingHand(pUser, item -> item instanceof CrossbowItem);
      ItemStack itemstack = pUser.getItemInHand(hand);
      if (pUser.isHolding(item -> item instanceof CrossbowItem)) {
         CrossbowItem.performShooting(pUser.level, pUser, hand, itemstack, pVelocity, (float)(14 - pUser.level.getDifficulty().getId() * 4));
      }

      this.onCrossbowAttackPerformed();
   }

   default void shootCrossbowProjectile(LivingEntity pUser, LivingEntity pTarget, ProjectileEntity pProjectile, float pProjectileAngle, float pVelocity) {
      double d0 = pTarget.getX() - pUser.getX();
      double d1 = pTarget.getZ() - pUser.getZ();
      double d2 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1);
      double d3 = pTarget.getY(0.3333333333333333D) - pProjectile.getY() + d2 * (double)0.2F;
      Vector3f vector3f = this.getProjectileShotVector(pUser, new Vector3d(d0, d3, d1), pProjectileAngle);
      pProjectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), pVelocity, (float)(14 - pUser.level.getDifficulty().getId() * 4));
      pUser.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (pUser.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   default Vector3f getProjectileShotVector(LivingEntity pUser, Vector3d pVectorTowardTarget, float pProjectileAngle) {
      Vector3d vector3d = pVectorTowardTarget.normalize();
      Vector3d vector3d1 = vector3d.cross(new Vector3d(0.0D, 1.0D, 0.0D));
      if (vector3d1.lengthSqr() <= 1.0E-7D) {
         vector3d1 = vector3d.cross(pUser.getUpVector(1.0F));
      }

      Quaternion quaternion = new Quaternion(new Vector3f(vector3d1), 90.0F, true);
      Vector3f vector3f = new Vector3f(vector3d);
      vector3f.transform(quaternion);
      Quaternion quaternion1 = new Quaternion(vector3f, pProjectileAngle, true);
      Vector3f vector3f1 = new Vector3f(vector3d);
      vector3f1.transform(quaternion1);
      return vector3f1;
   }
}
