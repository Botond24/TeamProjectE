package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SPlayerLookPacket implements IPacket<IClientPlayNetHandler> {
   private double x;
   private double y;
   private double z;
   private int entity;
   private EntityAnchorArgument.Type fromAnchor;
   private EntityAnchorArgument.Type toAnchor;
   private boolean atEntity;

   public SPlayerLookPacket() {
   }

   public SPlayerLookPacket(EntityAnchorArgument.Type pFromAnchor, double pX, double pY, double pZ) {
      this.fromAnchor = pFromAnchor;
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }

   public SPlayerLookPacket(EntityAnchorArgument.Type pFromAnchor, Entity pEntity, EntityAnchorArgument.Type pToAnchor) {
      this.fromAnchor = pFromAnchor;
      this.entity = pEntity.getId();
      this.toAnchor = pToAnchor;
      Vector3d vector3d = pToAnchor.apply(pEntity);
      this.x = vector3d.x;
      this.y = vector3d.y;
      this.z = vector3d.z;
      this.atEntity = true;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.fromAnchor = p_148837_1_.readEnum(EntityAnchorArgument.Type.class);
      this.x = p_148837_1_.readDouble();
      this.y = p_148837_1_.readDouble();
      this.z = p_148837_1_.readDouble();
      if (p_148837_1_.readBoolean()) {
         this.atEntity = true;
         this.entity = p_148837_1_.readVarInt();
         this.toAnchor = p_148837_1_.readEnum(EntityAnchorArgument.Type.class);
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.fromAnchor);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeBoolean(this.atEntity);
      if (this.atEntity) {
         pBuffer.writeVarInt(this.entity);
         pBuffer.writeEnum(this.toAnchor);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleLookAt(this);
   }

   @OnlyIn(Dist.CLIENT)
   public EntityAnchorArgument.Type getFromAnchor() {
      return this.fromAnchor;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Vector3d getPosition(World pLevel) {
      if (this.atEntity) {
         Entity entity = pLevel.getEntity(this.entity);
         return entity == null ? new Vector3d(this.x, this.y, this.z) : this.toAnchor.apply(entity);
      } else {
         return new Vector3d(this.x, this.y, this.z);
      }
   }
}