package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class FilledBucketTrigger extends AbstractCriterionTrigger<FilledBucketTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("filled_bucket");

   public ResourceLocation getId() {
      return ID;
   }

   public FilledBucketTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new FilledBucketTrigger.Instance(pEntityPredicate, itempredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, ItemStack pStack) {
      this.trigger(pPlayer, (p_226627_1_) -> {
         return p_226627_1_.matches(pStack);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate item;

      public Instance(EntityPredicate.AndPredicate p_i231585_1_, ItemPredicate p_i231585_2_) {
         super(FilledBucketTrigger.ID, p_i231585_1_);
         this.item = p_i231585_2_;
      }

      public static FilledBucketTrigger.Instance filledBucket(ItemPredicate pItemCondition) {
         return new FilledBucketTrigger.Instance(EntityPredicate.AndPredicate.ANY, pItemCondition);
      }

      public boolean matches(ItemStack pStack) {
         return this.item.matches(pStack);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}