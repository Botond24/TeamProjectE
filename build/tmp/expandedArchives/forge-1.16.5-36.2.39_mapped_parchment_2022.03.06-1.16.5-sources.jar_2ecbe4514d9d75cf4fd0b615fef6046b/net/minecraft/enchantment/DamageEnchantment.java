package net.minecraft.enchantment;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class DamageEnchantment extends Enchantment {
   private static final String[] NAMES = new String[]{"all", "undead", "arthropods"};
   private static final int[] MIN_COST = new int[]{1, 5, 5};
   private static final int[] LEVEL_COST = new int[]{11, 8, 8};
   private static final int[] LEVEL_COST_SPAN = new int[]{20, 20, 20};
   public final int type;

   public DamageEnchantment(Enchantment.Rarity pRarity, int pType, EquipmentSlotType... pApplicableSlots) {
      super(pRarity, EnchantmentType.WEAPON, pApplicableSlots);
      this.type = pType;
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return MIN_COST[this.type] + (pEnchantmentLevel - 1) * LEVEL_COST[this.type];
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + LEVEL_COST_SPAN[this.type];
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 5;
   }

   /**
    * Calculates the additional damage that will be dealt by an item with this enchantment. This alternative to
    * calcModifierDamage is sensitive to the targets EnumCreatureAttribute.
    * @param pLevel The level of the enchantment being used.
    */
   public float getDamageBonus(int pLevel, CreatureAttribute pCreatureType) {
      if (this.type == 0) {
         return 1.0F + (float)Math.max(0, pLevel - 1) * 0.5F;
      } else if (this.type == 1 && pCreatureType == CreatureAttribute.UNDEAD) {
         return (float)pLevel * 2.5F;
      } else {
         return this.type == 2 && pCreatureType == CreatureAttribute.ARTHROPOD ? (float)pLevel * 2.5F : 0.0F;
      }
   }

   /**
    * Determines if the enchantment passed can be applyied together with this enchantment.
    * @param pEnch The other enchantment to test compatibility with.
    */
   public boolean checkCompatibility(Enchantment pEnch) {
      return !(pEnch instanceof DamageEnchantment);
   }

   /**
    * Determines if this enchantment can be applied to a specific ItemStack.
    * @param pStack The ItemStack to test.
    */
   public boolean canEnchant(ItemStack pStack) {
      return pStack.getItem() instanceof AxeItem ? true : super.canEnchant(pStack);
   }

   /**
    * Called whenever a mob is damaged with an item that has this enchantment on it.
    * @param pUser The user of the enchantment.
    * @param pTarget The entity being attacked.
    * @param pLevel The level of the enchantment.
    */
   public void doPostAttack(LivingEntity pUser, Entity pTarget, int pLevel) {
      if (pTarget instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)pTarget;
         if (this.type == 2 && livingentity.getMobType() == CreatureAttribute.ARTHROPOD) {
            int i = 20 + pUser.getRandom().nextInt(10 * pLevel);
            livingentity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, i, 3));
         }
      }

   }
}