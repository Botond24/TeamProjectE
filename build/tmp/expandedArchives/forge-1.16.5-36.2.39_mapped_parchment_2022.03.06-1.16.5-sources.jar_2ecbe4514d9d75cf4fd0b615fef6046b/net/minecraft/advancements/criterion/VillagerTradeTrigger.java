package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class VillagerTradeTrigger extends AbstractCriterionTrigger<VillagerTradeTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("villager_trade");

   public ResourceLocation getId() {
      return ID;
   }

   public VillagerTradeTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "villager", pConditionsParser);
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new VillagerTradeTrigger.Instance(pEntityPredicate, entitypredicate$andpredicate, itempredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, AbstractVillagerEntity pVillager, ItemStack pStack) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pVillager);
      this.trigger(pPlayer, (p_227267_2_) -> {
         return p_227267_2_.matches(lootcontext, pStack);
      });
   }

   public static class Instance extends CriterionInstance {
      private final EntityPredicate.AndPredicate villager;
      private final ItemPredicate item;

      public Instance(EntityPredicate.AndPredicate pPlayer, EntityPredicate.AndPredicate pVillager, ItemPredicate pItem) {
         super(VillagerTradeTrigger.ID, pPlayer);
         this.villager = pVillager;
         this.item = pItem;
      }

      public static VillagerTradeTrigger.Instance tradedWithVillager() {
         return new VillagerTradeTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY, ItemPredicate.ANY);
      }

      public boolean matches(LootContext pContext, ItemStack pStack) {
         if (!this.villager.matches(pContext)) {
            return false;
         } else {
            return this.item.matches(pStack);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("villager", this.villager.toJson(pConditions));
         return jsonobject;
      }
   }
}