package net.minecraft.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.LootConditionManager;
import net.minecraft.util.JSONUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Base class for loot pool entry containers. This class just stores a list of conditions that are checked before the
 * entry generates loot.
 */
public abstract class LootEntry implements ILootEntry {
   /** Conditions for the loot entry to be applied. */
   protected final ILootCondition[] conditions;
   private final Predicate<LootContext> compositeCondition;

   protected LootEntry(ILootCondition[] pConditions) {
      this.conditions = pConditions;
      this.compositeCondition = LootConditionManager.andConditions(pConditions);
   }

   public void validate(ValidationTracker pValidationContext) {
      for(int i = 0; i < this.conditions.length; ++i) {
         this.conditions[i].validate(pValidationContext.forChild(".condition[" + i + "]"));
      }

   }

   protected final boolean canRun(LootContext pLootContext) {
      return this.compositeCondition.test(pLootContext);
   }

   public abstract LootPoolEntryType getType();

   public abstract static class Builder<T extends LootEntry.Builder<T>> implements ILootConditionConsumer<T> {
      private final List<ILootCondition> conditions = Lists.newArrayList();

      protected abstract T getThis();

      public T when(ILootCondition.IBuilder pConditionBuilder) {
         this.conditions.add(pConditionBuilder.build());
         return this.getThis();
      }

      public final T unwrap() {
         return this.getThis();
      }

      protected ILootCondition[] getConditions() {
         return this.conditions.toArray(new ILootCondition[0]);
      }

      public AlternativesLootEntry.Builder otherwise(LootEntry.Builder<?> pChildBuilder) {
         return new AlternativesLootEntry.Builder(this, pChildBuilder);
      }

      public abstract LootEntry build();
   }

   public abstract static class Serializer<T extends LootEntry> implements ILootSerializer<T> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public final void serialize(JsonObject pJson, T pValue, JsonSerializationContext pSerializationContext) {
         if (!ArrayUtils.isEmpty((Object[])pValue.conditions)) {
            pJson.add("conditions", pSerializationContext.serialize(pValue.conditions));
         }

         this.serializeCustom(pJson, pValue, pSerializationContext);
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public final T deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         ILootCondition[] ailootcondition = JSONUtils.getAsObject(pJson, "conditions", new ILootCondition[0], pSerializationContext, ILootCondition[].class);
         return this.deserializeCustom(pJson, pSerializationContext, ailootcondition);
      }

      public abstract void serializeCustom(JsonObject pObject, T pContext, JsonSerializationContext pConditions);

      public abstract T deserializeCustom(JsonObject pObject, JsonDeserializationContext pContext, ILootCondition[] pConditions);
   }
}