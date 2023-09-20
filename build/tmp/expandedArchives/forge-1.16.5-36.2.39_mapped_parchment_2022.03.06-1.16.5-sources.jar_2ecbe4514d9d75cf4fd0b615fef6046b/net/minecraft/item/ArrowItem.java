package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.world.World;

public class ArrowItem extends Item {
   public ArrowItem(Item.Properties p_i48531_1_) {
      super(p_i48531_1_);
   }

   public AbstractArrowEntity createArrow(World pLevel, ItemStack pStack, LivingEntity pShooter) {
      ArrowEntity arrowentity = new ArrowEntity(pLevel, pShooter);
      arrowentity.setEffectsFromItem(pStack);
      return arrowentity;
   }

   public boolean isInfinite(ItemStack stack, ItemStack bow, net.minecraft.entity.player.PlayerEntity player) {
      int enchant = net.minecraft.enchantment.EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.enchantment.Enchantments.INFINITY_ARROWS, bow);
      return enchant <= 0 ? false : this.getClass() == ArrowItem.class;
   }
}
