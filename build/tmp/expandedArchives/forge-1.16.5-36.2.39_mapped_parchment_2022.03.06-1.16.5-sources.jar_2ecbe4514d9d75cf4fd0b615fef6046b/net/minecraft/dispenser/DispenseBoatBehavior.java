package net.minecraft.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DispenseBoatBehavior extends DefaultDispenseItemBehavior {
   private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
   private final BoatEntity.Type type;

   public DispenseBoatBehavior(BoatEntity.Type pType) {
      this.type = pType;
   }

   /**
    * Dispense the specified stack, play the dispense sound and spawn particles.
    */
   public ItemStack execute(IBlockSource pSource, ItemStack pStack) {
      Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
      World world = pSource.getLevel();
      double d0 = pSource.x() + (double)((float)direction.getStepX() * 1.125F);
      double d1 = pSource.y() + (double)((float)direction.getStepY() * 1.125F);
      double d2 = pSource.z() + (double)((float)direction.getStepZ() * 1.125F);
      BlockPos blockpos = pSource.getPos().relative(direction);
      double d3;
      if (world.getFluidState(blockpos).is(FluidTags.WATER)) {
         d3 = 1.0D;
      } else {
         if (!world.getBlockState(blockpos).isAir() || !world.getFluidState(blockpos.below()).is(FluidTags.WATER)) {
            return this.defaultDispenseItemBehavior.dispense(pSource, pStack);
         }

         d3 = 0.0D;
      }

      BoatEntity boatentity = new BoatEntity(world, d0, d1 + d3, d2);
      boatentity.setType(this.type);
      boatentity.yRot = direction.toYRot();
      world.addFreshEntity(boatentity);
      pStack.shrink(1);
      return pStack;
   }

   /**
    * Play the dispense sound from the specified block.
    */
   protected void playSound(IBlockSource pSource) {
      pSource.getLevel().levelEvent(1000, pSource.getPos(), 0);
   }
}