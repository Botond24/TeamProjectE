package net.minecraft.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LootItemFunction that sets the stack's damage based on a {@link NumberProvider}, optionally adding to any existing
 * damage.
 */
public class SetDamage extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RandomValueRange damage;

   private SetDamage(ILootCondition[] p_i46622_1_, RandomValueRange p_i46622_2_) {
      super(p_i46622_1_);
      this.damage = p_i46622_2_;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_DAMAGE;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.isDamageableItem()) {
         float f = 1.0F - this.damage.getFloat(pContext.getRandom());
         pStack.setDamageValue(MathHelper.floor(f * (float)pStack.getMaxDamage()));
      } else {
         LOGGER.warn("Couldn't set damage of loot item {}", (Object)pStack);
      }

      return pStack;
   }

   public static LootFunction.Builder<?> setDamage(RandomValueRange p_215931_0_) {
      return simpleBuilder((p_215930_1_) -> {
         return new SetDamage(p_215930_1_, p_215931_0_);
      });
   }

   public static class Serializer extends LootFunction.Serializer<SetDamage> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetDamage pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.add("damage", pSerializationContext.serialize(pValue.damage));
      }

      public SetDamage deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         return new SetDamage(pConditions, JSONUtils.getAsObject(pObject, "damage", pDeserializationContext, RandomValueRange.class));
      }
   }
}