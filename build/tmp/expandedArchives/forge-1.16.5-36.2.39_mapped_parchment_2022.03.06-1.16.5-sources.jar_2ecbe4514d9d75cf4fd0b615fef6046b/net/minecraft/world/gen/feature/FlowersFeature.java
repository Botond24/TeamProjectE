package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public abstract class FlowersFeature<U extends IFeatureConfig> extends Feature<U> {
   public FlowersFeature(Codec<U> p_i231922_1_) {
      super(p_i231922_1_);
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, U p_241855_5_) {
      BlockState blockstate = this.getRandomFlower(p_241855_3_, p_241855_4_, p_241855_5_);
      int i = 0;

      for(int j = 0; j < this.getCount(p_241855_5_); ++j) {
         BlockPos blockpos = this.getPos(p_241855_3_, p_241855_4_, p_241855_5_);
         if (p_241855_1_.isEmptyBlock(blockpos) && blockpos.getY() < 255 && blockstate.canSurvive(p_241855_1_, blockpos) && this.isValid(p_241855_1_, blockpos, p_241855_5_)) {
            p_241855_1_.setBlock(blockpos, blockstate, 2);
            ++i;
         }
      }

      return i > 0;
   }

   public abstract boolean isValid(IWorld pLevel, BlockPos pPos, U pConfig);

   public abstract int getCount(U pConfig);

   public abstract BlockPos getPos(Random pRandom, BlockPos pPos, U pConfig);

   public abstract BlockState getRandomFlower(Random pRandom, BlockPos pPos, U pConfgi);
}