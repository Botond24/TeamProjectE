package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;

/**
 * A LootItemCondition which checks {@link LootContextParams#ORIGIN} and {@link LootContextParams#DAMAGE_SOURCE} against
 * a {@link DamageSourcePredicate}.
 */
public class DamageSourceProperties implements ILootCondition {
   private final DamageSourcePredicate predicate;

   private DamageSourceProperties(DamageSourcePredicate pDamageSourcePredicate) {
      this.predicate = pDamageSourcePredicate;
   }

   public LootConditionType getType() {
      return LootConditionManager.DAMAGE_SOURCE_PROPERTIES;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.ORIGIN, LootParameters.DAMAGE_SOURCE);
   }

   public boolean test(LootContext p_test_1_) {
      DamageSource damagesource = p_test_1_.getParamOrNull(LootParameters.DAMAGE_SOURCE);
      Vector3d vector3d = p_test_1_.getParamOrNull(LootParameters.ORIGIN);
      return vector3d != null && damagesource != null && this.predicate.matches(p_test_1_.getLevel(), vector3d, damagesource);
   }

   public static ILootCondition.IBuilder hasDamageSource(DamageSourcePredicate.Builder pBuilder) {
      return () -> {
         return new DamageSourceProperties(pBuilder.build());
      };
   }

   public static class Serializer implements ILootSerializer<DamageSourceProperties> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, DamageSourceProperties pValue, JsonSerializationContext pSerializationContext) {
         pJson.add("predicate", pValue.predicate.serializeToJson());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public DamageSourceProperties deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         DamageSourcePredicate damagesourcepredicate = DamageSourcePredicate.fromJson(pJson.get("predicate"));
         return new DamageSourceProperties(damagesourcepredicate);
      }
   }
}