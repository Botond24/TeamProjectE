package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class LakeWater extends Placement<ChanceConfig> {
   public LakeWater(Codec<ChanceConfig> p_i232090_1_) {
      super(p_i232090_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, ChanceConfig pConfig, BlockPos pPos) {
      if (pRandom.nextInt(pConfig.chance) == 0) {
         int i = pRandom.nextInt(16) + pPos.getX();
         int j = pRandom.nextInt(16) + pPos.getZ();
         int k = pRandom.nextInt(pHelper.getGenDepth());
         return Stream.of(new BlockPos(i, k, j));
      } else {
         return Stream.empty();
      }
   }
}