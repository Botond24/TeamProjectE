package net.minecraft.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SmithingRecipe implements IRecipe<IInventory> {
   private final Ingredient base;
   private final Ingredient addition;
   private final ItemStack result;
   private final ResourceLocation id;

   public SmithingRecipe(ResourceLocation pId, Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
      this.id = pId;
      this.base = pBase;
      this.addition = pAddition;
      this.result = pResult;
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(IInventory pInv, World pLevel) {
      return this.base.test(pInv.getItem(0)) && this.addition.test(pInv.getItem(1));
   }

   /**
    * Returns an Item that is the result of this recipe
    */
   public ItemStack assemble(IInventory pInv) {
      ItemStack itemstack = this.result.copy();
      CompoundNBT compoundnbt = pInv.getItem(0).getTag();
      if (compoundnbt != null) {
         itemstack.setTag(compoundnbt.copy());
      }

      return itemstack;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }

   /**
    * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
    * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
    */
   public ItemStack getResultItem() {
      return this.result;
   }

   public boolean isAdditionIngredient(ItemStack pAddition) {
      return this.addition.test(pAddition);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.SMITHING_TABLE);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public IRecipeSerializer<?> getSerializer() {
      return IRecipeSerializer.SMITHING;
   }

   public IRecipeType<?> getType() {
      return IRecipeType.SMITHING;
   }

   public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<SmithingRecipe> {
      public SmithingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
         Ingredient ingredient = Ingredient.fromJson(JSONUtils.getAsJsonObject(pJson, "base"));
         Ingredient ingredient1 = Ingredient.fromJson(JSONUtils.getAsJsonObject(pJson, "addition"));
         ItemStack itemstack = ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(pJson, "result"));
         return new SmithingRecipe(pRecipeId, ingredient, ingredient1, itemstack);
      }

      public SmithingRecipe fromNetwork(ResourceLocation pRecipeId, PacketBuffer pBuffer) {
         Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
         Ingredient ingredient1 = Ingredient.fromNetwork(pBuffer);
         ItemStack itemstack = pBuffer.readItem();
         return new SmithingRecipe(pRecipeId, ingredient, ingredient1, itemstack);
      }

      public void toNetwork(PacketBuffer pBuffer, SmithingRecipe pRecipe) {
         pRecipe.base.toNetwork(pBuffer);
         pRecipe.addition.toNetwork(pBuffer);
         pBuffer.writeItem(pRecipe.result);
      }
   }
}
