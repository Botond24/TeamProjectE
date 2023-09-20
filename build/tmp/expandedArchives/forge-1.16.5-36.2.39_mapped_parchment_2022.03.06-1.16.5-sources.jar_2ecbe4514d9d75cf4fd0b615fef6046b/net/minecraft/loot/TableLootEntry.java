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
 * A loot pool entry container that generates loot by referencing another loot table.
 */
public class TableLootEntry extends StandaloneLootEntry {
   private final ResourceLocation name;

   private TableLootEntry(ResourceLocation pLootTableId, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
      super(pWeight, pQuality, pConditions, pFunctions);
      this.name = pLootTableId;
   }

   public LootPoolEntryType getType() {
      return LootEntryManager.REFERENCE;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      LootTable loottable = pLootContext.getLootTable(this.name);
      loottable.getRandomItemsRaw(pLootContext, pStackConsumer);
   }

   public void validate(ValidationTracker pValidationContext) {
      if (pValidationContext.hasVisitedTable(this.name)) {
         pValidationContext.reportProblem("Table " + this.name + " is recursively called");
      } else {
         super.validate(pValidationContext);
         LootTable loottable = pValidationContext.resolveLootTable(this.name);
         if (loottable == null) {
            pValidationContext.reportProblem("Unknown loot table called " + this.name);
         } else {
            loottable.validate(pValidationContext.enterTable("->{" + this.name + "}", this.name));
         }

      }
   }

   public static StandaloneLootEntry.Builder<?> lootTableReference(ResourceLocation pTable) {
      return simpleBuilder((p_216173_1_, p_216173_2_, p_216173_3_, p_216173_4_) -> {
         return new TableLootEntry(pTable, p_216173_1_, p_216173_2_, p_216173_3_, p_216173_4_);
      });
   }

   public static class Serializer extends StandaloneLootEntry.Serializer<TableLootEntry> {
      public void serializeCustom(JsonObject pObject, TableLootEntry pContext, JsonSerializationContext pConditions) {
         super.serializeCustom(pObject, pContext, pConditions);
         pObject.addProperty("name", pContext.name.toString());
      }

      protected TableLootEntry deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pObject, "name"));
         return new TableLootEntry(resourcelocation, pWeight, pQuality, pConditions, pFunctions);
      }
   }
}