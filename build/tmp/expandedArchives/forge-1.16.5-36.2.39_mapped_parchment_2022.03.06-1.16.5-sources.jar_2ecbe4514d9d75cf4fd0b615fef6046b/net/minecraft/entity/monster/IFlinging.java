package net.minecraft.entity.monster;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IFlinging {
   @OnlyIn(Dist.CLIENT)
   int getAttackAnimationRemainingTicks();

   static boolean hurtAndThrowTarget(LivingEntity pHoglin, LivingEntity pTarget) {
      float f1 = (float)pHoglin.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f;
      if (!pHoglin.isBaby() && (int)f1 > 0) {
         f = f1 / 2.0F + (float)pHoglin.level.random.nextInt((int)f1);
      } else {
         f = f1;
      }

      boolean flag = pTarget.hurt(DamageSource.mobAttack(pHoglin), f);
      if (flag) {
         pHoglin.doEnchantDamageEffects(pHoglin, pTarget);
         if (!pHoglin.isBaby()) {
            throwTarget(pHoglin, pTarget);
         }
      }

      return flag;
   }

   static void throwTarget(LivingEntity pHoglin, LivingEntity pTarget) {
      double d0 = pHoglin.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      double d1 = pTarget.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
      double d2 = d0 - d1;
      if (!(d2 <= 0.0D)) {
         double d3 = pTarget.getX() - pHoglin.getX();
         double d4 = pTarget.getZ() - pHoglin.getZ();
         float f = (float)(pHoglin.level.random.nextInt(21) - 10);
         double d5 = d2 * (double)(pHoglin.level.random.nextFloat() * 0.5F + 0.2F);
         Vector3d vector3d = (new Vector3d(d3, 0.0D, d4)).normalize().scale(d5).yRot(f);
         double d6 = d2 * (double)pHoglin.level.random.nextFloat() * 0.5D;
         pTarget.push(vector3d.x, d6, vector3d.z);
         pTarget.hurtMarked = true;
      }
   }
}