package net.minecraft.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BlockModelDefinition implements Supplier<JsonElement> {
   private final Map<BlockModeInfo<?>, BlockModeInfo<?>.Field> values = Maps.newLinkedHashMap();

   public <T> BlockModelDefinition with(BlockModeInfo<T> pProperty, T pValue) {
      BlockModeInfo<?>.Field blockmodeinfo = this.values.put(pProperty, pProperty.withValue(pValue));
      if (blockmodeinfo != null) {
         throw new IllegalStateException("Replacing value of " + blockmodeinfo + " with " + pValue);
      } else {
         return this;
      }
   }

   public static BlockModelDefinition variant() {
      return new BlockModelDefinition();
   }

   public static BlockModelDefinition merge(BlockModelDefinition pDefinition1, BlockModelDefinition pDefinition2) {
      BlockModelDefinition blockmodeldefinition = new BlockModelDefinition();
      blockmodeldefinition.values.putAll(pDefinition1.values);
      blockmodeldefinition.values.putAll(pDefinition2.values);
      return blockmodeldefinition;
   }

   public JsonElement get() {
      JsonObject jsonobject = new JsonObject();
      this.values.values().forEach((p_240196_1_) -> {
         p_240196_1_.addToVariant(jsonobject);
      });
      return jsonobject;
   }

   public static JsonElement convertList(List<BlockModelDefinition> pDefinitions) {
      if (pDefinitions.size() == 1) {
         return pDefinitions.get(0).get();
      } else {
         JsonArray jsonarray = new JsonArray();
         pDefinitions.forEach((p_240195_1_) -> {
            jsonarray.add(p_240195_1_.get());
         });
         return jsonarray;
      }
   }
}