package net.minecraft.world.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.LegacyStructureDataUtil;
import net.minecraft.world.storage.DimensionSavedDataManager;

public class ChunkLoader implements AutoCloseable {
   private final IOWorker worker;
   protected final DataFixer fixerUpper;
   @Nullable
   private LegacyStructureDataUtil legacyStructureHandler;

   public ChunkLoader(File pRegionFolder, DataFixer pFixerUpper, boolean pSync) {
      this.fixerUpper = pFixerUpper;
      this.worker = new IOWorker(pRegionFolder, pSync, "chunk");
   }

   public CompoundNBT upgradeChunkTag(RegistryKey<World> pLevelKey, Supplier<DimensionSavedDataManager> pStorage, CompoundNBT pChunkData) {
      int i = getVersion(pChunkData);
      int j = 1493;
      if (i < 1493) {
         pChunkData = NBTUtil.update(this.fixerUpper, DefaultTypeReferences.CHUNK, pChunkData, i, 1493);
         if (pChunkData.getCompound("Level").getBoolean("hasLegacyStructureData")) {
            if (this.legacyStructureHandler == null) {
               this.legacyStructureHandler = LegacyStructureDataUtil.getLegacyStructureHandler(pLevelKey, pStorage.get());
            }

            pChunkData = this.legacyStructureHandler.updateFromLegacy(pChunkData);
         }
      }

      pChunkData = NBTUtil.update(this.fixerUpper, DefaultTypeReferences.CHUNK, pChunkData, Math.max(1493, i));
      if (i < SharedConstants.getCurrentVersion().getWorldVersion()) {
         pChunkData.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
      }

      return pChunkData;
   }

   public static int getVersion(CompoundNBT pChunkData) {
      return pChunkData.contains("DataVersion", 99) ? pChunkData.getInt("DataVersion") : -1;
   }

   @Nullable
   public CompoundNBT read(ChunkPos pChunkPos) throws IOException {
      return this.worker.load(pChunkPos);
   }

   public void write(ChunkPos pChunkPos, CompoundNBT pChunkData) {
      this.worker.store(pChunkPos, pChunkData);
      if (this.legacyStructureHandler != null) {
         this.legacyStructureHandler.removeIndex(pChunkPos.toLong());
      }

   }

   public void flushWorker() {
      this.worker.synchronize().join();
   }

   public void close() throws IOException {
      this.worker.close();
   }
}