package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;

public class BossBarCommand {
   private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType((p_208783_0_) -> {
      return new TranslationTextComponent("commands.bossbar.create.failed", p_208783_0_);
   });
   private static final DynamicCommandExceptionType ERROR_DOESNT_EXIST = new DynamicCommandExceptionType((p_208782_0_) -> {
      return new TranslationTextComponent("commands.bossbar.unknown", p_208782_0_);
   });
   private static final SimpleCommandExceptionType ERROR_NO_PLAYER_CHANGE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.bossbar.set.players.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_NAME_CHANGE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.bossbar.set.name.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_COLOR_CHANGE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.bossbar.set.color.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_STYLE_CHANGE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.bossbar.set.style.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_VALUE_CHANGE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.bossbar.set.value.unchanged"));
   private static final SimpleCommandExceptionType ERROR_NO_MAX_CHANGE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.bossbar.set.max.unchanged"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_HIDDEN = new SimpleCommandExceptionType(new TranslationTextComponent("commands.bossbar.set.visibility.unchanged.hidden"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_VISIBLE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.bossbar.set.visibility.unchanged.visible"));
   public static final SuggestionProvider<CommandSource> SUGGEST_BOSS_BAR = (p_201404_0_, p_201404_1_) -> {
      return ISuggestionProvider.suggestResource(p_201404_0_.getSource().getServer().getCustomBossEvents().getIds(), p_201404_1_);
   };

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("bossbar").requires((p_201423_0_) -> {
         return p_201423_0_.hasPermission(2);
      }).then(Commands.literal("add").then(Commands.argument("id", ResourceLocationArgument.id()).then(Commands.argument("name", ComponentArgument.textComponent()).executes((p_201426_0_) -> {
         return createBar(p_201426_0_.getSource(), ResourceLocationArgument.getId(p_201426_0_, "id"), ComponentArgument.getComponent(p_201426_0_, "name"));
      })))).then(Commands.literal("remove").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).executes((p_201429_0_) -> {
         return removeBar(p_201429_0_.getSource(), getBossBar(p_201429_0_));
      }))).then(Commands.literal("list").executes((p_201396_0_) -> {
         return listBars(p_201396_0_.getSource());
      })).then(Commands.literal("set").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).then(Commands.literal("name").then(Commands.argument("name", ComponentArgument.textComponent()).executes((p_201401_0_) -> {
         return setName(p_201401_0_.getSource(), getBossBar(p_201401_0_), ComponentArgument.getComponent(p_201401_0_, "name"));
      }))).then(Commands.literal("color").then(Commands.literal("pink").executes((p_201409_0_) -> {
         return setColor(p_201409_0_.getSource(), getBossBar(p_201409_0_), BossInfo.Color.PINK);
      })).then(Commands.literal("blue").executes((p_201422_0_) -> {
         return setColor(p_201422_0_.getSource(), getBossBar(p_201422_0_), BossInfo.Color.BLUE);
      })).then(Commands.literal("red").executes((p_201417_0_) -> {
         return setColor(p_201417_0_.getSource(), getBossBar(p_201417_0_), BossInfo.Color.RED);
      })).then(Commands.literal("green").executes((p_201424_0_) -> {
         return setColor(p_201424_0_.getSource(), getBossBar(p_201424_0_), BossInfo.Color.GREEN);
      })).then(Commands.literal("yellow").executes((p_201393_0_) -> {
         return setColor(p_201393_0_.getSource(), getBossBar(p_201393_0_), BossInfo.Color.YELLOW);
      })).then(Commands.literal("purple").executes((p_201391_0_) -> {
         return setColor(p_201391_0_.getSource(), getBossBar(p_201391_0_), BossInfo.Color.PURPLE);
      })).then(Commands.literal("white").executes((p_201406_0_) -> {
         return setColor(p_201406_0_.getSource(), getBossBar(p_201406_0_), BossInfo.Color.WHITE);
      }))).then(Commands.literal("style").then(Commands.literal("progress").executes((p_201399_0_) -> {
         return setStyle(p_201399_0_.getSource(), getBossBar(p_201399_0_), BossInfo.Overlay.PROGRESS);
      })).then(Commands.literal("notched_6").executes((p_201419_0_) -> {
         return setStyle(p_201419_0_.getSource(), getBossBar(p_201419_0_), BossInfo.Overlay.NOTCHED_6);
      })).then(Commands.literal("notched_10").executes((p_201412_0_) -> {
         return setStyle(p_201412_0_.getSource(), getBossBar(p_201412_0_), BossInfo.Overlay.NOTCHED_10);
      })).then(Commands.literal("notched_12").executes((p_201421_0_) -> {
         return setStyle(p_201421_0_.getSource(), getBossBar(p_201421_0_), BossInfo.Overlay.NOTCHED_12);
      })).then(Commands.literal("notched_20").executes((p_201403_0_) -> {
         return setStyle(p_201403_0_.getSource(), getBossBar(p_201403_0_), BossInfo.Overlay.NOTCHED_20);
      }))).then(Commands.literal("value").then(Commands.argument("value", IntegerArgumentType.integer(0)).executes((p_201408_0_) -> {
         return setValue(p_201408_0_.getSource(), getBossBar(p_201408_0_), IntegerArgumentType.getInteger(p_201408_0_, "value"));
      }))).then(Commands.literal("max").then(Commands.argument("max", IntegerArgumentType.integer(1)).executes((p_201395_0_) -> {
         return setMax(p_201395_0_.getSource(), getBossBar(p_201395_0_), IntegerArgumentType.getInteger(p_201395_0_, "max"));
      }))).then(Commands.literal("visible").then(Commands.argument("visible", BoolArgumentType.bool()).executes((p_201427_0_) -> {
         return setVisible(p_201427_0_.getSource(), getBossBar(p_201427_0_), BoolArgumentType.getBool(p_201427_0_, "visible"));
      }))).then(Commands.literal("players").executes((p_201430_0_) -> {
         return setPlayers(p_201430_0_.getSource(), getBossBar(p_201430_0_), Collections.emptyList());
      }).then(Commands.argument("targets", EntityArgument.players()).executes((p_201411_0_) -> {
         return setPlayers(p_201411_0_.getSource(), getBossBar(p_201411_0_), EntityArgument.getOptionalPlayers(p_201411_0_, "targets"));
      }))))).then(Commands.literal("get").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS_BAR).then(Commands.literal("value").executes((p_201418_0_) -> {
         return getValue(p_201418_0_.getSource(), getBossBar(p_201418_0_));
      })).then(Commands.literal("max").executes((p_201398_0_) -> {
         return getMax(p_201398_0_.getSource(), getBossBar(p_201398_0_));
      })).then(Commands.literal("visible").executes((p_201392_0_) -> {
         return getVisible(p_201392_0_.getSource(), getBossBar(p_201392_0_));
      })).then(Commands.literal("players").executes((p_201388_0_) -> {
         return getPlayers(p_201388_0_.getSource(), getBossBar(p_201388_0_));
      })))));
   }

   private static int getValue(CommandSource pSource, CustomServerBossInfo pBossbar) {
      pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.get.value", pBossbar.getDisplayName(), pBossbar.getValue()), true);
      return pBossbar.getValue();
   }

   private static int getMax(CommandSource pSource, CustomServerBossInfo pBossbar) {
      pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.get.max", pBossbar.getDisplayName(), pBossbar.getMax()), true);
      return pBossbar.getMax();
   }

   private static int getVisible(CommandSource pSource, CustomServerBossInfo pBossbar) {
      if (pBossbar.isVisible()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.get.visible.visible", pBossbar.getDisplayName()), true);
         return 1;
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.get.visible.hidden", pBossbar.getDisplayName()), true);
         return 0;
      }
   }

   private static int getPlayers(CommandSource pSource, CustomServerBossInfo pBossbar) {
      if (pBossbar.getPlayers().isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.get.players.none", pBossbar.getDisplayName()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.get.players.some", pBossbar.getDisplayName(), pBossbar.getPlayers().size(), TextComponentUtils.formatList(pBossbar.getPlayers(), PlayerEntity::getDisplayName)), true);
      }

      return pBossbar.getPlayers().size();
   }

   private static int setVisible(CommandSource pSource, CustomServerBossInfo pBossbar, boolean pVisible) throws CommandSyntaxException {
      if (pBossbar.isVisible() == pVisible) {
         if (pVisible) {
            throw ERROR_ALREADY_VISIBLE.create();
         } else {
            throw ERROR_ALREADY_HIDDEN.create();
         }
      } else {
         pBossbar.setVisible(pVisible);
         if (pVisible) {
            pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.visible.success.visible", pBossbar.getDisplayName()), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.visible.success.hidden", pBossbar.getDisplayName()), true);
         }

         return 0;
      }
   }

   private static int setValue(CommandSource pSource, CustomServerBossInfo pBossbar, int pValue) throws CommandSyntaxException {
      if (pBossbar.getValue() == pValue) {
         throw ERROR_NO_VALUE_CHANGE.create();
      } else {
         pBossbar.setValue(pValue);
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.value.success", pBossbar.getDisplayName(), pValue), true);
         return pValue;
      }
   }

   private static int setMax(CommandSource pSource, CustomServerBossInfo pBossbar, int pMax) throws CommandSyntaxException {
      if (pBossbar.getMax() == pMax) {
         throw ERROR_NO_MAX_CHANGE.create();
      } else {
         pBossbar.setMax(pMax);
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.max.success", pBossbar.getDisplayName(), pMax), true);
         return pMax;
      }
   }

   private static int setColor(CommandSource pSource, CustomServerBossInfo pBossbar, BossInfo.Color pColor) throws CommandSyntaxException {
      if (pBossbar.getColor().equals(pColor)) {
         throw ERROR_NO_COLOR_CHANGE.create();
      } else {
         pBossbar.setColor(pColor);
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.color.success", pBossbar.getDisplayName()), true);
         return 0;
      }
   }

   private static int setStyle(CommandSource pSource, CustomServerBossInfo pBossbar, BossInfo.Overlay pStyle) throws CommandSyntaxException {
      if (pBossbar.getOverlay().equals(pStyle)) {
         throw ERROR_NO_STYLE_CHANGE.create();
      } else {
         pBossbar.setOverlay(pStyle);
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.style.success", pBossbar.getDisplayName()), true);
         return 0;
      }
   }

   private static int setName(CommandSource pSource, CustomServerBossInfo pBossbar, ITextComponent pName) throws CommandSyntaxException {
      ITextComponent itextcomponent = TextComponentUtils.updateForEntity(pSource, pName, (Entity)null, 0);
      if (pBossbar.getName().equals(itextcomponent)) {
         throw ERROR_NO_NAME_CHANGE.create();
      } else {
         pBossbar.setName(itextcomponent);
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.name.success", pBossbar.getDisplayName()), true);
         return 0;
      }
   }

   private static int setPlayers(CommandSource pSource, CustomServerBossInfo pBossbar, Collection<ServerPlayerEntity> pPlayers) throws CommandSyntaxException {
      boolean flag = pBossbar.setPlayers(pPlayers);
      if (!flag) {
         throw ERROR_NO_PLAYER_CHANGE.create();
      } else {
         if (pBossbar.getPlayers().isEmpty()) {
            pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.players.success.none", pBossbar.getDisplayName()), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.set.players.success.some", pBossbar.getDisplayName(), pPlayers.size(), TextComponentUtils.formatList(pPlayers, PlayerEntity::getDisplayName)), true);
         }

         return pBossbar.getPlayers().size();
      }
   }

   private static int listBars(CommandSource pSource) {
      Collection<CustomServerBossInfo> collection = pSource.getServer().getCustomBossEvents().getEvents();
      if (collection.isEmpty()) {
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.list.bars.none"), false);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.list.bars.some", collection.size(), TextComponentUtils.formatList(collection, CustomServerBossInfo::getDisplayName)), false);
      }

      return collection.size();
   }

   private static int createBar(CommandSource pSource, ResourceLocation pId, ITextComponent pDisplayName) throws CommandSyntaxException {
      CustomServerBossInfoManager customserverbossinfomanager = pSource.getServer().getCustomBossEvents();
      if (customserverbossinfomanager.get(pId) != null) {
         throw ERROR_ALREADY_EXISTS.create(pId.toString());
      } else {
         CustomServerBossInfo customserverbossinfo = customserverbossinfomanager.create(pId, TextComponentUtils.updateForEntity(pSource, pDisplayName, (Entity)null, 0));
         pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.create.success", customserverbossinfo.getDisplayName()), true);
         return customserverbossinfomanager.getEvents().size();
      }
   }

   private static int removeBar(CommandSource pSource, CustomServerBossInfo pBossbar) {
      CustomServerBossInfoManager customserverbossinfomanager = pSource.getServer().getCustomBossEvents();
      pBossbar.removeAllPlayers();
      customserverbossinfomanager.remove(pBossbar);
      pSource.sendSuccess(new TranslationTextComponent("commands.bossbar.remove.success", pBossbar.getDisplayName()), true);
      return customserverbossinfomanager.getEvents().size();
   }

   public static CustomServerBossInfo getBossBar(CommandContext<CommandSource> pSource) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocationArgument.getId(pSource, "id");
      CustomServerBossInfo customserverbossinfo = pSource.getSource().getServer().getCustomBossEvents().get(resourcelocation);
      if (customserverbossinfo == null) {
         throw ERROR_DOESNT_EXIST.create(resourcelocation.toString());
      } else {
         return customserverbossinfo;
      }
   }
}