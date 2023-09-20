package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SMoveVehiclePacket implements IPacket<IClientPlayNetHandler> {
   private double x;
   private double y;
   private double z;
   private float yRot;
   private float xRot;

   public SMoveVehiclePacket() {
   }

   public SMoveVehiclePacket(Entity pVehicle) {
      this.x = pVehicle.getX();
      this.y = pVehicle.getY();
      this.z = pVehicle.getZ();
      this.yRot = pVehicle.yRot;
      this.xRot = pVehicle.xRot;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.x = p_148837_1_.readDouble();
      this.y = p_148837_1_.readDouble();
      this.z = p_148837_1_.readDouble();
      this.yRot = p_148837_1_.readFloat();
      this.xRot = p_148837_1_.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeFloat(this.yRot);
      pBuffer.writeFloat(this.xRot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleMoveVehicle(this);
   }

   @OnlyIn(Dist.CLIENT)
   public double getX() {
      return this.x;
   }

   @OnlyIn(Dist.CLIENT)
   public double getY() {
      return this.y;
   }

   @OnlyIn(Dist.CLIENT)
   public double getZ() {
      return this.z;
   }

   @OnlyIn(Dist.CLIENT)
   public float getYRot() {
      return this.yRot;
   }

   @OnlyIn(Dist.CLIENT)
   public float getXRot() {
      return this.xRot;
   }
}