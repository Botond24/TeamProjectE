package net.minecraft.inventory;

import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IInventory extends IClearable {
   /**
    * Returns the number of slots in the inventory.
    */
   int getContainerSize();

   boolean isEmpty();

   /**
    * Returns the stack in the given slot.
    */
   ItemStack getItem(int pIndex);

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   ItemStack removeItem(int pIndex, int pCount);

   /**
    * Removes a stack from the given slot and returns it.
    */
   ItemStack removeItemNoUpdate(int pIndex);

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   void setItem(int pIndex, ItemStack pStack);

   /**
    * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
    */
   default int getMaxStackSize() {
      return 64;
   }

   /**
    * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   void setChanged();

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   boolean stillValid(PlayerEntity pPlayer);

   default void startOpen(PlayerEntity pPlayer) {
   }

   default void stopOpen(PlayerEntity pPlayer) {
   }

   /**
    * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
    * guis use Slot.isItemValid
    */
   default boolean canPlaceItem(int pIndex, ItemStack pStack) {
      return true;
   }

   /**
    * Returns the total amount of the specified item in this inventory. This method does not check for nbt.
    */
   default int countItem(Item pItem) {
      int i = 0;

      for(int j = 0; j < this.getContainerSize(); ++j) {
         ItemStack itemstack = this.getItem(j);
         if (itemstack.getItem().equals(pItem)) {
            i += itemstack.getCount();
         }
      }

      return i;
   }

   /**
    * Returns true if any item from the passed set exists in this inventory.
    */
   default boolean hasAnyOf(Set<Item> pSet) {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         ItemStack itemstack = this.getItem(i);
         if (pSet.contains(itemstack.getItem()) && itemstack.getCount() > 0) {
            return true;
         }
      }

      return false;
   }
}