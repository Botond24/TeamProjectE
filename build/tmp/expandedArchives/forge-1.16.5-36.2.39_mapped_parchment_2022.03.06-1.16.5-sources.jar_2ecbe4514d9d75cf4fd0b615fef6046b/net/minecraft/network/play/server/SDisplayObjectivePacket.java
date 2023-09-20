package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SDisplayObjectivePacket implements IPacket<IClientPlayNetHandler> {
   private int slot;
   private String objectiveName;

   public SDisplayObjectivePacket() {
   }

   public SDisplayObjectivePacket(int pSlot, @Nullable ScoreObjective pObjective) {
      this.slot = pSlot;
      if (pObjective == null) {
         this.objectiveName = "";
      } else {
         this.objectiveName = pObjective.getName();
      }

   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.slot = p_148837_1_.readByte();
      this.objectiveName = p_148837_1_.readUtf(16);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.slot);
      pBuffer.writeUtf(this.objectiveName);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetDisplayObjective(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getSlot() {
      return this.slot;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public String getObjectiveName() {
      return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
   }
}