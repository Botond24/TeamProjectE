package net.minecraft.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;

/**
 * A loot pool entry that does not generate any items.
 */
public class EmptyLootEntry extends StandaloneLootEntry {
   private EmptyLootEntry(int p_i51258_1_, int p_i51258_2_, ILootCondition[] p_i51258_3_, ILootFunction[] p_i51258_4_) {
      super(p_i51258_1_, p_i51258_2_, p_i51258_3_, p_i51258_4_);
   }

   public LootPoolEntryType getType() {
      return LootEntryManager.EMPTY;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
   }

   public static StandaloneLootEntry.Builder<?> emptyItem() {
      return simpleBuilder(EmptyLootEntry::new);
   }

   public static class Serializer extends StandaloneLootEntry.Serializer<EmptyLootEntry> {
      public EmptyLootEntry deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
         return new EmptyLootEntry(pWeight, pQuality, pConditions, pFunctions);
      }
   }
}