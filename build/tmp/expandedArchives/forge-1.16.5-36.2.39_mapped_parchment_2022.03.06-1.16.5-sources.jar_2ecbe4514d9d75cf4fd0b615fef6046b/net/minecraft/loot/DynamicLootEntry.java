package net.minecraft.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * A loot pool entry container that will genererate the dynamic drops with a given name.
 * 
 * @see LootContext.DynamicDrops
 */
public class DynamicLootEntry extends StandaloneLootEntry {
   private final ResourceLocation name;

   private DynamicLootEntry(ResourceLocation pDynamicDropsName, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
      super(pWeight, pQuality, pConditions, pFunctions);
      this.name = pDynamicDropsName;
   }

   public LootPoolEntryType getType() {
      return LootEntryManager.DYNAMIC;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      pLootContext.addDynamicDrops(this.name, pStackConsumer);
   }

   public static StandaloneLootEntry.Builder<?> dynamicEntry(ResourceLocation pDynamicDropsName) {
      return simpleBuilder((p_216164_1_, p_216164_2_, p_216164_3_, p_216164_4_) -> {
         return new DynamicLootEntry(pDynamicDropsName, p_216164_1_, p_216164_2_, p_216164_3_, p_216164_4_);
      });
   }

   public static class Serializer extends StandaloneLootEntry.Serializer<DynamicLootEntry> {
      public void serializeCustom(JsonObject pObject, DynamicLootEntry pContext, JsonSerializationContext pConditions) {
         super.serializeCustom(pObject, pContext, pConditions);
         pObject.addProperty("name", pContext.name.toString());
      }

      protected DynamicLootEntry deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pObject, "name"));
         return new DynamicLootEntry(resourcelocation, pWeight, pQuality, pConditions, pFunctions);
      }
   }
}