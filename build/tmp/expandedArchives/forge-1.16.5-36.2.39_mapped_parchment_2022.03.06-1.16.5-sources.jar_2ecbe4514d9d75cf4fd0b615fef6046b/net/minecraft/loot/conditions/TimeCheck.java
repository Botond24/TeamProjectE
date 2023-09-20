package net.minecraft.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.util.JSONUtils;
import net.minecraft.world.server.ServerWorld;

/**
 * A LootItemCondition that checks the {@linkplain ServerLevel#getDayTime day time} against an {@link IntRange} after
 * applying an optional modulo division.
 */
public class TimeCheck implements ILootCondition {
   @Nullable
   private final Long period;
   private final RandomValueRange value;

   private TimeCheck(@Nullable Long p_i225898_1_, RandomValueRange p_i225898_2_) {
      this.period = p_i225898_1_;
      this.value = p_i225898_2_;
   }

   public LootConditionType getType() {
      return LootConditionManager.TIME_CHECK;
   }

   public boolean test(LootContext p_test_1_) {
      ServerWorld serverworld = p_test_1_.getLevel();
      long i = serverworld.getDayTime();
      if (this.period != null) {
         i %= this.period;
      }

      return this.value.matchesValue((int)i);
   }

   public static class Serializer implements ILootSerializer<TimeCheck> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, TimeCheck pValue, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("period", pValue.period);
         pJson.add("value", pSerializationContext.serialize(pValue.value));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public TimeCheck deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         Long olong = pJson.has("period") ? JSONUtils.getAsLong(pJson, "period") : null;
         RandomValueRange randomvaluerange = JSONUtils.getAsObject(pJson, "value", pSerializationContext, RandomValueRange.class);
         return new TimeCheck(olong, randomvaluerange);
      }
   }
}