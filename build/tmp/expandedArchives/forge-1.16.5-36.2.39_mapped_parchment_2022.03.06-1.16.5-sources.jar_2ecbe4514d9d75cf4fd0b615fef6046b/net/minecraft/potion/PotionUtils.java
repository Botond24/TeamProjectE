package net.minecraft.potion;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionUtils {
   private static final IFormattableTextComponent NO_EFFECT = (new TranslationTextComponent("effect.none")).withStyle(TextFormatting.GRAY);

   /**
    * Creates a list of {@code MobEffectInstance} from data on the passed {@code ItemStack} {@code CompoundTag}.
    * @param pStack The passed {@code ItemStack}
    */
   public static List<EffectInstance> getMobEffects(ItemStack pStack) {
      return getAllEffects(pStack.getTag());
   }

   /**
    * Creates a list of {@code MobEffectInstance} from a {@code Potion} as well as a {@code Collection} of {@code
    * MobEffectInstance}
    * @param pPotion the {@code Potion} being passed in
    * @param pEffects a collection of various {@code MobEffectInstance}
    */
   public static List<EffectInstance> getAllEffects(Potion pPotion, Collection<EffectInstance> pEffects) {
      List<EffectInstance> list = Lists.newArrayList();
      list.addAll(pPotion.getEffects());
      list.addAll(pEffects);
      return list;
   }

   /**
    * Creates a list of {@code MobEffectInstance}s from data on a {@code CompoundTag}.
    * @param pCompoundTag the passed {@code CompoundTag}
    */
   public static List<EffectInstance> getAllEffects(@Nullable CompoundNBT pCompoundTag) {
      List<EffectInstance> list = Lists.newArrayList();
      list.addAll(getPotion(pCompoundTag).getEffects());
      getCustomEffects(pCompoundTag, list);
      return list;
   }

   /**
    * Creates a list of {@code MobEffectInstance}s from data on the passed {@code {@code ItemStack}}
    * @param pStack the passed {@code ItemStack}
    */
   public static List<EffectInstance> getCustomEffects(ItemStack pStack) {
      return getCustomEffects(pStack.getTag());
   }

   /**
    * Creates a list of {@code MobEffectInstance} from data on the passed {@code CompoundTag}
    * @param pCompoundTag the passed {@code CompoundTag}
    */
   public static List<EffectInstance> getCustomEffects(@Nullable CompoundNBT pCompoundTag) {
      List<EffectInstance> list = Lists.newArrayList();
      getCustomEffects(pCompoundTag, list);
      return list;
   }

   /**
    * Fills a predefined list with {@code MobEffectInstance} from a {@code CompoundTag}
    * @param pCompoundTag the passed {@code CompoundTag}
    * @param pEffectList the predefined List holding {@code MobEffectInstance}
    */
   public static void getCustomEffects(@Nullable CompoundNBT pCompoundTag, List<EffectInstance> pEffectList) {
      if (pCompoundTag != null && pCompoundTag.contains("CustomPotionEffects", 9)) {
         ListNBT listnbt = pCompoundTag.getList("CustomPotionEffects", 10);

         for(int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            EffectInstance effectinstance = EffectInstance.load(compoundnbt);
            if (effectinstance != null) {
               pEffectList.add(effectinstance);
            }
         }
      }

   }

   /**
    * Gets the integer color of an {@code ItemStack} as defined by it's stored potion color tag
    * @param pStack the passed {@code ItemStack}
    */
   public static int getColor(ItemStack pStack) {
      CompoundNBT compoundnbt = pStack.getTag();
      if (compoundnbt != null && compoundnbt.contains("CustomPotionColor", 99)) {
         return compoundnbt.getInt("CustomPotionColor");
      } else {
         return getPotion(pStack) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(pStack));
      }
   }

   /**
    * Gets the integer color of an {@code Potion}
    * @param pPotion the passed {@code Potion}
    */
   public static int getColor(Potion pPotion) {
      return pPotion == Potions.EMPTY ? 16253176 : getColor(pPotion.getEffects());
   }

   /**
    * Gets the merged integer color based from a {@code Collection} of {@code MobEffectInstance}
    * @param pEffects the passed {@code Collection} of {@code MobEffectInstance}
    */
   public static int getColor(Collection<EffectInstance> pEffects) {
      int i = 3694022;
      if (pEffects.isEmpty()) {
         return 3694022;
      } else {
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         int j = 0;

         for(EffectInstance effectinstance : pEffects) {
            if (effectinstance.isVisible()) {
               int k = effectinstance.getEffect().getColor();
               int l = effectinstance.getAmplifier() + 1;
               f += (float)(l * (k >> 16 & 255)) / 255.0F;
               f1 += (float)(l * (k >> 8 & 255)) / 255.0F;
               f2 += (float)(l * (k >> 0 & 255)) / 255.0F;
               j += l;
            }
         }

         if (j == 0) {
            return 0;
         } else {
            f = f / (float)j * 255.0F;
            f1 = f1 / (float)j * 255.0F;
            f2 = f2 / (float)j * 255.0F;
            return (int)f << 16 | (int)f1 << 8 | (int)f2;
         }
      }
   }

   /**
    * Attempts to get the {@code Potion} from an {@code ItemStack}
    * If it fails, returns the default one : {@code Potions.WATER}
    * @param pStack the passed {@code ItemStack}
    */
   public static Potion getPotion(ItemStack pStack) {
      return getPotion(pStack.getTag());
   }

   /**
    * Attempts to get the {@code Potion} type of the passed {@code CompoundTag}
    * If no correct potion is found, returns the default one : {@code Potions.WATER}
    * @param pCompoundTag the passed {@code CompoundTag}
    */
   public static Potion getPotion(@Nullable CompoundNBT pCompoundTag) {
      return pCompoundTag == null ? Potions.EMPTY : Potion.byName(pCompoundTag.getString("Potion"));
   }

   /**
    * Sets the {@code Potion} type to the {@code ItemStack}
    * @param pStack the passed {@code ItemStack} to apply to
    * @param pPotion the passed {@code Potion} to use to apply to the {@code ItemStack}
    */
   public static ItemStack setPotion(ItemStack pStack, Potion pPotion) {
      ResourceLocation resourcelocation = Registry.POTION.getKey(pPotion);
      if (pPotion == Potions.EMPTY) {
         pStack.removeTagKey("Potion");
      } else {
         pStack.getOrCreateTag().putString("Potion", resourcelocation.toString());
      }

      return pStack;
   }

   /**
    * Sets a {@code Collection} of {@code MobEffectInstance} to a provided {@code ItemStack}'s NBT
    * @param pStack the passed {@code ItemStack}
    * @param pEffects the passed {@code Collection} of {@code MobEffectInstance}
    */
   public static ItemStack setCustomEffects(ItemStack pStack, Collection<EffectInstance> pEffects) {
      if (pEffects.isEmpty()) {
         return pStack;
      } else {
         CompoundNBT compoundnbt = pStack.getOrCreateTag();
         ListNBT listnbt = compoundnbt.getList("CustomPotionEffects", 9);

         for(EffectInstance effectinstance : pEffects) {
            listnbt.add(effectinstance.save(new CompoundNBT()));
         }

         compoundnbt.put("CustomPotionEffects", listnbt);
         return pStack;
      }
   }

   /**
    * Adds the tooltip of the {@code Potion} stored on the {@code ItemStack} along a "durationFactor"
    * @param pStack the passed {@code ItemStack}
    * @param pTooltips the passed list of current {@code Component} tooltips
    * @param pDurationFactor the passed durationFactor of the {@code Potion}
    */
   @OnlyIn(Dist.CLIENT)
   public static void addPotionTooltip(ItemStack pStack, List<ITextComponent> pTooltips, float pDurationFactor) {
      List<EffectInstance> list = getMobEffects(pStack);
      List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();
      if (list.isEmpty()) {
         pTooltips.add(NO_EFFECT);
      } else {
         for(EffectInstance effectinstance : list) {
            IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(effectinstance.getDescriptionId());
            Effect effect = effectinstance.getEffect();
            Map<Attribute, AttributeModifier> map = effect.getAttributeModifiers();
            if (!map.isEmpty()) {
               for(Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                  AttributeModifier attributemodifier = entry.getValue();
                  AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), effect.getAttributeModifierValue(effectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                  list1.add(new Pair<>(entry.getKey(), attributemodifier1));
               }
            }

            if (effectinstance.getAmplifier() > 0) {
               iformattabletextcomponent = new TranslationTextComponent("potion.withAmplifier", iformattabletextcomponent, new TranslationTextComponent("potion.potency." + effectinstance.getAmplifier()));
            }

            if (effectinstance.getDuration() > 20) {
               iformattabletextcomponent = new TranslationTextComponent("potion.withDuration", iformattabletextcomponent, EffectUtils.formatDuration(effectinstance, pDurationFactor));
            }

            pTooltips.add(iformattabletextcomponent.withStyle(effect.getCategory().getTooltipFormatting()));
         }
      }

      if (!list1.isEmpty()) {
         pTooltips.add(StringTextComponent.EMPTY);
         pTooltips.add((new TranslationTextComponent("potion.whenDrank")).withStyle(TextFormatting.DARK_PURPLE));

         for(Pair<Attribute, AttributeModifier> pair : list1) {
            AttributeModifier attributemodifier2 = pair.getSecond();
            double d0 = attributemodifier2.getAmount();
            double d1;
            if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
               d1 = attributemodifier2.getAmount();
            } else {
               d1 = attributemodifier2.getAmount() * 100.0D;
            }

            if (d0 > 0.0D) {
               pTooltips.add((new TranslationTextComponent("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(pair.getFirst().getDescriptionId()))).withStyle(TextFormatting.BLUE));
            } else if (d0 < 0.0D) {
               d1 = d1 * -1.0D;
               pTooltips.add((new TranslationTextComponent("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(pair.getFirst().getDescriptionId()))).withStyle(TextFormatting.RED));
            }
         }
      }

   }
}