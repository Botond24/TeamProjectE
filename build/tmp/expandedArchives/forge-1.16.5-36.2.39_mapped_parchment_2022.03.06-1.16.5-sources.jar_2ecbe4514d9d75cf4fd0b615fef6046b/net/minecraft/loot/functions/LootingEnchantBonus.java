package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;

/**
 * LootItemFunction that grows the stack's count by multiplying the {@linkplain LootContextParams#KILLER_ENTITY
 * killer}'s looting enchantment level with some multiplier. Optionally a limit to the stack size is applied.
 */
public class LootingEnchantBonus extends LootFunction {
   private final RandomValueRange value;
   private final int limit;

   private LootingEnchantBonus(ILootCondition[] p_i47145_1_, RandomValueRange p_i47145_2_, int p_i47145_3_) {
      super(p_i47145_1_);
      this.value = p_i47145_2_;
      this.limit = p_i47145_3_;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.LOOTING_ENCHANT;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.KILLER_ENTITY);
   }

   private boolean hasLimit() {
      return this.limit > 0;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Entity entity = pContext.getParamOrNull(LootParameters.KILLER_ENTITY);
      if (entity instanceof LivingEntity) {
         int i = pContext.getLootingModifier();
         if (i == 0) {
            return pStack;
         }

         float f = (float)i * this.value.getFloat(pContext.getRandom());
         pStack.grow(Math.round(f));
         if (this.hasLimit() && pStack.getCount() > this.limit) {
            pStack.setCount(this.limit);
         }
      }

      return pStack;
   }

   public static LootingEnchantBonus.Builder lootingMultiplier(RandomValueRange p_215915_0_) {
      return new LootingEnchantBonus.Builder(p_215915_0_);
   }

   public static class Builder extends LootFunction.Builder<LootingEnchantBonus.Builder> {
      private final RandomValueRange count;
      private int limit = 0;

      public Builder(RandomValueRange p_i50932_1_) {
         this.count = p_i50932_1_;
      }

      protected LootingEnchantBonus.Builder getThis() {
         return this;
      }

      public LootingEnchantBonus.Builder setLimit(int pLimit) {
         this.limit = pLimit;
         return this;
      }

      public ILootFunction build() {
         return new LootingEnchantBonus(this.getConditions(), this.count, this.limit);
      }
   }

   public static class Serializer extends LootFunction.Serializer<LootingEnchantBonus> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, LootingEnchantBonus pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.add("count", pSerializationContext.serialize(pValue.value));
         if (pValue.hasLimit()) {
            pJson.add("limit", pSerializationContext.serialize(pValue.limit));
         }

      }

      public LootingEnchantBonus deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         int i = JSONUtils.getAsInt(pObject, "limit", 0);
         return new LootingEnchantBonus(pConditions, JSONUtils.getAsObject(pObject, "count", pDeserializationContext, RandomValueRange.class), i);
      }
   }
}
