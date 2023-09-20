package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SQueryNBTResponsePacket implements IPacket<IClientPlayNetHandler> {
   private int transactionId;
   @Nullable
   private CompoundNBT tag;

   public SQueryNBTResponsePacket() {
   }

   public SQueryNBTResponsePacket(int pTransactionId, @Nullable CompoundNBT pTag) {
      this.transactionId = pTransactionId;
      this.tag = pTag;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.transactionId = p_148837_1_.readVarInt();
      this.tag = p_148837_1_.readNbt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeNbt(this.tag);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleTagQueryPacket(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getTransactionId() {
      return this.transactionId;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public CompoundNBT getTag() {
      return this.tag;
   }

   /**
    * Whether decoding errors will be ignored for this packet.
    */
   public boolean isSkippable() {
      return true;
   }
}