package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class EndGateway extends Placement<NoPlacementConfig> {
   public EndGateway(Codec<NoPlacementConfig> p_i232084_1_) {
      super(p_i232084_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, NoPlacementConfig pConfig, BlockPos pPos) {
      if (pRandom.nextInt(700) == 0) {
         int i = pRandom.nextInt(16) + pPos.getX();
         int j = pRandom.nextInt(16) + pPos.getZ();
         int k = pHelper.getHeight(Heightmap.Type.MOTION_BLOCKING, i, j);
         if (k > 0) {
            int l = k + 3 + pRandom.nextInt(7);
            return Stream.of(new BlockPos(i, l, j));
         }
      }

      return Stream.empty();
   }
}