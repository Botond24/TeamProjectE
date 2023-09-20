package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BigRedMushroomFeature extends AbstractBigMushroomFeature {
   public BigRedMushroomFeature(Codec<BigMushroomFeatureConfig> p_i231960_1_) {
      super(p_i231960_1_);
   }

   protected void makeCap(IWorld pLevel, Random pRandom, BlockPos pPos, int pTreeHeight, BlockPos.Mutable pMutablePos, BigMushroomFeatureConfig pConfig) {
      for(int i = pTreeHeight - 3; i <= pTreeHeight; ++i) {
         int j = i < pTreeHeight ? pConfig.foliageRadius : pConfig.foliageRadius - 1;
         int k = pConfig.foliageRadius - 2;

         for(int l = -j; l <= j; ++l) {
            for(int i1 = -j; i1 <= j; ++i1) {
               boolean flag = l == -j;
               boolean flag1 = l == j;
               boolean flag2 = i1 == -j;
               boolean flag3 = i1 == j;
               boolean flag4 = flag || flag1;
               boolean flag5 = flag2 || flag3;
               if (i >= pTreeHeight || flag4 != flag5) {
                  pMutablePos.setWithOffset(pPos, l, i, i1);
                  if (pLevel.getBlockState(pMutablePos).canBeReplacedByLeaves(pLevel, pMutablePos)) {
                     this.setBlock(pLevel, pMutablePos, pConfig.capProvider.getState(pRandom, pPos).setValue(HugeMushroomBlock.UP, Boolean.valueOf(i >= pTreeHeight - 1)).setValue(HugeMushroomBlock.WEST, Boolean.valueOf(l < -k)).setValue(HugeMushroomBlock.EAST, Boolean.valueOf(l > k)).setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(i1 < -k)).setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(i1 > k)));
                  }
               }
            }
         }
      }

   }

   protected int getTreeRadiusForHeight(int p_225563_1_, int p_225563_2_, int pFoliageRadius, int pY) {
      int i = 0;
      if (pY < p_225563_2_ && pY >= p_225563_2_ - 3) {
         i = pFoliageRadius;
      } else if (pY == p_225563_2_) {
         i = pFoliageRadius;
      }

      return i;
   }
}
