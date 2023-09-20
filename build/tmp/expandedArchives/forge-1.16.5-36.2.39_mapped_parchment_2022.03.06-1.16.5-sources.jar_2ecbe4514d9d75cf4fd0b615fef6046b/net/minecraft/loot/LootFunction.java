package net.minecraft.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.LootConditionManager;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.JSONUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A LootItemFunction that only modifies the stacks if a list of {@linkplain LootItemCondition predicates} passes.
 */
public abstract class LootFunction implements ILootFunction {
   protected final ILootCondition[] predicates;
   private final Predicate<LootContext> compositePredicates;

   protected LootFunction(ILootCondition[] pConditions) {
      this.predicates = pConditions;
      this.compositePredicates = LootConditionManager.andConditions(pConditions);
   }

   public final ItemStack apply(ItemStack p_apply_1_, LootContext p_apply_2_) {
      return this.compositePredicates.test(p_apply_2_) ? this.run(p_apply_1_, p_apply_2_) : p_apply_1_;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   protected abstract ItemStack run(ItemStack pStack, LootContext pContext);

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationTracker pContext) {
      ILootFunction.super.validate(pContext);

      for(int i = 0; i < this.predicates.length; ++i) {
         this.predicates[i].validate(pContext.forChild(".conditions[" + i + "]"));
      }

   }

   protected static LootFunction.Builder<?> simpleBuilder(Function<ILootCondition[], ILootFunction> pConstructor) {
      return new LootFunction.SimpleBuilder(pConstructor);
   }

   public abstract static class Builder<T extends LootFunction.Builder<T>> implements ILootFunction.IBuilder, ILootConditionConsumer<T> {
      private final List<ILootCondition> conditions = Lists.newArrayList();

      public T when(ILootCondition.IBuilder pConditionBuilder) {
         this.conditions.add(pConditionBuilder.build());
         return this.getThis();
      }

      public final T unwrap() {
         return this.getThis();
      }

      protected abstract T getThis();

      protected ILootCondition[] getConditions() {
         return this.conditions.toArray(new ILootCondition[0]);
      }
   }

   public abstract static class Serializer<T extends LootFunction> implements ILootSerializer<T> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, T pValue, JsonSerializationContext pSerializationContext) {
         if (!ArrayUtils.isEmpty((Object[])pValue.predicates)) {
            pJson.add("conditions", pSerializationContext.serialize(pValue.predicates));
         }

      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public final T deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         ILootCondition[] ailootcondition = JSONUtils.getAsObject(pJson, "conditions", new ILootCondition[0], pSerializationContext, ILootCondition[].class);
         return this.deserialize(pJson, pSerializationContext, ailootcondition);
      }

      public abstract T deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions);
   }

   static final class SimpleBuilder extends LootFunction.Builder<LootFunction.SimpleBuilder> {
      private final Function<ILootCondition[], ILootFunction> constructor;

      public SimpleBuilder(Function<ILootCondition[], ILootFunction> pConstructor) {
         this.constructor = pConstructor;
      }

      protected LootFunction.SimpleBuilder getThis() {
         return this;
      }

      public ILootFunction build() {
         return this.constructor.apply(this.getConditions());
      }
   }
}