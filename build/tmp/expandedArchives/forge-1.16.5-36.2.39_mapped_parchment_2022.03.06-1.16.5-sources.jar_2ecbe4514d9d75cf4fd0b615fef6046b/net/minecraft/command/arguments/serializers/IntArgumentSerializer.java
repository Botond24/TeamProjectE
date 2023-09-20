package net.minecraft.command.arguments.serializers;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;

public class IntArgumentSerializer implements IArgumentSerializer<IntegerArgumentType> {
   public void serializeToNetwork(IntegerArgumentType pArgument, PacketBuffer pBuffer) {
      boolean flag = pArgument.getMinimum() != Integer.MIN_VALUE;
      boolean flag1 = pArgument.getMaximum() != Integer.MAX_VALUE;
      pBuffer.writeByte(BrigadierSerializers.createNumberFlags(flag, flag1));
      if (flag) {
         pBuffer.writeInt(pArgument.getMinimum());
      }

      if (flag1) {
         pBuffer.writeInt(pArgument.getMaximum());
      }

   }

   public IntegerArgumentType deserializeFromNetwork(PacketBuffer pBuffer) {
      byte b0 = pBuffer.readByte();
      int i = BrigadierSerializers.numberHasMin(b0) ? pBuffer.readInt() : Integer.MIN_VALUE;
      int j = BrigadierSerializers.numberHasMax(b0) ? pBuffer.readInt() : Integer.MAX_VALUE;
      return IntegerArgumentType.integer(i, j);
   }

   public void serializeToJson(IntegerArgumentType pArgument, JsonObject pJson) {
      if (pArgument.getMinimum() != Integer.MIN_VALUE) {
         pJson.addProperty("min", pArgument.getMinimum());
      }

      if (pArgument.getMaximum() != Integer.MAX_VALUE) {
         pJson.addProperty("max", pArgument.getMaximum());
      }

   }
}