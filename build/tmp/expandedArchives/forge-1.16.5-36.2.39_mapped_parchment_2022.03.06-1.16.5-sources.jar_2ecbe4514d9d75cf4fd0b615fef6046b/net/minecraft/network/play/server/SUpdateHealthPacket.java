package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SUpdateHealthPacket implements IPacket<IClientPlayNetHandler> {
   private float health;
   private int food;
   private float saturation;

   public SUpdateHealthPacket() {
   }

   public SUpdateHealthPacket(float pHealth, int pFood, float pSaturation) {
      this.health = pHealth;
      this.food = pFood;
      this.saturation = pSaturation;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.health = p_148837_1_.readFloat();
      this.food = p_148837_1_.readVarInt();
      this.saturation = p_148837_1_.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeFloat(this.health);
      pBuffer.writeVarInt(this.food);
      pBuffer.writeFloat(this.saturation);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetHealth(this);
   }

   @OnlyIn(Dist.CLIENT)
   public float getHealth() {
      return this.health;
   }

   @OnlyIn(Dist.CLIENT)
   public int getFood() {
      return this.food;
   }

   @OnlyIn(Dist.CLIENT)
   public float getSaturation() {
      return this.saturation;
   }
}