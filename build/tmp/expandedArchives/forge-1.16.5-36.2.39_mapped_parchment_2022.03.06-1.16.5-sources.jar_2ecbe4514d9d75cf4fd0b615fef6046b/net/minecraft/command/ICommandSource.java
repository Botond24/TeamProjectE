package net.minecraft.command;

import java.util.UUID;
import net.minecraft.util.text.ITextComponent;

public interface ICommandSource {
   /** A {@code CommandSource} that ignores all messages. */
   ICommandSource NULL = new ICommandSource() {
      /**
       * Send a chat message to the CommandSender
       */
      public void sendMessage(ITextComponent pComponent, UUID pSenderUUID) {
      }

      public boolean acceptsSuccess() {
         return false;
      }

      public boolean acceptsFailure() {
         return false;
      }

      public boolean shouldInformAdmins() {
         return false;
      }
   };

   /**
    * Send a chat message to the CommandSender
    */
   void sendMessage(ITextComponent pComponent, UUID pSenderUUID);

   boolean acceptsSuccess();

   boolean acceptsFailure();

   boolean shouldInformAdmins();
}