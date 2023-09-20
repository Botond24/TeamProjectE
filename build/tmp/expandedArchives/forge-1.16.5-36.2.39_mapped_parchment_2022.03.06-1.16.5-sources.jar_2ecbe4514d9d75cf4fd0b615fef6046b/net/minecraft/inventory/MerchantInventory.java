package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MerchantInventory implements IInventory {
   private final IMerchant merchant;
   private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
   @Nullable
   private MerchantOffer activeOffer;
   private int selectionHint;
   private int futureXp;

   public MerchantInventory(IMerchant pMerchant) {
      this.merchant = pMerchant;
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.itemStacks.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.itemStacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      return this.itemStacks.get(pIndex);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      ItemStack itemstack = this.itemStacks.get(pIndex);
      if (pIndex == 2 && !itemstack.isEmpty()) {
         return ItemStackHelper.removeItem(this.itemStacks, pIndex, itemstack.getCount());
      } else {
         ItemStack itemstack1 = ItemStackHelper.removeItem(this.itemStacks, pIndex, pCount);
         if (!itemstack1.isEmpty() && this.isPaymentSlot(pIndex)) {
            this.updateSellItem();
         }

         return itemstack1;
      }
   }

   /**
    * if par1 slot has changed, does resetRecipeAndSlots need to be called?
    */
   private boolean isPaymentSlot(int pSlot) {
      return pSlot == 0 || pSlot == 1;
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      return ItemStackHelper.takeItem(this.itemStacks, pIndex);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.itemStacks.set(pIndex, pStack);
      if (!pStack.isEmpty() && pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

      if (this.isPaymentSlot(pIndex)) {
         this.updateSellItem();
      }

   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return this.merchant.getTradingPlayer() == pPlayer;
   }

   /**
    * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
      this.updateSellItem();
   }

   public void updateSellItem() {
      this.activeOffer = null;
      ItemStack itemstack;
      ItemStack itemstack1;
      if (this.itemStacks.get(0).isEmpty()) {
         itemstack = this.itemStacks.get(1);
         itemstack1 = ItemStack.EMPTY;
      } else {
         itemstack = this.itemStacks.get(0);
         itemstack1 = this.itemStacks.get(1);
      }

      if (itemstack.isEmpty()) {
         this.setItem(2, ItemStack.EMPTY);
         this.futureXp = 0;
      } else {
         MerchantOffers merchantoffers = this.merchant.getOffers();
         if (!merchantoffers.isEmpty()) {
            MerchantOffer merchantoffer = merchantoffers.getRecipeFor(itemstack, itemstack1, this.selectionHint);
            if (merchantoffer == null || merchantoffer.isOutOfStock()) {
               this.activeOffer = merchantoffer;
               merchantoffer = merchantoffers.getRecipeFor(itemstack1, itemstack, this.selectionHint);
            }

            if (merchantoffer != null && !merchantoffer.isOutOfStock()) {
               this.activeOffer = merchantoffer;
               this.setItem(2, merchantoffer.assemble());
               this.futureXp = merchantoffer.getXp();
            } else {
               this.setItem(2, ItemStack.EMPTY);
               this.futureXp = 0;
            }
         }

         this.merchant.notifyTradeUpdated(this.getItem(2));
      }
   }

   @Nullable
   public MerchantOffer getActiveOffer() {
      return this.activeOffer;
   }

   public void setSelectionHint(int pCurrentRecipeIndex) {
      this.selectionHint = pCurrentRecipeIndex;
      this.updateSellItem();
   }

   public void clearContent() {
      this.itemStacks.clear();
   }

   @OnlyIn(Dist.CLIENT)
   public int getFutureXp() {
      return this.futureXp;
   }
}