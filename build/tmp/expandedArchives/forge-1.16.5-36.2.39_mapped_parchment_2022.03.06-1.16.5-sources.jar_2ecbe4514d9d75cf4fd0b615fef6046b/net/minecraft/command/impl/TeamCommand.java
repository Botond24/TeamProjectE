package net.minecraft.command.impl;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ColorArgument;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.arguments.ScoreHolderArgument;
import net.minecraft.command.arguments.TeamArgument;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class TeamCommand {
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.add.duplicate"));
   private static final DynamicCommandExceptionType ERROR_TEAM_NAME_TOO_LONG = new DynamicCommandExceptionType((p_208916_0_) -> {
      return new TranslationTextComponent("commands.team.add.longName", p_208916_0_);
   });
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EMPTY = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.empty.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_NAME = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.name.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_COLOR = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.color.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.friendlyfire.alreadyEnabled"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.friendlyfire.alreadyDisabled"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.seeFriendlyInvisibles.alreadyEnabled"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.seeFriendlyInvisibles.alreadyDisabled"));
   private static final SimpleCommandExceptionType ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.nametagVisibility.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.deathMessageVisibility.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_COLLISION_UNCHANGED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.team.option.collisionRule.unchanged"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("team").requires((p_198780_0_) -> {
         return p_198780_0_.hasPermission(2);
      }).then(Commands.literal("list").executes((p_198760_0_) -> {
         return listTeams(p_198760_0_.getSource());
      }).then(Commands.argument("team", TeamArgument.team()).executes((p_198763_0_) -> {
         return listMembers(p_198763_0_.getSource(), TeamArgument.getTeam(p_198763_0_, "team"));
      }))).then(Commands.literal("add").then(Commands.argument("team", StringArgumentType.word()).executes((p_198767_0_) -> {
         return createTeam(p_198767_0_.getSource(), StringArgumentType.getString(p_198767_0_, "team"));
      }).then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((p_198779_0_) -> {
         return createTeam(p_198779_0_.getSource(), StringArgumentType.getString(p_198779_0_, "team"), ComponentArgument.getComponent(p_198779_0_, "displayName"));
      })))).then(Commands.literal("remove").then(Commands.argument("team", TeamArgument.team()).executes((p_198773_0_) -> {
         return deleteTeam(p_198773_0_.getSource(), TeamArgument.getTeam(p_198773_0_, "team"));
      }))).then(Commands.literal("empty").then(Commands.argument("team", TeamArgument.team()).executes((p_198785_0_) -> {
         return emptyTeam(p_198785_0_.getSource(), TeamArgument.getTeam(p_198785_0_, "team"));
      }))).then(Commands.literal("join").then(Commands.argument("team", TeamArgument.team()).executes((p_198758_0_) -> {
         return joinTeam(p_198758_0_.getSource(), TeamArgument.getTeam(p_198758_0_, "team"), Collections.singleton(p_198758_0_.getSource().getEntityOrException().getScoreboardName()));
      }).then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((p_198755_0_) -> {
         return joinTeam(p_198755_0_.getSource(), TeamArgument.getTeam(p_198755_0_, "team"), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198755_0_, "members"));
      })))).then(Commands.literal("leave").then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((p_198765_0_) -> {
         return leaveTeam(p_198765_0_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_198765_0_, "members"));
      }))).then(Commands.literal("modify").then(Commands.argument("team", TeamArgument.team()).then(Commands.literal("displayName").then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((p_211919_0_) -> {
         return setDisplayName(p_211919_0_.getSource(), TeamArgument.getTeam(p_211919_0_, "team"), ComponentArgument.getComponent(p_211919_0_, "displayName"));
      }))).then(Commands.literal("color").then(Commands.argument("value", ColorArgument.color()).executes((p_198762_0_) -> {
         return setColor(p_198762_0_.getSource(), TeamArgument.getTeam(p_198762_0_, "team"), ColorArgument.getColor(p_198762_0_, "value"));
      }))).then(Commands.literal("friendlyFire").then(Commands.argument("allowed", BoolArgumentType.bool()).executes((p_198775_0_) -> {
         return setFriendlyFire(p_198775_0_.getSource(), TeamArgument.getTeam(p_198775_0_, "team"), BoolArgumentType.getBool(p_198775_0_, "allowed"));
      }))).then(Commands.literal("seeFriendlyInvisibles").then(Commands.argument("allowed", BoolArgumentType.bool()).executes((p_198770_0_) -> {
         return setFriendlySight(p_198770_0_.getSource(), TeamArgument.getTeam(p_198770_0_, "team"), BoolArgumentType.getBool(p_198770_0_, "allowed"));
      }))).then(Commands.literal("nametagVisibility").then(Commands.literal("never").executes((p_198778_0_) -> {
         return setNametagVisibility(p_198778_0_.getSource(), TeamArgument.getTeam(p_198778_0_, "team"), Team.Visible.NEVER);
      })).then(Commands.literal("hideForOtherTeams").executes((p_198764_0_) -> {
         return setNametagVisibility(p_198764_0_.getSource(), TeamArgument.getTeam(p_198764_0_, "team"), Team.Visible.HIDE_FOR_OTHER_TEAMS);
      })).then(Commands.literal("hideForOwnTeam").executes((p_198766_0_) -> {
         return setNametagVisibility(p_198766_0_.getSource(), TeamArgument.getTeam(p_198766_0_, "team"), Team.Visible.HIDE_FOR_OWN_TEAM);
      })).then(Commands.literal("always").executes((p_198759_0_) -> {
         return setNametagVisibility(p_198759_0_.getSource(), TeamArgument.getTeam(p_198759_0_, "team"), Team.Visible.ALWAYS);
      }))).then(Commands.literal("deathMessageVisibility").then(Commands.literal("never").executes((p_198789_0_) -> {
         return setDeathMessageVisibility(p_198789_0_.getSource(), TeamArgument.getTeam(p_198789_0_, "team"), Team.Visible.NEVER);
      })).then(Commands.literal("hideForOtherTeams").executes((p_198791_0_) -> {
         return setDeathMessageVisibility(p_198791_0_.getSource(), TeamArgument.getTeam(p_198791_0_, "team"), Team.Visible.HIDE_FOR_OTHER_TEAMS);
      })).then(Commands.literal("hideForOwnTeam").executes((p_198769_0_) -> {
         return setDeathMessageVisibility(p_198769_0_.getSource(), TeamArgument.getTeam(p_198769_0_, "team"), Team.Visible.HIDE_FOR_OWN_TEAM);
      })).then(Commands.literal("always").executes((p_198774_0_) -> {
         return setDeathMessageVisibility(p_198774_0_.getSource(), TeamArgument.getTeam(p_198774_0_, "team"), Team.Visible.ALWAYS);
      }))).then(Commands.literal("collisionRule").then(Commands.literal("never").executes((p_198761_0_) -> {
         return setCollision(p_198761_0_.getSource(), TeamArgument.getTeam(p_198761_0_, "team"), Team.CollisionRule.NEVER);
      })).then(Commands.literal("pushOwnTeam").executes((p_198756_0_) -> {
         return setCollision(p_198756_0_.getSource(), TeamArgument.getTeam(p_198756_0_, "team"), Team.CollisionRule.PUSH_OWN_TEAM);
      })).then(Commands.literal("pushOtherTeams").executes((p_198754_0_) -> {
         return setCollision(p_198754_0_.getSource(), TeamArgument.getTeam(p_198754_0_, "team"), Team.CollisionRule.PUSH_OTHER_TEAMS);
      })).then(Commands.literal("always").executes((p_198790_0_) -> {
         return setCollision(p_198790_0_.getSource(), TeamArgument.getTeam(p_198790_0_, "team"), Team.CollisionRule.ALWAYS);
      }))).then(Commands.literal("prefix").then(Commands.argument("prefix", ComponentArgument.textComponent()).executes((p_207514_0_) -> {
         return setPrefix(p_207514_0_.getSource(), TeamArgument.getTeam(p_207514_0_, "team"), ComponentArgument.getComponent(p_207514_0_, "prefix"));
      }))).then(Commands.literal("suffix").then(Commands.argument("suffix", ComponentArgument.textComponent()).executes((p_207516_0_) -> {
         return setSuffix(p_207516_0_.getSource(), TeamArgument.getTeam(p_207516_0_, "team"), ComponentArgument.getComponent(p_207516_0_, "suffix"));
      }))))));
   }

   /**
    * Removes the listed players from their teams.
    */
   private static int leaveTeam(CommandSource pSource, Collection<String> pPlayers) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(String s : pPlayers) {
         scoreboard.removePlayerFromTeam(s);
      }

      if (pPlayers.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.team.leave.success.single", pPlayers.iterator().next()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.team.leave.success.multiple", pPlayers.size()), true);
      }

      return pPlayers.size();
   }

   private static int joinTeam(CommandSource pSource, ScorePlayerTeam pTeam, Collection<String> pPlayers) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(String s : pPlayers) {
         scoreboard.addPlayerToTeam(s, pTeam);
      }

      if (pPlayers.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.team.join.success.single", pPlayers.iterator().next(), pTeam.getFormattedDisplayName()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.team.join.success.multiple", pPlayers.size(), pTeam.getFormattedDisplayName()), true);
      }

      return pPlayers.size();
   }

   private static int setNametagVisibility(CommandSource pSource, ScorePlayerTeam pTeam, Team.Visible pVisibility) throws CommandSyntaxException {
      if (pTeam.getNameTagVisibility() == pVisibility) {
         throw ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED.create();
      } else {
         pTeam.setNameTagVisibility(pVisibility);
         pSource.sendSuccess(new TranslationTextComponent("commands.team.option.nametagVisibility.success", pTeam.getFormattedDisplayName(), pVisibility.getDisplayName()), true);
         return 0;
      }
   }

   private static int setDeathMessageVisibility(CommandSource pSource, ScorePlayerTeam pTeam, Team.Visible pVisibility) throws CommandSyntaxException {
      if (pTeam.getDeathMessageVisibility() == pVisibility) {
         throw ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED.create();
      } else {
         pTeam.setDeathMessageVisibility(pVisibility);
         pSource.sendSuccess(new TranslationTextComponent("commands.team.option.deathMessageVisibility.success", pTeam.getFormattedDisplayName(), pVisibility.getDisplayName()), true);
         return 0;
      }
   }

   private static int setCollision(CommandSource pSource, ScorePlayerTeam pTeam, Team.CollisionRule pRule) throws CommandSyntaxException {
      if (pTeam.getCollisionRule() == pRule) {
         throw ERROR_TEAM_COLLISION_UNCHANGED.create();
      } else {
         pTeam.setCollisionRule(pRule);
         pSource.sendSuccess(new TranslationTextComponent("commands.team.option.collisionRule.success", pTeam.getFormattedDisplayName(), pRule.getDisplayName()), true);
         return 0;
      }
   }

   private static int setFriendlySight(CommandSource pSource, ScorePlayerTeam pTeam, boolean pValue) throws CommandSyntaxException {
      if (pTeam.canSeeFriendlyInvisibles() == pValue) {
         if (pValue) {
            throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED.create();
         } else {
            throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED.create();
         }
      } else {
         pTeam.setSeeFriendlyInvisibles(pValue);
         pSource.sendSuccess(new TranslationTextComponent("commands.team.option.seeFriendlyInvisibles." + (pValue ? "enabled" : "disabled"), pTeam.getFormattedDisplayName()), true);
         return 0;
      }
   }

   private static int setFriendlyFire(CommandSource pSource, ScorePlayerTeam pTeam, boolean pValue) throws CommandSyntaxException {
      if (pTeam.isAllowFriendlyFire() == pValue) {
         if (pValue) {
            throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED.create();
         } else {
            throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED.create();
         }
      } else {
         pTeam.setAllowFriendlyFire(pValue);
         pSource.sendSuccess(new TranslationTextComponent("commands.team.option.friendlyfire." + (pValue ? "enabled" : "disabled"), pTeam.getFormattedDisplayName()), true);
         return 0;
      }
   }

   private static int setDisplayName(CommandSource pSource, ScorePlayerTeam pTeam, ITextComponent pValue) throws CommandSyntaxException {
      if (pTeam.getDisplayName().equals(pValue)) {
         throw ERROR_TEAM_ALREADY_NAME.create();
      } else {
         pTeam.setDisplayName(pValue);
         pSource.sendSuccess(new TranslationTextComponent("commands.team.option.name.success", pTeam.getFormattedDisplayName()), true);
         return 0;
      }
   }

   private static int setColor(CommandSource pSource, ScorePlayerTeam pTeam, TextFormatting pValue) throws CommandSyntaxException {
      if (pTeam.getColor() == pValue) {
         throw ERROR_TEAM_ALREADY_COLOR.create();
      } else {
         pTeam.setColor(pValue);
         pSource.sendSuccess(new TranslationTextComponent("commands.team.option.color.success", pTeam.getFormattedDisplayName(), pValue.getName()), true);
         return 0;
      }
   }

   private static int emptyTeam(CommandSource pSource, ScorePlayerTeam pTeam) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      Collection<String> collection = Lists.newArrayList(pTeam.getPlayers());
      if (collection.isEmpty()) {
         throw ERROR_TEAM_ALREADY_EMPTY.create();
      } else {
         for(String s : collection) {
            scoreboard.removePlayerFromTeam(s, pTeam);
         }

         pSource.sendSuccess(new TranslationTextComponent("commands.team.empty.success", collection.size(), pTeam.getFormattedDisplayName()), true);
         return collection.size();
      }
   }

   private static int deleteTeam(CommandSource pSource, ScorePlayerTeam pTeam) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      scoreboard.removePlayerTeam(pTeam);
      pSource.sendSuccess(new TranslationTextComponent("commands.team.remove.success", pTeam.getFormattedDisplayName()), true);
      return scoreboard.getPlayerTeams().size();
   }

   private static int createTeam(CommandSource pSource, String pName) throws CommandSyntaxException {
      return createTeam(pSource, pName, new StringTextComponent(pName));
   }

   private static int createTeam(CommandSource pSource, String pName, ITextComponent pDisplayName) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (scoreboard.getPlayerTeam(pName) != null) {
         throw ERROR_TEAM_ALREADY_EXISTS.create();
      } else if (pName.length() > 16) {
         throw ERROR_TEAM_NAME_TOO_LONG.create(16);
      } else {
         ScorePlayerTeam scoreplayerteam = scoreboard.addPlayerTeam(pName);
         scoreplayerteam.setDisplayName(pDisplayName);
         pSource.sendSuccess(new TranslationTextComponent("commands.team.add.success", scoreplayerteam.getFormattedDisplayName()), true);
         return scoreboard.getPlayerTeams().size();
      }
   }

   private static int listMembers(CommandSource pSource, ScorePlayerTeam pTeam) {
      Collection<String> collection = pTeam.getPlayers();
      if (collection.isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.team.list.members.empty", pTeam.getFormattedDisplayName()), false);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.team.list.members.success", pTeam.getFormattedDisplayName(), collection.size(), TextComponentUtils.formatList(collection)), false);
      }

      return collection.size();
   }

   private static int listTeams(CommandSource pSource) {
      Collection<ScorePlayerTeam> collection = pSource.getServer().getScoreboard().getPlayerTeams();
      if (collection.isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.team.list.teams.empty"), false);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.team.list.teams.success", collection.size(), TextComponentUtils.formatList(collection, ScorePlayerTeam::getFormattedDisplayName)), false);
      }

      return collection.size();
   }

   private static int setPrefix(CommandSource pSource, ScorePlayerTeam pTeam, ITextComponent pPrefix) {
      pTeam.setPlayerPrefix(pPrefix);
      pSource.sendSuccess(new TranslationTextComponent("commands.team.option.prefix.success", pPrefix), false);
      return 1;
   }

   private static int setSuffix(CommandSource pSource, ScorePlayerTeam pTeam, ITextComponent pSuffix) {
      pTeam.setPlayerSuffix(pSuffix);
      pSource.sendSuccess(new TranslationTextComponent("commands.team.option.suffix.success", pSuffix), false);
      return 1;
   }
}