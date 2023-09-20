package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.potion.Potion;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

public class ConsumeItemTrigger extends AbstractCriterionTrigger<ConsumeItemTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("consume_item");

   public ResourceLocation getId() {
      return ID;
   }

   public ConsumeItemTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      return new ConsumeItemTrigger.Instance(pEntityPredicate, ItemPredicate.fromJson(pJson.get("item")));
   }

   public void trigger(ServerPlayerEntity pPlayer, ItemStack pItem) {
      this.trigger(pPlayer, (p_226325_1_) -> {
         return p_226325_1_.matches(pItem);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate item;

      public Instance(EntityPredicate.AndPredicate p_i231522_1_, ItemPredicate p_i231522_2_) {
         super(ConsumeItemTrigger.ID, p_i231522_1_);
         this.item = p_i231522_2_;
      }

      public static ConsumeItemTrigger.Instance usedItem() {
         return new ConsumeItemTrigger.Instance(EntityPredicate.AndPredicate.ANY, ItemPredicate.ANY);
      }

      public static ConsumeItemTrigger.Instance usedItem(IItemProvider pItem) {
         return new ConsumeItemTrigger.Instance(EntityPredicate.AndPredicate.ANY, new ItemPredicate((ITag<Item>)null, pItem.asItem(), MinMaxBounds.IntBound.ANY, MinMaxBounds.IntBound.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, (Potion)null, NBTPredicate.ANY));
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