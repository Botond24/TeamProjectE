package net.minecraft.potion;

import net.minecraft.util.text.TextFormatting;

public enum EffectType {
   BENEFICIAL(TextFormatting.BLUE),
   HARMFUL(TextFormatting.RED),
   NEUTRAL(TextFormatting.BLUE);

   private final TextFormatting tooltipFormatting;

   private EffectType(TextFormatting pTooltipFormatting) {
      this.tooltipFormatting = pTooltipFormatting;
   }

   public TextFormatting getTooltipFormatting() {
      return this.tooltipFormatting;
   }
}