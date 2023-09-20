package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.HTTPUtil;
import net.minecraft.util.text.TranslationTextComponent;

public class PublishCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.publish.failed"));
   private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType((p_208900_0_) -> {
      return new TranslationTextComponent("commands.publish.alreadyPublished", p_208900_0_);
   });

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("publish").requires((p_198583_0_) -> {
         return p_198583_0_.hasPermission(4);
      }).executes((p_198580_0_) -> {
         return publish(p_198580_0_.getSource(), HTTPUtil.getAvailablePort());
      }).then(Commands.argument("port", IntegerArgumentType.integer(0, 65535)).executes((p_198582_0_) -> {
         return publish(p_198582_0_.getSource(), IntegerArgumentType.getInteger(p_198582_0_, "port"));
      })));
   }

   private static int publish(CommandSource pSource, int pPort) throws CommandSyntaxException {
      if (pSource.getServer().isPublished()) {
         throw ERROR_ALREADY_PUBLISHED.create(pSource.getServer().getPort());
      } else if (!pSource.getServer().publishServer(pSource.getServer().getDefaultGameType(), false, pPort)) {
         throw ERROR_FAILED.create();
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.publish.success", pPort), true);
         return pPort;
      }
   }
}