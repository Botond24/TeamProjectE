package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class BeeNestDestroyedTrigger extends AbstractCriterionTrigger<BeeNestDestroyedTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("bee_nest_destroyed");

   public ResourceLocation getId() {
      return ID;
   }

   public BeeNestDestroyedTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      Block block = deserializeBlock(pJson);
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(pJson.get("num_bees_inside"));
      return new BeeNestDestroyedTrigger.Instance(pEntityPredicate, block, itempredicate, minmaxbounds$intbound);
   }

   @Nullable
   private static Block deserializeBlock(JsonObject pJson) {
      if (pJson.has("block")) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJson, "block"));
         return Registry.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
         });
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayerEntity pPlayer, Block pBlock, ItemStack pStack, int pBeesNumber) {
      this.trigger(pPlayer, (p_226220_3_) -> {
         return p_226220_3_.matches(pBlock, pStack, pBeesNumber);
      });
   }

   public static class Instance extends CriterionInstance {
      @Nullable
      private final Block block;
      private final ItemPredicate item;
      private final MinMaxBounds.IntBound numBees;

      public Instance(EntityPredicate.AndPredicate pPlayer, @Nullable Block pBlock, ItemPredicate pItem, MinMaxBounds.IntBound pNumBees) {
         super(BeeNestDestroyedTrigger.ID, pPlayer);
         this.block = pBlock;
         this.item = pItem;
         this.numBees = pNumBees;
      }

      public static BeeNestDestroyedTrigger.Instance destroyedBeeNest(Block pBlock, ItemPredicate.Builder pItemConditionBuilder, MinMaxBounds.IntBound pBeesContained) {
         return new BeeNestDestroyedTrigger.Instance(EntityPredicate.AndPredicate.ANY, pBlock, pItemConditionBuilder.build(), pBeesContained);
      }

      public boolean matches(Block pBlock, ItemStack pStack, int pBeesNumber) {
         if (this.block != null && pBlock != this.block) {
            return false;
         } else {
            return !this.item.matches(pStack) ? false : this.numBees.matches(pBeesNumber);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         if (this.block != null) {
            jsonobject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
         }

         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("num_bees_inside", this.numBees.serializeToJson());
         return jsonobject;
      }
   }
}