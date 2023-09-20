package net.minecraft.data;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.state.Property;

public final class VariantPropertyBuilder {
   private static final VariantPropertyBuilder EMPTY = new VariantPropertyBuilder(ImmutableList.of());
   private static final Comparator<Property.ValuePair<?>> COMPARE_BY_NAME = Comparator.comparing((p_240192_0_) -> {
      return p_240192_0_.getProperty().getName();
   });
   private final List<Property.ValuePair<?>> values;

   public VariantPropertyBuilder extend(Property.ValuePair<?> pValue) {
      return new VariantPropertyBuilder(ImmutableList.<Property.ValuePair<?>>builder().addAll(this.values).add(pValue).build());
   }

   public VariantPropertyBuilder extend(VariantPropertyBuilder pSelector) {
      return new VariantPropertyBuilder(ImmutableList.<Property.ValuePair<?>>builder().addAll(this.values).addAll(pSelector.values).build());
   }

   private VariantPropertyBuilder(List<Property.ValuePair<?>> pValues) {
      this.values = pValues;
   }

   public static VariantPropertyBuilder empty() {
      return EMPTY;
   }

   public static VariantPropertyBuilder of(Property.ValuePair<?>... pValues) {
      return new VariantPropertyBuilder(ImmutableList.copyOf(pValues));
   }

   public boolean equals(Object p_equals_1_) {
      return this == p_equals_1_ || p_equals_1_ instanceof VariantPropertyBuilder && this.values.equals(((VariantPropertyBuilder)p_equals_1_).values);
   }

   public int hashCode() {
      return this.values.hashCode();
   }

   public String getKey() {
      return this.values.stream().sorted(COMPARE_BY_NAME).map(Property.ValuePair::toString).collect(Collectors.joining(","));
   }

   public String toString() {
      return this.getKey();
   }
}