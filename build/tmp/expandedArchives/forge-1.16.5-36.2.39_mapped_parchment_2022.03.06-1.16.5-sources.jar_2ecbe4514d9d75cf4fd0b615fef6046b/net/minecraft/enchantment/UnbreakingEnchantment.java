package net.minecraft.enchantment;

import java.util.Random;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public class UnbreakingEnchantment extends Enchantment {
   protected UnbreakingEnchantment(Enchantment.Rarity pRarity, EquipmentSlotType... pApplicableSlots) {
      super(pRarity, EnchantmentType.BREAKABLE, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 5 + (pEnchantmentLevel - 1) * 8;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return super.getMinCost(pEnchantmentLevel) + 50;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 3;
   }

   /**
    * Determines if this enchantment can be applied to a specific ItemStack.
    * @param pStack The ItemStack to test.
    */
   public boolean canEnchant(ItemStack pStack) {
      return pStack.isDamageableItem() ? true : super.canEnchant(pStack);
   }

   /**
    * Used by ItemStack.attemptDamageItem. Randomly determines if a point of damage should be negated using the
    * enchantment level (par1). If the ItemStack is Armor then there is a flat 60% chance for damage to be negated no
    * matter the enchantment level, otherwise there is a 1-(par/1) chance for damage to be negated.
    */
   public static boolean shouldIgnoreDurabilityDrop(ItemStack pStack, int pLevel, Random pRand) {
      if (pStack.getItem() instanceof ArmorItem && pRand.nextFloat() < 0.6F) {
         return false;
      } else {
         return pRand.nextInt(pLevel + 1) > 0;
      }
   }
}