package net.minecraft.client.gui.chat;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NormalChatListener implements IChatListener {
   private final Minecraft minecraft;

   public NormalChatListener(Minecraft p_i47393_1_) {
      this.minecraft = p_i47393_1_;
   }

   /**
    * Called whenever this listener receives a chat message, if this listener is registered to the given type in {@link
    * net.minecraft.client.gui.GuiIngame#chatListeners chatListeners}
    */
   public void handle(ChatType pChatType, ITextComponent pMessage, UUID pSender) {
      if (pChatType != ChatType.CHAT) {
         this.minecraft.gui.getChat().addMessage(pMessage);
      } else {
         this.minecraft.gui.getChat().enqueueMessage(pMessage);
      }

   }
}