package net.minecraft.item.crafting;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class FurnaceRecipe extends AbstractCookingRecipe {
   public FurnaceRecipe(ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
      super(IRecipeType.SMELTING, pId, pGroup, pIngredient, pResult, pExperience, pCookingTime);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.FURNACE);
   }

   public IRecipeSerializer<?> getSerializer() {
      return IRecipeSerializer.SMELTING_RECIPE;
   }
}