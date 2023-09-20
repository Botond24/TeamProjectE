package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public class NetherVegetationFeature extends Feature<BlockStateProvidingFeatureConfig> {
   public NetherVegetationFeature(Codec<BlockStateProvidingFeatureConfig> p_i231971_1_) {
      super(p_i231971_1_);
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, BlockStateProvidingFeatureConfig p_241855_5_) {
      return place(p_241855_1_, p_241855_3_, p_241855_4_, p_241855_5_, 8, 4);
   }

   public static boolean place(IWorld pLevel, Random pRandom, BlockPos pPos, BlockStateProvidingFeatureConfig pConfig, int pHorizontalRadius, int pVerticalRadius) {
      Block block = pLevel.getBlockState(pPos.below()).getBlock();
      if (!block.is(BlockTags.NYLIUM)) {
         return false;
      } else {
         int i = pPos.getY();
         if (i >= 1 && i + 1 < 256) {
            int j = 0;

            for(int k = 0; k < pHorizontalRadius * pHorizontalRadius; ++k) {
               BlockPos blockpos = pPos.offset(pRandom.nextInt(pHorizontalRadius) - pRandom.nextInt(pHorizontalRadius), pRandom.nextInt(pVerticalRadius) - pRandom.nextInt(pVerticalRadius), pRandom.nextInt(pHorizontalRadius) - pRandom.nextInt(pHorizontalRadius));
               BlockState blockstate = pConfig.stateProvider.getState(pRandom, blockpos);
               if (pLevel.isEmptyBlock(blockpos) && blockpos.getY() > 0 && blockstate.canSurvive(pLevel, blockpos)) {
                  pLevel.setBlock(blockpos, blockstate, 2);
                  ++j;
               }
            }

            return j > 0;
         } else {
            return false;
         }
      }
   }
}