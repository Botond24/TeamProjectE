package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CPickItemPacket implements IPacket<IServerPlayNetHandler> {
   private int slot;

   public CPickItemPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CPickItemPacket(int pSlot) {
      this.slot = pSlot;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.slot = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.slot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handlePickItem(this);
   }

   public int getSlot() {
      return this.slot;
   }
}