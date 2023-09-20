package net.minecraft.world.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ICarverConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilders;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeGenerationSettings {
   public static final Logger LOGGER = LogManager.getLogger();
   public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(() -> {
      return ConfiguredSurfaceBuilders.NOPE;
   }, ImmutableMap.of(), ImmutableList.of(), ImmutableList.of());
   public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec((p_242495_0_) -> {
      return p_242495_0_.group(ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder").forGetter((p_242501_0_) -> {
         return p_242501_0_.surfaceBuilder;
      }), Codec.simpleMap(GenerationStage.Carving.CODEC, ConfiguredCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)), IStringSerializable.keys(GenerationStage.Carving.values())).fieldOf("carvers").forGetter((p_242499_0_) -> {
         return p_242499_0_.carvers;
      }), ConfiguredFeature.LIST_CODEC.promotePartial(Util.prefix("Feature: ", LOGGER::error)).listOf().fieldOf("features").forGetter((p_242497_0_) -> {
         return p_242497_0_.features;
      }), StructureFeature.LIST_CODEC.promotePartial(Util.prefix("Structure start: ", LOGGER::error)).fieldOf("starts").forGetter((p_242488_0_) -> {
         return p_242488_0_.structureStarts;
      })).apply(p_242495_0_, BiomeGenerationSettings::new);
   });
   private final Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
   private final Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> carvers;
   private final java.util.Set<GenerationStage.Carving> carversView;
   private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features;
   private final List<Supplier<StructureFeature<?, ?>>> structureStarts;
   private final List<ConfiguredFeature<?, ?>> flowerFeatures;

   private BiomeGenerationSettings(Supplier<ConfiguredSurfaceBuilder<?>> p_i241935_1_, Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> p_i241935_2_, List<List<Supplier<ConfiguredFeature<?, ?>>>> p_i241935_3_, List<Supplier<StructureFeature<?, ?>>> p_i241935_4_) {
      this.surfaceBuilder = p_i241935_1_;
      this.carvers = p_i241935_2_;
      this.features = p_i241935_3_;
      this.structureStarts = p_i241935_4_;
      this.flowerFeatures = p_i241935_3_.stream().flatMap(Collection::stream).map(Supplier::get).flatMap(ConfiguredFeature::getFeatures).filter((p_242490_0_) -> {
         return p_242490_0_.feature == Feature.FLOWER;
      }).collect(ImmutableList.toImmutableList());
      this.carversView = java.util.Collections.unmodifiableSet(carvers.keySet());
   }

   public List<Supplier<ConfiguredCarver<?>>> getCarvers(GenerationStage.Carving pCarvingType) {
      return this.carvers.getOrDefault(pCarvingType, ImmutableList.of());
   }

   public java.util.Set<GenerationStage.Carving> getCarvingStages() {
       return this.carversView;
   }

   public boolean isValidStart(Structure<?> pStructure) {
      return this.structureStarts.stream().anyMatch((p_242494_1_) -> {
         return (p_242494_1_.get()).feature == pStructure;
      });
   }

   public Collection<Supplier<StructureFeature<?, ?>>> structures() {
      return this.structureStarts;
   }

   public StructureFeature<?, ?> withBiomeConfig(StructureFeature<?, ?> pStructure) {
      return DataFixUtils.orElse(this.structureStarts.stream().map(Supplier::get).filter((p_242492_1_) -> {
         return p_242492_1_.feature == pStructure.feature;
      }).findAny(), pStructure);
   }

   public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
      return this.flowerFeatures;
   }

   public List<List<Supplier<ConfiguredFeature<?, ?>>>> features() {
      return this.features;
   }

   public Supplier<ConfiguredSurfaceBuilder<?>> getSurfaceBuilder() {
      return this.surfaceBuilder;
   }

   public ISurfaceBuilderConfig getSurfaceBuilderConfig() {
      return this.surfaceBuilder.get().config();
   }

   public static class Builder {
      protected Optional<Supplier<ConfiguredSurfaceBuilder<?>>> surfaceBuilder = Optional.empty();
      protected final Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> carvers = Maps.newLinkedHashMap();
      protected final List<List<Supplier<ConfiguredFeature<?, ?>>>> features = Lists.newArrayList();
      protected final List<Supplier<StructureFeature<?, ?>>> structureStarts = Lists.newArrayList();

      public BiomeGenerationSettings.Builder surfaceBuilder(ConfiguredSurfaceBuilder<?> pConfiguredSurfaceBuilder) {
         return this.surfaceBuilder(() -> {
            return pConfiguredSurfaceBuilder;
         });
      }

      public BiomeGenerationSettings.Builder surfaceBuilder(Supplier<ConfiguredSurfaceBuilder<?>> pConfiguredSurfaceBuilderSupplier) {
         this.surfaceBuilder = Optional.of(pConfiguredSurfaceBuilderSupplier);
         return this;
      }

      public BiomeGenerationSettings.Builder addFeature(GenerationStage.Decoration pDecorationStage, ConfiguredFeature<?, ?> pFeature) {
         return this.addFeature(pDecorationStage.ordinal(), () -> {
            return pFeature;
         });
      }

      public BiomeGenerationSettings.Builder addFeature(int pStage, Supplier<ConfiguredFeature<?, ?>> pFeatures) {
         this.addFeatureStepsUpTo(pStage);
         this.features.get(pStage).add(pFeatures);
         return this;
      }

      public <C extends ICarverConfig> BiomeGenerationSettings.Builder addCarver(GenerationStage.Carving pCarvingStage, ConfiguredCarver<C> pCarver) {
         this.carvers.computeIfAbsent(pCarvingStage, (p_242511_0_) -> {
            return Lists.newArrayList();
         }).add(() -> {
            return pCarver;
         });
         return this;
      }

      public BiomeGenerationSettings.Builder addStructureStart(StructureFeature<?, ?> pStructure) {
         this.structureStarts.add(() -> {
            return pStructure;
         });
         return this;
      }

      protected void addFeatureStepsUpTo(int pStage) {
         while(this.features.size() <= pStage) {
            this.features.add(Lists.newArrayList());
         }

      }

      public BiomeGenerationSettings build() {
         return new BiomeGenerationSettings(this.surfaceBuilder.orElseThrow(() -> {
            return new IllegalStateException("Missing surface builder");
         }), this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (p_242518_0_) -> {
            return ImmutableList.copyOf((Collection)p_242518_0_.getValue());
         })), this.features.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList()), ImmutableList.copyOf(this.structureStarts));
      }
   }
}
