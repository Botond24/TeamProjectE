package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class EnterBlockTrigger extends AbstractCriterionTrigger<EnterBlockTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("enter_block");

   public ResourceLocation getId() {
      return ID;
   }

   public EnterBlockTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      Block block = deserializeBlock(pJson);
      StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(pJson.get("state"));
      if (block != null) {
         statepropertiespredicate.checkState(block.getStateDefinition(), (p_226548_1_) -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + p_226548_1_);
         });
      }

      return new EnterBlockTrigger.Instance(pEntityPredicate, block, statepropertiespredicate);
   }

   @Nullable
   private static Block deserializeBlock(JsonObject pJsonObject) {
      if (pJsonObject.has("block")) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJsonObject, "block"));
         return Registry.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
         });
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayerEntity pPlayer, BlockState pState) {
      this.trigger(pPlayer, (p_226549_1_) -> {
         return p_226549_1_.matches(pState);
      });
   }

   public static class Instance extends CriterionInstance {
      private final Block block;
      private final StatePropertiesPredicate state;

      public Instance(EntityPredicate.AndPredicate p_i231560_1_, @Nullable Block p_i231560_2_, StatePropertiesPredicate p_i231560_3_) {
         super(EnterBlockTrigger.ID, p_i231560_1_);
         this.block = p_i231560_2_;
         this.state = p_i231560_3_;
      }

      public static EnterBlockTrigger.Instance entersBlock(Block pBlock) {
         return new EnterBlockTrigger.Instance(EntityPredicate.AndPredicate.ANY, pBlock, StatePropertiesPredicate.ANY);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         if (this.block != null) {
            jsonobject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
         }

         jsonobject.add("state", this.state.serializeToJson());
         return jsonobject;
      }

      public boolean matches(BlockState pState) {
         if (this.block != null && !pState.is(this.block)) {
            return false;
         } else {
            return this.state.matches(pState);
         }
      }
   }
}