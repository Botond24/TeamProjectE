package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SPlayEntityEffectPacket implements IPacket<IClientPlayNetHandler> {
   private int entityId;
   private byte effectId;
   private byte effectAmplifier;
   private int effectDurationTicks;
   private byte flags;

   public SPlayEntityEffectPacket() {
   }

   public SPlayEntityEffectPacket(int pEntityId, EffectInstance pEffectInstance) {
      this.entityId = pEntityId;
      this.effectId = (byte)(Effect.getId(pEffectInstance.getEffect()) & 255);
      this.effectAmplifier = (byte)(pEffectInstance.getAmplifier() & 255);
      if (pEffectInstance.getDuration() > 32767) {
         this.effectDurationTicks = 32767;
      } else {
         this.effectDurationTicks = pEffectInstance.getDuration();
      }

      this.flags = 0;
      if (pEffectInstance.isAmbient()) {
         this.flags = (byte)(this.flags | 1);
      }

      if (pEffectInstance.isVisible()) {
         this.flags = (byte)(this.flags | 2);
      }

      if (pEffectInstance.showIcon()) {
         this.flags = (byte)(this.flags | 4);
      }

   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entityId = p_148837_1_.readVarInt();
      this.effectId = p_148837_1_.readByte();
      this.effectAmplifier = p_148837_1_.readByte();
      this.effectDurationTicks = p_148837_1_.readVarInt();
      this.flags = p_148837_1_.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeByte(this.effectId);
      pBuffer.writeByte(this.effectAmplifier);
      pBuffer.writeVarInt(this.effectDurationTicks);
      pBuffer.writeByte(this.flags);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isSuperLongDuration() {
      return this.effectDurationTicks == 32767;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleUpdateMobEffect(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }

   @OnlyIn(Dist.CLIENT)
   public byte getEffectId() {
      return this.effectId;
   }

   @OnlyIn(Dist.CLIENT)
   public byte getEffectAmplifier() {
      return this.effectAmplifier;
   }

   @OnlyIn(Dist.CLIENT)
   public int getEffectDurationTicks() {
      return this.effectDurationTicks;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isEffectVisible() {
      return (this.flags & 2) == 2;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isEffectAmbient() {
      return (this.flags & 1) == 1;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean effectShowsIcon() {
      return (this.flags & 4) == 4;
   }
}