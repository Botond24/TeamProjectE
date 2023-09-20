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
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.LootConditionManager;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
   private final String name;
   private final List<LootEntry> entries;
   private final List<ILootCondition> conditions;
   private final Predicate<LootContext> compositeCondition;
   private final ILootFunction[] functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
   private IRandomRange rolls;
   private RandomValueRange bonusRolls;

   private LootPool(LootEntry[] p_i51268_1_, ILootCondition[] p_i51268_2_, ILootFunction[] p_i51268_3_, IRandomRange p_i51268_4_, RandomValueRange p_i51268_5_, String name) {
      this.name = name;
      this.entries = Lists.newArrayList(p_i51268_1_);
      this.conditions = Lists.newArrayList(p_i51268_2_);
      this.compositeCondition = LootConditionManager.andConditions(p_i51268_2_);
      this.functions = p_i51268_3_;
      this.compositeFunction = LootFunctionManager.compose(p_i51268_3_);
      this.rolls = p_i51268_4_;
      this.bonusRolls = p_i51268_5_;
   }

   private void addRandomItem(Consumer<ItemStack> p_216095_1_, LootContext p_216095_2_) {
      Random random = p_216095_2_.getRandom();
      List<ILootGenerator> list = Lists.newArrayList();
      MutableInt mutableint = new MutableInt();

      for(LootEntry lootentry : this.entries) {
         lootentry.expand(p_216095_2_, (p_216097_3_) -> {
            int k = p_216097_3_.getWeight(p_216095_2_.getLuck());
            if (k > 0) {
               list.add(p_216097_3_);
               mutableint.add(k);
            }

         });
      }

      int i = list.size();
      if (mutableint.intValue() != 0 && i != 0) {
         if (i == 1) {
            list.get(0).createItemStack(p_216095_1_, p_216095_2_);
         } else {
            int j = random.nextInt(mutableint.intValue());

            for(ILootGenerator ilootgenerator : list) {
               j -= ilootgenerator.getWeight(p_216095_2_.getLuck());
               if (j < 0) {
                  ilootgenerator.createItemStack(p_216095_1_, p_216095_2_);
                  return;
               }
            }

         }
      }
   }

   /**
    * Generate the random items from this LootPool to the given {@code stackConsumer}.
    * This first checks this pool's conditions, generating nothing if they do not match.
    * Then the random items are generated based on the {@link LootPoolEntry LootPoolEntries} in this pool according to
    * the rolls and bonusRools, applying any loot functions.
    */
   public void addRandomItems(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      if (this.compositeCondition.test(pLootContext)) {
         Consumer<ItemStack> consumer = ILootFunction.decorate(this.compositeFunction, pStackConsumer, pLootContext);
         Random random = pLootContext.getRandom();
         int i = this.rolls.getInt(random) + MathHelper.floor(this.bonusRolls.getFloat(random) * pLootContext.getLuck());

         for(int j = 0; j < i; ++j) {
            this.addRandomItem(consumer, pLootContext);
         }

      }
   }

   /**
    * Validate this LootPool according to the given context.
    */
   public void validate(ValidationTracker pContext) {
      for(int i = 0; i < this.conditions.size(); ++i) {
         this.conditions.get(i).validate(pContext.forChild(".condition[" + i + "]"));
      }

      for(int j = 0; j < this.functions.length; ++j) {
         this.functions[j].validate(pContext.forChild(".functions[" + j + "]"));
      }

      for(int k = 0; k < this.entries.size(); ++k) {
         this.entries.get(k).validate(pContext.forChild(".entries[" + k + "]"));
      }

   }
   //======================== FORGE START =============================================
   private boolean isFrozen = false;
   public void freeze() { this.isFrozen = true; }
   public boolean isFrozen(){ return this.isFrozen; }
   private void checkFrozen() {
      if (this.isFrozen())
         throw new RuntimeException("Attempted to modify LootPool after being frozen!");
   }
   public String getName(){ return this.name; }
   public IRandomRange getRolls()      { return this.rolls; }
   public IRandomRange getBonusRolls() { return this.bonusRolls; }
   public void setRolls     (RandomValueRange v){ checkFrozen(); this.rolls = v; }
   public void setBonusRolls(RandomValueRange v){ checkFrozen(); this.bonusRolls = v; }
   //======================== FORGE END ===============================================

   public static LootPool.Builder lootPool() {
      return new LootPool.Builder();
   }

   public static class Builder implements ILootFunctionConsumer<LootPool.Builder>, ILootConditionConsumer<LootPool.Builder> {
      private final List<LootEntry> entries = Lists.newArrayList();
      private final List<ILootCondition> conditions = Lists.newArrayList();
      private final List<ILootFunction> functions = Lists.newArrayList();
      private IRandomRange rolls = new RandomValueRange(1.0F);
      private RandomValueRange bonusRolls = new RandomValueRange(0.0F, 0.0F);
      private String name;

      public LootPool.Builder setRolls(IRandomRange p_216046_1_) {
         this.rolls = p_216046_1_;
         return this;
      }

      public LootPool.Builder unwrap() {
         return this;
      }

      public LootPool.Builder add(LootEntry.Builder<?> pEntriesBuilder) {
         this.entries.add(pEntriesBuilder.build());
         return this;
      }

      public LootPool.Builder when(ILootCondition.IBuilder pConditionBuilder) {
         this.conditions.add(pConditionBuilder.build());
         return this;
      }

      public LootPool.Builder apply(ILootFunction.IBuilder pFunctionBuilder) {
         this.functions.add(pFunctionBuilder.build());
         return this;
      }

      public LootPool.Builder name(String name) {
         this.name = name;
         return this;
      }

      public LootPool.Builder bonusRolls(float min, float max) {
         this.bonusRolls = new RandomValueRange(min, max);
         return this;
      }

      public LootPool build() {
         if (this.rolls == null) {
            throw new IllegalArgumentException("Rolls not set");
         } else {
            return new LootPool(this.entries.toArray(new LootEntry[0]), this.conditions.toArray(new ILootCondition[0]), this.functions.toArray(new ILootFunction[0]), this.rolls, this.bonusRolls, name);
         }
      }
   }

   public static class Serializer implements JsonDeserializer<LootPool>, JsonSerializer<LootPool> {
      public LootPool deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(p_deserialize_1_, "loot pool");
         LootEntry[] alootentry = JSONUtils.getAsObject(jsonobject, "entries", p_deserialize_3_, LootEntry[].class);
         ILootCondition[] ailootcondition = JSONUtils.getAsObject(jsonobject, "conditions", new ILootCondition[0], p_deserialize_3_, ILootCondition[].class);
         ILootFunction[] ailootfunction = JSONUtils.getAsObject(jsonobject, "functions", new ILootFunction[0], p_deserialize_3_, ILootFunction[].class);
         IRandomRange irandomrange = RandomRanges.deserialize(jsonobject.get("rolls"), p_deserialize_3_);
         RandomValueRange randomvaluerange = JSONUtils.getAsObject(jsonobject, "bonus_rolls", new RandomValueRange(0.0F, 0.0F), p_deserialize_3_, RandomValueRange.class);
         return new LootPool(alootentry, ailootcondition, ailootfunction, irandomrange, randomvaluerange, net.minecraftforge.common.ForgeHooks.readPoolName(jsonobject));
      }

      public JsonElement serialize(LootPool p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
         JsonObject jsonobject = new JsonObject();
         if (p_serialize_1_.name != null && !p_serialize_1_.name.startsWith("custom#"))
            jsonobject.add("name", p_serialize_3_.serialize(p_serialize_1_.name));
         jsonobject.add("rolls", RandomRanges.serialize(p_serialize_1_.rolls, p_serialize_3_));
         jsonobject.add("entries", p_serialize_3_.serialize(p_serialize_1_.entries));
         if (p_serialize_1_.bonusRolls.getMin() != 0.0F && p_serialize_1_.bonusRolls.getMax() != 0.0F) {
            jsonobject.add("bonus_rolls", p_serialize_3_.serialize(p_serialize_1_.bonusRolls));
         }

         if (!p_serialize_1_.conditions.isEmpty()) {
            jsonobject.add("conditions", p_serialize_3_.serialize(p_serialize_1_.conditions));
         }

         if (!ArrayUtils.isEmpty((Object[])p_serialize_1_.functions)) {
            jsonobject.add("functions", p_serialize_3_.serialize(p_serialize_1_.functions));
         }

         return jsonobject;
      }
   }
}