package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SCombatPacket implements IPacket<IClientPlayNetHandler> {
   public SCombatPacket.Event event;
   public int playerId;
   public int killerId;
   public int duration;
   public ITextComponent message;

   public SCombatPacket() {
   }

   public SCombatPacket(CombatTracker p_i46931_1_, SCombatPacket.Event p_i46931_2_) {
      this(p_i46931_1_, p_i46931_2_, StringTextComponent.EMPTY);
   }

   public SCombatPacket(CombatTracker p_i49825_1_, SCombatPacket.Event p_i49825_2_, ITextComponent p_i49825_3_) {
      this.event = p_i49825_2_;
      LivingEntity livingentity = p_i49825_1_.getKiller();
      switch(p_i49825_2_) {
      case END_COMBAT:
         this.duration = p_i49825_1_.getCombatDuration();
         this.killerId = livingentity == null ? -1 : livingentity.getId();
         break;
      case ENTITY_DIED:
         this.playerId = p_i49825_1_.getMob().getId();
         this.killerId = livingentity == null ? -1 : livingentity.getId();
         this.message = p_i49825_3_;
      }

   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.event = p_148837_1_.readEnum(SCombatPacket.Event.class);
      if (this.event == SCombatPacket.Event.END_COMBAT) {
         this.duration = p_148837_1_.readVarInt();
         this.killerId = p_148837_1_.readInt();
      } else if (this.event == SCombatPacket.Event.ENTITY_DIED) {
         this.playerId = p_148837_1_.readVarInt();
         this.killerId = p_148837_1_.readInt();
         this.message = p_148837_1_.readComponent();
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.event);
      if (this.event == SCombatPacket.Event.END_COMBAT) {
         pBuffer.writeVarInt(this.duration);
         pBuffer.writeInt(this.killerId);
      } else if (this.event == SCombatPacket.Event.ENTITY_DIED) {
         pBuffer.writeVarInt(this.playerId);
         pBuffer.writeInt(this.killerId);
         pBuffer.writeComponent(this.message);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handlePlayerCombat(this);
   }

   /**
    * Whether decoding errors will be ignored for this packet.
    */
   public boolean isSkippable() {
      return this.event == SCombatPacket.Event.ENTITY_DIED;
   }

   public static enum Event {
      ENTER_COMBAT,
      END_COMBAT,
      ENTITY_DIED;
   }
}