package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BigBrownMushroomFeature extends AbstractBigMushroomFeature {
   public BigBrownMushroomFeature(Codec<BigMushroomFeatureConfig> p_i231957_1_) {
      super(p_i231957_1_);
   }

   protected void makeCap(IWorld pLevel, Random pRandom, BlockPos pPos, int pTreeHeight, BlockPos.Mutable pMutablePos, BigMushroomFeatureConfig pConfig) {
      int i = pConfig.foliageRadius;

      for(int j = -i; j <= i; ++j) {
         for(int k = -i; k <= i; ++k) {
            boolean flag = j == -i;
            boolean flag1 = j == i;
            boolean flag2 = k == -i;
            boolean flag3 = k == i;
            boolean flag4 = flag || flag1;
            boolean flag5 = flag2 || flag3;
            if (!flag4 || !flag5) {
               pMutablePos.setWithOffset(pPos, j, pTreeHeight, k);
               if (pLevel.getBlockState(pMutablePos).canBeReplacedByLeaves(pLevel, pMutablePos)) {
                  boolean flag6 = flag || flag5 && j == 1 - i;
                  boolean flag7 = flag1 || flag5 && j == i - 1;
                  boolean flag8 = flag2 || flag4 && k == 1 - i;
                  boolean flag9 = flag3 || flag4 && k == i - 1;
                  this.setBlock(pLevel, pMutablePos, pConfig.capProvider.getState(pRandom, pPos).setValue(HugeMushroomBlock.WEST, Boolean.valueOf(flag6)).setValue(HugeMushroomBlock.EAST, Boolean.valueOf(flag7)).setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(flag8)).setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(flag9)));
               }
            }
         }
      }

   }

   protected int getTreeRadiusForHeight(int p_225563_1_, int p_225563_2_, int pFoliageRadius, int pY) {
      return pY <= 3 ? 0 : pFoliageRadius;
   }
}
