package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public abstract class AbstractBigMushroomFeature extends Feature<BigMushroomFeatureConfig> {
   public AbstractBigMushroomFeature(Codec<BigMushroomFeatureConfig> p_i231923_1_) {
      super(p_i231923_1_);
   }

   protected void placeTrunk(IWorld pLevel, Random pRandom, BlockPos pPos, BigMushroomFeatureConfig pConfig, int pMaxHeight, BlockPos.Mutable pMutablePos) {
      for(int i = 0; i < pMaxHeight; ++i) {
         pMutablePos.set(pPos).move(Direction.UP, i);
         if (pLevel.getBlockState(pMutablePos).canBeReplacedByLogs(pLevel, pMutablePos)) {
            this.setBlock(pLevel, pMutablePos, pConfig.stemProvider.getState(pRandom, pPos));
         }
      }

   }

   protected int getTreeHeight(Random pRandom) {
      int i = pRandom.nextInt(3) + 4;
      if (pRandom.nextInt(12) == 0) {
         i *= 2;
      }

      return i;
   }

   protected boolean isValidPosition(IWorld pLevel, BlockPos pPos, int pMaxHeight, BlockPos.Mutable pMutablePos, BigMushroomFeatureConfig pConfig) {
      int i = pPos.getY();
      if (i >= 1 && i + pMaxHeight + 1 < 256) {
         Block block = pLevel.getBlockState(pPos.below()).getBlock();
         if (!isDirt(block) && !block.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return false;
         } else {
            for(int j = 0; j <= pMaxHeight; ++j) {
               int k = this.getTreeRadiusForHeight(-1, -1, pConfig.foliageRadius, j);

               for(int l = -k; l <= k; ++l) {
                  for(int i1 = -k; i1 <= k; ++i1) {
                     BlockState blockstate = pLevel.getBlockState(pMutablePos.setWithOffset(pPos, l, j, i1));
                     if (!blockstate.isAir(pLevel, pMutablePos.setWithOffset(pPos, l, j, i1)) && !blockstate.is(BlockTags.LEAVES)) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, BigMushroomFeatureConfig p_241855_5_) {
      int i = this.getTreeHeight(p_241855_3_);
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      if (!this.isValidPosition(p_241855_1_, p_241855_4_, i, blockpos$mutable, p_241855_5_)) {
         return false;
      } else {
         this.makeCap(p_241855_1_, p_241855_3_, p_241855_4_, i, blockpos$mutable, p_241855_5_);
         this.placeTrunk(p_241855_1_, p_241855_3_, p_241855_4_, p_241855_5_, i, blockpos$mutable);
         return true;
      }
   }

   protected abstract int getTreeRadiusForHeight(int p_225563_1_, int p_225563_2_, int pFoliageRadius, int pY);

   protected abstract void makeCap(IWorld pLevel, Random pRandom, BlockPos pPos, int pTreeHeight, BlockPos.Mutable pMutablePos, BigMushroomFeatureConfig pConfig);
}
