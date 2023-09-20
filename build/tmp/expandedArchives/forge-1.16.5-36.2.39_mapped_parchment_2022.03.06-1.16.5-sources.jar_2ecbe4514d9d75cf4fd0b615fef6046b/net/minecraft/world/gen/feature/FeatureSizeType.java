package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.registry.Registry;

public class FeatureSizeType<P extends AbstractFeatureSizeType> {
   public static final FeatureSizeType<TwoLayerFeature> TWO_LAYERS_FEATURE_SIZE = register("two_layers_feature_size", TwoLayerFeature.CODEC);
   public static final FeatureSizeType<ThreeLayerFeature> THREE_LAYERS_FEATURE_SIZE = register("three_layers_feature_size", ThreeLayerFeature.CODEC);
   private final Codec<P> codec;

   private static <P extends AbstractFeatureSizeType> FeatureSizeType<P> register(String pName, Codec<P> pCodec) {
      return Registry.register(Registry.FEATURE_SIZE_TYPES, pName, new FeatureSizeType<>(pCodec));
   }

   private FeatureSizeType(Codec<P> pCodec) {
      this.codec = pCodec;
   }

   public Codec<P> codec() {
      return this.codec;
   }
}