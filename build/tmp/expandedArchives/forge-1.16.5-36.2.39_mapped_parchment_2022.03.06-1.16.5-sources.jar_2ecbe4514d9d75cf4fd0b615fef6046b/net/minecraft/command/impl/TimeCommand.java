package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.TimeArgument;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class TimeCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("time").requires((p_198828_0_) -> {
         return p_198828_0_.hasPermission(2);
      }).then(Commands.literal("set").then(Commands.literal("day").executes((p_198832_0_) -> {
         return setTime(p_198832_0_.getSource(), 1000);
      })).then(Commands.literal("noon").executes((p_198825_0_) -> {
         return setTime(p_198825_0_.getSource(), 6000);
      })).then(Commands.literal("night").executes((p_198822_0_) -> {
         return setTime(p_198822_0_.getSource(), 13000);
      })).then(Commands.literal("midnight").executes((p_200563_0_) -> {
         return setTime(p_200563_0_.getSource(), 18000);
      })).then(Commands.argument("time", TimeArgument.time()).executes((p_200564_0_) -> {
         return setTime(p_200564_0_.getSource(), IntegerArgumentType.getInteger(p_200564_0_, "time"));
      }))).then(Commands.literal("add").then(Commands.argument("time", TimeArgument.time()).executes((p_198830_0_) -> {
         return addTime(p_198830_0_.getSource(), IntegerArgumentType.getInteger(p_198830_0_, "time"));
      }))).then(Commands.literal("query").then(Commands.literal("daytime").executes((p_198827_0_) -> {
         return queryTime(p_198827_0_.getSource(), getDayTime(p_198827_0_.getSource().getLevel()));
      })).then(Commands.literal("gametime").executes((p_198821_0_) -> {
         return queryTime(p_198821_0_.getSource(), (int)(p_198821_0_.getSource().getLevel().getGameTime() % 2147483647L));
      })).then(Commands.literal("day").executes((p_198831_0_) -> {
         return queryTime(p_198831_0_.getSource(), (int)(p_198831_0_.getSource().getLevel().getDayTime() / 24000L % 2147483647L));
      }))));
   }

   /**
    * Returns the day time (time wrapped within a day)
    */
   private static int getDayTime(ServerWorld pLevel) {
      return (int)(pLevel.getDayTime() % 24000L);
   }

   private static int queryTime(CommandSource pSource, int pTime) {
      pSource.sendSuccess(new TranslationTextComponent("commands.time.query", pTime), false);
      return pTime;
   }

   public static int setTime(CommandSource pSource, int pTime) {
      for(ServerWorld serverworld : pSource.getServer().getAllLevels()) {
         serverworld.setDayTime((long)pTime);
      }

      pSource.sendSuccess(new TranslationTextComponent("commands.time.set", pTime), true);
      return getDayTime(pSource.getLevel());
   }

   public static int addTime(CommandSource pSource, int pAmount) {
      for(ServerWorld serverworld : pSource.getServer().getAllLevels()) {
         serverworld.setDayTime(serverworld.getDayTime() + (long)pAmount);
      }

      int i = getDayTime(pSource.getLevel());
      pSource.sendSuccess(new TranslationTextComponent("commands.time.set", i), true);
      return i;
   }
}