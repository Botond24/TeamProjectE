package net.minecraft.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * A LootItemCondition that checks whether the {@linkplain LootContextParams#BLOCK_STATE block state} matches a given
 * Block and {@link StatePropertiesPredicate}.
 */
public class BlockStateProperty implements ILootCondition {
   private final Block block;
   private final StatePropertiesPredicate properties;

   private BlockStateProperty(Block pBlock, StatePropertiesPredicate pStatePredicate) {
      this.block = pBlock;
      this.properties = pStatePredicate;
   }

   public LootConditionType getType() {
      return LootConditionManager.BLOCK_STATE_PROPERTY;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.BLOCK_STATE);
   }

   public boolean test(LootContext p_test_1_) {
      BlockState blockstate = p_test_1_.getParamOrNull(LootParameters.BLOCK_STATE);
      return blockstate != null && this.block == blockstate.getBlock() && this.properties.matches(blockstate);
   }

   public static BlockStateProperty.Builder hasBlockStateProperties(Block pBlock) {
      return new BlockStateProperty.Builder(pBlock);
   }

   public static class Builder implements ILootCondition.IBuilder {
      private final Block block;
      private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

      public Builder(Block pBlock) {
         this.block = pBlock;
      }

      public BlockStateProperty.Builder setProperties(StatePropertiesPredicate.Builder pStatePredicateBuilder) {
         this.properties = pStatePredicateBuilder.build();
         return this;
      }

      public ILootCondition build() {
         return new BlockStateProperty(this.block, this.properties);
      }
   }

   public static class Serializer implements ILootSerializer<BlockStateProperty> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, BlockStateProperty pValue, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("block", Registry.BLOCK.getKey(pValue.block).toString());
         pJson.add("properties", pValue.properties.serializeToJson());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public BlockStateProperty deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJson, "block"));
         Block block = Registry.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new IllegalArgumentException("Can't find block " + resourcelocation);
         });
         StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(pJson.get("properties"));
         statepropertiespredicate.checkState(block.getStateDefinition(), (p_227568_1_) -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + p_227568_1_);
         });
         return new BlockStateProperty(block, statepropertiespredicate);
      }
   }
}