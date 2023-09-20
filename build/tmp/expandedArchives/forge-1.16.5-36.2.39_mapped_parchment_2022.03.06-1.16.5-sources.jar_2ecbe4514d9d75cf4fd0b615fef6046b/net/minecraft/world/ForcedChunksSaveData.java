package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

public class ForcedChunksSaveData extends WorldSavedData {
   private LongSet chunks = new LongOpenHashSet();

   public ForcedChunksSaveData() {
      super("chunks");
   }

   public void load(CompoundNBT p_76184_1_) {
      this.chunks = new LongOpenHashSet(p_76184_1_.getLongArray("Forced"));
      this.blockForcedChunks = new net.minecraftforge.common.world.ForgeChunkManager.TicketTracker<>();
      this.entityForcedChunks = new net.minecraftforge.common.world.ForgeChunkManager.TicketTracker<>();
      net.minecraftforge.common.world.ForgeChunkManager.readForgeForcedChunks(p_76184_1_, this.blockForcedChunks, this.entityForcedChunks);
   }

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompound the {@code CompoundTag} to save the {@code SavedData} to
    */
   public CompoundNBT save(CompoundNBT pCompound) {
      pCompound.putLongArray("Forced", this.chunks.toLongArray());
      net.minecraftforge.common.world.ForgeChunkManager.writeForgeForcedChunks(pCompound, this.blockForcedChunks, this.entityForcedChunks);
      return pCompound;
   }

   public LongSet getChunks() {
      return this.chunks;
   }

   /* ======================================== FORGE START =====================================*/
   private net.minecraftforge.common.world.ForgeChunkManager.TicketTracker<net.minecraft.util.math.BlockPos> blockForcedChunks = new net.minecraftforge.common.world.ForgeChunkManager.TicketTracker<>();
   private net.minecraftforge.common.world.ForgeChunkManager.TicketTracker<java.util.UUID> entityForcedChunks = new net.minecraftforge.common.world.ForgeChunkManager.TicketTracker<>();

   public net.minecraftforge.common.world.ForgeChunkManager.TicketTracker<net.minecraft.util.math.BlockPos> getBlockForcedChunks() {
      return this.blockForcedChunks;
   }

   public net.minecraftforge.common.world.ForgeChunkManager.TicketTracker<java.util.UUID> getEntityForcedChunks() {
      return this.entityForcedChunks;
   }
}
