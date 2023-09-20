package net.minecraft.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class EffectUtils {
   @OnlyIn(Dist.CLIENT)
   public static String formatDuration(EffectInstance pEffect, float pDurationFactor) {
      if (pEffect.isNoCounter()) {
         return "**:**";
      } else {
         int i = MathHelper.floor((float)pEffect.getDuration() * pDurationFactor);
         return StringUtils.formatTickDuration(i);
      }
   }

   public static boolean hasDigSpeed(LivingEntity pEntity) {
      return pEntity.hasEffect(Effects.DIG_SPEED) || pEntity.hasEffect(Effects.CONDUIT_POWER);
   }

   public static int getDigSpeedAmplification(LivingEntity pEntity) {
      int i = 0;
      int j = 0;
      if (pEntity.hasEffect(Effects.DIG_SPEED)) {
         i = pEntity.getEffect(Effects.DIG_SPEED).getAmplifier();
      }

      if (pEntity.hasEffect(Effects.CONDUIT_POWER)) {
         j = pEntity.getEffect(Effects.CONDUIT_POWER).getAmplifier();
      }

      return Math.max(i, j);
   }

   public static boolean hasWaterBreathing(LivingEntity pEntity) {
      return pEntity.hasEffect(Effects.WATER_BREATHING) || pEntity.hasEffect(Effects.CONDUIT_POWER);
   }
}