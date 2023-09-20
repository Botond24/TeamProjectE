package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CPlayerPacket implements IPacket<IServerPlayNetHandler> {
   protected double x;
   protected double y;
   protected double z;
   protected float yRot;
   protected float xRot;
   protected boolean onGround;
   protected boolean hasPos;
   protected boolean hasRot;

   public CPlayerPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CPlayerPacket(boolean p_i46875_1_) {
      this.onGround = p_i46875_1_;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleMovePlayer(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.onGround = p_148837_1_.readUnsignedByte() != 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.onGround ? 1 : 0);
   }

   public double getX(double pDefaultValue) {
      return this.hasPos ? this.x : pDefaultValue;
   }

   public double getY(double pDefaultValue) {
      return this.hasPos ? this.y : pDefaultValue;
   }

   public double getZ(double pDefaultValue) {
      return this.hasPos ? this.z : pDefaultValue;
   }

   public float getYRot(float pDefaultValue) {
      return this.hasRot ? this.yRot : pDefaultValue;
   }

   public float getXRot(float pDefaultValue) {
      return this.hasRot ? this.xRot : pDefaultValue;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public static class PositionPacket extends CPlayerPacket {
      public PositionPacket() {
         this.hasPos = true;
      }

      @OnlyIn(Dist.CLIENT)
      public PositionPacket(double pX, double pY, double pZ, boolean pOnGround) {
         this.x = pX;
         this.y = pY;
         this.z = pZ;
         this.onGround = pOnGround;
         this.hasPos = true;
      }

      public void read(PacketBuffer p_148837_1_) throws IOException {
         this.x = p_148837_1_.readDouble();
         this.y = p_148837_1_.readDouble();
         this.z = p_148837_1_.readDouble();
         super.read(p_148837_1_);
      }

      /**
       * Writes the raw packet data to the data stream.
       */
      public void write(PacketBuffer pBuffer) throws IOException {
         pBuffer.writeDouble(this.x);
         pBuffer.writeDouble(this.y);
         pBuffer.writeDouble(this.z);
         super.write(pBuffer);
      }
   }

   public static class PositionRotationPacket extends CPlayerPacket {
      public PositionRotationPacket() {
         this.hasPos = true;
         this.hasRot = true;
      }

      @OnlyIn(Dist.CLIENT)
      public PositionRotationPacket(double pX, double pY, double pZ, float pYRot, float pXRot, boolean pOnGround) {
         this.x = pX;
         this.y = pY;
         this.z = pZ;
         this.yRot = pYRot;
         this.xRot = pXRot;
         this.onGround = pOnGround;
         this.hasRot = true;
         this.hasPos = true;
      }

      public void read(PacketBuffer p_148837_1_) throws IOException {
         this.x = p_148837_1_.readDouble();
         this.y = p_148837_1_.readDouble();
         this.z = p_148837_1_.readDouble();
         this.yRot = p_148837_1_.readFloat();
         this.xRot = p_148837_1_.readFloat();
         super.read(p_148837_1_);
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
         super.write(pBuffer);
      }
   }

   public static class RotationPacket extends CPlayerPacket {
      public RotationPacket() {
         this.hasRot = true;
      }

      @OnlyIn(Dist.CLIENT)
      public RotationPacket(float pYRot, float pXRot, boolean pOnGround) {
         this.yRot = pYRot;
         this.xRot = pXRot;
         this.onGround = pOnGround;
         this.hasRot = true;
      }

      public void read(PacketBuffer p_148837_1_) throws IOException {
         this.yRot = p_148837_1_.readFloat();
         this.xRot = p_148837_1_.readFloat();
         super.read(p_148837_1_);
      }

      /**
       * Writes the raw packet data to the data stream.
       */
      public void write(PacketBuffer pBuffer) throws IOException {
         pBuffer.writeFloat(this.yRot);
         pBuffer.writeFloat(this.xRot);
         super.write(pBuffer);
      }
   }
}