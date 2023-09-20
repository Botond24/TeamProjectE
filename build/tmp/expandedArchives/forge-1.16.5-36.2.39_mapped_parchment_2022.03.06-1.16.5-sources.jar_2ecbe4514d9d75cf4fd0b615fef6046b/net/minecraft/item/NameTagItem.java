package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

public class NameTagItem extends Item {
   public NameTagItem(Item.Properties p_i48479_1_) {
      super(p_i48479_1_);
   }

   /**
    * Returns true if the item can be used on the given entity, e.g. shears on sheep.
    */
   public ActionResultType interactLivingEntity(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pTarget, Hand pHand) {
      if (pStack.hasCustomHoverName() && !(pTarget instanceof PlayerEntity)) {
         if (!pPlayer.level.isClientSide && pTarget.isAlive()) {
            pTarget.setCustomName(pStack.getHoverName());
            if (pTarget instanceof MobEntity) {
               ((MobEntity)pTarget).setPersistenceRequired();
            }

            pStack.shrink(1);
         }

         return ActionResultType.sidedSuccess(pPlayer.level.isClientSide);
      } else {
         return ActionResultType.PASS;
      }
   }
}