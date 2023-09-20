package net.minecraft.item;

import net.minecraft.entity.IEquipable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;

public class SaddleItem extends Item {
   public SaddleItem(Item.Properties p_i48474_1_) {
      super(p_i48474_1_);
   }

   /**
    * Returns true if the item can be used on the given entity, e.g. shears on sheep.
    */
   public ActionResultType interactLivingEntity(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pTarget, Hand pHand) {
      if (pTarget instanceof IEquipable && pTarget.isAlive()) {
         IEquipable iequipable = (IEquipable)pTarget;
         if (!iequipable.isSaddled() && iequipable.isSaddleable()) {
            if (!pPlayer.level.isClientSide) {
               iequipable.equipSaddle(SoundCategory.NEUTRAL);
               pStack.shrink(1);
            }

            return ActionResultType.sidedSuccess(pPlayer.level.isClientSide);
         }
      }

      return ActionResultType.PASS;
   }
}