package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class DefaultFlowersFeature extends FlowersFeature<BlockClusterFeatureConfig> {
   public DefaultFlowersFeature(Codec<BlockClusterFeatureConfig> p_i231945_1_) {
      super(p_i231945_1_);
   }

   public boolean isValid(IWorld pLevel, BlockPos pPos, BlockClusterFeatureConfig pConfig) {
      return !pConfig.blacklist.contains(pLevel.getBlockState(pPos));
   }

   public int getCount(BlockClusterFeatureConfig pConfig) {
      return pConfig.tries;
   }

   public BlockPos getPos(Random pRandom, BlockPos pPos, BlockClusterFeatureConfig pConfig) {
      return pPos.offset(pRandom.nextInt(pConfig.xspread) - pRandom.nextInt(pConfig.xspread), pRandom.nextInt(pConfig.yspread) - pRandom.nextInt(pConfig.yspread), pRandom.nextInt(pConfig.zspread) - pRandom.nextInt(pConfig.zspread));
   }

   public BlockState getRandomFlower(Random pRandom, BlockPos pPos, BlockClusterFeatureConfig pConfgi) {
      return pConfgi.stateProvider.getState(pRandom, pPos);
   }
}