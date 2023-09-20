package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CSelectTradePacket implements IPacket<IServerPlayNetHandler> {
   private int item;

   public CSelectTradePacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CSelectTradePacket(int pItem) {
      this.item = pItem;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.item = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.item);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleSelectTrade(this);
   }

   public int getItem() {
      return this.item;
   }
}