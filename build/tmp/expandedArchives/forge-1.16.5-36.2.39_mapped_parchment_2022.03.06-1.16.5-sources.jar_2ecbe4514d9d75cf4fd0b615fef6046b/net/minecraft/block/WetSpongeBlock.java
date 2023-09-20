package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WetSpongeBlock extends Block {
   public WetSpongeBlock(AbstractBlock.Properties p_i48294_1_) {
      super(p_i48294_1_);
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (pLevel.dimensionType().ultraWarm()) {
         pLevel.setBlock(pPos, Blocks.SPONGE.defaultBlockState(), 3);
         pLevel.levelEvent(2009, pPos, 0);
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, (1.0F + pLevel.getRandom().nextFloat() * 0.2F) * 0.7F);
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      Direction direction = Direction.getRandom(pRand);
      if (direction != Direction.UP) {
         BlockPos blockpos = pPos.relative(direction);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (!pState.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, direction.getOpposite())) {
            double d0 = (double)pPos.getX();
            double d1 = (double)pPos.getY();
            double d2 = (double)pPos.getZ();
            if (direction == Direction.DOWN) {
               d1 = d1 - 0.05D;
               d0 += pRand.nextDouble();
               d2 += pRand.nextDouble();
            } else {
               d1 = d1 + pRand.nextDouble() * 0.8D;
               if (direction.getAxis() == Direction.Axis.X) {
                  d2 += pRand.nextDouble();
                  if (direction == Direction.EAST) {
                     ++d0;
                  } else {
                     d0 += 0.05D;
                  }
               } else {
                  d0 += pRand.nextDouble();
                  if (direction == Direction.SOUTH) {
                     ++d2;
                  } else {
                     d2 += 0.05D;
                  }
               }
            }

            pLevel.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }
   }
}