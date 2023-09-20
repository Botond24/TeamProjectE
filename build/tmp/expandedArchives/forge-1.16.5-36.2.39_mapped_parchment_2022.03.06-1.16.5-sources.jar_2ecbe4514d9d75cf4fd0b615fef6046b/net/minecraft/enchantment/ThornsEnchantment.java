package net.minecraft.enchantment;

import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public class ThornsEnchantment extends Enchantment {
   public ThornsEnchantment(Enchantment.Rarity pRarity, EquipmentSlotType... pApplicableSlots) {
      super(pRarity, EnchantmentType.ARMOR_CHEST, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 10 + 20 * (pEnchantmentLevel - 1);
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
      return pStack.getItem() instanceof ArmorItem ? true : super.canEnchant(pStack);
   }

   /**
    * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be called.
    * @param pUser The user of the enchantment.
    * @param pAttacker The entity that attacked the user.
    * @param pLevel The level of the enchantment.
    */
   public void doPostHurt(LivingEntity pUser, Entity pAttacker, int pLevel) {
      Random random = pUser.getRandom();
      Entry<EquipmentSlotType, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.THORNS, pUser);
      if (shouldHit(pLevel, random)) {
         if (pAttacker != null) {
            pAttacker.hurt(DamageSource.thorns(pUser), (float)getDamage(pLevel, random));
         }

         if (entry != null) {
            entry.getValue().hurtAndBreak(2, pUser, (p_222183_1_) -> {
               p_222183_1_.broadcastBreakEvent(entry.getKey());
            });
         }
      }

   }

   public static boolean shouldHit(int pLevel, Random pRnd) {
      if (pLevel <= 0) {
         return false;
      } else {
         return pRnd.nextFloat() < 0.15F * (float)pLevel;
      }
   }

   public static int getDamage(int pLevel, Random pRnd) {
      return pLevel > 10 ? pLevel - 10 : 1 + pRnd.nextInt(4);
   }
}