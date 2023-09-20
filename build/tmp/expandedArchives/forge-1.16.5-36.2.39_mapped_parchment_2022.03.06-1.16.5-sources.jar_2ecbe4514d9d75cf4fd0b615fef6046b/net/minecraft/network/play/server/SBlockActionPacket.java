package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Triggers a block event on the client.
 * 
 * @see Block#triggerEvent
 * @see Level#blockEvent
 */
public class SBlockActionPacket implements IPacket<IClientPlayNetHandler> {
   private BlockPos pos;
   private int b0;
   private int b1;
   private Block block;

   /**
    * 
    * @param pB0 first parameter of the block event. The meaning of this value depends on the block.
    * @param pB1 second parameter of the block event. The meaning of this value depends on the block.
    */
   public SBlockActionPacket() {
   }

   /**
    * 
    * @param pB0 first parameter of the block event. The meaning of this value depends on the block.
    * @param pB1 second parameter of the block event. The meaning of this value depends on the block.
    */
   public SBlockActionPacket(BlockPos pPos, Block pBlock, int pB0, int pB1) {
      this.pos = pPos;
      this.block = pBlock;
      this.b0 = pB0;
      this.b1 = pB1;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.b0 = p_148837_1_.readUnsignedByte();
      this.b1 = p_148837_1_.readUnsignedByte();
      this.block = Registry.BLOCK.byId(p_148837_1_.readVarInt());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeByte(this.b0);
      pBuffer.writeByte(this.b1);
      pBuffer.writeVarInt(Registry.BLOCK.getId(this.block));
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleBlockEvent(this);
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }

   /**
    * First parameter of the block event. The meaning of this value depends on the block.
    */
   @OnlyIn(Dist.CLIENT)
   public int getB0() {
      return this.b0;
   }

   /**
    * Second parameter of the block event. The meaning of this value depends on the block.
    */
   @OnlyIn(Dist.CLIENT)
   public int getB1() {
      return this.b1;
   }

   @OnlyIn(Dist.CLIENT)
   public Block getBlock() {
      return this.block;
   }
}