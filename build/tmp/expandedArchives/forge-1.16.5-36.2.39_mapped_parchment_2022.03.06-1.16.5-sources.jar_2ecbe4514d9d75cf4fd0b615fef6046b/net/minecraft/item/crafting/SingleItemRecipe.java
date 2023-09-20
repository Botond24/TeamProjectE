package net.minecraft.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public abstract class SingleItemRecipe implements IRecipe<IInventory> {
   protected final Ingredient ingredient;
   protected final ItemStack result;
   private final IRecipeType<?> type;
   private final IRecipeSerializer<?> serializer;
   protected final ResourceLocation id;
   protected final String group;

   public SingleItemRecipe(IRecipeType<?> pType, IRecipeSerializer<?> pSerializer, ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult) {
      this.type = pType;
      this.serializer = pSerializer;
      this.id = pId;
      this.group = pGroup;
      this.ingredient = pIngredient;
      this.result = pResult;
   }

   public IRecipeType<?> getType() {
      return this.type;
   }

   public IRecipeSerializer<?> getSerializer() {
      return this.serializer;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   /**
    * Recipes with equal group are combined into one button in the recipe book
    */
   public String getGroup() {
      return this.group;
   }

   /**
    * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
    * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
    */
   public ItemStack getResultItem() {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> nonnulllist = NonNullList.create();
      nonnulllist.add(this.ingredient);
      return nonnulllist;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   /**
    * Returns an Item that is the result of this recipe
    */
   public ItemStack assemble(IInventory pInv) {
      return this.result.copy();
   }

   public static class Serializer<T extends SingleItemRecipe> extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
      final SingleItemRecipe.Serializer.IRecipeFactory<T> factory;

      protected Serializer(SingleItemRecipe.Serializer.IRecipeFactory<T> pFactory) {
         this.factory = pFactory;
      }

      public T fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
         String s = JSONUtils.getAsString(pJson, "group", "");
         Ingredient ingredient;
         if (JSONUtils.isArrayNode(pJson, "ingredient")) {
            ingredient = Ingredient.fromJson(JSONUtils.getAsJsonArray(pJson, "ingredient"));
         } else {
            ingredient = Ingredient.fromJson(JSONUtils.getAsJsonObject(pJson, "ingredient"));
         }

         String s1 = JSONUtils.getAsString(pJson, "result");
         int i = JSONUtils.getAsInt(pJson, "count");
         ItemStack itemstack = new ItemStack(Registry.ITEM.get(new ResourceLocation(s1)), i);
         return this.factory.create(pRecipeId, s, ingredient, itemstack);
      }

      public T fromNetwork(ResourceLocation pRecipeId, PacketBuffer pBuffer) {
         String s = pBuffer.readUtf(32767);
         Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
         ItemStack itemstack = pBuffer.readItem();
         return this.factory.create(pRecipeId, s, ingredient, itemstack);
      }

      public void toNetwork(PacketBuffer pBuffer, T pRecipe) {
         pBuffer.writeUtf(pRecipe.group);
         pRecipe.ingredient.toNetwork(pBuffer);
         pBuffer.writeItem(pRecipe.result);
      }

      interface IRecipeFactory<T extends SingleItemRecipe> {
         T create(ResourceLocation p_create_1_, String p_create_2_, Ingredient p_create_3_, ItemStack p_create_4_);
      }
   }
}
