package net.minecraft.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HangingEntityItem extends Item {
   private final EntityType<? extends HangingEntity> type;

   public HangingEntityItem(EntityType<? extends HangingEntity> pType, Item.Properties pProperties) {
      super(pProperties);
      this.type = pType;
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      Direction direction = pContext.getClickedFace();
      BlockPos blockpos1 = blockpos.relative(direction);
      PlayerEntity playerentity = pContext.getPlayer();
      ItemStack itemstack = pContext.getItemInHand();
      if (playerentity != null && !this.mayPlace(playerentity, direction, itemstack, blockpos1)) {
         return ActionResultType.FAIL;
      } else {
         World world = pContext.getLevel();
         HangingEntity hangingentity;
         if (this.type == EntityType.PAINTING) {
            hangingentity = new PaintingEntity(world, blockpos1, direction);
         } else {
            if (this.type != EntityType.ITEM_FRAME) {
               return ActionResultType.sidedSuccess(world.isClientSide);
            }

            hangingentity = new ItemFrameEntity(world, blockpos1, direction);
         }

         CompoundNBT compoundnbt = itemstack.getTag();
         if (compoundnbt != null) {
            EntityType.updateCustomEntityTag(world, playerentity, hangingentity, compoundnbt);
         }

         if (hangingentity.survives()) {
            if (!world.isClientSide) {
               hangingentity.playPlacementSound();
               world.addFreshEntity(hangingentity);
            }

            itemstack.shrink(1);
            return ActionResultType.sidedSuccess(world.isClientSide);
         } else {
            return ActionResultType.CONSUME;
         }
      }
   }

   protected boolean mayPlace(PlayerEntity pPlayer, Direction pDirection, ItemStack pHangingEntityStack, BlockPos pPos) {
      return !pDirection.getAxis().isVertical() && pPlayer.mayUseItemAt(pPos, pDirection, pHangingEntityStack);
   }
}