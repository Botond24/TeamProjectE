package net.minecraft.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * A loot pool entry that always generates a given item.
 */
public class ItemLootEntry extends StandaloneLootEntry {
   private final Item item;

   private ItemLootEntry(Item pItem, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
      super(pWeight, pQuality, pConditions, pFunctions);
      this.item = pItem;
   }

   public LootPoolEntryType getType() {
      return LootEntryManager.ITEM;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      pStackConsumer.accept(new ItemStack(this.item));
   }

   public static StandaloneLootEntry.Builder<?> lootTableItem(IItemProvider pItem) {
      return simpleBuilder((p_216169_1_, p_216169_2_, p_216169_3_, p_216169_4_) -> {
         return new ItemLootEntry(pItem.asItem(), p_216169_1_, p_216169_2_, p_216169_3_, p_216169_4_);
      });
   }

   public static class Serializer extends StandaloneLootEntry.Serializer<ItemLootEntry> {
      public void serializeCustom(JsonObject pObject, ItemLootEntry pContext, JsonSerializationContext pConditions) {
         super.serializeCustom(pObject, pContext, pConditions);
         ResourceLocation resourcelocation = Registry.ITEM.getKey(pContext.item);
         if (resourcelocation == null) {
            throw new IllegalArgumentException("Can't serialize unknown item " + pContext.item);
         } else {
            pObject.addProperty("name", resourcelocation.toString());
         }
      }

      protected ItemLootEntry deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
         Item item = JSONUtils.getAsItem(pObject, "name");
         return new ItemLootEntry(item, pWeight, pQuality, pConditions, pFunctions);
      }
   }
}