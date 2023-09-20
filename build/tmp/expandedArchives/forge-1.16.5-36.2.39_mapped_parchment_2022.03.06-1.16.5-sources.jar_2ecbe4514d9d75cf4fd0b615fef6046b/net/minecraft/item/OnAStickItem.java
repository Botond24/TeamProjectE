package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRideable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class OnAStickItem<T extends Entity & IRideable> extends Item {
   private final EntityType<T> canInteractWith;
   private final int consumeItemDamage;

   public OnAStickItem(Item.Properties pProperties, EntityType<T> pCanInteractWith, int pConsumeItemDamage) {
      super(pProperties);
      this.canInteractWith = pCanInteractWith;
      this.consumeItemDamage = pConsumeItemDamage;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (pLevel.isClientSide) {
         return ActionResult.pass(itemstack);
      } else {
         Entity entity = pPlayer.getVehicle();
         if (pPlayer.isPassenger() && entity instanceof IRideable && entity.getType() == this.canInteractWith) {
            IRideable irideable = (IRideable)entity;
            if (irideable.boost()) {
               itemstack.hurtAndBreak(this.consumeItemDamage, pPlayer, (p_234682_1_) -> {
                  p_234682_1_.broadcastBreakEvent(pHand);
               });
               if (itemstack.isEmpty()) {
                  ItemStack itemstack1 = new ItemStack(Items.FISHING_ROD);
                  itemstack1.setTag(itemstack.getTag());
                  return ActionResult.success(itemstack1);
               }

               return ActionResult.success(itemstack);
            }
         }

         pPlayer.awardStat(Stats.ITEM_USED.get(this));
         return ActionResult.pass(itemstack);
      }
   }
}