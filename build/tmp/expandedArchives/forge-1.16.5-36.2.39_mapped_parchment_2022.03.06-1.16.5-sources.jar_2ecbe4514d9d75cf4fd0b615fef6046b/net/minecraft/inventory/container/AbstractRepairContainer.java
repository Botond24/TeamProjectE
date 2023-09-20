package net.minecraft.inventory.container;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

public abstract class AbstractRepairContainer extends Container {
   protected final CraftResultInventory resultSlots = new CraftResultInventory();
   protected final IInventory inputSlots = new Inventory(2) {
      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         super.setChanged();
         AbstractRepairContainer.this.slotsChanged(this);
      }
   };
   protected final IWorldPosCallable access;
   protected final PlayerEntity player;

   protected abstract boolean mayPickup(PlayerEntity pPlayer, boolean pHasStack);

   protected abstract ItemStack onTake(PlayerEntity pPlayer, ItemStack pInputItem);

   protected abstract boolean isValidBlock(BlockState pState);

   public AbstractRepairContainer(@Nullable ContainerType<?> pType, int pContainerId, PlayerInventory pPlayerInventory, IWorldPosCallable pAccess) {
      super(pType, pContainerId);
      this.access = pAccess;
      this.player = pPlayerInventory.player;
      this.addSlot(new Slot(this.inputSlots, 0, 27, 47));
      this.addSlot(new Slot(this.inputSlots, 1, 76, 47));
      this.addSlot(new Slot(this.resultSlots, 2, 134, 47) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return false;
         }

         /**
          * Return whether this slot's stack can be taken from this slot.
          */
         public boolean mayPickup(PlayerEntity pPlayer) {
            return AbstractRepairContainer.this.mayPickup(pPlayer, this.hasItem());
         }

         public ItemStack onTake(PlayerEntity pPlayer, ItemStack pStack) {
            return AbstractRepairContainer.this.onTake(pPlayer, pStack);
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

   }

   /**
    * called when the Anvil Input Slot changes, calculates the new result and puts it in the output slot
    */
   public abstract void createResult();

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      super.slotsChanged(pInventory);
      if (pInventory == this.inputSlots) {
         this.createResult();
      }

   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.access.execute((p_234647_2_, p_234647_3_) -> {
         this.clearContainer(pPlayer, p_234647_2_, this.inputSlots);
      });
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return this.access.evaluate((p_234646_2_, p_234646_3_) -> {
         return !this.isValidBlock(p_234646_2_.getBlockState(p_234646_3_)) ? false : pPlayer.distanceToSqr((double)p_234646_3_.getX() + 0.5D, (double)p_234646_3_.getY() + 0.5D, (double)p_234646_3_.getZ() + 0.5D) <= 64.0D;
      }, true);
   }

   protected boolean shouldQuickMoveToAdditionalSlot(ItemStack pStack) {
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
         } else if (pIndex != 0 && pIndex != 1) {
            if (pIndex >= 3 && pIndex < 39) {
               int i = this.shouldQuickMoveToAdditionalSlot(itemstack) ? 1 : 0;
               if (!this.moveItemStackTo(itemstack1, i, 2, false)) {
                  return ItemStack.EMPTY;
               }
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
}