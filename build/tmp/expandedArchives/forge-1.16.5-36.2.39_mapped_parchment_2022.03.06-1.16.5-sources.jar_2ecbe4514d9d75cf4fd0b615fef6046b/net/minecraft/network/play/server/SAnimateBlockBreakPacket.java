package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SAnimateBlockBreakPacket implements IPacket<IClientPlayNetHandler> {
   private int id;
   private BlockPos pos;
   private int progress;

   public SAnimateBlockBreakPacket() {
   }

   public SAnimateBlockBreakPacket(int pId, BlockPos pPos, int pProgress) {
      this.id = pId;
      this.pos = pPos;
      this.progress = pProgress;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
      this.pos = p_148837_1_.readBlockPos();
      this.progress = p_148837_1_.readUnsignedByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeByte(this.progress);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleBlockDestruction(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }

   @OnlyIn(Dist.CLIENT)
   public int getProgress() {
      return this.progress;
   }
}