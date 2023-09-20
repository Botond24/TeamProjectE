package net.minecraft.inventory.container;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IContainerListener {
   void refreshContainer(Container pContainerToSend, NonNullList<ItemStack> pItemsList);

   /**
    * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
    * contents of that slot.
    */
   void slotChanged(Container pContainerToSend, int pSlotInd, ItemStack pStack);

   void setContainerData(Container pContainer, int pVarToUpdate, int pNewValue);
}