package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CUpdateBeaconPacket implements IPacket<IServerPlayNetHandler> {
   private int primary;
   private int secondary;

   public CUpdateBeaconPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CUpdateBeaconPacket(int pPrimary, int pSecondary) {
      this.primary = pPrimary;
      this.secondary = pSecondary;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.primary = p_148837_1_.readVarInt();
      this.secondary = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.primary);
      pBuffer.writeVarInt(this.secondary);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleSetBeaconPacket(this);
   }

   public int getPrimary() {
      return this.primary;
   }

   public int getSecondary() {
      return this.secondary;
   }
}