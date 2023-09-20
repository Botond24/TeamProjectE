package net.minecraft.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.EnderChestTileEntity;

public class EnderChestInventory extends Inventory {
   private EnderChestTileEntity activeChest;

   public EnderChestInventory() {
      super(27);
   }

   public void setActiveChest(EnderChestTileEntity pChestBlockEntity) {
      this.activeChest = pChestBlockEntity;
   }

   public void fromTag(ListNBT pContainerNbt) {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         this.setItem(i, ItemStack.EMPTY);
      }

      for(int k = 0; k < pContainerNbt.size(); ++k) {
         CompoundNBT compoundnbt = pContainerNbt.getCompound(k);
         int j = compoundnbt.getByte("Slot") & 255;
         if (j >= 0 && j < this.getContainerSize()) {
            this.setItem(j, ItemStack.of(compoundnbt));
         }
      }

   }

   public ListNBT createTag() {
      ListNBT listnbt = new ListNBT();

      for(int i = 0; i < this.getContainerSize(); ++i) {
         ItemStack itemstack = this.getItem(i);
         if (!itemstack.isEmpty()) {
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.putByte("Slot", (byte)i);
            itemstack.save(compoundnbt);
            listnbt.add(compoundnbt);
         }
      }

      return listnbt;
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return this.activeChest != null && !this.activeChest.stillValid(pPlayer) ? false : super.stillValid(pPlayer);
   }

   public void startOpen(PlayerEntity pPlayer) {
      if (this.activeChest != null) {
         this.activeChest.startOpen();
      }

      super.startOpen(pPlayer);
   }

   public void stopOpen(PlayerEntity pPlayer) {
      if (this.activeChest != null) {
         this.activeChest.stopOpen();
      }

      super.stopOpen(pPlayer);
      this.activeChest = null;
   }
}