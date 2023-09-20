package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class STagsListPacket implements IPacket<IClientPlayNetHandler> {
   private ITagCollectionSupplier tags;

   public STagsListPacket() {
   }

   public STagsListPacket(ITagCollectionSupplier p_i242087_1_) {
      this.tags = p_i242087_1_;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.tags = ITagCollectionSupplier.deserializeFromNetwork(p_148837_1_);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      this.tags.serializeToNetwork(pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleUpdateTags(this);
   }

   @OnlyIn(Dist.CLIENT)
   public ITagCollectionSupplier getTags() {
      return this.tags;
   }
}