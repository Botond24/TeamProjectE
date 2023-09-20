package net.minecraft.util.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DelegatingDynamicOps;

public class WorldGenSettingsExport<T> extends DelegatingDynamicOps<T> {
   private final DynamicRegistries registryHolder;

   public static <T> WorldGenSettingsExport<T> create(DynamicOps<T> pOps, DynamicRegistries pDynamicRegistries) {
      return new WorldGenSettingsExport<>(pOps, pDynamicRegistries);
   }

   private WorldGenSettingsExport(DynamicOps<T> p_i232591_1_, DynamicRegistries p_i232591_2_) {
      super(p_i232591_1_);
      this.registryHolder = p_i232591_2_;
   }

   protected <E> DataResult<T> encode(E pInstance, T pPrefix, RegistryKey<? extends Registry<E>> pRegistryKey, Codec<E> pMapCodec) {
      Optional<MutableRegistry<E>> optional = this.registryHolder.registry(pRegistryKey);
      if (optional.isPresent()) {
         MutableRegistry<E> mutableregistry = optional.get();
         Optional<RegistryKey<E>> optional1 = mutableregistry.getResourceKey(pInstance);
         if (optional1.isPresent()) {
            RegistryKey<E> registrykey = optional1.get();
            return ResourceLocation.CODEC.encode(registrykey.location(), this.delegate, pPrefix);
         }
      }

      return pMapCodec.encode(pInstance, this, pPrefix);
   }
}