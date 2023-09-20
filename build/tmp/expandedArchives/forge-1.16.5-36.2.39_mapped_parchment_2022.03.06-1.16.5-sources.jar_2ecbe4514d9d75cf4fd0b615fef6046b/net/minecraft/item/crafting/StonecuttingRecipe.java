package net.minecraft.item.crafting;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class StonecuttingRecipe extends SingleItemRecipe {
   public StonecuttingRecipe(ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult) {
      super(IRecipeType.STONECUTTING, IRecipeSerializer.STONECUTTER, pId, pGroup, pIngredient, pResult);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(IInventory pInv, World pLevel) {
      return this.ingredient.test(pInv.getItem(0));
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.STONECUTTER);
   }
}