package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSetPassengersPacket implements IPacket<IClientPlayNetHandler> {
   private int vehicle;
   private int[] passengers;

   public SSetPassengersPacket() {
   }

   public SSetPassengersPacket(Entity pVehicle) {
      this.vehicle = pVehicle.getId();
      List<Entity> list = pVehicle.getPassengers();
      this.passengers = new int[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         this.passengers[i] = list.get(i).getId();
      }

   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.vehicle = p_148837_1_.readVarInt();
      this.passengers = p_148837_1_.readVarIntArray();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.vehicle);
      pBuffer.writeVarIntArray(this.passengers);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetEntityPassengersPacket(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int[] getPassengers() {
      return this.passengers;
   }

   @OnlyIn(Dist.CLIENT)
   public int getVehicle() {
      return this.vehicle;
   }
}