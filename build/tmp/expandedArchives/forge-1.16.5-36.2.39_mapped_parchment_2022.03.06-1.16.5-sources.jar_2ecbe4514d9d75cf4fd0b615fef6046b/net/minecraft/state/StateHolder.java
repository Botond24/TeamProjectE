package net.minecraft.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public abstract class StateHolder<O, S> {
   private static final Function<Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Entry<Property<?>, Comparable<?>>, String>() {
      public String apply(@Nullable Entry<Property<?>, Comparable<?>> p_apply_1_) {
         if (p_apply_1_ == null) {
            return "<NULL>";
         } else {
            Property<?> property = p_apply_1_.getKey();
            return property.getName() + "=" + this.getName(property, p_apply_1_.getValue());
         }
      }

      private <T extends Comparable<T>> String getName(Property<T> p_235905_1_, Comparable<?> p_235905_2_) {
         return p_235905_1_.getName((T)p_235905_2_);
      }
   };
   protected final O owner;
   private final ImmutableMap<Property<?>, Comparable<?>> values;
   private Table<Property<?>, Comparable<?>, S> neighbours;
   protected final MapCodec<S> propertiesCodec;

   protected StateHolder(O pOwner, ImmutableMap<Property<?>, Comparable<?>> pValues, MapCodec<S> pPropertiesCodec) {
      this.owner = pOwner;
      this.values = pValues;
      this.propertiesCodec = pPropertiesCodec;
   }

   public <T extends Comparable<T>> S cycle(Property<T> pProperty) {
      return this.setValue(pProperty, findNextInCollection(pProperty.getPossibleValues(), this.getValue(pProperty)));
   }

   protected static <T> T findNextInCollection(Collection<T> pCollection, T pValue) {
      Iterator<T> iterator = pCollection.iterator();

      while(iterator.hasNext()) {
         if (iterator.next().equals(pValue)) {
            if (iterator.hasNext()) {
               return iterator.next();
            }

            return pCollection.iterator().next();
         }
      }

      return iterator.next();
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append(this.owner);
      if (!this.getValues().isEmpty()) {
         stringbuilder.append('[');
         stringbuilder.append(this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
         stringbuilder.append(']');
      }

      return stringbuilder.toString();
   }

   /**
    * @return an unmodifiable collection of all possible properties.
    */
   public Collection<Property<?>> getProperties() {
      return Collections.unmodifiableCollection(this.values.keySet());
   }

   public <T extends Comparable<T>> boolean hasProperty(Property<T> pProperty) {
      return this.values.containsKey(pProperty);
   }

   /**
    * @return the value of the given Property for this state
    */
   public <T extends Comparable<T>> T getValue(Property<T> pProperty) {
      Comparable<?> comparable = this.values.get(pProperty);
      if (comparable == null) {
         throw new IllegalArgumentException("Cannot get property " + pProperty + " as it does not exist in " + this.owner);
      } else {
         return pProperty.getValueClass().cast(comparable);
      }
   }

   public <T extends Comparable<T>> Optional<T> getOptionalValue(Property<T> pProperty) {
      Comparable<?> comparable = this.values.get(pProperty);
      return comparable == null ? Optional.empty() : Optional.of(pProperty.getValueClass().cast(comparable));
   }

   public <T extends Comparable<T>, V extends T> S setValue(Property<T> pProperty, V pValue) {
      Comparable<?> comparable = this.values.get(pProperty);
      if (comparable == null) {
         throw new IllegalArgumentException("Cannot set property " + pProperty + " as it does not exist in " + this.owner);
      } else if (comparable == pValue) {
         return (S)this;
      } else {
         S s = this.neighbours.get(pProperty, pValue);
         if (s == null) {
            throw new IllegalArgumentException("Cannot set property " + pProperty + " to " + pValue + " on " + this.owner + ", it is not an allowed value");
         } else {
            return s;
         }
      }
   }

   public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> p_235899_1_) {
      if (this.neighbours != null) {
         throw new IllegalStateException();
      } else {
         Table<Property<?>, Comparable<?>, S> table = HashBasedTable.create();

         for(Entry<Property<?>, Comparable<?>> entry : this.values.entrySet()) {
            Property<?> property = entry.getKey();

            for(Comparable<?> comparable : property.getPossibleValues()) {
               if (comparable != entry.getValue()) {
                  table.put(property, comparable, p_235899_1_.get(this.makeNeighbourValues(property, comparable)));
               }
            }
         }

         this.neighbours = (Table<Property<?>, Comparable<?>, S>)(table.isEmpty() ? table : ArrayTable.create(table));
      }
   }

   private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> p_235902_1_, Comparable<?> p_235902_2_) {
      Map<Property<?>, Comparable<?>> map = Maps.newHashMap(this.values);
      map.put(p_235902_1_, p_235902_2_);
      return map;
   }

   public ImmutableMap<Property<?>, Comparable<?>> getValues() {
      return this.values;
   }

   protected static <O, S extends StateHolder<O, S>> Codec<S> codec(Codec<O> p_235897_0_, Function<O, S> p_235897_1_) {
      return p_235897_0_.dispatch("Name", (p_235895_0_) -> {
         return p_235895_0_.owner;
      }, (p_235900_1_) -> {
         S s = p_235897_1_.apply(p_235900_1_);
         return s.getValues().isEmpty() ? Codec.unit(s) : s.propertiesCodec.fieldOf("Properties").codec();
      });
   }
}