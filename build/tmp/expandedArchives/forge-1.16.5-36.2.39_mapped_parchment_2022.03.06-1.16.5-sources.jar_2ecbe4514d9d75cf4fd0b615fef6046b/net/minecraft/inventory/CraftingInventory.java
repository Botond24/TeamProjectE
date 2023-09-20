package net.minecraft.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.NonNullList;

public class CraftingInventory implements IInventory, IRecipeHelperPopulator {
   private final NonNullList<ItemStack> items;
   private final int width;
   private final int height;
   private final Container menu;

   public CraftingInventory(Container pMenu, int pWidth, int pHeight) {
      this.items = NonNullList.withSize(pWidth * pHeight, ItemStack.EMPTY);
      this.menu = pMenu;
      this.width = pWidth;
      this.height = pHeight;
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
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
      return pIndex >= this.getContainerSize() ? ItemStack.EMPTY : this.items.get(pIndex);
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      return ItemStackHelper.takeItem(this.items, pIndex);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      ItemStack itemstack = ItemStackHelper.removeItem(this.items, pIndex, pCount);
      if (!itemstack.isEmpty()) {
         this.menu.slotsChanged(this);
      }

      return itemstack;
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.items.set(pIndex, pStack);
      this.menu.slotsChanged(this);
   }

   /**
    * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return true;
   }

   public void clearContent() {
      this.items.clear();
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   public void fillStackedContents(RecipeItemHelper pHelper) {
      for(ItemStack itemstack : this.items) {
         pHelper.accountSimpleStack(itemstack);
      }

   }
}