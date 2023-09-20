package net.minecraft.item.crafting;

import java.util.Optional;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public interface IRecipeType<T extends IRecipe<?>> {
   IRecipeType<ICraftingRecipe> CRAFTING = register("crafting");
   IRecipeType<FurnaceRecipe> SMELTING = register("smelting");
   IRecipeType<BlastingRecipe> BLASTING = register("blasting");
   IRecipeType<SmokingRecipe> SMOKING = register("smoking");
   IRecipeType<CampfireCookingRecipe> CAMPFIRE_COOKING = register("campfire_cooking");
   IRecipeType<StonecuttingRecipe> STONECUTTING = register("stonecutting");
   IRecipeType<SmithingRecipe> SMITHING = register("smithing");

   static <T extends IRecipe<?>> IRecipeType<T> register(final String pIdentifier) {
      return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(pIdentifier), new IRecipeType<T>() {
         public String toString() {
            return pIdentifier;
         }
      });
   }

   default <C extends IInventory> Optional<T> tryMatch(IRecipe<C> pRecipe, World pLevel, C pContainer) {
      return pRecipe.matches(pContainer, pLevel) ? Optional.of((T)pRecipe) : Optional.empty();
   }
}