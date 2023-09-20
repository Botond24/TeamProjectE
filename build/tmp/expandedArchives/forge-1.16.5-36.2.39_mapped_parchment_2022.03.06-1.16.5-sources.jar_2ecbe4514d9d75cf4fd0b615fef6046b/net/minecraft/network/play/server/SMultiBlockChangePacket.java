package net.minecraft.network.play.server;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.io.IOException;
import java.util.function.BiConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SMultiBlockChangePacket implements IPacket<IClientPlayNetHandler> {
   private SectionPos sectionPos;
   private short[] positions;
   private BlockState[] states;
   private boolean suppressLightUpdates;

   public SMultiBlockChangePacket() {
   }

   public SMultiBlockChangePacket(SectionPos pSectionPos, ShortSet pChangedBlocks, ChunkSection pLevelChunkSection, boolean pSuppressLightUpdates) {
      this.sectionPos = pSectionPos;
      this.suppressLightUpdates = pSuppressLightUpdates;
      this.initFields(pChangedBlocks.size());
      int i = 0;

      for(short short1 : pChangedBlocks) {
         this.positions[i] = short1;
         this.states[i] = pLevelChunkSection.getBlockState(SectionPos.sectionRelativeX(short1), SectionPos.sectionRelativeY(short1), SectionPos.sectionRelativeZ(short1));
         ++i;
      }

   }

   private void initFields(int p_244309_1_) {
      this.positions = new short[p_244309_1_];
      this.states = new BlockState[p_244309_1_];
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.sectionPos = SectionPos.of(p_148837_1_.readLong());
      this.suppressLightUpdates = p_148837_1_.readBoolean();
      int i = p_148837_1_.readVarInt();
      this.initFields(i);

      for(int j = 0; j < this.positions.length; ++j) {
         long k = p_148837_1_.readVarLong();
         this.positions[j] = (short)((int)(k & 4095L));
         this.states[j] = Block.BLOCK_STATE_REGISTRY.byId((int)(k >>> 12));
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeLong(this.sectionPos.asLong());
      pBuffer.writeBoolean(this.suppressLightUpdates);
      pBuffer.writeVarInt(this.positions.length);

      for(int i = 0; i < this.positions.length; ++i) {
         pBuffer.writeVarLong((long)(Block.getId(this.states[i]) << 12 | this.positions[i]));
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleChunkBlocksUpdate(this);
   }

   public void runUpdates(BiConsumer<BlockPos, BlockState> pConsumer) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int i = 0; i < this.positions.length; ++i) {
         short short1 = this.positions[i];
         blockpos$mutable.set(this.sectionPos.relativeToBlockX(short1), this.sectionPos.relativeToBlockY(short1), this.sectionPos.relativeToBlockZ(short1));
         pConsumer.accept(blockpos$mutable, this.states[i]);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldSuppressLightUpdates() {
      return this.suppressLightUpdates;
   }
}