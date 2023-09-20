package net.minecraft.enchantment;

import net.minecraft.inventory.EquipmentSlotType;

public class PowerEnchantment extends Enchantment {
   public PowerEnchantment(Enchantment.Rarity pRarity, EquipmentSlotType... pApplicableSlots) {
      super(pRarity, EnchantmentType.BOW, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 1 + (pEnchantmentLevel - 1) * 10;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 15;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 5;
   }
}