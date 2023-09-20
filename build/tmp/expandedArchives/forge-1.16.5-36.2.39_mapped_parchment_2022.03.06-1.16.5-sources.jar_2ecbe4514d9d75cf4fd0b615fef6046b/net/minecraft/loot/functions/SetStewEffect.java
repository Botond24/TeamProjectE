package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.potion.Effect;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * LootItemFunction that adds an effect to any suspicious stew items. A random effect is chosen from the given map every
 * time.
 */
public class SetStewEffect extends LootFunction {
   private final Map<Effect, RandomValueRange> effectDurationMap;

   private SetStewEffect(ILootCondition[] pConditions, Map<Effect, RandomValueRange> pEffectDurationMap) {
      super(pConditions);
      this.effectDurationMap = ImmutableMap.copyOf(pEffectDurationMap);
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_STEW_EFFECT;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.getItem() == Items.SUSPICIOUS_STEW && !this.effectDurationMap.isEmpty()) {
         Random random = pContext.getRandom();
         int i = random.nextInt(this.effectDurationMap.size());
         Entry<Effect, RandomValueRange> entry = Iterables.get(this.effectDurationMap.entrySet(), i);
         Effect effect = entry.getKey();
         int j = entry.getValue().getInt(random);
         if (!effect.isInstantenous()) {
            j *= 20;
         }

         SuspiciousStewItem.saveMobEffect(pStack, effect, j);
         return pStack;
      } else {
         return pStack;
      }
   }

   public static SetStewEffect.Builder stewEffect() {
      return new SetStewEffect.Builder();
   }

   public static class Builder extends LootFunction.Builder<SetStewEffect.Builder> {
      private final Map<Effect, RandomValueRange> effectDurationMap = Maps.newHashMap();

      protected SetStewEffect.Builder getThis() {
         return this;
      }

      public SetStewEffect.Builder withEffect(Effect p_216077_1_, RandomValueRange p_216077_2_) {
         this.effectDurationMap.put(p_216077_1_, p_216077_2_);
         return this;
      }

      public ILootFunction build() {
         return new SetStewEffect(this.getConditions(), this.effectDurationMap);
      }
   }

   public static class Serializer extends LootFunction.Serializer<SetStewEffect> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetStewEffect pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         if (!pValue.effectDurationMap.isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(Effect effect : pValue.effectDurationMap.keySet()) {
               JsonObject jsonobject = new JsonObject();
               ResourceLocation resourcelocation = Registry.MOB_EFFECT.getKey(effect);
               if (resourcelocation == null) {
                  throw new IllegalArgumentException("Don't know how to serialize mob effect " + effect);
               }

               jsonobject.add("type", new JsonPrimitive(resourcelocation.toString()));
               jsonobject.add("duration", pSerializationContext.serialize(pValue.effectDurationMap.get(effect)));
               jsonarray.add(jsonobject);
            }

            pJson.add("effects", jsonarray);
         }

      }

      public SetStewEffect deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         Map<Effect, RandomValueRange> map = Maps.newHashMap();
         if (pObject.has("effects")) {
            for(JsonElement jsonelement : JSONUtils.getAsJsonArray(pObject, "effects")) {
               String s = JSONUtils.getAsString(jsonelement.getAsJsonObject(), "type");
               Effect effect = Registry.MOB_EFFECT.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown mob effect '" + s + "'");
               });
               RandomValueRange randomvaluerange = JSONUtils.getAsObject(jsonelement.getAsJsonObject(), "duration", pDeserializationContext, RandomValueRange.class);
               map.put(effect, randomvaluerange);
            }
         }

         return new SetStewEffect(pConditions, map);
      }
   }
}