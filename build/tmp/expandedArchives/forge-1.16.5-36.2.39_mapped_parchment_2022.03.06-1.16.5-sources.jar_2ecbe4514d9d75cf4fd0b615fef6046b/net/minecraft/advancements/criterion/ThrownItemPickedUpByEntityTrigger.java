package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class ThrownItemPickedUpByEntityTrigger extends AbstractCriterionTrigger<ThrownItemPickedUpByEntityTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("thrown_item_picked_up_by_entity");

   public ResourceLocation getId() {
      return ID;
   }

   protected ThrownItemPickedUpByEntityTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "entity", pConditionsParser);
      return new ThrownItemPickedUpByEntityTrigger.Instance(pEntityPredicate, itempredicate, entitypredicate$andpredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, ItemStack pStack, Entity pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_234831_3_) -> {
         return p_234831_3_.matches(pPlayer, pStack, lootcontext);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate item;
      private final EntityPredicate.AndPredicate entity;

      public Instance(EntityPredicate.AndPredicate p_i231599_1_, ItemPredicate p_i231599_2_, EntityPredicate.AndPredicate p_i231599_3_) {
         super(ThrownItemPickedUpByEntityTrigger.ID, p_i231599_1_);
         this.item = p_i231599_2_;
         this.entity = p_i231599_3_;
      }

      public static ThrownItemPickedUpByEntityTrigger.Instance itemPickedUpByEntity(EntityPredicate.AndPredicate pPlayer, ItemPredicate.Builder pStack, EntityPredicate.AndPredicate pEntity) {
         return new ThrownItemPickedUpByEntityTrigger.Instance(pPlayer, pStack.build(), pEntity);
      }

      public boolean matches(ServerPlayerEntity pPlayer, ItemStack pStack, LootContext pContext) {
         if (!this.item.matches(pStack)) {
            return false;
         } else {
            return this.entity.matches(pContext);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}