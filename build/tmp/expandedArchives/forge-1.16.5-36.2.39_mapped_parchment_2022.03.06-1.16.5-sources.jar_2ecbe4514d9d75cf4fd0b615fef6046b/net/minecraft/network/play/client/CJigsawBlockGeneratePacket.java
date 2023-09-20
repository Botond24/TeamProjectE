package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CJigsawBlockGeneratePacket implements IPacket<IServerPlayNetHandler> {
   private BlockPos pos;
   private int levels;
   private boolean keepJigsaws;

   public CJigsawBlockGeneratePacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CJigsawBlockGeneratePacket(BlockPos pPos, int pLevels, boolean pKeepJigsaws) {
      this.pos = pPos;
      this.levels = pLevels;
      this.keepJigsaws = pKeepJigsaws;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.levels = p_148837_1_.readVarInt();
      this.keepJigsaws = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeVarInt(this.levels);
      pBuffer.writeBoolean(this.keepJigsaws);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleJigsawGenerate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int levels() {
      return this.levels;
   }

   public boolean keepJigsaws() {
      return this.keepJigsaws;
   }
}