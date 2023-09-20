package net.minecraft.util.registry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DynamicRegistries {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map<RegistryKey<? extends Registry<?>>, DynamicRegistries.CodecHolder<?>> REGISTRIES = Util.make(() -> {
      Builder<RegistryKey<? extends Registry<?>>, DynamicRegistries.CodecHolder<?>> builder = ImmutableMap.builder();
      put(builder, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
      put(builder, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
      put(builder, Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, ConfiguredSurfaceBuilder.DIRECT_CODEC);
      put(builder, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredCarver.DIRECT_CODEC);
      put(builder, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
      put(builder, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, StructureFeature.DIRECT_CODEC);
      put(builder, Registry.PROCESSOR_LIST_REGISTRY, IStructureProcessorType.DIRECT_CODEC);
      put(builder, Registry.TEMPLATE_POOL_REGISTRY, JigsawPattern.DIRECT_CODEC);
      put(builder, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DimensionSettings.DIRECT_CODEC);
      return builder.build();
   });
   private static final DynamicRegistries.Impl BUILTIN = Util.make(() -> {
      DynamicRegistries.Impl dynamicregistries$impl = new DynamicRegistries.Impl();
      DimensionType.registerBuiltin(dynamicregistries$impl);
      REGISTRIES.keySet().stream().filter((p_243616_0_) -> {
         return !p_243616_0_.equals(Registry.DIMENSION_TYPE_REGISTRY);
      }).forEach((p_243611_1_) -> {
         copyBuiltin(dynamicregistries$impl, p_243611_1_);
      });
      return dynamicregistries$impl;
   });

   public abstract <E> Optional<MutableRegistry<E>> registry(RegistryKey<? extends Registry<E>> p_230521_1_);

   public <E> MutableRegistry<E> registryOrThrow(RegistryKey<? extends Registry<E>> p_243612_1_) {
      return this.registry(p_243612_1_).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + p_243612_1_);
      });
   }

   public Registry<DimensionType> dimensionTypes() {
      return this.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
   }

   private static <E> void put(Builder<RegistryKey<? extends Registry<?>>, DynamicRegistries.CodecHolder<?>> pCodecHolder, RegistryKey<? extends Registry<E>> pRegistryKey, Codec<E> pCodec) {
      pCodecHolder.put(pRegistryKey, new DynamicRegistries.CodecHolder<>(pRegistryKey, pCodec, (Codec<E>)null));
   }

   private static <E> void put(Builder<RegistryKey<? extends Registry<?>>, DynamicRegistries.CodecHolder<?>> pCodecHolder, RegistryKey<? extends Registry<E>> pRegistryKey, Codec<E> pCodec, Codec<E> pCodec2) {
      pCodecHolder.put(pRegistryKey, new DynamicRegistries.CodecHolder<>(pRegistryKey, pCodec, pCodec2));
   }

   public static DynamicRegistries.Impl builtin() {
      DynamicRegistries.Impl dynamicregistries$impl = new DynamicRegistries.Impl();
      WorldSettingsImport.IResourceAccess.RegistryAccess worldsettingsimport$iresourceaccess$registryaccess = new WorldSettingsImport.IResourceAccess.RegistryAccess();

      for(DynamicRegistries.CodecHolder<?> codecholder : REGISTRIES.values()) {
         addBuiltinElements(dynamicregistries$impl, worldsettingsimport$iresourceaccess$registryaccess, codecholder);
      }

      WorldSettingsImport.create(JsonOps.INSTANCE, worldsettingsimport$iresourceaccess$registryaccess, dynamicregistries$impl);
      return dynamicregistries$impl;
   }

   private static <E> void addBuiltinElements(DynamicRegistries.Impl pRegistryHolder, WorldSettingsImport.IResourceAccess.RegistryAccess pRegistryAccess, DynamicRegistries.CodecHolder<E> pCodecHolder) {
      RegistryKey<? extends Registry<E>> registrykey = pCodecHolder.key();
      boolean flag = !registrykey.equals(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY) && !registrykey.equals(Registry.DIMENSION_TYPE_REGISTRY);
      Registry<E> registry = BUILTIN.registryOrThrow(registrykey);
      if (!registrykey.equals(Registry.DIMENSION_TYPE_REGISTRY))
         registry = ((Registry<Registry<E>>)WorldGenRegistries.REGISTRY).get((RegistryKey<Registry<E>>)registrykey);
      MutableRegistry<E> mutableregistry = pRegistryHolder.registryOrThrow(registrykey);

      for(Entry<RegistryKey<E>, E> entry : registry.entrySet()) {
         E e = entry.getValue();
         if (flag) {
            pRegistryAccess.add(BUILTIN, entry.getKey(), pCodecHolder.codec(), registry.getId(e), e, registry.lifecycle(e));
         } else {
            mutableregistry.registerMapping(registry.getId(e), entry.getKey(), e, registry.lifecycle(e));
         }
      }

   }

   private static <R extends Registry<?>> void copyBuiltin(DynamicRegistries.Impl pRegistryHolder, RegistryKey<R> pKey) {
      Registry<R> registry = (Registry<R>)WorldGenRegistries.REGISTRY;
      Registry<?> registry1 = registry.get(pKey);
      if (registry1 == null) {
         throw new IllegalStateException("Missing builtin registry: " + pKey);
      } else {
         copy(pRegistryHolder, registry1);
      }
   }

   private static <E> void copy(DynamicRegistries.Impl pRegistryHolder, Registry<E> pRegistry) {
      MutableRegistry<E> mutableregistry = pRegistryHolder.<E>registry(pRegistry.key()).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + pRegistry.key());
      });

      for(Entry<RegistryKey<E>, E> entry : pRegistry.entrySet()) {
         E e = entry.getValue();
         mutableregistry.registerMapping(pRegistry.getId(e), entry.getKey(), e, pRegistry.lifecycle(e));
      }

   }

   public static void load(DynamicRegistries.Impl p_243608_0_, WorldSettingsImport<?> p_243608_1_) {
      for(DynamicRegistries.CodecHolder<?> codecholder : REGISTRIES.values()) {
         readRegistry(p_243608_1_, p_243608_0_, codecholder);
      }

   }

   private static <E> void readRegistry(WorldSettingsImport<?> p_243610_0_, DynamicRegistries.Impl p_243610_1_, DynamicRegistries.CodecHolder<E> p_243610_2_) {
      RegistryKey<? extends Registry<E>> registrykey = p_243610_2_.key();
      SimpleRegistry<E> simpleregistry = Optional.ofNullable((SimpleRegistry<E>)p_243610_1_.registries.get(registrykey)).map((p_243604_0_) -> {
         return p_243604_0_;
      }).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + registrykey);
      });
      DataResult<SimpleRegistry<E>> dataresult = p_243610_0_.decodeElements(simpleregistry, p_243610_2_.key(), p_243610_2_.codec());
      dataresult.error().ifPresent((p_243603_0_) -> {
         LOGGER.error("Error loading registry data: {}", (Object)p_243603_0_.message());
      });
   }

   static final class CodecHolder<E> {
      private final RegistryKey<? extends Registry<E>> key;
      private final Codec<E> codec;
      @Nullable
      private final Codec<E> networkCodec;

      public CodecHolder(RegistryKey<? extends Registry<E>> pKey, Codec<E> pCodec, @Nullable Codec<E> pNetworkCodec) {
         this.key = pKey;
         this.codec = pCodec;
         this.networkCodec = pNetworkCodec;
      }

      public RegistryKey<? extends Registry<E>> key() {
         return this.key;
      }

      public Codec<E> codec() {
         return this.codec;
      }

      @Nullable
      public Codec<E> networkCodec() {
         return this.networkCodec;
      }

      public boolean sendToClient() {
         return this.networkCodec != null;
      }
   }

   public static final class Impl extends DynamicRegistries {
      public static final Codec<DynamicRegistries.Impl> NETWORK_CODEC = makeNetworkCodec();
      private final Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> registries;

      private static <E> Codec<DynamicRegistries.Impl> makeNetworkCodec() {
         Codec<RegistryKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(RegistryKey::createRegistryKey, RegistryKey::location);
         Codec<SimpleRegistry<E>> codec1 = codec.partialDispatch("type", (p_243634_0_) -> {
            return DataResult.success(p_243634_0_.key());
         }, (p_243640_0_) -> {
            return getNetworkCodec(p_243640_0_).map((p_243633_1_) -> {
               return SimpleRegistry.networkCodec(p_243640_0_, Lifecycle.experimental(), p_243633_1_);
            });
         });
         UnboundedMapCodec<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> unboundedmapcodec = Codec.unboundedMap(codec, codec1);
         return captureMap(unboundedmapcodec);
      }

      private static <K extends RegistryKey<? extends Registry<?>>, V extends SimpleRegistry<?>> Codec<DynamicRegistries.Impl> captureMap(UnboundedMapCodec<K, V> pUnboundedMapCodec) {
         return pUnboundedMapCodec.xmap(DynamicRegistries.Impl::new, (p_243635_0_) -> {
            return ((java.util.Set<Map.Entry<K, V>>)(Object)(p_243635_0_.registries.entrySet())).stream().filter((p_243632_0_) -> {
               return DynamicRegistries.REGISTRIES.get(p_243632_0_.getKey()).sendToClient();
            }).collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
         });
      }

      private static <E> DataResult<? extends Codec<E>> getNetworkCodec(RegistryKey<? extends Registry<E>> pRegistryKey) {
         return Optional.ofNullable((CodecHolder<E>)DynamicRegistries.REGISTRIES.get(pRegistryKey)).map((p_243630_0_) -> {
            return p_243630_0_.networkCodec();
         }).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown or not serializable registry: " + pRegistryKey);
         });
      }

      public Impl() {
         this(DynamicRegistries.REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), DynamicRegistries.Impl::createRegistry)));
      }

      private Impl(Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> p_i242074_1_) {
         this.registries = p_i242074_1_;
      }

      private static <E> SimpleRegistry<?> createRegistry(RegistryKey<? extends Registry<?>> p_243638_0_) {
         return new SimpleRegistry(p_243638_0_, Lifecycle.stable());
      }

      public <E> Optional<MutableRegistry<E>> registry(RegistryKey<? extends Registry<E>> p_230521_1_) {
         return Optional.ofNullable((MutableRegistry<E>)this.registries.get(p_230521_1_)).map((p_243629_0_) -> {
            return p_243629_0_;
         });
      }
   }
}
