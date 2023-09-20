package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEntityVelocityPacket implements IPacket<IClientPlayNetHandler> {
   private int id;
   private int xa;
   private int ya;
   private int za;

   public SEntityVelocityPacket() {
   }

   public SEntityVelocityPacket(Entity pEntity) {
      this(pEntity.getId(), pEntity.getDeltaMovement());
   }

   public SEntityVelocityPacket(int pId, Vector3d pDeltaMovement) {
      this.id = pId;
      double d0 = 3.9D;
      double d1 = MathHelper.clamp(pDeltaMovement.x, -3.9D, 3.9D);
      double d2 = MathHelper.clamp(pDeltaMovement.y, -3.9D, 3.9D);
      double d3 = MathHelper.clamp(pDeltaMovement.z, -3.9D, 3.9D);
      this.xa = (int)(d1 * 8000.0D);
      this.ya = (int)(d2 * 8000.0D);
      this.za = (int)(d3 * 8000.0D);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
      this.xa = p_148837_1_.readShort();
      this.ya = p_148837_1_.readShort();
      this.za = p_148837_1_.readShort();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeShort(this.xa);
      pBuffer.writeShort(this.ya);
      pBuffer.writeShort(this.za);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetEntityMotion(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public int getXa() {
      return this.xa;
   }

   @OnlyIn(Dist.CLIENT)
   public int getYa() {
      return this.ya;
   }

   @OnlyIn(Dist.CLIENT)
   public int getZa() {
      return this.za;
   }
}