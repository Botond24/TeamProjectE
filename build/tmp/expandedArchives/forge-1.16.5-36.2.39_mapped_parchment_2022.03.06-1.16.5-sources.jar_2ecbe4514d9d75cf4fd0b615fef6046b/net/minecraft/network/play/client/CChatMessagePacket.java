package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;

public class CChatMessagePacket implements IPacket<IServerPlayNetHandler> {
   private String message;

   public CChatMessagePacket() {
   }

   public CChatMessagePacket(String pMessage) {
      if (pMessage.length() > 256) {
         pMessage = pMessage.substring(0, 256);
      }

      this.message = pMessage;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.message = p_148837_1_.readUtf(256);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUtf(this.message);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleChat(this);
   }

   public String getMessage() {
      return this.message;
   }
}