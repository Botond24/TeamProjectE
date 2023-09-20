package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.ResourceLocation;

public class ConstructBeaconTrigger extends AbstractCriterionTrigger<ConstructBeaconTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("construct_beacon");

   public ResourceLocation getId() {
      return ID;
   }

   public ConstructBeaconTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(pJson.get("level"));
      return new ConstructBeaconTrigger.Instance(pEntityPredicate, minmaxbounds$intbound);
   }

   public void trigger(ServerPlayerEntity p_192180_1_, BeaconTileEntity p_192180_2_) {
      this.trigger(p_192180_1_, (p_226308_1_) -> {
         return p_226308_1_.matches(p_192180_2_);
      });
   }

   public static class Instance extends CriterionInstance {
      private final MinMaxBounds.IntBound level;

      public Instance(EntityPredicate.AndPredicate p_i231507_1_, MinMaxBounds.IntBound p_i231507_2_) {
         super(ConstructBeaconTrigger.ID, p_i231507_1_);
         this.level = p_i231507_2_;
      }

      public static ConstructBeaconTrigger.Instance constructedBeacon(MinMaxBounds.IntBound pLevel) {
         return new ConstructBeaconTrigger.Instance(EntityPredicate.AndPredicate.ANY, pLevel);
      }

      public boolean matches(BeaconTileEntity p_192252_1_) {
         return this.level.matches(p_192252_1_.getLevels());
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("level", this.level.serializeToJson());
         return jsonobject;
      }
   }
}