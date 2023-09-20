package net.minecraft.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.IRandomRange;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.RandomRanges;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;

/**
 * Applies a random enchantment to the stack.
 * 
 * @see EnchantmentHelper#enchantItem
 */
public class EnchantWithLevels extends LootFunction {
   private final IRandomRange levels;
   private final boolean treasure;

   private EnchantWithLevels(ILootCondition[] p_i51236_1_, IRandomRange p_i51236_2_, boolean p_i51236_3_) {
      super(p_i51236_1_);
      this.levels = p_i51236_2_;
      this.treasure = p_i51236_3_;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.ENCHANT_WITH_LEVELS;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Random random = pContext.getRandom();
      return EnchantmentHelper.enchantItem(random, pStack, this.levels.getInt(random), this.treasure);
   }

   public static EnchantWithLevels.Builder enchantWithLevels(IRandomRange p_215895_0_) {
      return new EnchantWithLevels.Builder(p_215895_0_);
   }

   public static class Builder extends LootFunction.Builder<EnchantWithLevels.Builder> {
      private final IRandomRange levels;
      private boolean treasure;

      public Builder(IRandomRange p_i51494_1_) {
         this.levels = p_i51494_1_;
      }

      protected EnchantWithLevels.Builder getThis() {
         return this;
      }

      public EnchantWithLevels.Builder allowTreasure() {
         this.treasure = true;
         return this;
      }

      public ILootFunction build() {
         return new EnchantWithLevels(this.getConditions(), this.levels, this.treasure);
      }
   }

   public static class Serializer extends LootFunction.Serializer<EnchantWithLevels> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, EnchantWithLevels pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.add("levels", RandomRanges.serialize(pValue.levels, pSerializationContext));
         pJson.addProperty("treasure", pValue.treasure);
      }

      public EnchantWithLevels deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         IRandomRange irandomrange = RandomRanges.deserialize(pObject.get("levels"), pDeserializationContext);
         boolean flag = JSONUtils.getAsBoolean(pObject, "treasure", false);
         return new EnchantWithLevels(pConditions, irandomrange, flag);
      }
   }
}