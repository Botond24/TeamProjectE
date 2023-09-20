package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEntityHeadLookPacket implements IPacket<IClientPlayNetHandler> {
   private int entityId;
   private byte yHeadRot;

   public SEntityHeadLookPacket() {
   }

   public SEntityHeadLookPacket(Entity pEntity, byte pYHeadRot) {
      this.entityId = pEntity.getId();
      this.yHeadRot = pYHeadRot;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entityId = p_148837_1_.readVarInt();
      this.yHeadRot = p_148837_1_.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeByte(this.yHeadRot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleRotateMob(this);
   }

   @OnlyIn(Dist.CLIENT)
   public Entity getEntity(World pLevel) {
      return pLevel.getEntity(this.entityId);
   }

   @OnlyIn(Dist.CLIENT)
   public byte getYHeadRot() {
      return this.yHeadRot;
   }
}