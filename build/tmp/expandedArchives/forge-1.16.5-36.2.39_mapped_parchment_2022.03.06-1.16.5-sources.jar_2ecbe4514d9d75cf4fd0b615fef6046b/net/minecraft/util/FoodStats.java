package net.minecraft.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FoodStats {
   private int foodLevel = 20;
   private float saturationLevel;
   private float exhaustionLevel;
   private int tickTimer;
   private int lastFoodLevel = 20;

   public FoodStats() {
      this.saturationLevel = 5.0F;
   }

   /**
    * Add food stats.
    */
   public void eat(int pFoodLevelModifier, float pSaturationLevelModifier) {
      this.foodLevel = Math.min(pFoodLevelModifier + this.foodLevel, 20);
      this.saturationLevel = Math.min(this.saturationLevel + (float)pFoodLevelModifier * pSaturationLevelModifier * 2.0F, (float)this.foodLevel);
   }

   public void eat(Item pItem, ItemStack pStack) {
      if (pItem.isEdible()) {
         Food food = pItem.getFoodProperties();
         this.eat(food.getNutrition(), food.getSaturationModifier());
      }

   }

   /**
    * Handles the food game logic.
    */
   public void tick(PlayerEntity pPlayer) {
      Difficulty difficulty = pPlayer.level.getDifficulty();
      this.lastFoodLevel = this.foodLevel;
      if (this.exhaustionLevel > 4.0F) {
         this.exhaustionLevel -= 4.0F;
         if (this.saturationLevel > 0.0F) {
            this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
         } else if (difficulty != Difficulty.PEACEFUL) {
            this.foodLevel = Math.max(this.foodLevel - 1, 0);
         }
      }

      boolean flag = pPlayer.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
      if (flag && this.saturationLevel > 0.0F && pPlayer.isHurt() && this.foodLevel >= 20) {
         ++this.tickTimer;
         if (this.tickTimer >= 10) {
            float f = Math.min(this.saturationLevel, 6.0F);
            pPlayer.heal(f / 6.0F);
            this.addExhaustion(f);
            this.tickTimer = 0;
         }
      } else if (flag && this.foodLevel >= 18 && pPlayer.isHurt()) {
         ++this.tickTimer;
         if (this.tickTimer >= 80) {
            pPlayer.heal(1.0F);
            this.addExhaustion(6.0F);
            this.tickTimer = 0;
         }
      } else if (this.foodLevel <= 0) {
         ++this.tickTimer;
         if (this.tickTimer >= 80) {
            if (pPlayer.getHealth() > 10.0F || difficulty == Difficulty.HARD || pPlayer.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
               pPlayer.hurt(DamageSource.STARVE, 1.0F);
            }

            this.tickTimer = 0;
         }
      } else {
         this.tickTimer = 0;
      }

   }

   /**
    * Reads the food data for the player.
    */
   public void readAdditionalSaveData(CompoundNBT pCompoundTag) {
      if (pCompoundTag.contains("foodLevel", 99)) {
         this.foodLevel = pCompoundTag.getInt("foodLevel");
         this.tickTimer = pCompoundTag.getInt("foodTickTimer");
         this.saturationLevel = pCompoundTag.getFloat("foodSaturationLevel");
         this.exhaustionLevel = pCompoundTag.getFloat("foodExhaustionLevel");
      }

   }

   /**
    * Writes the food data for the player.
    */
   public void addAdditionalSaveData(CompoundNBT pCompoundTag) {
      pCompoundTag.putInt("foodLevel", this.foodLevel);
      pCompoundTag.putInt("foodTickTimer", this.tickTimer);
      pCompoundTag.putFloat("foodSaturationLevel", this.saturationLevel);
      pCompoundTag.putFloat("foodExhaustionLevel", this.exhaustionLevel);
   }

   /**
    * Get the player's food level.
    */
   public int getFoodLevel() {
      return this.foodLevel;
   }

   /**
    * Get whether the player must eat food.
    */
   public boolean needsFood() {
      return this.foodLevel < 20;
   }

   /**
    * adds input to foodExhaustionLevel to a max of 40
    */
   public void addExhaustion(float pExhaustion) {
      this.exhaustionLevel = Math.min(this.exhaustionLevel + pExhaustion, 40.0F);
   }

   /**
    * Get the player's food saturation level.
    */
   public float getSaturationLevel() {
      return this.saturationLevel;
   }

   public void setFoodLevel(int pFoodLevel) {
      this.foodLevel = pFoodLevel;
   }

   @OnlyIn(Dist.CLIENT)
   public void setSaturation(float pSaturationLevel) {
      this.saturationLevel = pSaturationLevel;
   }
}