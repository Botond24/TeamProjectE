package net.minecraft.world.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerChunkProvider extends AbstractChunkProvider {
   private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
   private final TicketManager distanceManager;
   public final ChunkGenerator generator;
   public final ServerWorld level;
   private final Thread mainThread;
   private final ServerWorldLightManager lightEngine;
   private final ServerChunkProvider.ChunkExecutor mainThreadProcessor;
   public final ChunkManager chunkMap;
   private final DimensionSavedDataManager dataStorage;
   private long lastInhabitedUpdate;
   private boolean spawnEnemies = true;
   private boolean spawnFriendlies = true;
   private final long[] lastChunkPos = new long[4];
   private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
   private final IChunk[] lastChunk = new IChunk[4];
   @Nullable
   private WorldEntitySpawner.EntityDensityManager lastSpawnState;

   public ServerChunkProvider(ServerWorld p_i232603_1_, SaveFormat.LevelSave p_i232603_2_, DataFixer p_i232603_3_, TemplateManager p_i232603_4_, Executor p_i232603_5_, ChunkGenerator p_i232603_6_, int p_i232603_7_, boolean p_i232603_8_, IChunkStatusListener p_i232603_9_, Supplier<DimensionSavedDataManager> p_i232603_10_) {
      this.level = p_i232603_1_;
      this.mainThreadProcessor = new ServerChunkProvider.ChunkExecutor(p_i232603_1_);
      this.generator = p_i232603_6_;
      this.mainThread = Thread.currentThread();
      File file1 = p_i232603_2_.getDimensionPath(p_i232603_1_.dimension());
      File file2 = new File(file1, "data");
      file2.mkdirs();
      this.dataStorage = new DimensionSavedDataManager(file2, p_i232603_3_);
      this.chunkMap = new ChunkManager(p_i232603_1_, p_i232603_2_, p_i232603_3_, p_i232603_4_, p_i232603_5_, this.mainThreadProcessor, this, this.getGenerator(), p_i232603_9_, p_i232603_10_, p_i232603_7_, p_i232603_8_);
      this.lightEngine = this.chunkMap.getLightEngine();
      this.distanceManager = this.chunkMap.getDistanceManager();
      this.clearCache();
   }

   public ServerWorldLightManager getLightEngine() {
      return this.lightEngine;
   }

   @Nullable
   private ChunkHolder getVisibleChunkIfPresent(long p_217213_1_) {
      return this.chunkMap.getVisibleChunkIfPresent(p_217213_1_);
   }

   public int getTickingGenerated() {
      return this.chunkMap.getTickingGenerated();
   }

   private void storeInCache(long p_225315_1_, IChunk p_225315_3_, ChunkStatus p_225315_4_) {
      for(int i = 3; i > 0; --i) {
         this.lastChunkPos[i] = this.lastChunkPos[i - 1];
         this.lastChunkStatus[i] = this.lastChunkStatus[i - 1];
         this.lastChunk[i] = this.lastChunk[i - 1];
      }

      this.lastChunkPos[0] = p_225315_1_;
      this.lastChunkStatus[0] = p_225315_4_;
      this.lastChunk[0] = p_225315_3_;
   }

   @Nullable
   public IChunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
      if (Thread.currentThread() != this.mainThread) {
         return CompletableFuture.supplyAsync(() -> {
            return this.getChunk(pChunkX, pChunkZ, pRequiredStatus, pLoad);
         }, this.mainThreadProcessor).join();
      } else {
         IProfiler iprofiler = this.level.getProfiler();
         iprofiler.incrementCounter("getChunk");
         long i = ChunkPos.asLong(pChunkX, pChunkZ);

         for(int j = 0; j < 4; ++j) {
            if (i == this.lastChunkPos[j] && pRequiredStatus == this.lastChunkStatus[j]) {
               IChunk ichunk = this.lastChunk[j];
               if (ichunk != null || !pLoad) {
                  return ichunk;
               }
            }
         }

         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
         if (chunkholder != null && chunkholder.currentlyLoading != null) {
             return chunkholder.currentlyLoading; //Forge - If the requested chunk is loading, bypass the future chain to prevent a deadlock.
         }

         iprofiler.incrementCounter("getChunkCacheMiss");
         CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> completablefuture = this.getChunkFutureMainThread(pChunkX, pChunkZ, pRequiredStatus, pLoad);
         this.mainThreadProcessor.managedBlock(completablefuture::isDone);
         IChunk ichunk1 = completablefuture.join().map((p_222874_0_) -> {
            return p_222874_0_;
         }, (p_222870_1_) -> {
            if (pLoad) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + p_222870_1_));
            } else {
               return null;
            }
         });
         this.storeInCache(i, ichunk1, pRequiredStatus);
         return ichunk1;
      }
   }

   @Nullable
   public Chunk getChunkNow(int pChunkX, int pChunkZ) {
      if (Thread.currentThread() != this.mainThread) {
         return null;
      } else {
         this.level.getProfiler().incrementCounter("getChunkNow");
         long i = ChunkPos.asLong(pChunkX, pChunkZ);

         for(int j = 0; j < 4; ++j) {
            if (i == this.lastChunkPos[j] && this.lastChunkStatus[j] == ChunkStatus.FULL) {
               IChunk ichunk = this.lastChunk[j];
               return ichunk instanceof Chunk ? (Chunk)ichunk : null;
            }
         }

         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
         if (chunkholder == null) {
            return null;
         } else {
            Either<IChunk, ChunkHolder.IChunkLoadingError> either = chunkholder.getFutureIfPresent(ChunkStatus.FULL).getNow((Either<IChunk, ChunkHolder.IChunkLoadingError>)null);
            if (either == null) {
               return null;
            } else {
               IChunk ichunk1 = either.left().orElse((IChunk)null);
               if (ichunk1 != null) {
                  this.storeInCache(i, ichunk1, ChunkStatus.FULL);
                  if (ichunk1 instanceof Chunk) {
                     return (Chunk)ichunk1;
                  }
               }

               return null;
            }
         }
      }
   }

   private void clearCache() {
      Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
      Arrays.fill(this.lastChunkStatus, (Object)null);
      Arrays.fill(this.lastChunk, (Object)null);
   }

   @OnlyIn(Dist.CLIENT)
   public CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> getChunkFuture(int p_217232_1_, int p_217232_2_, ChunkStatus p_217232_3_, boolean p_217232_4_) {
      boolean flag = Thread.currentThread() == this.mainThread;
      CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> completablefuture;
      if (flag) {
         completablefuture = this.getChunkFutureMainThread(p_217232_1_, p_217232_2_, p_217232_3_, p_217232_4_);
         this.mainThreadProcessor.managedBlock(completablefuture::isDone);
      } else {
         completablefuture = CompletableFuture.supplyAsync(() -> {
            return this.getChunkFutureMainThread(p_217232_1_, p_217232_2_, p_217232_3_, p_217232_4_);
         }, this.mainThreadProcessor).thenCompose((p_217211_0_) -> {
            return p_217211_0_;
         });
      }

      return completablefuture;
   }

   private CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> getChunkFutureMainThread(int p_217233_1_, int p_217233_2_, ChunkStatus p_217233_3_, boolean p_217233_4_) {
      ChunkPos chunkpos = new ChunkPos(p_217233_1_, p_217233_2_);
      long i = chunkpos.toLong();
      int j = 33 + ChunkStatus.getDistance(p_217233_3_);
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
      if (p_217233_4_) {
         this.distanceManager.addTicket(TicketType.UNKNOWN, chunkpos, j, chunkpos);
         if (this.chunkAbsent(chunkholder, j)) {
            IProfiler iprofiler = this.level.getProfiler();
            iprofiler.push("chunkLoad");
            this.runDistanceManagerUpdates();
            chunkholder = this.getVisibleChunkIfPresent(i);
            iprofiler.pop();
            if (this.chunkAbsent(chunkholder, j)) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
            }
         }
      }

      return this.chunkAbsent(chunkholder, j) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkholder.getOrScheduleFuture(p_217233_3_, this.chunkMap);
   }

   private boolean chunkAbsent(@Nullable ChunkHolder p_217224_1_, int p_217224_2_) {
      return p_217224_1_ == null || p_217224_1_.getTicketLevel() > p_217224_2_;
   }

   /**
    * Checks to see if a chunk exists at x, z
    */
   public boolean hasChunk(int pX, int pZ) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent((new ChunkPos(pX, pZ)).toLong());
      int i = 33 + ChunkStatus.getDistance(ChunkStatus.FULL);
      return !this.chunkAbsent(chunkholder, i);
   }

   public IBlockReader getChunkForLighting(int pChunkX, int pChunkZ) {
      long i = ChunkPos.asLong(pChunkX, pChunkZ);
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
      if (chunkholder == null) {
         return null;
      } else {
         int j = CHUNK_STATUSES.size() - 1;

         while(true) {
            ChunkStatus chunkstatus = CHUNK_STATUSES.get(j);
            Optional<IChunk> optional = chunkholder.getFutureIfPresentUnchecked(chunkstatus).getNow(ChunkHolder.UNLOADED_CHUNK).left();
            if (optional.isPresent()) {
               return optional.get();
            }

            if (chunkstatus == ChunkStatus.LIGHT.getParent()) {
               return null;
            }

            --j;
         }
      }
   }

   public World getLevel() {
      return this.level;
   }

   public boolean pollTask() {
      return this.mainThreadProcessor.pollTask();
   }

   private boolean runDistanceManagerUpdates() {
      boolean flag = this.distanceManager.runAllUpdates(this.chunkMap);
      boolean flag1 = this.chunkMap.promoteChunkMap();
      if (!flag && !flag1) {
         return false;
      } else {
         this.clearCache();
         return true;
      }
   }

   public boolean isEntityTickingChunk(Entity p_217204_1_) {
      long i = ChunkPos.asLong(MathHelper.floor(p_217204_1_.getX()) >> 4, MathHelper.floor(p_217204_1_.getZ()) >> 4);
      return this.checkChunkFuture(i, ChunkHolder::getEntityTickingChunkFuture);
   }

   public boolean isEntityTickingChunk(ChunkPos p_222865_1_) {
      return this.checkChunkFuture(p_222865_1_.toLong(), ChunkHolder::getEntityTickingChunkFuture);
   }

   public boolean isTickingChunk(BlockPos p_222866_1_) {
      long i = ChunkPos.asLong(p_222866_1_.getX() >> 4, p_222866_1_.getZ() >> 4);
      return this.checkChunkFuture(i, ChunkHolder::getTickingChunkFuture);
   }

   private boolean checkChunkFuture(long pPos, Function<ChunkHolder, CompletableFuture<Either<Chunk, ChunkHolder.IChunkLoadingError>>> p_222872_3_) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pPos);
      if (chunkholder == null) {
         return false;
      } else {
         Either<Chunk, ChunkHolder.IChunkLoadingError> either = p_222872_3_.apply(chunkholder).getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK);
         return either.left().isPresent();
      }
   }

   public void save(boolean pFlush) {
      this.runDistanceManagerUpdates();
      this.chunkMap.saveAllChunks(pFlush);
   }

   public void close() throws IOException {
      this.save(true);
      this.lightEngine.close();
      this.chunkMap.close();
   }

   public void tick(BooleanSupplier p_217207_1_) {
      this.level.getProfiler().push("purge");
      this.distanceManager.purgeStaleTickets();
      this.runDistanceManagerUpdates();
      this.level.getProfiler().popPush("chunks");
      this.tickChunks();
      this.level.getProfiler().popPush("unload");
      this.chunkMap.tick(p_217207_1_);
      this.level.getProfiler().pop();
      this.clearCache();
   }

   private void tickChunks() {
      long i = this.level.getGameTime();
      long j = i - this.lastInhabitedUpdate;
      this.lastInhabitedUpdate = i;
      IWorldInfo iworldinfo = this.level.getLevelData();
      boolean flag = this.level.isDebug();
      boolean flag1 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
      if (!flag) {
         this.level.getProfiler().push("pollingChunks");
         int k = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
         boolean flag2 = iworldinfo.getGameTime() % 400L == 0L;
         this.level.getProfiler().push("naturalSpawnCount");
         int l = this.distanceManager.getNaturalSpawnChunkCount();
         WorldEntitySpawner.EntityDensityManager worldentityspawner$entitydensitymanager = WorldEntitySpawner.createState(l, this.level.getAllEntities(), this::getFullChunk);
         this.lastSpawnState = worldentityspawner$entitydensitymanager;
         this.level.getProfiler().pop();
         List<ChunkHolder> list = Lists.newArrayList(this.chunkMap.getChunks());
         Collections.shuffle(list);
         list.forEach((p_241099_7_) -> {
            Optional<Chunk> optional = p_241099_7_.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
            if (optional.isPresent()) {
               this.level.getProfiler().push("broadcast");
               p_241099_7_.broadcastChanges(optional.get());
               this.level.getProfiler().pop();
               Optional<Chunk> optional1 = p_241099_7_.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
               if (optional1.isPresent()) {
                  Chunk chunk = optional1.get();
                  ChunkPos chunkpos = p_241099_7_.getPos();
                  if (!this.chunkMap.noPlayersCloseForSpawning(chunkpos) || chunkMap.getDistanceManager().shouldForceTicks(chunkpos.toLong())) {
                     chunk.setInhabitedTime(chunk.getInhabitedTime() + j);
                     if (flag1 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunk.getPos())) {
                        WorldEntitySpawner.spawnForChunk(this.level, chunk, worldentityspawner$entitydensitymanager, this.spawnFriendlies, this.spawnEnemies, flag2);
                     }

                     this.level.tickChunk(chunk, k);
                  }
               }
            }
         });
         this.level.getProfiler().push("customSpawners");
         if (flag1) {
            this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
         }

         this.level.getProfiler().pop();
         this.level.getProfiler().pop();
      }

      this.chunkMap.tick();
   }

   private void getFullChunk(long p_241098_1_, Consumer<Chunk> p_241098_3_) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_241098_1_);
      if (chunkholder != null) {
         chunkholder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().ifPresent(p_241098_3_);
      }

   }

   /**
    * Converts the instance data to a readable string.
    */
   public String gatherStats() {
      return "ServerChunkCache: " + this.getLoadedChunksCount();
   }

   @VisibleForTesting
   public int getPendingTasksCount() {
      return this.mainThreadProcessor.getPendingTasksCount();
   }

   public ChunkGenerator getGenerator() {
      return this.generator;
   }

   public int getLoadedChunksCount() {
      return this.chunkMap.size();
   }

   public void blockChanged(BlockPos pPos) {
      int i = pPos.getX() >> 4;
      int j = pPos.getZ() >> 4;
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(ChunkPos.asLong(i, j));
      if (chunkholder != null) {
         chunkholder.blockChanged(pPos);
      }

   }

   public void onLightUpdate(LightType pType, SectionPos pPos) {
      this.mainThreadProcessor.execute(() -> {
         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pPos.chunk().toLong());
         if (chunkholder != null) {
            chunkholder.sectionLightChanged(pType, pPos.y());
         }

      });
   }

   public <T> void addRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue) {
      this.distanceManager.addRegionTicket(pType, pPos, pDistance, pValue);
   }

   public <T> void removeRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue) {
      this.distanceManager.removeRegionTicket(pType, pPos, pDistance, pValue);
   }

   public <T> void registerTickingTicket(TicketType<T> type, ChunkPos pos, int distance, T value) {
      this.distanceManager.registerTicking(type, pos, distance, value);
   }

   public <T> void releaseTickingTicket(TicketType<T> type, ChunkPos pos, int distance, T value) {
      this.distanceManager.releaseTicking(type, pos, distance, value);
   }

   public void updateChunkForced(ChunkPos pPos, boolean pAdd) {
      this.distanceManager.updateChunkForced(pPos, pAdd);
   }

   public void move(ServerPlayerEntity pPlayer) {
      this.chunkMap.move(pPlayer);
   }

   public void removeEntity(Entity pEntity) {
      this.chunkMap.removeEntity(pEntity);
   }

   public void addEntity(Entity pEntity) {
      this.chunkMap.addEntity(pEntity);
   }

   public void broadcastAndSend(Entity pEntity, IPacket<?> pPacket) {
      this.chunkMap.broadcastAndSend(pEntity, pPacket);
   }

   public void broadcast(Entity pEntity, IPacket<?> pPacket) {
      this.chunkMap.broadcast(pEntity, pPacket);
   }

   public void setViewDistance(int pViewDistance) {
      this.chunkMap.setViewDistance(pViewDistance);
   }

   public void setSpawnSettings(boolean pHostile, boolean pPeaceful) {
      this.spawnEnemies = pHostile;
      this.spawnFriendlies = pPeaceful;
   }

   @OnlyIn(Dist.CLIENT)
   public String getChunkDebugData(ChunkPos pChunkPos) {
      return this.chunkMap.getChunkDebugData(pChunkPos);
   }

   public DimensionSavedDataManager getDataStorage() {
      return this.dataStorage;
   }

   public PointOfInterestManager getPoiManager() {
      return this.chunkMap.getPoiManager();
   }

   @Nullable
   public WorldEntitySpawner.EntityDensityManager getLastSpawnState() {
      return this.lastSpawnState;
   }

   final class ChunkExecutor extends ThreadTaskExecutor<Runnable> {
      private ChunkExecutor(World p_i50985_2_) {
         super("Chunk source main thread executor for " + p_i50985_2_.dimension().location());
      }

      protected Runnable wrapRunnable(Runnable pRunnable) {
         return pRunnable;
      }

      protected boolean shouldRun(Runnable pRunnable) {
         return true;
      }

      protected boolean scheduleExecutables() {
         return true;
      }

      protected Thread getRunningThread() {
         return ServerChunkProvider.this.mainThread;
      }

      protected void doRunTask(Runnable pTask) {
         ServerChunkProvider.this.level.getProfiler().incrementCounter("runTask");
         super.doRunTask(pTask);
      }

      protected boolean pollTask() {
         if (ServerChunkProvider.this.runDistanceManagerUpdates()) {
            return true;
         } else {
            ServerChunkProvider.this.lightEngine.tryScheduleUpdate();
            return super.pollTask();
         }
      }
   }
}