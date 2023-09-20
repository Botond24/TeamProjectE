package net.minecraft.item.crafting;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public interface IRecipe<C extends IInventory> {
   /**
    * Used to check if a recipe matches current crafting inventory
    */
   boolean matches(C pInv, World pLevel);

   /**
    * Returns an Item that is the result of this recipe
    */
   ItemStack assemble(C pInv);

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   boolean canCraftInDimensions(int pWidth, int pHeight);

   /**
    * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
    * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
    */
   ItemStack getResultItem();

   default NonNullList<ItemStack> getRemainingItems(C pInv) {
      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pInv.getContainerSize(), ItemStack.EMPTY);

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack item = pInv.getItem(i);
         if (item.hasContainerItem()) {
            nonnulllist.set(i, item.getContainerItem());
         }
      }

      return nonnulllist;
   }

   default NonNullList<Ingredient> getIngredients() {
      return NonNullList.create();
   }

   /**
    * If true, this recipe does not appear in the recipe book and does not respect recipe unlocking (and the
    * doLimitedCrafting gamerule)
    */
   default boolean isSpecial() {
      return false;
   }

   /**
    * Recipes with equal group are combined into one button in the recipe book
    */
   default String getGroup() {
      return "";
   }

   default ItemStack getToastSymbol() {
      return new ItemStack(Blocks.CRAFTING_TABLE);
   }

   ResourceLocation getId();

   IRecipeSerializer<?> getSerializer();

   IRecipeType<?> getType();
}
