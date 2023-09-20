package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;

public class ConfiguredRandomFeatureList {
   public static final Codec<ConfiguredRandomFeatureList> CODEC = RecordCodecBuilder.create((p_236433_0_) -> {
      return p_236433_0_.group(ConfiguredFeature.CODEC.fieldOf("feature").forGetter((p_242789_0_) -> {
         return p_242789_0_.feature;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter((p_236432_0_) -> {
         return p_236432_0_.chance;
      })).apply(p_236433_0_, ConfiguredRandomFeatureList::new);
   });
   public final Supplier<ConfiguredFeature<?, ?>> feature;
   public final float chance;

   public ConfiguredRandomFeatureList(ConfiguredFeature<?, ?> pFeature, float pChance) {
      this(() -> {
         return pFeature;
      }, pChance);
   }

   private ConfiguredRandomFeatureList(Supplier<ConfiguredFeature<?, ?>> p_i241980_1_, float p_i241980_2_) {
      this.feature = p_i241980_1_;
      this.chance = p_i241980_2_;
   }

   public boolean place(ISeedReader pReader, ChunkGenerator pChunkGenerator, Random pRandom, BlockPos pPos) {
      return this.feature.get().place(pReader, pChunkGenerator, pRandom, pPos);
   }
}