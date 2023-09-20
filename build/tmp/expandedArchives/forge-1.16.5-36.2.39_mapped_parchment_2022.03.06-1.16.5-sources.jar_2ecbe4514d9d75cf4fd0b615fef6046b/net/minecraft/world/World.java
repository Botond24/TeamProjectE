package net.minecraft.world;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class World extends net.minecraftforge.common.capabilities.CapabilityProvider<World> implements IWorld, AutoCloseable, net.minecraftforge.common.extensions.IForgeWorld {
   protected static final Logger LOGGER = LogManager.getLogger();
   public static final Codec<RegistryKey<World>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC.xmap(RegistryKey.elementKey(Registry.DIMENSION_REGISTRY), RegistryKey::location);
   public static final RegistryKey<World> OVERWORLD = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld"));
   public static final RegistryKey<World> NETHER = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("the_nether"));
   public static final RegistryKey<World> END = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("the_end"));
   private static final Direction[] DIRECTIONS = Direction.values();
   public final List<TileEntity> blockEntityList = Lists.newArrayList();
   public final List<TileEntity> tickableBlockEntities = Lists.newArrayList();
   protected final List<TileEntity> pendingBlockEntities = Lists.newArrayList();
   protected final java.util.Set<TileEntity> blockEntitiesToUnload = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()); // Forge: faster "contains" makes removal much more efficient
   private final Thread thread;
   private final boolean isDebug;
   private int skyDarken;
   /**
    * Contains the current Linear Congruential Generator seed for block updates. Used with an A value of 3 and a C value
    * of 0x3c6ef35f, producing a highly planar series of values ill-suited for choosing random blocks in a 16x128x16
    * field.
    */
   protected int randValue = (new Random()).nextInt();
   protected final int addend = 1013904223;
   public float oRainLevel;
   public float rainLevel;
   public float oThunderLevel;
   public float thunderLevel;
   public final Random random = new Random();
   private final DimensionType dimensionType;
   protected final ISpawnWorldInfo levelData;
   private final Supplier<IProfiler> profiler;
   public final boolean isClientSide;
   protected boolean updatingBlockEntities;
   private final WorldBorder worldBorder;
   private final BiomeManager biomeManager;
   private final RegistryKey<World> dimension;
   public boolean restoringBlockSnapshots = false;
   public boolean captureBlockSnapshots = false;
   public java.util.ArrayList<net.minecraftforge.common.util.BlockSnapshot> capturedBlockSnapshots = new java.util.ArrayList<>();

   protected World(ISpawnWorldInfo pLevelData, RegistryKey<World> pDimension, final DimensionType pDimensionType, Supplier<IProfiler> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed) {
      super(World.class);
      this.profiler = pProfiler;
      this.levelData = pLevelData;
      this.dimensionType = pDimensionType;
      this.dimension = pDimension;
      this.isClientSide = pIsClientSide;
      if (pDimensionType.coordinateScale() != 1.0D) {
         this.worldBorder = new WorldBorder() {
            public double getCenterX() {
               return super.getCenterX() / pDimensionType.coordinateScale();
            }

            public double getCenterZ() {
               return super.getCenterZ() / pDimensionType.coordinateScale();
            }
         };
      } else {
         this.worldBorder = new WorldBorder();
      }

      this.thread = Thread.currentThread();
      this.biomeManager = new BiomeManager(this, pBiomeZoomSeed, pDimensionType.getBiomeZoomer());
      this.isDebug = pIsDebug;
   }

   public boolean isClientSide() {
      return this.isClientSide;
   }

   @Nullable
   public MinecraftServer getServer() {
      return null;
   }

   /**
    * Check if the given BlockPos has valid coordinates
    */
   public static boolean isInWorldBounds(BlockPos pPos) {
      return !isOutsideBuildHeight(pPos) && isInWorldBoundsHorizontal(pPos);
   }

   public static boolean isInSpawnableBounds(BlockPos pPos) {
      return !isOutsideSpawnableHeight(pPos.getY()) && isInWorldBoundsHorizontal(pPos);
   }

   private static boolean isInWorldBoundsHorizontal(BlockPos pPos) {
      return pPos.getX() >= -30000000 && pPos.getZ() >= -30000000 && pPos.getX() < 30000000 && pPos.getZ() < 30000000;
   }

   private static boolean isOutsideSpawnableHeight(int pY) {
      return pY < -20000000 || pY >= 20000000;
   }

   public static boolean isOutsideBuildHeight(BlockPos p_189509_0_) {
      return isOutsideBuildHeight(p_189509_0_.getY());
   }

   public static boolean isOutsideBuildHeight(int p_217405_0_) {
      return p_217405_0_ < 0 || p_217405_0_ >= 256;
   }

   public Chunk getChunkAt(BlockPos pPos) {
      return this.getChunk(pPos.getX() >> 4, pPos.getZ() >> 4);
   }

   public Chunk getChunk(int pChunkX, int pChunkZ) {
      return (Chunk)this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL);
   }

   public IChunk getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
      IChunk ichunk = this.getChunkSource().getChunk(pX, pZ, pRequiredStatus, pNonnull);
      if (ichunk == null && pNonnull) {
         throw new IllegalStateException("Should always be able to create a chunk!");
      } else {
         return ichunk;
      }
   }

   /**
    * Sets a block state into this world.Flags are as follows:
    * 1 will cause a block update.
    * 2 will send the change to clients.
    * 4 will prevent the block from being re-rendered.
    * 8 will force any re-renders to run on the main thread instead
    * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
    * 32 will prevent neighbor reactions from spawning drops.
    * 64 will signify the block is being moved.
    * Flags can be OR-ed
    */
   public boolean setBlock(BlockPos pPos, BlockState pNewState, int pFlags) {
      return this.setBlock(pPos, pNewState, pFlags, 512);
   }

   public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
      if (isOutsideBuildHeight(pPos)) {
         return false;
      } else if (!this.isClientSide && this.isDebug()) {
         return false;
      } else {
         Chunk chunk = this.getChunkAt(pPos);
         Block block = pState.getBlock();

         pPos = pPos.immutable(); // Forge - prevent mutable BlockPos leaks
         net.minecraftforge.common.util.BlockSnapshot blockSnapshot = null;
         if (this.captureBlockSnapshots && !this.isClientSide) {
             blockSnapshot = net.minecraftforge.common.util.BlockSnapshot.create(this.dimension, this, pPos, pFlags);
             this.capturedBlockSnapshots.add(blockSnapshot);
         }

         BlockState old = getBlockState(pPos);
         int oldLight = old.getLightValue(this, pPos);
         int oldOpacity = old.getLightBlock(this, pPos);

         BlockState blockstate = chunk.setBlockState(pPos, pState, (pFlags & 64) != 0);
         if (blockstate == null) {
            if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot);
            return false;
         } else {
            BlockState blockstate1 = this.getBlockState(pPos);
            if ((pFlags & 128) == 0 && blockstate1 != blockstate && (blockstate1.getLightBlock(this, pPos) != oldOpacity || blockstate1.getLightValue(this, pPos) != oldLight || blockstate1.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion())) {
               this.getProfiler().push("queueCheckLight");
               this.getChunkSource().getLightEngine().checkBlock(pPos);
               this.getProfiler().pop();
            }

            if (blockSnapshot == null) { // Don't notify clients or update physics while capturing blockstates
               this.markAndNotifyBlock(pPos, chunk, blockstate, pState, pFlags, pRecursionLeft);
            }
            return true;
         }
      }
   }

   // Split off from original setBlockState(BlockPos, BlockState, int, int) method in order to directly send client and physic updates
   public void markAndNotifyBlock(BlockPos pPos, @Nullable Chunk chunk, BlockState blockstate, BlockState pState, int pFlags, int pRecursionLeft)
   {
       Block block = pState.getBlock();
       BlockState blockstate1 = getBlockState(pPos);
       {
           {
            if (blockstate1 == pState) {
               if (blockstate != blockstate1) {
                  this.setBlocksDirty(pPos, blockstate, blockstate1);
               }

               if ((pFlags & 2) != 0 && (!this.isClientSide || (pFlags & 4) == 0) && (this.isClientSide || chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(ChunkHolder.LocationType.TICKING))) {
                  this.sendBlockUpdated(pPos, blockstate, pState, pFlags);
               }

               if ((pFlags & 1) != 0) {
                  this.blockUpdated(pPos, blockstate.getBlock());
                  if (!this.isClientSide && pState.hasAnalogOutputSignal()) {
                     this.updateNeighbourForOutputSignal(pPos, block);
                  }
               }

               if ((pFlags & 16) == 0 && pRecursionLeft > 0) {
                  int i = pFlags & -34;
                  blockstate.updateIndirectNeighbourShapes(this, pPos, i, pRecursionLeft - 1);
                  pState.updateNeighbourShapes(this, pPos, i, pRecursionLeft - 1);
                  pState.updateIndirectNeighbourShapes(this, pPos, i, pRecursionLeft - 1);
               }

               this.onBlockStateChange(pPos, blockstate, blockstate1);
            }
         }
      }
   }

   public void onBlockStateChange(BlockPos pPos, BlockState pBlockState, BlockState pNewState) {
   }

   public boolean removeBlock(BlockPos pPos, boolean pIsMoving) {
      FluidState fluidstate = this.getFluidState(pPos);
      return this.setBlock(pPos, fluidstate.createLegacyBlock(), 3 | (pIsMoving ? 64 : 0));
   }

   public boolean destroyBlock(BlockPos pPos, boolean pDropBlock, @Nullable Entity pEntity, int pRecursionLeft) {
      BlockState blockstate = this.getBlockState(pPos);
      if (blockstate.isAir(this, pPos)) {
         return false;
      } else {
         FluidState fluidstate = this.getFluidState(pPos);
         if (!(blockstate.getBlock() instanceof AbstractFireBlock)) {
            this.levelEvent(2001, pPos, Block.getId(blockstate));
         }

         if (pDropBlock) {
            TileEntity tileentity = blockstate.hasTileEntity() ? this.getBlockEntity(pPos) : null;
            Block.dropResources(blockstate, this, pPos, tileentity, pEntity, ItemStack.EMPTY);
         }

         return this.setBlock(pPos, fluidstate.createLegacyBlock(), 3, pRecursionLeft);
      }
   }

   /**
    * Convenience method to update the block on both the client and server
    */
   public boolean setBlockAndUpdate(BlockPos pPos, BlockState pState) {
      return this.setBlock(pPos, pState, 3);
   }

   /**
    * Flags are as in setBlockState
    */
   public abstract void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags);

   public void setBlocksDirty(BlockPos pBlockPos, BlockState pOldState, BlockState pNewState) {
   }

   public void updateNeighborsAt(BlockPos pPos, Block pBlock) {
      if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(this, pPos, this.getBlockState(pPos), java.util.EnumSet.allOf(Direction.class), false).isCanceled())
         return;
      this.neighborChanged(pPos.west(), pBlock, pPos);
      this.neighborChanged(pPos.east(), pBlock, pPos);
      this.neighborChanged(pPos.below(), pBlock, pPos);
      this.neighborChanged(pPos.above(), pBlock, pPos);
      this.neighborChanged(pPos.north(), pBlock, pPos);
      this.neighborChanged(pPos.south(), pBlock, pPos);
   }

   public void updateNeighborsAtExceptFromFacing(BlockPos pPos, Block pBlockType, Direction pSkipSide) {
      java.util.EnumSet<Direction> directions = java.util.EnumSet.allOf(Direction.class);
      directions.remove(pSkipSide);
      if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(this, pPos, this.getBlockState(pPos), directions, false).isCanceled())
         return;

      if (pSkipSide != Direction.WEST) {
         this.neighborChanged(pPos.west(), pBlockType, pPos);
      }

      if (pSkipSide != Direction.EAST) {
         this.neighborChanged(pPos.east(), pBlockType, pPos);
      }

      if (pSkipSide != Direction.DOWN) {
         this.neighborChanged(pPos.below(), pBlockType, pPos);
      }

      if (pSkipSide != Direction.UP) {
         this.neighborChanged(pPos.above(), pBlockType, pPos);
      }

      if (pSkipSide != Direction.NORTH) {
         this.neighborChanged(pPos.north(), pBlockType, pPos);
      }

      if (pSkipSide != Direction.SOUTH) {
         this.neighborChanged(pPos.south(), pBlockType, pPos);
      }

   }

   public void neighborChanged(BlockPos pPos, Block pBlock, BlockPos pFromPos) {
      if (!this.isClientSide) {
         BlockState blockstate = this.getBlockState(pPos);

         try {
            blockstate.neighborChanged(this, pPos, pBlock, pFromPos, false);
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being updated");
            crashreportcategory.setDetail("Source block type", () -> {
               try {
                  return String.format("ID #%s (%s // %s)", pBlock.getRegistryName(), pBlock.getDescriptionId(), pBlock.getClass().getCanonicalName());
               } catch (Throwable throwable1) {
                  return "ID #" + pBlock.getRegistryName();
               }
            });
            CrashReportCategory.populateBlockDetails(crashreportcategory, pPos, blockstate);
            throw new ReportedException(crashreport);
         }
      }
   }

   public int getHeight(Heightmap.Type pHeightmapType, int pX, int pZ) {
      int i;
      if (pX >= -30000000 && pZ >= -30000000 && pX < 30000000 && pZ < 30000000) {
         if (this.hasChunk(pX >> 4, pZ >> 4)) {
            i = this.getChunk(pX >> 4, pZ >> 4).getHeight(pHeightmapType, pX & 15, pZ & 15) + 1;
         } else {
            i = 0;
         }
      } else {
         i = this.getSeaLevel() + 1;
      }

      return i;
   }

   public WorldLightManager getLightEngine() {
      return this.getChunkSource().getLightEngine();
   }

   public BlockState getBlockState(BlockPos pPos) {
      if (isOutsideBuildHeight(pPos)) {
         return Blocks.VOID_AIR.defaultBlockState();
      } else {
         Chunk chunk = this.getChunk(pPos.getX() >> 4, pPos.getZ() >> 4);
         return chunk.getBlockState(pPos);
      }
   }

   public FluidState getFluidState(BlockPos pPos) {
      if (isOutsideBuildHeight(pPos)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         Chunk chunk = this.getChunkAt(pPos);
         return chunk.getFluidState(pPos);
      }
   }

   /**
    * Checks whether its daytime by seeing if the light subtracted from the skylight is less than 4. Always returns true
    * on the client because vanilla has no need for it on the client, therefore it is not synced to the client
    */
   public boolean isDay() {
      return !this.dimensionType().hasFixedTime() && this.skyDarken < 4;
   }

   public boolean isNight() {
      return !this.dimensionType().hasFixedTime() && !this.isDay();
   }

   /**
    * Plays a sound. On the server, the sound is broadcast to all nearby <em>except</em> the given player. On the
    * client, the sound only plays if the given player is the client player. Thus, this method is intended to be called
    * from code running on both sides. The client plays it locally and the server plays it for everyone else.
    */
   public void playSound(@Nullable PlayerEntity pPlayer, BlockPos pPos, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
      this.playSound(pPlayer, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, pSound, pCategory, pVolume, pPitch);
   }

   public abstract void playSound(@Nullable PlayerEntity pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch);

   public abstract void playSound(@Nullable PlayerEntity pPlayer, Entity pEntity, SoundEvent pEvent, SoundCategory pCategory, float pVolume, float pPitch);

   public void playLocalSound(double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
   }

   public void addParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
   }

   @OnlyIn(Dist.CLIENT)
   public void addParticle(IParticleData pParticleData, boolean pForceAlwaysRender, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
   }

   public void addAlwaysVisibleParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
   }

   public void addAlwaysVisibleParticle(IParticleData pParticleData, boolean pIgnoreRange, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
   }

   /**
    * Return getCelestialAngle()*2*PI
    */
   public float getSunAngle(float pPartialTicks) {
      float f = this.getTimeOfDay(pPartialTicks);
      return f * ((float)Math.PI * 2F);
   }

   public boolean addBlockEntity(TileEntity p_175700_1_) {
      if (p_175700_1_.getLevel() != this) p_175700_1_.setLevelAndPosition(this, p_175700_1_.getBlockPos()); // Forge - set the world early as vanilla doesn't set it until next tick
      if (this.updatingBlockEntities) {
         LOGGER.error("Adding block entity while ticking: {} @ {}", () -> {
            return Registry.BLOCK_ENTITY_TYPE.getKey(p_175700_1_.getType());
         }, p_175700_1_::getBlockPos);
         return pendingBlockEntities.add(p_175700_1_); // Forge: wait to add new TE if we're currently processing existing ones
      }

      boolean flag = this.blockEntityList.add(p_175700_1_);
      if (flag && p_175700_1_ instanceof ITickableTileEntity) {
         this.tickableBlockEntities.add(p_175700_1_);
      }

      p_175700_1_.onLoad();

      if (this.isClientSide) {
         BlockPos blockpos = p_175700_1_.getBlockPos();
         BlockState blockstate = this.getBlockState(blockpos);
         this.sendBlockUpdated(blockpos, blockstate, blockstate, 2);
      }

      return flag;
   }

   public void addAllPendingBlockEntities(Collection<TileEntity> p_147448_1_) {
      if (this.updatingBlockEntities) {
         p_147448_1_.stream().filter(te -> te.getLevel() != this).forEach(te -> te.setLevelAndPosition(this, te.getBlockPos())); // Forge - set the world early as vanilla doesn't set it until next tick
         this.pendingBlockEntities.addAll(p_147448_1_);
      } else {
         for(TileEntity tileentity : p_147448_1_) {
            this.addBlockEntity(tileentity);
         }
      }

   }

   public void tickBlockEntities() {
      IProfiler iprofiler = this.getProfiler();
      iprofiler.push("blockEntities");
      this.updatingBlockEntities = true;// Forge: Move above remove to prevent CMEs
      if (!this.blockEntitiesToUnload.isEmpty()) {
         this.blockEntitiesToUnload.forEach(e -> e.onChunkUnloaded());
         this.tickableBlockEntities.removeAll(this.blockEntitiesToUnload);
         this.blockEntityList.removeAll(this.blockEntitiesToUnload);
         this.blockEntitiesToUnload.clear();
      }

      Iterator<TileEntity> iterator = this.tickableBlockEntities.iterator();

      while(iterator.hasNext()) {
         TileEntity tileentity = iterator.next();
         if (!tileentity.isRemoved() && tileentity.hasLevel()) {
            BlockPos blockpos = tileentity.getBlockPos();
            if (this.getChunkSource().isTickingChunk(blockpos) && this.getWorldBorder().isWithinBounds(blockpos)) {
               try {
                  net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
                  iprofiler.push(() -> {
                     return String.valueOf(tileentity.getType().getRegistryName());
                  });
                  if (tileentity.getType().isValid(this.getBlockState(blockpos).getBlock())) {
                     ((ITickableTileEntity)tileentity).tick();
                  } else {
                     tileentity.logInvalidState();
                  }

                  iprofiler.pop();
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking block entity");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Block entity being ticked");
                  tileentity.fillCrashReportCategory(crashreportcategory);
                  if (net.minecraftforge.common.ForgeConfig.SERVER.removeErroringTileEntities.get()) {
                     LogManager.getLogger().fatal("{}", crashreport.getFriendlyReport());
                     tileentity.setRemoved();
                     this.removeBlockEntity(tileentity.getBlockPos());
                  } else
                  throw new ReportedException(crashreport);
               }
               finally {
                  net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
               }
            }
         }

         if (tileentity.isRemoved()) {
            iterator.remove();
            this.blockEntityList.remove(tileentity);
            if (this.hasChunkAt(tileentity.getBlockPos())) {
               //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
               Chunk chunk = this.getChunkAt(tileentity.getBlockPos());
               if (chunk.getBlockEntity(tileentity.getBlockPos(), Chunk.CreateEntityType.CHECK) == tileentity)
                  chunk.removeBlockEntity(tileentity.getBlockPos());
            }
         }
      }

      this.updatingBlockEntities = false;
      iprofiler.popPush("pendingBlockEntities");
      if (!this.pendingBlockEntities.isEmpty()) {
         for(int i = 0; i < this.pendingBlockEntities.size(); ++i) {
            TileEntity tileentity1 = this.pendingBlockEntities.get(i);
            if (!tileentity1.isRemoved()) {
               if (!this.blockEntityList.contains(tileentity1)) {
                  this.addBlockEntity(tileentity1);
               }

               if (this.hasChunkAt(tileentity1.getBlockPos())) {
                  Chunk chunk = this.getChunkAt(tileentity1.getBlockPos());
                  BlockState blockstate = chunk.getBlockState(tileentity1.getBlockPos());
                  chunk.setBlockEntity(tileentity1.getBlockPos(), tileentity1);
                  this.sendBlockUpdated(tileentity1.getBlockPos(), blockstate, blockstate, 3);
               }
            }
         }

         this.pendingBlockEntities.clear();
      }

      iprofiler.pop();
   }

   public void guardEntityTick(Consumer<Entity> pConsumerEntity, Entity pEntity) {
      try {
         net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackStart(pEntity);
         pConsumerEntity.accept(pEntity);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking entity");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being ticked");
         pEntity.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      } finally {
         net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackEnd(pEntity);
      }
   }

   public Explosion explode(@Nullable Entity pEntity, double pX, double pY, double pZ, float pExplosionRadius, Explosion.Mode pMode) {
      return this.explode(pEntity, (DamageSource)null, (ExplosionContext)null, pX, pY, pZ, pExplosionRadius, false, pMode);
   }

   public Explosion explode(@Nullable Entity pEntity, double pX, double pY, double pZ, float pExplosionRadius, boolean pCausesFire, Explosion.Mode pMode) {
      return this.explode(pEntity, (DamageSource)null, (ExplosionContext)null, pX, pY, pZ, pExplosionRadius, pCausesFire, pMode);
   }

   public Explosion explode(@Nullable Entity pExploder, @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pContext, double pX, double pY, double pZ, float pSize, boolean pCausesFire, Explosion.Mode pMode) {
      Explosion explosion = new Explosion(this, pExploder, pDamageSource, pContext, pX, pY, pZ, pSize, pCausesFire, pMode);
      if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this, explosion)) return explosion;
      explosion.explode();
      explosion.finalizeExplosion(true);
      return explosion;
   }

   /**
    * Returns the name of the current chunk provider, by calling chunkprovider.makeString()
    */
   public String gatherChunkSourceStats() {
      return this.getChunkSource().gatherStats();
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos) {
      if (isOutsideBuildHeight(pPos)) {
         return null;
      } else if (!this.isClientSide && Thread.currentThread() != this.thread) {
         return null;
      } else {
         TileEntity tileentity = null;
         if (this.updatingBlockEntities) {
            tileentity = this.getPendingBlockEntityAt(pPos);
         }

         if (tileentity == null) {
            tileentity = this.getChunkAt(pPos).getBlockEntity(pPos, Chunk.CreateEntityType.IMMEDIATE);
         }

         if (tileentity == null) {
            tileentity = this.getPendingBlockEntityAt(pPos);
         }

         return tileentity;
      }
   }

   @Nullable
   private TileEntity getPendingBlockEntityAt(BlockPos p_189508_1_) {
      for(int i = 0; i < this.pendingBlockEntities.size(); ++i) {
         TileEntity tileentity = this.pendingBlockEntities.get(i);
         if (!tileentity.isRemoved() && tileentity.getBlockPos().equals(p_189508_1_)) {
            return tileentity;
         }
      }

      return null;
   }

   public void setBlockEntity(BlockPos p_175690_1_, @Nullable TileEntity p_175690_2_) {
      if (!isOutsideBuildHeight(p_175690_1_)) {
         p_175690_1_ = p_175690_1_.immutable(); // Forge - prevent mutable BlockPos leaks
         if (p_175690_2_ != null && !p_175690_2_.isRemoved()) {
            if (this.updatingBlockEntities) {
               p_175690_2_.setLevelAndPosition(this, p_175690_1_);
               Iterator<TileEntity> iterator = this.pendingBlockEntities.iterator();

               while(iterator.hasNext()) {
                  TileEntity tileentity = iterator.next();
                  if (tileentity.getBlockPos().equals(p_175690_1_)) {
                     tileentity.setRemoved();
                     iterator.remove();
                  }
               }

               this.pendingBlockEntities.add(p_175690_2_);
            } else {
               Chunk chunk = this.getChunkAt(p_175690_1_);
               if (chunk != null) chunk.setBlockEntity(p_175690_1_, p_175690_2_);
               this.addBlockEntity(p_175690_2_);
            }
         }

      }
   }

   public void removeBlockEntity(BlockPos pPos) {
      TileEntity tileentity = this.getBlockEntity(pPos);
      if (tileentity != null && this.updatingBlockEntities) {
         tileentity.setRemoved();
         this.pendingBlockEntities.remove(tileentity);
         if (!(tileentity instanceof ITickableTileEntity)) //Forge: If they are not tickable they wont be removed in the update loop.
            this.blockEntityList.remove(tileentity);
      } else {
         if (tileentity != null) {
            this.pendingBlockEntities.remove(tileentity);
            this.blockEntityList.remove(tileentity);
            this.tickableBlockEntities.remove(tileentity);
         }

         this.getChunkAt(pPos).removeBlockEntity(pPos);
      }
      this.updateNeighbourForOutputSignal(pPos, getBlockState(pPos).getBlock()); //Notify neighbors of changes
   }

   public boolean isLoaded(BlockPos pPos) {
      return isOutsideBuildHeight(pPos) ? false : this.getChunkSource().hasChunk(pPos.getX() >> 4, pPos.getZ() >> 4);
   }

   public boolean loadedAndEntityCanStandOnFace(BlockPos pPos, Entity pEntity, Direction pDirection) {
      if (isOutsideBuildHeight(pPos)) {
         return false;
      } else {
         IChunk ichunk = this.getChunk(pPos.getX() >> 4, pPos.getZ() >> 4, ChunkStatus.FULL, false);
         return ichunk == null ? false : ichunk.getBlockState(pPos).entityCanStandOnFace(this, pPos, pEntity, pDirection);
      }
   }

   public boolean loadedAndEntityCanStandOn(BlockPos pPos, Entity pEntity) {
      return this.loadedAndEntityCanStandOnFace(pPos, pEntity, Direction.UP);
   }

   /**
    * Called on construction of the World class to setup the initial skylight values
    */
   public void updateSkyBrightness() {
      double d0 = 1.0D - (double)(this.getRainLevel(1.0F) * 5.0F) / 16.0D;
      double d1 = 1.0D - (double)(this.getThunderLevel(1.0F) * 5.0F) / 16.0D;
      double d2 = 0.5D + 2.0D * MathHelper.clamp((double)MathHelper.cos(this.getTimeOfDay(1.0F) * ((float)Math.PI * 2F)), -0.25D, 0.25D);
      this.skyDarken = (int)((1.0D - d2 * d0 * d1) * 11.0D);
   }

   /**
    * first boolean for hostile mobs and second for peaceful mobs
    */
   public void setSpawnSettings(boolean pHostile, boolean pPeaceful) {
      this.getChunkSource().setSpawnSettings(pHostile, pPeaceful);
   }

   /**
    * Called from World constructor to set rainingStrength and thunderingStrength
    */
   protected void prepareWeather() {
      if (this.levelData.isRaining()) {
         this.rainLevel = 1.0F;
         if (this.levelData.isThundering()) {
            this.thunderLevel = 1.0F;
         }
      }

   }

   public void close() throws IOException {
      this.getChunkSource().close();
   }

   @Nullable
   public IBlockReader getChunkForCollisions(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, false);
   }

   /**
    * Gets all entities within the specified AABB excluding the one passed into it.
    */
   public List<Entity> getEntities(@Nullable Entity pEntity, AxisAlignedBB pArea, @Nullable Predicate<? super Entity> pPredicate) {
      this.getProfiler().incrementCounter("getEntities");
      List<Entity> list = Lists.newArrayList();
      int i = MathHelper.floor((pArea.minX - getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.floor((pArea.maxX + getMaxEntityRadius()) / 16.0D);
      int k = MathHelper.floor((pArea.minZ - getMaxEntityRadius()) / 16.0D);
      int l = MathHelper.floor((pArea.maxZ + getMaxEntityRadius()) / 16.0D);
      AbstractChunkProvider abstractchunkprovider = this.getChunkSource();

      for(int i1 = i; i1 <= j; ++i1) {
         for(int j1 = k; j1 <= l; ++j1) {
            Chunk chunk = abstractchunkprovider.getChunk(i1, j1, false);
            if (chunk != null) {
               chunk.getEntities(pEntity, pArea, list, pPredicate);
            }
         }
      }
      for (net.minecraftforge.entity.PartEntity<?> p : this.getPartEntities()) {
         if (p != pEntity && p.getBoundingBox().intersects(pArea) && (pPredicate == null || pPredicate.test(p))) {
            list.add(p);
         }
      }

      return list;
   }

   public <T extends Entity> List<T> getEntities(@Nullable EntityType<T> p_217394_1_, AxisAlignedBB p_217394_2_, Predicate<? super T> p_217394_3_) {
      this.getProfiler().incrementCounter("getEntities");
      int i = MathHelper.floor((p_217394_2_.minX - getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.ceil((p_217394_2_.maxX + getMaxEntityRadius()) / 16.0D);
      int k = MathHelper.floor((p_217394_2_.minZ - getMaxEntityRadius()) / 16.0D);
      int l = MathHelper.ceil((p_217394_2_.maxZ + getMaxEntityRadius()) / 16.0D);
      List<T> list = Lists.newArrayList();

      for(int i1 = i; i1 < j; ++i1) {
         for(int j1 = k; j1 < l; ++j1) {
            Chunk chunk = this.getChunkSource().getChunk(i1, j1, false);
            if (chunk != null) {
               chunk.getEntities(p_217394_1_, p_217394_2_, list, p_217394_3_);
            }
         }
      }

      return list;
   }

   public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> pClazz, AxisAlignedBB pArea, @Nullable Predicate<? super T> pFilter) {
      this.getProfiler().incrementCounter("getEntities");
      int i = MathHelper.floor((pArea.minX - getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.ceil((pArea.maxX + getMaxEntityRadius()) / 16.0D);
      int k = MathHelper.floor((pArea.minZ - getMaxEntityRadius()) / 16.0D);
      int l = MathHelper.ceil((pArea.maxZ + getMaxEntityRadius()) / 16.0D);
      List<T> list = Lists.newArrayList();
      AbstractChunkProvider abstractchunkprovider = this.getChunkSource();

      for(int i1 = i; i1 < j; ++i1) {
         for(int j1 = k; j1 < l; ++j1) {
            Chunk chunk = abstractchunkprovider.getChunk(i1, j1, false);
            if (chunk != null) {
               chunk.getEntitiesOfClass(pClazz, pArea, list, pFilter);
            }
         }
      }

      return list;
   }

   public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, @Nullable Predicate<? super T> p_225316_3_) {
      this.getProfiler().incrementCounter("getLoadedEntities");
      int i = MathHelper.floor((p_225316_2_.minX - getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.ceil((p_225316_2_.maxX + getMaxEntityRadius()) / 16.0D);
      int k = MathHelper.floor((p_225316_2_.minZ - getMaxEntityRadius()) / 16.0D);
      int l = MathHelper.ceil((p_225316_2_.maxZ + getMaxEntityRadius()) / 16.0D);
      List<T> list = Lists.newArrayList();
      AbstractChunkProvider abstractchunkprovider = this.getChunkSource();

      for(int i1 = i; i1 < j; ++i1) {
         for(int j1 = k; j1 < l; ++j1) {
            Chunk chunk = abstractchunkprovider.getChunkNow(i1, j1);
            if (chunk != null) {
               chunk.getEntitiesOfClass(p_225316_1_, p_225316_2_, list, p_225316_3_);
            }
         }
      }

      return list;
   }

   /**
    * Returns the Entity with the given ID, or null if it doesn't exist in this World.
    */
   @Nullable
   public abstract Entity getEntity(int pId);

   public void blockEntityChanged(BlockPos p_175646_1_, TileEntity p_175646_2_) {
      if (this.hasChunkAt(p_175646_1_)) {
         this.getChunkAt(p_175646_1_).markUnsaved();
      }

      this.updateNeighbourForOutputSignal(p_175646_1_, getBlockState(p_175646_1_).getBlock()); //Notify neighbors of changes
   }

   public int getSeaLevel() {
      return 63;
   }

   /**
    * Returns the single highest strong power out of all directions using getStrongPower(BlockPos, EnumFacing)
    */
   public int getDirectSignalTo(BlockPos pPos) {
      int i = 0;
      i = Math.max(i, this.getDirectSignal(pPos.below(), Direction.DOWN));
      if (i >= 15) {
         return i;
      } else {
         i = Math.max(i, this.getDirectSignal(pPos.above(), Direction.UP));
         if (i >= 15) {
            return i;
         } else {
            i = Math.max(i, this.getDirectSignal(pPos.north(), Direction.NORTH));
            if (i >= 15) {
               return i;
            } else {
               i = Math.max(i, this.getDirectSignal(pPos.south(), Direction.SOUTH));
               if (i >= 15) {
                  return i;
               } else {
                  i = Math.max(i, this.getDirectSignal(pPos.west(), Direction.WEST));
                  if (i >= 15) {
                     return i;
                  } else {
                     i = Math.max(i, this.getDirectSignal(pPos.east(), Direction.EAST));
                     return i >= 15 ? i : i;
                  }
               }
            }
         }
      }
   }

   public boolean hasSignal(BlockPos pPos, Direction pSide) {
      return this.getSignal(pPos, pSide) > 0;
   }

   public int getSignal(BlockPos pPos, Direction pFacing) {
      BlockState blockstate = this.getBlockState(pPos);
      int i = blockstate.getSignal(this, pPos, pFacing);
      return blockstate.shouldCheckWeakPower(this, pPos, pFacing) ? Math.max(i, this.getDirectSignalTo(pPos)) : i;
   }

   public boolean hasNeighborSignal(BlockPos pPos) {
      if (this.getSignal(pPos.below(), Direction.DOWN) > 0) {
         return true;
      } else if (this.getSignal(pPos.above(), Direction.UP) > 0) {
         return true;
      } else if (this.getSignal(pPos.north(), Direction.NORTH) > 0) {
         return true;
      } else if (this.getSignal(pPos.south(), Direction.SOUTH) > 0) {
         return true;
      } else if (this.getSignal(pPos.west(), Direction.WEST) > 0) {
         return true;
      } else {
         return this.getSignal(pPos.east(), Direction.EAST) > 0;
      }
   }

   /**
    * Checks if the specified block or its neighbors are powered by a neighboring block. Used by blocks like TNT and
    * Doors.
    */
   public int getBestNeighborSignal(BlockPos pPos) {
      int i = 0;

      for(Direction direction : DIRECTIONS) {
         int j = this.getSignal(pPos.relative(direction), direction);
         if (j >= 15) {
            return 15;
         }

         if (j > i) {
            i = j;
         }
      }

      return i;
   }

   /**
    * If on MP, sends a quitting packet.
    */
   @OnlyIn(Dist.CLIENT)
   public void disconnect() {
   }

   public long getGameTime() {
      return this.levelData.getGameTime();
   }

   public long getDayTime() {
      return this.levelData.getDayTime();
   }

   public boolean mayInteract(PlayerEntity pPlayer, BlockPos pPos) {
      return true;
   }

   /**
    * sends a Packet 38 (Entity Status) to all tracked players of that entity
    */
   public void broadcastEntityEvent(Entity pEntity, byte pState) {
   }

   public void blockEvent(BlockPos pPos, Block pBlock, int pEventID, int pEventParam) {
      this.getBlockState(pPos).triggerEvent(this, pPos, pEventID, pEventParam);
   }

   /**
    * Returns the world's WorldInfo object
    */
   public IWorldInfo getLevelData() {
      return this.levelData;
   }

   /**
    * Gets the GameRules instance.
    */
   public GameRules getGameRules() {
      return this.levelData.getGameRules();
   }

   public float getThunderLevel(float pDelta) {
      return MathHelper.lerp(pDelta, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(pDelta);
   }

   /**
    * Sets the strength of the thunder.
    */
   @OnlyIn(Dist.CLIENT)
   public void setThunderLevel(float pStrength) {
      this.oThunderLevel = pStrength;
      this.thunderLevel = pStrength;
   }

   /**
    * Returns rain strength.
    */
   public float getRainLevel(float pDelta) {
      return MathHelper.lerp(pDelta, this.oRainLevel, this.rainLevel);
   }

   /**
    * Sets the strength of the rain.
    */
   @OnlyIn(Dist.CLIENT)
   public void setRainLevel(float pStrength) {
      this.oRainLevel = pStrength;
      this.rainLevel = pStrength;
   }

   /**
    * Returns true if the current thunder strength (weighted with the rain strength) is greater than 0.9
    */
   public boolean isThundering() {
      if (this.dimensionType().hasSkyLight() && !this.dimensionType().hasCeiling()) {
         return (double)this.getThunderLevel(1.0F) > 0.9D;
      } else {
         return false;
      }
   }

   /**
    * Returns true if the current rain strength is greater than 0.2
    */
   public boolean isRaining() {
      return (double)this.getRainLevel(1.0F) > 0.2D;
   }

   /**
    * Check if precipitation is currently happening at a position
    */
   public boolean isRainingAt(BlockPos pPosition) {
      if (!this.isRaining()) {
         return false;
      } else if (!this.canSeeSky(pPosition)) {
         return false;
      } else if (this.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, pPosition).getY() > pPosition.getY()) {
         return false;
      } else {
         Biome biome = this.getBiome(pPosition);
         return biome.getPrecipitation() == Biome.RainType.RAIN && biome.getTemperature(pPosition) >= 0.15F;
      }
   }

   public boolean isHumidAt(BlockPos pPos) {
      Biome biome = this.getBiome(pPos);
      return biome.isHumid();
   }

   @Nullable
   public abstract MapData getMapData(String pMapName);

   public abstract void setMapData(MapData p_217399_1_);

   public abstract int getFreeMapId();

   public void globalLevelEvent(int pId, BlockPos pPos, int pData) {
   }

   /**
    * Adds some basic stats of the world to the given crash report.
    */
   public CrashReportCategory fillReportDetails(CrashReport pReport) {
      CrashReportCategory crashreportcategory = pReport.addCategory("Affected level", 1);
      crashreportcategory.setDetail("All players", () -> {
         return this.players().size() + " total; " + this.players();
      });
      crashreportcategory.setDetail("Chunk stats", this.getChunkSource()::gatherStats);
      crashreportcategory.setDetail("Level dimension", () -> {
         return this.dimension().location().toString();
      });

      try {
         this.levelData.fillCrashReportCategory(crashreportcategory);
      } catch (Throwable throwable) {
         crashreportcategory.setDetailError("Level Data Unobtainable", throwable);
      }

      return crashreportcategory;
   }

   public abstract void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress);

   @OnlyIn(Dist.CLIENT)
   public void createFireworks(double pX, double pY, double pZ, double pMotionX, double pMotionY, double pMotionZ, @Nullable CompoundNBT pCompound) {
   }

   public abstract Scoreboard getScoreboard();

   public void updateNeighbourForOutputSignal(BlockPos pPos, Block pBlock) {
      for(Direction direction : Direction.values()) {
         BlockPos blockpos = pPos.relative(direction);
         if (this.hasChunkAt(blockpos)) {
            BlockState blockstate = this.getBlockState(blockpos);
            blockstate.onNeighborChange(this, blockpos, pPos);
            if (blockstate.isRedstoneConductor(this, blockpos)) {
               blockpos = blockpos.relative(direction);
               blockstate = this.getBlockState(blockpos);
               if (blockstate.getWeakChanges(this, blockpos)) {
                  blockstate.neighborChanged(this, blockpos, pBlock, pPos, false);
               }
            }
         }
      }

   }

   public DifficultyInstance getCurrentDifficultyAt(BlockPos pPos) {
      long i = 0L;
      float f = 0.0F;
      if (this.hasChunkAt(pPos)) {
         f = this.getMoonBrightness();
         i = this.getChunkAt(pPos).getInhabitedTime();
      }

      return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), i, f);
   }

   public int getSkyDarken() {
      return this.skyDarken;
   }

   public void setSkyFlashTime(int pTimeFlash) {
   }

   public WorldBorder getWorldBorder() {
      return this.worldBorder;
   }

   public void sendPacketToServer(IPacket<?> pPacket) {
      throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
   }

   public DimensionType dimensionType() {
      return this.dimensionType;
   }

   public RegistryKey<World> dimension() {
      return this.dimension;
   }

   public Random getRandom() {
      return this.random;
   }

   public boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState) {
      return pState.test(this.getBlockState(pPos));
   }

   public abstract RecipeManager getRecipeManager();

   public abstract ITagCollectionSupplier getTagManager();

   public BlockPos getBlockRandomPos(int pX, int pY, int pZ, int pYMask) {
      this.randValue = this.randValue * 3 + 1013904223;
      int i = this.randValue >> 2;
      return new BlockPos(pX + (i & 15), pY + (i >> 16 & pYMask), pZ + (i >> 8 & 15));
   }

   public boolean noSave() {
      return false;
   }

   public IProfiler getProfiler() {
      return this.profiler.get();
   }

   public Supplier<IProfiler> getProfilerSupplier() {
      return this.profiler;
   }

   public BiomeManager getBiomeManager() {
      return this.biomeManager;
   }

   private double maxEntityRadius = 2.0D;
   @Override
   public double getMaxEntityRadius() {
      return maxEntityRadius;
   }
   @Override
   public double increaseMaxEntityRadius(double value) {
      if (value > maxEntityRadius)
         maxEntityRadius = value;
      return maxEntityRadius;
   }

   public final boolean isDebug() {
      return this.isDebug;
   }
}
