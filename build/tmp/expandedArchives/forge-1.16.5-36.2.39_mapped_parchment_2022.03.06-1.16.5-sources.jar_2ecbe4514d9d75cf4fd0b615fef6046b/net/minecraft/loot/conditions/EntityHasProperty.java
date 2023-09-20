package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector3d;

/**
 * A LootItemCondition that checks a given {@link EntityPredicate} against a given {@link LootContext.EntityTarget}.
 */
public class EntityHasProperty implements ILootCondition {
   private final EntityPredicate predicate;
   private final LootContext.EntityTarget entityTarget;

   private EntityHasProperty(EntityPredicate pEntityPredicate, LootContext.EntityTarget pEntityTarget) {
      this.predicate = pEntityPredicate;
      this.entityTarget = pEntityTarget;
   }

   public LootConditionType getType() {
      return LootConditionManager.ENTITY_PROPERTIES;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.ORIGIN, this.entityTarget.getParam());
   }

   public boolean test(LootContext p_test_1_) {
      Entity entity = p_test_1_.getParamOrNull(this.entityTarget.getParam());
      Vector3d vector3d = p_test_1_.getParamOrNull(LootParameters.ORIGIN);
      return this.predicate.matches(p_test_1_.getLevel(), vector3d, entity);
   }

   public static ILootCondition.IBuilder entityPresent(LootContext.EntityTarget pTarget) {
      return hasProperties(pTarget, EntityPredicate.Builder.entity());
   }

   public static ILootCondition.IBuilder hasProperties(LootContext.EntityTarget pTarget, EntityPredicate.Builder pPredicateBuilder) {
      return () -> {
         return new EntityHasProperty(pPredicateBuilder.build(), pTarget);
      };
   }

   public static ILootCondition.IBuilder hasProperties(LootContext.EntityTarget pTarget, EntityPredicate pEntityPredicate) {
      return () -> {
         return new EntityHasProperty(pEntityPredicate, pTarget);
      };
   }

   public static class Serializer implements ILootSerializer<EntityHasProperty> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, EntityHasProperty pValue, JsonSerializationContext pSerializationContext) {
         pJson.add("predicate", pValue.predicate.serializeToJson());
         pJson.add("entity", pSerializationContext.serialize(pValue.entityTarget));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public EntityHasProperty deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         EntityPredicate entitypredicate = EntityPredicate.fromJson(pJson.get("predicate"));
         return new EntityHasProperty(entitypredicate, JSONUtils.getAsObject(pJson, "entity", pSerializationContext, LootContext.EntityTarget.class));
      }
   }
}