package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SCameraPacket implements IPacket<IClientPlayNetHandler> {
   public int cameraId;

   public SCameraPacket() {
   }

   public SCameraPacket(Entity pCameraEntity) {
      this.cameraId = pCameraEntity.getId();
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.cameraId = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.cameraId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetCamera(this);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Entity getEntity(World pLevel) {
      return pLevel.getEntity(this.cameraId);
   }
}