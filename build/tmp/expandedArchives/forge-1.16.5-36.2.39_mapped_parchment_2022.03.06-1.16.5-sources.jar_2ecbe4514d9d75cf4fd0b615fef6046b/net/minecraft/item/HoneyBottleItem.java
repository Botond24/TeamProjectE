package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class HoneyBottleItem extends Item {
   public HoneyBottleItem(Item.Properties p_i225737_1_) {
      super(p_i225737_1_);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity pEntityLiving) {
      super.finishUsingItem(pStack, pLevel, pEntityLiving);
      if (pEntityLiving instanceof ServerPlayerEntity) {
         ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)pEntityLiving;
         CriteriaTriggers.CONSUME_ITEM.trigger(serverplayerentity, pStack);
         serverplayerentity.awardStat(Stats.ITEM_USED.get(this));
      }

      if (!pLevel.isClientSide) {
         pEntityLiving.removeEffect(Effects.POISON);
      }

      if (pStack.isEmpty()) {
         return new ItemStack(Items.GLASS_BOTTLE);
      } else {
         if (pEntityLiving instanceof PlayerEntity && !((PlayerEntity)pEntityLiving).abilities.instabuild) {
            ItemStack itemstack = new ItemStack(Items.GLASS_BOTTLE);
            PlayerEntity playerentity = (PlayerEntity)pEntityLiving;
            if (!playerentity.inventory.add(itemstack)) {
               playerentity.drop(itemstack, false);
            }
         }

         return pStack;
      }
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 40;
   }

   /**
    * returns the action that specifies what animation to play when the items is being used
    */
   public UseAction getUseAnimation(ItemStack pStack) {
      return UseAction.DRINK;
   }

   public SoundEvent getDrinkingSound() {
      return SoundEvents.HONEY_DRINK;
   }

   public SoundEvent getEatingSound() {
      return SoundEvents.HONEY_DRINK;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      return DrinkHelper.useDrink(pLevel, pPlayer, pHand);
   }
}