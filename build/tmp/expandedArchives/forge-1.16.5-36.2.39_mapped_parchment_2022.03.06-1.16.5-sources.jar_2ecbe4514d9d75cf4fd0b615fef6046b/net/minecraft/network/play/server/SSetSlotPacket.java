package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSetSlotPacket implements IPacket<IClientPlayNetHandler> {
   private int containerId;
   private int slot;
   private ItemStack itemStack = ItemStack.EMPTY;

   public SSetSlotPacket() {
   }

   public SSetSlotPacket(int p_i46951_1_, int p_i46951_2_, ItemStack p_i46951_3_) {
      this.containerId = p_i46951_1_;
      this.slot = p_i46951_2_;
      this.itemStack = p_i46951_3_.copy();
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleContainerSetSlot(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readByte();
      this.slot = p_148837_1_.readShort();
      this.itemStack = p_148837_1_.readItem();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeShort(this.slot);
      pBuffer.writeItem(this.itemStack);
   }

   @OnlyIn(Dist.CLIENT)
   public int getContainerId() {
      return this.containerId;
   }

   @OnlyIn(Dist.CLIENT)
   public int getSlot() {
      return this.slot;
   }

   @OnlyIn(Dist.CLIENT)
   public ItemStack getItem() {
      return this.itemStack;
   }
}