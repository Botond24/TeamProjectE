package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class TameAnimalTrigger extends AbstractCriterionTrigger<TameAnimalTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("tame_animal");

   public ResourceLocation getId() {
      return ID;
   }

   public TameAnimalTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "entity", pConditionsParser);
      return new TameAnimalTrigger.Instance(pEntityPredicate, entitypredicate$andpredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, AnimalEntity pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_227251_1_) -> {
         return p_227251_1_.matches(lootcontext);
      });
   }

   public static class Instance extends CriterionInstance {
      private final EntityPredicate.AndPredicate entity;

      public Instance(EntityPredicate.AndPredicate p_i231963_1_, EntityPredicate.AndPredicate p_i231963_2_) {
         super(TameAnimalTrigger.ID, p_i231963_1_);
         this.entity = p_i231963_2_;
      }

      public static TameAnimalTrigger.Instance tamedAnimal() {
         return new TameAnimalTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY);
      }

      public static TameAnimalTrigger.Instance tamedAnimal(EntityPredicate pEntityCondition) {
         return new TameAnimalTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.wrap(pEntityCondition));
      }

      public boolean matches(LootContext pContext) {
         return this.entity.matches(pContext);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}