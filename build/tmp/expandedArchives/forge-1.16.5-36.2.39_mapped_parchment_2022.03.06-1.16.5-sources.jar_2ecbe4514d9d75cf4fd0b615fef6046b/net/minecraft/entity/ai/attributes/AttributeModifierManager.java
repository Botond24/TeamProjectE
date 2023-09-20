package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AttributeModifierManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<Attribute, ModifiableAttributeInstance> attributes = Maps.newHashMap();
   private final Set<ModifiableAttributeInstance> dirtyAttributes = Sets.newHashSet();
   private final AttributeModifierMap supplier;

   public AttributeModifierManager(AttributeModifierMap p_i231502_1_) {
      this.supplier = p_i231502_1_;
   }

   private void onAttributeModified(ModifiableAttributeInstance p_233783_1_) {
      if (p_233783_1_.getAttribute().isClientSyncable()) {
         this.dirtyAttributes.add(p_233783_1_);
      }

   }

   public Set<ModifiableAttributeInstance> getDirtyAttributes() {
      return this.dirtyAttributes;
   }

   public Collection<ModifiableAttributeInstance> getSyncableAttributes() {
      return this.attributes.values().stream().filter((p_233796_0_) -> {
         return p_233796_0_.getAttribute().isClientSyncable();
      }).collect(Collectors.toList());
   }

   @Nullable
   public ModifiableAttributeInstance getInstance(Attribute pAttribute) {
      return this.attributes.computeIfAbsent(pAttribute, (p_233798_1_) -> {
         return this.supplier.createInstance(this::onAttributeModified, p_233798_1_);
      });
   }

   public boolean hasAttribute(Attribute pAttribute) {
      return this.attributes.get(pAttribute) != null || this.supplier.hasAttribute(pAttribute);
   }

   public boolean hasModifier(Attribute pAttribute, UUID pUuid) {
      ModifiableAttributeInstance modifiableattributeinstance = this.attributes.get(pAttribute);
      return modifiableattributeinstance != null ? modifiableattributeinstance.getModifier(pUuid) != null : this.supplier.hasModifier(pAttribute, pUuid);
   }

   public double getValue(Attribute pAttribute) {
      ModifiableAttributeInstance modifiableattributeinstance = this.attributes.get(pAttribute);
      return modifiableattributeinstance != null ? modifiableattributeinstance.getValue() : this.supplier.getValue(pAttribute);
   }

   public double getBaseValue(Attribute pAttribute) {
      ModifiableAttributeInstance modifiableattributeinstance = this.attributes.get(pAttribute);
      return modifiableattributeinstance != null ? modifiableattributeinstance.getBaseValue() : this.supplier.getBaseValue(pAttribute);
   }

   public double getModifierValue(Attribute pAttribute, UUID pUuid) {
      ModifiableAttributeInstance modifiableattributeinstance = this.attributes.get(pAttribute);
      return modifiableattributeinstance != null ? modifiableattributeinstance.getModifier(pUuid).getAmount() : this.supplier.getModifierValue(pAttribute, pUuid);
   }

   public void removeAttributeModifiers(Multimap<Attribute, AttributeModifier> pMap) {
      pMap.asMap().forEach((p_233781_1_, p_233781_2_) -> {
         ModifiableAttributeInstance modifiableattributeinstance = this.attributes.get(p_233781_1_);
         if (modifiableattributeinstance != null) {
            p_233781_2_.forEach(modifiableattributeinstance::removeModifier);
         }

      });
   }

   public void addTransientAttributeModifiers(Multimap<Attribute, AttributeModifier> pMap) {
      pMap.forEach((p_233780_1_, p_233780_2_) -> {
         ModifiableAttributeInstance modifiableattributeinstance = this.getInstance(p_233780_1_);
         if (modifiableattributeinstance != null) {
            modifiableattributeinstance.removeModifier(p_233780_2_);
            modifiableattributeinstance.addTransientModifier(p_233780_2_);
         }

      });
   }

   @OnlyIn(Dist.CLIENT)
   public void assignValues(AttributeModifierManager pManager) {
      pManager.attributes.values().forEach((p_233792_1_) -> {
         ModifiableAttributeInstance modifiableattributeinstance = this.getInstance(p_233792_1_.getAttribute());
         if (modifiableattributeinstance != null) {
            modifiableattributeinstance.replaceFrom(p_233792_1_);
         }

      });
   }

   public ListNBT save() {
      ListNBT listnbt = new ListNBT();

      for(ModifiableAttributeInstance modifiableattributeinstance : this.attributes.values()) {
         listnbt.add(modifiableattributeinstance.save());
      }

      return listnbt;
   }

   public void load(ListNBT pNbt) {
      for(int i = 0; i < pNbt.size(); ++i) {
         CompoundNBT compoundnbt = pNbt.getCompound(i);
         String s = compoundnbt.getString("Name");
         Util.ifElse(Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(s)), (p_233787_2_) -> {
            ModifiableAttributeInstance modifiableattributeinstance = this.getInstance(p_233787_2_);
            if (modifiableattributeinstance != null) {
               modifiableattributeinstance.load(compoundnbt);
            }

         }, () -> {
            LOGGER.warn("Ignoring unknown attribute '{}'", (Object)s);
         });
      }

   }
}