package net.minecraft.network.login.server;

import java.io.IOException;
import net.minecraft.client.network.login.IClientLoginNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEnableCompressionPacket implements IPacket<IClientLoginNetHandler> {
   private int compressionThreshold;

   public SEnableCompressionPacket() {
   }

   public SEnableCompressionPacket(int pCompressionThreshold) {
      this.compressionThreshold = pCompressionThreshold;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.compressionThreshold = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.compressionThreshold);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientLoginNetHandler pHandler) {
      pHandler.handleCompression(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getCompressionThreshold() {
      return this.compressionThreshold;
   }
}