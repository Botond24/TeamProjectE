package net.minecraft.block;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class PumpkinBlock extends StemGrownBlock {
   public PumpkinBlock(AbstractBlock.Properties p_i48347_1_) {
      super(p_i48347_1_);
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getItem() == Items.SHEARS) {
         if (!pLevel.isClientSide) {
            Direction direction = pHit.getDirection();
            Direction direction1 = direction.getAxis() == Direction.Axis.Y ? pPlayer.getDirection().getOpposite() : direction;
            pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            pLevel.setBlock(pPos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction1), 11);
            ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + 0.5D + (double)direction1.getStepX() * 0.65D, (double)pPos.getY() + 0.1D, (double)pPos.getZ() + 0.5D + (double)direction1.getStepZ() * 0.65D, new ItemStack(Items.PUMPKIN_SEEDS, 4));
            itementity.setDeltaMovement(0.05D * (double)direction1.getStepX() + pLevel.random.nextDouble() * 0.02D, 0.05D, 0.05D * (double)direction1.getStepZ() + pLevel.random.nextDouble() * 0.02D);
            pLevel.addFreshEntity(itementity);
            itemstack.hurtAndBreak(1, pPlayer, (p_220282_1_) -> {
               p_220282_1_.broadcastBreakEvent(pHand);
            });
         }

         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else {
         return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
      }
   }

   public StemBlock getStem() {
      return (StemBlock)Blocks.PUMPKIN_STEM;
   }

   public AttachedStemBlock getAttachedStem() {
      return (AttachedStemBlock)Blocks.ATTACHED_PUMPKIN_STEM;
   }
}