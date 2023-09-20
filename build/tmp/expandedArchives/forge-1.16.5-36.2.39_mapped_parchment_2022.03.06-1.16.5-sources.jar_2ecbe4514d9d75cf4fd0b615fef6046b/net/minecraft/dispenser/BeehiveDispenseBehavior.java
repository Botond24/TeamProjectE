package net.minecraft.dispenser;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.IShearable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BeehiveDispenseBehavior extends OptionalDispenseBehavior {
   /**
    * Dispense the specified stack, play the dispense sound and spawn particles.
    */
   protected ItemStack execute(IBlockSource pSource, ItemStack pStack) {
      World world = pSource.getLevel();
      if (!world.isClientSide()) {
         BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
         this.setSuccess(tryShearBeehive((ServerWorld)world, blockpos) || tryShearLivingEntity((ServerWorld)world, blockpos));
         if (this.isSuccess() && pStack.hurt(1, world.getRandom(), (ServerPlayerEntity)null)) {
            pStack.setCount(0);
         }
      }

      return pStack;
   }

   private static boolean tryShearBeehive(ServerWorld pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (blockstate.is(BlockTags.BEEHIVES)) {
         int i = blockstate.getValue(BeehiveBlock.HONEY_LEVEL);
         if (i >= 5) {
            pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
            BeehiveBlock.dropHoneycomb(pLevel, pPos);
            ((BeehiveBlock)blockstate.getBlock()).releaseBeesAndResetHoneyLevel(pLevel, blockstate, pPos, (PlayerEntity)null, BeehiveTileEntity.State.BEE_RELEASED);
            return true;
         }
      }

      return false;
   }

   private static boolean tryShearLivingEntity(ServerWorld pLevel, BlockPos pPos) {
      for(LivingEntity livingentity : pLevel.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(pPos), EntityPredicates.NO_SPECTATORS)) {
         if (livingentity instanceof IShearable) {
            IShearable ishearable = (IShearable)livingentity;
            if (ishearable.readyForShearing()) {
               ishearable.shear(SoundCategory.BLOCKS);
               return true;
            }
         }
      }

      return false;
   }
}