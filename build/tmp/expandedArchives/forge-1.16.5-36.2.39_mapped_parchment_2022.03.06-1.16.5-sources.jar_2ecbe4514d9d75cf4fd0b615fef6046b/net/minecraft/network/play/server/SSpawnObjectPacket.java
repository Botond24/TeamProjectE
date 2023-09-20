package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSpawnObjectPacket implements IPacket<IClientPlayNetHandler> {
   private int id;
   private UUID uuid;
   private double x;
   private double y;
   private double z;
   private int xa;
   private int ya;
   private int za;
   private int xRot;
   private int yRot;
   private EntityType<?> type;
   private int data;

   public SSpawnObjectPacket() {
   }

   public SSpawnObjectPacket(int pId, UUID pUuid, double pX, double pY, double pZ, float pXRot, float pYRot, EntityType<?> pType, int pData, Vector3d pDeltaMovement) {
      this.id = pId;
      this.uuid = pUuid;
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.xRot = MathHelper.floor(pXRot * 256.0F / 360.0F);
      this.yRot = MathHelper.floor(pYRot * 256.0F / 360.0F);
      this.type = pType;
      this.data = pData;
      this.xa = (int)(MathHelper.clamp(pDeltaMovement.x, -3.9D, 3.9D) * 8000.0D);
      this.ya = (int)(MathHelper.clamp(pDeltaMovement.y, -3.9D, 3.9D) * 8000.0D);
      this.za = (int)(MathHelper.clamp(pDeltaMovement.z, -3.9D, 3.9D) * 8000.0D);
   }

   public SSpawnObjectPacket(Entity pEntity) {
      this(pEntity, 0);
   }

   public SSpawnObjectPacket(Entity pEntity, int pData) {
      this(pEntity.getId(), pEntity.getUUID(), pEntity.getX(), pEntity.getY(), pEntity.getZ(), pEntity.xRot, pEntity.yRot, pEntity.getType(), pData, pEntity.getDeltaMovement());
   }

   public SSpawnObjectPacket(Entity pEntity, EntityType<?> pType, int pData, BlockPos pPos) {
      this(pEntity.getId(), pEntity.getUUID(), (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), pEntity.xRot, pEntity.yRot, pType, pData, pEntity.getDeltaMovement());
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
      this.uuid = p_148837_1_.readUUID();
      this.type = Registry.ENTITY_TYPE.byId(p_148837_1_.readVarInt());
      this.x = p_148837_1_.readDouble();
      this.y = p_148837_1_.readDouble();
      this.z = p_148837_1_.readDouble();
      this.xRot = p_148837_1_.readByte();
      this.yRot = p_148837_1_.readByte();
      this.data = p_148837_1_.readInt();
      this.xa = p_148837_1_.readShort();
      this.ya = p_148837_1_.readShort();
      this.za = p_148837_1_.readShort();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeUUID(this.uuid);
      pBuffer.writeVarInt(Registry.ENTITY_TYPE.getId(this.type));
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeByte(this.xRot);
      pBuffer.writeByte(this.yRot);
      pBuffer.writeInt(this.data);
      pBuffer.writeShort(this.xa);
      pBuffer.writeShort(this.ya);
      pBuffer.writeShort(this.za);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleAddEntity(this);
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
   public double getXa() {
      return (double)this.xa / 8000.0D;
   }

   @OnlyIn(Dist.CLIENT)
   public double getYa() {
      return (double)this.ya / 8000.0D;
   }

   @OnlyIn(Dist.CLIENT)
   public double getZa() {
      return (double)this.za / 8000.0D;
   }

   @OnlyIn(Dist.CLIENT)
   public int getxRot() {
      return this.xRot;
   }

   @OnlyIn(Dist.CLIENT)
   public int getyRot() {
      return this.yRot;
   }

   @OnlyIn(Dist.CLIENT)
   public EntityType<?> getType() {
      return this.type;
   }

   @OnlyIn(Dist.CLIENT)
   public int getData() {
      return this.data;
   }
}