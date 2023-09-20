package net.minecraft.network.status.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.IServerStatusNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CPingPacket implements IPacket<IServerStatusNetHandler> {
   private long time;

   public CPingPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CPingPacket(long pTime) {
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
   public void handle(IServerStatusNetHandler pHandler) {
      pHandler.handlePingRequest(this);
   }

   public long getTime() {
      return this.time;
   }
}