package net.minecraft.util.registry;

import com.mojang.serialization.Lifecycle;
import java.util.OptionalInt;
import net.minecraft.util.RegistryKey;

public abstract class MutableRegistry<T> extends Registry<T> {
   public MutableRegistry(RegistryKey<? extends Registry<T>> p_i232512_1_, Lifecycle p_i232512_2_) {
      super(p_i232512_1_, p_i232512_2_);
   }

   public abstract <V extends T> V registerMapping(int pId, RegistryKey<T> pName, V pInstance, Lifecycle pLifecycle);

   public abstract <V extends T> V register(RegistryKey<T> pName, V pInstance, Lifecycle pLifecycle);

   public abstract <V extends T> V registerOrOverride(OptionalInt pIndex, RegistryKey<T> pRegistryKey, V pValue, Lifecycle pLifecycle);
}