package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;

public class CResourcePackStatusPacket implements IPacket<IServerPlayNetHandler> {
   private CResourcePackStatusPacket.Action action;

   public CResourcePackStatusPacket() {
   }

   public CResourcePackStatusPacket(CResourcePackStatusPacket.Action pAction) {
      this.action = pAction;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.action = p_148837_1_.readEnum(CResourcePackStatusPacket.Action.class);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.action);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleResourcePackResponse(this);
   }

   public static enum Action {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED;
   }
}