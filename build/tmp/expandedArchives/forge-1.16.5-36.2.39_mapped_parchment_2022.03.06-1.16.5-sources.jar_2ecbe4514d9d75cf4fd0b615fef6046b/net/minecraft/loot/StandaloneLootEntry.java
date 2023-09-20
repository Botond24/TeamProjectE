package net.minecraft.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A LootPoolEntryContainer that expands into a single LootPoolEntry.
 */
public abstract class StandaloneLootEntry extends LootEntry {
   /** The weight of the entry. */
   protected final int weight;
   /** The quality of the entry. */
   protected final int quality;
   /** Functions that are ran on the entry. */
   protected final ILootFunction[] functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
   private final ILootGenerator entry = new StandaloneLootEntry.Generator() {
      /**
       * Generate the loot stacks of this entry.
       * Contrary to the method name this method does not always generate one stack, it can also generate zero or
       * multiple stacks.
       */
      public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
         StandaloneLootEntry.this.createItemStack(ILootFunction.decorate(StandaloneLootEntry.this.compositeFunction, pStackConsumer, pLootContext), pLootContext);
      }
   };

   protected StandaloneLootEntry(int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
      super(pConditions);
      this.weight = pWeight;
      this.quality = pQuality;
      this.functions = pFunctions;
      this.compositeFunction = LootFunctionManager.compose(pFunctions);
   }

   public void validate(ValidationTracker pValidationContext) {
      super.validate(pValidationContext);

      for(int i = 0; i < this.functions.length; ++i) {
         this.functions[i].validate(pValidationContext.forChild(".functions[" + i + "]"));
      }

   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   protected abstract void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext);

   public boolean expand(LootContext p_expand_1_, Consumer<ILootGenerator> p_expand_2_) {
      if (this.canRun(p_expand_1_)) {
         p_expand_2_.accept(this.entry);
         return true;
      } else {
         return false;
      }
   }

   public static StandaloneLootEntry.Builder<?> simpleBuilder(StandaloneLootEntry.ILootEntryBuilder pEntryBuilder) {
      return new StandaloneLootEntry.BuilderImpl(pEntryBuilder);
   }

   public abstract static class Builder<T extends StandaloneLootEntry.Builder<T>> extends LootEntry.Builder<T> implements ILootFunctionConsumer<T> {
      protected int weight = 1;
      protected int quality = 0;
      private final List<ILootFunction> functions = Lists.newArrayList();

      public T apply(ILootFunction.IBuilder pFunctionBuilder) {
         this.functions.add(pFunctionBuilder.build());
         return this.getThis();
      }

      /**
       * Creates an array from the functions list
       */
      protected ILootFunction[] getFunctions() {
         return this.functions.toArray(new ILootFunction[0]);
      }

      public T setWeight(int pWeight) {
         this.weight = pWeight;
         return this.getThis();
      }

      public T setQuality(int pQuality) {
         this.quality = pQuality;
         return this.getThis();
      }
   }

   static class BuilderImpl extends StandaloneLootEntry.Builder<StandaloneLootEntry.BuilderImpl> {
      private final StandaloneLootEntry.ILootEntryBuilder constructor;

      public BuilderImpl(StandaloneLootEntry.ILootEntryBuilder p_i50485_1_) {
         this.constructor = p_i50485_1_;
      }

      protected StandaloneLootEntry.BuilderImpl getThis() {
         return this;
      }

      public LootEntry build() {
         return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
      }
   }

   public abstract class Generator implements ILootGenerator {
      protected Generator() {
      }

      /**
       * Gets the effective weight based on the loot entry's weight and quality multiplied by looter's luck.
       */
      public int getWeight(float pLuck) {
         return Math.max(MathHelper.floor((float)StandaloneLootEntry.this.weight + (float)StandaloneLootEntry.this.quality * pLuck), 0);
      }
   }

   @FunctionalInterface
   public interface ILootEntryBuilder {
      StandaloneLootEntry build(int p_build_1_, int p_build_2_, ILootCondition[] p_build_3_, ILootFunction[] p_build_4_);
   }

   public abstract static class Serializer<T extends StandaloneLootEntry> extends LootEntry.Serializer<T> {
      public void serializeCustom(JsonObject pObject, T pContext, JsonSerializationContext pConditions) {
         if (pContext.weight != 1) {
            pObject.addProperty("weight", pContext.weight);
         }

         if (pContext.quality != 0) {
            pObject.addProperty("quality", pContext.quality);
         }

         if (!ArrayUtils.isEmpty((Object[])pContext.functions)) {
            pObject.add("functions", pConditions.serialize(pContext.functions));
         }

      }

      public final T deserializeCustom(JsonObject pObject, JsonDeserializationContext pContext, ILootCondition[] pConditions) {
         int i = JSONUtils.getAsInt(pObject, "weight", 1);
         int j = JSONUtils.getAsInt(pObject, "quality", 0);
         ILootFunction[] ailootfunction = JSONUtils.getAsObject(pObject, "functions", new ILootFunction[0], pContext, ILootFunction[].class);
         return this.deserialize(pObject, pContext, i, j, pConditions, ailootfunction);
      }

      protected abstract T deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions);
   }
}