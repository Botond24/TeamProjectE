package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SUpdateTileEntityPacket implements IPacket<IClientPlayNetHandler> {
   private BlockPos pos;
   /** Used only for vanilla tile entities */
   private int type;
   private CompoundNBT tag;

   public SUpdateTileEntityPacket() {
   }

   public SUpdateTileEntityPacket(BlockPos pPos, int pType, CompoundNBT pTag) {
      this.pos = pPos;
      this.type = pType;
      this.tag = pTag;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.type = p_148837_1_.readUnsignedByte();
      this.tag = p_148837_1_.readNbt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeByte((byte)this.type);
      pBuffer.writeNbt(this.tag);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleBlockEntityData(this);
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }

   @OnlyIn(Dist.CLIENT)
   public int getType() {
      return this.type;
   }

   @OnlyIn(Dist.CLIENT)
   public CompoundNBT getTag() {
      return this.tag;
   }
}