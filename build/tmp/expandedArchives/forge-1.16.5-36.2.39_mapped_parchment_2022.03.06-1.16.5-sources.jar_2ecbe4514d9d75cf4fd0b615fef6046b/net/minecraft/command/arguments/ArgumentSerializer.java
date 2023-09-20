package net.minecraft.command.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;

public class ArgumentSerializer<T extends ArgumentType<?>> implements IArgumentSerializer<T> {
   private final Supplier<T> constructor;

   public ArgumentSerializer(Supplier<T> pConstructor) {
      this.constructor = pConstructor;
   }

   public void serializeToNetwork(T pArgument, PacketBuffer pBuffer) {
   }

   public T deserializeFromNetwork(PacketBuffer pBuffer) {
      return this.constructor.get();
   }

   public void serializeToJson(T pArgument, JsonObject pJson) {
   }
}