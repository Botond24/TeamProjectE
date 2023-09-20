package net.minecraft.command.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.PacketBuffer;

public interface IArgumentSerializer<T extends ArgumentType<?>> {
   void serializeToNetwork(T pArgument, PacketBuffer pBuffer);

   T deserializeFromNetwork(PacketBuffer pBuffer);

   void serializeToJson(T pArgument, JsonObject pJson);
}