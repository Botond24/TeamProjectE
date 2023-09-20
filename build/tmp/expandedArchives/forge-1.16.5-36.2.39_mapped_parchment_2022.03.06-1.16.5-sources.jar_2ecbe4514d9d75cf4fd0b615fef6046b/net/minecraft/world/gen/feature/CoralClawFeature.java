package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class CoralClawFeature extends CoralFeature {
   public CoralClawFeature(Codec<NoFeatureConfig> p_i231939_1_) {
      super(p_i231939_1_);
   }

   protected boolean placeFeature(IWorld pLevel, Random pRandom, BlockPos pPos, BlockState pState) {
      if (!this.placeCoralBlock(pLevel, pRandom, pPos, pState)) {
         return false;
      } else {
         Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
         int i = pRandom.nextInt(2) + 2;
         List<Direction> list = Lists.newArrayList(direction, direction.getClockWise(), direction.getCounterClockWise());
         Collections.shuffle(list, pRandom);

         for(Direction direction1 : list.subList(0, i)) {
            BlockPos.Mutable blockpos$mutable = pPos.mutable();
            int j = pRandom.nextInt(2) + 1;
            blockpos$mutable.move(direction1);
            int k;
            Direction direction2;
            if (direction1 == direction) {
               direction2 = direction;
               k = pRandom.nextInt(3) + 2;
            } else {
               blockpos$mutable.move(Direction.UP);
               Direction[] adirection = new Direction[]{direction1, Direction.UP};
               direction2 = Util.getRandom(adirection, pRandom);
               k = pRandom.nextInt(3) + 3;
            }

            for(int l = 0; l < j && this.placeCoralBlock(pLevel, pRandom, blockpos$mutable, pState); ++l) {
               blockpos$mutable.move(direction2);
            }

            blockpos$mutable.move(direction2.getOpposite());
            blockpos$mutable.move(Direction.UP);

            for(int i1 = 0; i1 < k; ++i1) {
               blockpos$mutable.move(direction);
               if (!this.placeCoralBlock(pLevel, pRandom, blockpos$mutable, pState)) {
                  break;
               }

               if (pRandom.nextFloat() < 0.25F) {
                  blockpos$mutable.move(Direction.UP);
               }
            }
         }

         return true;
      }
   }
}