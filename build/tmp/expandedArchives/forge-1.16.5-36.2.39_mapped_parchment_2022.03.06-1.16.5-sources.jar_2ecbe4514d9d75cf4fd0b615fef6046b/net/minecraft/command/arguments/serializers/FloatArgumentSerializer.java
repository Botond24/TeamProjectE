package net.minecraft.command.arguments.serializers;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;

public class FloatArgumentSerializer implements IArgumentSerializer<FloatArgumentType> {
   public void serializeToNetwork(FloatArgumentType pArgument, PacketBuffer pBuffer) {
      boolean flag = pArgument.getMinimum() != -Float.MAX_VALUE;
      boolean flag1 = pArgument.getMaximum() != Float.MAX_VALUE;
      pBuffer.writeByte(BrigadierSerializers.createNumberFlags(flag, flag1));
      if (flag) {
         pBuffer.writeFloat(pArgument.getMinimum());
      }

      if (flag1) {
         pBuffer.writeFloat(pArgument.getMaximum());
      }

   }

   public FloatArgumentType deserializeFromNetwork(PacketBuffer pBuffer) {
      byte b0 = pBuffer.readByte();
      float f = BrigadierSerializers.numberHasMin(b0) ? pBuffer.readFloat() : -Float.MAX_VALUE;
      float f1 = BrigadierSerializers.numberHasMax(b0) ? pBuffer.readFloat() : Float.MAX_VALUE;
      return FloatArgumentType.floatArg(f, f1);
   }

   public void serializeToJson(FloatArgumentType pArgument, JsonObject pJson) {
      if (pArgument.getMinimum() != -Float.MAX_VALUE) {
         pJson.addProperty("min", pArgument.getMinimum());
      }

      if (pArgument.getMaximum() != Float.MAX_VALUE) {
         pJson.addProperty("max", pArgument.getMaximum());
      }

   }
}