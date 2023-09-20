package net.minecraft.potion;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class Effect extends net.minecraftforge.registries.ForgeRegistryEntry<Effect> implements net.minecraftforge.common.extensions.IForgeEffect {
   /** Contains a Map of the AttributeModifiers registered by potions */
   private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
   private final EffectType category;
   private final int color;
   @Nullable
   private String descriptionId;

   /**
    * Gets a Potion from the potion registry using a numeric Id.
    */
   @Nullable
   public static Effect byId(int pPotionID) {
      return Registry.MOB_EFFECT.byId(pPotionID);
   }

   /**
    * Gets the numeric Id associated with a potion.
    */
   public static int getId(Effect pPotion) {
      return Registry.MOB_EFFECT.getId(pPotion);
   }

   protected Effect(EffectType pCategory, int pColor) {
      this.category = pCategory;
      this.color = pColor;
   }

   public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
      if (this == Effects.REGENERATION) {
         if (pLivingEntity.getHealth() < pLivingEntity.getMaxHealth()) {
            pLivingEntity.heal(1.0F);
         }
      } else if (this == Effects.POISON) {
         if (pLivingEntity.getHealth() > 1.0F) {
            pLivingEntity.hurt(DamageSource.MAGIC, 1.0F);
         }
      } else if (this == Effects.WITHER) {
         pLivingEntity.hurt(DamageSource.WITHER, 1.0F);
      } else if (this == Effects.HUNGER && pLivingEntity instanceof PlayerEntity) {
         ((PlayerEntity)pLivingEntity).causeFoodExhaustion(0.005F * (float)(pAmplifier + 1));
      } else if (this == Effects.SATURATION && pLivingEntity instanceof PlayerEntity) {
         if (!pLivingEntity.level.isClientSide) {
            ((PlayerEntity)pLivingEntity).getFoodData().eat(pAmplifier + 1, 1.0F);
         }
      } else if ((this != Effects.HEAL || pLivingEntity.isInvertedHealAndHarm()) && (this != Effects.HARM || !pLivingEntity.isInvertedHealAndHarm())) {
         if (this == Effects.HARM && !pLivingEntity.isInvertedHealAndHarm() || this == Effects.HEAL && pLivingEntity.isInvertedHealAndHarm()) {
            pLivingEntity.hurt(DamageSource.MAGIC, (float)(6 << pAmplifier));
         }
      } else {
         pLivingEntity.heal((float)Math.max(4 << pAmplifier, 0));
      }

   }

   public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
      if ((this != Effects.HEAL || pLivingEntity.isInvertedHealAndHarm()) && (this != Effects.HARM || !pLivingEntity.isInvertedHealAndHarm())) {
         if (this == Effects.HARM && !pLivingEntity.isInvertedHealAndHarm() || this == Effects.HEAL && pLivingEntity.isInvertedHealAndHarm()) {
            int j = (int)(pHealth * (double)(6 << pAmplifier) + 0.5D);
            if (pSource == null) {
               pLivingEntity.hurt(DamageSource.MAGIC, (float)j);
            } else {
               pLivingEntity.hurt(DamageSource.indirectMagic(pSource, pIndirectSource), (float)j);
            }
         } else {
            this.applyEffectTick(pLivingEntity, pAmplifier);
         }
      } else {
         int i = (int)(pHealth * (double)(4 << pAmplifier) + 0.5D);
         pLivingEntity.heal((float)i);
      }

   }

   /**
    * checks if Potion effect is ready to be applied this tick.
    */
   public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
      if (this == Effects.REGENERATION) {
         int k = 50 >> pAmplifier;
         if (k > 0) {
            return pDuration % k == 0;
         } else {
            return true;
         }
      } else if (this == Effects.POISON) {
         int j = 25 >> pAmplifier;
         if (j > 0) {
            return pDuration % j == 0;
         } else {
            return true;
         }
      } else if (this == Effects.WITHER) {
         int i = 40 >> pAmplifier;
         if (i > 0) {
            return pDuration % i == 0;
         } else {
            return true;
         }
      } else {
         return this == Effects.HUNGER;
      }
   }

   /**
    * Returns true if the potion has an instant effect instead of a continuous one (eg Harming)
    */
   public boolean isInstantenous() {
      return false;
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("effect", Registry.MOB_EFFECT.getKey(this));
      }

      return this.descriptionId;
   }

   /**
    * returns the name of the potion
    */
   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public ITextComponent getDisplayName() {
      return new TranslationTextComponent(this.getDescriptionId());
   }

   public EffectType getCategory() {
      return this.category;
   }

   /**
    * Returns the color of the potion liquid.
    */
   public int getColor() {
      return this.color;
   }

   /**
    * Adds an attribute modifier to this effect. This method can be called for more than one attribute. The attributes
    * are applied to an entity when the potion effect is active and removed when it stops.
    */
   public Effect addAttributeModifier(Attribute pAttribute, String pUuid, double pAmount, AttributeModifier.Operation pOperation) {
      AttributeModifier attributemodifier = new AttributeModifier(UUID.fromString(pUuid), this::getDescriptionId, pAmount, pOperation);
      this.attributeModifiers.put(pAttribute, attributemodifier);
      return this;
   }

   public Map<Attribute, AttributeModifier> getAttributeModifiers() {
      return this.attributeModifiers;
   }

   public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeModifierManager pAttributeMap, int pAmplifier) {
      for(Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
         ModifiableAttributeInstance modifiableattributeinstance = pAttributeMap.getInstance(entry.getKey());
         if (modifiableattributeinstance != null) {
            modifiableattributeinstance.removeModifier(entry.getValue());
         }
      }

   }

   public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeModifierManager pAttributeMap, int pAmplifier) {
      for(Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
         ModifiableAttributeInstance modifiableattributeinstance = pAttributeMap.getInstance(entry.getKey());
         if (modifiableattributeinstance != null) {
            AttributeModifier attributemodifier = entry.getValue();
            modifiableattributeinstance.removeModifier(attributemodifier);
            modifiableattributeinstance.addPermanentModifier(new AttributeModifier(attributemodifier.getId(), this.getDescriptionId() + " " + pAmplifier, this.getAttributeModifierValue(pAmplifier, attributemodifier), attributemodifier.getOperation()));
         }
      }

   }

   public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
      return pModifier.getAmount() * (double)(pAmplifier + 1);
   }

   /**
    * Get if the potion is beneficial to the player. Beneficial potions are shown on the first row of the HUD
    */
   public boolean isBeneficial() {
      return this.category == EffectType.BENEFICIAL;
   }
}
