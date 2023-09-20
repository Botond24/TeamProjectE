package net.minecraft.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.potion.EffectInstance;

public class Food {
   private final int nutrition;
   private final float saturationModifier;
   private final boolean isMeat;
   private final boolean canAlwaysEat;
   private final boolean fastFood;
   private final List<Pair<java.util.function.Supplier<EffectInstance>, Float>> effects;

   private Food(Food.Builder builder) {
      this.nutrition = builder.nutrition;
      this.saturationModifier = builder.saturationModifier;
      this.isMeat = builder.isMeat;
      this.canAlwaysEat = builder.canAlwaysEat;
      this.fastFood = builder.fastFood;
      this.effects = builder.effects;
   }

   // Forge: Use builder method instead
   @Deprecated
   private Food(int pNutrition, float pSaturationModifier, boolean pIsMeat, boolean pCanAlwaysEat, boolean pFastFood, List<Pair<EffectInstance, Float>> pEffects) {
      this.nutrition = pNutrition;
      this.saturationModifier = pSaturationModifier;
      this.isMeat = pIsMeat;
      this.canAlwaysEat = pCanAlwaysEat;
      this.fastFood = pFastFood;
      this.effects = pEffects.stream().map(pair -> Pair.<java.util.function.Supplier<EffectInstance>, Float>of(pair::getFirst, pair.getSecond())).collect(java.util.stream.Collectors.toList());
   }

   public int getNutrition() {
      return this.nutrition;
   }

   public float getSaturationModifier() {
      return this.saturationModifier;
   }

   public boolean isMeat() {
      return this.isMeat;
   }

   public boolean canAlwaysEat() {
      return this.canAlwaysEat;
   }

   public boolean isFastFood() {
      return this.fastFood;
   }

   public List<Pair<EffectInstance, Float>> getEffects() {
      return this.effects.stream().map(pair -> Pair.of(pair.getFirst() != null ? pair.getFirst().get() : null, pair.getSecond())).collect(java.util.stream.Collectors.toList());
   }

   public static class Builder {
      private int nutrition;
      private float saturationModifier;
      private boolean isMeat;
      private boolean canAlwaysEat;
      private boolean fastFood;
      private final List<Pair<java.util.function.Supplier<EffectInstance>, Float>> effects = Lists.newArrayList();

      public Food.Builder nutrition(int pNutrition) {
         this.nutrition = pNutrition;
         return this;
      }

      public Food.Builder saturationMod(float pSaturationModifier) {
         this.saturationModifier = pSaturationModifier;
         return this;
      }

      public Food.Builder meat() {
         this.isMeat = true;
         return this;
      }

      public Food.Builder alwaysEat() {
         this.canAlwaysEat = true;
         return this;
      }

      public Food.Builder fast() {
         this.fastFood = true;
         return this;
      }

      public Food.Builder effect(java.util.function.Supplier<EffectInstance> effectIn, float probability) {
          this.effects.add(Pair.of(effectIn, probability));
          return this;
       }

      // Forge: Use supplier method instead
      @Deprecated
      public Food.Builder effect(EffectInstance pEffect, float pProbability) {
         this.effects.add(Pair.of(() -> pEffect, pProbability));
         return this;
      }

      public Food build() {
         return new Food(this);
      }
   }
}
