package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class NetherMagma extends Placement<NoPlacementConfig> {
   public NetherMagma(Codec<NoPlacementConfig> p_i232103_1_) {
      super(p_i232103_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, NoPlacementConfig pConfig, BlockPos pPos) {
      int i = pHelper.getSeaLevel();
      int j = i - 5 + pRandom.nextInt(10);
      return Stream.of(new BlockPos(pPos.getX(), j, pPos.getZ()));
   }
}