package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SScoreboardObjectivePacket implements IPacket<IClientPlayNetHandler> {
   private String objectiveName;
   private ITextComponent displayName;
   private ScoreCriteria.RenderType renderType;
   private int method;

   public SScoreboardObjectivePacket() {
   }

   public SScoreboardObjectivePacket(ScoreObjective pObjective, int pMethod) {
      this.objectiveName = pObjective.getName();
      this.displayName = pObjective.getDisplayName();
      this.renderType = pObjective.getRenderType();
      this.method = pMethod;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.objectiveName = p_148837_1_.readUtf(16);
      this.method = p_148837_1_.readByte();
      if (this.method == 0 || this.method == 2) {
         this.displayName = p_148837_1_.readComponent();
         this.renderType = p_148837_1_.readEnum(ScoreCriteria.RenderType.class);
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUtf(this.objectiveName);
      pBuffer.writeByte(this.method);
      if (this.method == 0 || this.method == 2) {
         pBuffer.writeComponent(this.displayName);
         pBuffer.writeEnum(this.renderType);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleAddObjective(this);
   }

   @OnlyIn(Dist.CLIENT)
   public String getObjectiveName() {
      return this.objectiveName;
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getDisplayName() {
      return this.displayName;
   }

   @OnlyIn(Dist.CLIENT)
   public int getMethod() {
      return this.method;
   }

   @OnlyIn(Dist.CLIENT)
   public ScoreCriteria.RenderType getRenderType() {
      return this.renderType;
   }
}