package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

public class PositionTrigger extends AbstractCriterionTrigger<PositionTrigger.Instance> {
   private final ResourceLocation id;

   public PositionTrigger(ResourceLocation p_i47432_1_) {
      this.id = p_i47432_1_;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public PositionTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      JsonObject jsonobject = JSONUtils.getAsJsonObject(pJson, "location", pJson);
      LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject);
      return new PositionTrigger.Instance(this.id, pEntityPredicate, locationpredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer) {
      this.trigger(pPlayer, (p_226923_1_) -> {
         return p_226923_1_.matches(pPlayer.getLevel(), pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
      });
   }

   public static class Instance extends CriterionInstance {
      private final LocationPredicate location;

      public Instance(ResourceLocation p_i231661_1_, EntityPredicate.AndPredicate p_i231661_2_, LocationPredicate p_i231661_3_) {
         super(p_i231661_1_, p_i231661_2_);
         this.location = p_i231661_3_;
      }

      public static PositionTrigger.Instance located(LocationPredicate pLocation) {
         return new PositionTrigger.Instance(CriteriaTriggers.LOCATION.id, EntityPredicate.AndPredicate.ANY, pLocation);
      }

      public static PositionTrigger.Instance sleptInBed() {
         return new PositionTrigger.Instance(CriteriaTriggers.SLEPT_IN_BED.id, EntityPredicate.AndPredicate.ANY, LocationPredicate.ANY);
      }

      public static PositionTrigger.Instance raidWon() {
         return new PositionTrigger.Instance(CriteriaTriggers.RAID_WIN.id, EntityPredicate.AndPredicate.ANY, LocationPredicate.ANY);
      }

      public boolean matches(ServerWorld pLevel, double pX, double pY, double pZ) {
         return this.location.matches(pLevel, pX, pY, pZ);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("location", this.location.serializeToJson());
         return jsonobject;
      }
   }
}