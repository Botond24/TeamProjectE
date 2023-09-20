package net.minecraft.enchantment;

import net.minecraft.block.Block;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.TridentItem;

public enum EnchantmentType implements net.minecraftforge.common.IExtensibleEnum {
   ARMOR {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof ArmorItem;
      }
   },
   ARMOR_FEET {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof ArmorItem && ((ArmorItem)pItem).getSlot() == EquipmentSlotType.FEET;
      }
   },
   ARMOR_LEGS {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof ArmorItem && ((ArmorItem)pItem).getSlot() == EquipmentSlotType.LEGS;
      }
   },
   ARMOR_CHEST {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof ArmorItem && ((ArmorItem)pItem).getSlot() == EquipmentSlotType.CHEST;
      }
   },
   ARMOR_HEAD {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof ArmorItem && ((ArmorItem)pItem).getSlot() == EquipmentSlotType.HEAD;
      }
   },
   WEAPON {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof SwordItem;
      }
   },
   DIGGER {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof ToolItem;
      }
   },
   FISHING_ROD {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof FishingRodItem;
      }
   },
   TRIDENT {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof TridentItem;
      }
   },
   BREAKABLE {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem.canBeDepleted();
      }
   },
   BOW {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof BowItem;
      }
   },
   WEARABLE {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof IArmorVanishable || Block.byItem(pItem) instanceof IArmorVanishable;
      }
   },
   CROSSBOW {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof CrossbowItem;
      }
   },
   VANISHABLE {
      /**
       * Return true if the item passed can be enchanted by a enchantment of this type.
       */
      public boolean canEnchant(Item pItem) {
         return pItem instanceof IVanishable || Block.byItem(pItem) instanceof IVanishable || BREAKABLE.canEnchant(pItem);
      }
   };

   private EnchantmentType() {
   }

   private java.util.function.Predicate<Item> delegate;
   private EnchantmentType(java.util.function.Predicate<Item> delegate) {
      this.delegate = delegate;
   }

   public static EnchantmentType create(String name, java.util.function.Predicate<Item> delegate) {
      throw new IllegalStateException("Enum not extended");
   }

   /**
    * Return true if the item passed can be enchanted by a enchantment of this type.
    */
   public boolean canEnchant(Item pItem) {
      return this.delegate == null ? false : this.delegate.test(pItem);
   }
}
