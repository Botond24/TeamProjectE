package net.minecraft.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;

public class HealthBoostEffect extends Effect {
   public HealthBoostEffect(EffectType p_i50393_1_, int p_i50393_2_) {
      super(p_i50393_1_, p_i50393_2_);
   }

   public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeModifierManager pAttributeMap, int pAmplifier) {
      super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
      if (pLivingEntity.getHealth() > pLivingEntity.getMaxHealth()) {
         pLivingEntity.setHealth(pLivingEntity.getMaxHealth());
      }

   }
}