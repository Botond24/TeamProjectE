package net.minecraft.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTable {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final LootTable EMPTY = new LootTable(LootParameterSets.EMPTY, new LootPool[0], new ILootFunction[0]);
   public static final LootParameterSet DEFAULT_PARAM_SET = LootParameterSets.ALL_PARAMS;
   private final LootParameterSet paramSet;
   private final List<LootPool> pools;
   private final ILootFunction[] functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

   private LootTable(LootParameterSet pParamSet, LootPool[] pPools, ILootFunction[] pFunctions) {
      this.paramSet = pParamSet;
      this.pools = Lists.newArrayList(pPools);
      this.functions = pFunctions;
      this.compositeFunction = LootFunctionManager.compose(pFunctions);
   }

   /**
    * Create a wrapped Consumer which will split stacks according to their maximum stack size before passing them on to
    * the given stackConsumer.
    */
   public static Consumer<ItemStack> createStackSplitter(Consumer<ItemStack> pStackConsumer) {
      return (p_216125_1_) -> {
         if (p_216125_1_.getCount() < p_216125_1_.getMaxStackSize()) {
            pStackConsumer.accept(p_216125_1_);
         } else {
            int i = p_216125_1_.getCount();

            while(i > 0) {
               ItemStack itemstack = p_216125_1_.copy();
               itemstack.setCount(Math.min(p_216125_1_.getMaxStackSize(), i));
               i -= itemstack.getCount();
               pStackConsumer.accept(itemstack);
            }
         }

      };
   }

   /**
    * Generate items to the given Consumer, ignoring maximum stack size.
    */
   public void getRandomItemsRaw(LootContext pContext, Consumer<ItemStack> pStacksOut) {
      if (pContext.addVisitedTable(this)) {
         Consumer<ItemStack> consumer = ILootFunction.decorate(this.compositeFunction, pStacksOut, pContext);

         for(LootPool lootpool : this.pools) {
            lootpool.addRandomItems(consumer, pContext);
         }

         pContext.removeVisitedTable(this);
      } else {
         LOGGER.warn("Detected infinite loop in loot tables");
      }

   }

   /**
    * Generate random items to the given Consumer, ensuring they do not exeed their maximum stack size.
    */
   @Deprecated //Use other method or manually call ForgeHooks.modifyLoot
   public void getRandomItems(LootContext pContextData, Consumer<ItemStack> pStacksOut) {
      this.getRandomItemsRaw(pContextData, createStackSplitter(pStacksOut));
   }

   /**
    * Generate random items to a List.
    */
   public List<ItemStack> getRandomItems(LootContext pContext) {
      List<ItemStack> list = Lists.newArrayList();
      this.getRandomItems(pContext, list::add);
      list = net.minecraftforge.common.ForgeHooks.modifyLoot(this.getLootTableId(), list, pContext);
      return list;
   }

   /**
    * Get the parameter set for this LootTable.
    */
   public LootParameterSet getParamSet() {
      return this.paramSet;
   }

   /**
    * Validate this LootTable using the given ValidationContext.
    */
   public void validate(ValidationTracker pValidator) {
      for(int i = 0; i < this.pools.size(); ++i) {
         this.pools.get(i).validate(pValidator.forChild(".pools[" + i + "]"));
      }

      for(int j = 0; j < this.functions.length; ++j) {
         this.functions[j].validate(pValidator.forChild(".functions[" + j + "]"));
      }

   }

   /**
    * Fill the given container with random items from this loot table.
    */
   public void fill(IInventory pContainer, LootContext pContext) {
      List<ItemStack> list = this.getRandomItems(pContext);
      Random random = pContext.getRandom();
      List<Integer> list1 = this.getAvailableSlots(pContainer, random);
      this.shuffleAndSplitItems(list, list1.size(), random);

      for(ItemStack itemstack : list) {
         if (list1.isEmpty()) {
            LOGGER.warn("Tried to over-fill a container");
            return;
         }

         if (itemstack.isEmpty()) {
            pContainer.setItem(list1.remove(list1.size() - 1), ItemStack.EMPTY);
         } else {
            pContainer.setItem(list1.remove(list1.size() - 1), itemstack);
         }
      }

   }

   /**
    * shuffles items by changing their order and splitting stacks
    */
   private void shuffleAndSplitItems(List<ItemStack> pStacks, int pEmptySlotsCount, Random pRand) {
      List<ItemStack> list = Lists.newArrayList();
      Iterator<ItemStack> iterator = pStacks.iterator();

      while(iterator.hasNext()) {
         ItemStack itemstack = iterator.next();
         if (itemstack.isEmpty()) {
            iterator.remove();
         } else if (itemstack.getCount() > 1) {
            list.add(itemstack);
            iterator.remove();
         }
      }

      while(pEmptySlotsCount - pStacks.size() - list.size() > 0 && !list.isEmpty()) {
         ItemStack itemstack2 = list.remove(MathHelper.nextInt(pRand, 0, list.size() - 1));
         int i = MathHelper.nextInt(pRand, 1, itemstack2.getCount() / 2);
         ItemStack itemstack1 = itemstack2.split(i);
         if (itemstack2.getCount() > 1 && pRand.nextBoolean()) {
            list.add(itemstack2);
         } else {
            pStacks.add(itemstack2);
         }

         if (itemstack1.getCount() > 1 && pRand.nextBoolean()) {
            list.add(itemstack1);
         } else {
            pStacks.add(itemstack1);
         }
      }

      pStacks.addAll(list);
      Collections.shuffle(pStacks, pRand);
   }

   private List<Integer> getAvailableSlots(IInventory pInventory, Random pRand) {
      List<Integer> list = Lists.newArrayList();

      for(int i = 0; i < pInventory.getContainerSize(); ++i) {
         if (pInventory.getItem(i).isEmpty()) {
            list.add(i);
         }
      }

      Collections.shuffle(list, pRand);
      return list;
   }

   public static LootTable.Builder lootTable() {
      return new LootTable.Builder();
   }

   //======================== FORGE START =============================================
   private boolean isFrozen = false;
   public void freeze() {
      this.isFrozen = true;
      this.pools.forEach(LootPool::freeze);
   }
   public boolean isFrozen(){ return this.isFrozen; }
   private void checkFrozen() {
      if (this.isFrozen())
         throw new RuntimeException("Attempted to modify LootTable after being finalized!");
   }

   private ResourceLocation lootTableId;
   public void setLootTableId(final ResourceLocation id) {
      if (this.lootTableId != null) throw new IllegalStateException("Attempted to rename loot table from '" + this.lootTableId + "' to '" + id + "': this is not supported");
      this.lootTableId = java.util.Objects.requireNonNull(id);
   }
   public ResourceLocation getLootTableId() { return this.lootTableId; }

   public LootPool getPool(String name) {
      return pools.stream().filter(e -> name.equals(e.getName())).findFirst().orElse(null);
   }

   public LootPool removePool(String name) {
      checkFrozen();
      for (LootPool pool : this.pools) {
         if (name.equals(pool.getName())) {
            this.pools.remove(pool);
            return pool;
         }
      }
      return null;
   }

   public void addPool(LootPool pool) {
      checkFrozen();
      if (pools.stream().anyMatch(e -> e == pool || e.getName() != null && e.getName().equals(pool.getName())))
         throw new RuntimeException("Attempted to add a duplicate pool to loot table: " + pool.getName());
      this.pools.add(pool);
   }
   //======================== FORGE END ===============================================

   public static class Builder implements ILootFunctionConsumer<LootTable.Builder> {
      private final List<LootPool> pools = Lists.newArrayList();
      private final List<ILootFunction> functions = Lists.newArrayList();
      private LootParameterSet paramSet = LootTable.DEFAULT_PARAM_SET;

      public LootTable.Builder withPool(LootPool.Builder pLootPool) {
         this.pools.add(pLootPool.build());
         return this;
      }

      public LootTable.Builder setParamSet(LootParameterSet pParameterSet) {
         this.paramSet = pParameterSet;
         return this;
      }

      public LootTable.Builder apply(ILootFunction.IBuilder pFunctionBuilder) {
         this.functions.add(pFunctionBuilder.build());
         return this;
      }

      public LootTable.Builder unwrap() {
         return this;
      }

      public LootTable build() {
         return new LootTable(this.paramSet, this.pools.toArray(new LootPool[0]), this.functions.toArray(new ILootFunction[0]));
      }
   }

   public static class Serializer implements JsonDeserializer<LootTable>, JsonSerializer<LootTable> {
      public LootTable deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(p_deserialize_1_, "loot table");
         LootPool[] alootpool = JSONUtils.getAsObject(jsonobject, "pools", new LootPool[0], p_deserialize_3_, LootPool[].class);
         LootParameterSet lootparameterset = null;
         if (jsonobject.has("type")) {
            String s = JSONUtils.getAsString(jsonobject, "type");
            lootparameterset = LootParameterSets.get(new ResourceLocation(s));
         }

         ILootFunction[] ailootfunction = JSONUtils.getAsObject(jsonobject, "functions", new ILootFunction[0], p_deserialize_3_, ILootFunction[].class);
         return new LootTable(lootparameterset != null ? lootparameterset : LootParameterSets.ALL_PARAMS, alootpool, ailootfunction);
      }

      public JsonElement serialize(LootTable p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
         JsonObject jsonobject = new JsonObject();
         if (p_serialize_1_.paramSet != LootTable.DEFAULT_PARAM_SET) {
            ResourceLocation resourcelocation = LootParameterSets.getKey(p_serialize_1_.paramSet);
            if (resourcelocation != null) {
               jsonobject.addProperty("type", resourcelocation.toString());
            } else {
               LootTable.LOGGER.warn("Failed to find id for param set " + p_serialize_1_.paramSet);
            }
         }

         if (!p_serialize_1_.pools.isEmpty()) {
            jsonobject.add("pools", p_serialize_3_.serialize(p_serialize_1_.pools));
         }

         if (!ArrayUtils.isEmpty((Object[])p_serialize_1_.functions)) {
            jsonobject.add("functions", p_serialize_3_.serialize(p_serialize_1_.functions));
         }

         return jsonobject;
      }
   }
}
