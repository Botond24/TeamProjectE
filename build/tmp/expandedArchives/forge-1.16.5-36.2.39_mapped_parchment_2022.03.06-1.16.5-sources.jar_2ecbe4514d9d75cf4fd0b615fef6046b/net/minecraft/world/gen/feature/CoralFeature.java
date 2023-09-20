package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public abstract class CoralFeature extends Feature<NoFeatureConfig> {
   public CoralFeature(Codec<NoFeatureConfig> p_i231940_1_) {
      super(p_i231940_1_);
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, NoFeatureConfig p_241855_5_) {
      BlockState blockstate = BlockTags.CORAL_BLOCKS.getRandomElement(p_241855_3_).defaultBlockState();
      return this.placeFeature(p_241855_1_, p_241855_3_, p_241855_4_, blockstate);
   }

   protected abstract boolean placeFeature(IWorld pLevel, Random pRandom, BlockPos pPos, BlockState pState);

   protected boolean placeCoralBlock(IWorld pLevel, Random pRandom, BlockPos pPos, BlockState pState) {
      BlockPos blockpos = pPos.above();
      BlockState blockstate = pLevel.getBlockState(pPos);
      if ((blockstate.is(Blocks.WATER) || blockstate.is(BlockTags.CORALS)) && pLevel.getBlockState(blockpos).is(Blocks.WATER)) {
         pLevel.setBlock(pPos, pState, 3);
         if (pRandom.nextFloat() < 0.25F) {
            pLevel.setBlock(blockpos, BlockTags.CORALS.getRandomElement(pRandom).defaultBlockState(), 2);
         } else if (pRandom.nextFloat() < 0.05F) {
            pLevel.setBlock(blockpos, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(pRandom.nextInt(4) + 1)), 2);
         }

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (pRandom.nextFloat() < 0.2F) {
               BlockPos blockpos1 = pPos.relative(direction);
               if (pLevel.getBlockState(blockpos1).is(Blocks.WATER)) {
                  BlockState blockstate1 = BlockTags.WALL_CORALS.getRandomElement(pRandom).defaultBlockState().setValue(DeadCoralWallFanBlock.FACING, direction);
                  pLevel.setBlock(blockpos1, blockstate1, 2);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}