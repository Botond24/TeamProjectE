package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public class BlockPileFeature extends Feature<BlockStateProvidingFeatureConfig> {
   public BlockPileFeature(Codec<BlockStateProvidingFeatureConfig> p_i231932_1_) {
      super(p_i231932_1_);
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, BlockStateProvidingFeatureConfig p_241855_5_) {
      if (p_241855_4_.getY() < 5) {
         return false;
      } else {
         int i = 2 + p_241855_3_.nextInt(2);
         int j = 2 + p_241855_3_.nextInt(2);

         for(BlockPos blockpos : BlockPos.betweenClosed(p_241855_4_.offset(-i, 0, -j), p_241855_4_.offset(i, 1, j))) {
            int k = p_241855_4_.getX() - blockpos.getX();
            int l = p_241855_4_.getZ() - blockpos.getZ();
            if ((float)(k * k + l * l) <= p_241855_3_.nextFloat() * 10.0F - p_241855_3_.nextFloat() * 6.0F) {
               this.tryPlaceBlock(p_241855_1_, blockpos, p_241855_3_, p_241855_5_);
            } else if ((double)p_241855_3_.nextFloat() < 0.031D) {
               this.tryPlaceBlock(p_241855_1_, blockpos, p_241855_3_, p_241855_5_);
            }
         }

         return true;
      }
   }

   private boolean mayPlaceOn(IWorld pLevel, BlockPos pPos, Random pRandom) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos);
      return blockstate.is(Blocks.GRASS_PATH) ? pRandom.nextBoolean() : blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP);
   }

   private void tryPlaceBlock(IWorld pLevel, BlockPos pPos, Random pRandom, BlockStateProvidingFeatureConfig pConfig) {
      if (pLevel.isEmptyBlock(pPos) && this.mayPlaceOn(pLevel, pPos, pRandom)) {
         pLevel.setBlock(pPos, pConfig.stateProvider.getState(pRandom, pPos), 4);
      }

   }
}