package net.minecraft.network.status.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.IServerStatusNetHandler;

public class CServerQueryPacket implements IPacket<IServerStatusNetHandler> {
   public void read(PacketBuffer p_148837_1_) throws IOException {
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerStatusNetHandler pHandler) {
      pHandler.handleStatusRequest(this);
   }
}