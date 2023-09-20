package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

public class UsedTotemTrigger extends AbstractCriterionTrigger<UsedTotemTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("used_totem");

   public ResourceLocation getId() {
      return ID;
   }

   public UsedTotemTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new UsedTotemTrigger.Instance(pEntityPredicate, itempredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, ItemStack pItem) {
      this.trigger(pPlayer, (p_227409_1_) -> {
         return p_227409_1_.matches(pItem);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate item;

      public Instance(EntityPredicate.AndPredicate p_i232051_1_, ItemPredicate p_i232051_2_) {
         super(UsedTotemTrigger.ID, p_i232051_1_);
         this.item = p_i232051_2_;
      }

      public static UsedTotemTrigger.Instance usedTotem(IItemProvider pItem) {
         return new UsedTotemTrigger.Instance(EntityPredicate.AndPredicate.ANY, ItemPredicate.Builder.item().of(pItem).build());
      }

      public boolean matches(ItemStack pItem) {
         return this.item.matches(pItem);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}