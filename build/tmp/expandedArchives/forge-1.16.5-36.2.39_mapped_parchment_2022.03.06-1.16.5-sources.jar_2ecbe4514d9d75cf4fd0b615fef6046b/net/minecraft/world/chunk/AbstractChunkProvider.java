package net.minecraft.world.chunk;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.lighting.WorldLightManager;

public abstract class AbstractChunkProvider implements IChunkLightProvider, AutoCloseable {
   @Nullable
   public Chunk getChunk(int pChunkX, int pChunkZ, boolean pLoad) {
      return (Chunk)this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, pLoad);
   }

   @Nullable
   public Chunk getChunkNow(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, false);
   }

   @Nullable
   public IBlockReader getChunkForLighting(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.EMPTY, false);
   }

   /**
    * Checks to see if a chunk exists at x, z
    */
   public boolean hasChunk(int pX, int pZ) {
      return this.getChunk(pX, pZ, ChunkStatus.FULL, false) != null;
   }

   @Nullable
   public abstract IChunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad);

   /**
    * Converts the instance data to a readable string.
    */
   public abstract String gatherStats();

   public void close() throws IOException {
   }

   public abstract WorldLightManager getLightEngine();

   public void setSpawnSettings(boolean pHostile, boolean pPeaceful) {
   }

   public void updateChunkForced(ChunkPos pPos, boolean pAdd) {
   }

   public boolean isEntityTickingChunk(Entity p_217204_1_) {
      return true;
   }

   public boolean isEntityTickingChunk(ChunkPos p_222865_1_) {
      return true;
   }

   public boolean isTickingChunk(BlockPos p_222866_1_) {
      return true;
   }
}