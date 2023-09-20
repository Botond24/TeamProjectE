package net.minecraft.state;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;

public class BooleanProperty extends Property<Boolean> {
   private final ImmutableSet<Boolean> values = ImmutableSet.of(true, false);

   protected BooleanProperty(String pName) {
      super(pName, Boolean.class);
   }

   public Collection<Boolean> getPossibleValues() {
      return this.values;
   }

   public static BooleanProperty create(String pName) {
      return new BooleanProperty(pName);
   }

   public Optional<Boolean> getValue(String pValue) {
      return !"true".equals(pValue) && !"false".equals(pValue) ? Optional.empty() : Optional.of(Boolean.valueOf(pValue));
   }

   /**
    * Get the name for the given value.
    */
   public String getName(Boolean pValue) {
      return pValue.toString();
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ instanceof BooleanProperty && super.equals(p_equals_1_)) {
         BooleanProperty booleanproperty = (BooleanProperty)p_equals_1_;
         return this.values.equals(booleanproperty.values);
      } else {
         return false;
      }
   }

   public int generateHashCode() {
      return 31 * super.generateHashCode() + this.values.hashCode();
   }
}