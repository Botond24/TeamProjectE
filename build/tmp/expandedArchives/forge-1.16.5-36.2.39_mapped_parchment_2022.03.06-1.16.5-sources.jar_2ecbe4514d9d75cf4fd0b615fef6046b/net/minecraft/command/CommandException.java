package net.minecraft.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.text.ITextComponent;

public class CommandException extends RuntimeException {
   private final ITextComponent message;

   public CommandException(ITextComponent pMessage) {
      super(pMessage.getString(), (Throwable)null, CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES, CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES);
      this.message = pMessage;
   }

   public ITextComponent getComponent() {
      return this.message;
   }
}