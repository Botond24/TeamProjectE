package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSpawnMobPacket implements IPacket<IClientPlayNetHandler> {
   private int id;
   private UUID uuid;
   private int type;
   private double x;
   private double y;
   private double z;
   private int xd;
   private int yd;
   private int zd;
   private byte yRot;
   private byte xRot;
   private byte yHeadRot;

   public SSpawnMobPacket() {
   }

   public SSpawnMobPacket(LivingEntity pEntity) {
      this.id = pEntity.getId();
      this.uuid = pEntity.getUUID();
      this.type = Registry.ENTITY_TYPE.getId(pEntity.getType());
      this.x = pEntity.getX();
      this.y = pEntity.getY();
      this.z = pEntity.getZ();
      this.yRot = (byte)((int)(pEntity.yRot * 256.0F / 360.0F));
      this.xRot = (byte)((int)(pEntity.xRot * 256.0F / 360.0F));
      this.yHeadRot = (byte)((int)(pEntity.yHeadRot * 256.0F / 360.0F));
      double d0 = 3.9D;
      Vector3d vector3d = pEntity.getDeltaMovement();
      double d1 = MathHelper.clamp(vector3d.x, -3.9D, 3.9D);
      double d2 = MathHelper.clamp(vector3d.y, -3.9D, 3.9D);
      double d3 = MathHelper.clamp(vector3d.z, -3.9D, 3.9D);
      this.xd = (int)(d1 * 8000.0D);
      this.yd = (int)(d2 * 8000.0D);
      this.zd = (int)(d3 * 8000.0D);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
      this.uuid = p_148837_1_.readUUID();
      this.type = p_148837_1_.readVarInt();
      this.x = p_148837_1_.readDouble();
      this.y = p_148837_1_.readDouble();
      this.z = p_148837_1_.readDouble();
      this.yRot = p_148837_1_.readByte();
      this.xRot = p_148837_1_.readByte();
      this.yHeadRot = p_148837_1_.readByte();
      this.xd = p_148837_1_.readShort();
      this.yd = p_148837_1_.readShort();
      this.zd = p_148837_1_.readShort();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeUUID(this.uuid);
      pBuffer.writeVarInt(this.type);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeByte(this.yRot);
      pBuffer.writeByte(this.xRot);
      pBuffer.writeByte(this.yHeadRot);
      pBuffer.writeShort(this.xd);
      pBuffer.writeShort(this.yd);
      pBuffer.writeShort(this.zd);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleAddMob(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public UUID getUUID() {
      return this.uuid;
   }

   @OnlyIn(Dist.CLIENT)
   public int getType() {
      return this.type;
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
   public int getXd() {
      return this.xd;
   }

   @OnlyIn(Dist.CLIENT)
   public int getYd() {
      return this.yd;
   }

   @OnlyIn(Dist.CLIENT)
   public int getZd() {
      return this.zd;
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
   public byte getyHeadRot() {
      return this.yHeadRot;
   }
}