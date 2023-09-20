package net.minecraft.enchantment;

import net.minecraft.inventory.EquipmentSlotType;

public class SweepingEnchantment extends Enchantment {
   public SweepingEnchantment(Enchantment.Rarity pRarity, EquipmentSlotType... pApplicableSlots) {
      super(pRarity, EnchantmentType.WEAPON, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 5 + (pEnchantmentLevel - 1) * 9;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 15;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 3;
   }

   public static float getSweepingDamageRatio(int pLevel) {
      return 1.0F - 1.0F / (float)(pLevel + 1);
   }
}