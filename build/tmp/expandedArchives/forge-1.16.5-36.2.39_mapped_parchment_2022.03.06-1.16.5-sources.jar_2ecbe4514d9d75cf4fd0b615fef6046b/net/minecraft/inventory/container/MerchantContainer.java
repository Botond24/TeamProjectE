package net.minecraft.inventory.container;

import net.minecraft.entity.Entity;
import net.minecraft.entity.NPCMerchant;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MerchantContainer extends Container {
   private final IMerchant trader;
   private final MerchantInventory tradeContainer;
   @OnlyIn(Dist.CLIENT)
   private int merchantLevel;
   @OnlyIn(Dist.CLIENT)
   private boolean showProgressBar;
   @OnlyIn(Dist.CLIENT)
   private boolean canRestock;

   public MerchantContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, new NPCMerchant(pPlayerInventory.player));
   }

   public MerchantContainer(int pContainerId, PlayerInventory pPlayerInventory, IMerchant pTrader) {
      super(ContainerType.MERCHANT, pContainerId);
      this.trader = pTrader;
      this.tradeContainer = new MerchantInventory(pTrader);
      this.addSlot(new Slot(this.tradeContainer, 0, 136, 37));
      this.addSlot(new Slot(this.tradeContainer, 1, 162, 37));
      this.addSlot(new MerchantResultSlot(pPlayerInventory.player, pTrader, this.tradeContainer, 2, 220, 37));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 108 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 108 + k * 18, 142));
      }

   }

   @OnlyIn(Dist.CLIENT)
   public void setShowProgressBar(boolean pShowProgressBar) {
      this.showProgressBar = pShowProgressBar;
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      this.tradeContainer.updateSellItem();
      super.slotsChanged(pInventory);
   }

   public void setSelectionHint(int pCurrentRecipeIndex) {
      this.tradeContainer.setSelectionHint(pCurrentRecipeIndex);
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return this.trader.getTradingPlayer() == pPlayer;
   }

   @OnlyIn(Dist.CLIENT)
   public int getTraderXp() {
      return this.trader.getVillagerXp();
   }

   @OnlyIn(Dist.CLIENT)
   public int getFutureTraderXp() {
      return this.tradeContainer.getFutureXp();
   }

   @OnlyIn(Dist.CLIENT)
   public void setXp(int pXp) {
      this.trader.overrideXp(pXp);
   }

   @OnlyIn(Dist.CLIENT)
   public int getTraderLevel() {
      return this.merchantLevel;
   }

   @OnlyIn(Dist.CLIENT)
   public void setMerchantLevel(int pLevel) {
      this.merchantLevel = pLevel;
   }

   @OnlyIn(Dist.CLIENT)
   public void setCanRestock(boolean pCanRestock) {
      this.canRestock = pCanRestock;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean canRestock() {
      return this.canRestock;
   }

   /**
    * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
    * null for the initial slot that was double-clicked.
    */
   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return false;
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (pIndex == 2) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
            this.playTradeSound();
         } else if (pIndex != 0 && pIndex != 1) {
            if (pIndex >= 3 && pIndex < 30) {
               if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
      }

      return itemstack;
   }

   private void playTradeSound() {
      if (!this.trader.getLevel().isClientSide) {
         Entity entity = (Entity)this.trader;
         this.trader.getLevel().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundCategory.NEUTRAL, 1.0F, 1.0F, false);
      }

   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.trader.setTradingPlayer((PlayerEntity)null);
      if (!this.trader.getLevel().isClientSide) {
         if (!pPlayer.isAlive() || pPlayer instanceof ServerPlayerEntity && ((ServerPlayerEntity)pPlayer).hasDisconnected()) {
            ItemStack itemstack = this.tradeContainer.removeItemNoUpdate(0);
            if (!itemstack.isEmpty()) {
               pPlayer.drop(itemstack, false);
            }

            itemstack = this.tradeContainer.removeItemNoUpdate(1);
            if (!itemstack.isEmpty()) {
               pPlayer.drop(itemstack, false);
            }
         } else {
            pPlayer.inventory.placeItemBackInInventory(pPlayer.level, this.tradeContainer.removeItemNoUpdate(0));
            pPlayer.inventory.placeItemBackInInventory(pPlayer.level, this.tradeContainer.removeItemNoUpdate(1));
         }

      }
   }

   public void tryMoveItems(int pSelectedMerchantRecipe) {
      if (this.getOffers().size() > pSelectedMerchantRecipe) {
         ItemStack itemstack = this.tradeContainer.getItem(0);
         if (!itemstack.isEmpty()) {
            if (!this.moveItemStackTo(itemstack, 3, 39, true)) {
               return;
            }

            this.tradeContainer.setItem(0, itemstack);
         }

         ItemStack itemstack1 = this.tradeContainer.getItem(1);
         if (!itemstack1.isEmpty()) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return;
            }

            this.tradeContainer.setItem(1, itemstack1);
         }

         if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
            ItemStack itemstack2 = this.getOffers().get(pSelectedMerchantRecipe).getCostA();
            this.moveFromInventoryToPaymentSlot(0, itemstack2);
            ItemStack itemstack3 = this.getOffers().get(pSelectedMerchantRecipe).getCostB();
            this.moveFromInventoryToPaymentSlot(1, itemstack3);
         }

      }
   }

   private void moveFromInventoryToPaymentSlot(int pPaymentSlotIndex, ItemStack pPaymentSlot) {
      if (!pPaymentSlot.isEmpty()) {
         for(int i = 3; i < 39; ++i) {
            ItemStack itemstack = this.slots.get(i).getItem();
            if (!itemstack.isEmpty() && this.isSameItem(pPaymentSlot, itemstack)) {
               ItemStack itemstack1 = this.tradeContainer.getItem(pPaymentSlotIndex);
               int j = itemstack1.isEmpty() ? 0 : itemstack1.getCount();
               int k = Math.min(pPaymentSlot.getMaxStackSize() - j, itemstack.getCount());
               ItemStack itemstack2 = itemstack.copy();
               int l = j + k;
               itemstack.shrink(k);
               itemstack2.setCount(l);
               this.tradeContainer.setItem(pPaymentSlotIndex, itemstack2);
               if (l >= pPaymentSlot.getMaxStackSize()) {
                  break;
               }
            }
         }
      }

   }

   private boolean isSameItem(ItemStack pStack1, ItemStack pStack2) {
      return pStack1.getItem() == pStack2.getItem() && ItemStack.tagMatches(pStack1, pStack2);
   }

   /**
    * net.minecraft.client.network.play.ClientPlayNetHandler uses this to set offers for the client side
    * MerchantContainer
    */
   @OnlyIn(Dist.CLIENT)
   public void setOffers(MerchantOffers pOffers) {
      this.trader.overrideOffers(pOffers);
   }

   public MerchantOffers getOffers() {
      return this.trader.getOffers();
   }

   @OnlyIn(Dist.CLIENT)
   public boolean showProgressBar() {
      return this.showProgressBar;
   }
}