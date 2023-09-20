package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;

public class CPlayerAbilitiesPacket implements IPacket<IServerPlayNetHandler> {
   private boolean isFlying;

   public CPlayerAbilitiesPacket() {
   }

   public CPlayerAbilitiesPacket(PlayerAbilities pAbilities) {
      this.isFlying = pAbilities.flying;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      byte b0 = p_148837_1_.readByte();
      this.isFlying = (b0 & 2) != 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      byte b0 = 0;
      if (this.isFlying) {
         b0 = (byte)(b0 | 2);
      }

      pBuffer.writeByte(b0);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handlePlayerAbilities(this);
   }

   public boolean isFlying() {
      return this.isFlying;
   }
}