package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec2Argument;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderCommand {
   private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType(new TranslationTextComponent("commands.worldborder.center.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_SIZE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.worldborder.set.failed.nochange"));
   private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType(new TranslationTextComponent("commands.worldborder.set.failed.small."));
   private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType(new TranslationTextComponent("commands.worldborder.set.failed.big."));
   private static final SimpleCommandExceptionType ERROR_SAME_WARNING_TIME = new SimpleCommandExceptionType(new TranslationTextComponent("commands.worldborder.warning.time.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new SimpleCommandExceptionType(new TranslationTextComponent("commands.worldborder.warning.distance.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new SimpleCommandExceptionType(new TranslationTextComponent("commands.worldborder.damage.buffer.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new SimpleCommandExceptionType(new TranslationTextComponent("commands.worldborder.damage.amount.failed"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("worldborder").requires((p_198903_0_) -> {
         return p_198903_0_.hasPermission(2);
      }).then(Commands.literal("add").then(Commands.argument("distance", FloatArgumentType.floatArg(-6.0E7F, 6.0E7F)).executes((p_198908_0_) -> {
         return setSize(p_198908_0_.getSource(), p_198908_0_.getSource().getLevel().getWorldBorder().getSize() + (double)FloatArgumentType.getFloat(p_198908_0_, "distance"), 0L);
      }).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_198901_0_) -> {
         return setSize(p_198901_0_.getSource(), p_198901_0_.getSource().getLevel().getWorldBorder().getSize() + (double)FloatArgumentType.getFloat(p_198901_0_, "distance"), p_198901_0_.getSource().getLevel().getWorldBorder().getLerpRemainingTime() + (long)IntegerArgumentType.getInteger(p_198901_0_, "time") * 1000L);
      })))).then(Commands.literal("set").then(Commands.argument("distance", FloatArgumentType.floatArg(-6.0E7F, 6.0E7F)).executes((p_198906_0_) -> {
         return setSize(p_198906_0_.getSource(), (double)FloatArgumentType.getFloat(p_198906_0_, "distance"), 0L);
      }).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_198909_0_) -> {
         return setSize(p_198909_0_.getSource(), (double)FloatArgumentType.getFloat(p_198909_0_, "distance"), (long)IntegerArgumentType.getInteger(p_198909_0_, "time") * 1000L);
      })))).then(Commands.literal("center").then(Commands.argument("pos", Vec2Argument.vec2()).executes((p_198893_0_) -> {
         return setCenter(p_198893_0_.getSource(), Vec2Argument.getVec2(p_198893_0_, "pos"));
      }))).then(Commands.literal("damage").then(Commands.literal("amount").then(Commands.argument("damagePerBlock", FloatArgumentType.floatArg(0.0F)).executes((p_198897_0_) -> {
         return setDamageAmount(p_198897_0_.getSource(), FloatArgumentType.getFloat(p_198897_0_, "damagePerBlock"));
      }))).then(Commands.literal("buffer").then(Commands.argument("distance", FloatArgumentType.floatArg(0.0F)).executes((p_198905_0_) -> {
         return setDamageBuffer(p_198905_0_.getSource(), FloatArgumentType.getFloat(p_198905_0_, "distance"));
      })))).then(Commands.literal("get").executes((p_198900_0_) -> {
         return getSize(p_198900_0_.getSource());
      })).then(Commands.literal("warning").then(Commands.literal("distance").then(Commands.argument("distance", IntegerArgumentType.integer(0)).executes((p_198892_0_) -> {
         return setWarningDistance(p_198892_0_.getSource(), IntegerArgumentType.getInteger(p_198892_0_, "distance"));
      }))).then(Commands.literal("time").then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_198907_0_) -> {
         return setWarningTime(p_198907_0_.getSource(), IntegerArgumentType.getInteger(p_198907_0_, "time"));
      })))));
   }

   private static int setDamageBuffer(CommandSource pSource, float pDistance) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getLevel().getWorldBorder();
      if (worldborder.getDamageSafeZone() == (double)pDistance) {
         throw ERROR_SAME_DAMAGE_BUFFER.create();
      } else {
         worldborder.setDamageSafeZone((double)pDistance);
         pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", pDistance)), true);
         return (int)pDistance;
      }
   }

   private static int setDamageAmount(CommandSource pSource, float pDamagePerBlock) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getLevel().getWorldBorder();
      if (worldborder.getDamagePerBlock() == (double)pDamagePerBlock) {
         throw ERROR_SAME_DAMAGE_AMOUNT.create();
      } else {
         worldborder.setDamagePerBlock((double)pDamagePerBlock);
         pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", pDamagePerBlock)), true);
         return (int)pDamagePerBlock;
      }
   }

   private static int setWarningTime(CommandSource pSource, int pTime) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getLevel().getWorldBorder();
      if (worldborder.getWarningTime() == pTime) {
         throw ERROR_SAME_WARNING_TIME.create();
      } else {
         worldborder.setWarningTime(pTime);
         pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.warning.time.success", pTime), true);
         return pTime;
      }
   }

   private static int setWarningDistance(CommandSource pSource, int pDistance) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getLevel().getWorldBorder();
      if (worldborder.getWarningBlocks() == pDistance) {
         throw ERROR_SAME_WARNING_DISTANCE.create();
      } else {
         worldborder.setWarningBlocks(pDistance);
         pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.warning.distance.success", pDistance), true);
         return pDistance;
      }
   }

   private static int getSize(CommandSource pSource) {
      double d0 = pSource.getLevel().getWorldBorder().getSize();
      pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", d0)), false);
      return MathHelper.floor(d0 + 0.5D);
   }

   private static int setCenter(CommandSource pSource, Vector2f pPos) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getLevel().getWorldBorder();
      if (worldborder.getCenterX() == (double)pPos.x && worldborder.getCenterZ() == (double)pPos.y) {
         throw ERROR_SAME_CENTER.create();
      } else {
         worldborder.setCenter((double)pPos.x, (double)pPos.y);
         pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", pPos.x), String.format("%.2f", pPos.y)), true);
         return 0;
      }
   }

   private static int setSize(CommandSource pSource, double pNewSize, long pTime) throws CommandSyntaxException {
      WorldBorder worldborder = pSource.getLevel().getWorldBorder();
      double d0 = worldborder.getSize();
      if (d0 == pNewSize) {
         throw ERROR_SAME_SIZE.create();
      } else if (pNewSize < 1.0D) {
         throw ERROR_TOO_SMALL.create();
      } else if (pNewSize > 6.0E7D) {
         throw ERROR_TOO_BIG.create();
      } else {
         if (pTime > 0L) {
            worldborder.lerpSizeBetween(d0, pNewSize, pTime);
            if (pNewSize > d0) {
               pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.set.grow", String.format(Locale.ROOT, "%.1f", pNewSize), Long.toString(pTime / 1000L)), true);
            } else {
               pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.set.shrink", String.format(Locale.ROOT, "%.1f", pNewSize), Long.toString(pTime / 1000L)), true);
            }
         } else {
            worldborder.setSize(pNewSize);
            pSource.sendSuccess(new TranslationTextComponent("commands.worldborder.set.immediate", String.format(Locale.ROOT, "%.1f", pNewSize)), true);
         }

         return (int)(pNewSize - d0);
      }
   }
}