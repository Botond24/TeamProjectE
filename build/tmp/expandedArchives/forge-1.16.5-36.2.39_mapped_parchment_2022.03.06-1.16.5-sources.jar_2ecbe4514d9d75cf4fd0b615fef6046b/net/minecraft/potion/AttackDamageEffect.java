package net.minecraft.potion;

import net.minecraft.entity.ai.attributes.AttributeModifier;

public class AttackDamageEffect extends Effect {
   protected final double multiplier;

   protected AttackDamageEffect(EffectType pCategory, int pColor, double pMultiplier) {
      super(pCategory, pColor);
      this.multiplier = pMultiplier;
   }

   public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
      return this.multiplier * (double)(pAmplifier + 1);
   }
}