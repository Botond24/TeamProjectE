package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class DarkOakTreePlacement extends HeightmapBasedPlacement<NoPlacementConfig> {
   public DarkOakTreePlacement(Codec<NoPlacementConfig> p_i232082_1_) {
      super(p_i232082_1_);
   }

   protected Heightmap.Type type(NoPlacementConfig p_241858_1_) {
      return Heightmap.Type.MOTION_BLOCKING;
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, NoPlacementConfig pConfig, BlockPos pPos) {
      return IntStream.range(0, 16).mapToObj((p_242881_5_) -> {
         int i = p_242881_5_ / 4;
         int j = p_242881_5_ % 4;
         int k = i * 4 + 1 + pRandom.nextInt(3) + pPos.getX();
         int l = j * 4 + 1 + pRandom.nextInt(3) + pPos.getZ();
         int i1 = pHelper.getHeight(this.type(pConfig), k, l);
         return new BlockPos(k, i1, l);
      });
   }
}