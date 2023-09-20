package net.minecraft.block;

import java.util.Random;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CryingObsidianBlock extends Block {
   public CryingObsidianBlock(AbstractBlock.Properties p_i241176_1_) {
      super(p_i241176_1_);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pRand.nextInt(5) == 0) {
         Direction direction = Direction.getRandom(pRand);
         if (direction != Direction.UP) {
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            if (!pState.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, direction.getOpposite())) {
               double d0 = direction.getStepX() == 0 ? pRand.nextDouble() : 0.5D + (double)direction.getStepX() * 0.6D;
               double d1 = direction.getStepY() == 0 ? pRand.nextDouble() : 0.5D + (double)direction.getStepY() * 0.6D;
               double d2 = direction.getStepZ() == 0 ? pRand.nextDouble() : 0.5D + (double)direction.getStepZ() * 0.6D;
               pLevel.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, 0.0D, 0.0D, 0.0D);
            }
         }
      }
   }
}