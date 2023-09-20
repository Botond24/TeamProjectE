package net.minecraft.world.gen.feature.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

public class StructureIndexesSavedData extends WorldSavedData {
   private LongSet all = new LongOpenHashSet();
   private LongSet remaining = new LongOpenHashSet();

   public StructureIndexesSavedData(String p_i48654_1_) {
      super(p_i48654_1_);
   }

   public void load(CompoundNBT p_76184_1_) {
      this.all = new LongOpenHashSet(p_76184_1_.getLongArray("All"));
      this.remaining = new LongOpenHashSet(p_76184_1_.getLongArray("Remaining"));
   }

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompound the {@code CompoundTag} to save the {@code SavedData} to
    */
   public CompoundNBT save(CompoundNBT pCompound) {
      pCompound.putLongArray("All", this.all.toLongArray());
      pCompound.putLongArray("Remaining", this.remaining.toLongArray());
      return pCompound;
   }

   public void addIndex(long pChunkPos) {
      this.all.add(pChunkPos);
      this.remaining.add(pChunkPos);
   }

   public boolean hasStartIndex(long pChunkPos) {
      return this.all.contains(pChunkPos);
   }

   public boolean hasUnhandledIndex(long pChunkPos) {
      return this.remaining.contains(pChunkPos);
   }

   public void removeIndex(long pChunkPos) {
      this.remaining.remove(pChunkPos);
   }

   public LongSet getAll() {
      return this.all;
   }
}