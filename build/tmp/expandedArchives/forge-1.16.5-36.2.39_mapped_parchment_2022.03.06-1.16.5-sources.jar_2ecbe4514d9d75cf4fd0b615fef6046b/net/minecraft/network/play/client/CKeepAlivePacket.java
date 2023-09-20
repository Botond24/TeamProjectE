package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CKeepAlivePacket implements IPacket<IServerPlayNetHandler> {
   private long id;

   public CKeepAlivePacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CKeepAlivePacket(long pId) {
      this.id = pId;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleKeepAlive(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeLong(this.id);
   }

   public long getId() {
      return this.id;
   }
}