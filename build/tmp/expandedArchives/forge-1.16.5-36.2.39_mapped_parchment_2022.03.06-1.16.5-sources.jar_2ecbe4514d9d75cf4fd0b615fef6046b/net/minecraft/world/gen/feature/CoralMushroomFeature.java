package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class CoralMushroomFeature extends CoralFeature {
   public CoralMushroomFeature(Codec<NoFeatureConfig> p_i231941_1_) {
      super(p_i231941_1_);
   }

   protected boolean placeFeature(IWorld pLevel, Random pRandom, BlockPos pPos, BlockState pState) {
      int i = pRandom.nextInt(3) + 3;
      int j = pRandom.nextInt(3) + 3;
      int k = pRandom.nextInt(3) + 3;
      int l = pRandom.nextInt(3) + 1;
      BlockPos.Mutable blockpos$mutable = pPos.mutable();

      for(int i1 = 0; i1 <= j; ++i1) {
         for(int j1 = 0; j1 <= i; ++j1) {
            for(int k1 = 0; k1 <= k; ++k1) {
               blockpos$mutable.set(i1 + pPos.getX(), j1 + pPos.getY(), k1 + pPos.getZ());
               blockpos$mutable.move(Direction.DOWN, l);
               if ((i1 != 0 && i1 != j || j1 != 0 && j1 != i) && (k1 != 0 && k1 != k || j1 != 0 && j1 != i) && (i1 != 0 && i1 != j || k1 != 0 && k1 != k) && (i1 == 0 || i1 == j || j1 == 0 || j1 == i || k1 == 0 || k1 == k) && !(pRandom.nextFloat() < 0.1F) && !this.placeCoralBlock(pLevel, pRandom, blockpos$mutable, pState)) {
               }
            }
         }
      }

      return true;
   }
}