package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SConfirmTransactionPacket implements IPacket<IClientPlayNetHandler> {
   private int containerId;
   private short uid;
   private boolean accepted;

   public SConfirmTransactionPacket() {
   }

   public SConfirmTransactionPacket(int p_i46958_1_, short p_i46958_2_, boolean p_i46958_3_) {
      this.containerId = p_i46958_1_;
      this.uid = p_i46958_2_;
      this.accepted = p_i46958_3_;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleContainerAck(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readUnsignedByte();
      this.uid = p_148837_1_.readShort();
      this.accepted = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeShort(this.uid);
      pBuffer.writeBoolean(this.accepted);
   }

   @OnlyIn(Dist.CLIENT)
   public int getContainerId() {
      return this.containerId;
   }

   @OnlyIn(Dist.CLIENT)
   public short getUid() {
      return this.uid;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isAccepted() {
      return this.accepted;
   }
}