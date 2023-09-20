package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import java.util.function.Function;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;

public class ListCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("list").executes((p_198523_0_) -> {
         return listPlayers(p_198523_0_.getSource());
      }).then(Commands.literal("uuids").executes((p_208202_0_) -> {
         return listPlayersWithUuids(p_208202_0_.getSource());
      })));
   }

   private static int listPlayers(CommandSource pSource) {
      return format(pSource, PlayerEntity::getDisplayName);
   }

   private static int listPlayersWithUuids(CommandSource pSource) {
      return format(pSource, (p_244373_0_) -> {
         return new TranslationTextComponent("commands.list.nameAndId", p_244373_0_.getName(), p_244373_0_.getGameProfile().getId());
      });
   }

   private static int format(CommandSource pSource, Function<ServerPlayerEntity, ITextComponent> pNameExtractor) {
      PlayerList playerlist = pSource.getServer().getPlayerList();
      List<ServerPlayerEntity> list = playerlist.getPlayers();
      ITextComponent itextcomponent = TextComponentUtils.formatList(list, pNameExtractor);
      pSource.sendSuccess(new TranslationTextComponent("commands.list.players", list.size(), playerlist.getMaxPlayers(), itextcomponent), false);
      return list.size();
   }
}