package net.minecraft.world.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkPrimer implements IChunk {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ChunkPos chunkPos;
   private volatile boolean isDirty;
   @Nullable
   private BiomeContainer biomes;
   @Nullable
   private volatile WorldLightManager lightEngine;
   private final Map<Heightmap.Type, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Type.class);
   private volatile ChunkStatus status = ChunkStatus.EMPTY;
   private final Map<BlockPos, TileEntity> blockEntities = Maps.newHashMap();
   private final Map<BlockPos, CompoundNBT> blockEntityNbts = Maps.newHashMap();
   private final ChunkSection[] sections = new ChunkSection[16];
   private final List<CompoundNBT> entities = Lists.newArrayList();
   private final List<BlockPos> lights = Lists.newArrayList();
   private final ShortList[] postProcessing = new ShortList[16];
   private final Map<Structure<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
   private final Map<Structure<?>, LongSet> structuresRefences = Maps.newHashMap();
   private final UpgradeData upgradeData;
   private final ChunkPrimerTickList<Block> blockTicks;
   private final ChunkPrimerTickList<Fluid> liquidTicks;
   private long inhabitedTime;
   private final Map<GenerationStage.Carving, BitSet> carvingMasks = new Object2ObjectArrayMap<>();
   private volatile boolean isLightCorrect;

   public ChunkPrimer(ChunkPos p_i48700_1_, UpgradeData p_i48700_2_) {
      this(p_i48700_1_, p_i48700_2_, (ChunkSection[])null, new ChunkPrimerTickList<>((p_205332_0_) -> {
         return p_205332_0_ == null || p_205332_0_.defaultBlockState().isAir();
      }, p_i48700_1_), new ChunkPrimerTickList<>((p_205766_0_) -> {
         return p_205766_0_ == null || p_205766_0_ == Fluids.EMPTY;
      }, p_i48700_1_));
   }

   public ChunkPrimer(ChunkPos p_i49941_1_, UpgradeData p_i49941_2_, @Nullable ChunkSection[] p_i49941_3_, ChunkPrimerTickList<Block> p_i49941_4_, ChunkPrimerTickList<Fluid> p_i49941_5_) {
      this.chunkPos = p_i49941_1_;
      this.upgradeData = p_i49941_2_;
      this.blockTicks = p_i49941_4_;
      this.liquidTicks = p_i49941_5_;
      if (p_i49941_3_ != null) {
         if (this.sections.length == p_i49941_3_.length) {
            System.arraycopy(p_i49941_3_, 0, this.sections, 0, this.sections.length);
         } else {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", p_i49941_3_.length, this.sections.length);
         }
      }

   }

   public BlockState getBlockState(BlockPos pPos) {
      int i = pPos.getY();
      if (World.isOutsideBuildHeight(i)) {
         return Blocks.VOID_AIR.defaultBlockState();
      } else {
         ChunkSection chunksection = this.getSections()[i >> 4];
         return ChunkSection.isEmpty(chunksection) ? Blocks.AIR.defaultBlockState() : chunksection.getBlockState(pPos.getX() & 15, i & 15, pPos.getZ() & 15);
      }
   }

   public FluidState getFluidState(BlockPos pPos) {
      int i = pPos.getY();
      if (World.isOutsideBuildHeight(i)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         ChunkSection chunksection = this.getSections()[i >> 4];
         return ChunkSection.isEmpty(chunksection) ? Fluids.EMPTY.defaultFluidState() : chunksection.getFluidState(pPos.getX() & 15, i & 15, pPos.getZ() & 15);
      }
   }

   public Stream<BlockPos> getLights() {
      return this.lights.stream();
   }

   public ShortList[] getPackedLights() {
      ShortList[] ashortlist = new ShortList[16];

      for(BlockPos blockpos : this.lights) {
         IChunk.getOrCreateOffsetList(ashortlist, blockpos.getY() >> 4).add(packOffsetCoordinates(blockpos));
      }

      return ashortlist;
   }

   public void addLight(short pPackedPosition, int pLightValue) {
      this.addLight(unpackOffsetCoordinates(pPackedPosition, pLightValue, this.chunkPos));
   }

   public void addLight(BlockPos pLightPos) {
      this.lights.add(pLightPos.immutable());
   }

   @Nullable
   public BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();
      if (j >= 0 && j < 256) {
         if (this.sections[j >> 4] == Chunk.EMPTY_SECTION && pState.is(Blocks.AIR)) {
            return pState;
         } else {
            if (pState.getLightValue(this, pPos) > 0) {
               this.lights.add(new BlockPos((i & 15) + this.getPos().getMinBlockX(), j, (k & 15) + this.getPos().getMinBlockZ()));
            }

            ChunkSection chunksection = this.getOrCreateSection(j >> 4);
            BlockState blockstate = chunksection.setBlockState(i & 15, j & 15, k & 15, pState);
            if (this.status.isOrAfter(ChunkStatus.FEATURES) && pState != blockstate && (pState.getLightBlock(this, pPos) != blockstate.getLightBlock(this, pPos) || pState.getLightValue(this, pPos) != blockstate.getLightValue(this, pPos) || pState.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion())) {
               WorldLightManager worldlightmanager = this.getLightEngine();
               worldlightmanager.checkBlock(pPos);
            }

            EnumSet<Heightmap.Type> enumset1 = this.getStatus().heightmapsAfter();
            EnumSet<Heightmap.Type> enumset = null;

            for(Heightmap.Type heightmap$type : enumset1) {
               Heightmap heightmap = this.heightmaps.get(heightmap$type);
               if (heightmap == null) {
                  if (enumset == null) {
                     enumset = EnumSet.noneOf(Heightmap.Type.class);
                  }

                  enumset.add(heightmap$type);
               }
            }

            if (enumset != null) {
               Heightmap.primeHeightmaps(this, enumset);
            }

            for(Heightmap.Type heightmap$type1 : enumset1) {
               this.heightmaps.get(heightmap$type1).update(i & 15, j, k & 15, pState);
            }

            return blockstate;
         }
      } else {
         return Blocks.VOID_AIR.defaultBlockState();
      }
   }

   public ChunkSection getOrCreateSection(int p_217332_1_) {
      if (this.sections[p_217332_1_] == Chunk.EMPTY_SECTION) {
         this.sections[p_217332_1_] = new ChunkSection(p_217332_1_ << 4);
      }

      return this.sections[p_217332_1_];
   }

   public void setBlockEntity(BlockPos p_177426_1_, TileEntity p_177426_2_) {
      p_177426_2_.setPosition(p_177426_1_);
      this.blockEntities.put(p_177426_1_, p_177426_2_);
   }

   public Set<BlockPos> getBlockEntitiesPos() {
      Set<BlockPos> set = Sets.newHashSet(this.blockEntityNbts.keySet());
      set.addAll(this.blockEntities.keySet());
      return set;
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos) {
      return this.blockEntities.get(pPos);
   }

   public Map<BlockPos, TileEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public void addEntity(CompoundNBT pEntityCompound) {
      this.entities.add(pEntityCompound);
   }

   /**
    * Adds an entity to the chunk.
    */
   public void addEntity(Entity pEntity) {
      if (!pEntity.isPassenger()) {
         CompoundNBT compoundnbt = new CompoundNBT();
         pEntity.save(compoundnbt);
         this.addEntity(compoundnbt);
      }
   }

   public List<CompoundNBT> getEntities() {
      return this.entities;
   }

   public void setBiomes(BiomeContainer pBiomes) {
      this.biomes = pBiomes;
   }

   @Nullable
   public BiomeContainer getBiomes() {
      return this.biomes;
   }

   public void setUnsaved(boolean pModified) {
      this.isDirty = pModified;
   }

   public boolean isUnsaved() {
      return this.isDirty;
   }

   public ChunkStatus getStatus() {
      return this.status;
   }

   public void setStatus(ChunkStatus pStatus) {
      this.status = pStatus;
      this.setUnsaved(true);
   }

   public ChunkSection[] getSections() {
      return this.sections;
   }

   @Nullable
   public WorldLightManager getLightEngine() {
      return this.lightEngine;
   }

   public Collection<Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
      return Collections.unmodifiableSet(this.heightmaps.entrySet());
   }

   public void setHeightmap(Heightmap.Type pType, long[] pData) {
      this.getOrCreateHeightmapUnprimed(pType).setRawData(pData);
   }

   public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Type pType) {
      return this.heightmaps.computeIfAbsent(pType, (p_217333_1_) -> {
         return new Heightmap(this, p_217333_1_);
      });
   }

   public int getHeight(Heightmap.Type pHeightmapType, int pX, int pZ) {
      Heightmap heightmap = this.heightmaps.get(pHeightmapType);
      if (heightmap == null) {
         Heightmap.primeHeightmaps(this, EnumSet.of(pHeightmapType));
         heightmap = this.heightmaps.get(pHeightmapType);
      }

      return heightmap.getFirstAvailable(pX & 15, pZ & 15) - 1;
   }

   /**
    * Gets a {@link ChunkPos} representing the x and z coordinates of this chunk.
    */
   public ChunkPos getPos() {
      return this.chunkPos;
   }

   public void setLastSaveTime(long p_177432_1_) {
   }

   @Nullable
   public StructureStart<?> getStartForFeature(Structure<?> pStructure) {
      return this.structureStarts.get(pStructure);
   }

   public void setStartForFeature(Structure<?> pStructure, StructureStart<?> pStart) {
      this.structureStarts.put(pStructure, pStart);
      this.isDirty = true;
   }

   public Map<Structure<?>, StructureStart<?>> getAllStarts() {
      return Collections.unmodifiableMap(this.structureStarts);
   }

   public void setAllStarts(Map<Structure<?>, StructureStart<?>> pStructureStarts) {
      this.structureStarts.clear();
      this.structureStarts.putAll(pStructureStarts);
      this.isDirty = true;
   }

   public LongSet getReferencesForFeature(Structure<?> pStructure) {
      return this.structuresRefences.computeIfAbsent(pStructure, (p_235966_0_) -> {
         return new LongOpenHashSet();
      });
   }

   public void addReferenceForFeature(Structure<?> pStructure, long pChunkValue) {
      this.structuresRefences.computeIfAbsent(pStructure, (p_235965_0_) -> {
         return new LongOpenHashSet();
      }).add(pChunkValue);
      this.isDirty = true;
   }

   public Map<Structure<?>, LongSet> getAllReferences() {
      return Collections.unmodifiableMap(this.structuresRefences);
   }

   public void setAllReferences(Map<Structure<?>, LongSet> pStructureReferences) {
      this.structuresRefences.clear();
      this.structuresRefences.putAll(pStructureReferences);
      this.isDirty = true;
   }

   public static short packOffsetCoordinates(BlockPos pPos) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();
      int l = i & 15;
      int i1 = j & 15;
      int j1 = k & 15;
      return (short)(l | i1 << 4 | j1 << 8);
   }

   public static BlockPos unpackOffsetCoordinates(short pPackedPos, int pYOffset, ChunkPos pChunkPos) {
      int i = (pPackedPos & 15) + (pChunkPos.x << 4);
      int j = (pPackedPos >>> 4 & 15) + (pYOffset << 4);
      int k = (pPackedPos >>> 8 & 15) + (pChunkPos.z << 4);
      return new BlockPos(i, j, k);
   }

   public void markPosForPostprocessing(BlockPos pPos) {
      if (!World.isOutsideBuildHeight(pPos)) {
         IChunk.getOrCreateOffsetList(this.postProcessing, pPos.getY() >> 4).add(packOffsetCoordinates(pPos));
      }

   }

   public ShortList[] getPostProcessing() {
      return this.postProcessing;
   }

   public void addPackedPostProcess(short pPackedPosition, int pIndex) {
      IChunk.getOrCreateOffsetList(this.postProcessing, pIndex).add(pPackedPosition);
   }

   public ChunkPrimerTickList<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public ChunkPrimerTickList<Fluid> getLiquidTicks() {
      return this.liquidTicks;
   }

   public UpgradeData getUpgradeData() {
      return this.upgradeData;
   }

   public void setInhabitedTime(long pNewInhabitedTime) {
      this.inhabitedTime = pNewInhabitedTime;
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void setBlockEntityNbt(CompoundNBT pNbt) {
      this.blockEntityNbts.put(new BlockPos(pNbt.getInt("x"), pNbt.getInt("y"), pNbt.getInt("z")), pNbt);
   }

   public Map<BlockPos, CompoundNBT> getBlockEntityNbts() {
      return Collections.unmodifiableMap(this.blockEntityNbts);
   }

   public CompoundNBT getBlockEntityNbt(BlockPos pPos) {
      return this.blockEntityNbts.get(pPos);
   }

   @Nullable
   public CompoundNBT getBlockEntityNbtForSaving(BlockPos pPos) {
      TileEntity tileentity = this.getBlockEntity(pPos);
      return tileentity != null ? tileentity.save(new CompoundNBT()) : this.blockEntityNbts.get(pPos);
   }

   public void removeBlockEntity(BlockPos pPos) {
      this.blockEntities.remove(pPos);
      this.blockEntityNbts.remove(pPos);
   }

   @Nullable
   public BitSet getCarvingMask(GenerationStage.Carving pType) {
      return this.carvingMasks.get(pType);
   }

   public BitSet getOrCreateCarvingMask(GenerationStage.Carving pType) {
      return this.carvingMasks.computeIfAbsent(pType, (p_235964_0_) -> {
         return new BitSet(65536);
      });
   }

   public void setCarvingMask(GenerationStage.Carving pType, BitSet pMask) {
      this.carvingMasks.put(pType, pMask);
   }

   public void setLightEngine(WorldLightManager pLightManager) {
      this.lightEngine = pLightManager;
   }

   public boolean isLightCorrect() {
      return this.isLightCorrect;
   }

   public void setLightCorrect(boolean pLightCorrect) {
      this.isLightCorrect = pLightCorrect;
      this.setUnsaved(true);
   }
}
