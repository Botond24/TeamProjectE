package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class KilledTrigger extends AbstractCriterionTrigger<KilledTrigger.Instance> {
   private final ResourceLocation id;

   public KilledTrigger(ResourceLocation p_i47433_1_) {
      this.id = p_i47433_1_;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public KilledTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      return new KilledTrigger.Instance(this.id, pEntityPredicate, EntityPredicate.AndPredicate.fromJson(pJson, "entity", pConditionsParser), DamageSourcePredicate.fromJson(pJson.get("killing_blow")));
   }

   public void trigger(ServerPlayerEntity pPlayer, Entity pEntity, DamageSource pSource) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_226846_3_) -> {
         return p_226846_3_.matches(pPlayer, lootcontext, pSource);
      });
   }

   public static class Instance extends CriterionInstance {
      private final EntityPredicate.AndPredicate entityPredicate;
      private final DamageSourcePredicate killingBlow;

      public Instance(ResourceLocation p_i231630_1_, EntityPredicate.AndPredicate p_i231630_2_, EntityPredicate.AndPredicate p_i231630_3_, DamageSourcePredicate p_i231630_4_) {
         super(p_i231630_1_, p_i231630_2_);
         this.entityPredicate = p_i231630_3_;
         this.killingBlow = p_i231630_4_;
      }

      public static KilledTrigger.Instance playerKilledEntity(EntityPredicate.Builder pBuilder) {
         return new KilledTrigger.Instance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.wrap(pBuilder.build()), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.Instance playerKilledEntity() {
         return new KilledTrigger.Instance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY, DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.Instance playerKilledEntity(EntityPredicate.Builder pEntityBuilder, DamageSourcePredicate.Builder pSourceBuilder) {
         return new KilledTrigger.Instance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.wrap(pEntityBuilder.build()), pSourceBuilder.build());
      }

      public static KilledTrigger.Instance entityKilledPlayer() {
         return new KilledTrigger.Instance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY, DamageSourcePredicate.ANY);
      }

      public boolean matches(ServerPlayerEntity pPlayer, LootContext pContext, DamageSource pSource) {
         return !this.killingBlow.matches(pPlayer, pSource) ? false : this.entityPredicate.matches(pContext);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("entity", this.entityPredicate.toJson(pConditions));
         jsonobject.add("killing_blow", this.killingBlow.serializeToJson());
         return jsonobject;
      }
   }
}