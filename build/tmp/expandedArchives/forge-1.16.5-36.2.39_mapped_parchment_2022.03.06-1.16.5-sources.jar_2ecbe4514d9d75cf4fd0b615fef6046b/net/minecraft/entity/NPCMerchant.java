package net.minecraft.entity;

import javax.annotation.Nullable;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NPCMerchant implements IMerchant {
   private final MerchantInventory container;
   private final PlayerEntity source;
   private MerchantOffers offers = new MerchantOffers();
   private int xp;

   public NPCMerchant(PlayerEntity pSource) {
      this.source = pSource;
      this.container = new MerchantInventory(this);
   }

   @Nullable
   public PlayerEntity getTradingPlayer() {
      return this.source;
   }

   public void setTradingPlayer(@Nullable PlayerEntity pTradingPlayer) {
   }

   public MerchantOffers getOffers() {
      return this.offers;
   }

   @OnlyIn(Dist.CLIENT)
   public void overrideOffers(@Nullable MerchantOffers pOffers) {
      this.offers = pOffers;
   }

   public void notifyTrade(MerchantOffer pOffer) {
      pOffer.increaseUses();
   }

   /**
    * Notifies the merchant of a possible merchantrecipe being fulfilled or not. Usually, this is just a sound byte
    * being played depending if the suggested itemstack is not null.
    */
   public void notifyTradeUpdated(ItemStack pStack) {
   }

   public World getLevel() {
      return this.source.level;
   }

   public int getVillagerXp() {
      return this.xp;
   }

   public void overrideXp(int pXp) {
      this.xp = pXp;
   }

   public boolean showProgressBar() {
      return true;
   }

   public SoundEvent getNotifyTradeSound() {
      return SoundEvents.VILLAGER_YES;
   }
}