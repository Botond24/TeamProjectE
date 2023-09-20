package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.world.Difficulty;

public class CSetDifficultyPacket implements IPacket<IServerPlayNetHandler> {
   private Difficulty difficulty;

   public CSetDifficultyPacket() {
   }

   public CSetDifficultyPacket(Difficulty pDifficulty) {
      this.difficulty = pDifficulty;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleChangeDifficulty(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.difficulty = Difficulty.byId(p_148837_1_.readUnsignedByte());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.difficulty.getId());
   }

   public Difficulty getDifficulty() {
      return this.difficulty;
   }
}