package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MapItem extends AbstractMapItem {
   public MapItem(Item.Properties p_i48506_1_) {
      super(p_i48506_1_);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = FilledMapItem.create(pLevel, MathHelper.floor(pPlayer.getX()), MathHelper.floor(pPlayer.getZ()), (byte)0, true, false);
      ItemStack itemstack1 = pPlayer.getItemInHand(pHand);
      if (!pPlayer.abilities.instabuild) {
         itemstack1.shrink(1);
      }

      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      pPlayer.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
      if (itemstack1.isEmpty()) {
         return ActionResult.sidedSuccess(itemstack, pLevel.isClientSide());
      } else {
         if (!pPlayer.inventory.add(itemstack.copy())) {
            pPlayer.drop(itemstack, false);
         }

         return ActionResult.sidedSuccess(itemstack1, pLevel.isClientSide());
      }
   }
}