package net.minecraft.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;

public class Inventory implements IInventory, IRecipeHelperPopulator {
   private final int size;
   private final NonNullList<ItemStack> items;
   private List<IInventoryChangedListener> listeners;

   public Inventory(int pSize) {
      this.size = pSize;
      this.items = NonNullList.withSize(pSize, ItemStack.EMPTY);
   }

   public Inventory(ItemStack... pItems) {
      this.size = pItems.length;
      this.items = NonNullList.of(ItemStack.EMPTY, pItems);
   }

   /**
    * Add a listener that will be notified when any item in this inventory is modified.
    */
   public void addListener(IInventoryChangedListener pListener) {
      if (this.listeners == null) {
         this.listeners = Lists.newArrayList();
      }

      this.listeners.add(pListener);
   }

   /**
    * removes the specified IInvBasic from receiving further change notices
    */
   public void removeListener(IInventoryChangedListener pListener) {
      this.listeners.remove(pListener);
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      return pIndex >= 0 && pIndex < this.items.size() ? this.items.get(pIndex) : ItemStack.EMPTY;
   }

   public List<ItemStack> removeAllItems() {
      List<ItemStack> list = this.items.stream().filter((p_233544_0_) -> {
         return !p_233544_0_.isEmpty();
      }).collect(Collectors.toList());
      this.clearContent();
      return list;
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      ItemStack itemstack = ItemStackHelper.removeItem(this.items, pIndex, pCount);
      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   public ItemStack removeItemType(Item pItem, int pAmount) {
      ItemStack itemstack = new ItemStack(pItem, 0);

      for(int i = this.size - 1; i >= 0; --i) {
         ItemStack itemstack1 = this.getItem(i);
         if (itemstack1.getItem().equals(pItem)) {
            int j = pAmount - itemstack.getCount();
            ItemStack itemstack2 = itemstack1.split(j);
            itemstack.grow(itemstack2.getCount());
            if (itemstack.getCount() == pAmount) {
               break;
            }
         }
      }

      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   public ItemStack addItem(ItemStack pStack) {
      ItemStack itemstack = pStack.copy();
      this.moveItemToOccupiedSlotsWithSameType(itemstack);
      if (itemstack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.moveItemToEmptySlots(itemstack);
         return itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
      }
   }

   public boolean canAddItem(ItemStack pStack) {
      boolean flag = false;

      for(ItemStack itemstack : this.items) {
         if (itemstack.isEmpty() || this.isSameItem(itemstack, pStack) && itemstack.getCount() < itemstack.getMaxStackSize()) {
            flag = true;
            break;
         }
      }

      return flag;
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      ItemStack itemstack = this.items.get(pIndex);
      if (itemstack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.items.set(pIndex, ItemStack.EMPTY);
         return itemstack;
      }
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.items.set(pIndex, pStack);
      if (!pStack.isEmpty() && pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.size;
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
    * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
      if (this.listeners != null) {
         for(IInventoryChangedListener iinventorychangedlistener : this.listeners) {
            iinventorychangedlistener.containerChanged(this);
         }
      }

   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return true;
   }

   public void clearContent() {
      this.items.clear();
      this.setChanged();
   }

   public void fillStackedContents(RecipeItemHelper pHelper) {
      for(ItemStack itemstack : this.items) {
         pHelper.accountStack(itemstack);
      }

   }

   public String toString() {
      return this.items.stream().filter((p_223371_0_) -> {
         return !p_223371_0_.isEmpty();
      }).collect(Collectors.toList()).toString();
   }

   private void moveItemToEmptySlots(ItemStack pStack) {
      for(int i = 0; i < this.size; ++i) {
         ItemStack itemstack = this.getItem(i);
         if (itemstack.isEmpty()) {
            this.setItem(i, pStack.copy());
            pStack.setCount(0);
            return;
         }
      }

   }

   private void moveItemToOccupiedSlotsWithSameType(ItemStack pStack) {
      for(int i = 0; i < this.size; ++i) {
         ItemStack itemstack = this.getItem(i);
         if (this.isSameItem(itemstack, pStack)) {
            this.moveItemsBetweenStacks(pStack, itemstack);
            if (pStack.isEmpty()) {
               return;
            }
         }
      }

   }

   private boolean isSameItem(ItemStack p_233540_1_, ItemStack p_233540_2_) {
      return p_233540_1_.getItem() == p_233540_2_.getItem() && ItemStack.tagMatches(p_233540_1_, p_233540_2_);
   }

   private void moveItemsBetweenStacks(ItemStack p_223373_1_, ItemStack p_223373_2_) {
      int i = Math.min(this.getMaxStackSize(), p_223373_2_.getMaxStackSize());
      int j = Math.min(p_223373_1_.getCount(), i - p_223373_2_.getCount());
      if (j > 0) {
         p_223373_2_.grow(j);
         p_223373_1_.shrink(j);
         this.setChanged();
      }

   }

   public void fromTag(ListNBT pContainerNbt) {
      for(int i = 0; i < pContainerNbt.size(); ++i) {
         ItemStack itemstack = ItemStack.of(pContainerNbt.getCompound(i));
         if (!itemstack.isEmpty()) {
            this.addItem(itemstack);
         }
      }

   }

   public ListNBT createTag() {
      ListNBT listnbt = new ListNBT();

      for(int i = 0; i < this.getContainerSize(); ++i) {
         ItemStack itemstack = this.getItem(i);
         if (!itemstack.isEmpty()) {
            listnbt.add(itemstack.save(new CompoundNBT()));
         }
      }

      return listnbt;
   }
}