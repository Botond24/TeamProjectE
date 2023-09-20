package net.minecraft.enchantment;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

public class ProtectionEnchantment extends Enchantment {
   public final ProtectionEnchantment.Type type;

   public ProtectionEnchantment(Enchantment.Rarity pRarity, ProtectionEnchantment.Type pType, EquipmentSlotType... pApplicableSlots) {
      super(pRarity, pType == ProtectionEnchantment.Type.FALL ? EnchantmentType.ARMOR_FEET : EnchantmentType.ARMOR, pApplicableSlots);
      this.type = pType;
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return this.type.getMinCost() + (pEnchantmentLevel - 1) * this.type.getLevelCost();
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + this.type.getLevelCost();
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 4;
   }

   /**
    * Calculates the damage protection of the enchantment based on level and damage source passed.
    * @param pLevel The level of the enchantment being used.
    * @param pSource The source of the damage.
    */
   public int getDamageProtection(int pLevel, DamageSource pSource) {
      if (pSource.isBypassInvul()) {
         return 0;
      } else if (this.type == ProtectionEnchantment.Type.ALL) {
         return pLevel;
      } else if (this.type == ProtectionEnchantment.Type.FIRE && pSource.isFire()) {
         return pLevel * 2;
      } else if (this.type == ProtectionEnchantment.Type.FALL && pSource == DamageSource.FALL) {
         return pLevel * 3;
      } else if (this.type == ProtectionEnchantment.Type.EXPLOSION && pSource.isExplosion()) {
         return pLevel * 2;
      } else {
         return this.type == ProtectionEnchantment.Type.PROJECTILE && pSource.isProjectile() ? pLevel * 2 : 0;
      }
   }

   /**
    * Determines if the enchantment passed can be applyied together with this enchantment.
    * @param pEnch The other enchantment to test compatibility with.
    */
   public boolean checkCompatibility(Enchantment pEnch) {
      if (pEnch instanceof ProtectionEnchantment) {
         ProtectionEnchantment protectionenchantment = (ProtectionEnchantment)pEnch;
         if (this.type == protectionenchantment.type) {
            return false;
         } else {
            return this.type == ProtectionEnchantment.Type.FALL || protectionenchantment.type == ProtectionEnchantment.Type.FALL;
         }
      } else {
         return super.checkCompatibility(pEnch);
      }
   }

   /**
    * Gets the amount of ticks an entity should be set fire, adjusted for fire protection.
    */
   public static int getFireAfterDampener(LivingEntity pLivingEntity, int pLevel) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, pLivingEntity);
      if (i > 0) {
         pLevel -= MathHelper.floor((float)pLevel * (float)i * 0.15F);
      }

      return pLevel;
   }

   public static double getExplosionKnockbackAfterDampener(LivingEntity pLivingEntity, double pDamage) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, pLivingEntity);
      if (i > 0) {
         pDamage -= (double)MathHelper.floor(pDamage * (double)((float)i * 0.15F));
      }

      return pDamage;
   }

   public static enum Type {
      ALL("all", 1, 11),
      FIRE("fire", 10, 8),
      FALL("fall", 5, 6),
      EXPLOSION("explosion", 5, 8),
      PROJECTILE("projectile", 3, 6);

      private final String name;
      private final int minCost;
      private final int levelCost;

      private Type(String p_i48839_3_, int p_i48839_4_, int p_i48839_5_) {
         this.name = p_i48839_3_;
         this.minCost = p_i48839_4_;
         this.levelCost = p_i48839_5_;
      }

      public int getMinCost() {
         return this.minCost;
      }

      public int getLevelCost() {
         return this.levelCost;
      }
   }
}