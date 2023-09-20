package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SCollectItemPacket implements IPacket<IClientPlayNetHandler> {
   private int itemId;
   private int playerId;
   private int amount;

   public SCollectItemPacket() {
   }

   public SCollectItemPacket(int pItemId, int pPlayerId, int pAmount) {
      this.itemId = pItemId;
      this.playerId = pPlayerId;
      this.amount = pAmount;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.itemId = p_148837_1_.readVarInt();
      this.playerId = p_148837_1_.readVarInt();
      this.amount = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.itemId);
      pBuffer.writeVarInt(this.playerId);
      pBuffer.writeVarInt(this.amount);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleTakeItemEntity(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getItemId() {
      return this.itemId;
   }

   @OnlyIn(Dist.CLIENT)
   public int getPlayerId() {
      return this.playerId;
   }

   @OnlyIn(Dist.CLIENT)
   public int getAmount() {
      return this.amount;
   }
}