package net.minecraft.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemFrameItem extends HangingEntityItem {
   public ItemFrameItem(Item.Properties p_i48486_1_) {
      super(EntityType.ITEM_FRAME, p_i48486_1_);
   }

   protected boolean mayPlace(PlayerEntity pPlayer, Direction pDirection, ItemStack pHangingEntityStack, BlockPos pPos) {
      return !World.isOutsideBuildHeight(pPos) && pPlayer.mayUseItemAt(pPos, pDirection, pHangingEntityStack);
   }
}