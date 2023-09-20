package net.minecraft.command.arguments.serializers;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;

public class DoubleArgumentSerializer implements IArgumentSerializer<DoubleArgumentType> {
   public void serializeToNetwork(DoubleArgumentType pArgument, PacketBuffer pBuffer) {
      boolean flag = pArgument.getMinimum() != -Double.MAX_VALUE;
      boolean flag1 = pArgument.getMaximum() != Double.MAX_VALUE;
      pBuffer.writeByte(BrigadierSerializers.createNumberFlags(flag, flag1));
      if (flag) {
         pBuffer.writeDouble(pArgument.getMinimum());
      }

      if (flag1) {
         pBuffer.writeDouble(pArgument.getMaximum());
      }

   }

   public DoubleArgumentType deserializeFromNetwork(PacketBuffer pBuffer) {
      byte b0 = pBuffer.readByte();
      double d0 = BrigadierSerializers.numberHasMin(b0) ? pBuffer.readDouble() : -Double.MAX_VALUE;
      double d1 = BrigadierSerializers.numberHasMax(b0) ? pBuffer.readDouble() : Double.MAX_VALUE;
      return DoubleArgumentType.doubleArg(d0, d1);
   }

   public void serializeToJson(DoubleArgumentType pArgument, JsonObject pJson) {
      if (pArgument.getMinimum() != -Double.MAX_VALUE) {
         pJson.addProperty("min", pArgument.getMinimum());
      }

      if (pArgument.getMaximum() != Double.MAX_VALUE) {
         pJson.addProperty("max", pArgument.getMaximum());
      }

   }
}