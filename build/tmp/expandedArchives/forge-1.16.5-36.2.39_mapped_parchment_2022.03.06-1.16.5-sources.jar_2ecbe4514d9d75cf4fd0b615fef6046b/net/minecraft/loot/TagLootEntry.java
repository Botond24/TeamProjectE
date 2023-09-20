package net.minecraft.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * A loot pool entry container that generates based on an item tag.
 * If {@code expand} is set to true, it will expand into separate LootPoolEntries for every item in the tag, otherwise
 * it will simply generate all items in the tag.
 */
public class TagLootEntry extends StandaloneLootEntry {
   private final ITag<Item> tag;
   private final boolean expand;

   private TagLootEntry(ITag<Item> pTag, boolean pExpand, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
      super(pWeight, pQuality, pConditions, pFunctions);
      this.tag = pTag;
      this.expand = pExpand;
   }

   public LootPoolEntryType getType() {
      return LootEntryManager.TAG;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      this.tag.getValues().forEach((p_216174_1_) -> {
         pStackConsumer.accept(new ItemStack(p_216174_1_));
      });
   }

   private boolean expandTag(LootContext pContext, Consumer<ILootGenerator> pGeneratorConsumer) {
      if (!this.canRun(pContext)) {
         return false;
      } else {
         for(final Item item : this.tag.getValues()) {
            pGeneratorConsumer.accept(new StandaloneLootEntry.Generator() {
               /**
                * Generate the loot stacks of this entry.
                * Contrary to the method name this method does not always generate one stack, it can also generate zero
                * or multiple stacks.
                */
               public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
                  pStackConsumer.accept(new ItemStack(item));
               }
            });
         }

         return true;
      }
   }

   public boolean expand(LootContext p_expand_1_, Consumer<ILootGenerator> p_expand_2_) {
      return this.expand ? this.expandTag(p_expand_1_, p_expand_2_) : super.expand(p_expand_1_, p_expand_2_);
   }

   public static StandaloneLootEntry.Builder<?> expandTag(ITag<Item> pTag) {
      return simpleBuilder((p_216177_1_, p_216177_2_, p_216177_3_, p_216177_4_) -> {
         return new TagLootEntry(pTag, true, p_216177_1_, p_216177_2_, p_216177_3_, p_216177_4_);
      });
   }

   public static class Serializer extends StandaloneLootEntry.Serializer<TagLootEntry> {
      public void serializeCustom(JsonObject pObject, TagLootEntry pContext, JsonSerializationContext pConditions) {
         super.serializeCustom(pObject, pContext, pConditions);
         pObject.addProperty("name", TagCollectionManager.getInstance().getItems().getIdOrThrow(pContext.tag).toString());
         pObject.addProperty("expand", pContext.expand);
      }

      protected TagLootEntry deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, ILootCondition[] pConditions, ILootFunction[] pFunctions) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pObject, "name"));
         ITag<Item> itag = TagCollectionManager.getInstance().getItems().getTag(resourcelocation);
         if (itag == null) {
            throw new JsonParseException("Can't find tag: " + resourcelocation);
         } else {
            boolean flag = JSONUtils.getAsBoolean(pObject, "expand");
            return new TagLootEntry(itag, flag, pWeight, pQuality, pConditions, pFunctions);
         }
      }
   }
}