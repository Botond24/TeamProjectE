package net.minecraft.command.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TranslationTextComponent;

public class BanListCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("banlist").requires((p_198233_0_) -> {
         return p_198233_0_.hasPermission(3);
      }).executes((p_198231_0_) -> {
         PlayerList playerlist = p_198231_0_.getSource().getServer().getPlayerList();
         return showList(p_198231_0_.getSource(), Lists.newArrayList(Iterables.concat(playerlist.getBans().getEntries(), playerlist.getIpBans().getEntries())));
      }).then(Commands.literal("ips").executes((p_198228_0_) -> {
         return showList(p_198228_0_.getSource(), p_198228_0_.getSource().getServer().getPlayerList().getIpBans().getEntries());
      })).then(Commands.literal("players").executes((p_198232_0_) -> {
         return showList(p_198232_0_.getSource(), p_198232_0_.getSource().getServer().getPlayerList().getBans().getEntries());
      })));
   }

   private static int showList(CommandSource pSource, Collection<? extends BanEntry<?>> pBannedPlayerList) {
      if (pBannedPlayerList.isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.banlist.none"), false);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.banlist.list", pBannedPlayerList.size()), false);

         for(BanEntry<?> banentry : pBannedPlayerList) {
            pSource.sendSuccess(new TranslationTextComponent("commands.banlist.entry", banentry.getDisplayName(), banentry.getSource(), banentry.getReason()), false);
         }
      }

      return pBannedPlayerList.size();
   }
}