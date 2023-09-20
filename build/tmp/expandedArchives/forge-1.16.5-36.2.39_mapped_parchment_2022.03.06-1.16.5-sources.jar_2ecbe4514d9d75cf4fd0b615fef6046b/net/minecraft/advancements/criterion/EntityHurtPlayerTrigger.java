package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class EntityHurtPlayerTrigger extends AbstractCriterionTrigger<EntityHurtPlayerTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");

   public ResourceLocation getId() {
      return ID;
   }

   public EntityHurtPlayerTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      DamagePredicate damagepredicate = DamagePredicate.fromJson(pJson.get("damage"));
      return new EntityHurtPlayerTrigger.Instance(pEntityPredicate, damagepredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, DamageSource pSource, float pAmountDealt, float pAmountTaken, boolean pWasBlocked) {
      this.trigger(pPlayer, (p_226603_5_) -> {
         return p_226603_5_.matches(pPlayer, pSource, pAmountDealt, pAmountTaken, pWasBlocked);
      });
   }

   public static class Instance extends CriterionInstance {
      private final DamagePredicate damage;

      public Instance(EntityPredicate.AndPredicate p_i231572_1_, DamagePredicate p_i231572_2_) {
         super(EntityHurtPlayerTrigger.ID, p_i231572_1_);
         this.damage = p_i231572_2_;
      }

      public static EntityHurtPlayerTrigger.Instance entityHurtPlayer(DamagePredicate.Builder pDamageConditionBuilder) {
         return new EntityHurtPlayerTrigger.Instance(EntityPredicate.AndPredicate.ANY, pDamageConditionBuilder.build());
      }

      public boolean matches(ServerPlayerEntity pPlayer, DamageSource pSource, float pAmountDealt, float pAmountTaken, boolean pWasBlocked) {
         return this.damage.matches(pPlayer, pSource, pAmountDealt, pAmountTaken, pWasBlocked);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("damage", this.damage.serializeToJson());
         return jsonobject;
      }
   }
}