package net.minecraft.entity.merchant;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IMerchant {
   void setTradingPlayer(@Nullable PlayerEntity pTradingPlayer);

   @Nullable
   PlayerEntity getTradingPlayer();

   MerchantOffers getOffers();

   @OnlyIn(Dist.CLIENT)
   void overrideOffers(@Nullable MerchantOffers pOffers);

   void notifyTrade(MerchantOffer pOffer);

   /**
    * Notifies the merchant of a possible merchantrecipe being fulfilled or not. Usually, this is just a sound byte
    * being played depending if the suggested itemstack is not null.
    */
   void notifyTradeUpdated(ItemStack pStack);

   World getLevel();

   int getVillagerXp();

   void overrideXp(int pXp);

   boolean showProgressBar();

   SoundEvent getNotifyTradeSound();

   default boolean canRestock() {
      return false;
   }

   default void openTradingScreen(PlayerEntity pPlayer, ITextComponent pDisplayName, int pLevel) {
      OptionalInt optionalint = pPlayer.openMenu(new SimpleNamedContainerProvider((p_213701_1_, p_213701_2_, p_213701_3_) -> {
         return new MerchantContainer(p_213701_1_, p_213701_2_, this);
      }, pDisplayName));
      if (optionalint.isPresent()) {
         MerchantOffers merchantoffers = this.getOffers();
         if (!merchantoffers.isEmpty()) {
            pPlayer.sendMerchantOffers(optionalint.getAsInt(), merchantoffers, pLevel, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
         }
      }

   }
}