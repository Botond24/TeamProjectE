package net.minecraft.world.gen.placement;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.FeatureSpreadConfig;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class CountMultilayerPlacement extends Placement<FeatureSpreadConfig> {
   public CountMultilayerPlacement(Codec<FeatureSpreadConfig> p_i242034_1_) {
      super(p_i242034_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, FeatureSpreadConfig pConfig, BlockPos pPos) {
      List<BlockPos> list = Lists.newArrayList();
      int i = 0;

      boolean flag;
      do {
         flag = false;

         for(int j = 0; j < pConfig.count().sample(pRandom); ++j) {
            int k = pRandom.nextInt(16) + pPos.getX();
            int l = pRandom.nextInt(16) + pPos.getZ();
            int i1 = pHelper.getHeight(Heightmap.Type.MOTION_BLOCKING, k, l);
            int j1 = findOnGroundYPosition(pHelper, k, i1, l, i);
            if (j1 != Integer.MAX_VALUE) {
               list.add(new BlockPos(k, j1, l));
               flag = true;
            }
         }

         ++i;
      } while(flag);

      return list.stream();
   }

   private static int findOnGroundYPosition(WorldDecoratingHelper pContext, int pX, int pY, int pZ, int p_242915_4_) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(pX, pY, pZ);
      int i = 0;
      BlockState blockstate = pContext.getBlockState(blockpos$mutable);

      for(int j = pY; j >= 1; --j) {
         blockpos$mutable.setY(j - 1);
         BlockState blockstate1 = pContext.getBlockState(blockpos$mutable);
         if (!isEmpty(blockstate1) && isEmpty(blockstate) && !blockstate1.is(Blocks.BEDROCK)) {
            if (i == p_242915_4_) {
               return blockpos$mutable.getY() + 1;
            }

            ++i;
         }

         blockstate = blockstate1;
      }

      return Integer.MAX_VALUE;
   }

   private static boolean isEmpty(BlockState pState) {
      return pState.isAir() || pState.is(Blocks.WATER) || pState.is(Blocks.LAVA);
   }
}