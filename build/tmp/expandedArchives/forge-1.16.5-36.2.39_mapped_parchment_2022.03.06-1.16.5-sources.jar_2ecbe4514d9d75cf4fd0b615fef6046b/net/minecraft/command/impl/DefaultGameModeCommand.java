package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;

public class DefaultGameModeCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("defaultgamemode").requires((p_198342_0_) -> {
         return p_198342_0_.hasPermission(2);
      });

      for(GameType gametype : GameType.values()) {
         if (gametype != GameType.NOT_SET) {
            literalargumentbuilder.then(Commands.literal(gametype.getName()).executes((p_198343_1_) -> {
               return setMode(p_198343_1_.getSource(), gametype);
            }));
         }
      }

      pDispatcher.register(literalargumentbuilder);
   }

   /**
    * Set Gametype of player who ran the command
    */
   private static int setMode(CommandSource pCommandSource, GameType pGamemode) {
      int i = 0;
      MinecraftServer minecraftserver = pCommandSource.getServer();
      minecraftserver.setDefaultGameType(pGamemode);
      if (minecraftserver.getForceGameType()) {
         for(ServerPlayerEntity serverplayerentity : minecraftserver.getPlayerList().getPlayers()) {
            if (serverplayerentity.gameMode.getGameModeForPlayer() != pGamemode) {
               serverplayerentity.setGameMode(pGamemode);
               ++i;
            }
         }
      }

      pCommandSource.sendSuccess(new TranslationTextComponent("commands.defaultgamemode.success", pGamemode.getDisplayName()), true);
      return i;
   }
}