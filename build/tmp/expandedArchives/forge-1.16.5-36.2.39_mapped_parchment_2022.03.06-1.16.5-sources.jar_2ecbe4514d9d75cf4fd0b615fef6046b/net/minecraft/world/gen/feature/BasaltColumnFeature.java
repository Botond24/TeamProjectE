package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public class BasaltColumnFeature extends Feature<ColumnConfig> {
   private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(Blocks.LAVA, Blocks.BEDROCK, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);

   public BasaltColumnFeature(Codec<ColumnConfig> p_i231925_1_) {
      super(p_i231925_1_);
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, ColumnConfig p_241855_5_) {
      int i = p_241855_2_.getSeaLevel();
      if (!canPlaceAt(p_241855_1_, i, p_241855_4_.mutable())) {
         return false;
      } else {
         int j = p_241855_5_.height().sample(p_241855_3_);
         boolean flag = p_241855_3_.nextFloat() < 0.9F;
         int k = Math.min(j, flag ? 5 : 8);
         int l = flag ? 50 : 15;
         boolean flag1 = false;

         for(BlockPos blockpos : BlockPos.randomBetweenClosed(p_241855_3_, l, p_241855_4_.getX() - k, p_241855_4_.getY(), p_241855_4_.getZ() - k, p_241855_4_.getX() + k, p_241855_4_.getY(), p_241855_4_.getZ() + k)) {
            int i1 = j - blockpos.distManhattan(p_241855_4_);
            if (i1 >= 0) {
               flag1 |= this.placeColumn(p_241855_1_, i, blockpos, i1, p_241855_5_.reach().sample(p_241855_3_));
            }
         }

         return flag1;
      }
   }

   private boolean placeColumn(IWorld pLevel, int pSeaLevel, BlockPos pPos, int pDistance, int pReach) {
      boolean flag = false;

      for(BlockPos blockpos : BlockPos.betweenClosed(pPos.getX() - pReach, pPos.getY(), pPos.getZ() - pReach, pPos.getX() + pReach, pPos.getY(), pPos.getZ() + pReach)) {
         int i = blockpos.distManhattan(pPos);
         BlockPos blockpos1 = isAirOrLavaOcean(pLevel, pSeaLevel, blockpos) ? findSurface(pLevel, pSeaLevel, blockpos.mutable(), i) : findAir(pLevel, blockpos.mutable(), i);
         if (blockpos1 != null) {
            int j = pDistance - i / 2;

            for(BlockPos.Mutable blockpos$mutable = blockpos1.mutable(); j >= 0; --j) {
               if (isAirOrLavaOcean(pLevel, pSeaLevel, blockpos$mutable)) {
                  this.setBlock(pLevel, blockpos$mutable, Blocks.BASALT.defaultBlockState());
                  blockpos$mutable.move(Direction.UP);
                  flag = true;
               } else {
                  if (!pLevel.getBlockState(blockpos$mutable).is(Blocks.BASALT)) {
                     break;
                  }

                  blockpos$mutable.move(Direction.UP);
               }
            }
         }
      }

      return flag;
   }

   @Nullable
   private static BlockPos findSurface(IWorld pLevel, int pSeaLevel, BlockPos.Mutable pPos, int pDistance) {
      while(pPos.getY() > 1 && pDistance > 0) {
         --pDistance;
         if (canPlaceAt(pLevel, pSeaLevel, pPos)) {
            return pPos;
         }

         pPos.move(Direction.DOWN);
      }

      return null;
   }

   private static boolean canPlaceAt(IWorld pLevel, int pSeaLevel, BlockPos.Mutable pPos) {
      if (!isAirOrLavaOcean(pLevel, pSeaLevel, pPos)) {
         return false;
      } else {
         BlockState blockstate = pLevel.getBlockState(pPos.move(Direction.DOWN));
         pPos.move(Direction.UP);
         return !blockstate.isAir() && !CANNOT_PLACE_ON.contains(blockstate.getBlock());
      }
   }

   @Nullable
   private static BlockPos findAir(IWorld pLevel, BlockPos.Mutable pPos, int pDistance) {
      while(pPos.getY() < pLevel.getMaxBuildHeight() && pDistance > 0) {
         --pDistance;
         BlockState blockstate = pLevel.getBlockState(pPos);
         if (CANNOT_PLACE_ON.contains(blockstate.getBlock())) {
            return null;
         }

         if (blockstate.isAir()) {
            return pPos;
         }

         pPos.move(Direction.UP);
      }

      return null;
   }

   private static boolean isAirOrLavaOcean(IWorld pLevel, int pSeaLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return blockstate.isAir() || blockstate.is(Blocks.LAVA) && pPos.getY() <= pSeaLevel;
   }
}