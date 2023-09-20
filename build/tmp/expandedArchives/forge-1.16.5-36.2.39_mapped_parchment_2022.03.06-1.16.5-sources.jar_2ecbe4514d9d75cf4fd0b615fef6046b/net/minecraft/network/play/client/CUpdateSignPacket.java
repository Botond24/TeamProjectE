package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CUpdateSignPacket implements IPacket<IServerPlayNetHandler> {
   private BlockPos pos;
   private String[] lines;

   public CUpdateSignPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CUpdateSignPacket(BlockPos pPos, String pLine0, String pLine1, String pLine2, String pLine3) {
      this.pos = pPos;
      this.lines = new String[]{pLine0, pLine1, pLine2, pLine3};
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.lines = new String[4];

      for(int i = 0; i < 4; ++i) {
         this.lines[i] = p_148837_1_.readUtf(384);
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);

      for(int i = 0; i < 4; ++i) {
         pBuffer.writeUtf(this.lines[i]);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleSignUpdate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public String[] getLines() {
      return this.lines;
   }
}