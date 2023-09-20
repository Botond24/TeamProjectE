package net.minecraft.world.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.math.ChunkPos;

/**
 * Handles reading and writing the {@link net.minecraft.world.level.chunk.storage.RegionFile region files} for a {@link
 * net.minecraft.world.level.Level}.
 */
public final class RegionFileCache implements AutoCloseable {
   private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
   private final File folder;
   private final boolean sync;

   RegionFileCache(File pFolder, boolean pSync) {
      this.folder = pFolder;
      this.sync = pSync;
   }

   private RegionFile getRegionFile(ChunkPos pChunkPos) throws IOException {
      long i = ChunkPos.asLong(pChunkPos.getRegionX(), pChunkPos.getRegionZ());
      RegionFile regionfile = this.regionCache.getAndMoveToFirst(i);
      if (regionfile != null) {
         return regionfile;
      } else {
         if (this.regionCache.size() >= 256) {
            this.regionCache.removeLast().close();
         }

         if (!this.folder.exists()) {
            this.folder.mkdirs();
         }

         File file1 = new File(this.folder, "r." + pChunkPos.getRegionX() + "." + pChunkPos.getRegionZ() + ".mca");
         RegionFile regionfile1 = new RegionFile(file1, this.folder, this.sync);
         this.regionCache.putAndMoveToFirst(i, regionfile1);
         return regionfile1;
      }
   }

   @Nullable
   public CompoundNBT read(ChunkPos pChunkPos) throws IOException {
      RegionFile regionfile = this.getRegionFile(pChunkPos);

      Object object;
      try (DataInputStream datainputstream = regionfile.getChunkDataInputStream(pChunkPos)) {
         if (datainputstream != null) {
            return CompressedStreamTools.read(datainputstream);
         }

         object = null;
      }

      return (CompoundNBT)object;
   }

   protected void write(ChunkPos pChunkPos, CompoundNBT pChunkData) throws IOException {
      RegionFile regionfile = this.getRegionFile(pChunkPos);

      try (DataOutputStream dataoutputstream = regionfile.getChunkDataOutputStream(pChunkPos)) {
         CompressedStreamTools.write(pChunkData, dataoutputstream);
      }

   }

   public void close() throws IOException {
      SuppressedExceptions<IOException> suppressedexceptions = new SuppressedExceptions<>();

      for(RegionFile regionfile : this.regionCache.values()) {
         try {
            regionfile.close();
         } catch (IOException ioexception) {
            suppressedexceptions.add(ioexception);
         }
      }

      suppressedexceptions.throwIfPresent();
   }

   public void flush() throws IOException {
      for(RegionFile regionfile : this.regionCache.values()) {
         regionfile.flush();
      }

   }
}