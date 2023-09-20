package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignItem extends WallOrFloorItem {
   public SignItem(Item.Properties pProperties, Block pStandingBlock, Block pWallBlock) {
      super(pStandingBlock, pWallBlock, pProperties);
   }

   protected boolean updateCustomBlockEntityTag(BlockPos pPos, World pLevel, @Nullable PlayerEntity pPlayer, ItemStack pStack, BlockState pState) {
      boolean flag = super.updateCustomBlockEntityTag(pPos, pLevel, pPlayer, pStack, pState);
      if (!pLevel.isClientSide && !flag && pPlayer != null) {
         pPlayer.openTextEdit((SignTileEntity)pLevel.getBlockEntity(pPos));
      }

      return flag;
   }
}