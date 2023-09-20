package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class CountNoisePlacement extends Placement<NoiseDependant> {
   public CountNoisePlacement(Codec<NoiseDependant> p_i242017_1_) {
      super(p_i242017_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, NoiseDependant pConfig, BlockPos pPos) {
      double d0 = Biome.BIOME_INFO_NOISE.getValue((double)pPos.getX() / 200.0D, (double)pPos.getZ() / 200.0D, false);
      int i = d0 < pConfig.noiseLevel ? pConfig.belowNoise : pConfig.aboveNoise;
      return IntStream.range(0, i).mapToObj((p_242879_1_) -> {
         return pPos;
      });
   }
}