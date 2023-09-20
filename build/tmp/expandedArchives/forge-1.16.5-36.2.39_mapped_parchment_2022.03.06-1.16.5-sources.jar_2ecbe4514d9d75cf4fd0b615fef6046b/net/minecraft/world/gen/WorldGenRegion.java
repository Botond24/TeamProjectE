package net.minecraft.world.gen;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.WorldGenTickList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenRegion implements ISeedReader {
   private static final Logger LOGGER = LogManager.getLogger();
   private final List<IChunk> cache;
   private final int x;
   private final int z;
   private final int size;
   private final ServerWorld level;
   private final long seed;
   private final IWorldInfo levelData;
   private final Random random;
   private final DimensionType dimensionType;
   private final ITickList<Block> blockTicks = new WorldGenTickList<>((p_205335_1_) -> {
      return this.getChunk(p_205335_1_).getBlockTicks();
   });
   private final ITickList<Fluid> liquidTicks = new WorldGenTickList<>((p_205334_1_) -> {
      return this.getChunk(p_205334_1_).getLiquidTicks();
   });
   private final BiomeManager biomeManager;
   private final ChunkPos firstPos;
   private final ChunkPos lastPos;
   private final StructureManager structureFeatureManager;

   public WorldGenRegion(ServerWorld p_i50698_1_, List<IChunk> p_i50698_2_) {
      int i = MathHelper.floor(Math.sqrt((double)p_i50698_2_.size()));
      if (i * i != p_i50698_2_.size()) {
         throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Cache size is not a square."));
      } else {
         ChunkPos chunkpos = p_i50698_2_.get(p_i50698_2_.size() / 2).getPos();
         this.cache = p_i50698_2_;
         this.x = chunkpos.x;
         this.z = chunkpos.z;
         this.size = i;
         this.level = p_i50698_1_;
         this.seed = p_i50698_1_.getSeed();
         this.levelData = p_i50698_1_.getLevelData();
         this.random = p_i50698_1_.getRandom();
         this.dimensionType = p_i50698_1_.dimensionType();
         this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(this.seed), p_i50698_1_.dimensionType().getBiomeZoomer());
         this.firstPos = p_i50698_2_.get(0).getPos();
         this.lastPos = p_i50698_2_.get(p_i50698_2_.size() - 1).getPos();
         this.structureFeatureManager = p_i50698_1_.structureFeatureManager().forWorldGenRegion(this);
      }
   }

   public int getCenterX() {
      return this.x;
   }

   public int getCenterZ() {
      return this.z;
   }

   public IChunk getChunk(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.EMPTY);
   }

   @Nullable
   public IChunk getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
      IChunk ichunk;
      if (this.hasChunk(pX, pZ)) {
         int i = pX - this.firstPos.x;
         int j = pZ - this.firstPos.z;
         ichunk = this.cache.get(i + j * this.size);
         if (ichunk.getStatus().isOrAfter(pRequiredStatus)) {
            return ichunk;
         }
      } else {
         ichunk = null;
      }

      if (!pNonnull) {
         return null;
      } else {
         LOGGER.error("Requested chunk : {} {}", pX, pZ);
         LOGGER.error("Region bounds : {} {} | {} {}", this.firstPos.x, this.firstPos.z, this.lastPos.x, this.lastPos.z);
         if (ichunk != null) {
            throw (RuntimeException)Util.pauseInIde(new RuntimeException(String.format("Chunk is not of correct status. Expecting %s, got %s | %s %s", pRequiredStatus, ichunk.getStatus(), pX, pZ)));
         } else {
            throw (RuntimeException)Util.pauseInIde(new RuntimeException(String.format("We are asking a region for a chunk out of bound | %s %s", pX, pZ)));
         }
      }
   }

   public boolean hasChunk(int pChunkX, int pChunkZ) {
      return pChunkX >= this.firstPos.x && pChunkX <= this.lastPos.x && pChunkZ >= this.firstPos.z && pChunkZ <= this.lastPos.z;
   }

   public BlockState getBlockState(BlockPos pPos) {
      return this.getChunk(pPos.getX() >> 4, pPos.getZ() >> 4).getBlockState(pPos);
   }

   public FluidState getFluidState(BlockPos pPos) {
      return this.getChunk(pPos).getFluidState(pPos);
   }

   @Nullable
   public PlayerEntity getNearestPlayer(double pX, double pY, double pZ, double pDistance, Predicate<Entity> pPredicate) {
      return null;
   }

   public int getSkyDarken() {
      return 0;
   }

   public BiomeManager getBiomeManager() {
      return this.biomeManager;
   }

   public Biome getUncachedNoiseBiome(int pX, int pY, int pZ) {
      return this.level.getUncachedNoiseBiome(pX, pY, pZ);
   }

   @OnlyIn(Dist.CLIENT)
   public float getShade(Direction pDirection, boolean pIsShade) {
      return 1.0F;
   }

   public WorldLightManager getLightEngine() {
      return this.level.getLightEngine();
   }

   public boolean destroyBlock(BlockPos pPos, boolean pDropBlock, @Nullable Entity pEntity, int pRecursionLeft) {
      BlockState blockstate = this.getBlockState(pPos);
      if (blockstate.isAir(this, pPos)) {
         return false;
      } else {
         if (pDropBlock) {
            TileEntity tileentity = blockstate.hasTileEntity() ? this.getBlockEntity(pPos) : null;
            Block.dropResources(blockstate, this.level, pPos, tileentity, pEntity, ItemStack.EMPTY);
         }

         return this.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3, pRecursionLeft);
      }
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos) {
      IChunk ichunk = this.getChunk(pPos);
      TileEntity tileentity = ichunk.getBlockEntity(pPos);
      if (tileentity != null) {
         return tileentity;
      } else {
         CompoundNBT compoundnbt = ichunk.getBlockEntityNbt(pPos);
         BlockState blockstate = ichunk.getBlockState(pPos);
         if (compoundnbt != null) {
            if ("DUMMY".equals(compoundnbt.getString("id"))) {
               Block block = blockstate.getBlock();
               if (!blockstate.hasTileEntity()) {
                  return null;
               }

               tileentity = blockstate.createTileEntity(this.level);
            } else {
               tileentity = TileEntity.loadStatic(blockstate, compoundnbt);
            }

            if (tileentity != null) {
               ichunk.setBlockEntity(pPos, tileentity);
               return tileentity;
            }
         }

         if (blockstate.hasTileEntity()) {
            LOGGER.warn("Tried to access a block entity before it was created. {}", (Object)pPos);
         }

         return null;
      }
   }

   public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
      IChunk ichunk = this.getChunk(pPos);
      BlockState blockstate = ichunk.setBlockState(pPos, pState, false);
      if (blockstate != null) {
         this.level.onBlockStateChange(pPos, blockstate, pState);
      }

      Block block = pState.getBlock();
      if (pState.hasTileEntity()) {
         if (ichunk.getStatus().getChunkType() == ChunkStatus.Type.LEVELCHUNK) {
            ichunk.setBlockEntity(pPos, pState.createTileEntity(this));
         } else {
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.putInt("x", pPos.getX());
            compoundnbt.putInt("y", pPos.getY());
            compoundnbt.putInt("z", pPos.getZ());
            compoundnbt.putString("id", "DUMMY");
            ichunk.setBlockEntityNbt(compoundnbt);
         }
      } else if (blockstate != null && blockstate.hasTileEntity()) {
         ichunk.removeBlockEntity(pPos);
      }

      if (pState.hasPostProcess(this, pPos)) {
         this.markPosForPostprocessing(pPos);
      }

      return true;
   }

   private void markPosForPostprocessing(BlockPos pPos) {
      this.getChunk(pPos).markPosForPostprocessing(pPos);
   }

   public boolean addFreshEntity(Entity pEntity) {
      int i = MathHelper.floor(pEntity.getX() / 16.0D);
      int j = MathHelper.floor(pEntity.getZ() / 16.0D);
      this.getChunk(i, j).addEntity(pEntity);
      return true;
   }

   public boolean removeBlock(BlockPos pPos, boolean pIsMoving) {
      return this.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
   }

   public WorldBorder getWorldBorder() {
      return this.level.getWorldBorder();
   }

   public boolean isClientSide() {
      return false;
   }

   @Deprecated
   public ServerWorld getLevel() {
      return this.level;
   }

   public DynamicRegistries registryAccess() {
      return this.level.registryAccess();
   }

   /**
    * Returns the world's WorldInfo object
    */
   public IWorldInfo getLevelData() {
      return this.levelData;
   }

   public DifficultyInstance getCurrentDifficultyAt(BlockPos pPos) {
      if (!this.hasChunk(pPos.getX() >> 4, pPos.getZ() >> 4)) {
         throw new RuntimeException("We are asking a region for a chunk out of bound");
      } else {
         return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
      }
   }

   /**
    * Gets the world's chunk provider
    */
   public AbstractChunkProvider getChunkSource() {
      return this.level.getChunkSource();
   }

   /**
    * gets the random world seed
    */
   public long getSeed() {
      return this.seed;
   }

   public ITickList<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public ITickList<Fluid> getLiquidTicks() {
      return this.liquidTicks;
   }

   public int getSeaLevel() {
      return this.level.getSeaLevel();
   }

   public Random getRandom() {
      return this.random;
   }

   public int getHeight(Heightmap.Type pHeightmapType, int pX, int pZ) {
      return this.getChunk(pX >> 4, pZ >> 4).getHeight(pHeightmapType, pX & 15, pZ & 15) + 1;
   }

   /**
    * Plays a sound. On the server, the sound is broadcast to all nearby <em>except</em> the given player. On the
    * client, the sound only plays if the given player is the client player. Thus, this method is intended to be called
    * from code running on both sides. The client plays it locally and the server plays it for everyone else.
    */
   public void playSound(@Nullable PlayerEntity pPlayer, BlockPos pPos, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
   }

   public void addParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
   }

   public void levelEvent(@Nullable PlayerEntity pPlayer, int pType, BlockPos pPos, int pData) {
   }

   public DimensionType dimensionType() {
      return this.dimensionType;
   }

   public boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState) {
      return pState.test(this.getBlockState(pPos));
   }

   public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> pClazz, AxisAlignedBB pArea, @Nullable Predicate<? super T> pFilter) {
      return Collections.emptyList();
   }

   /**
    * Gets all entities within the specified AABB excluding the one passed into it.
    */
   public List<Entity> getEntities(@Nullable Entity pEntity, AxisAlignedBB pArea, @Nullable Predicate<? super Entity> pPredicate) {
      return Collections.emptyList();
   }

   public List<PlayerEntity> players() {
      return Collections.emptyList();
   }

   public Stream<? extends StructureStart<?>> startsForFeature(SectionPos pPos, Structure<?> pStructure) {
      return this.structureFeatureManager.startsForFeature(pPos, pStructure);
   }
}
