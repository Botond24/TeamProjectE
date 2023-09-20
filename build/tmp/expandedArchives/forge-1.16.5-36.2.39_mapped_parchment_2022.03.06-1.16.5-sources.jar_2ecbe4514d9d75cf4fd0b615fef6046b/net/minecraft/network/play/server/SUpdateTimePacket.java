package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SUpdateTimePacket implements IPacket<IClientPlayNetHandler> {
   private long gameTime;
   private long dayTime;

   public SUpdateTimePacket() {
   }

   public SUpdateTimePacket(long pGameTime, long pDayTime, boolean pDaylightCycleEnabled) {
      this.gameTime = pGameTime;
      this.dayTime = pDayTime;
      if (!pDaylightCycleEnabled) {
         this.dayTime = -this.dayTime;
         if (this.dayTime == 0L) {
            this.dayTime = -1L;
         }
      }

   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.gameTime = p_148837_1_.readLong();
      this.dayTime = p_148837_1_.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeLong(this.gameTime);
      pBuffer.writeLong(this.dayTime);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetTime(this);
   }

   @OnlyIn(Dist.CLIENT)
   public long getGameTime() {
      return this.gameTime;
   }

   @OnlyIn(Dist.CLIENT)
   public long getDayTime() {
      return this.dayTime;
   }
}