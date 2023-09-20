package net.minecraft.block;

import java.util.Random;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.NetherVegetationFeature;
import net.minecraft.world.gen.feature.TwistingVineFeature;
import net.minecraft.world.lighting.LightEngine;
import net.minecraft.world.server.ServerWorld;

public class NyliumBlock extends Block implements IGrowable {
   public NyliumBlock(AbstractBlock.Properties p_i241184_1_) {
      super(p_i241184_1_);
   }

   private static boolean canBeNylium(BlockState pState, IWorldReader pReader, BlockPos pPos) {
      BlockPos blockpos = pPos.above();
      BlockState blockstate = pReader.getBlockState(blockpos);
      int i = LightEngine.getLightBlockInto(pReader, pState, pPos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(pReader, blockpos));
      return i < pReader.getMaxLightLevel();
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (!canBeNylium(pState, pLevel, pPos)) {
         pLevel.setBlockAndUpdate(pPos, Blocks.NETHERRACK.defaultBlockState());
      }

   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return pLevel.getBlockState(pPos.above()).isAir();
   }

   public boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      BlockPos blockpos = pPos.above();
      if (blockstate.is(Blocks.CRIMSON_NYLIUM)) {
         NetherVegetationFeature.place(pLevel, pRand, blockpos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
      } else if (blockstate.is(Blocks.WARPED_NYLIUM)) {
         NetherVegetationFeature.place(pLevel, pRand, blockpos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
         NetherVegetationFeature.place(pLevel, pRand, blockpos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);
         if (pRand.nextInt(8) == 0) {
            TwistingVineFeature.place(pLevel, pRand, blockpos, 3, 1, 2);
         }
      }

   }
}