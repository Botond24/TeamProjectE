package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;

public class CRenameItemPacket implements IPacket<IServerPlayNetHandler> {
   private String name;

   public CRenameItemPacket() {
   }

   public CRenameItemPacket(String pName) {
      this.name = pName;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.name = p_148837_1_.readUtf(32767);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUtf(this.name);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleRenameItem(this);
   }

   public String getName() {
      return this.name;
   }
}