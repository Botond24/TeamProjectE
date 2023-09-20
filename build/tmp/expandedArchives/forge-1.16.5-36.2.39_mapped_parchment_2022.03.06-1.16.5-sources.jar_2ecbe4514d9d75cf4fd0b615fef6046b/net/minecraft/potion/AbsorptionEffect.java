package net.minecraft.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;

public class AbsorptionEffect extends Effect {
   protected AbsorptionEffect(EffectType p_i50395_1_, int p_i50395_2_) {
      super(p_i50395_1_, p_i50395_2_);
   }

   public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeModifierManager pAttributeMap, int pAmplifier) {
      pLivingEntity.setAbsorptionAmount(pLivingEntity.getAbsorptionAmount() - (float)(4 * (pAmplifier + 1)));
      super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
   }

   public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeModifierManager pAttributeMap, int pAmplifier) {
      pLivingEntity.setAbsorptionAmount(pLivingEntity.getAbsorptionAmount() + (float)(4 * (pAmplifier + 1)));
      super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
   }
}