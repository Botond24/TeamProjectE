package net.minecraft.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * LootItemFunction that sets the LootTable and optionally the loot table seed on the stack's {@code BlockEntityTag}.
 * The effect of this is that containers such as chests will receive the given LootTable when placed.
 */
public class SetLootTable extends LootFunction {
   private final ResourceLocation name;
   private final long seed;

   private SetLootTable(ILootCondition[] pConditions, ResourceLocation pLootTableId, long pLootTableSeed) {
      super(pConditions);
      this.name = pLootTableId;
      this.seed = pLootTableSeed;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_LOOT_TABLE;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.isEmpty()) {
         return pStack;
      } else {
         CompoundNBT compoundnbt = new CompoundNBT();
         compoundnbt.putString("LootTable", this.name.toString());
         if (this.seed != 0L) {
            compoundnbt.putLong("LootTableSeed", this.seed);
         }

         pStack.getOrCreateTag().put("BlockEntityTag", compoundnbt);
         return pStack;
      }
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationTracker pContext) {
      if (pContext.hasVisitedTable(this.name)) {
         pContext.reportProblem("Table " + this.name + " is recursively called");
      } else {
         super.validate(pContext);
         LootTable loottable = pContext.resolveLootTable(this.name);
         if (loottable == null) {
            pContext.reportProblem("Unknown loot table called " + this.name);
         } else {
            loottable.validate(pContext.enterTable("->{" + this.name + "}", this.name));
         }

      }
   }

   public static class Serializer extends LootFunction.Serializer<SetLootTable> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetLootTable pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.addProperty("name", pValue.name.toString());
         if (pValue.seed != 0L) {
            pJson.addProperty("seed", pValue.seed);
         }

      }

      public SetLootTable deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pObject, "name"));
         long i = JSONUtils.getAsLong(pObject, "seed", 0L);
         return new SetLootTable(pConditions, resourcelocation, i);
      }
   }
}