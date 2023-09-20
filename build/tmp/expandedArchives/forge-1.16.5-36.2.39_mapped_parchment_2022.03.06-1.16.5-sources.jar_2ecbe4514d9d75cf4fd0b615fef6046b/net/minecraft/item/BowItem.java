package net.minecraft.item;

import java.util.function.Predicate;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class BowItem extends ShootableItem implements IVanishable {
   public BowItem(Item.Properties p_i48522_1_) {
      super(p_i48522_1_);
   }

   /**
    * Called when the player stops using an Item (stops holding the right mouse button).
    */
   public void releaseUsing(ItemStack pStack, World pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
      if (pEntityLiving instanceof PlayerEntity) {
         PlayerEntity playerentity = (PlayerEntity)pEntityLiving;
         boolean flag = playerentity.abilities.instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, pStack) > 0;
         ItemStack itemstack = playerentity.getProjectile(pStack);

         int i = this.getUseDuration(pStack) - pTimeLeft;
         i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(pStack, pLevel, playerentity, i, !itemstack.isEmpty() || flag);
         if (i < 0) return;

         if (!itemstack.isEmpty() || flag) {
            if (itemstack.isEmpty()) {
               itemstack = new ItemStack(Items.ARROW);
            }

            float f = getPowerForTime(i);
            if (!((double)f < 0.1D)) {
               boolean flag1 = playerentity.abilities.instabuild || (itemstack.getItem() instanceof ArrowItem && ((ArrowItem)itemstack.getItem()).isInfinite(itemstack, pStack, playerentity));
               if (!pLevel.isClientSide) {
                  ArrowItem arrowitem = (ArrowItem)(itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                  AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(pLevel, itemstack, playerentity);
                  abstractarrowentity = customArrow(abstractarrowentity);
                  abstractarrowentity.shootFromRotation(playerentity, playerentity.xRot, playerentity.yRot, 0.0F, f * 3.0F, 1.0F);
                  if (f == 1.0F) {
                     abstractarrowentity.setCritArrow(true);
                  }

                  int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, pStack);
                  if (j > 0) {
                     abstractarrowentity.setBaseDamage(abstractarrowentity.getBaseDamage() + (double)j * 0.5D + 0.5D);
                  }

                  int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, pStack);
                  if (k > 0) {
                     abstractarrowentity.setKnockback(k);
                  }

                  if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, pStack) > 0) {
                     abstractarrowentity.setSecondsOnFire(100);
                  }

                  pStack.hurtAndBreak(1, playerentity, (p_220009_1_) -> {
                     p_220009_1_.broadcastBreakEvent(playerentity.getUsedItemHand());
                  });
                  if (flag1 || playerentity.abilities.instabuild && (itemstack.getItem() == Items.SPECTRAL_ARROW || itemstack.getItem() == Items.TIPPED_ARROW)) {
                     abstractarrowentity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                  }

                  pLevel.addFreshEntity(abstractarrowentity);
               }

               pLevel.playSound((PlayerEntity)null, playerentity.getX(), playerentity.getY(), playerentity.getZ(), SoundEvents.ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
               if (!flag1 && !playerentity.abilities.instabuild) {
                  itemstack.shrink(1);
                  if (itemstack.isEmpty()) {
                     playerentity.inventory.removeItem(itemstack);
                  }
               }

               playerentity.awardStat(Stats.ITEM_USED.get(this));
            }
         }
      }
   }

   /**
    * Gets the velocity of the arrow entity from the bow's charge
    */
   public static float getPowerForTime(int pCharge) {
      float f = (float)pCharge / 20.0F;
      f = (f * f + f * 2.0F) / 3.0F;
      if (f > 1.0F) {
         f = 1.0F;
      }

      return f;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 72000;
   }

   /**
    * returns the action that specifies what animation to play when the items is being used
    */
   public UseAction getUseAnimation(ItemStack pStack) {
      return UseAction.BOW;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      boolean flag = !pPlayer.getProjectile(itemstack).isEmpty();

      ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, pLevel, pPlayer, pHand, flag);
      if (ret != null) return ret;

      if (!pPlayer.abilities.instabuild && !flag) {
         return ActionResult.fail(itemstack);
      } else {
         pPlayer.startUsingItem(pHand);
         return ActionResult.consume(itemstack);
      }
   }

   /**
    * Get the predicate to match ammunition when searching the player's inventory, not their main/offhand
    */
   public Predicate<ItemStack> getAllSupportedProjectiles() {
      return ARROW_ONLY;
   }

   public AbstractArrowEntity customArrow(AbstractArrowEntity arrow) {
      return arrow;
   }

   public int getDefaultProjectileRange() {
      return 15;
   }
}
