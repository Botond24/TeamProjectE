package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * LootItemFunction that modifies the stack's count based on an enchantment level on the {@linkplain
 * LootContextParams#TOOL tool} using various formulas.
 */
public class ApplyBonus extends LootFunction {
   private static final Map<ResourceLocation, ApplyBonus.IFormulaDeserializer> FORMULAS = Maps.newHashMap();
   private final Enchantment enchantment;
   private final ApplyBonus.IFormula formula;

   private ApplyBonus(ILootCondition[] pConditions, Enchantment pEnchantment, ApplyBonus.IFormula pFormula) {
      super(pConditions);
      this.enchantment = pEnchantment;
      this.formula = pFormula;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.APPLY_BONUS;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.TOOL);
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      ItemStack itemstack = pContext.getParamOrNull(LootParameters.TOOL);
      if (itemstack != null) {
         int i = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemstack);
         int j = this.formula.calculateNewCount(pContext.getRandom(), pStack.getCount(), i);
         pStack.setCount(j);
      }

      return pStack;
   }

   public static LootFunction.Builder<?> addBonusBinomialDistributionCount(Enchantment pEnchantment, float pProbability, int pExtraRounds) {
      return simpleBuilder((p_215864_3_) -> {
         return new ApplyBonus(p_215864_3_, pEnchantment, new ApplyBonus.BinomialWithBonusCountFormula(pExtraRounds, pProbability));
      });
   }

   public static LootFunction.Builder<?> addOreBonusCount(Enchantment p_215869_0_) {
      return simpleBuilder((p_215866_1_) -> {
         return new ApplyBonus(p_215866_1_, p_215869_0_, new ApplyBonus.OreDropsFormula());
      });
   }

   public static LootFunction.Builder<?> addUniformBonusCount(Enchantment pEnchantment) {
      return simpleBuilder((p_215872_1_) -> {
         return new ApplyBonus(p_215872_1_, pEnchantment, new ApplyBonus.UniformBonusCountFormula(1));
      });
   }

   public static LootFunction.Builder<?> addUniformBonusCount(Enchantment pEnchantment, int pBonusMultiplier) {
      return simpleBuilder((p_215868_2_) -> {
         return new ApplyBonus(p_215868_2_, pEnchantment, new ApplyBonus.UniformBonusCountFormula(pBonusMultiplier));
      });
   }

   static {
      FORMULAS.put(ApplyBonus.BinomialWithBonusCountFormula.TYPE, ApplyBonus.BinomialWithBonusCountFormula::deserialize);
      FORMULAS.put(ApplyBonus.OreDropsFormula.TYPE, ApplyBonus.OreDropsFormula::deserialize);
      FORMULAS.put(ApplyBonus.UniformBonusCountFormula.TYPE, ApplyBonus.UniformBonusCountFormula::deserialize);
   }

   /**
    * Applies a bonus based on a binomial distribution with {@code n = enchantmentLevel + extraRounds} and {@code p =
    * probability}.
    */
   static final class BinomialWithBonusCountFormula implements ApplyBonus.IFormula {
      public static final ResourceLocation TYPE = new ResourceLocation("binomial_with_bonus_count");
      private final int extraRounds;
      private final float probability;

      public BinomialWithBonusCountFormula(int pExtraRounds, float pProbability) {
         this.extraRounds = pExtraRounds;
         this.probability = pProbability;
      }

      public int calculateNewCount(Random pRandom, int pOriginalCount, int pEnchantmentLevel) {
         for(int i = 0; i < pEnchantmentLevel + this.extraRounds; ++i) {
            if (pRandom.nextFloat() < this.probability) {
               ++pOriginalCount;
            }
         }

         return pOriginalCount;
      }

      public void serializeParams(JsonObject pJson, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("extra", this.extraRounds);
         pJson.addProperty("probability", this.probability);
      }

      public static ApplyBonus.IFormula deserialize(JsonObject pJson, JsonDeserializationContext pDeserializationContext) {
         int i = JSONUtils.getAsInt(pJson, "extra");
         float f = JSONUtils.getAsFloat(pJson, "probability");
         return new ApplyBonus.BinomialWithBonusCountFormula(i, f);
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }

   interface IFormula {
      int calculateNewCount(Random pRandom, int pOriginalCount, int pEnchantmentLevel);

      void serializeParams(JsonObject pJson, JsonSerializationContext pSerializationContext);

      ResourceLocation getType();
   }

   interface IFormulaDeserializer {
      ApplyBonus.IFormula deserialize(JsonObject p_deserialize_1_, JsonDeserializationContext p_deserialize_2_);
   }

   /**
    * Applies a bonus count with a special formula used for fortune ore drops.
    */
   static final class OreDropsFormula implements ApplyBonus.IFormula {
      public static final ResourceLocation TYPE = new ResourceLocation("ore_drops");

      private OreDropsFormula() {
      }

      public int calculateNewCount(Random pRandom, int pOriginalCount, int pEnchantmentLevel) {
         if (pEnchantmentLevel > 0) {
            int i = pRandom.nextInt(pEnchantmentLevel + 2) - 1;
            if (i < 0) {
               i = 0;
            }

            return pOriginalCount * (i + 1);
         } else {
            return pOriginalCount;
         }
      }

      public void serializeParams(JsonObject pJson, JsonSerializationContext pSerializationContext) {
      }

      public static ApplyBonus.IFormula deserialize(JsonObject pJson, JsonDeserializationContext pDeserializationContext) {
         return new ApplyBonus.OreDropsFormula();
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }

   public static class Serializer extends LootFunction.Serializer<ApplyBonus> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, ApplyBonus pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.addProperty("enchantment", Registry.ENCHANTMENT.getKey(pValue.enchantment).toString());
         pJson.addProperty("formula", pValue.formula.getType().toString());
         JsonObject jsonobject = new JsonObject();
         pValue.formula.serializeParams(jsonobject, pSerializationContext);
         if (jsonobject.size() > 0) {
            pJson.add("parameters", jsonobject);
         }

      }

      public ApplyBonus deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pObject, "enchantment"));
         Enchantment enchantment = Registry.ENCHANTMENT.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonParseException("Invalid enchantment id: " + resourcelocation);
         });
         ResourceLocation resourcelocation1 = new ResourceLocation(JSONUtils.getAsString(pObject, "formula"));
         ApplyBonus.IFormulaDeserializer applybonus$iformuladeserializer = ApplyBonus.FORMULAS.get(resourcelocation1);
         if (applybonus$iformuladeserializer == null) {
            throw new JsonParseException("Invalid formula id: " + resourcelocation1);
         } else {
            ApplyBonus.IFormula applybonus$iformula;
            if (pObject.has("parameters")) {
               applybonus$iformula = applybonus$iformuladeserializer.deserialize(JSONUtils.getAsJsonObject(pObject, "parameters"), pDeserializationContext);
            } else {
               applybonus$iformula = applybonus$iformuladeserializer.deserialize(new JsonObject(), pDeserializationContext);
            }

            return new ApplyBonus(pConditions, enchantment, applybonus$iformula);
         }
      }
   }

   /**
    * Adds a bonus count based on the enchantment level scaled by a constant multiplier.
    */
   static final class UniformBonusCountFormula implements ApplyBonus.IFormula {
      public static final ResourceLocation TYPE = new ResourceLocation("uniform_bonus_count");
      private final int bonusMultiplier;

      public UniformBonusCountFormula(int pBonusMultiplier) {
         this.bonusMultiplier = pBonusMultiplier;
      }

      public int calculateNewCount(Random pRandom, int pOriginalCount, int pEnchantmentLevel) {
         return pOriginalCount + pRandom.nextInt(this.bonusMultiplier * pEnchantmentLevel + 1);
      }

      public void serializeParams(JsonObject pJson, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("bonusMultiplier", this.bonusMultiplier);
      }

      public static ApplyBonus.IFormula deserialize(JsonObject pJson, JsonDeserializationContext pDeserializationContext) {
         int i = JSONUtils.getAsInt(pJson, "bonusMultiplier");
         return new ApplyBonus.UniformBonusCountFormula(i);
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }
}