package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class RecipeUnlockedTrigger extends AbstractCriterionTrigger<RecipeUnlockedTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("recipe_unlocked");

   public ResourceLocation getId() {
      return ID;
   }

   public RecipeUnlockedTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJson, "recipe"));
      return new RecipeUnlockedTrigger.Instance(pEntityPredicate, resourcelocation);
   }

   public void trigger(ServerPlayerEntity pPlayer, IRecipe<?> pRecipe) {
      this.trigger(pPlayer, (p_227018_1_) -> {
         return p_227018_1_.matches(pRecipe);
      });
   }

   public static RecipeUnlockedTrigger.Instance unlocked(ResourceLocation pRecipeID) {
      return new RecipeUnlockedTrigger.Instance(EntityPredicate.AndPredicate.ANY, pRecipeID);
   }

   public static class Instance extends CriterionInstance {
      private final ResourceLocation recipe;

      public Instance(EntityPredicate.AndPredicate p_i231865_1_, ResourceLocation p_i231865_2_) {
         super(RecipeUnlockedTrigger.ID, p_i231865_1_);
         this.recipe = p_i231865_2_;
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.addProperty("recipe", this.recipe.toString());
         return jsonobject;
      }

      public boolean matches(IRecipe<?> pRecipe) {
         return this.recipe.equals(pRecipe.getId());
      }
   }
}