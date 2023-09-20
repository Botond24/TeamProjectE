package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public class HugeFungusFeature extends Feature<HugeFungusConfig> {
   public HugeFungusFeature(Codec<HugeFungusConfig> p_i231959_1_) {
      super(p_i231959_1_);
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, HugeFungusConfig p_241855_5_) {
      Block block = p_241855_5_.validBaseState.getBlock();
      BlockPos blockpos = null;
      Block block1 = p_241855_1_.getBlockState(p_241855_4_.below()).getBlock();
      if (block1 == block) {
         blockpos = p_241855_4_;
      }

      if (blockpos == null) {
         return false;
      } else {
         int i = MathHelper.nextInt(p_241855_3_, 4, 13);
         if (p_241855_3_.nextInt(12) == 0) {
            i *= 2;
         }

         if (!p_241855_5_.planted) {
            int j = p_241855_2_.getGenDepth();
            if (blockpos.getY() + i + 1 >= j) {
               return false;
            }
         }

         boolean flag = !p_241855_5_.planted && p_241855_3_.nextFloat() < 0.06F;
         p_241855_1_.setBlock(p_241855_4_, Blocks.AIR.defaultBlockState(), 4);
         this.placeStem(p_241855_1_, p_241855_3_, p_241855_5_, blockpos, i, flag);
         this.placeHat(p_241855_1_, p_241855_3_, p_241855_5_, blockpos, i, flag);
         return true;
      }
   }

   private static boolean isReplaceable(IWorld pLevel, BlockPos pPos, boolean pReplacePlants) {
      return pLevel.isStateAtPosition(pPos, (p_236320_1_) -> {
         Material material = p_236320_1_.getMaterial();
         return p_236320_1_.getMaterial().isReplaceable() || pReplacePlants && material == Material.PLANT;
      });
   }

   private void placeStem(IWorld pLevel, Random pRandom, HugeFungusConfig pConfig, BlockPos pPos, int pHeight, boolean pDoubleWide) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      BlockState blockstate = pConfig.stemState;
      int i = pDoubleWide ? 1 : 0;

      for(int j = -i; j <= i; ++j) {
         for(int k = -i; k <= i; ++k) {
            boolean flag = pDoubleWide && MathHelper.abs(j) == i && MathHelper.abs(k) == i;

            for(int l = 0; l < pHeight; ++l) {
               blockpos$mutable.setWithOffset(pPos, j, l, k);
               if (isReplaceable(pLevel, blockpos$mutable, true)) {
                  if (pConfig.planted) {
                     if (!pLevel.getBlockState(blockpos$mutable.below()).isAir()) {
                        pLevel.destroyBlock(blockpos$mutable, true);
                     }

                     pLevel.setBlock(blockpos$mutable, blockstate, 3);
                  } else if (flag) {
                     if (pRandom.nextFloat() < 0.1F) {
                        this.setBlock(pLevel, blockpos$mutable, blockstate);
                     }
                  } else {
                     this.setBlock(pLevel, blockpos$mutable, blockstate);
                  }
               }
            }
         }
      }

   }

   private void placeHat(IWorld pLevel, Random pRandom, HugeFungusConfig pConfig, BlockPos pPos, int pHeight, boolean pDoubleWide) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      boolean flag = pConfig.hatState.is(Blocks.NETHER_WART_BLOCK);
      int i = Math.min(pRandom.nextInt(1 + pHeight / 3) + 5, pHeight);
      int j = pHeight - i;

      for(int k = j; k <= pHeight; ++k) {
         int l = k < pHeight - pRandom.nextInt(3) ? 2 : 1;
         if (i > 8 && k < j + 4) {
            l = 3;
         }

         if (pDoubleWide) {
            ++l;
         }

         for(int i1 = -l; i1 <= l; ++i1) {
            for(int j1 = -l; j1 <= l; ++j1) {
               boolean flag1 = i1 == -l || i1 == l;
               boolean flag2 = j1 == -l || j1 == l;
               boolean flag3 = !flag1 && !flag2 && k != pHeight;
               boolean flag4 = flag1 && flag2;
               boolean flag5 = k < j + 3;
               blockpos$mutable.setWithOffset(pPos, i1, k, j1);
               if (isReplaceable(pLevel, blockpos$mutable, false)) {
                  if (pConfig.planted && !pLevel.getBlockState(blockpos$mutable.below()).isAir()) {
                     pLevel.destroyBlock(blockpos$mutable, true);
                  }

                  if (flag5) {
                     if (!flag3) {
                        this.placeHatDropBlock(pLevel, pRandom, blockpos$mutable, pConfig.hatState, flag);
                     }
                  } else if (flag3) {
                     this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutable, 0.1F, 0.2F, flag ? 0.1F : 0.0F);
                  } else if (flag4) {
                     this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutable, 0.01F, 0.7F, flag ? 0.083F : 0.0F);
                  } else {
                     this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutable, 5.0E-4F, 0.98F, flag ? 0.07F : 0.0F);
                  }
               }
            }
         }
      }

   }

   private void placeHatBlock(IWorld pLevel, Random pRandom, HugeFungusConfig pConfig, BlockPos.Mutable pPos, float pDecorationChance, float pHatChance, float pWeepingVineChance) {
      if (pRandom.nextFloat() < pDecorationChance) {
         this.setBlock(pLevel, pPos, pConfig.decorState);
      } else if (pRandom.nextFloat() < pHatChance) {
         this.setBlock(pLevel, pPos, pConfig.hatState);
         if (pRandom.nextFloat() < pWeepingVineChance) {
            tryPlaceWeepingVines(pPos, pLevel, pRandom);
         }
      }

   }

   private void placeHatDropBlock(IWorld pLevel, Random pRandom, BlockPos pPos, BlockState pState, boolean pWeepingVines) {
      if (pLevel.getBlockState(pPos.below()).is(pState.getBlock())) {
         this.setBlock(pLevel, pPos, pState);
      } else if ((double)pRandom.nextFloat() < 0.15D) {
         this.setBlock(pLevel, pPos, pState);
         if (pWeepingVines && pRandom.nextInt(11) == 0) {
            tryPlaceWeepingVines(pPos, pLevel, pRandom);
         }
      }

   }

   private static void tryPlaceWeepingVines(BlockPos pPos, IWorld pLevel, Random pRandom) {
      BlockPos.Mutable blockpos$mutable = pPos.mutable().move(Direction.DOWN);
      if (pLevel.isEmptyBlock(blockpos$mutable)) {
         int i = MathHelper.nextInt(pRandom, 1, 5);
         if (pRandom.nextInt(7) == 0) {
            i *= 2;
         }

         int j = 23;
         int k = 25;
         WeepingVineFeature.placeWeepingVinesColumn(pLevel, pRandom, blockpos$mutable, i, 23, 25);
      }
   }
}