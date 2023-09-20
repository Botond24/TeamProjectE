package net.minecraft.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DrinkHelper {
   public static ActionResult<ItemStack> useDrink(World p_234707_0_, PlayerEntity p_234707_1_, Hand p_234707_2_) {
      p_234707_1_.startUsingItem(p_234707_2_);
      return ActionResult.consume(p_234707_1_.getItemInHand(p_234707_2_));
   }

   public static ItemStack createFilledResult(ItemStack pEmptyStack, PlayerEntity pPlayer, ItemStack pFilledStack, boolean pPreventDuplicates) {
      boolean flag = pPlayer.abilities.instabuild;
      if (pPreventDuplicates && flag) {
         if (!pPlayer.inventory.contains(pFilledStack)) {
            pPlayer.inventory.add(pFilledStack);
         }

         return pEmptyStack;
      } else {
         if (!flag) {
            pEmptyStack.shrink(1);
         }

         if (pEmptyStack.isEmpty()) {
            return pFilledStack;
         } else {
            if (!pPlayer.inventory.add(pFilledStack)) {
               pPlayer.drop(pFilledStack, false);
            }

            return pEmptyStack;
         }
      }
   }

   public static ItemStack createFilledResult(ItemStack pEmptyStack, PlayerEntity pPlayer, ItemStack pFilledStack) {
      return createFilledResult(pEmptyStack, pPlayer, pFilledStack, true);
   }
}