package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LeadItem extends Item {
   public LeadItem(Item.Properties p_i48484_1_) {
      super(p_i48484_1_);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      Block block = world.getBlockState(blockpos).getBlock();
      if (block.is(BlockTags.FENCES)) {
         PlayerEntity playerentity = pContext.getPlayer();
         if (!world.isClientSide && playerentity != null) {
            bindPlayerMobs(playerentity, world, blockpos);
         }

         return ActionResultType.sidedSuccess(world.isClientSide);
      } else {
         return ActionResultType.PASS;
      }
   }

   public static ActionResultType bindPlayerMobs(PlayerEntity pPlayer, World pLevel, BlockPos pPos) {
      LeashKnotEntity leashknotentity = null;
      boolean flag = false;
      double d0 = 7.0D;
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();

      for(MobEntity mobentity : pLevel.getEntitiesOfClass(MobEntity.class, new AxisAlignedBB((double)i - 7.0D, (double)j - 7.0D, (double)k - 7.0D, (double)i + 7.0D, (double)j + 7.0D, (double)k + 7.0D))) {
         if (mobentity.getLeashHolder() == pPlayer) {
            if (leashknotentity == null) {
               leashknotentity = LeashKnotEntity.getOrCreateKnot(pLevel, pPos);
            }

            mobentity.setLeashedTo(leashknotentity, true);
            flag = true;
         }
      }

      return flag ? ActionResultType.SUCCESS : ActionResultType.PASS;
   }
}