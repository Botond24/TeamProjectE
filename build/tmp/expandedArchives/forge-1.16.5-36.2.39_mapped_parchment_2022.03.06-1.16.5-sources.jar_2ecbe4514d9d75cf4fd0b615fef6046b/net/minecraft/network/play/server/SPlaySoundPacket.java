package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SPlaySoundPacket implements IPacket<IClientPlayNetHandler> {
   private ResourceLocation name;
   private SoundCategory source;
   private int x;
   private int y = Integer.MAX_VALUE;
   private int z;
   private float volume;
   private float pitch;

   public SPlaySoundPacket() {
   }

   public SPlaySoundPacket(ResourceLocation pName, SoundCategory pSource, Vector3d pPosition, float pVolume, float pPitch) {
      this.name = pName;
      this.source = pSource;
      this.x = (int)(pPosition.x * 8.0D);
      this.y = (int)(pPosition.y * 8.0D);
      this.z = (int)(pPosition.z * 8.0D);
      this.volume = pVolume;
      this.pitch = pPitch;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.name = p_148837_1_.readResourceLocation();
      this.source = p_148837_1_.readEnum(SoundCategory.class);
      this.x = p_148837_1_.readInt();
      this.y = p_148837_1_.readInt();
      this.z = p_148837_1_.readInt();
      this.volume = p_148837_1_.readFloat();
      this.pitch = p_148837_1_.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeResourceLocation(this.name);
      pBuffer.writeEnum(this.source);
      pBuffer.writeInt(this.x);
      pBuffer.writeInt(this.y);
      pBuffer.writeInt(this.z);
      pBuffer.writeFloat(this.volume);
      pBuffer.writeFloat(this.pitch);
   }

   @OnlyIn(Dist.CLIENT)
   public ResourceLocation getName() {
      return this.name;
   }

   @OnlyIn(Dist.CLIENT)
   public SoundCategory getSource() {
      return this.source;
   }

   @OnlyIn(Dist.CLIENT)
   public double getX() {
      return (double)((float)this.x / 8.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public double getY() {
      return (double)((float)this.y / 8.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public double getZ() {
      return (double)((float)this.z / 8.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public float getVolume() {
      return this.volume;
   }

   @OnlyIn(Dist.CLIENT)
   public float getPitch() {
      return this.pitch;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleCustomSoundEvent(this);
   }
}