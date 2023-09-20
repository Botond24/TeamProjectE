package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ChorusFruitItem extends Item {
   public ChorusFruitItem(Item.Properties p_i50053_1_) {
      super(p_i50053_1_);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity pEntityLiving) {
      ItemStack itemstack = super.finishUsingItem(pStack, pLevel, pEntityLiving);
      if (!pLevel.isClientSide) {
         double d0 = pEntityLiving.getX();
         double d1 = pEntityLiving.getY();
         double d2 = pEntityLiving.getZ();

         for(int i = 0; i < 16; ++i) {
            double d3 = pEntityLiving.getX() + (pEntityLiving.getRandom().nextDouble() - 0.5D) * 16.0D;
            double d4 = MathHelper.clamp(pEntityLiving.getY() + (double)(pEntityLiving.getRandom().nextInt(16) - 8), 0.0D, (double)(pLevel.getHeight() - 1));
            double d5 = pEntityLiving.getZ() + (pEntityLiving.getRandom().nextDouble() - 0.5D) * 16.0D;
            if (pEntityLiving.isPassenger()) {
               pEntityLiving.stopRiding();
            }

            net.minecraftforge.event.entity.living.EntityTeleportEvent.ChorusFruit event = net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(pEntityLiving, d3, d4, d5);
            if (event.isCanceled()) return itemstack;
            if (pEntityLiving.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
               SoundEvent soundevent = pEntityLiving instanceof FoxEntity ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
               pLevel.playSound((PlayerEntity)null, d0, d1, d2, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
               pEntityLiving.playSound(soundevent, 1.0F, 1.0F);
               break;
            }
         }

         if (pEntityLiving instanceof PlayerEntity) {
            ((PlayerEntity)pEntityLiving).getCooldowns().addCooldown(this, 20);
         }
      }

      return itemstack;
   }
}
