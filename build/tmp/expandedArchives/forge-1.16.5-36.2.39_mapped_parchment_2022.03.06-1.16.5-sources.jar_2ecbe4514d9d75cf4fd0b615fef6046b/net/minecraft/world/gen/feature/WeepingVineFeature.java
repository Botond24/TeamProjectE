package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.AbstractTopPlantBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public class WeepingVineFeature extends Feature<NoFeatureConfig> {
   private static final Direction[] DIRECTIONS = Direction.values();

   public WeepingVineFeature(Codec<NoFeatureConfig> p_i232004_1_) {
      super(p_i232004_1_);
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, NoFeatureConfig p_241855_5_) {
      if (!p_241855_1_.isEmptyBlock(p_241855_4_)) {
         return false;
      } else {
         BlockState blockstate = p_241855_1_.getBlockState(p_241855_4_.above());
         if (!blockstate.is(Blocks.NETHERRACK) && !blockstate.is(Blocks.NETHER_WART_BLOCK)) {
            return false;
         } else {
            this.placeRoofNetherWart(p_241855_1_, p_241855_3_, p_241855_4_);
            this.placeRoofWeepingVines(p_241855_1_, p_241855_3_, p_241855_4_);
            return true;
         }
      }
   }

   private void placeRoofNetherWart(IWorld pLevel, Random pRandom, BlockPos pPos) {
      pLevel.setBlock(pPos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      BlockPos.Mutable blockpos$mutable1 = new BlockPos.Mutable();

      for(int i = 0; i < 200; ++i) {
         blockpos$mutable.setWithOffset(pPos, pRandom.nextInt(6) - pRandom.nextInt(6), pRandom.nextInt(2) - pRandom.nextInt(5), pRandom.nextInt(6) - pRandom.nextInt(6));
         if (pLevel.isEmptyBlock(blockpos$mutable)) {
            int j = 0;

            for(Direction direction : DIRECTIONS) {
               BlockState blockstate = pLevel.getBlockState(blockpos$mutable1.setWithOffset(blockpos$mutable, direction));
               if (blockstate.is(Blocks.NETHERRACK) || blockstate.is(Blocks.NETHER_WART_BLOCK)) {
                  ++j;
               }

               if (j > 1) {
                  break;
               }
            }

            if (j == 1) {
               pLevel.setBlock(blockpos$mutable, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
            }
         }
      }

   }

   private void placeRoofWeepingVines(IWorld pLevel, Random pRandom, BlockPos pPos) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int i = 0; i < 100; ++i) {
         blockpos$mutable.setWithOffset(pPos, pRandom.nextInt(8) - pRandom.nextInt(8), pRandom.nextInt(2) - pRandom.nextInt(7), pRandom.nextInt(8) - pRandom.nextInt(8));
         if (pLevel.isEmptyBlock(blockpos$mutable)) {
            BlockState blockstate = pLevel.getBlockState(blockpos$mutable.above());
            if (blockstate.is(Blocks.NETHERRACK) || blockstate.is(Blocks.NETHER_WART_BLOCK)) {
               int j = MathHelper.nextInt(pRandom, 1, 8);
               if (pRandom.nextInt(6) == 0) {
                  j *= 2;
               }

               if (pRandom.nextInt(5) == 0) {
                  j = 1;
               }

               int k = 17;
               int l = 25;
               placeWeepingVinesColumn(pLevel, pRandom, blockpos$mutable, j, 17, 25);
            }
         }
      }

   }

   public static void placeWeepingVinesColumn(IWorld pLevel, Random pRandom, BlockPos.Mutable pPos, int pHeight, int pMinAge, int pMaxAge) {
      for(int i = 0; i <= pHeight; ++i) {
         if (pLevel.isEmptyBlock(pPos)) {
            if (i == pHeight || !pLevel.isEmptyBlock(pPos.below())) {
               pLevel.setBlock(pPos, Blocks.WEEPING_VINES.defaultBlockState().setValue(AbstractTopPlantBlock.AGE, Integer.valueOf(MathHelper.nextInt(pRandom, pMinAge, pMaxAge))), 2);
               break;
            }

            pLevel.setBlock(pPos, Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
         }

         pPos.move(Direction.DOWN);
      }

   }
}