package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class DecoratedPlacement extends Placement<DecoratedPlacementConfig> {
   public DecoratedPlacement(Codec<DecoratedPlacementConfig> p_i242019_1_) {
      super(p_i242019_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, DecoratedPlacementConfig pConfig, BlockPos pPos) {
      return pConfig.outer().getPositions(pHelper, pRandom, pPos).flatMap((p_242882_3_) -> {
         return pConfig.inner().getPositions(pHelper, pRandom, p_242882_3_);
      });
   }
}