package net.minecraft.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

public abstract class ProjectileDispenseBehavior extends DefaultDispenseItemBehavior {
   /**
    * Dispense the specified stack, play the dispense sound and spawn particles.
    */
   public ItemStack execute(IBlockSource pSource, ItemStack pStack) {
      World world = pSource.getLevel();
      IPosition iposition = DispenserBlock.getDispensePosition(pSource);
      Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
      ProjectileEntity projectileentity = this.getProjectile(world, iposition, pStack);
      projectileentity.shoot((double)direction.getStepX(), (double)((float)direction.getStepY() + 0.1F), (double)direction.getStepZ(), this.getPower(), this.getUncertainty());
      world.addFreshEntity(projectileentity);
      pStack.shrink(1);
      return pStack;
   }

   /**
    * Play the dispense sound from the specified block.
    */
   protected void playSound(IBlockSource pSource) {
      pSource.getLevel().levelEvent(1002, pSource.getPos(), 0);
   }

   /**
    * Return the projectile entity spawned by this dispense behavior.
    */
   protected abstract ProjectileEntity getProjectile(World pLevel, IPosition pPosition, ItemStack pStack);

   protected float getUncertainty() {
      return 6.0F;
   }

   protected float getPower() {
      return 1.1F;
   }
}