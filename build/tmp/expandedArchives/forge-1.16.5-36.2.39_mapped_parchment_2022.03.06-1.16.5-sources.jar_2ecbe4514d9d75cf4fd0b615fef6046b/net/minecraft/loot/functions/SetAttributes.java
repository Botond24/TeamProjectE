package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

/**
 * LootItemFunction that adds a list of attribute modifiers to the stacks.
 */
public class SetAttributes extends LootFunction {
   private final List<SetAttributes.Modifier> modifiers;

   private SetAttributes(ILootCondition[] pConditions, List<SetAttributes.Modifier> pModifiers) {
      super(pConditions);
      this.modifiers = ImmutableList.copyOf(pModifiers);
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_ATTRIBUTES;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Random random = pContext.getRandom();

      for(SetAttributes.Modifier setattributes$modifier : this.modifiers) {
         UUID uuid = setattributes$modifier.id;
         if (uuid == null) {
            uuid = UUID.randomUUID();
         }

         EquipmentSlotType equipmentslottype = Util.getRandom(setattributes$modifier.slots, random);
         pStack.addAttributeModifier(setattributes$modifier.attribute, new AttributeModifier(uuid, setattributes$modifier.name, (double)setattributes$modifier.amount.getFloat(random), setattributes$modifier.operation), equipmentslottype);
      }

      return pStack;
   }

   static class Modifier {
      private final String name;
      private final Attribute attribute;
      private final AttributeModifier.Operation operation;
      private final RandomValueRange amount;
      @Nullable
      private final UUID id;
      private final EquipmentSlotType[] slots;

      private Modifier(String p_i232172_1_, Attribute p_i232172_2_, AttributeModifier.Operation p_i232172_3_, RandomValueRange p_i232172_4_, EquipmentSlotType[] p_i232172_5_, @Nullable UUID p_i232172_6_) {
         this.name = p_i232172_1_;
         this.attribute = p_i232172_2_;
         this.operation = p_i232172_3_;
         this.amount = p_i232172_4_;
         this.id = p_i232172_6_;
         this.slots = p_i232172_5_;
      }

      public JsonObject serialize(JsonSerializationContext pContext) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("name", this.name);
         jsonobject.addProperty("attribute", Registry.ATTRIBUTE.getKey(this.attribute).toString());
         jsonobject.addProperty("operation", operationToString(this.operation));
         jsonobject.add("amount", pContext.serialize(this.amount));
         if (this.id != null) {
            jsonobject.addProperty("id", this.id.toString());
         }

         if (this.slots.length == 1) {
            jsonobject.addProperty("slot", this.slots[0].getName());
         } else {
            JsonArray jsonarray = new JsonArray();

            for(EquipmentSlotType equipmentslottype : this.slots) {
               jsonarray.add(new JsonPrimitive(equipmentslottype.getName()));
            }

            jsonobject.add("slot", jsonarray);
         }

         return jsonobject;
      }

      public static SetAttributes.Modifier deserialize(JsonObject pJsonObj, JsonDeserializationContext pContext) {
         String s = JSONUtils.getAsString(pJsonObj, "name");
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJsonObj, "attribute"));
         Attribute attribute = Registry.ATTRIBUTE.get(resourcelocation);
         if (attribute == null) {
            throw new JsonSyntaxException("Unknown attribute: " + resourcelocation);
         } else {
            AttributeModifier.Operation attributemodifier$operation = operationFromString(JSONUtils.getAsString(pJsonObj, "operation"));
            RandomValueRange randomvaluerange = JSONUtils.getAsObject(pJsonObj, "amount", pContext, RandomValueRange.class);
            UUID uuid = null;
            EquipmentSlotType[] aequipmentslottype;
            if (JSONUtils.isStringValue(pJsonObj, "slot")) {
               aequipmentslottype = new EquipmentSlotType[]{EquipmentSlotType.byName(JSONUtils.getAsString(pJsonObj, "slot"))};
            } else {
               if (!JSONUtils.isArrayNode(pJsonObj, "slot")) {
                  throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
               }

               JsonArray jsonarray = JSONUtils.getAsJsonArray(pJsonObj, "slot");
               aequipmentslottype = new EquipmentSlotType[jsonarray.size()];
               int i = 0;

               for(JsonElement jsonelement : jsonarray) {
                  aequipmentslottype[i++] = EquipmentSlotType.byName(JSONUtils.convertToString(jsonelement, "slot"));
               }

               if (aequipmentslottype.length == 0) {
                  throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
               }
            }

            if (pJsonObj.has("id")) {
               String s1 = JSONUtils.getAsString(pJsonObj, "id");

               try {
                  uuid = UUID.fromString(s1);
               } catch (IllegalArgumentException illegalargumentexception) {
                  throw new JsonSyntaxException("Invalid attribute modifier id '" + s1 + "' (must be UUID format, with dashes)");
               }
            }

            return new SetAttributes.Modifier(s, attribute, attributemodifier$operation, randomvaluerange, aequipmentslottype, uuid);
         }
      }

      private static String operationToString(AttributeModifier.Operation pOperation) {
         switch(pOperation) {
         case ADDITION:
            return "addition";
         case MULTIPLY_BASE:
            return "multiply_base";
         case MULTIPLY_TOTAL:
            return "multiply_total";
         default:
            throw new IllegalArgumentException("Unknown operation " + pOperation);
         }
      }

      private static AttributeModifier.Operation operationFromString(String pName) {
         switch(pName) {
         case "addition":
            return AttributeModifier.Operation.ADDITION;
         case "multiply_base":
            return AttributeModifier.Operation.MULTIPLY_BASE;
         case "multiply_total":
            return AttributeModifier.Operation.MULTIPLY_TOTAL;
         default:
            throw new JsonSyntaxException("Unknown attribute modifier operation " + pName);
         }
      }
   }

   public static class Serializer extends LootFunction.Serializer<SetAttributes> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetAttributes pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         JsonArray jsonarray = new JsonArray();

         for(SetAttributes.Modifier setattributes$modifier : pValue.modifiers) {
            jsonarray.add(setattributes$modifier.serialize(pSerializationContext));
         }

         pJson.add("modifiers", jsonarray);
      }

      public SetAttributes deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         JsonArray jsonarray = JSONUtils.getAsJsonArray(pObject, "modifiers");
         List<SetAttributes.Modifier> list = Lists.newArrayListWithExpectedSize(jsonarray.size());

         for(JsonElement jsonelement : jsonarray) {
            list.add(SetAttributes.Modifier.deserialize(JSONUtils.convertToJsonObject(jsonelement, "modifier"), pDeserializationContext));
         }

         if (list.isEmpty()) {
            throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
         } else {
            return new SetAttributes(pConditions, list);
         }
      }
   }
}