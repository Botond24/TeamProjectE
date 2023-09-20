package net.minecraft.item;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IArmorMaterial {
   int getDurabilityForSlot(EquipmentSlotType pSlot);

   int getDefenseForSlot(EquipmentSlotType pSlot);

   int getEnchantmentValue();

   SoundEvent getEquipSound();

   Ingredient getRepairIngredient();

   @OnlyIn(Dist.CLIENT)
   String getName();

   float getToughness();

   /**
    * Gets the percentage of knockback resistance provided by armor of the material.
    */
   float getKnockbackResistance();
}