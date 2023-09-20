package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IStructureReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import org.apache.logging.log4j.LogManager;

public interface IChunk extends IBlockReader, IStructureReader {
   @Nullable
   BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving);

   void setBlockEntity(BlockPos p_177426_1_, TileEntity p_177426_2_);

   /**
    * Adds an entity to the chunk.
    */
   void addEntity(Entity pEntity);

   @Nullable
   default ChunkSection getHighestSection() {
      ChunkSection[] achunksection = this.getSections();

      for(int i = achunksection.length - 1; i >= 0; --i) {
         ChunkSection chunksection = achunksection[i];
         if (!ChunkSection.isEmpty(chunksection)) {
            return chunksection;
         }
      }

      return null;
   }

   /**
    * Returns the topmost ExtendedBlockStorage instance for this Chunk that actually contains a block.
    */
   default int getHighestSectionPosition() {
      ChunkSection chunksection = this.getHighestSection();
      return chunksection == null ? 0 : chunksection.bottomBlockY();
   }

   Set<BlockPos> getBlockEntitiesPos();

   ChunkSection[] getSections();

   Collection<Entry<Heightmap.Type, Heightmap>> getHeightmaps();

   void setHeightmap(Heightmap.Type pType, long[] pData);

   Heightmap getOrCreateHeightmapUnprimed(Heightmap.Type pType);

   int getHeight(Heightmap.Type pHeightmapType, int pX, int pZ);

   /**
    * Gets a {@link ChunkPos} representing the x and z coordinates of this chunk.
    */
   ChunkPos getPos();

   void setLastSaveTime(long p_177432_1_);

   Map<Structure<?>, StructureStart<?>> getAllStarts();

   void setAllStarts(Map<Structure<?>, StructureStart<?>> pStructureStarts);

   /**
    * Returns whether the ExtendedBlockStorages containing levels (in blocks) from arg 1 to arg 2 are fully empty (true)
    * or not (false).
    */
   default boolean isYSpaceEmpty(int pStartY, int pEndY) {
      if (pStartY < 0) {
         pStartY = 0;
      }

      if (pEndY >= 256) {
         pEndY = 255;
      }

      for(int i = pStartY; i <= pEndY; i += 16) {
         if (!ChunkSection.isEmpty(this.getSections()[i >> 4])) {
            return false;
         }
      }

      return true;
   }

   @Nullable
   BiomeContainer getBiomes();

   void setUnsaved(boolean pModified);

   boolean isUnsaved();

   ChunkStatus getStatus();

   void removeBlockEntity(BlockPos pPos);

   default void markPosForPostprocessing(BlockPos pPos) {
      LogManager.getLogger().warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", (Object)pPos);
   }

   ShortList[] getPostProcessing();

   default void addPackedPostProcess(short pPackedPosition, int pIndex) {
      getOrCreateOffsetList(this.getPostProcessing(), pIndex).add(pPackedPosition);
   }

   default void setBlockEntityNbt(CompoundNBT pNbt) {
      LogManager.getLogger().warn("Trying to set a BlockEntity, but this operation is not supported.");
   }

   @Nullable
   CompoundNBT getBlockEntityNbt(BlockPos pPos);

   @Nullable
   CompoundNBT getBlockEntityNbtForSaving(BlockPos pPos);

   Stream<BlockPos> getLights();

   ITickList<Block> getBlockTicks();

   ITickList<Fluid> getLiquidTicks();

   UpgradeData getUpgradeData();

   void setInhabitedTime(long pNewInhabitedTime);

   long getInhabitedTime();

   static ShortList getOrCreateOffsetList(ShortList[] pPackedPositions, int pIndex) {
      if (pPackedPositions[pIndex] == null) {
         pPackedPositions[pIndex] = new ShortArrayList();
      }

      return pPackedPositions[pIndex];
   }

   boolean isLightCorrect();

   void setLightCorrect(boolean pLightCorrect);

   @Nullable
   default net.minecraft.world.IWorld getWorldForge() {
      return null;
   }
}
