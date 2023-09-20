package net.minecraft.block.trees;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;

public abstract class Tree {
   /**
    * Get a {@link net.minecraft.world.gen.feature.ConfiguredFeature} of tree
    */
   @Nullable
   protected abstract ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random pRandom, boolean pLargeHive);

   public boolean growTree(ServerWorld pLevel, ChunkGenerator pChunkGenerator, BlockPos pPos, BlockState pState, Random pRand) {
      ConfiguredFeature<BaseTreeFeatureConfig, ?> configuredfeature = this.getConfiguredFeature(pRand, this.hasFlowers(pLevel, pPos));
      if (configuredfeature == null) {
         return false;
      } else {
         pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 4);
         configuredfeature.config.setFromSapling();
         if (configuredfeature.place(pLevel, pChunkGenerator, pRand, pPos)) {
            return true;
         } else {
            pLevel.setBlock(pPos, pState, 4);
            return false;
         }
      }
   }

   private boolean hasFlowers(IWorld pLevel, BlockPos pPos) {
      for(BlockPos blockpos : BlockPos.Mutable.betweenClosed(pPos.below().north(2).west(2), pPos.above().south(2).east(2))) {
         if (pLevel.getBlockState(blockpos).is(BlockTags.FLOWERS)) {
            return true;
         }
      }

      return false;
   }
}