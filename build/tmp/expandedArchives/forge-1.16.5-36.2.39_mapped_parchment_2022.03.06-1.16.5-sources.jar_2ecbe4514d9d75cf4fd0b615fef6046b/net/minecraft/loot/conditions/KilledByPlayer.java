package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;

/**
 * A LootItemCondition that matches if the last damage to an entity was done by a player.
 * 
 * @see LootContextParams#LAST_DAMAGE_PLAYER
 */
public class KilledByPlayer implements ILootCondition {
   private static final KilledByPlayer INSTANCE = new KilledByPlayer();

   private KilledByPlayer() {
   }

   public LootConditionType getType() {
      return LootConditionManager.KILLED_BY_PLAYER;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.LAST_DAMAGE_PLAYER);
   }

   public boolean test(LootContext p_test_1_) {
      return p_test_1_.hasParam(LootParameters.LAST_DAMAGE_PLAYER);
   }

   public static ILootCondition.IBuilder killedByPlayer() {
      return () -> {
         return INSTANCE;
      };
   }

   public static class Serializer implements ILootSerializer<KilledByPlayer> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, KilledByPlayer pValue, JsonSerializationContext pSerializationContext) {
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public KilledByPlayer deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         return KilledByPlayer.INSTANCE;
      }
   }
}