package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import java.util.Collection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.ResourceLocation;

public class FishingRodHookedTrigger extends AbstractCriterionTrigger<FishingRodHookedTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("fishing_rod_hooked");

   public ResourceLocation getId() {
      return ID;
   }

   public FishingRodHookedTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("rod"));
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "entity", pConditionsParser);
      ItemPredicate itempredicate1 = ItemPredicate.fromJson(pJson.get("item"));
      return new FishingRodHookedTrigger.Instance(pEntityPredicate, itempredicate, entitypredicate$andpredicate, itempredicate1);
   }

   public void trigger(ServerPlayerEntity pPlayer, ItemStack pRod, FishingBobberEntity pEntity, Collection<ItemStack> pItems) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, (Entity)(pEntity.getHookedIn() != null ? pEntity.getHookedIn() : pEntity));
      this.trigger(pPlayer, (p_234658_3_) -> {
         return p_234658_3_.matches(pRod, lootcontext, pItems);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate rod;
      private final EntityPredicate.AndPredicate entity;
      private final ItemPredicate item;

      public Instance(EntityPredicate.AndPredicate p_i231592_1_, ItemPredicate p_i231592_2_, EntityPredicate.AndPredicate p_i231592_3_, ItemPredicate p_i231592_4_) {
         super(FishingRodHookedTrigger.ID, p_i231592_1_);
         this.rod = p_i231592_2_;
         this.entity = p_i231592_3_;
         this.item = p_i231592_4_;
      }

      public static FishingRodHookedTrigger.Instance fishedItem(ItemPredicate pRod, EntityPredicate pBobber, ItemPredicate pItem) {
         return new FishingRodHookedTrigger.Instance(EntityPredicate.AndPredicate.ANY, pRod, EntityPredicate.AndPredicate.wrap(pBobber), pItem);
      }

      public boolean matches(ItemStack pRod, LootContext pContext, Collection<ItemStack> pItems) {
         if (!this.rod.matches(pRod)) {
            return false;
         } else if (!this.entity.matches(pContext)) {
            return false;
         } else {
            if (this.item != ItemPredicate.ANY) {
               boolean flag = false;
               Entity entity = pContext.getParamOrNull(LootParameters.THIS_ENTITY);
               if (entity instanceof ItemEntity) {
                  ItemEntity itementity = (ItemEntity)entity;
                  if (this.item.matches(itementity.getItem())) {
                     flag = true;
                  }
               }

               for(ItemStack itemstack : pItems) {
                  if (this.item.matches(itemstack)) {
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }

            return true;
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("rod", this.rod.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(pConditions));
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}