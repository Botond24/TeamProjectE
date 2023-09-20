package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlintAndSteelItem extends Item {
   public FlintAndSteelItem(Item.Properties p_i48493_1_) {
      super(p_i48493_1_);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      PlayerEntity playerentity = pContext.getPlayer();
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      if (CampfireBlock.canLight(blockstate)) {
         world.playSound(playerentity, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
         world.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
         if (playerentity != null) {
            pContext.getItemInHand().hurtAndBreak(1, playerentity, (p_219999_1_) -> {
               p_219999_1_.broadcastBreakEvent(pContext.getHand());
            });
         }

         return ActionResultType.sidedSuccess(world.isClientSide());
      } else {
         BlockPos blockpos1 = blockpos.relative(pContext.getClickedFace());
         if (AbstractFireBlock.canBePlacedAt(world, blockpos1, pContext.getHorizontalDirection())) {
            world.playSound(playerentity, blockpos1, SoundEvents.FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
            BlockState blockstate1 = AbstractFireBlock.getState(world, blockpos1);
            world.setBlock(blockpos1, blockstate1, 11);
            ItemStack itemstack = pContext.getItemInHand();
            if (playerentity instanceof ServerPlayerEntity) {
               CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity)playerentity, blockpos1, itemstack);
               itemstack.hurtAndBreak(1, playerentity, (p_219998_1_) -> {
                  p_219998_1_.broadcastBreakEvent(pContext.getHand());
               });
            }

            return ActionResultType.sidedSuccess(world.isClientSide());
         } else {
            return ActionResultType.FAIL;
         }
      }
   }
}