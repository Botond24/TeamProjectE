package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class EffectsChangedTrigger extends AbstractCriterionTrigger<EffectsChangedTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("effects_changed");

   public ResourceLocation getId() {
      return ID;
   }

   public EffectsChangedTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(pJson.get("effects"));
      return new EffectsChangedTrigger.Instance(pEntityPredicate, mobeffectspredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer) {
      this.trigger(pPlayer, (p_226524_1_) -> {
         return p_226524_1_.matches(pPlayer);
      });
   }

   public static class Instance extends CriterionInstance {
      private final MobEffectsPredicate effects;

      public Instance(EntityPredicate.AndPredicate pPlayer, MobEffectsPredicate pEffects) {
         super(EffectsChangedTrigger.ID, pPlayer);
         this.effects = pEffects;
      }

      public static EffectsChangedTrigger.Instance hasEffects(MobEffectsPredicate pEffects) {
         return new EffectsChangedTrigger.Instance(EntityPredicate.AndPredicate.ANY, pEffects);
      }

      public boolean matches(ServerPlayerEntity pPlayer) {
         return this.effects.matches(pPlayer);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("effects", this.effects.serializeToJson());
         return jsonobject;
      }
   }
}