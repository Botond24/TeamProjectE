package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSpawnPlayerPacket implements IPacket<IClientPlayNetHandler> {
   private int entityId;
   private UUID playerId;
   private double x;
   private double y;
   private double z;
   private byte yRot;
   private byte xRot;

   public SSpawnPlayerPacket() {
   }

   public SSpawnPlayerPacket(PlayerEntity pPlayer) {
      this.entityId = pPlayer.getId();
      this.playerId = pPlayer.getGameProfile().getId();
      this.x = pPlayer.getX();
      this.y = pPlayer.getY();
      this.z = pPlayer.getZ();
      this.yRot = (byte)((int)(pPlayer.yRot * 256.0F / 360.0F));
      this.xRot = (byte)((int)(pPlayer.xRot * 256.0F / 360.0F));
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entityId = p_148837_1_.readVarInt();
      this.playerId = p_148837_1_.readUUID();
      this.x = p_148837_1_.readDouble();
      this.y = p_148837_1_.readDouble();
      this.z = p_148837_1_.readDouble();
      this.yRot = p_148837_1_.readByte();
      this.xRot = p_148837_1_.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeUUID(this.playerId);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeByte(this.yRot);
      pBuffer.writeByte(this.xRot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleAddPlayer(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }

   @OnlyIn(Dist.CLIENT)
   public UUID getPlayerId() {
      return this.playerId;
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
}