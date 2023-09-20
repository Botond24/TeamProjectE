package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

public class PlacedBlockTrigger extends AbstractCriterionTrigger<PlacedBlockTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("placed_block");

   public ResourceLocation getId() {
      return ID;
   }

   public PlacedBlockTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      Block block = deserializeBlock(pJson);
      StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(pJson.get("state"));
      if (block != null) {
         statepropertiespredicate.checkState(block.getStateDefinition(), (p_226948_1_) -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + p_226948_1_ + ":");
         });
      }

      LocationPredicate locationpredicate = LocationPredicate.fromJson(pJson.get("location"));
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new PlacedBlockTrigger.Instance(pEntityPredicate, block, statepropertiespredicate, locationpredicate, itempredicate);
   }

   @Nullable
   private static Block deserializeBlock(JsonObject pObject) {
      if (pObject.has("block")) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pObject, "block"));
         return Registry.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
         });
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayerEntity pPlayer, BlockPos pPos, ItemStack pItem) {
      BlockState blockstate = pPlayer.getLevel().getBlockState(pPos);
      this.trigger(pPlayer, (p_226949_4_) -> {
         return p_226949_4_.matches(blockstate, pPos, pPlayer.getLevel(), pItem);
      });
   }

   public static class Instance extends CriterionInstance {
      private final Block block;
      private final StatePropertiesPredicate state;
      private final LocationPredicate location;
      private final ItemPredicate item;

      public Instance(EntityPredicate.AndPredicate p_i231810_1_, @Nullable Block p_i231810_2_, StatePropertiesPredicate p_i231810_3_, LocationPredicate p_i231810_4_, ItemPredicate p_i231810_5_) {
         super(PlacedBlockTrigger.ID, p_i231810_1_);
         this.block = p_i231810_2_;
         this.state = p_i231810_3_;
         this.location = p_i231810_4_;
         this.item = p_i231810_5_;
      }

      public static PlacedBlockTrigger.Instance placedBlock(Block pBlock) {
         return new PlacedBlockTrigger.Instance(EntityPredicate.AndPredicate.ANY, pBlock, StatePropertiesPredicate.ANY, LocationPredicate.ANY, ItemPredicate.ANY);
      }

      public boolean matches(BlockState pState, BlockPos pPos, ServerWorld pLevel, ItemStack pItem) {
         if (this.block != null && !pState.is(this.block)) {
            return false;
         } else if (!this.state.matches(pState)) {
            return false;
         } else if (!this.location.matches(pLevel, (float)pPos.getX(), (float)pPos.getY(), (float)pPos.getZ())) {
            return false;
         } else {
            return this.item.matches(pItem);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         if (this.block != null) {
            jsonobject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
         }

         jsonobject.add("state", this.state.serializeToJson());
         jsonobject.add("location", this.location.serializeToJson());
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}