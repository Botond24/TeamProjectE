package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class EnchantedItemTrigger extends AbstractCriterionTrigger<EnchantedItemTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("enchanted_item");

   public ResourceLocation getId() {
      return ID;
   }

   public EnchantedItemTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(pJson.get("levels"));
      return new EnchantedItemTrigger.Instance(pEntityPredicate, itempredicate, minmaxbounds$intbound);
   }

   public void trigger(ServerPlayerEntity pPlayer, ItemStack pItem, int pLevelsSpent) {
      this.trigger(pPlayer, (p_226528_2_) -> {
         return p_226528_2_.matches(pItem, pLevelsSpent);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate item;
      private final MinMaxBounds.IntBound levels;

      public Instance(EntityPredicate.AndPredicate pPlayer, ItemPredicate pItem, MinMaxBounds.IntBound pLevels) {
         super(EnchantedItemTrigger.ID, pPlayer);
         this.item = pItem;
         this.levels = pLevels;
      }

      public static EnchantedItemTrigger.Instance enchantedItem() {
         return new EnchantedItemTrigger.Instance(EntityPredicate.AndPredicate.ANY, ItemPredicate.ANY, MinMaxBounds.IntBound.ANY);
      }

      public boolean matches(ItemStack pItem, int pLevels) {
         if (!this.item.matches(pItem)) {
            return false;
         } else {
            return this.levels.matches(pLevels);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("levels", this.levels.serializeToJson());
         return jsonobject;
      }
   }
}