package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.item.Item;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SCooldownPacket implements IPacket<IClientPlayNetHandler> {
   private Item item;
   private int duration;

   public SCooldownPacket() {
   }

   public SCooldownPacket(Item pItem, int pDuration) {
      this.item = pItem;
      this.duration = pDuration;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.item = Item.byId(p_148837_1_.readVarInt());
      this.duration = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(Item.getId(this.item));
      pBuffer.writeVarInt(this.duration);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleItemCooldown(this);
   }

   @OnlyIn(Dist.CLIENT)
   public Item getItem() {
      return this.item;
   }

   @OnlyIn(Dist.CLIENT)
   public int getDuration() {
      return this.duration;
   }
}