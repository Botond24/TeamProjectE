package net.minecraft.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.IRandomRange;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.RandomRanges;
import net.minecraft.loot.conditions.ILootCondition;

/**
 * LootItemFunction that sets the stack's count based on a {@link NumberProvider}, optionally adding to any existing
 * count.
 */
public class SetCount extends LootFunction {
   private final IRandomRange value;

   private SetCount(ILootCondition[] p_i51222_1_, IRandomRange p_i51222_2_) {
      super(p_i51222_1_);
      this.value = p_i51222_2_;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_COUNT;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      pStack.setCount(this.value.getInt(pContext.getRandom()));
      return pStack;
   }

   public static LootFunction.Builder<?> setCount(IRandomRange p_215932_0_) {
      return simpleBuilder((p_215934_1_) -> {
         return new SetCount(p_215934_1_, p_215932_0_);
      });
   }

   public static class Serializer extends LootFunction.Serializer<SetCount> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetCount pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.add("count", RandomRanges.serialize(pValue.value, pSerializationContext));
      }

      public SetCount deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         IRandomRange irandomrange = RandomRanges.deserialize(pObject.get("count"), pDeserializationContext);
         return new SetCount(pConditions, irandomrange);
      }
   }
}