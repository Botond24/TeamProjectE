package net.minecraft.command.impl;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.arguments.ObjectiveArgument;
import net.minecraft.command.arguments.ObjectiveCriteriaArgument;
import net.minecraft.command.arguments.OperationArgument;
import net.minecraft.command.arguments.ScoreHolderArgument;
import net.minecraft.command.arguments.ScoreboardSlotArgument;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;

public class ScoreboardCommand {
   private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(new TranslationTextComponent("commands.scoreboard.objectives.add.duplicate"));
   private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(new TranslationTextComponent("commands.scoreboard.objectives.display.alreadyEmpty"));
   private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(new TranslationTextComponent("commands.scoreboard.objectives.display.alreadySet"));
   private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.scoreboard.players.enable.failed"));
   private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(new TranslationTextComponent("commands.scoreboard.players.enable.invalid"));
   private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((p_208907_0_, p_208907_1_) -> {
      return new TranslationTextComponent("commands.scoreboard.players.get.null", p_208907_0_, p_208907_1_);
   });

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("scoreboard").requires((p_198650_0_) -> {
         return p_198650_0_.hasPermission(2);
      }).then(Commands.literal("objectives").then(Commands.literal("list").executes((p_198640_0_) -> {
         return listObjectives(p_198640_0_.getSource());
      })).then(Commands.literal("add").then(Commands.argument("objective", StringArgumentType.word()).then(Commands.argument("criteria", ObjectiveCriteriaArgument.criteria()).executes((p_198636_0_) -> {
         return addObjective(p_198636_0_.getSource(), StringArgumentType.getString(p_198636_0_, "objective"), ObjectiveCriteriaArgument.getCriteria(p_198636_0_, "criteria"), new StringTextComponent(StringArgumentType.getString(p_198636_0_, "objective")));
      }).then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((p_198649_0_) -> {
         return addObjective(p_198649_0_.getSource(), StringArgumentType.getString(p_198649_0_, "objective"), ObjectiveCriteriaArgument.getCriteria(p_198649_0_, "criteria"), ComponentArgument.getComponent(p_198649_0_, "displayName"));
      }))))).then(Commands.literal("modify").then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.literal("displayname").then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((p_211750_0_) -> {
         return setDisplayName(p_211750_0_.getSource(), ObjectiveArgument.getObjective(p_211750_0_, "objective"), ComponentArgument.getComponent(p_211750_0_, "displayName"));
      }))).then(createRenderTypeModify()))).then(Commands.literal("remove").then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_198646_0_) -> {
         return removeObjective(p_198646_0_.getSource(), ObjectiveArgument.getObjective(p_198646_0_, "objective"));
      }))).then(Commands.literal("setdisplay").then(Commands.argument("slot", ScoreboardSlotArgument.displaySlot()).executes((p_198652_0_) -> {
         return clearDisplaySlot(p_198652_0_.getSource(), ScoreboardSlotArgument.getDisplaySlot(p_198652_0_, "slot"));
      }).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_198639_0_) -> {
         return setDisplaySlot(p_198639_0_.getSource(), ScoreboardSlotArgument.getDisplaySlot(p_198639_0_, "slot"), ObjectiveArgument.getObjective(p_198639_0_, "objective"));
      }))))).then(Commands.literal("players").then(Commands.literal("list").executes((p_198642_0_) -> {
         return listTrackedPlayers(p_198642_0_.getSource());
      }).then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((p_198631_0_) -> {
         return listTrackedPlayerScores(p_198631_0_.getSource(), ScoreHolderArgument.getName(p_198631_0_, "target"));
      }))).then(Commands.literal("set").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer()).executes((p_198655_0_) -> {
         return setScore(p_198655_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198655_0_, "targets"), ObjectiveArgument.getWritableObjective(p_198655_0_, "objective"), IntegerArgumentType.getInteger(p_198655_0_, "score"));
      }))))).then(Commands.literal("get").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_198660_0_) -> {
         return getScore(p_198660_0_.getSource(), ScoreHolderArgument.getName(p_198660_0_, "target"), ObjectiveArgument.getObjective(p_198660_0_, "objective"));
      })))).then(Commands.literal("add").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer(0)).executes((p_198645_0_) -> {
         return addScore(p_198645_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198645_0_, "targets"), ObjectiveArgument.getWritableObjective(p_198645_0_, "objective"), IntegerArgumentType.getInteger(p_198645_0_, "score"));
      }))))).then(Commands.literal("remove").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer(0)).executes((p_198648_0_) -> {
         return removeScore(p_198648_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198648_0_, "targets"), ObjectiveArgument.getWritableObjective(p_198648_0_, "objective"), IntegerArgumentType.getInteger(p_198648_0_, "score"));
      }))))).then(Commands.literal("reset").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((p_198635_0_) -> {
         return resetScores(p_198635_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198635_0_, "targets"));
      }).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_198630_0_) -> {
         return resetScore(p_198630_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198630_0_, "targets"), ObjectiveArgument.getObjective(p_198630_0_, "objective"));
      })))).then(Commands.literal("enable").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).suggests((p_198638_0_, p_198638_1_) -> {
         return suggestTriggers(p_198638_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198638_0_, "targets"), p_198638_1_);
      }).executes((p_198628_0_) -> {
         return enableTrigger(p_198628_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198628_0_, "targets"), ObjectiveArgument.getObjective(p_198628_0_, "objective"));
      })))).then(Commands.literal("operation").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.argument("operation", OperationArgument.operation()).then(Commands.argument("source", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("sourceObjective", ObjectiveArgument.objective()).executes((p_198657_0_) -> {
         return performOperation(p_198657_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198657_0_, "targets"), ObjectiveArgument.getWritableObjective(p_198657_0_, "targetObjective"), OperationArgument.getOperation(p_198657_0_, "operation"), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198657_0_, "source"), ObjectiveArgument.getObjective(p_198657_0_, "sourceObjective"));
      })))))))));
   }

   private static LiteralArgumentBuilder<CommandSource> createRenderTypeModify() {
      LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("rendertype");

      for(ScoreCriteria.RenderType scorecriteria$rendertype : ScoreCriteria.RenderType.values()) {
         literalargumentbuilder.then(Commands.literal(scorecriteria$rendertype.getId()).executes((p_211912_1_) -> {
            return setRenderType(p_211912_1_.getSource(), ObjectiveArgument.getObjective(p_211912_1_, "objective"), scorecriteria$rendertype);
         }));
      }

      return literalargumentbuilder;
   }

   private static CompletableFuture<Suggestions> suggestTriggers(CommandSource pSource, Collection<String> pTargets, SuggestionsBuilder pSuggestions) {
      List<String> list = Lists.newArrayList();
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(ScoreObjective scoreobjective : scoreboard.getObjectives()) {
         if (scoreobjective.getCriteria() == ScoreCriteria.TRIGGER) {
            boolean flag = false;

            for(String s : pTargets) {
               if (!scoreboard.hasPlayerScore(s, scoreobjective) || scoreboard.getOrCreatePlayerScore(s, scoreobjective).isLocked()) {
                  flag = true;
                  break;
               }
            }

            if (flag) {
               list.add(scoreobjective.getName());
            }
         }
      }

      return ISuggestionProvider.suggest(list, pSuggestions);
   }

   private static int getScore(CommandSource pSource, String pPlayer, ScoreObjective pObjective) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (!scoreboard.hasPlayerScore(pPlayer, pObjective)) {
         throw ERROR_NO_VALUE.create(pObjective.getName(), pPlayer);
      } else {
         Score score = scoreboard.getOrCreatePlayerScore(pPlayer, pObjective);
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.get.success", pPlayer, score.getScore(), pObjective.getFormattedDisplayName()), false);
         return score.getScore();
      }
   }

   private static int performOperation(CommandSource pSource, Collection<String> pTargetEntities, ScoreObjective pTargetObjectives, OperationArgument.IOperation pOperation, Collection<String> pSourceEntities, ScoreObjective pSourceObjective) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      int i = 0;

      for(String s : pTargetEntities) {
         Score score = scoreboard.getOrCreatePlayerScore(s, pTargetObjectives);

         for(String s1 : pSourceEntities) {
            Score score1 = scoreboard.getOrCreatePlayerScore(s1, pSourceObjective);
            pOperation.apply(score, score1);
         }

         i += score.getScore();
      }

      if (pTargetEntities.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.operation.success.single", pTargetObjectives.getFormattedDisplayName(), pTargetEntities.iterator().next(), i), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.operation.success.multiple", pTargetObjectives.getFormattedDisplayName(), pTargetEntities.size()), true);
      }

      return i;
   }

   private static int enableTrigger(CommandSource pSource, Collection<String> pTargets, ScoreObjective pObjective) throws CommandSyntaxException {
      if (pObjective.getCriteria() != ScoreCriteria.TRIGGER) {
         throw ERROR_NOT_TRIGGER.create();
      } else {
         Scoreboard scoreboard = pSource.getServer().getScoreboard();
         int i = 0;

         for(String s : pTargets) {
            Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
            if (score.isLocked()) {
               score.setLocked(false);
               ++i;
            }
         }

         if (i == 0) {
            throw ERROR_TRIGGER_ALREADY_ENABLED.create();
         } else {
            if (pTargets.size() == 1) {
               pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.enable.success.single", pObjective.getFormattedDisplayName(), pTargets.iterator().next()), true);
            } else {
               pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.enable.success.multiple", pObjective.getFormattedDisplayName(), pTargets.size()), true);
            }

            return i;
         }
      }
   }

   private static int resetScores(CommandSource pSource, Collection<String> pTargets) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(String s : pTargets) {
         scoreboard.resetPlayerScore(s, (ScoreObjective)null);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.reset.all.single", pTargets.iterator().next()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.reset.all.multiple", pTargets.size()), true);
      }

      return pTargets.size();
   }

   private static int resetScore(CommandSource pSource, Collection<String> pTargets, ScoreObjective pObjective) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(String s : pTargets) {
         scoreboard.resetPlayerScore(s, pObjective);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.reset.specific.single", pObjective.getFormattedDisplayName(), pTargets.iterator().next()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.reset.specific.multiple", pObjective.getFormattedDisplayName(), pTargets.size()), true);
      }

      return pTargets.size();
   }

   private static int setScore(CommandSource pSource, Collection<String> pTargets, ScoreObjective pObjective, int pNewValue) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(String s : pTargets) {
         Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
         score.setScore(pNewValue);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.set.success.single", pObjective.getFormattedDisplayName(), pTargets.iterator().next(), pNewValue), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.set.success.multiple", pObjective.getFormattedDisplayName(), pTargets.size(), pNewValue), true);
      }

      return pNewValue * pTargets.size();
   }

   private static int addScore(CommandSource pSource, Collection<String> pTargets, ScoreObjective pObjective, int pAmount) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      int i = 0;

      for(String s : pTargets) {
         Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
         score.setScore(score.getScore() + pAmount);
         i += score.getScore();
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.add.success.single", pAmount, pObjective.getFormattedDisplayName(), pTargets.iterator().next(), i), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.add.success.multiple", pAmount, pObjective.getFormattedDisplayName(), pTargets.size()), true);
      }

      return i;
   }

   private static int removeScore(CommandSource pSource, Collection<String> pTargets, ScoreObjective pObjective, int pAmount) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      int i = 0;

      for(String s : pTargets) {
         Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
         score.setScore(score.getScore() - pAmount);
         i += score.getScore();
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.remove.success.single", pAmount, pObjective.getFormattedDisplayName(), pTargets.iterator().next(), i), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.remove.success.multiple", pAmount, pObjective.getFormattedDisplayName(), pTargets.size()), true);
      }

      return i;
   }

   private static int listTrackedPlayers(CommandSource pSource) {
      Collection<String> collection = pSource.getServer().getScoreboard().getTrackedPlayers();
      if (collection.isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.list.empty"), false);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.list.success", collection.size(), TextComponentUtils.formatList(collection)), false);
      }

      return collection.size();
   }

   private static int listTrackedPlayerScores(CommandSource pSource, String pPlayer) {
      Map<ScoreObjective, Score> map = pSource.getServer().getScoreboard().getPlayerScores(pPlayer);
      if (map.isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.list.entity.empty", pPlayer), false);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.list.entity.success", pPlayer, map.size()), false);

         for(Entry<ScoreObjective, Score> entry : map.entrySet()) {
            pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.players.list.entity.entry", entry.getKey().getFormattedDisplayName(), entry.getValue().getScore()), false);
         }
      }

      return map.size();
   }

   private static int clearDisplaySlot(CommandSource pSource, int pSlotId) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (scoreboard.getDisplayObjective(pSlotId) == null) {
         throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
      } else {
         scoreboard.setDisplayObjective(pSlotId, (ScoreObjective)null);
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.objectives.display.cleared", Scoreboard.getDisplaySlotNames()[pSlotId]), true);
         return 0;
      }
   }

   private static int setDisplaySlot(CommandSource pSource, int pSlotId, ScoreObjective pObjective) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (scoreboard.getDisplayObjective(pSlotId) == pObjective) {
         throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
      } else {
         scoreboard.setDisplayObjective(pSlotId, pObjective);
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.objectives.display.set", Scoreboard.getDisplaySlotNames()[pSlotId], pObjective.getDisplayName()), true);
         return 0;
      }
   }

   private static int setDisplayName(CommandSource pSource, ScoreObjective pObjective, ITextComponent pDisplayName) {
      if (!pObjective.getDisplayName().equals(pDisplayName)) {
         pObjective.setDisplayName(pDisplayName);
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.objectives.modify.displayname", pObjective.getName(), pObjective.getFormattedDisplayName()), true);
      }

      return 0;
   }

   private static int setRenderType(CommandSource pSource, ScoreObjective pObjective, ScoreCriteria.RenderType pRenderType) {
      if (pObjective.getRenderType() != pRenderType) {
         pObjective.setRenderType(pRenderType);
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.objectives.modify.rendertype", pObjective.getFormattedDisplayName()), true);
      }

      return 0;
   }

   private static int removeObjective(CommandSource pSource, ScoreObjective pObjective) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      scoreboard.removeObjective(pObjective);
      pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.objectives.remove.success", pObjective.getFormattedDisplayName()), true);
      return scoreboard.getObjectives().size();
   }

   private static int addObjective(CommandSource pSource, String pName, ScoreCriteria pCriteria, ITextComponent pDisplayName) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (scoreboard.getObjective(pName) != null) {
         throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
      } else if (pName.length() > 16) {
         throw ObjectiveArgument.ERROR_OBJECTIVE_NAME_TOO_LONG.create(16);
      } else {
         scoreboard.addObjective(pName, pCriteria, pDisplayName, pCriteria.getDefaultRenderType());
         ScoreObjective scoreobjective = scoreboard.getObjective(pName);
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.objectives.add.success", scoreobjective.getFormattedDisplayName()), true);
         return scoreboard.getObjectives().size();
      }
   }

   private static int listObjectives(CommandSource pSource) {
      Collection<ScoreObjective> collection = pSource.getServer().getScoreboard().getObjectives();
      if (collection.isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.objectives.list.empty"), false);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.scoreboard.objectives.list.success", collection.size(), TextComponentUtils.formatList(collection, ScoreObjective::getFormattedDisplayName)), false);
      }

      return collection.size();
   }
}