package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SMountEntityPacket implements IPacket<IClientPlayNetHandler> {
   private int sourceId;
   /** The entity that is holding the leash, or -1 to clear the holder. */
   private int destId;

   /**
    * 
    * @param pDestination The entity to link to or {@code null} to break any existing link.
    */
   public SMountEntityPacket() {
   }

   /**
    * 
    * @param pDestination The entity to link to or {@code null} to break any existing link.
    */
   public SMountEntityPacket(Entity pSource, @Nullable Entity pDestination) {
      this.sourceId = pSource.getId();
      this.destId = pDestination != null ? pDestination.getId() : 0;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.sourceId = p_148837_1_.readInt();
      this.destId = p_148837_1_.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeInt(this.sourceId);
      pBuffer.writeInt(this.destId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleEntityLinkPacket(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getSourceId() {
      return this.sourceId;
   }

   @OnlyIn(Dist.CLIENT)
   public int getDestId() {
      return this.destId;
   }
}