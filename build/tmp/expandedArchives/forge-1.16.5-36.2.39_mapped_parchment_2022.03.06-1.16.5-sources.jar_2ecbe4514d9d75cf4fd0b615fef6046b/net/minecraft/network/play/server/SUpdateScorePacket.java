package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SUpdateScorePacket implements IPacket<IClientPlayNetHandler> {
   private String owner = "";
   @Nullable
   private String objectiveName;
   private int score;
   private ServerScoreboard.Action method;

   public SUpdateScorePacket() {
   }

   public SUpdateScorePacket(ServerScoreboard.Action pMethod, @Nullable String pObjectiveName, String pOwner, int pScore) {
      if (pMethod != ServerScoreboard.Action.REMOVE && pObjectiveName == null) {
         throw new IllegalArgumentException("Need an objective name");
      } else {
         this.owner = pOwner;
         this.objectiveName = pObjectiveName;
         this.score = pScore;
         this.method = pMethod;
      }
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.owner = p_148837_1_.readUtf(40);
      this.method = p_148837_1_.readEnum(ServerScoreboard.Action.class);
      String s = p_148837_1_.readUtf(16);
      this.objectiveName = Objects.equals(s, "") ? null : s;
      if (this.method != ServerScoreboard.Action.REMOVE) {
         this.score = p_148837_1_.readVarInt();
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUtf(this.owner);
      pBuffer.writeEnum(this.method);
      pBuffer.writeUtf(this.objectiveName == null ? "" : this.objectiveName);
      if (this.method != ServerScoreboard.Action.REMOVE) {
         pBuffer.writeVarInt(this.score);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetScore(this);
   }

   @OnlyIn(Dist.CLIENT)
   public String getOwner() {
      return this.owner;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public String getObjectiveName() {
      return this.objectiveName;
   }

   @OnlyIn(Dist.CLIENT)
   public int getScore() {
      return this.score;
   }

   @OnlyIn(Dist.CLIENT)
   public ServerScoreboard.Action getMethod() {
      return this.method;
   }
}