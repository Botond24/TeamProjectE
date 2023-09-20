package net.minecraft.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketBuffer;

public interface IParticleData {
   ParticleType<?> getType();

   void writeToNetwork(PacketBuffer pBuffer);

   String writeToString();

   @Deprecated
   public interface IDeserializer<T extends IParticleData> {
      T fromCommand(ParticleType<T> pParticleType, StringReader pReader) throws CommandSyntaxException;

      T fromNetwork(ParticleType<T> pParticleType, PacketBuffer pBuffer);
   }
}