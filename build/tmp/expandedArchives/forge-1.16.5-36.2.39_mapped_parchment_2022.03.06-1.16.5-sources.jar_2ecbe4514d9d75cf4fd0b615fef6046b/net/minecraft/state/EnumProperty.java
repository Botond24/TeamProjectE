package net.minecraft.state;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.IStringSerializable;

public class EnumProperty<T extends Enum<T> & IStringSerializable> extends Property<T> {
   private final ImmutableSet<T> values;
   /** Map of names to Enum values */
   private final Map<String, T> names = Maps.newHashMap();

   protected EnumProperty(String pName, Class<T> pClazz, Collection<T> pValues) {
      super(pName, pClazz);
      this.values = ImmutableSet.copyOf(pValues);

      for(T t : pValues) {
         String s = t.getSerializedName();
         if (this.names.containsKey(s)) {
            throw new IllegalArgumentException("Multiple values have the same name '" + s + "'");
         }

         this.names.put(s, t);
      }

   }

   public Collection<T> getPossibleValues() {
      return this.values;
   }

   public Optional<T> getValue(String pValue) {
      return Optional.ofNullable(this.names.get(pValue));
   }

   /**
    * Get the name for the given value.
    */
   public String getName(T pValue) {
      return pValue.getSerializedName();
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ instanceof EnumProperty && super.equals(p_equals_1_)) {
         EnumProperty<?> enumproperty = (EnumProperty)p_equals_1_;
         return this.values.equals(enumproperty.values) && this.names.equals(enumproperty.names);
      } else {
         return false;
      }
   }

   public int generateHashCode() {
      int i = super.generateHashCode();
      i = 31 * i + this.values.hashCode();
      return 31 * i + this.names.hashCode();
   }

   /**
    * Create a new EnumProperty with all Enum constants of the given class.
    */
   public static <T extends Enum<T> & IStringSerializable> EnumProperty<T> create(String pName, Class<T> pClazz) {
      return create(pName, pClazz, Predicates.alwaysTrue());
   }

   /**
    * Create a new EnumProperty with all Enum constants of the given class that match the given Predicate.
    */
   public static <T extends Enum<T> & IStringSerializable> EnumProperty<T> create(String pName, Class<T> pClazz, Predicate<T> pFilter) {
      return create(pName, pClazz, Arrays.<T>stream(pClazz.getEnumConstants()).filter(pFilter).collect(Collectors.toList()));
   }

   /**
    * Create a new EnumProperty with the specified values
    */
   public static <T extends Enum<T> & IStringSerializable> EnumProperty<T> create(String pName, Class<T> pClazz, T... pValues) {
      return create(pName, pClazz, Lists.newArrayList(pValues));
   }

   /**
    * Create a new EnumProperty with the specified values
    */
   public static <T extends Enum<T> & IStringSerializable> EnumProperty<T> create(String pName, Class<T> pClazz, Collection<T> pValues) {
      return new EnumProperty<>(pName, pClazz, pValues);
   }
}