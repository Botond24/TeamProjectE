package net.minecraft.network.login.server;

import java.io.IOException;
import net.minecraft.client.network.login.IClientLoginNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SCustomPayloadLoginPacket implements IPacket<IClientLoginNetHandler>, net.minecraftforge.fml.network.ICustomPacket<SCustomPayloadLoginPacket> {
   private int transactionId;
   private ResourceLocation identifier;
   private PacketBuffer data;

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.transactionId = p_148837_1_.readVarInt();
      this.identifier = p_148837_1_.readResourceLocation();
      int i = p_148837_1_.readableBytes();
      if (i >= 0 && i <= 1048576) {
         this.data = new PacketBuffer(p_148837_1_.readBytes(i));
      } else {
         throw new IOException("Payload may not be larger than 1048576 bytes");
      }
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeResourceLocation(this.identifier);
      pBuffer.writeBytes(this.data.copy());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientLoginNetHandler pHandler) {
      pHandler.handleCustomQuery(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getTransactionId() {
      return this.transactionId;
   }
}
