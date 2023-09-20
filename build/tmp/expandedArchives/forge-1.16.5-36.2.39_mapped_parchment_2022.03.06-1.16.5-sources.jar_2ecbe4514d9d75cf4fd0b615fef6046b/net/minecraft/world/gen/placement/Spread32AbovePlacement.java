package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class Spread32AbovePlacement extends Placement<NoPlacementConfig> {
   public Spread32AbovePlacement(Codec<NoPlacementConfig> p_i242031_1_) {
      super(p_i242031_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, NoPlacementConfig pConfig, BlockPos pPos) {
      int i = pRandom.nextInt(pPos.getY() + 32);
      return Stream.of(new BlockPos(pPos.getX(), i, pPos.getZ()));
   }
}