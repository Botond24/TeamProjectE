package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEntityTeleportPacket implements IPacket<IClientPlayNetHandler> {
   private int id;
   private double x;
   private double y;
   private double z;
   private byte yRot;
   private byte xRot;
   private boolean onGround;

   public SEntityTeleportPacket() {
   }

   public SEntityTeleportPacket(Entity pEntity) {
      this.id = pEntity.getId();
      this.x = pEntity.getX();
      this.y = pEntity.getY();
      this.z = pEntity.getZ();
      this.yRot = (byte)((int)(pEntity.yRot * 256.0F / 360.0F));
      this.xRot = (byte)((int)(pEntity.xRot * 256.0F / 360.0F));
      this.onGround = pEntity.isOnGround();
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
      this.x = p_148837_1_.readDouble();
      this.y = p_148837_1_.readDouble();
      this.z = p_148837_1_.readDouble();
      this.yRot = p_148837_1_.readByte();
      this.xRot = p_148837_1_.readByte();
      this.onGround = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeByte(this.yRot);
      pBuffer.writeByte(this.xRot);
      pBuffer.writeBoolean(this.onGround);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleTeleportEntity(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
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
   public byte getyRot() {
      return this.yRot;
   }

   @OnlyIn(Dist.CLIENT)
   public byte getxRot() {
      return this.xRot;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isOnGround() {
      return this.onGround;
   }
}