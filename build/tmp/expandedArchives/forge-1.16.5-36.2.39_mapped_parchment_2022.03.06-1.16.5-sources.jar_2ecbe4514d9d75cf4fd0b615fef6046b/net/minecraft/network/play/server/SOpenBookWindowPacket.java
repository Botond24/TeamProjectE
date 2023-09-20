package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SOpenBookWindowPacket implements IPacket<IClientPlayNetHandler> {
   private Hand hand;

   public SOpenBookWindowPacket() {
   }

   public SOpenBookWindowPacket(Hand pHand) {
      this.hand = pHand;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.hand = p_148837_1_.readEnum(Hand.class);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.hand);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleOpenBook(this);
   }

   @OnlyIn(Dist.CLIENT)
   public Hand getHand() {
      return this.hand;
   }
}