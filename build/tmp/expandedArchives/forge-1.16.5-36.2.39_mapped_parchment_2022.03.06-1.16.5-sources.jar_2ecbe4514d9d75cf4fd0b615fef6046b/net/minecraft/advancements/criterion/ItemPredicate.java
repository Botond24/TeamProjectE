package net.minecraft.advancements.criterion;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class ItemPredicate {
   private static final Map<ResourceLocation, java.util.function.Function<JsonObject, ItemPredicate>> custom_predicates = new java.util.HashMap<>();
   private static final Map<ResourceLocation, java.util.function.Function<JsonObject, ItemPredicate>> unmod_predicates = java.util.Collections.unmodifiableMap(custom_predicates);
   public static final ItemPredicate ANY = new ItemPredicate();
   @Nullable
   private final ITag<Item> tag;
   @Nullable
   private final Item item;
   private final MinMaxBounds.IntBound count;
   private final MinMaxBounds.IntBound durability;
   private final EnchantmentPredicate[] enchantments;
   private final EnchantmentPredicate[] storedEnchantments;
   @Nullable
   private final Potion potion;
   private final NBTPredicate nbt;

   public ItemPredicate() {
      this.tag = null;
      this.item = null;
      this.potion = null;
      this.count = MinMaxBounds.IntBound.ANY;
      this.durability = MinMaxBounds.IntBound.ANY;
      this.enchantments = EnchantmentPredicate.NONE;
      this.storedEnchantments = EnchantmentPredicate.NONE;
      this.nbt = NBTPredicate.ANY;
   }

   public ItemPredicate(@Nullable ITag<Item> pTag, @Nullable Item pItem, MinMaxBounds.IntBound pCount, MinMaxBounds.IntBound pDurability, EnchantmentPredicate[] pEnchantment, EnchantmentPredicate[] pStoredEnchantments, @Nullable Potion pPotion, NBTPredicate pNbt) {
      this.tag = pTag;
      this.item = pItem;
      this.count = pCount;
      this.durability = pDurability;
      this.enchantments = pEnchantment;
      this.storedEnchantments = pStoredEnchantments;
      this.potion = pPotion;
      this.nbt = pNbt;
   }

   public boolean matches(ItemStack pItem) {
      if (this == ANY) {
         return true;
      } else if (this.tag != null && !this.tag.contains(pItem.getItem())) {
         return false;
      } else if (this.item != null && pItem.getItem() != this.item) {
         return false;
      } else if (!this.count.matches(pItem.getCount())) {
         return false;
      } else if (!this.durability.isAny() && !pItem.isDamageableItem()) {
         return false;
      } else if (!this.durability.matches(pItem.getMaxDamage() - pItem.getDamageValue())) {
         return false;
      } else if (!this.nbt.matches(pItem)) {
         return false;
      } else {
         if (this.enchantments.length > 0) {
            Map<Enchantment, Integer> map = EnchantmentHelper.deserializeEnchantments(pItem.getEnchantmentTags());

            for(EnchantmentPredicate enchantmentpredicate : this.enchantments) {
               if (!enchantmentpredicate.containedIn(map)) {
                  return false;
               }
            }
         }

         if (this.storedEnchantments.length > 0) {
            Map<Enchantment, Integer> map1 = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(pItem));

            for(EnchantmentPredicate enchantmentpredicate1 : this.storedEnchantments) {
               if (!enchantmentpredicate1.containedIn(map1)) {
                  return false;
               }
            }
         }

         Potion potion = PotionUtils.getPotion(pItem);
         return this.potion == null || this.potion == potion;
      }
   }

   public static ItemPredicate fromJson(@Nullable JsonElement pElement) {
      if (pElement != null && !pElement.isJsonNull()) {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "item");
         if (jsonobject.has("type")) {
            final ResourceLocation rl = new ResourceLocation(JSONUtils.getAsString(jsonobject, "type"));
            if (custom_predicates.containsKey(rl)) return custom_predicates.get(rl).apply(jsonobject);
            else throw new JsonSyntaxException("There is no ItemPredicate of type "+rl);
         }
         MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(jsonobject.get("count"));
         MinMaxBounds.IntBound minmaxbounds$intbound1 = MinMaxBounds.IntBound.fromJson(jsonobject.get("durability"));
         if (jsonobject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
         } else {
            NBTPredicate nbtpredicate = NBTPredicate.fromJson(jsonobject.get("nbt"));
            Item item = null;
            if (jsonobject.has("item")) {
               ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(jsonobject, "item"));
               item = Registry.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown item id '" + resourcelocation + "'");
               });
            }

            ITag<Item> itag = null;
            if (jsonobject.has("tag")) {
               ResourceLocation resourcelocation1 = new ResourceLocation(JSONUtils.getAsString(jsonobject, "tag"));
               itag = TagCollectionManager.getInstance().getItems().getTag(resourcelocation1);
               if (itag == null) {
                  throw new JsonSyntaxException("Unknown item tag '" + resourcelocation1 + "'");
               }
            }

            Potion potion = null;
            if (jsonobject.has("potion")) {
               ResourceLocation resourcelocation2 = new ResourceLocation(JSONUtils.getAsString(jsonobject, "potion"));
               potion = Registry.POTION.getOptional(resourcelocation2).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown potion '" + resourcelocation2 + "'");
               });
            }

            EnchantmentPredicate[] aenchantmentpredicate1 = EnchantmentPredicate.fromJsonArray(jsonobject.get("enchantments"));
            EnchantmentPredicate[] aenchantmentpredicate = EnchantmentPredicate.fromJsonArray(jsonobject.get("stored_enchantments"));
            return new ItemPredicate(itag, item, minmaxbounds$intbound, minmaxbounds$intbound1, aenchantmentpredicate1, aenchantmentpredicate, potion, nbtpredicate);
         }
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.item != null) {
            jsonobject.addProperty("item", Registry.ITEM.getKey(this.item).toString());
         }

         if (this.tag != null) {
            jsonobject.addProperty("tag", TagCollectionManager.getInstance().getItems().getIdOrThrow(this.tag).toString());
         }

         jsonobject.add("count", this.count.serializeToJson());
         jsonobject.add("durability", this.durability.serializeToJson());
         jsonobject.add("nbt", this.nbt.serializeToJson());
         if (this.enchantments.length > 0) {
            JsonArray jsonarray = new JsonArray();

            for(EnchantmentPredicate enchantmentpredicate : this.enchantments) {
               jsonarray.add(enchantmentpredicate.serializeToJson());
            }

            jsonobject.add("enchantments", jsonarray);
         }

         if (this.storedEnchantments.length > 0) {
            JsonArray jsonarray1 = new JsonArray();

            for(EnchantmentPredicate enchantmentpredicate1 : this.storedEnchantments) {
               jsonarray1.add(enchantmentpredicate1.serializeToJson());
            }

            jsonobject.add("stored_enchantments", jsonarray1);
         }

         if (this.potion != null) {
            jsonobject.addProperty("potion", Registry.POTION.getKey(this.potion).toString());
         }

         return jsonobject;
      }
   }

   public static ItemPredicate[] fromJsonArray(@Nullable JsonElement pElement) {
      if (pElement != null && !pElement.isJsonNull()) {
         JsonArray jsonarray = JSONUtils.convertToJsonArray(pElement, "items");
         ItemPredicate[] aitempredicate = new ItemPredicate[jsonarray.size()];

         for(int i = 0; i < aitempredicate.length; ++i) {
            aitempredicate[i] = fromJson(jsonarray.get(i));
         }

         return aitempredicate;
      } else {
         return new ItemPredicate[0];
      }
   }

   public static void register(ResourceLocation name, java.util.function.Function<JsonObject, ItemPredicate> deserializer) {
      custom_predicates.put(name, deserializer);
   }

   public static Map<ResourceLocation, java.util.function.Function<JsonObject, ItemPredicate>> getPredicates() {
      return unmod_predicates;
   }

   public static class Builder {
      private final List<EnchantmentPredicate> enchantments = Lists.newArrayList();
      private final List<EnchantmentPredicate> storedEnchantments = Lists.newArrayList();
      @Nullable
      private Item item;
      @Nullable
      private ITag<Item> tag;
      private MinMaxBounds.IntBound count = MinMaxBounds.IntBound.ANY;
      private MinMaxBounds.IntBound durability = MinMaxBounds.IntBound.ANY;
      @Nullable
      private Potion potion;
      private NBTPredicate nbt = NBTPredicate.ANY;

      private Builder() {
      }

      public static ItemPredicate.Builder item() {
         return new ItemPredicate.Builder();
      }

      public ItemPredicate.Builder of(IItemProvider p_200308_1_) {
         this.item = p_200308_1_.asItem();
         return this;
      }

      public ItemPredicate.Builder of(ITag<Item> pTag) {
         this.tag = pTag;
         return this;
      }

      public ItemPredicate.Builder hasNbt(CompoundNBT pNbt) {
         this.nbt = new NBTPredicate(pNbt);
         return this;
      }

      public ItemPredicate.Builder hasEnchantment(EnchantmentPredicate pEnchantmentCondition) {
         this.enchantments.add(pEnchantmentCondition);
         return this;
      }

      public ItemPredicate build() {
         return new ItemPredicate(this.tag, this.item, this.count, this.durability, this.enchantments.toArray(EnchantmentPredicate.NONE), this.storedEnchantments.toArray(EnchantmentPredicate.NONE), this.potion, this.nbt);
      }
   }
}
