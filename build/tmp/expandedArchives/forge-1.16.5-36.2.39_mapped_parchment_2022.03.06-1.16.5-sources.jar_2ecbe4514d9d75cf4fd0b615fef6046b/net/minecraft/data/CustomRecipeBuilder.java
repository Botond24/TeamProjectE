package net.minecraft.data;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;

public class CustomRecipeBuilder {
   private final SpecialRecipeSerializer<?> serializer;

   public CustomRecipeBuilder(SpecialRecipeSerializer<?> pSerializer) {
      this.serializer = pSerializer;
   }

   public static CustomRecipeBuilder special(SpecialRecipeSerializer<?> pSerializer) {
      return new CustomRecipeBuilder(pSerializer);
   }

   /**
    * Builds this recipe into an {@link IFinishedRecipe}.
    */
   public void save(Consumer<IFinishedRecipe> pFinishedRecipeConsumer, final String pId) {
      pFinishedRecipeConsumer.accept(new IFinishedRecipe() {
         public void serializeRecipeData(JsonObject pJson) {
         }

         public IRecipeSerializer<?> getType() {
            return CustomRecipeBuilder.this.serializer;
         }

         /**
          * Gets the ID for the recipe.
          */
         public ResourceLocation getId() {
            return new ResourceLocation(pId);
         }

         /**
          * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
          */
         @Nullable
         public JsonObject serializeAdvancement() {
            return null;
         }

         /**
          * Gets the ID for the advancement associated with this recipe. Should not be null if {@link
          * #getAdvancementJson} is non-null.
          */
         public ResourceLocation getAdvancementId() {
            return new ResourceLocation("");
         }
      });
   }
}