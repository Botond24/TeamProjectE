package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CConfirmTeleportPacket implements IPacket<IServerPlayNetHandler> {
   private int id;

   public CConfirmTeleportPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CConfirmTeleportPacket(int pId) {
      this.id = pId;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleAcceptTeleportPacket(this);
   }

   public int getId() {
      return this.id;
   }
}