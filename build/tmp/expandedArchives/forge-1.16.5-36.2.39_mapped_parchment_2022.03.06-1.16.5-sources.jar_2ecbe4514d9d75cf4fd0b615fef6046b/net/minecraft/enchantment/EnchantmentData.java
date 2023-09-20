package net.minecraft.enchantment;

import net.minecraft.util.WeightedRandom;

/**
 * Defines an immutable instance of an enchantment and its level.
 */
public class EnchantmentData extends WeightedRandom.Item {
   /** The enchantment being represented. */
   public final Enchantment enchantment;
   /** The level of the enchantment. */
   public final int level;

   public EnchantmentData(Enchantment pEnchantment, int pLevel) {
      super(pEnchantment.getRarity().getWeight());
      this.enchantment = pEnchantment;
      this.level = pLevel;
   }
}