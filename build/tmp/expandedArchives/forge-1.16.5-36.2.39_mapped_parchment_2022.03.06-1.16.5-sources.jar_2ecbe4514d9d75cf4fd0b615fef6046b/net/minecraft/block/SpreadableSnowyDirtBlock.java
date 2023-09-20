package net.minecraft.block;

import java.util.Random;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.lighting.LightEngine;
import net.minecraft.world.server.ServerWorld;

public abstract class SpreadableSnowyDirtBlock extends SnowyDirtBlock {
   protected SpreadableSnowyDirtBlock(AbstractBlock.Properties p_i48324_1_) {
      super(p_i48324_1_);
   }

   private static boolean canBeGrass(BlockState pState, IWorldReader pLevelReader, BlockPos pPos) {
      BlockPos blockpos = pPos.above();
      BlockState blockstate = pLevelReader.getBlockState(blockpos);
      if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowBlock.LAYERS) == 1) {
         return true;
      } else if (blockstate.getFluidState().getAmount() == 8) {
         return false;
      } else {
         int i = LightEngine.getLightBlockInto(pLevelReader, pState, pPos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(pLevelReader, blockpos));
         return i < pLevelReader.getMaxLightLevel();
      }
   }

   private static boolean canPropagate(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.above();
      return canBeGrass(pState, pLevel, pPos) && !pLevel.getFluidState(blockpos).is(FluidTags.WATER);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (!canBeGrass(pState, pLevel, pPos)) {
         if (!pLevel.isAreaLoaded(pPos, 3)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
         pLevel.setBlockAndUpdate(pPos, Blocks.DIRT.defaultBlockState());
      } else {
         if (pLevel.getMaxLocalRawBrightness(pPos.above()) >= 9) {
            BlockState blockstate = this.defaultBlockState();

            for(int i = 0; i < 4; ++i) {
               BlockPos blockpos = pPos.offset(pRandom.nextInt(3) - 1, pRandom.nextInt(5) - 3, pRandom.nextInt(3) - 1);
               if (pLevel.getBlockState(blockpos).is(Blocks.DIRT) && canPropagate(blockstate, pLevel, blockpos)) {
                  pLevel.setBlockAndUpdate(blockpos, blockstate.setValue(SNOWY, Boolean.valueOf(pLevel.getBlockState(blockpos.above()).is(Blocks.SNOW))));
               }
            }
         }

      }
   }
}
