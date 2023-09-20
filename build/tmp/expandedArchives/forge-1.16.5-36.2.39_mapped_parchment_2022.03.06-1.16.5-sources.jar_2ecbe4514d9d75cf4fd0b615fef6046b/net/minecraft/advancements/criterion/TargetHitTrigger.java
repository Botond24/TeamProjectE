package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class TargetHitTrigger extends AbstractCriterionTrigger<TargetHitTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("target_hit");

   public ResourceLocation getId() {
      return ID;
   }

   public TargetHitTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(pJson.get("signal_strength"));
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "projectile", pConditionsParser);
      return new TargetHitTrigger.Instance(pEntityPredicate, minmaxbounds$intbound, entitypredicate$andpredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, Entity pProjectile, Vector3d pVector, int pSignalStrength) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pProjectile);
      this.trigger(pPlayer, (p_236349_3_) -> {
         return p_236349_3_.matches(lootcontext, pVector, pSignalStrength);
      });
   }

   public static class Instance extends CriterionInstance {
      private final MinMaxBounds.IntBound signalStrength;
      private final EntityPredicate.AndPredicate projectile;

      public Instance(EntityPredicate.AndPredicate p_i231990_1_, MinMaxBounds.IntBound p_i231990_2_, EntityPredicate.AndPredicate p_i231990_3_) {
         super(TargetHitTrigger.ID, p_i231990_1_);
         this.signalStrength = p_i231990_2_;
         this.projectile = p_i231990_3_;
      }

      public static TargetHitTrigger.Instance targetHit(MinMaxBounds.IntBound pSignalStrength, EntityPredicate.AndPredicate pProjectile) {
         return new TargetHitTrigger.Instance(EntityPredicate.AndPredicate.ANY, pSignalStrength, pProjectile);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("signal_strength", this.signalStrength.serializeToJson());
         jsonobject.add("projectile", this.projectile.toJson(pConditions));
         return jsonobject;
      }

      public boolean matches(LootContext pContext, Vector3d pVector, int pSignalStrength) {
         if (!this.signalStrength.matches(pSignalStrength)) {
            return false;
         } else {
            return this.projectile.matches(pContext);
         }
      }
   }
}