package net.minecraft.world.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.SerializableTickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.gen.DebugChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Chunk extends net.minecraftforge.common.capabilities.CapabilityProvider<Chunk> implements IChunk, net.minecraftforge.common.extensions.IForgeChunk {
   private static final Logger LOGGER = LogManager.getLogger();
   @Nullable
   public static final ChunkSection EMPTY_SECTION = null;
   private final ChunkSection[] sections = new ChunkSection[16];
   private BiomeContainer biomes;
   private final Map<BlockPos, CompoundNBT> pendingBlockEntities = Maps.newHashMap();
   private boolean loaded;
   private final World level;
   private final Map<Heightmap.Type, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Type.class);
   private final UpgradeData upgradeData;
   /** A Map of ChunkPositions to TileEntities in this chunk */
   private final Map<BlockPos, TileEntity> blockEntities = Maps.newHashMap();
   private final ClassInheritanceMultiMap<Entity>[] entitySections;
   private final Map<Structure<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
   private final Map<Structure<?>, LongSet> structuresRefences = Maps.newHashMap();
   private final ShortList[] postProcessing = new ShortList[16];
   private ITickList<Block> blockTicks;
   private ITickList<Fluid> liquidTicks;
   private boolean lastSaveHadEntities;
   private long lastSaveTime;
   private volatile boolean unsaved;
   /** the cumulative number of ticks players have been in this chunk */
   private long inhabitedTime;
   @Nullable
   private Supplier<ChunkHolder.LocationType> fullStatus;
   @Nullable
   private Consumer<Chunk> postLoad;
   private final ChunkPos chunkPos;
   private volatile boolean isLightCorrect;

   public Chunk(World pLevel, ChunkPos pPos, BiomeContainer pBiomes) {
      this(pLevel, pPos, pBiomes, UpgradeData.EMPTY, EmptyTickList.empty(), EmptyTickList.empty(), 0L, (ChunkSection[])null, (Consumer<Chunk>)null);
   }

   public Chunk(World pLevel, ChunkPos pPos, BiomeContainer pBiomes, UpgradeData pData, ITickList<Block> pBlockTicks, ITickList<Fluid> pLiquidTicks, long pInhabitedTime, @Nullable ChunkSection[] pSections, @Nullable Consumer<Chunk> pPostLoad) {
      super(Chunk.class);
      this.entitySections = new ClassInheritanceMultiMap[16];
      this.level = pLevel;
      this.chunkPos = pPos;
      this.upgradeData = pData;

      for(Heightmap.Type heightmap$type : Heightmap.Type.values()) {
         if (ChunkStatus.FULL.heightmapsAfter().contains(heightmap$type)) {
            this.heightmaps.put(heightmap$type, new Heightmap(this, heightmap$type));
         }
      }

      for(int i = 0; i < this.entitySections.length; ++i) {
         this.entitySections[i] = new ClassInheritanceMultiMap<>(Entity.class);
      }

      this.biomes = pBiomes;
      this.blockTicks = pBlockTicks;
      this.liquidTicks = pLiquidTicks;
      this.inhabitedTime = pInhabitedTime;
      this.postLoad = pPostLoad;
      if (pSections != null) {
         if (this.sections.length == pSections.length) {
            System.arraycopy(pSections, 0, this.sections, 0, this.sections.length);
         } else {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", pSections.length, this.sections.length);
         }
      }
      this.gatherCapabilities();
   }

   public Chunk(World p_i49947_1_, ChunkPrimer p_i49947_2_) {
      this(p_i49947_1_, p_i49947_2_.getPos(), p_i49947_2_.getBiomes(), p_i49947_2_.getUpgradeData(), p_i49947_2_.getBlockTicks(), p_i49947_2_.getLiquidTicks(), p_i49947_2_.getInhabitedTime(), p_i49947_2_.getSections(), (Consumer<Chunk>)null);

      for(CompoundNBT compoundnbt : p_i49947_2_.getEntities()) {
         EntityType.loadEntityRecursive(compoundnbt, p_i49947_1_, (p_217325_1_) -> {
            this.addEntity(p_217325_1_);
            return p_217325_1_;
         });
      }

      for(TileEntity tileentity : p_i49947_2_.getBlockEntities().values()) {
         this.addBlockEntity(tileentity);
      }

      this.pendingBlockEntities.putAll(p_i49947_2_.getBlockEntityNbts());

      for(int i = 0; i < p_i49947_2_.getPostProcessing().length; ++i) {
         this.postProcessing[i] = p_i49947_2_.getPostProcessing()[i];
      }

      this.setAllStarts(p_i49947_2_.getAllStarts());
      this.setAllReferences(p_i49947_2_.getAllReferences());

      for(Entry<Heightmap.Type, Heightmap> entry : p_i49947_2_.getHeightmaps()) {
         if (ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) {
            this.getOrCreateHeightmapUnprimed(entry.getKey()).setRawData(entry.getValue().getRawData());
         }
      }

      this.setLightCorrect(p_i49947_2_.isLightCorrect());
      this.unsaved = true;
   }

   public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Type pType) {
      return this.heightmaps.computeIfAbsent(pType, (p_217319_1_) -> {
         return new Heightmap(this, p_217319_1_);
      });
   }

   public Set<BlockPos> getBlockEntitiesPos() {
      Set<BlockPos> set = Sets.newHashSet(this.pendingBlockEntities.keySet());
      set.addAll(this.blockEntities.keySet());
      return set;
   }

   public ChunkSection[] getSections() {
      return this.sections;
   }

   public BlockState getBlockState(BlockPos pPos) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();
      if (this.level.isDebug()) {
         BlockState blockstate = null;
         if (j == 60) {
            blockstate = Blocks.BARRIER.defaultBlockState();
         }

         if (j == 70) {
            blockstate = DebugChunkGenerator.getBlockStateFor(i, k);
         }

         return blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate;
      } else {
         try {
            if (j >= 0 && j >> 4 < this.sections.length) {
               ChunkSection chunksection = this.sections[j >> 4];
               if (!ChunkSection.isEmpty(chunksection)) {
                  return chunksection.getBlockState(i & 15, j & 15, k & 15);
               }
            }

            return Blocks.AIR.defaultBlockState();
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting block state");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
            crashreportcategory.setDetail("Location", () -> {
               return CrashReportCategory.formatLocation(i, j, k);
            });
            throw new ReportedException(crashreport);
         }
      }
   }

   public FluidState getFluidState(BlockPos pPos) {
      return this.getFluidState(pPos.getX(), pPos.getY(), pPos.getZ());
   }

   public FluidState getFluidState(int pBx, int pBy, int pBz) {
      try {
         if (pBy >= 0 && pBy >> 4 < this.sections.length) {
            ChunkSection chunksection = this.sections[pBy >> 4];
            if (!ChunkSection.isEmpty(chunksection)) {
               return chunksection.getFluidState(pBx & 15, pBy & 15, pBz & 15);
            }
         }

         return Fluids.EMPTY.defaultFluidState();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting fluid state");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
         crashreportcategory.setDetail("Location", () -> {
            return CrashReportCategory.formatLocation(pBx, pBy, pBz);
         });
         throw new ReportedException(crashreport);
      }
   }

   @Nullable
   public BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving) {
      int i = pPos.getX() & 15;
      int j = pPos.getY();
      int k = pPos.getZ() & 15;
      ChunkSection chunksection = this.sections[j >> 4];
      if (chunksection == EMPTY_SECTION) {
         if (pState.isAir()) {
            return null;
         }

         chunksection = new ChunkSection(j >> 4 << 4);
         this.sections[j >> 4] = chunksection;
      }

      boolean flag = chunksection.isEmpty();
      BlockState blockstate = chunksection.setBlockState(i, j & 15, k, pState);
      if (blockstate == pState) {
         return null;
      } else {
         Block block = pState.getBlock();
         Block block1 = blockstate.getBlock();
         this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING).update(i, j, k, pState);
         this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).update(i, j, k, pState);
         this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR).update(i, j, k, pState);
         this.heightmaps.get(Heightmap.Type.WORLD_SURFACE).update(i, j, k, pState);
         boolean flag1 = chunksection.isEmpty();
         if (flag != flag1) {
            this.level.getChunkSource().getLightEngine().updateSectionStatus(pPos, flag1);
         }

         if (!this.level.isClientSide) {
            blockstate.onRemove(this.level, pPos, pState, pIsMoving);
         } else if ((block1 != block || !pState.hasTileEntity()) && blockstate.hasTileEntity()) {
            this.level.removeBlockEntity(pPos);
         }

         if (!chunksection.getBlockState(i, j & 15, k).is(block)) {
            return null;
         } else {
            if (blockstate.hasTileEntity()) {
               TileEntity tileentity = this.getBlockEntity(pPos, Chunk.CreateEntityType.CHECK);
               if (tileentity != null) {
                  tileentity.clearCache();
               }
            }

            if (!this.level.isClientSide && !this.level.captureBlockSnapshots) {
               pState.onPlace(this.level, pPos, blockstate, pIsMoving);
            }

            if (pState.hasTileEntity()) {
               TileEntity tileentity1 = this.getBlockEntity(pPos, Chunk.CreateEntityType.CHECK);
               if (tileentity1 == null) {
                  tileentity1 = pState.createTileEntity(this.level);
                  this.level.setBlockEntity(pPos, tileentity1);
               } else {
                  tileentity1.clearCache();
               }
            }

            this.unsaved = true;
            return blockstate;
         }
      }
   }

   @Nullable
   public WorldLightManager getLightEngine() {
      return this.level.getChunkSource().getLightEngine();
   }

   /**
    * Adds an entity to the chunk.
    */
   public void addEntity(Entity pEntity) {
      this.lastSaveHadEntities = true;
      int i = MathHelper.floor(pEntity.getX() / 16.0D);
      int j = MathHelper.floor(pEntity.getZ() / 16.0D);
      if (i != this.chunkPos.x || j != this.chunkPos.z) {
         LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", i, j, this.chunkPos.x, this.chunkPos.z, pEntity);
         pEntity.removed = true;
      }

      int k = MathHelper.floor(pEntity.getY() / 16.0D);
      if (k < 0) {
         k = 0;
      }

      if (k >= this.entitySections.length) {
         k = this.entitySections.length - 1;
      }

      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityEvent.EnteringChunk(pEntity, this.chunkPos.x, this.chunkPos.z, pEntity.xChunk, pEntity.zChunk));
      pEntity.inChunk = true;
      pEntity.xChunk = this.chunkPos.x;
      pEntity.yChunk = k;
      pEntity.zChunk = this.chunkPos.z;
      this.entitySections[k].add(pEntity);
      this.markUnsaved(); // Forge - ensure chunks are marked to save after an entity add
   }

   public void setHeightmap(Heightmap.Type pType, long[] pData) {
      this.heightmaps.get(pType).setRawData(pData);
   }

   public void removeEntity(Entity p_76622_1_) {
      this.removeEntity(p_76622_1_, p_76622_1_.yChunk);
   }

   public void removeEntity(Entity p_76608_1_, int p_76608_2_) {
      if (p_76608_2_ < 0) {
         p_76608_2_ = 0;
      }

      if (p_76608_2_ >= this.entitySections.length) {
         p_76608_2_ = this.entitySections.length - 1;
      }

      this.entitySections[p_76608_2_].remove(p_76608_1_);
      this.markUnsaved(); // Forge - ensure chunks are marked to save after entity removals
   }

   public int getHeight(Heightmap.Type pHeightmapType, int pX, int pZ) {
      return this.heightmaps.get(pHeightmapType).getFirstAvailable(pX & 15, pZ & 15) - 1;
   }

   @Nullable
   private TileEntity createBlockEntity(BlockPos pPos) {
      BlockState blockstate = this.getBlockState(pPos);
      Block block = blockstate.getBlock();
      return !blockstate.hasTileEntity() ? null : blockstate.createTileEntity(this.level);
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos) {
      return this.getBlockEntity(pPos, Chunk.CreateEntityType.CHECK);
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos, Chunk.CreateEntityType pCreationMode) {
      TileEntity tileentity = this.blockEntities.get(pPos);
      if (tileentity != null && tileentity.isRemoved()) {
         blockEntities.remove(pPos);
         tileentity = null;
      }
      if (tileentity == null) {
         CompoundNBT compoundnbt = this.pendingBlockEntities.remove(pPos);
         if (compoundnbt != null) {
            TileEntity tileentity1 = this.promotePendingBlockEntity(pPos, compoundnbt);
            if (tileentity1 != null) {
               return tileentity1;
            }
         }
      }

      if (tileentity == null) {
         if (pCreationMode == Chunk.CreateEntityType.IMMEDIATE) {
            tileentity = this.createBlockEntity(pPos);
            this.level.setBlockEntity(pPos, tileentity);
         }
      }

      return tileentity;
   }

   public void addBlockEntity(TileEntity p_150813_1_) {
      this.setBlockEntity(p_150813_1_.getBlockPos(), p_150813_1_);
      if (this.loaded || this.level.isClientSide()) {
         this.level.setBlockEntity(p_150813_1_.getBlockPos(), p_150813_1_);
      }

   }

   public void setBlockEntity(BlockPos p_177426_1_, TileEntity p_177426_2_) {
      if (this.getBlockState(p_177426_1_).hasTileEntity()) {
         p_177426_2_.setLevelAndPosition(this.level, p_177426_1_);
         p_177426_2_.clearRemoved();
         TileEntity tileentity = this.blockEntities.put(p_177426_1_.immutable(), p_177426_2_);
         if (tileentity != null && tileentity != p_177426_2_) {
            tileentity.setRemoved();
         }

      }
   }

   public void setBlockEntityNbt(CompoundNBT pNbt) {
      this.pendingBlockEntities.put(new BlockPos(pNbt.getInt("x"), pNbt.getInt("y"), pNbt.getInt("z")), pNbt);
   }

   @Nullable
   public CompoundNBT getBlockEntityNbtForSaving(BlockPos pPos) {
      TileEntity tileentity = this.getBlockEntity(pPos);
      if (tileentity != null && !tileentity.isRemoved()) {
         try {
         CompoundNBT compoundnbt1 = tileentity.save(new CompoundNBT());
         compoundnbt1.putBoolean("keepPacked", false);
         return compoundnbt1;
         } catch (Exception e) {
            LogManager.getLogger().error("A TileEntity type {} has thrown an exception trying to write state. It will not persist, Report this to the mod author", tileentity.getClass().getName(), e);
            return null;
         }
      } else {
         CompoundNBT compoundnbt = this.pendingBlockEntities.get(pPos);
         if (compoundnbt != null) {
            compoundnbt = compoundnbt.copy();
            compoundnbt.putBoolean("keepPacked", true);
         }

         return compoundnbt;
      }
   }

   public void removeBlockEntity(BlockPos pPos) {
      if (this.loaded || this.level.isClientSide()) {
         TileEntity tileentity = this.blockEntities.remove(pPos);
         if (tileentity != null) {
            tileentity.setRemoved();
         }
      }

   }

   public void runPostLoad() {
      if (this.postLoad != null) {
         this.postLoad.accept(this);
         this.postLoad = null;
      }

   }

   /**
    * Sets the isModified flag for this Chunk
    */
   public void markUnsaved() {
      this.unsaved = true;
   }

   public void getEntities(@Nullable Entity p_177414_1_, AxisAlignedBB p_177414_2_, List<Entity> p_177414_3_, @Nullable Predicate<? super Entity> p_177414_4_) {
      int i = MathHelper.floor((p_177414_2_.minY - this.level.getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.floor((p_177414_2_.maxY + this.level.getMaxEntityRadius()) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entitySections.length - 1);
      j = MathHelper.clamp(j, 0, this.entitySections.length - 1);

      for(int k = i; k <= j; ++k) {
         ClassInheritanceMultiMap<Entity> classinheritancemultimap = this.entitySections[k];
         List<Entity> list = classinheritancemultimap.getAllInstances();
         int l = list.size();

         for(int i1 = 0; i1 < l; ++i1) {
            Entity entity = list.get(i1);
            if (entity.getBoundingBox().intersects(p_177414_2_) && entity != p_177414_1_) {
               if (p_177414_4_ == null || p_177414_4_.test(entity)) {
                  p_177414_3_.add(entity);
               }

               if (false) // // Forge: Fix MC-158205: Moved into World.getEntities()
               if (entity instanceof EnderDragonEntity) {
                  for(EnderDragonPartEntity enderdragonpartentity : ((EnderDragonEntity)entity).getSubEntities()) {
                     if (enderdragonpartentity != p_177414_1_ && enderdragonpartentity.getBoundingBox().intersects(p_177414_2_) && (p_177414_4_ == null || p_177414_4_.test(enderdragonpartentity))) {
                        p_177414_3_.add(enderdragonpartentity);
                     }
                  }
               }
            }
         }
      }

   }

   public <T extends Entity> void getEntities(@Nullable EntityType<?> p_217313_1_, AxisAlignedBB p_217313_2_, List<? super T> p_217313_3_, Predicate<? super T> p_217313_4_) {
      int i = MathHelper.floor((p_217313_2_.minY - this.level.getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.floor((p_217313_2_.maxY + this.level.getMaxEntityRadius()) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entitySections.length - 1);
      j = MathHelper.clamp(j, 0, this.entitySections.length - 1);

      for(int k = i; k <= j; ++k) {
         for(Entity entity : this.entitySections[k].find(Entity.class)) {
            if ((p_217313_1_ == null || entity.getType() == p_217313_1_) && entity.getBoundingBox().intersects(p_217313_2_) && p_217313_4_.test((T)entity)) {
               p_217313_3_.add((T)entity);
            }
         }
      }

   }

   public <T extends Entity> void getEntitiesOfClass(Class<? extends T> p_177430_1_, AxisAlignedBB p_177430_2_, List<T> p_177430_3_, @Nullable Predicate<? super T> p_177430_4_) {
      int i = MathHelper.floor((p_177430_2_.minY - this.level.getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.floor((p_177430_2_.maxY + this.level.getMaxEntityRadius()) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entitySections.length - 1);
      j = MathHelper.clamp(j, 0, this.entitySections.length - 1);

      for(int k = i; k <= j; ++k) {
         for(T t : this.entitySections[k].find(p_177430_1_)) {
            if (t.getBoundingBox().intersects(p_177430_2_) && (p_177430_4_ == null || p_177430_4_.test(t))) {
               p_177430_3_.add(t);
            }
         }
      }

   }

   public boolean isEmpty() {
      return false;
   }

   /**
    * Gets a {@link ChunkPos} representing the x and z coordinates of this chunk.
    */
   public ChunkPos getPos() {
      return this.chunkPos;
   }

   @OnlyIn(Dist.CLIENT)
   public void replaceWithPacketData(@Nullable BiomeContainer p_227073_1_, PacketBuffer p_227073_2_, CompoundNBT p_227073_3_, int p_227073_4_) {
      boolean flag = p_227073_1_ != null;
      Predicate<BlockPos> predicate = flag ? (p_217315_0_) -> {
         return true;
      } : (p_217323_1_) -> {
         return (p_227073_4_ & 1 << (p_217323_1_.getY() >> 4)) != 0;
      };
      Sets.newHashSet(this.blockEntities.keySet()).stream().filter(predicate).forEach(this.level::removeBlockEntity);

      for (TileEntity tileEntity : blockEntities.values()) {
         tileEntity.clearCache();
         tileEntity.getBlockState();
      }

      for(int i = 0; i < this.sections.length; ++i) {
         ChunkSection chunksection = this.sections[i];
         if ((p_227073_4_ & 1 << i) == 0) {
            if (flag && chunksection != EMPTY_SECTION) {
               this.sections[i] = EMPTY_SECTION;
            }
         } else {
            if (chunksection == EMPTY_SECTION) {
               chunksection = new ChunkSection(i << 4);
               this.sections[i] = chunksection;
            }

            chunksection.read(p_227073_2_);
         }
      }

      if (p_227073_1_ != null) {
         this.biomes = p_227073_1_;
      }

      for(Heightmap.Type heightmap$type : Heightmap.Type.values()) {
         String s = heightmap$type.getSerializationKey();
         if (p_227073_3_.contains(s, 12)) {
            this.setHeightmap(heightmap$type, p_227073_3_.getLongArray(s));
         }
      }

      for(TileEntity tileentity : this.blockEntities.values()) {
         tileentity.clearCache();
      }

   }

   public BiomeContainer getBiomes() {
      return this.biomes;
   }

   public void setLoaded(boolean pLoaded) {
      this.loaded = pLoaded;
   }

   public World getLevel() {
      return this.level;
   }

   public Collection<Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
      return Collections.unmodifiableSet(this.heightmaps.entrySet());
   }

   public Map<BlockPos, TileEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public ClassInheritanceMultiMap<Entity>[] getEntitySections() {
      return this.entitySections;
   }

   public CompoundNBT getBlockEntityNbt(BlockPos pPos) {
      return this.pendingBlockEntities.get(pPos);
   }

   public Stream<BlockPos> getLights() {
      return StreamSupport.stream(BlockPos.betweenClosed(this.chunkPos.getMinBlockX(), 0, this.chunkPos.getMinBlockZ(), this.chunkPos.getMaxBlockX(), 255, this.chunkPos.getMaxBlockZ()).spliterator(), false).filter((p_217312_1_) -> {
         return this.getBlockState(p_217312_1_).getLightValue(getLevel(), p_217312_1_) != 0;
      });
   }

   public ITickList<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public ITickList<Fluid> getLiquidTicks() {
      return this.liquidTicks;
   }

   public void setUnsaved(boolean pModified) {
      this.unsaved = pModified;
   }

   public boolean isUnsaved() {
      return this.unsaved || this.lastSaveHadEntities && this.level.getGameTime() != this.lastSaveTime;
   }

   public void setLastSaveHadEntities(boolean p_177409_1_) {
      this.lastSaveHadEntities = p_177409_1_;
   }

   public void setLastSaveTime(long p_177432_1_) {
      this.lastSaveTime = p_177432_1_;
   }

   @Nullable
   public StructureStart<?> getStartForFeature(Structure<?> pStructure) {
      return this.structureStarts.get(pStructure);
   }

   public void setStartForFeature(Structure<?> pStructure, StructureStart<?> pStart) {
      this.structureStarts.put(pStructure, pStart);
   }

   public Map<Structure<?>, StructureStart<?>> getAllStarts() {
      return this.structureStarts;
   }

   public void setAllStarts(Map<Structure<?>, StructureStart<?>> pStructureStarts) {
      this.structureStarts.clear();
      this.structureStarts.putAll(pStructureStarts);
   }

   public LongSet getReferencesForFeature(Structure<?> pStructure) {
      return this.structuresRefences.computeIfAbsent(pStructure, (p_235961_0_) -> {
         return new LongOpenHashSet();
      });
   }

   public void addReferenceForFeature(Structure<?> pStructure, long pChunkValue) {
      this.structuresRefences.computeIfAbsent(pStructure, (p_235960_0_) -> {
         return new LongOpenHashSet();
      }).add(pChunkValue);
   }

   public Map<Structure<?>, LongSet> getAllReferences() {
      return this.structuresRefences;
   }

   public void setAllReferences(Map<Structure<?>, LongSet> pStructureReferences) {
      this.structuresRefences.clear();
      this.structuresRefences.putAll(pStructureReferences);
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void setInhabitedTime(long pNewInhabitedTime) {
      this.inhabitedTime = pNewInhabitedTime;
   }

   public void postProcessGeneration() {
      ChunkPos chunkpos = this.getPos();

      for(int i = 0; i < this.postProcessing.length; ++i) {
         if (this.postProcessing[i] != null) {
            for(Short oshort : this.postProcessing[i]) {
               BlockPos blockpos = ChunkPrimer.unpackOffsetCoordinates(oshort, i, chunkpos);
               BlockState blockstate = this.getBlockState(blockpos);
               BlockState blockstate1 = Block.updateFromNeighbourShapes(blockstate, this.level, blockpos);
               this.level.setBlock(blockpos, blockstate1, 20);
            }

            this.postProcessing[i].clear();
         }
      }

      this.unpackTicks();

      for(BlockPos blockpos1 : Sets.newHashSet(this.pendingBlockEntities.keySet())) {
         this.getBlockEntity(blockpos1);
      }

      this.pendingBlockEntities.clear();
      this.upgradeData.upgrade(this);
   }

   /**
    * Sets up or deserializes a {@link TileEntity} at the desired location from the given compound. If the compound's
    * TileEntity id is {@code "DUMMY"}, the TileEntity may be created by the {@link ITileProvider} instance if the
    * {@link Block} at the given position is in fact a provider. Otherwise, the TileEntity is deserialized at the given
    * position.
    */
   @Nullable
   private TileEntity promotePendingBlockEntity(BlockPos pPos, CompoundNBT pCompound) {
      BlockState blockstate = this.getBlockState(pPos);
      TileEntity tileentity;
      if ("DUMMY".equals(pCompound.getString("id"))) {
         if (blockstate.hasTileEntity()) {
            tileentity = blockstate.createTileEntity(this.level);
         } else {
            tileentity = null;
            LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", pPos, blockstate);
         }
      } else {
         tileentity = TileEntity.loadStatic(blockstate, pCompound);
      }

      if (tileentity != null) {
         tileentity.setLevelAndPosition(this.level, pPos);
         this.addBlockEntity(tileentity);
      } else {
         LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", blockstate, pPos);
      }

      return tileentity;
   }

   public UpgradeData getUpgradeData() {
      return this.upgradeData;
   }

   public ShortList[] getPostProcessing() {
      return this.postProcessing;
   }

   /**
    * Reschedule all serialized scheduled ticks this chunk had
    */
   public void unpackTicks() {
      if (this.blockTicks instanceof ChunkPrimerTickList) {
         ((ChunkPrimerTickList<Block>)this.blockTicks).copyOut(this.level.getBlockTicks(), (p_222881_1_) -> {
            return this.getBlockState(p_222881_1_).getBlock();
         });
         this.blockTicks = EmptyTickList.empty();
      } else if (this.blockTicks instanceof SerializableTickList) {
         ((SerializableTickList)this.blockTicks).copyOut(this.level.getBlockTicks());
         this.blockTicks = EmptyTickList.empty();
      }

      if (this.liquidTicks instanceof ChunkPrimerTickList) {
         ((ChunkPrimerTickList<Fluid>)this.liquidTicks).copyOut(this.level.getLiquidTicks(), (p_222878_1_) -> {
            return this.getFluidState(p_222878_1_).getType();
         });
         this.liquidTicks = EmptyTickList.empty();
      } else if (this.liquidTicks instanceof SerializableTickList) {
         ((SerializableTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks());
         this.liquidTicks = EmptyTickList.empty();
      }

   }

   /**
    * Remove scheduled ticks belonging to this chunk from the world and keep it locally for incoming serialization
    */
   public void packTicks(ServerWorld pServerLevel) {
      if (this.blockTicks == EmptyTickList.<Block>empty()) {
         this.blockTicks = new SerializableTickList<>(Registry.BLOCK::getKey, pServerLevel.getBlockTicks().fetchTicksInChunk(this.chunkPos, true, false), pServerLevel.getGameTime());
         this.setUnsaved(true);
      }

      if (this.liquidTicks == EmptyTickList.<Fluid>empty()) {
         this.liquidTicks = new SerializableTickList<>(Registry.FLUID::getKey, pServerLevel.getLiquidTicks().fetchTicksInChunk(this.chunkPos, true, false), pServerLevel.getGameTime());
         this.setUnsaved(true);
      }

   }

   public ChunkStatus getStatus() {
      return ChunkStatus.FULL;
   }

   public ChunkHolder.LocationType getFullStatus() {
      return this.fullStatus == null ? ChunkHolder.LocationType.BORDER : this.fullStatus.get();
   }

   public void setFullStatus(Supplier<ChunkHolder.LocationType> pLocationType) {
      this.fullStatus = pLocationType;
   }

   public boolean isLightCorrect() {
      return this.isLightCorrect;
   }

   public void setLightCorrect(boolean pLightCorrect) {
      this.isLightCorrect = pLightCorrect;
      this.setUnsaved(true);
   }

   public static enum CreateEntityType {
      IMMEDIATE,
      QUEUED,
      CHECK;
   }

   /**
    * <strong>FOR INTERNAL USE ONLY</strong>
    * <p>
    * Only public for use in {@link AnvilChunkLoader}.
    */
   @java.lang.Deprecated
   @javax.annotation.Nullable
   public final CompoundNBT writeCapsToNBT() {
      return this.serializeCaps();
   }

   /**
    * <strong>FOR INTERNAL USE ONLY</strong>
    * <p>
    * Only public for use in {@link AnvilChunkLoader}.
    */
   @java.lang.Deprecated
   public final void readCapsFromNBT(CompoundNBT tag) {
      this.deserializeCaps(tag);
   }

   @Override
   public World getWorldForge() {
      return getLevel();
   }
}
