package net.minecraft.block.trees;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;

public abstract class BigTree extends Tree {
   public boolean growTree(ServerWorld pLevel, ChunkGenerator pChunkGenerator, BlockPos pPos, BlockState pState, Random pRand) {
      for(int i = 0; i >= -1; --i) {
         for(int j = 0; j >= -1; --j) {
            if (isTwoByTwoSapling(pState, pLevel, pPos, i, j)) {
               return this.placeMega(pLevel, pChunkGenerator, pPos, pState, pRand, i, j);
            }
         }
      }

      return super.growTree(pLevel, pChunkGenerator, pPos, pState, pRand);
   }

   /**
    * Get a {@link net.minecraft.world.gen.feature.ConfiguredFeature} of the huge variant of this tree
    */
   @Nullable
   protected abstract ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredMegaFeature(Random pRand);

   public boolean placeMega(ServerWorld pLevel, ChunkGenerator pChunkGenerator, BlockPos pPos, BlockState pState, Random pRandom, int pBranchX, int pBranchY) {
      ConfiguredFeature<BaseTreeFeatureConfig, ?> configuredfeature = this.getConfiguredMegaFeature(pRandom);
      if (configuredfeature == null) {
         return false;
      } else {
         configuredfeature.config.setFromSapling();
         BlockState blockstate = Blocks.AIR.defaultBlockState();
         pLevel.setBlock(pPos.offset(pBranchX, 0, pBranchY), blockstate, 4);
         pLevel.setBlock(pPos.offset(pBranchX + 1, 0, pBranchY), blockstate, 4);
         pLevel.setBlock(pPos.offset(pBranchX, 0, pBranchY + 1), blockstate, 4);
         pLevel.setBlock(pPos.offset(pBranchX + 1, 0, pBranchY + 1), blockstate, 4);
         if (configuredfeature.place(pLevel, pChunkGenerator, pRandom, pPos.offset(pBranchX, 0, pBranchY))) {
            return true;
         } else {
            pLevel.setBlock(pPos.offset(pBranchX, 0, pBranchY), pState, 4);
            pLevel.setBlock(pPos.offset(pBranchX + 1, 0, pBranchY), pState, 4);
            pLevel.setBlock(pPos.offset(pBranchX, 0, pBranchY + 1), pState, 4);
            pLevel.setBlock(pPos.offset(pBranchX + 1, 0, pBranchY + 1), pState, 4);
            return false;
         }
      }
   }

   public static boolean isTwoByTwoSapling(BlockState pBlockUnder, IBlockReader pLevel, BlockPos pPos, int pXOffset, int pZOffset) {
      Block block = pBlockUnder.getBlock();
      return block == pLevel.getBlockState(pPos.offset(pXOffset, 0, pZOffset)).getBlock() && block == pLevel.getBlockState(pPos.offset(pXOffset + 1, 0, pZOffset)).getBlock() && block == pLevel.getBlockState(pPos.offset(pXOffset, 0, pZOffset + 1)).getBlock() && block == pLevel.getBlockState(pPos.offset(pXOffset + 1, 0, pZOffset + 1)).getBlock();
   }
}