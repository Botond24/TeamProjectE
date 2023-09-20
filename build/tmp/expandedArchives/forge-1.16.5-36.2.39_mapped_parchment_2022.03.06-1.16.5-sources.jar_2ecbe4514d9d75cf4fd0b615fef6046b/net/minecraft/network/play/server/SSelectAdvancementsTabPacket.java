package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSelectAdvancementsTabPacket implements IPacket<IClientPlayNetHandler> {
   @Nullable
   private ResourceLocation tab;

   public SSelectAdvancementsTabPacket() {
   }

   public SSelectAdvancementsTabPacket(@Nullable ResourceLocation pTab) {
      this.tab = pTab;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSelectAdvancementsTab(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      if (p_148837_1_.readBoolean()) {
         this.tab = p_148837_1_.readResourceLocation();
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBoolean(this.tab != null);
      if (this.tab != null) {
         pBuffer.writeResourceLocation(this.tab);
      }

   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public ResourceLocation getTab() {
      return this.tab;
   }
}