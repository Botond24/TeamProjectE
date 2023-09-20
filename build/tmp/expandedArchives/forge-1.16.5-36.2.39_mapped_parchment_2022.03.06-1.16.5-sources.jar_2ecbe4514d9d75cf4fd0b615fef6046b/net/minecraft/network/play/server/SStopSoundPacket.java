package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SStopSoundPacket implements IPacket<IClientPlayNetHandler> {
   private ResourceLocation name;
   private SoundCategory source;

   public SStopSoundPacket() {
   }

   public SStopSoundPacket(@Nullable ResourceLocation pName, @Nullable SoundCategory pSource) {
      this.name = pName;
      this.source = pSource;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      int i = p_148837_1_.readByte();
      if ((i & 1) > 0) {
         this.source = p_148837_1_.readEnum(SoundCategory.class);
      }

      if ((i & 2) > 0) {
         this.name = p_148837_1_.readResourceLocation();
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      if (this.source != null) {
         if (this.name != null) {
            pBuffer.writeByte(3);
            pBuffer.writeEnum(this.source);
            pBuffer.writeResourceLocation(this.name);
         } else {
            pBuffer.writeByte(1);
            pBuffer.writeEnum(this.source);
         }
      } else if (this.name != null) {
         pBuffer.writeByte(2);
         pBuffer.writeResourceLocation(this.name);
      } else {
         pBuffer.writeByte(0);
      }

   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public ResourceLocation getName() {
      return this.name;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public SoundCategory getSource() {
      return this.source;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleStopSoundEvent(this);
   }
}