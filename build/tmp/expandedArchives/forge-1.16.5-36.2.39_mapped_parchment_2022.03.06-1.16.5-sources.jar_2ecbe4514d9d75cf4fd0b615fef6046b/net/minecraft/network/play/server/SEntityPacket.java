package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEntityPacket implements IPacket<IClientPlayNetHandler> {
   protected int entityId;
   protected short xa;
   protected short ya;
   protected short za;
   protected byte yRot;
   protected byte xRot;
   protected boolean onGround;
   protected boolean hasRot;
   protected boolean hasPos;

   public static long entityToPacket(double pEntityPosition) {
      return MathHelper.lfloor(pEntityPosition * 4096.0D);
   }

   @OnlyIn(Dist.CLIENT)
   public static double packetToEntity(long pPacketPosition) {
      return (double)pPacketPosition / 4096.0D;
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d updateEntityPosition(Vector3d pEntityPosition) {
      double d0 = this.xa == 0 ? pEntityPosition.x : packetToEntity(entityToPacket(pEntityPosition.x) + (long)this.xa);
      double d1 = this.ya == 0 ? pEntityPosition.y : packetToEntity(entityToPacket(pEntityPosition.y) + (long)this.ya);
      double d2 = this.za == 0 ? pEntityPosition.z : packetToEntity(entityToPacket(pEntityPosition.z) + (long)this.za);
      return new Vector3d(d0, d1, d2);
   }

   public static Vector3d packetToEntity(long pX, long pY, long pZ) {
      return (new Vector3d((double)pX, (double)pY, (double)pZ)).scale((double)2.4414062E-4F);
   }

   public SEntityPacket() {
   }

   public SEntityPacket(int p_i46936_1_) {
      this.entityId = p_i46936_1_;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entityId = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.entityId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleMoveEntity(this);
   }

   public String toString() {
      return "Entity_" + super.toString();
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Entity getEntity(World pLevel) {
      return pLevel.getEntity(this.entityId);
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
   public boolean hasRotation() {
      return this.hasRot;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean hasPosition() {
      return this.hasPos;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isOnGround() {
      return this.onGround;
   }

   public static class LookPacket extends SEntityPacket {
      public LookPacket() {
         this.hasRot = true;
      }

      public LookPacket(int pEntityId, byte pYRot, byte pXRot, boolean pOnGround) {
         super(pEntityId);
         this.yRot = pYRot;
         this.xRot = pXRot;
         this.hasRot = true;
         this.onGround = pOnGround;
      }

      public void read(PacketBuffer p_148837_1_) throws IOException {
         super.read(p_148837_1_);
         this.yRot = p_148837_1_.readByte();
         this.xRot = p_148837_1_.readByte();
         this.onGround = p_148837_1_.readBoolean();
      }

      /**
       * Writes the raw packet data to the data stream.
       */
      public void write(PacketBuffer pBuffer) throws IOException {
         super.write(pBuffer);
         pBuffer.writeByte(this.yRot);
         pBuffer.writeByte(this.xRot);
         pBuffer.writeBoolean(this.onGround);
      }
   }

   public static class MovePacket extends SEntityPacket {
      public MovePacket() {
         this.hasRot = true;
         this.hasPos = true;
      }

      public MovePacket(int pEntityId, short pXa, short pYa, short pZa, byte pYRot, byte pXRot, boolean pOnGround) {
         super(pEntityId);
         this.xa = pXa;
         this.ya = pYa;
         this.za = pZa;
         this.yRot = pYRot;
         this.xRot = pXRot;
         this.onGround = pOnGround;
         this.hasRot = true;
         this.hasPos = true;
      }

      public void read(PacketBuffer p_148837_1_) throws IOException {
         super.read(p_148837_1_);
         this.xa = p_148837_1_.readShort();
         this.ya = p_148837_1_.readShort();
         this.za = p_148837_1_.readShort();
         this.yRot = p_148837_1_.readByte();
         this.xRot = p_148837_1_.readByte();
         this.onGround = p_148837_1_.readBoolean();
      }

      /**
       * Writes the raw packet data to the data stream.
       */
      public void write(PacketBuffer pBuffer) throws IOException {
         super.write(pBuffer);
         pBuffer.writeShort(this.xa);
         pBuffer.writeShort(this.ya);
         pBuffer.writeShort(this.za);
         pBuffer.writeByte(this.yRot);
         pBuffer.writeByte(this.xRot);
         pBuffer.writeBoolean(this.onGround);
      }
   }

   public static class RelativeMovePacket extends SEntityPacket {
      public RelativeMovePacket() {
         this.hasPos = true;
      }

      public RelativeMovePacket(int pEntityId, short pXa, short pYa, short pZa, boolean pOnGround) {
         super(pEntityId);
         this.xa = pXa;
         this.ya = pYa;
         this.za = pZa;
         this.onGround = pOnGround;
         this.hasPos = true;
      }

      public void read(PacketBuffer p_148837_1_) throws IOException {
         super.read(p_148837_1_);
         this.xa = p_148837_1_.readShort();
         this.ya = p_148837_1_.readShort();
         this.za = p_148837_1_.readShort();
         this.onGround = p_148837_1_.readBoolean();
      }

      /**
       * Writes the raw packet data to the data stream.
       */
      public void write(PacketBuffer pBuffer) throws IOException {
         super.write(pBuffer);
         pBuffer.writeShort(this.xa);
         pBuffer.writeShort(this.ya);
         pBuffer.writeShort(this.za);
         pBuffer.writeBoolean(this.onGround);
      }
   }
}