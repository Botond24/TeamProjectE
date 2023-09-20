package net.minecraft.inventory.container;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BrewingStandContainer extends Container {
   private final IInventory brewingStand;
   private final IIntArray brewingStandData;
   private final Slot ingredientSlot;

   public BrewingStandContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, new Inventory(5), new IntArray(2));
   }

   public BrewingStandContainer(int pContainerId, PlayerInventory pPlayerInventory, IInventory pBrewingStandContainer, IIntArray pBrewingStandData) {
      super(ContainerType.BREWING_STAND, pContainerId);
      checkContainerSize(pBrewingStandContainer, 5);
      checkContainerDataCount(pBrewingStandData, 2);
      this.brewingStand = pBrewingStandContainer;
      this.brewingStandData = pBrewingStandData;
      this.addSlot(new BrewingStandContainer.PotionSlot(pBrewingStandContainer, 0, 56, 51));
      this.addSlot(new BrewingStandContainer.PotionSlot(pBrewingStandContainer, 1, 79, 58));
      this.addSlot(new BrewingStandContainer.PotionSlot(pBrewingStandContainer, 2, 102, 51));
      this.ingredientSlot = this.addSlot(new BrewingStandContainer.IngredientSlot(pBrewingStandContainer, 3, 79, 17));
      this.addSlot(new BrewingStandContainer.FuelSlot(pBrewingStandContainer, 4, 17, 17));
      this.addDataSlots(pBrewingStandData);

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
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return this.brewingStand.stillValid(pPlayer);
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
         if ((pIndex < 0 || pIndex > 2) && pIndex != 3 && pIndex != 4) {
            if (BrewingStandContainer.FuelSlot.mayPlaceItem(itemstack)) {
               if (this.moveItemStackTo(itemstack1, 4, 5, false) || this.ingredientSlot.mayPlace(itemstack1) && !this.moveItemStackTo(itemstack1, 3, 4, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.ingredientSlot.mayPlace(itemstack1)) {
               if (!this.moveItemStackTo(itemstack1, 3, 4, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (BrewingStandContainer.PotionSlot.mayPlaceItem(itemstack) && itemstack.getCount() == 1) {
               if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 5 && pIndex < 32) {
               if (!this.moveItemStackTo(itemstack1, 32, 41, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 32 && pIndex < 41) {
               if (!this.moveItemStackTo(itemstack1, 5, 32, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 5, 41, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
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
   public int getFuel() {
      return this.brewingStandData.get(1);
   }

   @OnlyIn(Dist.CLIENT)
   public int getBrewingTicks() {
      return this.brewingStandData.get(0);
   }

   static class FuelSlot extends Slot {
      public FuelSlot(IInventory p_i47070_1_, int p_i47070_2_, int p_i47070_3_, int p_i47070_4_) {
         super(p_i47070_1_, p_i47070_2_, p_i47070_3_, p_i47070_4_);
      }

      /**
       * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
       */
      public boolean mayPlace(ItemStack pStack) {
         return mayPlaceItem(pStack);
      }

      /**
       * Returns true if the given ItemStack is usable as a fuel in the brewing stand.
       */
      public static boolean mayPlaceItem(ItemStack pItemStack) {
         return pItemStack.getItem() == Items.BLAZE_POWDER;
      }

      /**
       * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
       * case of armor slots)
       */
      public int getMaxStackSize() {
         return 64;
      }
   }

   static class IngredientSlot extends Slot {
      public IngredientSlot(IInventory p_i47069_1_, int p_i47069_2_, int p_i47069_3_, int p_i47069_4_) {
         super(p_i47069_1_, p_i47069_2_, p_i47069_3_, p_i47069_4_);
      }

      /**
       * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
       */
      public boolean mayPlace(ItemStack pStack) {
         return net.minecraftforge.common.brewing.BrewingRecipeRegistry.isValidIngredient(pStack);
      }

      /**
       * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
       * case of armor slots)
       */
      public int getMaxStackSize() {
         return 64;
      }
   }

   static class PotionSlot extends Slot {
      public PotionSlot(IInventory p_i47598_1_, int p_i47598_2_, int p_i47598_3_, int p_i47598_4_) {
         super(p_i47598_1_, p_i47598_2_, p_i47598_3_, p_i47598_4_);
      }

      /**
       * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
       */
      public boolean mayPlace(ItemStack pStack) {
         return mayPlaceItem(pStack);
      }

      /**
       * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
       * case of armor slots)
       */
      public int getMaxStackSize() {
         return 1;
      }

      public ItemStack onTake(PlayerEntity pPlayer, ItemStack pStack) {
         Potion potion = PotionUtils.getPotion(pStack);
         if (pPlayer instanceof ServerPlayerEntity) {
            net.minecraftforge.event.ForgeEventFactory.onPlayerBrewedPotion(pPlayer, pStack);
            CriteriaTriggers.BREWED_POTION.trigger((ServerPlayerEntity)pPlayer, potion);
         }

         super.onTake(pPlayer, pStack);
         return pStack;
      }

      /**
       * Returns true if this itemstack can be filled with a potion
       */
      public static boolean mayPlaceItem(ItemStack pStack) {
         return net.minecraftforge.common.brewing.BrewingRecipeRegistry.isValidInput(pStack);
      }
   }
}
