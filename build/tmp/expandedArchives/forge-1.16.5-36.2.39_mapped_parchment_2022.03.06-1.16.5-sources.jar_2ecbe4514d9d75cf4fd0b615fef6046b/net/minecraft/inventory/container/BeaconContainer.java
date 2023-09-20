package net.minecraft.inventory.container;

import javax.annotation.Nullable;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BeaconContainer extends Container {
   private final IInventory beacon = new Inventory(1) {
      /**
       * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
       * guis use Slot.isItemValid
       */
      public boolean canPlaceItem(int pIndex, ItemStack pStack) {
         return pStack.getItem().is(ItemTags.BEACON_PAYMENT_ITEMS);
      }

      /**
       * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
       */
      public int getMaxStackSize() {
         return 1;
      }
   };
   private final BeaconContainer.BeaconSlot paymentSlot;
   private final IWorldPosCallable access;
   private final IIntArray beaconData;

   public BeaconContainer(int pContainerId, IInventory pContainer) {
      this(pContainerId, pContainer, new IntArray(3), IWorldPosCallable.NULL);
   }

   public BeaconContainer(int pContainerId, IInventory pContainer, IIntArray pBeaconData, IWorldPosCallable pAccess) {
      super(ContainerType.BEACON, pContainerId);
      checkContainerDataCount(pBeaconData, 3);
      this.beaconData = pBeaconData;
      this.access = pAccess;
      this.paymentSlot = new BeaconContainer.BeaconSlot(this.beacon, 0, 136, 110);
      this.addSlot(this.paymentSlot);
      this.addDataSlots(pBeaconData);
      int i = 36;
      int j = 137;

      for(int k = 0; k < 3; ++k) {
         for(int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(pContainer, l + k * 9 + 9, 36 + l * 18, 137 + k * 18));
         }
      }

      for(int i1 = 0; i1 < 9; ++i1) {
         this.addSlot(new Slot(pContainer, i1, 36 + i1 * 18, 195));
      }

   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      if (!pPlayer.level.isClientSide) {
         ItemStack itemstack = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
         if (!itemstack.isEmpty()) {
            pPlayer.drop(itemstack, false);
         }

      }
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.BEACON);
   }

   public void setData(int pId, int pData) {
      super.setData(pId, pData);
      this.broadcastChanges();
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
         if (pIndex == 0) {
            if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (this.moveItemStackTo(itemstack1, 0, 1, false)) { //Forge Fix Shift Clicking in beacons with stacks larger then 1.
            return ItemStack.EMPTY;
         } else if (pIndex >= 1 && pIndex < 28) {
            if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 28 && pIndex < 37) {
            if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 1, 37, false)) {
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

   @OnlyIn(Dist.CLIENT)
   public int getLevels() {
      return this.beaconData.get(0);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Effect getPrimaryEffect() {
      return Effect.byId(this.beaconData.get(1));
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Effect getSecondaryEffect() {
      return Effect.byId(this.beaconData.get(2));
   }

   public void updateEffects(int pPrimaryEffectStrength, int pSecondaryEffectStrength) {
      if (this.paymentSlot.hasItem()) {
         this.beaconData.set(1, pPrimaryEffectStrength);
         this.beaconData.set(2, pSecondaryEffectStrength);
         this.paymentSlot.remove(1);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public boolean hasPayment() {
      return !this.beacon.getItem(0).isEmpty();
   }

   class BeaconSlot extends Slot {
      public BeaconSlot(IInventory pContainer, int pContainerIndex, int pXPosition, int pYPosition) {
         super(pContainer, pContainerIndex, pXPosition, pYPosition);
      }

      /**
       * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
       */
      public boolean mayPlace(ItemStack pStack) {
         return pStack.getItem().is(ItemTags.BEACON_PAYMENT_ITEMS);
      }

      /**
       * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
       * case of armor slots)
       */
      public int getMaxStackSize() {
         return 1;
      }
   }
}
