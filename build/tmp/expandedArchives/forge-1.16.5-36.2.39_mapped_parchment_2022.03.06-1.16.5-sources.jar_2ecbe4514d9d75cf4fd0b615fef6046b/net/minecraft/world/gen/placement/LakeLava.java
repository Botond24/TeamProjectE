package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class LakeLava extends Placement<ChanceConfig> {
   public LakeLava(Codec<ChanceConfig> p_i232089_1_) {
      super(p_i232089_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, ChanceConfig pConfig, BlockPos pPos) {
      if (pRandom.nextInt(pConfig.chance / 10) == 0) {
         int i = pRandom.nextInt(16) + pPos.getX();
         int j = pRandom.nextInt(16) + pPos.getZ();
         int k = pRandom.nextInt(pRandom.nextInt(pHelper.getGenDepth() - 8) + 8);
         if (k < pHelper.getSeaLevel() || pRandom.nextInt(pConfig.chance / 8) == 0) {
            return Stream.of(new BlockPos(i, k, j));
         }
      }

      return Stream.empty();
   }
}