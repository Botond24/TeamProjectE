package net.minecraft.util.registry;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DelegatingDynamicOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldSettingsImport<T> extends DelegatingDynamicOps<T> {
   private static final Logger LOGGER = LogManager.getLogger();
   private final WorldSettingsImport.IResourceAccess resources;
   private final DynamicRegistries.Impl registryHolder;
   private final Map<RegistryKey<? extends Registry<?>>, WorldSettingsImport.ResultMap<?>> readCache;
   private final WorldSettingsImport<JsonElement> jsonOps;

   public static <T> WorldSettingsImport<T> create(DynamicOps<T> p_244335_0_, IResourceManager p_244335_1_, DynamicRegistries.Impl p_244335_2_) {
      return create(p_244335_0_, WorldSettingsImport.IResourceAccess.forResourceManager(p_244335_1_), p_244335_2_);
   }

   public static <T> WorldSettingsImport<T> create(DynamicOps<T> p_244336_0_, WorldSettingsImport.IResourceAccess p_244336_1_, DynamicRegistries.Impl p_244336_2_) {
      WorldSettingsImport<T> worldsettingsimport = new WorldSettingsImport<>(p_244336_0_, p_244336_1_, p_244336_2_, Maps.newIdentityHashMap());
      DynamicRegistries.load(p_244336_2_, worldsettingsimport);
      return worldsettingsimport;
   }

   private WorldSettingsImport(DynamicOps<T> p_i242092_1_, WorldSettingsImport.IResourceAccess p_i242092_2_, DynamicRegistries.Impl p_i242092_3_, IdentityHashMap<RegistryKey<? extends Registry<?>>, WorldSettingsImport.ResultMap<?>> p_i242092_4_) {
      super(p_i242092_1_);
      this.resources = p_i242092_2_;
      this.registryHolder = p_i242092_3_;
      this.readCache = p_i242092_4_;
      this.jsonOps = p_i242092_1_ == JsonOps.INSTANCE ? (WorldSettingsImport<JsonElement>)this : new WorldSettingsImport<>(JsonOps.INSTANCE, p_i242092_2_, p_i242092_3_, p_i242092_4_);
   }

   protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T pInput, RegistryKey<? extends Registry<E>> pRegistryKey, Codec<E> pMapCodec, boolean pAllowInlineDefinitions) {
      Optional<MutableRegistry<E>> optional = this.registryHolder.registry(pRegistryKey);
      if (!optional.isPresent()) {
         return DataResult.error("Unknown registry: " + pRegistryKey);
      } else {
         MutableRegistry<E> mutableregistry = optional.get();
         DataResult<Pair<ResourceLocation, T>> dataresult = ResourceLocation.CODEC.decode(this.delegate, pInput);
         if (!dataresult.result().isPresent()) {
            return !pAllowInlineDefinitions ? DataResult.error("Inline definitions not allowed here") : pMapCodec.decode(this, pInput).map((p_240874_0_) -> {
               return p_240874_0_.mapFirst((p_240891_0_) -> {
                  return () -> {
                     return p_240891_0_;
                  };
               });
            });
         } else {
            Pair<ResourceLocation, T> pair = dataresult.result().get();
            ResourceLocation resourcelocation = pair.getFirst();
            return this.readAndRegisterElement(pRegistryKey, mutableregistry, pMapCodec, resourcelocation).map((p_240875_1_) -> {
               return Pair.of(p_240875_1_, pair.getSecond());
            });
         }
      }
   }

   public <E> DataResult<SimpleRegistry<E>> decodeElements(SimpleRegistry<E> pSimpleRegistry, RegistryKey<? extends Registry<E>> pRegistryKey, Codec<E> pMapCodec) {
      Collection<ResourceLocation> collection = this.resources.listResources(pRegistryKey);
      DataResult<SimpleRegistry<E>> dataresult = DataResult.success(pSimpleRegistry, Lifecycle.stable());
      String s = pRegistryKey.location().getPath() + "/";

      for(ResourceLocation resourcelocation : collection) {
         String s1 = resourcelocation.getPath();
         if (!s1.endsWith(".json")) {
            LOGGER.warn("Skipping resource {} since it is not a json file", (Object)resourcelocation);
         } else if (!s1.startsWith(s)) {
            LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", (Object)resourcelocation);
         } else {
            String s2 = s1.substring(s.length(), s1.length() - ".json".length());
            ResourceLocation resourcelocation1 = new ResourceLocation(resourcelocation.getNamespace(), s2);
            dataresult = dataresult.flatMap((p_240885_4_) -> {
               return this.readAndRegisterElement(pRegistryKey, p_240885_4_, pMapCodec, resourcelocation1).map((p_240877_1_) -> {
                  return p_240885_4_;
               });
            });
         }
      }

      return dataresult.setPartial(pSimpleRegistry);
   }

   private <E> DataResult<Supplier<E>> readAndRegisterElement(RegistryKey<? extends Registry<E>> pRegistryKey, MutableRegistry<E> pMutableRegistry, Codec<E> pMapCodec, ResourceLocation pId) {
      RegistryKey<E> registrykey = RegistryKey.create(pRegistryKey, pId);
      WorldSettingsImport.ResultMap<E> resultmap = this.readCache(pRegistryKey);
      DataResult<Supplier<E>> dataresult = resultmap.values.get(registrykey);
      if (dataresult != null) {
         return dataresult;
      } else {
         Supplier<E> supplier = Suppliers.memoize(() -> {
            E e = pMutableRegistry.get(registrykey);
            if (e == null) {
               throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + registrykey);
            } else {
               return e;
            }
         });
         resultmap.values.put(registrykey, DataResult.success(supplier));
         DataResult<Pair<E, OptionalInt>> dataresult1 = this.resources.parseElement(this.jsonOps, pRegistryKey, registrykey, pMapCodec);
         Optional<Pair<E, OptionalInt>> optional = dataresult1.result();
         if (optional.isPresent()) {
            Pair<E, OptionalInt> pair = optional.get();
            pMutableRegistry.registerOrOverride(pair.getSecond(), registrykey, pair.getFirst(), dataresult1.lifecycle());
         }

         DataResult<Supplier<E>> dataresult2;
         if (!optional.isPresent() && pMutableRegistry.get(registrykey) != null) {
            dataresult2 = DataResult.success(() -> {
               return pMutableRegistry.get(registrykey);
            }, Lifecycle.stable());
         } else {
            dataresult2 = dataresult1.map((p_244339_2_) -> {
               return () -> {
                  return pMutableRegistry.get(registrykey);
               };
            });
         }

         resultmap.values.put(registrykey, dataresult2);
         return dataresult2;
      }
   }

   private <E> WorldSettingsImport.ResultMap<E> readCache(RegistryKey<? extends Registry<E>> pKey) {
      return (WorldSettingsImport.ResultMap<E>)this.readCache.computeIfAbsent(pKey, (p_244344_0_) -> {
         return new WorldSettingsImport.ResultMap();
      });
   }

   protected <E> DataResult<Registry<E>> registry(RegistryKey<? extends Registry<E>> pRegistryKey) {
      return (DataResult)this.registryHolder.registry(pRegistryKey).map((p_244337_0_) -> {
         return DataResult.success(p_244337_0_, p_244337_0_.elementsLifecycle());
      }).orElseGet(() -> {
         return DataResult.error("Unknown registry: " + pRegistryKey);
      });
   }

   public interface IResourceAccess {
      Collection<ResourceLocation> listResources(RegistryKey<? extends Registry<?>> pRegistryKey);

      <E> DataResult<Pair<E, OptionalInt>> parseElement(DynamicOps<JsonElement> p_241879_1_, RegistryKey<? extends Registry<E>> p_241879_2_, RegistryKey<E> p_241879_3_, Decoder<E> p_241879_4_);

      static WorldSettingsImport.IResourceAccess forResourceManager(final IResourceManager pManager) {
         return new WorldSettingsImport.IResourceAccess() {
            public Collection<ResourceLocation> listResources(RegistryKey<? extends Registry<?>> pRegistryKey) {
               return pManager.listResources(pRegistryKey.location().getPath(), (p_244348_0_) -> {
                  return p_244348_0_.endsWith(".json");
               });
            }

            public <E> DataResult<Pair<E, OptionalInt>> parseElement(DynamicOps<JsonElement> p_241879_1_, RegistryKey<? extends Registry<E>> p_241879_2_, RegistryKey<E> p_241879_3_, Decoder<E> p_241879_4_) {
               ResourceLocation resourcelocation = p_241879_3_.location();
               ResourceLocation resourcelocation1 = new ResourceLocation(resourcelocation.getNamespace(), p_241879_2_.location().getPath() + "/" + resourcelocation.getPath() + ".json");

               try (
                  IResource iresource = pManager.getResource(resourcelocation1);
                  Reader reader = new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8);
               ) {
                  JsonParser jsonparser = new JsonParser();
                  JsonElement jsonelement = jsonparser.parse(reader);
                  if (jsonelement!= null) jsonelement.getAsJsonObject().addProperty("forge:registry_name", p_241879_3_.location().toString());
                  return p_241879_4_.parse(p_241879_1_, jsonelement).map((p_244347_0_) -> {
                     return Pair.of(p_244347_0_, OptionalInt.empty());
                  });
               } catch (JsonIOException | JsonSyntaxException | IOException ioexception) {
                  return DataResult.error("Failed to parse " + resourcelocation1 + " file: " + ioexception.getMessage());
               }
            }

            public String toString() {
               return "ResourceAccess[" + pManager + "]";
            }
         };
      }

      public static final class RegistryAccess implements WorldSettingsImport.IResourceAccess {
         private final Map<RegistryKey<?>, JsonElement> data = Maps.newIdentityHashMap();
         private final Object2IntMap<RegistryKey<?>> ids = new Object2IntOpenCustomHashMap<>(Util.identityStrategy());
         private final Map<RegistryKey<?>, Lifecycle> lifecycles = Maps.newIdentityHashMap();

         public <E> void add(DynamicRegistries.Impl pDynamicRegistries, RegistryKey<E> pKey, Encoder<E> pEncoder, int pId, E pInstance, Lifecycle pLifecycle) {
            DataResult<JsonElement> dataresult = pEncoder.encodeStart(WorldGenSettingsExport.create(JsonOps.INSTANCE, pDynamicRegistries), pInstance);
            Optional<PartialResult<JsonElement>> optional = dataresult.error();
            if (optional.isPresent()) {
               WorldSettingsImport.LOGGER.error("Error adding element: {}", (Object)optional.get().message());
            } else {
               this.data.put(pKey, dataresult.result().get());
               this.ids.put(pKey, pId);
               this.lifecycles.put(pKey, pLifecycle);
            }
         }

         public Collection<ResourceLocation> listResources(RegistryKey<? extends Registry<?>> pRegistryKey) {
            return this.data.keySet().stream().filter((p_244355_1_) -> {
               return p_244355_1_.isFor(pRegistryKey);
            }).map((p_244354_1_) -> {
               return new ResourceLocation(p_244354_1_.location().getNamespace(), pRegistryKey.location().getPath() + "/" + p_244354_1_.location().getPath() + ".json");
            }).collect(Collectors.toList());
         }

         public <E> DataResult<Pair<E, OptionalInt>> parseElement(DynamicOps<JsonElement> p_241879_1_, RegistryKey<? extends Registry<E>> p_241879_2_, RegistryKey<E> p_241879_3_, Decoder<E> p_241879_4_) {
            JsonElement jsonelement = this.data.get(p_241879_3_);
            if (jsonelement!= null) jsonelement.getAsJsonObject().addProperty("forge:registry_name", p_241879_3_.location().toString());
            return jsonelement == null ? DataResult.error("Unknown element: " + p_241879_3_) : p_241879_4_.parse(p_241879_1_, jsonelement).setLifecycle(this.lifecycles.get(p_241879_3_)).map((p_244353_2_) -> {
               return Pair.of(p_244353_2_, OptionalInt.of(this.ids.getInt(p_241879_3_)));
            });
         }
      }
   }

   static final class ResultMap<E> {
      private final Map<RegistryKey<E>, DataResult<Supplier<E>>> values = Maps.newIdentityHashMap();

      private ResultMap() {
      }
   }
}
