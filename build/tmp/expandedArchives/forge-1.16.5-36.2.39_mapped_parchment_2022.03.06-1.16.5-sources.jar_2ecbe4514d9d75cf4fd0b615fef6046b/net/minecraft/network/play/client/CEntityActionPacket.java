package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CEntityActionPacket implements IPacket<IServerPlayNetHandler> {
   private int id;
   private CEntityActionPacket.Action action;
   private int data;

   public CEntityActionPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CEntityActionPacket(Entity pEntity, CEntityActionPacket.Action pAction) {
      this(pEntity, pAction, 0);
   }

   @OnlyIn(Dist.CLIENT)
   public CEntityActionPacket(Entity pEntity, CEntityActionPacket.Action pAction, int pData) {
      this.id = pEntity.getId();
      this.action = pAction;
      this.data = pData;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
      this.action = p_148837_1_.readEnum(CEntityActionPacket.Action.class);
      this.data = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeEnum(this.action);
      pBuffer.writeVarInt(this.data);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handlePlayerCommand(this);
   }

   public CEntityActionPacket.Action getAction() {
      return this.action;
   }

   public int getData() {
      return this.data;
   }

   public static enum Action {
      PRESS_SHIFT_KEY,
      RELEASE_SHIFT_KEY,
      STOP_SLEEPING,
      START_SPRINTING,
      STOP_SPRINTING,
      START_RIDING_JUMP,
      STOP_RIDING_JUMP,
      OPEN_INVENTORY,
      START_FALL_FLYING;
   }
}