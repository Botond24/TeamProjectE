package net.minecraft.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;

public class BlockModeInfo<T> {
   private final String key;
   private final Function<T, JsonElement> serializer;

   public BlockModeInfo(String pKey, Function<T, JsonElement> pSerializer) {
      this.key = pKey;
      this.serializer = pSerializer;
   }

   public BlockModeInfo<T>.Field withValue(T pValue) {
      return new BlockModeInfo.Field(pValue);
   }

   public String toString() {
      return this.key;
   }

   public class Field {
      private final T value;

      public Field(T pValue) {
         this.value = pValue;
      }

      public void addToVariant(JsonObject pJsonObject) {
         pJsonObject.add(BlockModeInfo.this.key, BlockModeInfo.this.serializer.apply(this.value));
      }

      public String toString() {
         return BlockModeInfo.this.key + "=" + this.value;
      }
   }
}