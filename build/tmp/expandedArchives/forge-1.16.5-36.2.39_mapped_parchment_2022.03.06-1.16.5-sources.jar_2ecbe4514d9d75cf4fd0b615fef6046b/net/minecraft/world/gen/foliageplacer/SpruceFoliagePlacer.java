package net.minecraft.world.gen.foliageplacer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.FeatureSpread;

public class SpruceFoliagePlacer extends FoliagePlacer {
   public static final Codec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.create((p_242836_0_) -> {
      return foliagePlacerParts(p_242836_0_).and(FeatureSpread.codec(0, 16, 8).fieldOf("trunk_height").forGetter((p_242835_0_) -> {
         return p_242835_0_.trunkHeight;
      })).apply(p_242836_0_, SpruceFoliagePlacer::new);
   });
   private final FeatureSpread trunkHeight;

   public SpruceFoliagePlacer(FeatureSpread p_i242003_1_, FeatureSpread p_i242003_2_, FeatureSpread p_i242003_3_) {
      super(p_i242003_1_, p_i242003_2_);
      this.trunkHeight = p_i242003_3_;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
   }

   protected void createFoliage(IWorldGenerationReader p_230372_1_, Random p_230372_2_, BaseTreeFeatureConfig p_230372_3_, int p_230372_4_, FoliagePlacer.Foliage p_230372_5_, int p_230372_6_, int p_230372_7_, Set<BlockPos> p_230372_8_, int p_230372_9_, MutableBoundingBox p_230372_10_) {
      BlockPos blockpos = p_230372_5_.foliagePos();
      int i = p_230372_2_.nextInt(2);
      int j = 1;
      int k = 0;

      for(int l = p_230372_9_; l >= -p_230372_6_; --l) {
         this.placeLeavesRow(p_230372_1_, p_230372_2_, p_230372_3_, blockpos, i, p_230372_8_, l, p_230372_5_.doubleTrunk(), p_230372_10_);
         if (i >= j) {
            i = k;
            k = 1;
            j = Math.min(j + 1, p_230372_7_ + p_230372_5_.radiusOffset());
         } else {
            ++i;
         }
      }

   }

   public int foliageHeight(Random pRandom, int pHeight, BaseTreeFeatureConfig pConfig) {
      return Math.max(4, pHeight - this.trunkHeight.sample(pRandom));
   }

   protected boolean shouldSkipLocation(Random pRandom, int p_230373_2_, int p_230373_3_, int p_230373_4_, int p_230373_5_, boolean p_230373_6_) {
      return p_230373_2_ == p_230373_5_ && p_230373_4_ == p_230373_5_ && p_230373_5_ > 0;
   }
}