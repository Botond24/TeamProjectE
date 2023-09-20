package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * A LootItemCondition that provides a random chance based on the level of a certain enchantment on the {@linkplain
 * LootContextParams#TOOL tool}.
 * The chances are given as an array of float values that represent the given chance (0..1) for the enchantment level
 * corresponding to the index.
 * {@code [0.2, 0.3, 0.6]} would provide a 20% chance for not enchanted, 30% chance for enchanted at level 1 and 60%
 * chance for enchanted at level 2 or above.
 */
public class TableBonus implements ILootCondition {
   private final Enchantment enchantment;
   private final float[] values;

   private TableBonus(Enchantment pEnchantment, float[] pChances) {
      this.enchantment = pEnchantment;
      this.values = pChances;
   }

   public LootConditionType getType() {
      return LootConditionManager.TABLE_BONUS;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.TOOL);
   }

   public boolean test(LootContext p_test_1_) {
      ItemStack itemstack = p_test_1_.getParamOrNull(LootParameters.TOOL);
      int i = itemstack != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemstack) : 0;
      float f = this.values[Math.min(i, this.values.length - 1)];
      return p_test_1_.getRandom().nextFloat() < f;
   }

   public static ILootCondition.IBuilder bonusLevelFlatChance(Enchantment pEnchantment, float... pChances) {
      return () -> {
         return new TableBonus(pEnchantment, pChances);
      };
   }

   public static class Serializer implements ILootSerializer<TableBonus> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, TableBonus pValue, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("enchantment", Registry.ENCHANTMENT.getKey(pValue.enchantment).toString());
         pJson.add("chances", pSerializationContext.serialize(pValue.values));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public TableBonus deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJson, "enchantment"));
         Enchantment enchantment = Registry.ENCHANTMENT.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonParseException("Invalid enchantment id: " + resourcelocation);
         });
         float[] afloat = JSONUtils.getAsObject(pJson, "chances", pSerializationContext, float[].class);
         return new TableBonus(enchantment, afloat);
      }
   }
}