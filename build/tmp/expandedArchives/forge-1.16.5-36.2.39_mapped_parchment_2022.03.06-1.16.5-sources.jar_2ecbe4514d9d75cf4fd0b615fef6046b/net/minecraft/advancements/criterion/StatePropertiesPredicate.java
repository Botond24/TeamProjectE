package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.JSONUtils;

public class StatePropertiesPredicate {
   public static final StatePropertiesPredicate ANY = new StatePropertiesPredicate(ImmutableList.of());
   private final List<StatePropertiesPredicate.Matcher> properties;

   private static StatePropertiesPredicate.Matcher fromJson(String pName, JsonElement pElement) {
      if (pElement.isJsonPrimitive()) {
         String s2 = pElement.getAsString();
         return new StatePropertiesPredicate.ExactMatcher(pName, s2);
      } else {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "value");
         String s = jsonobject.has("min") ? getStringOrNull(jsonobject.get("min")) : null;
         String s1 = jsonobject.has("max") ? getStringOrNull(jsonobject.get("max")) : null;
         return (StatePropertiesPredicate.Matcher)(s != null && s.equals(s1) ? new StatePropertiesPredicate.ExactMatcher(pName, s) : new StatePropertiesPredicate.RangedMacher(pName, s, s1));
      }
   }

   @Nullable
   private static String getStringOrNull(JsonElement pElement) {
      return pElement.isJsonNull() ? null : pElement.getAsString();
   }

   private StatePropertiesPredicate(List<StatePropertiesPredicate.Matcher> p_i225790_1_) {
      this.properties = ImmutableList.copyOf(p_i225790_1_);
   }

   public <S extends StateHolder<?, S>> boolean matches(StateContainer<?, S> pProperties, S pTargetProperty) {
      for(StatePropertiesPredicate.Matcher statepropertiespredicate$matcher : this.properties) {
         if (!statepropertiespredicate$matcher.match(pProperties, pTargetProperty)) {
            return false;
         }
      }

      return true;
   }

   public boolean matches(BlockState pState) {
      return this.matches(pState.getBlock().getStateDefinition(), pState);
   }

   public boolean matches(FluidState pState) {
      return this.matches(pState.getType().getStateDefinition(), pState);
   }

   public void checkState(StateContainer<?, ?> pProperties, Consumer<String> pStringConsumer) {
      this.properties.forEach((p_227184_2_) -> {
         p_227184_2_.checkState(pProperties, pStringConsumer);
      });
   }

   public static StatePropertiesPredicate fromJson(@Nullable JsonElement pElement) {
      if (pElement != null && !pElement.isJsonNull()) {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "properties");
         List<StatePropertiesPredicate.Matcher> list = Lists.newArrayList();

         for(Entry<String, JsonElement> entry : jsonobject.entrySet()) {
            list.add(fromJson(entry.getKey(), entry.getValue()));
         }

         return new StatePropertiesPredicate(list);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.properties.isEmpty()) {
            this.properties.forEach((p_227187_1_) -> {
               jsonobject.add(p_227187_1_.getName(), p_227187_1_.toJson());
            });
         }

         return jsonobject;
      }
   }

   public static class Builder {
      private final List<StatePropertiesPredicate.Matcher> matchers = Lists.newArrayList();

      private Builder() {
      }

      public static StatePropertiesPredicate.Builder properties() {
         return new StatePropertiesPredicate.Builder();
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<?> pProperty, String pValue) {
         this.matchers.add(new StatePropertiesPredicate.ExactMatcher(pProperty.getName(), pValue));
         return this;
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Integer> pIntProp, int pValue) {
         return this.hasProperty(pIntProp, Integer.toString(pValue));
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> pBoolProp, boolean pValue) {
         return this.hasProperty(pBoolProp, Boolean.toString(pValue));
      }

      public <T extends Comparable<T> & IStringSerializable> StatePropertiesPredicate.Builder hasProperty(Property<T> pProp, T pValue) {
         return this.hasProperty(pProp, pValue.getSerializedName());
      }

      public StatePropertiesPredicate build() {
         return new StatePropertiesPredicate(this.matchers);
      }
   }

   static class ExactMatcher extends StatePropertiesPredicate.Matcher {
      private final String value;

      public ExactMatcher(String p_i225792_1_, String p_i225792_2_) {
         super(p_i225792_1_);
         this.value = p_i225792_2_;
      }

      protected <T extends Comparable<T>> boolean match(StateHolder<?, ?> pProperties, Property<T> pPropertyTarget) {
         T t = pProperties.getValue(pPropertyTarget);
         Optional<T> optional = pPropertyTarget.getValue(this.value);
         return optional.isPresent() && t.compareTo(optional.get()) == 0;
      }

      public JsonElement toJson() {
         return new JsonPrimitive(this.value);
      }
   }

   abstract static class Matcher {
      private final String name;

      public Matcher(String p_i225793_1_) {
         this.name = p_i225793_1_;
      }

      public <S extends StateHolder<?, S>> boolean match(StateContainer<?, S> pProperties, S pPropertyToMatch) {
         Property<?> property = pProperties.getProperty(this.name);
         return property == null ? false : this.match(pPropertyToMatch, property);
      }

      protected abstract <T extends Comparable<T>> boolean match(StateHolder<?, ?> pProperties, Property<T> pPropertyTarget);

      public abstract JsonElement toJson();

      public String getName() {
         return this.name;
      }

      public void checkState(StateContainer<?, ?> pProperties, Consumer<String> pPropertyConsumer) {
         Property<?> property = pProperties.getProperty(this.name);
         if (property == null) {
            pPropertyConsumer.accept(this.name);
         }

      }
   }

   static class RangedMacher extends StatePropertiesPredicate.Matcher {
      @Nullable
      private final String minValue;
      @Nullable
      private final String maxValue;

      public RangedMacher(String p_i225794_1_, @Nullable String p_i225794_2_, @Nullable String p_i225794_3_) {
         super(p_i225794_1_);
         this.minValue = p_i225794_2_;
         this.maxValue = p_i225794_3_;
      }

      protected <T extends Comparable<T>> boolean match(StateHolder<?, ?> pProperties, Property<T> pPropertyTarget) {
         T t = pProperties.getValue(pPropertyTarget);
         if (this.minValue != null) {
            Optional<T> optional = pPropertyTarget.getValue(this.minValue);
            if (!optional.isPresent() || t.compareTo(optional.get()) < 0) {
               return false;
            }
         }

         if (this.maxValue != null) {
            Optional<T> optional1 = pPropertyTarget.getValue(this.maxValue);
            if (!optional1.isPresent() || t.compareTo(optional1.get()) > 0) {
               return false;
            }
         }

         return true;
      }

      public JsonElement toJson() {
         JsonObject jsonobject = new JsonObject();
         if (this.minValue != null) {
            jsonobject.addProperty("min", this.minValue);
         }

         if (this.maxValue != null) {
            jsonobject.addProperty("max", this.maxValue);
         }

         return jsonobject;
      }
   }
}