package net.minecraft.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.registry.Registry;

public class AttributeModifierMap {
   private final Map<Attribute, ModifiableAttributeInstance> instances;

   public AttributeModifierMap(Map<Attribute, ModifiableAttributeInstance> p_i231503_1_) {
      this.instances = ImmutableMap.copyOf(p_i231503_1_);
   }

   private ModifiableAttributeInstance getAttributeInstance(Attribute pAttribute) {
      ModifiableAttributeInstance modifiableattributeinstance = this.instances.get(pAttribute);
      if (modifiableattributeinstance == null) {
         throw new IllegalArgumentException("Can't find attribute " + Registry.ATTRIBUTE.getKey(pAttribute));
      } else {
         return modifiableattributeinstance;
      }
   }

   public double getValue(Attribute pAttribute) {
      return this.getAttributeInstance(pAttribute).getValue();
   }

   public double getBaseValue(Attribute pAttribute) {
      return this.getAttributeInstance(pAttribute).getBaseValue();
   }

   public double getModifierValue(Attribute pAttribute, UUID pId) {
      AttributeModifier attributemodifier = this.getAttributeInstance(pAttribute).getModifier(pId);
      if (attributemodifier == null) {
         throw new IllegalArgumentException("Can't find modifier " + pId + " on attribute " + Registry.ATTRIBUTE.getKey(pAttribute));
      } else {
         return attributemodifier.getAmount();
      }
   }

   @Nullable
   public ModifiableAttributeInstance createInstance(Consumer<ModifiableAttributeInstance> pOnChangedCallback, Attribute pAttribute) {
      ModifiableAttributeInstance modifiableattributeinstance = this.instances.get(pAttribute);
      if (modifiableattributeinstance == null) {
         return null;
      } else {
         ModifiableAttributeInstance modifiableattributeinstance1 = new ModifiableAttributeInstance(pAttribute, pOnChangedCallback);
         modifiableattributeinstance1.replaceFrom(modifiableattributeinstance);
         return modifiableattributeinstance1;
      }
   }

   public static AttributeModifierMap.MutableAttribute builder() {
      return new AttributeModifierMap.MutableAttribute();
   }

   public boolean hasAttribute(Attribute pAttribute) {
      return this.instances.containsKey(pAttribute);
   }

   public boolean hasModifier(Attribute pAttribute, UUID pId) {
      ModifiableAttributeInstance modifiableattributeinstance = this.instances.get(pAttribute);
      return modifiableattributeinstance != null && modifiableattributeinstance.getModifier(pId) != null;
   }

   public static class MutableAttribute {
      private final Map<Attribute, ModifiableAttributeInstance> builder = Maps.newHashMap();
      private boolean instanceFrozen;
      private final java.util.List<AttributeModifierMap.MutableAttribute> others = new java.util.ArrayList<>();

      public MutableAttribute() { }

      public MutableAttribute(AttributeModifierMap attributeMap) {
         this.builder.putAll(attributeMap.instances);
      }

      public void combine(MutableAttribute other) {
         this.builder.putAll(other.builder);
         others.add(other);
      }

      public boolean hasAttribute(Attribute attribute) {
         return this.builder.containsKey(attribute);
      }

      private ModifiableAttributeInstance create(Attribute pAttribute) {
         ModifiableAttributeInstance modifiableattributeinstance = new ModifiableAttributeInstance(pAttribute, (p_233816_2_) -> {
            if (this.instanceFrozen) {
               throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + Registry.ATTRIBUTE.getKey(pAttribute));
            }
         });
         this.builder.put(pAttribute, modifiableattributeinstance);
         return modifiableattributeinstance;
      }

      public AttributeModifierMap.MutableAttribute add(Attribute pAttribute) {
         this.create(pAttribute);
         return this;
      }

      public AttributeModifierMap.MutableAttribute add(Attribute pAttribute, double pValue) {
         ModifiableAttributeInstance modifiableattributeinstance = this.create(pAttribute);
         modifiableattributeinstance.setBaseValue(pValue);
         return this;
      }

      public AttributeModifierMap build() {
         this.instanceFrozen = true;
         others.forEach(o -> o.instanceFrozen = true);
         return new AttributeModifierMap(this.builder);
      }
   }
}
