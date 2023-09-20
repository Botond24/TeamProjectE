package net.minecraft.loot.conditions;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.JSONUtils;

/**
 * A LootItemCondition that combines a list of other conditions using "or".
 * 
 * @see LootItemConditions#orConditions
 */
public class Alternative implements ILootCondition {
   private final ILootCondition[] terms;
   private final Predicate<LootContext> composedPredicate;

   private Alternative(ILootCondition[] pTerms) {
      this.terms = pTerms;
      this.composedPredicate = LootConditionManager.orConditions(pTerms);
   }

   public LootConditionType getType() {
      return LootConditionManager.ALTERNATIVE;
   }

   public final boolean test(LootContext p_test_1_) {
      return this.composedPredicate.test(p_test_1_);
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationTracker pContext) {
      ILootCondition.super.validate(pContext);

      for(int i = 0; i < this.terms.length; ++i) {
         this.terms[i].validate(pContext.forChild(".term[" + i + "]"));
      }

   }

   public static Alternative.Builder alternative(ILootCondition.IBuilder... pBuilders) {
      return new Alternative.Builder(pBuilders);
   }

   public static class Builder implements ILootCondition.IBuilder {
      private final List<ILootCondition> terms = Lists.newArrayList();

      public Builder(ILootCondition.IBuilder... p_i50046_1_) {
         for(ILootCondition.IBuilder ilootcondition$ibuilder : p_i50046_1_) {
            this.terms.add(ilootcondition$ibuilder.build());
         }

      }

      public Alternative.Builder or(ILootCondition.IBuilder pBuilder) {
         this.terms.add(pBuilder.build());
         return this;
      }

      public ILootCondition build() {
         return new Alternative(this.terms.toArray(new ILootCondition[0]));
      }
   }

   public static class Serializer implements ILootSerializer<Alternative> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, Alternative pValue, JsonSerializationContext pSerializationContext) {
         pJson.add("terms", pSerializationContext.serialize(pValue.terms));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public Alternative deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         ILootCondition[] ailootcondition = JSONUtils.getAsObject(pJson, "terms", pSerializationContext, ILootCondition[].class);
         return new Alternative(ailootcondition);
      }
   }
}