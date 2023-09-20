package net.minecraft.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

public class DefaultDispenseItemBehavior implements IDispenseItemBehavior {
   public final ItemStack dispense(IBlockSource p_dispense_1_, ItemStack p_dispense_2_) {
      ItemStack itemstack = this.execute(p_dispense_1_, p_dispense_2_);
      this.playSound(p_dispense_1_);
      this.playAnimation(p_dispense_1_, p_dispense_1_.getBlockState().getValue(DispenserBlock.FACING));
      return itemstack;
   }

   /**
    * Dispense the specified stack, play the dispense sound and spawn particles.
    */
   protected ItemStack execute(IBlockSource pSource, ItemStack pStack) {
      Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
      IPosition iposition = DispenserBlock.getDispensePosition(pSource);
      ItemStack itemstack = pStack.split(1);
      spawnItem(pSource.getLevel(), itemstack, 6, direction, iposition);
      return pStack;
   }

   public static void spawnItem(World pLevel, ItemStack pStack, int pSpeed, Direction pFacing, IPosition pPosition) {
      double d0 = pPosition.x();
      double d1 = pPosition.y();
      double d2 = pPosition.z();
      if (pFacing.getAxis() == Direction.Axis.Y) {
         d1 = d1 - 0.125D;
      } else {
         d1 = d1 - 0.15625D;
      }

      ItemEntity itementity = new ItemEntity(pLevel, d0, d1, d2, pStack);
      double d3 = pLevel.random.nextDouble() * 0.1D + 0.2D;
      itementity.setDeltaMovement(pLevel.random.nextGaussian() * (double)0.0075F * (double)pSpeed + (double)pFacing.getStepX() * d3, pLevel.random.nextGaussian() * (double)0.0075F * (double)pSpeed + (double)0.2F, pLevel.random.nextGaussian() * (double)0.0075F * (double)pSpeed + (double)pFacing.getStepZ() * d3);
      pLevel.addFreshEntity(itementity);
   }

   /**
    * Play the dispense sound from the specified block.
    */
   protected void playSound(IBlockSource pSource) {
      pSource.getLevel().levelEvent(1000, pSource.getPos(), 0);
   }

   /**
    * Order clients to display dispense particles from the specified block and facing.
    */
   protected void playAnimation(IBlockSource pSource, Direction pFacing) {
      pSource.getLevel().levelEvent(2000, pSource.getPos(), pFacing.get3DDataValue());
   }
}