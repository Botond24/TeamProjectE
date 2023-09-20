package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LootItemFunction that applies a random enchantment to the stack. If an empty list is given, chooses from all
 * enchantments.
 */
public class EnchantRandomly extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   private final List<Enchantment> enchantments;

   private EnchantRandomly(ILootCondition[] pConditions, Collection<Enchantment> pPossibleEnchantments) {
      super(pConditions);
      this.enchantments = ImmutableList.copyOf(pPossibleEnchantments);
   }

   public LootFunctionType getType() {
      return LootFunctionManager.ENCHANT_RANDOMLY;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Random random = pContext.getRandom();
      Enchantment enchantment;
      if (this.enchantments.isEmpty()) {
         boolean flag = pStack.getItem() == Items.BOOK;
         List<Enchantment> list = Registry.ENCHANTMENT.stream().filter(Enchantment::isDiscoverable).filter((p_237421_2_) -> {
            return flag || p_237421_2_.canEnchant(pStack);
         }).collect(Collectors.toList());
         if (list.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", (Object)pStack);
            return pStack;
         }

         enchantment = list.get(random.nextInt(list.size()));
      } else {
         enchantment = this.enchantments.get(random.nextInt(this.enchantments.size()));
      }

      return enchantItem(pStack, enchantment, random);
   }

   private static ItemStack enchantItem(ItemStack pStack, Enchantment pEnchantment, Random pRandom) {
      int i = MathHelper.nextInt(pRandom, pEnchantment.getMinLevel(), pEnchantment.getMaxLevel());
      if (pStack.getItem() == Items.BOOK) {
         pStack = new ItemStack(Items.ENCHANTED_BOOK);
         EnchantedBookItem.addEnchantment(pStack, new EnchantmentData(pEnchantment, i));
      } else {
         pStack.enchant(pEnchantment, i);
      }

      return pStack;
   }

   public static LootFunction.Builder<?> randomApplicableEnchantment() {
      return simpleBuilder((p_237422_0_) -> {
         return new EnchantRandomly(p_237422_0_, ImmutableList.of());
      });
   }

   public static class Builder extends LootFunction.Builder<EnchantRandomly.Builder> {
      private final Set<Enchantment> enchantments = Sets.newHashSet();

      protected EnchantRandomly.Builder getThis() {
         return this;
      }

      public EnchantRandomly.Builder withEnchantment(Enchantment pEnchantment) {
         this.enchantments.add(pEnchantment);
         return this;
      }

      public ILootFunction build() {
         return new EnchantRandomly(this.getConditions(), this.enchantments);
      }
   }

   public static class Serializer extends LootFunction.Serializer<EnchantRandomly> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, EnchantRandomly pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         if (!pValue.enchantments.isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(Enchantment enchantment : pValue.enchantments) {
               ResourceLocation resourcelocation = Registry.ENCHANTMENT.getKey(enchantment);
               if (resourcelocation == null) {
                  throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
               }

               jsonarray.add(new JsonPrimitive(resourcelocation.toString()));
            }

            pJson.add("enchantments", jsonarray);
         }

      }

      public EnchantRandomly deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         List<Enchantment> list = Lists.newArrayList();
         if (pObject.has("enchantments")) {
            for(JsonElement jsonelement : JSONUtils.getAsJsonArray(pObject, "enchantments")) {
               String s = JSONUtils.convertToString(jsonelement, "enchantment");
               Enchantment enchantment = Registry.ENCHANTMENT.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown enchantment '" + s + "'");
               });
               list.add(enchantment);
            }
         }

         return new EnchantRandomly(pConditions, list);
      }
   }
}