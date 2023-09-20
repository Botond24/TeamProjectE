package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SUnloadChunkPacket implements IPacket<IClientPlayNetHandler> {
   private int x;
   private int z;

   public SUnloadChunkPacket() {
   }

   public SUnloadChunkPacket(int pX, int pZ) {
      this.x = pX;
      this.z = pZ;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.x = p_148837_1_.readInt();
      this.z = p_148837_1_.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeInt(this.x);
      pBuffer.writeInt(this.z);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleForgetLevelChunk(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getX() {
      return this.x;
   }

   @OnlyIn(Dist.CLIENT)
   public int getZ() {
      return this.z;
   }
}