package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;

public class GameModeCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("gamemode").requires((p_198485_0_) -> {
         return p_198485_0_.hasPermission(2);
      });

      for(GameType gametype : GameType.values()) {
         if (gametype != GameType.NOT_SET) {
            literalargumentbuilder.then(Commands.literal(gametype.getName()).executes((p_198483_1_) -> {
               return setMode(p_198483_1_, Collections.singleton(p_198483_1_.getSource().getPlayerOrException()), gametype);
            }).then(Commands.argument("target", EntityArgument.players()).executes((p_198486_1_) -> {
               return setMode(p_198486_1_, EntityArgument.getPlayers(p_198486_1_, "target"), gametype);
            })));
         }
      }

      pDispatcher.register(literalargumentbuilder);
   }

   private static void logGamemodeChange(CommandSource pSource, ServerPlayerEntity pPlayer, GameType pGameType) {
      ITextComponent itextcomponent = new TranslationTextComponent("gameMode." + pGameType.getName());
      if (pSource.getEntity() == pPlayer) {
         pSource.sendSuccess(new TranslationTextComponent("commands.gamemode.success.self", itextcomponent), true);
      } else {
         if (pSource.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            pPlayer.sendMessage(new TranslationTextComponent("gameMode.changed", itextcomponent), Util.NIL_UUID);
         }

         pSource.sendSuccess(new TranslationTextComponent("commands.gamemode.success.other", pPlayer.getDisplayName(), itextcomponent), true);
      }

   }

   private static int setMode(CommandContext<CommandSource> pSource, Collection<ServerPlayerEntity> pPlayers, GameType pGameType) {
      int i = 0;

      for(ServerPlayerEntity serverplayerentity : pPlayers) {
         if (serverplayerentity.gameMode.getGameModeForPlayer() != pGameType) {
            serverplayerentity.setGameMode(pGameType);
            logGamemodeChange(pSource.getSource(), serverplayerentity, pGameType);
            ++i;
         }
      }

      return i;
   }
}