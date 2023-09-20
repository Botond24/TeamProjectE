package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * LootItemFunction that copies a set of block state properties to the {@code "BlockStateTag"} NBT tag of the ItemStack.
 * This tag is checked when the block is placed.
 */
public class CopyBlockState extends LootFunction {
   private final Block block;
   private final Set<Property<?>> properties;

   private CopyBlockState(ILootCondition[] pConditions, Block pBlock, Set<Property<?>> pProperties) {
      super(pConditions);
      this.block = pBlock;
      this.properties = pProperties;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.COPY_STATE;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.BLOCK_STATE);
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   protected ItemStack run(ItemStack pStack, LootContext pContext) {
      BlockState blockstate = pContext.getParamOrNull(LootParameters.BLOCK_STATE);
      if (blockstate != null) {
         CompoundNBT compoundnbt = pStack.getOrCreateTag();
         CompoundNBT compoundnbt1;
         if (compoundnbt.contains("BlockStateTag", 10)) {
            compoundnbt1 = compoundnbt.getCompound("BlockStateTag");
         } else {
            compoundnbt1 = new CompoundNBT();
            compoundnbt.put("BlockStateTag", compoundnbt1);
         }

         this.properties.stream().filter(blockstate::hasProperty).forEach((p_227548_2_) -> {
            compoundnbt1.putString(p_227548_2_.getName(), serialize(blockstate, p_227548_2_));
         });
      }

      return pStack;
   }

   public static CopyBlockState.Builder copyState(Block pBlock) {
      return new CopyBlockState.Builder(pBlock);
   }

   private static <T extends Comparable<T>> String serialize(BlockState pBlockState, Property<T> pProperty) {
      T t = pBlockState.getValue(pProperty);
      return pProperty.getName(t);
   }

   public static class Builder extends LootFunction.Builder<CopyBlockState.Builder> {
      private final Block block;
      private final Set<Property<?>> properties = Sets.newHashSet();

      private Builder(Block pBlock) {
         this.block = pBlock;
      }

      public CopyBlockState.Builder copy(Property<?> pProperty) {
         if (!this.block.getStateDefinition().getProperties().contains(pProperty)) {
            throw new IllegalStateException("Property " + pProperty + " is not present on block " + this.block);
         } else {
            this.properties.add(pProperty);
            return this;
         }
      }

      protected CopyBlockState.Builder getThis() {
         return this;
      }

      public ILootFunction build() {
         return new CopyBlockState(this.getConditions(), this.block, this.properties);
      }
   }

   public static class Serializer extends LootFunction.Serializer<CopyBlockState> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, CopyBlockState pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.addProperty("block", Registry.BLOCK.getKey(pValue.block).toString());
         JsonArray jsonarray = new JsonArray();
         pValue.properties.forEach((p_227553_1_) -> {
            jsonarray.add(p_227553_1_.getName());
         });
         pJson.add("properties", jsonarray);
      }

      public CopyBlockState deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pObject, "block"));
         Block block = Registry.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new IllegalArgumentException("Can't find block " + resourcelocation);
         });
         StateContainer<Block, BlockState> statecontainer = block.getStateDefinition();
         Set<Property<?>> set = Sets.newHashSet();
         JsonArray jsonarray = JSONUtils.getAsJsonArray(pObject, "properties", (JsonArray)null);
         if (jsonarray != null) {
            jsonarray.forEach((p_227554_2_) -> {
               set.add(statecontainer.getProperty(JSONUtils.convertToString(p_227554_2_, "property")));
            });
         }

         return new CopyBlockState(pConditions, block, set);
      }
   }
}