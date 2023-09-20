package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.regex.Matcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.server.management.IPBanList;
import net.minecraft.util.text.TranslationTextComponent;

public class PardonIpCommand {
   private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(new TranslationTextComponent("commands.pardonip.invalid"));
   private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.pardonip.failed"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("pardon-ip").requires((p_198556_0_) -> {
         return p_198556_0_.hasPermission(3);
      }).then(Commands.argument("target", StringArgumentType.word()).suggests((p_198554_0_, p_198554_1_) -> {
         return ISuggestionProvider.suggest(p_198554_0_.getSource().getServer().getPlayerList().getIpBans().getUserList(), p_198554_1_);
      }).executes((p_198555_0_) -> {
         return unban(p_198555_0_.getSource(), StringArgumentType.getString(p_198555_0_, "target"));
      })));
   }

   private static int unban(CommandSource pSource, String pIpAddress) throws CommandSyntaxException {
      Matcher matcher = BanIpCommand.IP_ADDRESS_PATTERN.matcher(pIpAddress);
      if (!matcher.matches()) {
         throw ERROR_INVALID.create();
      } else {
         IPBanList ipbanlist = pSource.getServer().getPlayerList().getIpBans();
         if (!ipbanlist.isBanned(pIpAddress)) {
            throw ERROR_NOT_BANNED.create();
         } else {
            ipbanlist.remove(pIpAddress);
            pSource.sendSuccess(new TranslationTextComponent("commands.pardonip.success", pIpAddress), true);
            return 1;
         }
      }
   }
}