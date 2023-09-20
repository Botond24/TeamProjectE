package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSetExperiencePacket implements IPacket<IClientPlayNetHandler> {
   private float experienceProgress;
   private int totalExperience;
   private int experienceLevel;

   public SSetExperiencePacket() {
   }

   public SSetExperiencePacket(float pExperienceProgress, int pTotalExperience, int pExperienceLevel) {
      this.experienceProgress = pExperienceProgress;
      this.totalExperience = pTotalExperience;
      this.experienceLevel = pExperienceLevel;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.experienceProgress = p_148837_1_.readFloat();
      this.experienceLevel = p_148837_1_.readVarInt();
      this.totalExperience = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeFloat(this.experienceProgress);
      pBuffer.writeVarInt(this.experienceLevel);
      pBuffer.writeVarInt(this.totalExperience);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetExperience(this);
   }

   @OnlyIn(Dist.CLIENT)
   public float getExperienceProgress() {
      return this.experienceProgress;
   }

   @OnlyIn(Dist.CLIENT)
   public int getTotalExperience() {
      return this.totalExperience;
   }

   @OnlyIn(Dist.CLIENT)
   public int getExperienceLevel() {
      return this.experienceLevel;
   }
}