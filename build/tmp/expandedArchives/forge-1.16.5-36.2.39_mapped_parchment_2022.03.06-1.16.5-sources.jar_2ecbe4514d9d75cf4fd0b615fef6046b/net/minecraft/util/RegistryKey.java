package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.registry.Registry;

public class RegistryKey<T> implements Comparable<RegistryKey<?>> {
   private static final Map<String, RegistryKey<?>> VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());
   private final ResourceLocation registryName;
   private final ResourceLocation location;

   public static <T> RegistryKey<T> create(RegistryKey<? extends Registry<T>> pParent, ResourceLocation pLocation) {
      return create(pParent.location, pLocation);
   }

   public static <T> RegistryKey<Registry<T>> createRegistryKey(ResourceLocation pLocation) {
      return create(Registry.ROOT_REGISTRY_NAME, pLocation);
   }

   private static <T> RegistryKey<T> create(ResourceLocation pParent, ResourceLocation pLocation) {
      String s = (pParent + ":" + pLocation).intern();
      return (RegistryKey<T>)VALUES.computeIfAbsent(s, (p_240906_2_) -> {
         return new RegistryKey(pParent, pLocation);
      });
   }

   private RegistryKey(ResourceLocation p_i232592_1_, ResourceLocation p_i232592_2_) {
      this.registryName = p_i232592_1_;
      this.location = p_i232592_2_;
   }

   public String toString() {
      return "ResourceKey[" + this.registryName + " / " + this.location + ']';
   }

   /**
    * Returns true if the registry represented by the parent key
    */
   public boolean isFor(RegistryKey<? extends Registry<?>> pKey) {
      return this.registryName.equals(pKey.location());
   }

   public ResourceLocation location() {
      return this.location;
   }

   public static <T> Function<ResourceLocation, RegistryKey<T>> elementKey(RegistryKey<? extends Registry<T>> pParent) {
      return (p_240907_1_) -> {
         return create(pParent, p_240907_1_);
      };
   }

   public ResourceLocation getRegistryName() {
      return this.registryName;
   }

   @Override
   public int compareTo(RegistryKey<?> o) {
      int ret = this.getRegistryName().compareTo(o.getRegistryName());
      if (ret == 0) ret = this.location().compareTo(o.location());
      return ret;
   }
}
