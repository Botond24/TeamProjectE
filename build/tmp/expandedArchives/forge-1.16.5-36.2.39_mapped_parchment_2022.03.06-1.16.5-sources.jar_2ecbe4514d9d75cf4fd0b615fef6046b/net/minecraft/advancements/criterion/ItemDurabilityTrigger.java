package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class ItemDurabilityTrigger extends AbstractCriterionTrigger<ItemDurabilityTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("item_durability_changed");

   public ResourceLocation getId() {
      return ID;
   }

   public ItemDurabilityTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(pJson.get("durability"));
      MinMaxBounds.IntBound minmaxbounds$intbound1 = MinMaxBounds.IntBound.fromJson(pJson.get("delta"));
      return new ItemDurabilityTrigger.Instance(pEntityPredicate, itempredicate, minmaxbounds$intbound, minmaxbounds$intbound1);
   }

   public void trigger(ServerPlayerEntity pPlayer, ItemStack pItem, int pNewDurability) {
      this.trigger(pPlayer, (p_226653_2_) -> {
         return p_226653_2_.matches(pItem, pNewDurability);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate item;
      private final MinMaxBounds.IntBound durability;
      private final MinMaxBounds.IntBound delta;

      public Instance(EntityPredicate.AndPredicate pPlayer, ItemPredicate pItem, MinMaxBounds.IntBound pDurability, MinMaxBounds.IntBound pDelta) {
         super(ItemDurabilityTrigger.ID, pPlayer);
         this.item = pItem;
         this.durability = pDurability;
         this.delta = pDelta;
      }

      public static ItemDurabilityTrigger.Instance changedDurability(EntityPredicate.AndPredicate pPlayer, ItemPredicate pItem, MinMaxBounds.IntBound pDurability) {
         return new ItemDurabilityTrigger.Instance(pPlayer, pItem, pDurability, MinMaxBounds.IntBound.ANY);
      }

      public boolean matches(ItemStack pItem, int pDurability) {
         if (!this.item.matches(pItem)) {
            return false;
         } else if (!this.durability.matches(pItem.getMaxDamage() - pDurability)) {
            return false;
         } else {
            return this.delta.matches(pItem.getDamageValue() - pDurability);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("durability", this.durability.serializeToJson());
         jsonobject.add("delta", this.delta.serializeToJson());
         return jsonobject;
      }
   }
}