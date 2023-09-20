package net.minecraft.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ServerRecipePlacerFurnace;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractFurnaceContainer extends RecipeBookContainer<IInventory> {
   private final IInventory container;
   private final IIntArray data;
   protected final World level;
   private final IRecipeType<? extends AbstractCookingRecipe> recipeType;
   private final RecipeBookCategory recipeBookType;

   protected AbstractFurnaceContainer(ContainerType<?> pMenuType, IRecipeType<? extends AbstractCookingRecipe> pRecipeType, RecipeBookCategory pRecipeBookType, int pContainerId, PlayerInventory pPlayerInventory) {
      this(pMenuType, pRecipeType, pRecipeBookType, pContainerId, pPlayerInventory, new Inventory(3), new IntArray(4));
   }

   protected AbstractFurnaceContainer(ContainerType<?> pMenuType, IRecipeType<? extends AbstractCookingRecipe> pRecipeType, RecipeBookCategory pRecipeBookType, int pContainerId, PlayerInventory pPlayerInventory, IInventory pContainer, IIntArray pData) {
      super(pMenuType, pContainerId);
      this.recipeType = pRecipeType;
      this.recipeBookType = pRecipeBookType;
      checkContainerSize(pContainer, 3);
      checkContainerDataCount(pData, 4);
      this.container = pContainer;
      this.data = pData;
      this.level = pPlayerInventory.player.level;
      this.addSlot(new Slot(pContainer, 0, 56, 17));
      this.addSlot(new FurnaceFuelSlot(this, pContainer, 1, 56, 53));
      this.addSlot(new FurnaceResultSlot(pPlayerInventory.player, pContainer, 2, 116, 35));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

      this.addDataSlots(pData);
   }

   public void fillCraftSlotsStackedContents(RecipeItemHelper pItemHelper) {
      if (this.container instanceof IRecipeHelperPopulator) {
         ((IRecipeHelperPopulator)this.container).fillStackedContents(pItemHelper);
      }

   }

   public void clearCraftingContent() {
      this.container.clearContent();
   }

   public void handlePlacement(boolean pPlaceAll, IRecipe<?> pRecipe, ServerPlayerEntity pPlayer) {
      (new ServerRecipePlacerFurnace<>(this)).recipeClicked(pPlayer, (IRecipe<IInventory>) pRecipe, pPlaceAll);
   }

   public boolean recipeMatches(IRecipe<? super IInventory> pRecipe) {
      return pRecipe.matches(this.container, this.level);
   }

   public int getResultSlotIndex() {
      return 2;
   }

   public int getGridWidth() {
      return 1;
   }

   public int getGridHeight() {
      return 1;
   }

   @OnlyIn(Dist.CLIENT)
   public int getSize() {
      return 3;
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
         if (pIndex == 2) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex != 1 && pIndex != 0) {
            if (this.canSmelt(itemstack1)) {
               if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.isFuel(itemstack1)) {
               if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 3 && pIndex < 30) {
               if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
               return ItemStack.EMPTY;
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

   protected boolean canSmelt(ItemStack pStack) {
      return this.level.getRecipeManager().getRecipeFor((IRecipeType)this.recipeType, new Inventory(pStack), this.level).isPresent();
   }

   protected boolean isFuel(ItemStack pStack) {
      return net.minecraftforge.common.ForgeHooks.getBurnTime(pStack, this.recipeType) > 0;
   }

   @OnlyIn(Dist.CLIENT)
   public int getBurnProgress() {
      int i = this.data.get(2);
      int j = this.data.get(3);
      return j != 0 && i != 0 ? i * 24 / j : 0;
   }

   @OnlyIn(Dist.CLIENT)
   public int getLitProgress() {
      int i = this.data.get(1);
      if (i == 0) {
         i = 200;
      }

      return this.data.get(0) * 13 / i;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isLit() {
      return this.data.get(0) > 0;
   }

   @OnlyIn(Dist.CLIENT)
   public RecipeBookCategory getRecipeBookType() {
      return this.recipeBookType;
   }
}
