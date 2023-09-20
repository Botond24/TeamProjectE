package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SServerDifficultyPacket implements IPacket<IClientPlayNetHandler> {
   private Difficulty difficulty;
   private boolean locked;

   public SServerDifficultyPacket() {
   }

   public SServerDifficultyPacket(Difficulty pDifficulty, boolean pLocked) {
      this.difficulty = pDifficulty;
      this.locked = pLocked;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleChangeDifficulty(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.difficulty = Difficulty.byId(p_148837_1_.readUnsignedByte());
      this.locked = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.difficulty.getId());
      pBuffer.writeBoolean(this.locked);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isLocked() {
      return this.locked;
   }

   @OnlyIn(Dist.CLIENT)
   public Difficulty getDifficulty() {
      return this.difficulty;
   }
}