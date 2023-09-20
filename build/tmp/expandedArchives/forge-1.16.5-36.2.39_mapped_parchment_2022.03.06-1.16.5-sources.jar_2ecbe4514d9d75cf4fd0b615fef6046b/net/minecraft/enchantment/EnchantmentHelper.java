package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnchantmentHelper {
   /**
    * Returns the level of enchantment on the ItemStack passed.
    */
   public static int getItemEnchantmentLevel(Enchantment pEnchantment, ItemStack pStack) {
      if (pStack.isEmpty()) {
         return 0;
      } else {
         ResourceLocation resourcelocation = Registry.ENCHANTMENT.getKey(pEnchantment);
         ListNBT listnbt = pStack.getEnchantmentTags();

         for(int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            ResourceLocation resourcelocation1 = ResourceLocation.tryParse(compoundnbt.getString("id"));
            if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
               return MathHelper.clamp(compoundnbt.getInt("lvl"), 0, 255);
            }
         }

         return 0;
      }
   }

   /**
    * Return the enchantments for the specified stack.
    */
   public static Map<Enchantment, Integer> getEnchantments(ItemStack pStack) {
      ListNBT listnbt = pStack.getItem() == Items.ENCHANTED_BOOK ? EnchantedBookItem.getEnchantments(pStack) : pStack.getEnchantmentTags();
      return deserializeEnchantments(listnbt);
   }

   public static Map<Enchantment, Integer> deserializeEnchantments(ListNBT pSerialized) {
      Map<Enchantment, Integer> map = Maps.newLinkedHashMap();

      for(int i = 0; i < pSerialized.size(); ++i) {
         CompoundNBT compoundnbt = pSerialized.getCompound(i);
         Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(compoundnbt.getString("id"))).ifPresent((p_226651_2_) -> {
            Integer integer = map.put(p_226651_2_, compoundnbt.getInt("lvl"));
         });
      }

      return map;
   }

   /**
    * Set the enchantments for the specified stack.
    */
   public static void setEnchantments(Map<Enchantment, Integer> pEnchantmentsMap, ItemStack pStack) {
      ListNBT listnbt = new ListNBT();

      for(Entry<Enchantment, Integer> entry : pEnchantmentsMap.entrySet()) {
         Enchantment enchantment = entry.getKey();
         if (enchantment != null) {
            int i = entry.getValue();
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.putString("id", String.valueOf((Object)Registry.ENCHANTMENT.getKey(enchantment)));
            compoundnbt.putShort("lvl", (short)i);
            listnbt.add(compoundnbt);
            if (pStack.getItem() == Items.ENCHANTED_BOOK) {
               EnchantedBookItem.addEnchantment(pStack, new EnchantmentData(enchantment, i));
            }
         }
      }

      if (listnbt.isEmpty()) {
         pStack.removeTagKey("Enchantments");
      } else if (pStack.getItem() != Items.ENCHANTED_BOOK) {
         pStack.addTagElement("Enchantments", listnbt);
      }

   }

   /**
    * Executes the enchantment modifier on the ItemStack passed.
    */
   private static void runIterationOnItem(EnchantmentHelper.IEnchantmentVisitor pVisitor, ItemStack pStack) {
      if (!pStack.isEmpty()) {
         ListNBT listnbt = pStack.getEnchantmentTags();

         for(int i = 0; i < listnbt.size(); ++i) {
            String s = listnbt.getCompound(i).getString("id");
            int j = listnbt.getCompound(i).getInt("lvl");
            Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(s)).ifPresent((p_222184_2_) -> {
               pVisitor.accept(p_222184_2_, j);
            });
         }

      }
   }

   /**
    * Executes the enchantment modifier on the array of ItemStack passed.
    */
   private static void runIterationOnInventory(EnchantmentHelper.IEnchantmentVisitor pVisitor, Iterable<ItemStack> pStacks) {
      for(ItemStack itemstack : pStacks) {
         runIterationOnItem(pVisitor, itemstack);
      }

   }

   /**
    * Returns the modifier of protection enchantments on armors equipped on player.
    */
   public static int getDamageProtection(Iterable<ItemStack> pStacks, DamageSource pSource) {
      MutableInt mutableint = new MutableInt();
      runIterationOnInventory((p_212576_2_, p_212576_3_) -> {
         mutableint.add(p_212576_2_.getDamageProtection(p_212576_3_, pSource));
      }, pStacks);
      return mutableint.intValue();
   }

   public static float getDamageBonus(ItemStack pStack, CreatureAttribute pCreatureAttribute) {
      MutableFloat mutablefloat = new MutableFloat();
      runIterationOnItem((p_212573_2_, p_212573_3_) -> {
         mutablefloat.add(p_212573_2_.getDamageBonus(p_212573_3_, pCreatureAttribute));
      }, pStack);
      return mutablefloat.floatValue();
   }

   public static float getSweepingDamageRatio(LivingEntity pEntity) {
      int i = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, pEntity);
      return i > 0 ? SweepingEnchantment.getSweepingDamageRatio(i) : 0.0F;
   }

   public static void doPostHurtEffects(LivingEntity pUser, Entity pAttacker) {
      EnchantmentHelper.IEnchantmentVisitor enchantmenthelper$ienchantmentvisitor = (p_212575_2_, p_212575_3_) -> {
         p_212575_2_.doPostHurt(pUser, pAttacker, p_212575_3_);
      };
      if (pUser != null) {
         runIterationOnInventory(enchantmenthelper$ienchantmentvisitor, pUser.getAllSlots());
      }

      if (pAttacker instanceof PlayerEntity) {
         runIterationOnItem(enchantmenthelper$ienchantmentvisitor, pUser.getMainHandItem());
      }

   }

   public static void doPostDamageEffects(LivingEntity pUser, Entity pTarget) {
      EnchantmentHelper.IEnchantmentVisitor enchantmenthelper$ienchantmentvisitor = (p_212574_2_, p_212574_3_) -> {
         p_212574_2_.doPostAttack(pUser, pTarget, p_212574_3_);
      };
      if (pUser != null) {
         runIterationOnInventory(enchantmenthelper$ienchantmentvisitor, pUser.getAllSlots());
      }

      if (pUser instanceof PlayerEntity) {
         runIterationOnItem(enchantmenthelper$ienchantmentvisitor, pUser.getMainHandItem());
      }

   }

   public static int getEnchantmentLevel(Enchantment pEnchantment, LivingEntity pEntity) {
      Iterable<ItemStack> iterable = pEnchantment.getSlotItems(pEntity).values();
      if (iterable == null) {
         return 0;
      } else {
         int i = 0;

         for(ItemStack itemstack : iterable) {
            int j = getItemEnchantmentLevel(pEnchantment, itemstack);
            if (j > i) {
               i = j;
            }
         }

         return i;
      }
   }

   /**
    * Returns the Knockback modifier of the enchantment on the players held item.
    */
   public static int getKnockbackBonus(LivingEntity pPlayer) {
      return getEnchantmentLevel(Enchantments.KNOCKBACK, pPlayer);
   }

   /**
    * Returns the fire aspect modifier of the players held item.
    */
   public static int getFireAspect(LivingEntity pPlayer) {
      return getEnchantmentLevel(Enchantments.FIRE_ASPECT, pPlayer);
   }

   public static int getRespiration(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.RESPIRATION, pEntity);
   }

   public static int getDepthStrider(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, pEntity);
   }

   public static int getBlockEfficiency(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, pEntity);
   }

   public static int getFishingLuckBonus(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.FISHING_LUCK, pStack);
   }

   public static int getFishingSpeedBonus(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.FISHING_SPEED, pStack);
   }

   public static int getMobLooting(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.MOB_LOOTING, pEntity);
   }

   public static boolean hasAquaAffinity(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.AQUA_AFFINITY, pEntity) > 0;
   }

   /**
    * Checks if the player has any armor enchanted with the frost walker enchantment.
    * @return If player has equipment with frost walker
    */
   public static boolean hasFrostWalker(LivingEntity pPlayer) {
      return getEnchantmentLevel(Enchantments.FROST_WALKER, pPlayer) > 0;
   }

   public static boolean hasSoulSpeed(LivingEntity pEntity) {
      return getEnchantmentLevel(Enchantments.SOUL_SPEED, pEntity) > 0;
   }

   public static boolean hasBindingCurse(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.BINDING_CURSE, pStack) > 0;
   }

   public static boolean hasVanishingCurse(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, pStack) > 0;
   }

   public static int getLoyalty(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.LOYALTY, pStack);
   }

   public static int getRiptide(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.RIPTIDE, pStack);
   }

   public static boolean hasChanneling(ItemStack pStack) {
      return getItemEnchantmentLevel(Enchantments.CHANNELING, pStack) > 0;
   }

   /**
    * Gets an item with a specified enchantment from a living entity. If there are more than one valid items a random
    * one will be returned.
    */
   @Nullable
   public static Entry<EquipmentSlotType, ItemStack> getRandomItemWith(Enchantment pTargetEnchantment, LivingEntity pEntity) {
      return getRandomItemWith(pTargetEnchantment, pEntity, (p_234845_0_) -> {
         return true;
      });
   }

   @Nullable
   public static Entry<EquipmentSlotType, ItemStack> getRandomItemWith(Enchantment pEnchantment, LivingEntity pLivingEntity, Predicate<ItemStack> pStackCondition) {
      Map<EquipmentSlotType, ItemStack> map = pEnchantment.getSlotItems(pLivingEntity);
      if (map.isEmpty()) {
         return null;
      } else {
         List<Entry<EquipmentSlotType, ItemStack>> list = Lists.newArrayList();

         for(Entry<EquipmentSlotType, ItemStack> entry : map.entrySet()) {
            ItemStack itemstack = entry.getValue();
            if (!itemstack.isEmpty() && getItemEnchantmentLevel(pEnchantment, itemstack) > 0 && pStackCondition.test(itemstack)) {
               list.add(entry);
            }
         }

         return list.isEmpty() ? null : list.get(pLivingEntity.getRandom().nextInt(list.size()));
      }
   }

   /**
    * Returns the enchantability of itemstack, using a separate calculation for each enchantNum (0, 1 or 2), cutting to
    * the max enchantability power of the table, which is locked to a max of 15.
    */
   public static int getEnchantmentCost(Random pRand, int pEnchantNum, int pPower, ItemStack pStack) {
      Item item = pStack.getItem();
      int i = pStack.getItemEnchantability();
      if (i <= 0) {
         return 0;
      } else {
         if (pPower > 15) {
            pPower = 15;
         }

         int j = pRand.nextInt(8) + 1 + (pPower >> 1) + pRand.nextInt(pPower + 1);
         if (pEnchantNum == 0) {
            return Math.max(j / 3, 1);
         } else {
            return pEnchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, pPower * 2);
         }
      }
   }

   /**
    * Applys a random enchantment to the specified item.
    */
   public static ItemStack enchantItem(Random pRandom, ItemStack pStack, int pLevel, boolean pAllowTreasure) {
      List<EnchantmentData> list = selectEnchantment(pRandom, pStack, pLevel, pAllowTreasure);
      boolean flag = pStack.getItem() == Items.BOOK;
      if (flag) {
         pStack = new ItemStack(Items.ENCHANTED_BOOK);
      }

      for(EnchantmentData enchantmentdata : list) {
         if (flag) {
            EnchantedBookItem.addEnchantment(pStack, enchantmentdata);
         } else {
            pStack.enchant(enchantmentdata.enchantment, enchantmentdata.level);
         }
      }

      return pStack;
   }

   /**
    * Create a list of random EnchantmentData (enchantments) that can be added together to the ItemStack, the 3rd
    * parameter is the total enchantability level.
    */
   public static List<EnchantmentData> selectEnchantment(Random pRandom, ItemStack pItemStack, int pLevel, boolean pAllowTreasure) {
      List<EnchantmentData> list = Lists.newArrayList();
      Item item = pItemStack.getItem();
      int i = pItemStack.getItemEnchantability();
      if (i <= 0) {
         return list;
      } else {
         pLevel = pLevel + 1 + pRandom.nextInt(i / 4 + 1) + pRandom.nextInt(i / 4 + 1);
         float f = (pRandom.nextFloat() + pRandom.nextFloat() - 1.0F) * 0.15F;
         pLevel = MathHelper.clamp(Math.round((float)pLevel + (float)pLevel * f), 1, Integer.MAX_VALUE);
         List<EnchantmentData> list1 = getAvailableEnchantmentResults(pLevel, pItemStack, pAllowTreasure);
         if (!list1.isEmpty()) {
            list.add(WeightedRandom.getRandomItem(pRandom, list1));

            while(pRandom.nextInt(50) <= pLevel) {
               filterCompatibleEnchantments(list1, Util.lastOf(list));
               if (list1.isEmpty()) {
                  break;
               }

               list.add(WeightedRandom.getRandomItem(pRandom, list1));
               pLevel /= 2;
            }
         }

         return list;
      }
   }

   public static void filterCompatibleEnchantments(List<EnchantmentData> pDataList, EnchantmentData pData) {
      Iterator<EnchantmentData> iterator = pDataList.iterator();

      while(iterator.hasNext()) {
         if (!pData.enchantment.isCompatibleWith((iterator.next()).enchantment)) {
            iterator.remove();
         }
      }

   }

   public static boolean isEnchantmentCompatible(Collection<Enchantment> pEnchantments, Enchantment pEnchantment) {
      for(Enchantment enchantment : pEnchantments) {
         if (!enchantment.isCompatibleWith(pEnchantment)) {
            return false;
         }
      }

      return true;
   }

   public static List<EnchantmentData> getAvailableEnchantmentResults(int pLevel, ItemStack pStack, boolean pAllowTreasure) {
      List<EnchantmentData> list = Lists.newArrayList();
      Item item = pStack.getItem();
      boolean flag = pStack.getItem() == Items.BOOK;

      for(Enchantment enchantment : Registry.ENCHANTMENT) {
         if ((!enchantment.isTreasureOnly() || pAllowTreasure) && enchantment.isDiscoverable() && (enchantment.canApplyAtEnchantingTable(pStack) || (flag && enchantment.isAllowedOnBooks()))) {
            for(int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
               if (pLevel >= enchantment.getMinCost(i) && pLevel <= enchantment.getMaxCost(i)) {
                  list.add(new EnchantmentData(enchantment, i));
                  break;
               }
            }
         }
      }

      return list;
   }

   @FunctionalInterface
   interface IEnchantmentVisitor {
      void accept(Enchantment p_accept_1_, int p_accept_2_);
   }
}
