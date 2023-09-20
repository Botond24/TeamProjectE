package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnchantedBookItem extends Item {
   public EnchantedBookItem(Item.Properties p_i48505_1_) {
      super(p_i48505_1_);
   }

   /**
    * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
    * but other items can override it (for instance, written books always return true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return true;
   }

   /**
    * Checks isDamagable and if it cannot be stacked
    */
   public boolean isEnchantable(ItemStack pStack) {
      return false;
   }

   public static ListNBT getEnchantments(ItemStack pEnchantedBookStack) {
      CompoundNBT compoundnbt = pEnchantedBookStack.getTag();
      return compoundnbt != null ? compoundnbt.getList("StoredEnchantments", 10) : new ListNBT();
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      ItemStack.appendEnchantmentNames(pTooltip, getEnchantments(pStack));
   }

   /**
    * Adds an stored enchantment to an enchanted book ItemStack
    */
   public static void addEnchantment(ItemStack pStack, EnchantmentData pInstance) {
      ListNBT listnbt = getEnchantments(pStack);
      boolean flag = true;
      ResourceLocation resourcelocation = Registry.ENCHANTMENT.getKey(pInstance.enchantment);

      for(int i = 0; i < listnbt.size(); ++i) {
         CompoundNBT compoundnbt = listnbt.getCompound(i);
         ResourceLocation resourcelocation1 = ResourceLocation.tryParse(compoundnbt.getString("id"));
         if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
            if (compoundnbt.getInt("lvl") < pInstance.level) {
               compoundnbt.putShort("lvl", (short)pInstance.level);
            }

            flag = false;
            break;
         }
      }

      if (flag) {
         CompoundNBT compoundnbt1 = new CompoundNBT();
         compoundnbt1.putString("id", String.valueOf((Object)resourcelocation));
         compoundnbt1.putShort("lvl", (short)pInstance.level);
         listnbt.add(compoundnbt1);
      }

      pStack.getOrCreateTag().put("StoredEnchantments", listnbt);
   }

   /**
    * Returns the ItemStack of an enchanted version of this item.
    */
   public static ItemStack createForEnchantment(EnchantmentData pInstance) {
      ItemStack itemstack = new ItemStack(Items.ENCHANTED_BOOK);
      addEnchantment(itemstack, pInstance);
      return itemstack;
   }

   /**
    * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
    */
   public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) {
      if (pGroup == ItemGroup.TAB_SEARCH) {
         for(Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.category != null) {
               for(int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
                  pItems.add(createForEnchantment(new EnchantmentData(enchantment, i)));
               }
            }
         }
      } else if (pGroup.getEnchantmentCategories().length != 0) {
         for(Enchantment enchantment1 : Registry.ENCHANTMENT) {
            if (pGroup.hasEnchantmentCategory(enchantment1.category)) {
               pItems.add(createForEnchantment(new EnchantmentData(enchantment1, enchantment1.getMaxLevel())));
            }
         }
      }

   }
}