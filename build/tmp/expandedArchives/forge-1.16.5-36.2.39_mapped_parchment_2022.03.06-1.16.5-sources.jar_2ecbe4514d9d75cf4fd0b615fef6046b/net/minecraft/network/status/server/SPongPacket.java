package net.minecraft.network.status.server;

import java.io.IOException;
import net.minecraft.client.network.status.IClientStatusNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

public class SPongPacket implements IPacket<IClientStatusNetHandler> {
   private long time;

   public SPongPacket() {
   }

   public SPongPacket(long pTime) {
      this.time = pTime;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.time = p_148837_1_.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeLong(this.time);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientStatusNetHandler pHandler) {
      pHandler.handlePongResponse(this);
   }
}