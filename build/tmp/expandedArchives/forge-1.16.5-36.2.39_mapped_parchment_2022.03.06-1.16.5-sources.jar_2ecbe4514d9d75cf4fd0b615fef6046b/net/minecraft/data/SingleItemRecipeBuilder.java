package net.minecraft.data;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class SingleItemRecipeBuilder {
   private final Item result;
   private final Ingredient ingredient;
   private final int count;
   private final Advancement.Builder advancement = Advancement.Builder.advancement();
   private String group;
   private final IRecipeSerializer<?> type;

   public SingleItemRecipeBuilder(IRecipeSerializer<?> pType, Ingredient pIngredient, IItemProvider pResult, int pCount) {
      this.type = pType;
      this.result = pResult.asItem();
      this.ingredient = pIngredient;
      this.count = pCount;
   }

   public static SingleItemRecipeBuilder stonecutting(Ingredient pIngredient, IItemProvider pResult) {
      return new SingleItemRecipeBuilder(IRecipeSerializer.STONECUTTER, pIngredient, pResult, 1);
   }

   public static SingleItemRecipeBuilder stonecutting(Ingredient pIngredient, IItemProvider pResult, int pCount) {
      return new SingleItemRecipeBuilder(IRecipeSerializer.STONECUTTER, pIngredient, pResult, pCount);
   }

   public SingleItemRecipeBuilder unlocks(String p_218643_1_, ICriterionInstance p_218643_2_) {
      this.advancement.addCriterion(p_218643_1_, p_218643_2_);
      return this;
   }

   public void save(Consumer<IFinishedRecipe> p_218645_1_, String p_218645_2_) {
      ResourceLocation resourcelocation = Registry.ITEM.getKey(this.result);
      if ((new ResourceLocation(p_218645_2_)).equals(resourcelocation)) {
         throw new IllegalStateException("Single Item Recipe " + p_218645_2_ + " should remove its 'save' argument");
      } else {
         this.save(p_218645_1_, new ResourceLocation(p_218645_2_));
      }
   }

   public void save(Consumer<IFinishedRecipe> p_218647_1_, ResourceLocation p_218647_2_) {
      this.ensureValid(p_218647_2_);
      this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(p_218647_2_)).rewards(AdvancementRewards.Builder.recipe(p_218647_2_)).requirements(IRequirementsStrategy.OR);
      p_218647_1_.accept(new SingleItemRecipeBuilder.Result(p_218647_2_, this.type, this.group == null ? "" : this.group, this.ingredient, this.result, this.count, this.advancement, new ResourceLocation(p_218647_2_.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + p_218647_2_.getPath())));
   }

   private void ensureValid(ResourceLocation pId) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pId);
      }
   }

   public static class Result implements IFinishedRecipe {
      private final ResourceLocation id;
      private final String group;
      private final Ingredient ingredient;
      private final Item result;
      private final int count;
      private final Advancement.Builder advancement;
      private final ResourceLocation advancementId;
      private final IRecipeSerializer<?> type;

      public Result(ResourceLocation pId, IRecipeSerializer<?> pType, String pGroup, Ingredient pIngredient, Item pResult, int pCount, Advancement.Builder pAdvancement, ResourceLocation pAdvancementId) {
         this.id = pId;
         this.type = pType;
         this.group = pGroup;
         this.ingredient = pIngredient;
         this.result = pResult;
         this.count = pCount;
         this.advancement = pAdvancement;
         this.advancementId = pAdvancementId;
      }

      public void serializeRecipeData(JsonObject pJson) {
         if (!this.group.isEmpty()) {
            pJson.addProperty("group", this.group);
         }

         pJson.add("ingredient", this.ingredient.toJson());
         pJson.addProperty("result", Registry.ITEM.getKey(this.result).toString());
         pJson.addProperty("count", this.count);
      }

      /**
       * Gets the ID for the recipe.
       */
      public ResourceLocation getId() {
         return this.id;
      }

      public IRecipeSerializer<?> getType() {
         return this.type;
      }

      /**
       * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
       */
      @Nullable
      public JsonObject serializeAdvancement() {
         return this.advancement.serializeToJson();
      }

      /**
       * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #getAdvancementJson}
       * is non-null.
       */
      @Nullable
      public ResourceLocation getAdvancementId() {
         return this.advancementId;
      }
   }
}