package net.minecraft.command.arguments.serializers;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;

public class BrigadierSerializers {
   public static void bootstrap() {
      ArgumentTypes.register("brigadier:bool", BoolArgumentType.class, new ArgumentSerializer<>(BoolArgumentType::bool));
      ArgumentTypes.register("brigadier:float", FloatArgumentType.class, new FloatArgumentSerializer());
      ArgumentTypes.register("brigadier:double", DoubleArgumentType.class, new DoubleArgumentSerializer());
      ArgumentTypes.register("brigadier:integer", IntegerArgumentType.class, new IntArgumentSerializer());
      ArgumentTypes.register("brigadier:long", LongArgumentType.class, new LongArgumentSerializer());
      ArgumentTypes.register("brigadier:string", StringArgumentType.class, new StringArgumentSerializer());
   }

   public static byte createNumberFlags(boolean pMin, boolean pMax) {
      byte b0 = 0;
      if (pMin) {
         b0 = (byte)(b0 | 1);
      }

      if (pMax) {
         b0 = (byte)(b0 | 2);
      }

      return b0;
   }

   public static boolean numberHasMin(byte pFlags) {
      return (pFlags & 1) != 0;
   }

   public static boolean numberHasMax(byte pFlags) {
      return (pFlags & 2) != 0;
   }
}