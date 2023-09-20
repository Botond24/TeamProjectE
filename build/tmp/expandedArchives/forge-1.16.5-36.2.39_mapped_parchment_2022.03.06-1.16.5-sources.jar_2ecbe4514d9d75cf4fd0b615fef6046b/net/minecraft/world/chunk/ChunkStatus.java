package net.minecraft.world.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.ServerWorldLightManager;

public class ChunkStatus extends net.minecraftforge.registries.ForgeRegistryEntry<ChunkStatus> {
   private static final EnumSet<Heightmap.Type> PRE_FEATURES = EnumSet.of(Heightmap.Type.OCEAN_FLOOR_WG, Heightmap.Type.WORLD_SURFACE_WG);
   private static final EnumSet<Heightmap.Type> POST_FEATURES = EnumSet.of(Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE, Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);
   private static final ChunkStatus.ILoadingWorker PASSTHROUGH_LOAD_TASK = (p_222588_0_, p_222588_1_, p_222588_2_, p_222588_3_, p_222588_4_, p_222588_5_) -> {
      if (p_222588_5_ instanceof ChunkPrimer && !p_222588_5_.getStatus().isOrAfter(p_222588_0_)) {
         ((ChunkPrimer)p_222588_5_).setStatus(p_222588_0_);
      }

      return CompletableFuture.completedFuture(Either.left(p_222588_5_));
   };
   public static final ChunkStatus EMPTY = registerSimple("empty", (ChunkStatus)null, -1, PRE_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_223194_0_, p_223194_1_, p_223194_2_, p_223194_3_) -> {
   });
   public static final ChunkStatus STRUCTURE_STARTS = register("structure_starts", EMPTY, 0, PRE_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222586_0_, p_222586_1_, p_222586_2_, p_222586_3_, p_222586_4_, p_222586_5_, p_222586_6_, p_222586_7_) -> {
      if (!p_222586_7_.getStatus().isOrAfter(p_222586_0_)) {
         if (p_222586_1_.getServer().getWorldData().worldGenSettings().generateFeatures()) {
            p_222586_2_.createStructures(p_222586_1_.registryAccess(), p_222586_1_.structureFeatureManager(), p_222586_7_, p_222586_3_, p_222586_1_.getSeed());
         }

         if (p_222586_7_ instanceof ChunkPrimer) {
            ((ChunkPrimer)p_222586_7_).setStatus(p_222586_0_);
         }
      }

      return CompletableFuture.completedFuture(Either.left(p_222586_7_));
   });
   public static final ChunkStatus STRUCTURE_REFERENCES = registerSimple("structure_references", STRUCTURE_STARTS, 8, PRE_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222587_0_, p_222587_1_, p_222587_2_, p_222587_3_) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(p_222587_0_, p_222587_2_);
      p_222587_1_.createReferences(worldgenregion, p_222587_0_.structureFeatureManager().forWorldGenRegion(worldgenregion), p_222587_3_);
   });
   public static final ChunkStatus BIOMES = registerSimple("biomes", STRUCTURE_REFERENCES, 0, PRE_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222594_0_, p_222594_1_, p_222594_2_, p_222594_3_) -> {
      p_222594_1_.createBiomes(p_222594_0_.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), p_222594_3_);
   });
   public static final ChunkStatus NOISE = registerSimple("noise", BIOMES, 8, PRE_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222592_0_, p_222592_1_, p_222592_2_, p_222592_3_) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(p_222592_0_, p_222592_2_);
      p_222592_1_.fillFromNoise(worldgenregion, p_222592_0_.structureFeatureManager().forWorldGenRegion(worldgenregion), p_222592_3_);
   });
   public static final ChunkStatus SURFACE = registerSimple("surface", NOISE, 0, PRE_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222589_0_, p_222589_1_, p_222589_2_, p_222589_3_) -> {
      p_222589_1_.buildSurfaceAndBedrock(new WorldGenRegion(p_222589_0_, p_222589_2_), p_222589_3_);
   });
   public static final ChunkStatus CARVERS = registerSimple("carvers", SURFACE, 0, PRE_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222590_0_, p_222590_1_, p_222590_2_, p_222590_3_) -> {
      p_222590_1_.applyCarvers(p_222590_0_.getSeed(), p_222590_0_.getBiomeManager(), p_222590_3_, GenerationStage.Carving.AIR);
   });
   public static final ChunkStatus LIQUID_CARVERS = registerSimple("liquid_carvers", CARVERS, 0, POST_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222601_0_, p_222601_1_, p_222601_2_, p_222601_3_) -> {
      p_222601_1_.applyCarvers(p_222601_0_.getSeed(), p_222601_0_.getBiomeManager(), p_222601_3_, GenerationStage.Carving.LIQUID);
   });
   public static final ChunkStatus FEATURES = register("features", LIQUID_CARVERS, 8, POST_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222605_0_, p_222605_1_, p_222605_2_, p_222605_3_, p_222605_4_, p_222605_5_, p_222605_6_, p_222605_7_) -> {
      ChunkPrimer chunkprimer = (ChunkPrimer)p_222605_7_;
      chunkprimer.setLightEngine(p_222605_4_);
      if (!p_222605_7_.getStatus().isOrAfter(p_222605_0_)) {
         Heightmap.primeHeightmaps(p_222605_7_, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
         WorldGenRegion worldgenregion = new WorldGenRegion(p_222605_1_, p_222605_6_);
         p_222605_2_.applyBiomeDecoration(worldgenregion, p_222605_1_.structureFeatureManager().forWorldGenRegion(worldgenregion));
         chunkprimer.setStatus(p_222605_0_);
      }

      return CompletableFuture.completedFuture(Either.left(p_222605_7_));
   });
   public static final ChunkStatus LIGHT = register("light", FEATURES, 1, POST_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222604_0_, p_222604_1_, p_222604_2_, p_222604_3_, p_222604_4_, p_222604_5_, p_222604_6_, p_222604_7_) -> {
      return lightChunk(p_222604_0_, p_222604_4_, p_222604_7_);
   }, (p_223195_0_, p_223195_1_, p_223195_2_, p_223195_3_, p_223195_4_, p_223195_5_) -> {
      return lightChunk(p_223195_0_, p_223195_3_, p_223195_5_);
   });
   public static final ChunkStatus SPAWN = registerSimple("spawn", LIGHT, 0, POST_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222602_0_, p_222602_1_, p_222602_2_, p_222602_3_) -> {
      p_222602_1_.spawnOriginalMobs(new WorldGenRegion(p_222602_0_, p_222602_2_));
   });
   public static final ChunkStatus HEIGHTMAPS = registerSimple("heightmaps", SPAWN, 0, POST_FEATURES, ChunkStatus.Type.PROTOCHUNK, (p_222603_0_, p_222603_1_, p_222603_2_, p_222603_3_) -> {
   });
   public static final ChunkStatus FULL = register("full", HEIGHTMAPS, 0, POST_FEATURES, ChunkStatus.Type.LEVELCHUNK, (p_222598_0_, p_222598_1_, p_222598_2_, p_222598_3_, p_222598_4_, p_222598_5_, p_222598_6_, p_222598_7_) -> {
      return p_222598_5_.apply(p_222598_7_);
   }, (p_223205_0_, p_223205_1_, p_223205_2_, p_223205_3_, p_223205_4_, p_223205_5_) -> {
      return p_223205_4_.apply(p_223205_5_);
   });
   private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(FULL, FEATURES, LIQUID_CARVERS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS);
   private static final IntList RANGE_BY_STATUS = Util.make(new IntArrayList(getStatusList().size()), (p_223202_0_) -> {
      int i = 0;

      for(int j = getStatusList().size() - 1; j >= 0; --j) {
         while(i + 1 < STATUS_BY_RANGE.size() && j <= STATUS_BY_RANGE.get(i + 1).getIndex()) {
            ++i;
         }

         p_223202_0_.add(0, i);
      }

   });
   private final String name;
   private final int index;
   private final ChunkStatus parent;
   private final ChunkStatus.IGenerationWorker generationTask;
   private final ChunkStatus.ILoadingWorker loadingTask;
   private final int range;
   private final ChunkStatus.Type chunkType;
   private final EnumSet<Heightmap.Type> heightmapsAfter;

   private static CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> lightChunk(ChunkStatus pStatus, ServerWorldLightManager pLightEngine, IChunk pChunk) {
      boolean flag = isLighted(pStatus, pChunk);
      if (!pChunk.getStatus().isOrAfter(pStatus)) {
         ((ChunkPrimer)pChunk).setStatus(pStatus);
      }

      return pLightEngine.lightChunk(pChunk, flag).thenApply(Either::left);
   }

   private static ChunkStatus registerSimple(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, EnumSet<Heightmap.Type> pHeightmaps, ChunkStatus.Type pType, ChunkStatus.ISelectiveWorker pGenerationWorker) {
      return register(pKey, pParent, pTaskRange, pHeightmaps, pType, pGenerationWorker);
   }

   private static ChunkStatus register(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, EnumSet<Heightmap.Type> pHeightmaps, ChunkStatus.Type pType, ChunkStatus.IGenerationWorker pGenerationWorker) {
      return register(pKey, pParent, pTaskRange, pHeightmaps, pType, pGenerationWorker, PASSTHROUGH_LOAD_TASK);
   }

   private static ChunkStatus register(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, EnumSet<Heightmap.Type> pHeightmaps, ChunkStatus.Type pType, ChunkStatus.IGenerationWorker pGenerationWorker, ChunkStatus.ILoadingWorker pLoadingWorker) {
      return Registry.register(Registry.CHUNK_STATUS, pKey, new ChunkStatus(pKey, pParent, pTaskRange, pHeightmaps, pType, pGenerationWorker, pLoadingWorker));
   }

   public static List<ChunkStatus> getStatusList() {
      List<ChunkStatus> list = Lists.newArrayList();

      ChunkStatus chunkstatus;
      for(chunkstatus = FULL; chunkstatus.getParent() != chunkstatus; chunkstatus = chunkstatus.getParent()) {
         list.add(chunkstatus);
      }

      list.add(chunkstatus);
      Collections.reverse(list);
      return list;
   }

   private static boolean isLighted(ChunkStatus pStatus, IChunk pChunk) {
      return pChunk.getStatus().isOrAfter(pStatus) && pChunk.isLightCorrect();
   }

   public static ChunkStatus getStatus(int p_222581_0_) {
      if (p_222581_0_ >= STATUS_BY_RANGE.size()) {
         return EMPTY;
      } else {
         return p_222581_0_ < 0 ? FULL : STATUS_BY_RANGE.get(p_222581_0_);
      }
   }

   public static int maxDistance() {
      return STATUS_BY_RANGE.size();
   }

   public static int getDistance(ChunkStatus pStatus) {
      return RANGE_BY_STATUS.getInt(pStatus.getIndex());
   }

   public ChunkStatus(String pName, @Nullable ChunkStatus pParent, int pRange, EnumSet<Heightmap.Type> pHeightmapsAfter, ChunkStatus.Type pChunkType, ChunkStatus.IGenerationWorker pGenerationTask, ChunkStatus.ILoadingWorker pLoadingTask) {
      this.name = pName;
      this.parent = pParent == null ? this : pParent;
      this.generationTask = pGenerationTask;
      this.loadingTask = pLoadingTask;
      this.range = pRange;
      this.chunkType = pChunkType;
      this.heightmapsAfter = pHeightmapsAfter;
      this.index = pParent == null ? 0 : pParent.getIndex() + 1;
   }

   public int getIndex() {
      return this.index;
   }

   public String getName() {
      return this.name;
   }

   public ChunkStatus getParent() {
      return this.parent;
   }

   public CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> generate(ServerWorld p_223198_1_, ChunkGenerator p_223198_2_, TemplateManager p_223198_3_, ServerWorldLightManager p_223198_4_, Function<IChunk, CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>>> p_223198_5_, List<IChunk> p_223198_6_) {
      return this.generationTask.doWork(this, p_223198_1_, p_223198_2_, p_223198_3_, p_223198_4_, p_223198_5_, p_223198_6_, p_223198_6_.get(p_223198_6_.size() / 2));
   }

   public CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> load(ServerWorld pLevel, TemplateManager pTemplateManager, ServerWorldLightManager pLightEngine, Function<IChunk, CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>>> pLoadingFunction, IChunk pLoadingChunk) {
      return this.loadingTask.doWork(this, pLevel, pTemplateManager, pLightEngine, pLoadingFunction, pLoadingChunk);
   }

   /**
    * Distance in chunks between the edge of the center chunk and the edge of the chunk region needed for the task. The
    * task will only affect the center chunk, only reading from the chunks in the margin.
    */
   public int getRange() {
      return this.range;
   }

   public ChunkStatus.Type getChunkType() {
      return this.chunkType;
   }

   public static ChunkStatus byName(String pLocation) {
      return Registry.CHUNK_STATUS.get(ResourceLocation.tryParse(pLocation));
   }

   public EnumSet<Heightmap.Type> heightmapsAfter() {
      return this.heightmapsAfter;
   }

   public boolean isOrAfter(ChunkStatus pStatus) {
      return this.getIndex() >= pStatus.getIndex();
   }

   public String toString() {
      return Registry.CHUNK_STATUS.getKey(this).toString();
   }

   interface IGenerationWorker {
      CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> doWork(ChunkStatus p_doWork_1_, ServerWorld p_doWork_2_, ChunkGenerator p_doWork_3_, TemplateManager p_doWork_4_, ServerWorldLightManager p_doWork_5_, Function<IChunk, CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>>> p_doWork_6_, List<IChunk> p_doWork_7_, IChunk p_doWork_8_);
   }

   interface ILoadingWorker {
      CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> doWork(ChunkStatus p_doWork_1_, ServerWorld p_doWork_2_, TemplateManager p_doWork_3_, ServerWorldLightManager p_doWork_4_, Function<IChunk, CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>>> p_doWork_5_, IChunk p_doWork_6_);
   }

   interface ISelectiveWorker extends ChunkStatus.IGenerationWorker {
      default CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> doWork(ChunkStatus p_doWork_1_, ServerWorld p_doWork_2_, ChunkGenerator p_doWork_3_, TemplateManager p_doWork_4_, ServerWorldLightManager p_doWork_5_, Function<IChunk, CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>>> p_doWork_6_, List<IChunk> p_doWork_7_, IChunk p_doWork_8_) {
         if (!p_doWork_8_.getStatus().isOrAfter(p_doWork_1_)) {
            this.doWork(p_doWork_2_, p_doWork_3_, p_doWork_7_, p_doWork_8_);
            if (p_doWork_8_ instanceof ChunkPrimer) {
               ((ChunkPrimer)p_doWork_8_).setStatus(p_doWork_1_);
            }
         }

         return CompletableFuture.completedFuture(Either.left(p_doWork_8_));
      }

      void doWork(ServerWorld p_doWork_1_, ChunkGenerator p_doWork_2_, List<IChunk> p_doWork_3_, IChunk p_doWork_4_);
   }

   public static enum Type {
      PROTOCHUNK,
      LEVELCHUNK;
   }
}
