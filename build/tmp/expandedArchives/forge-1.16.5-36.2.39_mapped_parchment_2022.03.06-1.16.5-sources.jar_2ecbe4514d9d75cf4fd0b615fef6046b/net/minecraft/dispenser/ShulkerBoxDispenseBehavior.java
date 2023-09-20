package net.minecraft.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class ShulkerBoxDispenseBehavior extends OptionalDispenseBehavior {
   /**
    * Dispense the specified stack, play the dispense sound and spawn particles.
    */
   protected ItemStack execute(IBlockSource pSource, ItemStack pStack) {
      this.setSuccess(false);
      Item item = pStack.getItem();
      if (item instanceof BlockItem) {
         Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
         BlockPos blockpos = pSource.getPos().relative(direction);
         Direction direction1 = pSource.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
         this.setSuccess(((BlockItem)item).place(new DirectionalPlaceContext(pSource.getLevel(), blockpos, direction, pStack, direction1)).consumesAction());
      }

      return pStack;
   }
}