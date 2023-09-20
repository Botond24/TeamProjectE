package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CEnchantItemPacket implements IPacket<IServerPlayNetHandler> {
   private int containerId;
   private int buttonId;

   public CEnchantItemPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CEnchantItemPacket(int pContainerId, int pButtonId) {
      this.containerId = pContainerId;
      this.buttonId = pButtonId;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleContainerButtonClick(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readByte();
      this.buttonId = p_148837_1_.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeByte(this.buttonId);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getButtonId() {
      return this.buttonId;
   }
}