package net.minecraft.command.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;

public class KillCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("kill").requires((p_198521_0_) -> {
         return p_198521_0_.hasPermission(2);
      }).executes((p_198520_0_) -> {
         return kill(p_198520_0_.getSource(), ImmutableList.of(p_198520_0_.getSource().getEntityOrException()));
      }).then(Commands.argument("targets", EntityArgument.entities()).executes((p_229810_0_) -> {
         return kill(p_229810_0_.getSource(), EntityArgument.getEntities(p_229810_0_, "targets"));
      })));
   }

   private static int kill(CommandSource pSource, Collection<? extends Entity> pTargets) {
      for(Entity entity : pTargets) {
         entity.kill();
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.kill.success.single", pTargets.iterator().next().getDisplayName()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.kill.success.multiple", pTargets.size()), true);
      }

      return pTargets.size();
   }
}