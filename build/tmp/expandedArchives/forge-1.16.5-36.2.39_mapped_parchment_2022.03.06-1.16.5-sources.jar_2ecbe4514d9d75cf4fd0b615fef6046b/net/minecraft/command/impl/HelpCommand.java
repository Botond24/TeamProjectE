package net.minecraft.command.impl;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Map;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class HelpCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.help.failed"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("help").executes((p_198511_1_) -> {
         Map<CommandNode<CommandSource>, String> map = pDispatcher.getSmartUsage(pDispatcher.getRoot(), p_198511_1_.getSource());

         for(String s : map.values()) {
            p_198511_1_.getSource().sendSuccess(new StringTextComponent("/" + s), false);
         }

         return map.size();
      }).then(Commands.argument("command", StringArgumentType.greedyString()).executes((p_198512_1_) -> {
         ParseResults<CommandSource> parseresults = pDispatcher.parse(StringArgumentType.getString(p_198512_1_, "command"), p_198512_1_.getSource());
         if (parseresults.getContext().getNodes().isEmpty()) {
            throw ERROR_FAILED.create();
         } else {
            Map<CommandNode<CommandSource>, String> map = pDispatcher.getSmartUsage(Iterables.getLast(parseresults.getContext().getNodes()).getNode(), p_198512_1_.getSource());

            for(String s : map.values()) {
               p_198512_1_.getSource().sendSuccess(new StringTextComponent("/" + parseresults.getReader().getString() + " " + s), false);
            }

            return map.size();
         }
      })));
   }
}