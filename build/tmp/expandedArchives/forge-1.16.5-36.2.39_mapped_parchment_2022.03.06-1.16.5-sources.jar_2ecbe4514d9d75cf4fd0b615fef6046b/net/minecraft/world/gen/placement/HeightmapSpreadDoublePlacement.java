package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class HeightmapSpreadDoublePlacement<DC extends IPlacementConfig> extends HeightmapBasedPlacement<DC> {
   public HeightmapSpreadDoublePlacement(Codec<DC> p_i242027_1_) {
      super(p_i242027_1_);
   }

   protected Heightmap.Type type(DC p_241858_1_) {
      return Heightmap.Type.MOTION_BLOCKING;
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, DC pConfig, BlockPos pPos) {
      int i = pPos.getX();
      int j = pPos.getZ();
      int k = pHelper.getHeight(this.type(pConfig), i, j);
      return k == 0 ? Stream.of() : Stream.of(new BlockPos(i, pRandom.nextInt(k * 2), j));
   }
}