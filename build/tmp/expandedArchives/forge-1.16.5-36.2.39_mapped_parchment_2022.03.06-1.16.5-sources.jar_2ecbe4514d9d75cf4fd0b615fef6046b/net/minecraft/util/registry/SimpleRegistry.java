package net.minecraft.util.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleRegistry<T> extends MutableRegistry<T> {
   protected static final Logger LOGGER = LogManager.getLogger();
   private final ObjectList<T> byId = new ObjectArrayList<>(256);
   private final Object2IntMap<T> toId = new Object2IntOpenCustomHashMap<>(Util.identityStrategy());
   private final BiMap<ResourceLocation, T> storage;
   private final BiMap<RegistryKey<T>, T> keyStorage;
   private final Map<T, Lifecycle> lifecycles;
   private Lifecycle elementsLifecycle;
   protected Object[] randomCache;
   private int nextId;

   public SimpleRegistry(RegistryKey<? extends Registry<T>> p_i232509_1_, Lifecycle p_i232509_2_) {
      super(p_i232509_1_, p_i232509_2_);
      this.toId.defaultReturnValue(-1);
      this.storage = HashBiMap.create();
      this.keyStorage = HashBiMap.create();
      this.lifecycles = Maps.newIdentityHashMap();
      this.elementsLifecycle = p_i232509_2_;
   }

   public static <T> MapCodec<SimpleRegistry.Entry<T>> withNameAndId(RegistryKey<? extends Registry<T>> pRegistryKey, MapCodec<T> pMapCodec) {
      return RecordCodecBuilder.mapCodec((p_243542_2_) -> {
         return p_243542_2_.group(ResourceLocation.CODEC.xmap(RegistryKey.elementKey(pRegistryKey), RegistryKey::location).fieldOf("name").forGetter((p_243545_0_) -> {
            return p_243545_0_.key;
         }), Codec.INT.fieldOf("id").forGetter((p_243543_0_) -> {
            return p_243543_0_.id;
         }), pMapCodec.forGetter((p_243538_0_) -> {
            return p_243538_0_.value;
         })).apply(p_243542_2_, SimpleRegistry.Entry::new);
      });
   }

   public <V extends T> V registerMapping(int pId, RegistryKey<T> pName, V pInstance, Lifecycle pLifecycle) {
      return this.registerMapping(pId, pName, pInstance, pLifecycle, true);
   }

   private <V extends T> V registerMapping(int pId, RegistryKey<T> pKey, V pValue, Lifecycle pLifecycle, boolean pLogDuplicateKeys) {
      Validate.notNull(pKey);
      Validate.notNull((T)pValue);
      this.byId.size(Math.max(this.byId.size(), pId + 1));
      this.byId.set(pId, pValue);
      this.toId.put((T)pValue, pId);
      this.randomCache = null;
      if (pLogDuplicateKeys && this.keyStorage.containsKey(pKey)) {
         LOGGER.debug("Adding duplicate key '{}' to registry", (Object)pKey);
      }

      if (this.storage.containsValue(pValue)) {
         LOGGER.error("Adding duplicate value '{}' to registry", pValue);
      }

      this.storage.put(pKey.location(), (T)pValue);
      this.keyStorage.put(pKey, (T)pValue);
      this.lifecycles.put((T)pValue, pLifecycle);
      this.elementsLifecycle = this.elementsLifecycle.add(pLifecycle);
      if (this.nextId <= pId) {
         this.nextId = pId + 1;
      }

      return pValue;
   }

   public <V extends T> V register(RegistryKey<T> pName, V pInstance, Lifecycle pLifecycle) {
      return this.registerMapping(this.nextId, pName, pInstance, pLifecycle);
   }

   public <V extends T> V registerOrOverride(OptionalInt pIndex, RegistryKey<T> pRegistryKey, V pValue, Lifecycle pLifecycle) {
      Validate.notNull(pRegistryKey);
      Validate.notNull((T)pValue);
      T t = this.keyStorage.get(pRegistryKey);
      int i;
      if (t == null) {
         i = pIndex.isPresent() ? pIndex.getAsInt() : this.nextId;
      } else {
         i = this.toId.getInt(t);
         if (pIndex.isPresent() && pIndex.getAsInt() != i) {
            throw new IllegalStateException("ID mismatch");
         }

         this.toId.removeInt(t);
         this.lifecycles.remove(t);
      }

      return this.registerMapping(i, pRegistryKey, pValue, pLifecycle, false);
   }

   /**
    * Gets the name we use to identify the given object.
    */
   @Nullable
   public ResourceLocation getKey(T pValue) {
      return this.storage.inverse().get(pValue);
   }

   public Optional<RegistryKey<T>> getResourceKey(T pValue) {
      return Optional.ofNullable(this.keyStorage.inverse().get(pValue));
   }

   /**
    * Gets the integer ID we use to identify the given object.
    */
   public int getId(@Nullable T pValue) {
      return this.toId.getInt(pValue);
   }

   @Nullable
   public T get(@Nullable RegistryKey<T> pKey) {
      return this.keyStorage.get(pKey);
   }

   @Nullable
   public T byId(int pValue) {
      return (T)(pValue >= 0 && pValue < this.byId.size() ? this.byId.get(pValue) : null);
   }

   public Lifecycle lifecycle(T pObject) {
      return this.lifecycles.get(pObject);
   }

   public Lifecycle elementsLifecycle() {
      return this.elementsLifecycle;
   }

   public Iterator<T> iterator() {
      return Iterators.filter(this.byId.iterator(), Objects::nonNull);
   }

   @Nullable
   public T get(@Nullable ResourceLocation pName) {
      return this.storage.get(pName);
   }

   /**
    * Gets all the keys recognized by this registry.
    */
   public Set<ResourceLocation> keySet() {
      return Collections.unmodifiableSet(this.storage.keySet());
   }

   public Set<Map.Entry<RegistryKey<T>, T>> entrySet() {
      return Collections.unmodifiableMap(this.keyStorage).entrySet();
   }

   @Nullable
   public T getRandom(Random p_186801_1_) {
      if (this.randomCache == null) {
         Collection<?> collection = this.storage.values();
         if (collection.isEmpty()) {
            return (T)null;
         }

         this.randomCache = collection.toArray(new Object[collection.size()]);
      }

      return Util.getRandom((T[])this.randomCache, p_186801_1_);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean containsKey(ResourceLocation pName) {
      return this.storage.containsKey(pName);
   }

   public static <T> Codec<SimpleRegistry<T>> networkCodec(RegistryKey<? extends Registry<T>> pRegistryKey, Lifecycle pLifecycle, Codec<T> pCodec) {
      return withNameAndId(pRegistryKey, pCodec.fieldOf("element")).codec().listOf().xmap((p_243540_2_) -> {
         SimpleRegistry<T> simpleregistry = new SimpleRegistry<>(pRegistryKey, pLifecycle);

         for(SimpleRegistry.Entry<T> entry : p_243540_2_) {
            simpleregistry.registerMapping(entry.id, entry.key, entry.value, pLifecycle);
         }

         return simpleregistry;
      }, (p_243544_0_) -> {
         Builder<SimpleRegistry.Entry<T>> builder = ImmutableList.builder();

         for(T t : p_243544_0_) {
            builder.add(new SimpleRegistry.Entry<>(p_243544_0_.getResourceKey(t).get(), p_243544_0_.getId(t), t));
         }

         return builder.build();
      });
   }

   public static <T> Codec<SimpleRegistry<T>> dataPackCodec(RegistryKey<? extends Registry<T>> pRegistryKey, Lifecycle pLifecycle, Codec<T> pMapCodec) {
      return SimpleRegistryCodec.create(pRegistryKey, pLifecycle, pMapCodec);
   }

   public static <T> Codec<SimpleRegistry<T>> directCodec(RegistryKey<? extends Registry<T>> pRegistryKey, Lifecycle pLifecycle, Codec<T> pMapCodec) {
      // FORGE: Fix MC-197860
      return new net.minecraftforge.common.LenientUnboundedMapCodec<>(ResourceLocation.CODEC.xmap(RegistryKey.elementKey(pRegistryKey), RegistryKey::location), pMapCodec).xmap((p_239656_2_) -> {
         SimpleRegistry<T> simpleregistry = new SimpleRegistry<>(pRegistryKey, pLifecycle);
         p_239656_2_.forEach((p_239653_2_, p_239653_3_) -> {
            simpleregistry.register(p_239653_2_, p_239653_3_, pLifecycle);
         });
         return simpleregistry;
      }, (p_239651_0_) -> {
         return ImmutableMap.copyOf(p_239651_0_.keyStorage);
      });
   }

   public static class Entry<T> {
      public final RegistryKey<T> key;
      public final int id;
      public final T value;

      public Entry(RegistryKey<T> pKey, int pId, T pValue) {
         this.key = pKey;
         this.id = pId;
         this.value = pValue;
      }
   }
}
