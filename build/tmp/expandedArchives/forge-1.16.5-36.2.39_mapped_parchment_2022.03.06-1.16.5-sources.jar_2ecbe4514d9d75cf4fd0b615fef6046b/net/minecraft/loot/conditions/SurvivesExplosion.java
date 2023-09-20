package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;

/**
 * A LootItemCondition that checks whether an item should survive from an explosion or not.
 * This condition checks the {@linkplain LootContextParams#EXPLOSION_RADIUS explosion radius loot parameter}.
 */
public class SurvivesExplosion implements ILootCondition {
   private static final SurvivesExplosion INSTANCE = new SurvivesExplosion();

   private SurvivesExplosion() {
   }

   public LootConditionType getType() {
      return LootConditionManager.SURVIVES_EXPLOSION;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.EXPLOSION_RADIUS);
   }

   public boolean test(LootContext p_test_1_) {
      Float f = p_test_1_.getParamOrNull(LootParameters.EXPLOSION_RADIUS);
      if (f != null) {
         Random random = p_test_1_.getRandom();
         float f1 = 1.0F / f;
         return random.nextFloat() <= f1;
      } else {
         return true;
      }
   }

   public static ILootCondition.IBuilder survivesExplosion() {
      return () -> {
         return INSTANCE;
      };
   }

   public static class Serializer implements ILootSerializer<SurvivesExplosion> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SurvivesExplosion pValue, JsonSerializationContext pSerializationContext) {
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public SurvivesExplosion deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         return SurvivesExplosion.INSTANCE;
      }
   }
}