package net.minecraft.inventory.container;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.stats.Stats;

public class MerchantResultSlot extends Slot {
   private final MerchantInventory slots;
   private final PlayerEntity player;
   private int removeCount;
   private final IMerchant merchant;

   public MerchantResultSlot(PlayerEntity pPlayer, IMerchant pMerchant, MerchantInventory pSlots, int pSlot, int pXPosition, int pYPosition) {
      super(pSlots, pSlot, pXPosition, pYPosition);
      this.player = pPlayer;
      this.merchant = pMerchant;
      this.slots = pSlots;
   }

   /**
    * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
    */
   public boolean mayPlace(ItemStack pStack) {
      return false;
   }

   /**
    * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new stack.
    */
   public ItemStack remove(int pAmount) {
      if (this.hasItem()) {
         this.removeCount += Math.min(pAmount, this.getItem().getCount());
      }

      return super.remove(pAmount);
   }

   /**
    * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
    * internal count then calls onCrafting(item).
    */
   protected void onQuickCraft(ItemStack pStack, int pAmount) {
      this.removeCount += pAmount;
      this.checkTakeAchievements(pStack);
   }

   /**
    * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
    */
   protected void checkTakeAchievements(ItemStack pStack) {
      pStack.onCraftedBy(this.player.level, this.player, this.removeCount);
      this.removeCount = 0;
   }

   public ItemStack onTake(PlayerEntity pPlayer, ItemStack pStack) {
      this.checkTakeAchievements(pStack);
      MerchantOffer merchantoffer = this.slots.getActiveOffer();
      if (merchantoffer != null) {
         ItemStack itemstack = this.slots.getItem(0);
         ItemStack itemstack1 = this.slots.getItem(1);
         if (merchantoffer.take(itemstack, itemstack1) || merchantoffer.take(itemstack1, itemstack)) {
            this.merchant.notifyTrade(merchantoffer);
            pPlayer.awardStat(Stats.TRADED_WITH_VILLAGER);
            this.slots.setItem(0, itemstack);
            this.slots.setItem(1, itemstack1);
         }

         this.merchant.overrideXp(this.merchant.getVillagerXp() + merchantoffer.getXp());
      }

      return pStack;
   }
}