package net.minecraft.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;

public interface IMultiPartPredicateBuilder extends Supplier<JsonElement> {
   void validate(StateContainer<?, ?> pStateDefinition);

   static IMultiPartPredicateBuilder.Properties condition() {
      return new IMultiPartPredicateBuilder.Properties();
   }

   static IMultiPartPredicateBuilder or(IMultiPartPredicateBuilder... pConditions) {
      return new IMultiPartPredicateBuilder.Serializer(IMultiPartPredicateBuilder.Operator.OR, Arrays.asList(pConditions));
   }

   public static enum Operator {
      AND("AND"),
      OR("OR");

      private final String id;

      private Operator(String pId) {
         this.id = pId;
      }
   }

   public static class Properties implements IMultiPartPredicateBuilder {
      private final Map<Property<?>, String> terms = Maps.newHashMap();

      private static <T extends Comparable<T>> String joinValues(Property<T> pProperty, Stream<T> pValueStream) {
         return pValueStream.map(pProperty::getName).collect(Collectors.joining("|"));
      }

      private static <T extends Comparable<T>> String getTerm(Property<T> pProperty, T pFirstValue, T[] pAdditionalValues) {
         return joinValues(pProperty, Stream.concat(Stream.of(pFirstValue), Stream.of(pAdditionalValues)));
      }

      private <T extends Comparable<T>> void putValue(Property<T> pProperty, String pValue) {
         String s = this.terms.put(pProperty, pValue);
         if (s != null) {
            throw new IllegalStateException("Tried to replace " + pProperty + " value from " + s + " to " + pValue);
         }
      }

      public final <T extends Comparable<T>> IMultiPartPredicateBuilder.Properties term(Property<T> pProperty, T pValue) {
         this.putValue(pProperty, pProperty.getName(pValue));
         return this;
      }

      @SafeVarargs
      public final <T extends Comparable<T>> IMultiPartPredicateBuilder.Properties term(Property<T> pProperty, T pFirstValue, T... pAdditionalValues) {
         this.putValue(pProperty, getTerm(pProperty, pFirstValue, pAdditionalValues));
         return this;
      }

      public JsonElement get() {
         JsonObject jsonobject = new JsonObject();
         this.terms.forEach((p_240102_1_, p_240102_2_) -> {
            jsonobject.addProperty(p_240102_1_.getName(), p_240102_2_);
         });
         return jsonobject;
      }

      public void validate(StateContainer<?, ?> pStateDefinition) {
         List<Property<?>> list = this.terms.keySet().stream().filter((p_240097_1_) -> {
            return pStateDefinition.getProperty(p_240097_1_.getName()) != p_240097_1_;
         }).collect(Collectors.toList());
         if (!list.isEmpty()) {
            throw new IllegalStateException("Properties " + list + " are missing from " + pStateDefinition);
         }
      }
   }

   public static class Serializer implements IMultiPartPredicateBuilder {
      private final IMultiPartPredicateBuilder.Operator operation;
      private final List<IMultiPartPredicateBuilder> subconditions;

      private Serializer(IMultiPartPredicateBuilder.Operator pOperation, List<IMultiPartPredicateBuilder> pSubconditions) {
         this.operation = pOperation;
         this.subconditions = pSubconditions;
      }

      public void validate(StateContainer<?, ?> pStateDefinition) {
         this.subconditions.forEach((p_240093_1_) -> {
            p_240093_1_.validate(pStateDefinition);
         });
      }

      public JsonElement get() {
         JsonArray jsonarray = new JsonArray();
         this.subconditions.stream().map(Supplier::get).forEach(jsonarray::add);
         JsonObject jsonobject = new JsonObject();
         jsonobject.add(this.operation.id, jsonarray);
         return jsonobject;
      }
   }
}