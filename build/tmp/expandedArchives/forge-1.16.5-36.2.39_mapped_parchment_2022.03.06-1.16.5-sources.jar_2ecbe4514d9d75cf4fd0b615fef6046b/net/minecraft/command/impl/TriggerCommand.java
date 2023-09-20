package net.minecraft.command.impl;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ObjectiveArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.TranslationTextComponent;

public class TriggerCommand {
   private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.trigger.failed.unprimed"));
   private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.trigger.failed.invalid"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("trigger").then(Commands.argument("objective", ObjectiveArgument.objective()).suggests((p_198853_0_, p_198853_1_) -> {
         return suggestObjectives(p_198853_0_.getSource(), p_198853_1_);
      }).executes((p_198854_0_) -> {
         return simpleTrigger(p_198854_0_.getSource(), getScore(p_198854_0_.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(p_198854_0_, "objective")));
      }).then(Commands.literal("add").then(Commands.argument("value", IntegerArgumentType.integer()).executes((p_198849_0_) -> {
         return addValue(p_198849_0_.getSource(), getScore(p_198849_0_.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(p_198849_0_, "objective")), IntegerArgumentType.getInteger(p_198849_0_, "value"));
      }))).then(Commands.literal("set").then(Commands.argument("value", IntegerArgumentType.integer()).executes((p_198855_0_) -> {
         return setValue(p_198855_0_.getSource(), getScore(p_198855_0_.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(p_198855_0_, "objective")), IntegerArgumentType.getInteger(p_198855_0_, "value"));
      })))));
   }

   public static CompletableFuture<Suggestions> suggestObjectives(CommandSource pSource, SuggestionsBuilder pBuilder) {
      Entity entity = pSource.getEntity();
      List<String> list = Lists.newArrayList();
      if (entity != null) {
         Scoreboard scoreboard = pSource.getServer().getScoreboard();
         String s = entity.getScoreboardName();

         for(ScoreObjective scoreobjective : scoreboard.getObjectives()) {
            if (scoreobjective.getCriteria() == ScoreCriteria.TRIGGER && scoreboard.hasPlayerScore(s, scoreobjective)) {
               Score score = scoreboard.getOrCreatePlayerScore(s, scoreobjective);
               if (!score.isLocked()) {
                  list.add(scoreobjective.getName());
               }
            }
         }
      }

      return ISuggestionProvider.suggest(list, pBuilder);
   }

   private static int addValue(CommandSource pSource, Score pObjective, int pAmount) {
      pObjective.add(pAmount);
      pSource.sendSuccess(new TranslationTextComponent("commands.trigger.add.success", pObjective.getObjective().getFormattedDisplayName(), pAmount), true);
      return pObjective.getScore();
   }

   private static int setValue(CommandSource pSource, Score pObjective, int pValue) {
      pObjective.setScore(pValue);
      pSource.sendSuccess(new TranslationTextComponent("commands.trigger.set.success", pObjective.getObjective().getFormattedDisplayName(), pValue), true);
      return pValue;
   }

   private static int simpleTrigger(CommandSource pSource, Score pObjectives) {
      pObjectives.add(1);
      pSource.sendSuccess(new TranslationTextComponent("commands.trigger.simple.success", pObjectives.getObjective().getFormattedDisplayName()), true);
      return pObjectives.getScore();
   }

   private static Score getScore(ServerPlayerEntity pPlayer, ScoreObjective pObjective) throws CommandSyntaxException {
      if (pObjective.getCriteria() != ScoreCriteria.TRIGGER) {
         throw ERROR_INVALID_OBJECTIVE.create();
      } else {
         Scoreboard scoreboard = pPlayer.getScoreboard();
         String s = pPlayer.getScoreboardName();
         if (!scoreboard.hasPlayerScore(s, pObjective)) {
            throw ERROR_NOT_PRIMED.create();
         } else {
            Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
            if (score.isLocked()) {
               throw ERROR_NOT_PRIMED.create();
            } else {
               score.setLocked(true);
               return score;
            }
         }
      }
   }
}