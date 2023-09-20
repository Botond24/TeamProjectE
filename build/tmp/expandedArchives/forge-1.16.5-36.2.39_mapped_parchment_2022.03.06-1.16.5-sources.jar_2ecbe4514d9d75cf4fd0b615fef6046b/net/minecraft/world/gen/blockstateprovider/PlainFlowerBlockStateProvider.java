package net.minecraft.world.gen.blockstateprovider;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class PlainFlowerBlockStateProvider extends BlockStateProvider {
   public static final Codec<PlainFlowerBlockStateProvider> CODEC;
   public static final PlainFlowerBlockStateProvider INSTANCE = new PlainFlowerBlockStateProvider();
   private static final BlockState[] LOW_NOISE_FLOWERS = new BlockState[]{Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState()};
   private static final BlockState[] HIGH_NOISE_FLOWERS = new BlockState[]{Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState()};

   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.PLAIN_FLOWER_PROVIDER;
   }

   public BlockState getState(Random pRandom, BlockPos pBlockPos) {
      double d0 = Biome.BIOME_INFO_NOISE.getValue((double)pBlockPos.getX() / 200.0D, (double)pBlockPos.getZ() / 200.0D, false);
      if (d0 < -0.8D) {
         return Util.getRandom(LOW_NOISE_FLOWERS, pRandom);
      } else {
         return pRandom.nextInt(3) > 0 ? Util.getRandom(HIGH_NOISE_FLOWERS, pRandom) : Blocks.DANDELION.defaultBlockState();
      }
   }

   static {
      CODEC = Codec.unit(() -> {
         return INSTANCE;
      });
   }
}