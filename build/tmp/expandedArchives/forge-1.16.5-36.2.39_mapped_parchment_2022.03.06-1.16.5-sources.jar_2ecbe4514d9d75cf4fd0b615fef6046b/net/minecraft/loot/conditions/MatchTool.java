package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;

/**
 * A LootItemCondition that checks the {@linkplain LootContextParams#TOOL tool} against an {@link ItemPredicate}.
 */
public class MatchTool implements ILootCondition {
   private final ItemPredicate predicate;

   public MatchTool(ItemPredicate pToolPredicate) {
      this.predicate = pToolPredicate;
   }

   public LootConditionType getType() {
      return LootConditionManager.MATCH_TOOL;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.TOOL);
   }

   public boolean test(LootContext p_test_1_) {
      ItemStack itemstack = p_test_1_.getParamOrNull(LootParameters.TOOL);
      return itemstack != null && this.predicate.matches(itemstack);
   }

   public static ILootCondition.IBuilder toolMatches(ItemPredicate.Builder pToolPredicateBuilder) {
      return () -> {
         return new MatchTool(pToolPredicateBuilder.build());
      };
   }

   public static class Serializer implements ILootSerializer<MatchTool> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, MatchTool pValue, JsonSerializationContext pSerializationContext) {
         pJson.add("predicate", pValue.predicate.serializeToJson());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public MatchTool deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("predicate"));
         return new MatchTool(itempredicate);
      }
   }
}