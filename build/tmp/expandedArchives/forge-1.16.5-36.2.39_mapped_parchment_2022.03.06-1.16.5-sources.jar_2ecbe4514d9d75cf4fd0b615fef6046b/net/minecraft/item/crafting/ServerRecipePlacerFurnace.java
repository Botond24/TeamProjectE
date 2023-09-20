package net.minecraft.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ServerRecipePlacerFurnace<C extends IInventory> extends ServerRecipePlacer<C> {
   private boolean recipeMatchesPlaced;

   public ServerRecipePlacerFurnace(RecipeBookContainer<C> p_i50751_1_) {
      super(p_i50751_1_);
   }

   protected void handleRecipeClicked(IRecipe<C> pRecipe, boolean pPlaceAll) {
      this.recipeMatchesPlaced = this.menu.recipeMatches(pRecipe);
      int i = this.stackedContents.getBiggestCraftableStack(pRecipe, (IntList)null);
      if (this.recipeMatchesPlaced) {
         ItemStack itemstack = this.menu.getSlot(0).getItem();
         if (itemstack.isEmpty() || i <= itemstack.getCount()) {
            return;
         }
      }

      int j = this.getStackSize(pPlaceAll, i, this.recipeMatchesPlaced);
      IntList intlist = new IntArrayList();
      if (this.stackedContents.canCraft(pRecipe, intlist, j)) {
         if (!this.recipeMatchesPlaced) {
            this.moveItemToInventory(this.menu.getResultSlotIndex());
            this.moveItemToInventory(0);
         }

         this.placeRecipe(j, intlist);
      }
   }

   protected void clearGrid() {
      this.moveItemToInventory(this.menu.getResultSlotIndex());
      super.clearGrid();
   }

   protected void placeRecipe(int p_201516_1_, IntList p_201516_2_) {
      Iterator<Integer> iterator = p_201516_2_.iterator();
      Slot slot = this.menu.getSlot(0);
      ItemStack itemstack = RecipeItemHelper.fromStackingIndex(iterator.next());
      if (!itemstack.isEmpty()) {
         int i = Math.min(itemstack.getMaxStackSize(), p_201516_1_);
         if (this.recipeMatchesPlaced) {
            i -= slot.getItem().getCount();
         }

         for(int j = 0; j < i; ++j) {
            this.moveItemToGrid(slot, itemstack);
         }

      }
   }
}