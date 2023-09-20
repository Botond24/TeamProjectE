package net.minecraft.client.gui.screen.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreativeCraftingListener implements IContainerListener {
   private final Minecraft minecraft;

   public CreativeCraftingListener(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void refreshContainer(Container pContainerToSend, NonNullList<ItemStack> pItemsList) {
   }

   /**
    * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
    * contents of that slot.
    */
   public void slotChanged(Container pContainerToSend, int pSlotInd, ItemStack pStack) {
      this.minecraft.gameMode.handleCreativeModeItemAdd(pStack, pSlotInd);
   }

   public void setContainerData(Container pContainer, int pVarToUpdate, int pNewValue) {
   }
}