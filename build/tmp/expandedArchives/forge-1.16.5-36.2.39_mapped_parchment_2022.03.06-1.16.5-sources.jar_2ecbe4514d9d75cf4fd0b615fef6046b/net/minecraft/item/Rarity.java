package net.minecraft.item;

import net.minecraft.util.text.TextFormatting;

public enum Rarity implements net.minecraftforge.common.IExtensibleEnum {
   COMMON(TextFormatting.WHITE),
   UNCOMMON(TextFormatting.YELLOW),
   RARE(TextFormatting.AQUA),
   EPIC(TextFormatting.LIGHT_PURPLE);

   public final TextFormatting color;

   private Rarity(TextFormatting pColor) {
      this.color = pColor;
   }

   public static Rarity create(String name, TextFormatting pColor) {
      throw new IllegalStateException("Enum not extended");
   }
}
