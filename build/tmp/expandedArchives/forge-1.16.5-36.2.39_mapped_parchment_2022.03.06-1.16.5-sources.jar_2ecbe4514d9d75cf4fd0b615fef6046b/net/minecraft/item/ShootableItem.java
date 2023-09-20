package net.minecraft.item;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Hand;

public abstract class ShootableItem extends Item {
   public static final Predicate<ItemStack> ARROW_ONLY = (p_220002_0_) -> {
      return p_220002_0_.getItem().is(ItemTags.ARROWS);
   };
   public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or((p_220003_0_) -> {
      return p_220003_0_.getItem() == Items.FIREWORK_ROCKET;
   });

   public ShootableItem(Item.Properties p_i50040_1_) {
      super(p_i50040_1_);
   }

   public Predicate<ItemStack> getSupportedHeldProjectiles() {
      return this.getAllSupportedProjectiles();
   }

   /**
    * Get the predicate to match ammunition when searching the player's inventory, not their main/offhand
    */
   public abstract Predicate<ItemStack> getAllSupportedProjectiles();

   public static ItemStack getHeldProjectile(LivingEntity pShooter, Predicate<ItemStack> pIsAmmo) {
      if (pIsAmmo.test(pShooter.getItemInHand(Hand.OFF_HAND))) {
         return pShooter.getItemInHand(Hand.OFF_HAND);
      } else {
         return pIsAmmo.test(pShooter.getItemInHand(Hand.MAIN_HAND)) ? pShooter.getItemInHand(Hand.MAIN_HAND) : ItemStack.EMPTY;
      }
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return 1;
   }

   public abstract int getDefaultProjectileRange();
}