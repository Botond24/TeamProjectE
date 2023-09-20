package net.minecraft.particles;

import com.mojang.serialization.Codec;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ParticleType<T extends IParticleData>  extends net.minecraftforge.registries.ForgeRegistryEntry<ParticleType<?>>{
   private final boolean overrideLimiter;
   private final IParticleData.IDeserializer<T> deserializer;

   public ParticleType(boolean pOverrideLimiter, IParticleData.IDeserializer<T> pDeserializer) {
      this.overrideLimiter = pOverrideLimiter;
      this.deserializer = pDeserializer;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean getOverrideLimiter() {
      return this.overrideLimiter;
   }

   public IParticleData.IDeserializer<T> getDeserializer() {
      return this.deserializer;
   }

   public abstract Codec<T> codec();
}
