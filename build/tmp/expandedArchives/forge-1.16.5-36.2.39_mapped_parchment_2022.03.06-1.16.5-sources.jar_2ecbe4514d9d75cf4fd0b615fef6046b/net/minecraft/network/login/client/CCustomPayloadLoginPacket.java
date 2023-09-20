package net.minecraft.network.login.client;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.IServerLoginNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CCustomPayloadLoginPacket implements IPacket<IServerLoginNetHandler>, net.minecraftforge.fml.network.ICustomPacket<CCustomPayloadLoginPacket> {
   private int transactionId;
   private PacketBuffer data;

   public CCustomPayloadLoginPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CCustomPayloadLoginPacket(int pTransactionId, @Nullable PacketBuffer pData) {
      this.transactionId = pTransactionId;
      this.data = pData;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.transactionId = p_148837_1_.readVarInt();
      if (p_148837_1_.readBoolean()) {
         int i = p_148837_1_.readableBytes();
         if (i < 0 || i > 1048576) {
            throw new IOException("Payload may not be larger than 1048576 bytes");
         }

         this.data = new PacketBuffer(p_148837_1_.readBytes(i));
      } else {
         this.data = null;
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.transactionId);
      if (this.data != null) {
         pBuffer.writeBoolean(true);
         pBuffer.writeBytes(this.data.copy());
      } else {
         pBuffer.writeBoolean(false);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerLoginNetHandler pHandler) {
      pHandler.handleCustomQueryPacket(this);
   }
}
