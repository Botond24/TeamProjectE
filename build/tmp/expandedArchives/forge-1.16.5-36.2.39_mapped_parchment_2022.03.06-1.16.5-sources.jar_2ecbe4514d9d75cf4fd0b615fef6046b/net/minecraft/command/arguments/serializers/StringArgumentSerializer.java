package net.minecraft.command.arguments.serializers;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;

public class StringArgumentSerializer implements IArgumentSerializer<StringArgumentType> {
   public void serializeToNetwork(StringArgumentType pArgument, PacketBuffer pBuffer) {
      pBuffer.writeEnum(pArgument.getType());
   }

   public StringArgumentType deserializeFromNetwork(PacketBuffer pBuffer) {
      StringType stringtype = pBuffer.readEnum(StringType.class);
      switch(stringtype) {
      case SINGLE_WORD:
         return StringArgumentType.word();
      case QUOTABLE_PHRASE:
         return StringArgumentType.string();
      case GREEDY_PHRASE:
      default:
         return StringArgumentType.greedyString();
      }
   }

   public void serializeToJson(StringArgumentType pArgument, JsonObject pJson) {
      switch(pArgument.getType()) {
      case SINGLE_WORD:
         pJson.addProperty("type", "word");
         break;
      case QUOTABLE_PHRASE:
         pJson.addProperty("type", "phrase");
         break;
      case GREEDY_PHRASE:
      default:
         pJson.addProperty("type", "greedy");
      }

   }
}