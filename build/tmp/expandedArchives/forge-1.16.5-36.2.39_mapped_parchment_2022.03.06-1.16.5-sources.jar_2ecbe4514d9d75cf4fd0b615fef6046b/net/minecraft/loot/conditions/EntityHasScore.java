package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.JSONUtils;

/**
 * A LootItemCondition that checks if an Entity selected by a {@link LootContext.EntityTarget} has a given set of
 * scores.
 * If one of the given objectives does not exist or the entity does not have a score for that objective, the condition
 * fails.
 */
public class EntityHasScore implements ILootCondition {
   private final Map<String, RandomValueRange> scores;
   private final LootContext.EntityTarget entityTarget;

   private EntityHasScore(Map<String, RandomValueRange> pScoreRanges, LootContext.EntityTarget pEntityTarget) {
      this.scores = ImmutableMap.copyOf(pScoreRanges);
      this.entityTarget = pEntityTarget;
   }

   public LootConditionType getType() {
      return LootConditionManager.ENTITY_SCORES;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.entityTarget.getParam());
   }

   public boolean test(LootContext p_test_1_) {
      Entity entity = p_test_1_.getParamOrNull(this.entityTarget.getParam());
      if (entity == null) {
         return false;
      } else {
         Scoreboard scoreboard = entity.level.getScoreboard();

         for(Entry<String, RandomValueRange> entry : this.scores.entrySet()) {
            if (!this.hasScore(entity, scoreboard, entry.getKey(), entry.getValue())) {
               return false;
            }
         }

         return true;
      }
   }

   protected boolean hasScore(Entity p_186631_1_, Scoreboard p_186631_2_, String p_186631_3_, RandomValueRange p_186631_4_) {
      ScoreObjective scoreobjective = p_186631_2_.getObjective(p_186631_3_);
      if (scoreobjective == null) {
         return false;
      } else {
         String s = p_186631_1_.getScoreboardName();
         return !p_186631_2_.hasPlayerScore(s, scoreobjective) ? false : p_186631_4_.matchesValue(p_186631_2_.getOrCreatePlayerScore(s, scoreobjective).getScore());
      }
   }

   public static class Serializer implements ILootSerializer<EntityHasScore> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, EntityHasScore pValue, JsonSerializationContext pSerializationContext) {
         JsonObject jsonobject = new JsonObject();

         for(Entry<String, RandomValueRange> entry : pValue.scores.entrySet()) {
            jsonobject.add(entry.getKey(), pSerializationContext.serialize(entry.getValue()));
         }

         pJson.add("scores", jsonobject);
         pJson.add("entity", pSerializationContext.serialize(pValue.entityTarget));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public EntityHasScore deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         Set<Entry<String, JsonElement>> set = JSONUtils.getAsJsonObject(pJson, "scores").entrySet();
         Map<String, RandomValueRange> map = Maps.newLinkedHashMap();

         for(Entry<String, JsonElement> entry : set) {
            map.put(entry.getKey(), JSONUtils.convertToObject(entry.getValue(), "score", pSerializationContext, RandomValueRange.class));
         }

         return new EntityHasScore(map, JSONUtils.getAsObject(pJson, "entity", pSerializationContext, LootContext.EntityTarget.class));
      }
   }
}