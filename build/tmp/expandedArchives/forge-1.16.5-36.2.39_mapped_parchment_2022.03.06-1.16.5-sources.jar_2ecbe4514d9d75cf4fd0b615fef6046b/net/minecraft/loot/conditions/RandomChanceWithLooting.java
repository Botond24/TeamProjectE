package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.JSONUtils;

/**
 * A LootItemCondition that does a random chance check with a bonus based on the {@linkplain
 * EnchantmentHelper#getMobLooting looting enchantment}.
 */
public class RandomChanceWithLooting implements ILootCondition {
   private final float percent;
   private final float lootingMultiplier;

   private RandomChanceWithLooting(float pPercent, float pLootingMultiplier) {
      this.percent = pPercent;
      this.lootingMultiplier = pLootingMultiplier;
   }

   public LootConditionType getType() {
      return LootConditionManager.RANDOM_CHANCE_WITH_LOOTING;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.KILLER_ENTITY);
   }

   public boolean test(LootContext p_test_1_) {
      int i = p_test_1_.getLootingModifier();
      return p_test_1_.getRandom().nextFloat() < this.percent + (float)i * this.lootingMultiplier;
   }

   /**
    * 
    * @param pChance The base chance
    * @param pLootingMultiplier The multiplier for the looting level. The result of the multiplication is added to the
    * chance.
    */
   public static ILootCondition.IBuilder randomChanceAndLootingBoost(float pChance, float pLootingMultiplier) {
      return () -> {
         return new RandomChanceWithLooting(pChance, pLootingMultiplier);
      };
   }

   public static class Serializer implements ILootSerializer<RandomChanceWithLooting> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, RandomChanceWithLooting pValue, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("chance", pValue.percent);
         pJson.addProperty("looting_multiplier", pValue.lootingMultiplier);
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public RandomChanceWithLooting deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         return new RandomChanceWithLooting(JSONUtils.getAsFloat(pJson, "chance"), JSONUtils.getAsFloat(pJson, "looting_multiplier"));
      }
   }
}
