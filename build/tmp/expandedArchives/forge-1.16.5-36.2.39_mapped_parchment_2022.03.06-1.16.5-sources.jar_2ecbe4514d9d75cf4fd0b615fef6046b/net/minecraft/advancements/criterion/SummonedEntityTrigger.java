package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class SummonedEntityTrigger extends AbstractCriterionTrigger<SummonedEntityTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("summoned_entity");

   public ResourceLocation getId() {
      return ID;
   }

   public SummonedEntityTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "entity", pConditionsParser);
      return new SummonedEntityTrigger.Instance(pEntityPredicate, entitypredicate$andpredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, Entity pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_227229_1_) -> {
         return p_227229_1_.matches(lootcontext);
      });
   }

   public static class Instance extends CriterionInstance {
      private final EntityPredicate.AndPredicate entity;

      public Instance(EntityPredicate.AndPredicate p_i231938_1_, EntityPredicate.AndPredicate p_i231938_2_) {
         super(SummonedEntityTrigger.ID, p_i231938_1_);
         this.entity = p_i231938_2_;
      }

      public static SummonedEntityTrigger.Instance summonedEntity(EntityPredicate.Builder pEntityBuilder) {
         return new SummonedEntityTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.wrap(pEntityBuilder.build()));
      }

      public boolean matches(LootContext pLootContext) {
         return this.entity.matches(pLootContext);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}