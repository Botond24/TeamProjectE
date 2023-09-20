package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;

public class ChunkPrimerWrapper extends ChunkPrimer {
   private final Chunk wrapped;

   public ChunkPrimerWrapper(Chunk pWrapped) {
      super(pWrapped.getPos(), UpgradeData.EMPTY);
      this.wrapped = pWrapped;
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos) {
      return this.wrapped.getBlockEntity(pPos);
   }

   @Nullable
   public BlockState getBlockState(BlockPos pPos) {
      return this.wrapped.getBlockState(pPos);
   }

   public FluidState getFluidState(BlockPos pPos) {
      return this.wrapped.getFluidState(pPos);
   }

   public int getMaxLightLevel() {
      return this.wrapped.getMaxLightLevel();
   }

   @Nullable
   public BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving) {
      return null;
   }

   public void setBlockEntity(BlockPos p_177426_1_, TileEntity p_177426_2_) {
   }

   /**
    * Adds an entity to the chunk.
    */
   public void addEntity(Entity pEntity) {
   }

   public void setStatus(ChunkStatus pStatus) {
   }

   public ChunkSection[] getSections() {
      return this.wrapped.getSections();
   }

   @Nullable
   public WorldLightManager getLightEngine() {
      return this.wrapped.getLightEngine();
   }

   public void setHeightmap(Heightmap.Type pType, long[] pData) {
   }

   private Heightmap.Type fixType(Heightmap.Type pTypes) {
      if (pTypes == Heightmap.Type.WORLD_SURFACE_WG) {
         return Heightmap.Type.WORLD_SURFACE;
      } else {
         return pTypes == Heightmap.Type.OCEAN_FLOOR_WG ? Heightmap.Type.OCEAN_FLOOR : pTypes;
      }
   }

   public int getHeight(Heightmap.Type pHeightmapType, int pX, int pZ) {
      return this.wrapped.getHeight(this.fixType(pHeightmapType), pX, pZ);
   }

   /**
    * Gets a {@link ChunkPos} representing the x and z coordinates of this chunk.
    */
   public ChunkPos getPos() {
      return this.wrapped.getPos();
   }

   public void setLastSaveTime(long p_177432_1_) {
   }

   @Nullable
   public StructureStart<?> getStartForFeature(Structure<?> pStructure) {
      return this.wrapped.getStartForFeature(pStructure);
   }

   public void setStartForFeature(Structure<?> pStructure, StructureStart<?> pStart) {
   }

   public Map<Structure<?>, StructureStart<?>> getAllStarts() {
      return this.wrapped.getAllStarts();
   }

   public void setAllStarts(Map<Structure<?>, StructureStart<?>> pStructureStarts) {
   }

   public LongSet getReferencesForFeature(Structure<?> pStructure) {
      return this.wrapped.getReferencesForFeature(pStructure);
   }

   public void addReferenceForFeature(Structure<?> pStructure, long pChunkValue) {
   }

   public Map<Structure<?>, LongSet> getAllReferences() {
      return this.wrapped.getAllReferences();
   }

   public void setAllReferences(Map<Structure<?>, LongSet> pStructureReferences) {
   }

   public BiomeContainer getBiomes() {
      return this.wrapped.getBiomes();
   }

   public void setUnsaved(boolean pModified) {
   }

   public boolean isUnsaved() {
      return false;
   }

   public ChunkStatus getStatus() {
      return this.wrapped.getStatus();
   }

   public void removeBlockEntity(BlockPos pPos) {
   }

   public void markPosForPostprocessing(BlockPos pPos) {
   }

   public void setBlockEntityNbt(CompoundNBT pNbt) {
   }

   @Nullable
   public CompoundNBT getBlockEntityNbt(BlockPos pPos) {
      return this.wrapped.getBlockEntityNbt(pPos);
   }

   @Nullable
   public CompoundNBT getBlockEntityNbtForSaving(BlockPos pPos) {
      return this.wrapped.getBlockEntityNbtForSaving(pPos);
   }

   public void setBiomes(BiomeContainer pBiomes) {
   }

   public Stream<BlockPos> getLights() {
      return this.wrapped.getLights();
   }

   public ChunkPrimerTickList<Block> getBlockTicks() {
      return new ChunkPrimerTickList<>((p_209219_0_) -> {
         return p_209219_0_.defaultBlockState().isAir();
      }, this.getPos());
   }

   public ChunkPrimerTickList<Fluid> getLiquidTicks() {
      return new ChunkPrimerTickList<>((p_209218_0_) -> {
         return p_209218_0_ == Fluids.EMPTY;
      }, this.getPos());
   }

   public BitSet getCarvingMask(GenerationStage.Carving pType) {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
   }

   public BitSet getOrCreateCarvingMask(GenerationStage.Carving pType) {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
   }

   public Chunk getWrapped() {
      return this.wrapped;
   }

   public boolean isLightCorrect() {
      return this.wrapped.isLightCorrect();
   }

   public void setLightCorrect(boolean pLightCorrect) {
      this.wrapped.setLightCorrect(pLightCorrect);
   }
}