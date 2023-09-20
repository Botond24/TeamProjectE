package net.minecraft.command.arguments.serializers;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;

public class LongArgumentSerializer implements IArgumentSerializer<LongArgumentType> {
   public void serializeToNetwork(LongArgumentType pArgument, PacketBuffer pBuffer) {
      boolean flag = pArgument.getMinimum() != Long.MIN_VALUE;
      boolean flag1 = pArgument.getMaximum() != Long.MAX_VALUE;
      pBuffer.writeByte(BrigadierSerializers.createNumberFlags(flag, flag1));
      if (flag) {
         pBuffer.writeLong(pArgument.getMinimum());
      }

      if (flag1) {
         pBuffer.writeLong(pArgument.getMaximum());
      }

   }

   public LongArgumentType deserializeFromNetwork(PacketBuffer pBuffer) {
      byte b0 = pBuffer.readByte();
      long i = BrigadierSerializers.numberHasMin(b0) ? pBuffer.readLong() : Long.MIN_VALUE;
      long j = BrigadierSerializers.numberHasMax(b0) ? pBuffer.readLong() : Long.MAX_VALUE;
      return LongArgumentType.longArg(i, j);
   }

   public void serializeToJson(LongArgumentType pArgument, JsonObject pJson) {
      if (pArgument.getMinimum() != Long.MIN_VALUE) {
         pJson.addProperty("min", pArgument.getMinimum());
      }

      if (pArgument.getMaximum() != Long.MAX_VALUE) {
         pJson.addProperty("max", pArgument.getMaximum());
      }

   }
}