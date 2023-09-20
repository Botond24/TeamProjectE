package net.minecraft.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.state.Property;

public abstract class BlockStateVariantBuilder {
   private final Map<VariantPropertyBuilder, List<BlockModelDefinition>> values = Maps.newHashMap();

   protected void putValue(VariantPropertyBuilder pSelector, List<BlockModelDefinition> pValues) {
      List<BlockModelDefinition> list = this.values.put(pSelector, pValues);
      if (list != null) {
         throw new IllegalStateException("Value " + pSelector + " is already defined");
      }
   }

   Map<VariantPropertyBuilder, List<BlockModelDefinition>> getEntries() {
      this.verifyComplete();
      return ImmutableMap.copyOf(this.values);
   }

   private void verifyComplete() {
      List<Property<?>> list = this.getDefinedProperties();
      Stream<VariantPropertyBuilder> stream = Stream.of(VariantPropertyBuilder.empty());

      for(Property<?> property : list) {
         stream = stream.flatMap((p_240138_1_) -> {
            return property.getAllValues().map(p_240138_1_::extend);
         });
      }

      List<VariantPropertyBuilder> list1 = stream.filter((p_240139_1_) -> {
         return !this.values.containsKey(p_240139_1_);
      }).collect(Collectors.toList());
      if (!list1.isEmpty()) {
         throw new IllegalStateException("Missing definition for properties: " + list1);
      }
   }

   abstract List<Property<?>> getDefinedProperties();

   public static <T1 extends Comparable<T1>> BlockStateVariantBuilder.One<T1> property(Property<T1> pProperty1) {
      return new BlockStateVariantBuilder.One<>(pProperty1);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> BlockStateVariantBuilder.Two<T1, T2> properties(Property<T1> pProperty1, Property<T2> pProperty2) {
      return new BlockStateVariantBuilder.Two<>(pProperty1, pProperty2);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> BlockStateVariantBuilder.Three<T1, T2, T3> properties(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3) {
      return new BlockStateVariantBuilder.Three<>(pProperty1, pProperty2, pProperty3);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> BlockStateVariantBuilder.Four<T1, T2, T3, T4> properties(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3, Property<T4> pProperty4) {
      return new BlockStateVariantBuilder.Four<>(pProperty1, pProperty2, pProperty3, pProperty4);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> BlockStateVariantBuilder.Five<T1, T2, T3, T4, T5> properties(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3, Property<T4> pProperty4, Property<T5> pProperty5) {
      return new BlockStateVariantBuilder.Five<>(pProperty1, pProperty2, pProperty3, pProperty4, pProperty5);
   }

   public static class Five<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> extends BlockStateVariantBuilder {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;
      private final Property<T4> property4;
      private final Property<T5> property5;

      private Five(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3, Property<T4> pProperty4, Property<T5> pProperty5) {
         this.property1 = pProperty1;
         this.property2 = pProperty2;
         this.property3 = pProperty3;
         this.property4 = pProperty4;
         this.property5 = pProperty5;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3, this.property4, this.property5);
      }

      public BlockStateVariantBuilder.Five<T1, T2, T3, T4, T5> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, T4 pProperty4Value, T5 pProperty5Value, List<BlockModelDefinition> pVariants) {
         VariantPropertyBuilder variantpropertybuilder = VariantPropertyBuilder.of(this.property1.value(pProperty1Value), this.property2.value(pProperty2Value), this.property3.value(pProperty3Value), this.property4.value(pProperty4Value), this.property5.value(pProperty5Value));
         this.putValue(variantpropertybuilder, pVariants);
         return this;
      }

      public BlockStateVariantBuilder.Five<T1, T2, T3, T4, T5> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, T4 pProperty4Value, T5 pProperty5Value, BlockModelDefinition pVariant) {
         return this.select(pProperty1Value, pProperty2Value, pProperty3Value, pProperty4Value, pProperty5Value, Collections.singletonList(pVariant));
      }
   }

   public static class Four<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> extends BlockStateVariantBuilder {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;
      private final Property<T4> property4;

      private Four(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3, Property<T4> pProperty4) {
         this.property1 = pProperty1;
         this.property2 = pProperty2;
         this.property3 = pProperty3;
         this.property4 = pProperty4;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3, this.property4);
      }

      public BlockStateVariantBuilder.Four<T1, T2, T3, T4> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, T4 pProperty4Value, List<BlockModelDefinition> pVariants) {
         VariantPropertyBuilder variantpropertybuilder = VariantPropertyBuilder.of(this.property1.value(pProperty1Value), this.property2.value(pProperty2Value), this.property3.value(pProperty3Value), this.property4.value(pProperty4Value));
         this.putValue(variantpropertybuilder, pVariants);
         return this;
      }

      public BlockStateVariantBuilder.Four<T1, T2, T3, T4> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, T4 pProperty4Value, BlockModelDefinition pVariant) {
         return this.select(pProperty1Value, pProperty2Value, pProperty3Value, pProperty4Value, Collections.singletonList(pVariant));
      }
   }

   @FunctionalInterface
   public interface ITriFunction<P1, P2, P3, R> {
      R apply(P1 p_apply_1_, P2 p_apply_2_, P3 p_apply_3_);
   }

   public static class One<T1 extends Comparable<T1>> extends BlockStateVariantBuilder {
      private final Property<T1> property1;

      private One(Property<T1> pProperty1) {
         this.property1 = pProperty1;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1);
      }

      public BlockStateVariantBuilder.One<T1> select(T1 pPropertyValue, List<BlockModelDefinition> pVariants) {
         VariantPropertyBuilder variantpropertybuilder = VariantPropertyBuilder.of(this.property1.value(pPropertyValue));
         this.putValue(variantpropertybuilder, pVariants);
         return this;
      }

      public BlockStateVariantBuilder.One<T1> select(T1 pPropertyValue, BlockModelDefinition pVariant) {
         return this.select(pPropertyValue, Collections.singletonList(pVariant));
      }

      public BlockStateVariantBuilder generate(Function<T1, BlockModelDefinition> pPropertyValueToVariantMapper) {
         this.property1.getPossibleValues().forEach((p_240146_2_) -> {
            this.select(p_240146_2_, pPropertyValueToVariantMapper.apply(p_240146_2_));
         });
         return this;
      }
   }

   public static class Three<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> extends BlockStateVariantBuilder {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;

      private Three(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3) {
         this.property1 = pProperty1;
         this.property2 = pProperty2;
         this.property3 = pProperty3;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3);
      }

      public BlockStateVariantBuilder.Three<T1, T2, T3> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, List<BlockModelDefinition> pVariants) {
         VariantPropertyBuilder variantpropertybuilder = VariantPropertyBuilder.of(this.property1.value(pProperty1Value), this.property2.value(pProperty2Value), this.property3.value(pProperty3Value));
         this.putValue(variantpropertybuilder, pVariants);
         return this;
      }

      public BlockStateVariantBuilder.Three<T1, T2, T3> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, BlockModelDefinition pVariant) {
         return this.select(pProperty1Value, pProperty2Value, pProperty3Value, Collections.singletonList(pVariant));
      }

      public BlockStateVariantBuilder generate(BlockStateVariantBuilder.ITriFunction<T1, T2, T3, BlockModelDefinition> pPropertyValuesToVariantMapper) {
         this.property1.getPossibleValues().forEach((p_240163_2_) -> {
            this.property2.getPossibleValues().forEach((p_240164_3_) -> {
               this.property3.getPossibleValues().forEach((p_240165_4_) -> {
                  this.select((T1)p_240163_2_, (T2)p_240164_3_, p_240165_4_, pPropertyValuesToVariantMapper.apply((T1)p_240163_2_, (T2)p_240164_3_, p_240165_4_));
               });
            });
         });
         return this;
      }
   }

   public static class Two<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends BlockStateVariantBuilder {
      private final Property<T1> property1;
      private final Property<T2> property2;

      private Two(Property<T1> pProperty1, Property<T2> pProperty2) {
         this.property1 = pProperty1;
         this.property2 = pProperty2;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2);
      }

      public BlockStateVariantBuilder.Two<T1, T2> select(T1 pProperty1Value, T2 pProperty2Value, List<BlockModelDefinition> pVariants) {
         VariantPropertyBuilder variantpropertybuilder = VariantPropertyBuilder.of(this.property1.value(pProperty1Value), this.property2.value(pProperty2Value));
         this.putValue(variantpropertybuilder, pVariants);
         return this;
      }

      public BlockStateVariantBuilder.Two<T1, T2> select(T1 pProperty1Value, T2 pProperty2Value, BlockModelDefinition pVariant) {
         return this.select(pProperty1Value, pProperty2Value, Collections.singletonList(pVariant));
      }

      public BlockStateVariantBuilder generate(BiFunction<T1, T2, BlockModelDefinition> pPropertyValuesToVariantMapper) {
         this.property1.getPossibleValues().forEach((p_240156_2_) -> {
            this.property2.getPossibleValues().forEach((p_240154_3_) -> {
               this.select((T1)p_240156_2_, p_240154_3_, pPropertyValuesToVariantMapper.apply((T1)p_240156_2_, p_240154_3_));
            });
         });
         return this;
      }

      public BlockStateVariantBuilder generateList(BiFunction<T1, T2, List<BlockModelDefinition>> pPropertyValuesToVariantsMapper) {
         this.property1.getPossibleValues().forEach((p_240153_2_) -> {
            this.property2.getPossibleValues().forEach((p_240151_3_) -> {
               this.select((T1)p_240153_2_, p_240151_3_, pPropertyValuesToVariantsMapper.apply((T1)p_240153_2_, p_240151_3_));
            });
         });
         return this;
      }
   }
}