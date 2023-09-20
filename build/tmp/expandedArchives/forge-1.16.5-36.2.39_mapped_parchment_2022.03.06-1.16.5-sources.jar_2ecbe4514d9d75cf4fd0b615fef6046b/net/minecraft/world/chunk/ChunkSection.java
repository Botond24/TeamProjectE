package net.minecraft.world.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.palette.IPalette;
import net.minecraft.util.palette.IdentityPalette;
import net.minecraft.util.palette.PalettedContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChunkSection {
   private static final IPalette<BlockState> GLOBAL_BLOCKSTATE_PALETTE = new IdentityPalette<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState());
   private final int bottomBlockY;
   private short nonEmptyBlockCount;
   private short tickingBlockCount;
   private short tickingFluidCount;
   private final PalettedContainer<BlockState> states;

   public ChunkSection(int p_i49943_1_) {
      this(p_i49943_1_, (short)0, (short)0, (short)0);
   }

   public ChunkSection(int pY, short pNonEmptyBlockCount, short pTickingBlockCount, short pTickingFluidCount) {
      this.bottomBlockY = pY;
      this.nonEmptyBlockCount = pNonEmptyBlockCount;
      this.tickingBlockCount = pTickingBlockCount;
      this.tickingFluidCount = pTickingFluidCount;
      this.states = new PalettedContainer<>(GLOBAL_BLOCKSTATE_PALETTE, Block.BLOCK_STATE_REGISTRY, NBTUtil::readBlockState, NBTUtil::writeBlockState, Blocks.AIR.defaultBlockState());
   }

   public BlockState getBlockState(int pX, int pY, int pZ) {
      return this.states.get(pX, pY, pZ);
   }

   public FluidState getFluidState(int pX, int pY, int pZ) {
      return this.states.get(pX, pY, pZ).getFluidState();
   }

   public void acquire() {
      this.states.acquire();
   }

   public void release() {
      this.states.release();
   }

   public BlockState setBlockState(int pX, int pY, int pZ, BlockState pBlockState) {
      return this.setBlockState(pX, pY, pZ, pBlockState, true);
   }

   public BlockState setBlockState(int pX, int pY, int pZ, BlockState pState, boolean pUseLocks) {
      BlockState blockstate;
      if (pUseLocks) {
         blockstate = this.states.getAndSet(pX, pY, pZ, pState);
      } else {
         blockstate = this.states.getAndSetUnchecked(pX, pY, pZ, pState);
      }

      FluidState fluidstate = blockstate.getFluidState();
      FluidState fluidstate1 = pState.getFluidState();
      if (!blockstate.isAir()) {
         --this.nonEmptyBlockCount;
         if (blockstate.isRandomlyTicking()) {
            --this.tickingBlockCount;
         }
      }

      if (!fluidstate.isEmpty()) {
         --this.tickingFluidCount;
      }

      if (!pState.isAir()) {
         ++this.nonEmptyBlockCount;
         if (pState.isRandomlyTicking()) {
            ++this.tickingBlockCount;
         }
      }

      if (!fluidstate1.isEmpty()) {
         ++this.tickingFluidCount;
      }

      return blockstate;
   }

   /**
    * Returns whether or not this block storage's Chunk is fully empty, based on its internal reference count.
    */
   public boolean isEmpty() {
      return this.nonEmptyBlockCount == 0;
   }

   public static boolean isEmpty(@Nullable ChunkSection pSection) {
      return pSection == Chunk.EMPTY_SECTION || pSection.isEmpty();
   }

   public boolean isRandomlyTicking() {
      return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
   }

   /**
    * Returns whether or not this block storage's Chunk will require random ticking, used to avoid looping through
    * random block ticks when there are no blocks that would randomly tick.
    */
   public boolean isRandomlyTickingBlocks() {
      return this.tickingBlockCount > 0;
   }

   public boolean isRandomlyTickingFluids() {
      return this.tickingFluidCount > 0;
   }

   /**
    * Gets the y coordinate that this chunk section starts at (which is a multiple of 16). To get the y number, use
    * <code>section.getYLocation() >> 4</code>. Note that there is a section below the world for lighting purposes.
    */
   public int bottomBlockY() {
      return this.bottomBlockY;
   }

   public void recalcBlockCounts() {
      this.nonEmptyBlockCount = 0;
      this.tickingBlockCount = 0;
      this.tickingFluidCount = 0;
      this.states.count((p_225496_1_, p_225496_2_) -> {
         FluidState fluidstate = p_225496_1_.getFluidState();
         if (!p_225496_1_.isAir()) {
            this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + p_225496_2_);
            if (p_225496_1_.isRandomlyTicking()) {
               this.tickingBlockCount = (short)(this.tickingBlockCount + p_225496_2_);
            }
         }

         if (!fluidstate.isEmpty()) {
            this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + p_225496_2_);
            if (fluidstate.isRandomlyTicking()) {
               this.tickingFluidCount = (short)(this.tickingFluidCount + p_225496_2_);
            }
         }

      });
   }

   public PalettedContainer<BlockState> getStates() {
      return this.states;
   }

   @OnlyIn(Dist.CLIENT)
   public void read(PacketBuffer pPacketBuffer) {
      this.nonEmptyBlockCount = pPacketBuffer.readShort();
      this.states.read(pPacketBuffer);
   }

   public void write(PacketBuffer pPacketBuffer) {
      pPacketBuffer.writeShort(this.nonEmptyBlockCount);
      this.states.write(pPacketBuffer);
   }

   public int getSerializedSize() {
      return 2 + this.states.getSerializedSize();
   }

   public boolean maybeHas(Predicate<BlockState> pPredicate) {
      return this.states.maybeHas(pPredicate);
   }
}