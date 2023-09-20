package net.minecraft.network.play.client;

import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.world.server.ServerWorld;

public class CSpectatePacket implements IPacket<IServerPlayNetHandler> {
   private UUID uuid;

   public CSpectatePacket() {
   }

   public CSpectatePacket(UUID pUuid) {
      this.uuid = pUuid;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.uuid = p_148837_1_.readUUID();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUUID(this.uuid);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleTeleportToEntityPacket(this);
   }

   @Nullable
   public Entity getEntity(ServerWorld pLevel) {
      return pLevel.getEntity(this.uuid);
   }
}