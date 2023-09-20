package net.minecraft.command.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.PotionArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.TranslationTextComponent;

public class EffectCommand {
   private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.effect.give.failed"));
   private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.effect.clear.everything.failed"));
   private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.effect.clear.specific.failed"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("effect").requires((p_198359_0_) -> {
         return p_198359_0_.hasPermission(2);
      }).then(Commands.literal("clear").executes((p_198352_0_) -> {
         return clearEffects(p_198352_0_.getSource(), ImmutableList.of(p_198352_0_.getSource().getEntityOrException()));
      }).then(Commands.argument("targets", EntityArgument.entities()).executes((p_198356_0_) -> {
         return clearEffects(p_198356_0_.getSource(), EntityArgument.getEntities(p_198356_0_, "targets"));
      }).then(Commands.argument("effect", PotionArgument.effect()).executes((p_198351_0_) -> {
         return clearEffect(p_198351_0_.getSource(), EntityArgument.getEntities(p_198351_0_, "targets"), PotionArgument.getEffect(p_198351_0_, "effect"));
      })))).then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("effect", PotionArgument.effect()).executes((p_198357_0_) -> {
         return giveEffect(p_198357_0_.getSource(), EntityArgument.getEntities(p_198357_0_, "targets"), PotionArgument.getEffect(p_198357_0_, "effect"), (Integer)null, 0, true);
      }).then(Commands.argument("seconds", IntegerArgumentType.integer(1, 1000000)).executes((p_198350_0_) -> {
         return giveEffect(p_198350_0_.getSource(), EntityArgument.getEntities(p_198350_0_, "targets"), PotionArgument.getEffect(p_198350_0_, "effect"), IntegerArgumentType.getInteger(p_198350_0_, "seconds"), 0, true);
      }).then(Commands.argument("amplifier", IntegerArgumentType.integer(0, 255)).executes((p_198358_0_) -> {
         return giveEffect(p_198358_0_.getSource(), EntityArgument.getEntities(p_198358_0_, "targets"), PotionArgument.getEffect(p_198358_0_, "effect"), IntegerArgumentType.getInteger(p_198358_0_, "seconds"), IntegerArgumentType.getInteger(p_198358_0_, "amplifier"), true);
      }).then(Commands.argument("hideParticles", BoolArgumentType.bool()).executes((p_229759_0_) -> {
         return giveEffect(p_229759_0_.getSource(), EntityArgument.getEntities(p_229759_0_, "targets"), PotionArgument.getEffect(p_229759_0_, "effect"), IntegerArgumentType.getInteger(p_229759_0_, "seconds"), IntegerArgumentType.getInteger(p_229759_0_, "amplifier"), !BoolArgumentType.getBool(p_229759_0_, "hideParticles"));
      }))))))));
   }

   private static int giveEffect(CommandSource pSource, Collection<? extends Entity> pTargets, Effect pEffect, @Nullable Integer pSeconds, int pAmplifier, boolean pShowParticles) throws CommandSyntaxException {
      int i = 0;
      int j;
      if (pSeconds != null) {
         if (pEffect.isInstantenous()) {
            j = pSeconds;
         } else {
            j = pSeconds * 20;
         }
      } else if (pEffect.isInstantenous()) {
         j = 1;
      } else {
         j = 600;
      }

      for(Entity entity : pTargets) {
         if (entity instanceof LivingEntity) {
            EffectInstance effectinstance = new EffectInstance(pEffect, j, pAmplifier, false, pShowParticles);
            if (((LivingEntity)entity).addEffect(effectinstance)) {
               ++i;
            }
         }
      }

      if (i == 0) {
         throw ERROR_GIVE_FAILED.create();
      } else {
         if (pTargets.size() == 1) {
            pSource.sendSuccess(new TranslationTextComponent("commands.effect.give.success.single", pEffect.getDisplayName(), pTargets.iterator().next().getDisplayName(), j / 20), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.effect.give.success.multiple", pEffect.getDisplayName(), pTargets.size(), j / 20), true);
         }

         return i;
      }
   }

   private static int clearEffects(CommandSource pSource, Collection<? extends Entity> pTargets) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : pTargets) {
         if (entity instanceof LivingEntity && ((LivingEntity)entity).removeAllEffects()) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_CLEAR_EVERYTHING_FAILED.create();
      } else {
         if (pTargets.size() == 1) {
            pSource.sendSuccess(new TranslationTextComponent("commands.effect.clear.everything.success.single", pTargets.iterator().next().getDisplayName()), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.effect.clear.everything.success.multiple", pTargets.size()), true);
         }

         return i;
      }
   }

   private static int clearEffect(CommandSource pSource, Collection<? extends Entity> pTargets, Effect pEffect) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : pTargets) {
         if (entity instanceof LivingEntity && ((LivingEntity)entity).removeEffect(pEffect)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_CLEAR_SPECIFIC_FAILED.create();
      } else {
         if (pTargets.size() == 1) {
            pSource.sendSuccess(new TranslationTextComponent("commands.effect.clear.specific.success.single", pEffect.getDisplayName(), pTargets.iterator().next().getDisplayName()), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent("commands.effect.clear.specific.success.multiple", pEffect.getDisplayName(), pTargets.size()), true);
         }

         return i;
      }
   }
}