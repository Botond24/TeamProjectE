package net.minecraft.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChestContainer extends Container {
   private final IInventory container;
   private final int containerRows;

   private ChestContainer(ContainerType<?> pType, int pContainerId, PlayerInventory pPlayerInventory, int pRows) {
      this(pType, pContainerId, pPlayerInventory, new Inventory(9 * pRows), pRows);
   }

   public static ChestContainer oneRow(int pId, PlayerInventory pPlayer) {
      return new ChestContainer(ContainerType.GENERIC_9x1, pId, pPlayer, 1);
   }

   public static ChestContainer twoRows(int pId, PlayerInventory pPlayer) {
      return new ChestContainer(ContainerType.GENERIC_9x2, pId, pPlayer, 2);
   }

   public static ChestContainer threeRows(int pId, PlayerInventory pPlayer) {
      return new ChestContainer(ContainerType.GENERIC_9x3, pId, pPlayer, 3);
   }

   public static ChestContainer fourRows(int pId, PlayerInventory pPlayer) {
      return new ChestContainer(ContainerType.GENERIC_9x4, pId, pPlayer, 4);
   }

   public static ChestContainer fiveRows(int pId, PlayerInventory pPlayer) {
      return new ChestContainer(ContainerType.GENERIC_9x5, pId, pPlayer, 5);
   }

   public static ChestContainer sixRows(int pId, PlayerInventory pPlayer) {
      return new ChestContainer(ContainerType.GENERIC_9x6, pId, pPlayer, 6);
   }

   public static ChestContainer threeRows(int pId, PlayerInventory pPlayer, IInventory pBlockEntity) {
      return new ChestContainer(ContainerType.GENERIC_9x3, pId, pPlayer, pBlockEntity, 3);
   }

   public static ChestContainer sixRows(int pId, PlayerInventory pPlayer, IInventory pBlockEntity) {
      return new ChestContainer(ContainerType.GENERIC_9x6, pId, pPlayer, pBlockEntity, 6);
   }

   public ChestContainer(ContainerType<?> pType, int pContainerId, PlayerInventory pPlayerInventory, IInventory pContainer, int pRows) {
      super(pType, pContainerId);
      checkContainerSize(pContainer, pRows * 9);
      this.container = pContainer;
      this.containerRows = pRows;
      pContainer.startOpen(pPlayerInventory.player);
      int i = (this.containerRows - 4) * 18;

      for(int j = 0; j < this.containerRows; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(pContainer, k + j * 9, 8 + k * 18, 18 + j * 18));
         }
      }

      for(int l = 0; l < 3; ++l) {
         for(int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(pPlayerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
         }
      }

      for(int i1 = 0; i1 < 9; ++i1) {
         this.addSlot(new Slot(pPlayerInventory, i1, 8 + i1 * 18, 161 + i));
      }

   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return this.container.stillValid(pPlayer);
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
         if (pIndex < this.containerRows * 9) {
            if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return itemstack;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.container.stopOpen(pPlayer);
   }

   /**
    * Gets the inventory associated with this chest container.
    * 
    * @see #field_75155_e
    */
   public IInventory getContainer() {
      return this.container;
   }

   @OnlyIn(Dist.CLIENT)
   public int getRowCount() {
      return this.containerRows;
   }
}