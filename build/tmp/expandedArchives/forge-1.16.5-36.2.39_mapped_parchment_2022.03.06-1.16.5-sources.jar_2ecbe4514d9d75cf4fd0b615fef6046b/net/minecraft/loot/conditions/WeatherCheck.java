package net.minecraft.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.util.JSONUtils;
import net.minecraft.world.server.ServerWorld;

/**
 * A LootItemCondition that checks whether it currently raining or trhundering.
 * Both checks are optional.
 */
public class WeatherCheck implements ILootCondition {
   @Nullable
   private final Boolean isRaining;
   @Nullable
   private final Boolean isThundering;

   private WeatherCheck(@Nullable Boolean pIsRaining, @Nullable Boolean pIsThundering) {
      this.isRaining = pIsRaining;
      this.isThundering = pIsThundering;
   }

   public LootConditionType getType() {
      return LootConditionManager.WEATHER_CHECK;
   }

   public boolean test(LootContext p_test_1_) {
      ServerWorld serverworld = p_test_1_.getLevel();
      if (this.isRaining != null && this.isRaining != serverworld.isRaining()) {
         return false;
      } else {
         return this.isThundering == null || this.isThundering == serverworld.isThundering();
      }
   }

   public static class Serializer implements ILootSerializer<WeatherCheck> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, WeatherCheck pValue, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("raining", pValue.isRaining);
         pJson.addProperty("thundering", pValue.isThundering);
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public WeatherCheck deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         Boolean obool = pJson.has("raining") ? JSONUtils.getAsBoolean(pJson, "raining") : null;
         Boolean obool1 = pJson.has("thundering") ? JSONUtils.getAsBoolean(pJson, "thundering") : null;
         return new WeatherCheck(obool, obool1);
      }
   }
}