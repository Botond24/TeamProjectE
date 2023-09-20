package net.minecraft.world.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.merchant.IReputationTracking;
import net.minecraft.entity.merchant.IReputationType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SAnimateBlockBreakPacket;
import net.minecraft.network.play.server.SBlockActionPacket;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.network.play.server.SSpawnMovingSoundEffectPacket;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.network.play.server.SWorldSpawnChangedPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CSVWriter;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.GameRules;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapIdTracker;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerWorld extends World implements ISeedReader, net.minecraftforge.common.extensions.IForgeWorldServer {
   public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
   private static final Logger LOGGER = LogManager.getLogger();
   private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectLinkedOpenHashMap<>();
   private final Map<UUID, Entity> entitiesByUuid = Maps.newHashMap();
   private final Queue<Entity> toAddAfterTick = Queues.newArrayDeque();
   private final List<ServerPlayerEntity> players = Lists.newArrayList();
   private final ServerChunkProvider chunkSource;
   boolean tickingEntities;
   private final MinecraftServer server;
   private final IServerWorldInfo serverLevelData;
   public boolean noSave;
   private boolean allPlayersSleeping;
   private int emptyTime;
   private final Teleporter portalForcer;
   private final ServerTickList<Block> blockTicks = new ServerTickList<>(this, (p_205341_0_) -> {
      return p_205341_0_ == null || p_205341_0_.defaultBlockState().isAir();
   }, Registry.BLOCK::getKey, this::tickBlock);
   private final ServerTickList<Fluid> liquidTicks = new ServerTickList<>(this, (p_205774_0_) -> {
      return p_205774_0_ == null || p_205774_0_ == Fluids.EMPTY;
   }, Registry.FLUID::getKey, this::tickLiquid);
   private final Set<PathNavigator> navigations = Sets.newHashSet();
   protected final RaidManager raids;
   private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet<>();
   private boolean handlingTick;
   private final List<ISpecialSpawner> customSpawners;
   @Nullable
   private final DragonFightManager dragonFight;
   private final StructureManager structureFeatureManager;
   private final boolean tickTime;
   private net.minecraftforge.common.util.WorldCapabilityData capabilityData;
   private final it.unimi.dsi.fastutil.ints.Int2ObjectMap<net.minecraftforge.entity.PartEntity<?>> partEntities = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();

   public ServerWorld(MinecraftServer p_i241885_1_, Executor p_i241885_2_, SaveFormat.LevelSave p_i241885_3_, IServerWorldInfo p_i241885_4_, RegistryKey<World> p_i241885_5_, DimensionType p_i241885_6_, IChunkStatusListener p_i241885_7_, ChunkGenerator p_i241885_8_, boolean p_i241885_9_, long p_i241885_10_, List<ISpecialSpawner> p_i241885_12_, boolean p_i241885_13_) {
      super(p_i241885_4_, p_i241885_5_, p_i241885_6_, p_i241885_1_::getProfiler, false, p_i241885_9_, p_i241885_10_);
      this.tickTime = p_i241885_13_;
      this.server = p_i241885_1_;
      this.customSpawners = p_i241885_12_;
      this.serverLevelData = p_i241885_4_;
      this.chunkSource = new ServerChunkProvider(this, p_i241885_3_, p_i241885_1_.getFixerUpper(), p_i241885_1_.getStructureManager(), p_i241885_2_, p_i241885_8_, p_i241885_1_.getPlayerList().getViewDistance(), p_i241885_1_.forceSynchronousWrites(), p_i241885_7_, () -> {
         return p_i241885_1_.overworld().getDataStorage();
      });
      this.portalForcer = new Teleporter(this);
      this.updateSkyBrightness();
      this.prepareWeather();
      this.getWorldBorder().setAbsoluteMaxSize(p_i241885_1_.getAbsoluteMaxWorldSize());
      this.raids = this.getDataStorage().computeIfAbsent(() -> {
         return new RaidManager(this);
      }, RaidManager.getFileId(this.dimensionType()));
      if (!p_i241885_1_.isSingleplayer()) {
         p_i241885_4_.setGameType(p_i241885_1_.getDefaultGameType());
      }

      this.structureFeatureManager = new StructureManager(this, p_i241885_1_.getWorldData().worldGenSettings());
      if (this.dimensionType().createDragonFight()) {
         this.dragonFight = new DragonFightManager(this, p_i241885_1_.getWorldData().worldGenSettings().seed(), p_i241885_1_.getWorldData().endDragonFightData());
      } else {
         this.dragonFight = null;
      }
      this.initCapabilities();
   }

   public void setWeatherParameters(int pClearTime, int pWeatherTime, boolean pIsRaining, boolean pIsThundering) {
      this.serverLevelData.setClearWeatherTime(pClearTime);
      this.serverLevelData.setRainTime(pWeatherTime);
      this.serverLevelData.setThunderTime(pWeatherTime);
      this.serverLevelData.setRaining(pIsRaining);
      this.serverLevelData.setThundering(pIsThundering);
   }

   public Biome getUncachedNoiseBiome(int pX, int pY, int pZ) {
      return this.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(pX, pY, pZ);
   }

   public StructureManager structureFeatureManager() {
      return this.structureFeatureManager;
   }

   /**
    * Runs a single tick for the world
    */
   public void tick(BooleanSupplier pHasTimeLeft) {
      IProfiler iprofiler = this.getProfiler();
      this.handlingTick = true;
      iprofiler.push("world border");
      this.getWorldBorder().tick();
      iprofiler.popPush("weather");
      boolean flag = this.isRaining();
      if (this.dimensionType().hasSkyLight()) {
         if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
            int i = this.serverLevelData.getClearWeatherTime();
            int j = this.serverLevelData.getThunderTime();
            int k = this.serverLevelData.getRainTime();
            boolean flag1 = this.levelData.isThundering();
            boolean flag2 = this.levelData.isRaining();
            if (i > 0) {
               --i;
               j = flag1 ? 0 : 1;
               k = flag2 ? 0 : 1;
               flag1 = false;
               flag2 = false;
            } else {
               if (j > 0) {
                  --j;
                  if (j == 0) {
                     flag1 = !flag1;
                  }
               } else if (flag1) {
                  j = this.random.nextInt(12000) + 3600;
               } else {
                  j = this.random.nextInt(168000) + 12000;
               }

               if (k > 0) {
                  --k;
                  if (k == 0) {
                     flag2 = !flag2;
                  }
               } else if (flag2) {
                  k = this.random.nextInt(12000) + 12000;
               } else {
                  k = this.random.nextInt(168000) + 12000;
               }
            }

            this.serverLevelData.setThunderTime(j);
            this.serverLevelData.setRainTime(k);
            this.serverLevelData.setClearWeatherTime(i);
            this.serverLevelData.setThundering(flag1);
            this.serverLevelData.setRaining(flag2);
         }

         this.oThunderLevel = this.thunderLevel;
         if (this.levelData.isThundering()) {
            this.thunderLevel = (float)((double)this.thunderLevel + 0.01D);
         } else {
            this.thunderLevel = (float)((double)this.thunderLevel - 0.01D);
         }

         this.thunderLevel = MathHelper.clamp(this.thunderLevel, 0.0F, 1.0F);
         this.oRainLevel = this.rainLevel;
         if (this.levelData.isRaining()) {
            this.rainLevel = (float)((double)this.rainLevel + 0.01D);
         } else {
            this.rainLevel = (float)((double)this.rainLevel - 0.01D);
         }

         this.rainLevel = MathHelper.clamp(this.rainLevel, 0.0F, 1.0F);
      }

      if (this.oRainLevel != this.rainLevel) {
         this.server.getPlayerList().broadcastAll(new SChangeGameStatePacket(SChangeGameStatePacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
      }

      if (this.oThunderLevel != this.thunderLevel) {
         this.server.getPlayerList().broadcastAll(new SChangeGameStatePacket(SChangeGameStatePacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
      }

      /* The function in use here has been replaced in order to only send the weather info to players in the correct dimension,
       * rather than to all players on the server. This is what causes the client-side rain, as the
       * client believes that it has started raining locally, rather than in another dimension.
       */
      if (flag != this.isRaining()) {
         if (flag) {
            this.server.getPlayerList().broadcastAll(new SChangeGameStatePacket(SChangeGameStatePacket.STOP_RAINING, 0.0F), this.dimension());
         } else {
            this.server.getPlayerList().broadcastAll(new SChangeGameStatePacket(SChangeGameStatePacket.START_RAINING, 0.0F), this.dimension());
         }

         this.server.getPlayerList().broadcastAll(new SChangeGameStatePacket(SChangeGameStatePacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
         this.server.getPlayerList().broadcastAll(new SChangeGameStatePacket(SChangeGameStatePacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
      }

      if (this.allPlayersSleeping && this.players.stream().noneMatch((p_241132_0_) -> {
         return !p_241132_0_.isSpectator() && !p_241132_0_.isSleepingLongEnough();
      })) {
         this.allPlayersSleeping = false;
         if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            long l = this.getDayTime() + 24000L;
            this.setDayTime(net.minecraftforge.event.ForgeEventFactory.onSleepFinished(this, l - l % 24000L, this.getDayTime()));
         }

         this.wakeUpAllPlayers();
         if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
            this.stopWeather();
         }
      }

      this.updateSkyBrightness();
      this.tickTime();
      iprofiler.popPush("chunkSource");
      this.getChunkSource().tick(pHasTimeLeft);
      iprofiler.popPush("tickPending");
      if (!this.isDebug()) {
         this.blockTicks.tick();
         this.liquidTicks.tick();
      }

      iprofiler.popPush("raid");
      this.raids.tick();
      iprofiler.popPush("blockEvents");
      this.runBlockEvents();
      this.handlingTick = false;
      iprofiler.popPush("entities");
      boolean flag3 = !this.players.isEmpty() || net.minecraftforge.common.world.ForgeChunkManager.hasForcedChunks(this); //Forge: Replace vanilla's has forced chunk check with forge's that checks both the vanilla and forge added ones
      if (flag3) {
         this.resetEmptyTime();
      }

      if (flag3 || this.emptyTime++ < 300) {
         if (this.dragonFight != null) {
            this.dragonFight.tick();
         }

         this.tickingEntities = true;
         ObjectIterator<Entry<Entity>> objectiterator = this.entitiesById.int2ObjectEntrySet().iterator();

         label164:
         while(true) {
            Entity entity1;
            while(true) {
               if (!objectiterator.hasNext()) {
                  this.tickingEntities = false;

                  Entity entity;
                  while((entity = this.toAddAfterTick.poll()) != null) {
                     this.add(entity);
                  }

                  this.tickBlockEntities();
                  break label164;
               }

               Entry<Entity> entry = objectiterator.next();
               entity1 = entry.getValue();
               Entity entity2 = entity1.getVehicle();
               if (!this.server.isSpawningAnimals() && (entity1 instanceof AnimalEntity || entity1 instanceof WaterMobEntity)) {
                  entity1.remove();
               }

               if (!this.server.areNpcsEnabled() && entity1 instanceof INPC) {
                  entity1.remove();
               }

               iprofiler.push("checkDespawn");
               if (!entity1.removed) {
                  entity1.checkDespawn();
               }

               iprofiler.pop();
               if (entity2 == null) {
                  break;
               }

               if (entity2.removed || !entity2.hasPassenger(entity1)) {
                  entity1.stopRiding();
                  break;
               }
            }

            iprofiler.push("tick");
            if (!entity1.removed && !(entity1 instanceof net.minecraftforge.entity.PartEntity)) {
               this.guardEntityTick(this::tickNonPassenger, entity1);
            }

            iprofiler.pop();
            iprofiler.push("remove");
            if (entity1.removed) {
               this.removeFromChunk(entity1);
               objectiterator.remove();
               this.removeEntityComplete(entity1, entity1 instanceof ServerPlayerEntity); //Forge: Keep cap data until revive. Every other entity removes directly.
            }

            iprofiler.pop();
         }
      }

      iprofiler.pop();
   }

   protected void tickTime() {
      if (this.tickTime) {
         long i = this.levelData.getGameTime() + 1L;
         this.serverLevelData.setGameTime(i);
         this.serverLevelData.getScheduledEvents().tick(this.server, i);
         if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            this.setDayTime(this.levelData.getDayTime() + 1L);
         }

      }
   }

   public void setDayTime(long pTime) {
      this.serverLevelData.setDayTime(pTime);
   }

   public void tickCustomSpawners(boolean pSpawnHostiles, boolean pSpawnPassives) {
      for(ISpecialSpawner ispecialspawner : this.customSpawners) {
         ispecialspawner.tick(this, pSpawnHostiles, pSpawnPassives);
      }

   }

   private void wakeUpAllPlayers() {
      this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()).forEach((p_241131_0_) -> {
         p_241131_0_.stopSleepInBed(false, false);
      });
   }

   public void tickChunk(Chunk pChunk, int pRandomTickSpeed) {
      ChunkPos chunkpos = pChunk.getPos();
      boolean flag = this.isRaining();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      IProfiler iprofiler = this.getProfiler();
      iprofiler.push("thunder");
      if (flag && this.isThundering() && this.random.nextInt(100000) == 0) {
         BlockPos blockpos = this.findLightingTargetAround(this.getBlockRandomPos(i, 0, j, 15));
         if (this.isRainingAt(blockpos)) {
            DifficultyInstance difficultyinstance = this.getCurrentDifficultyAt(blockpos);
            boolean flag1 = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && this.random.nextDouble() < (double)difficultyinstance.getEffectiveDifficulty() * 0.01D;
            if (flag1) {
               SkeletonHorseEntity skeletonhorseentity = EntityType.SKELETON_HORSE.create(this);
               skeletonhorseentity.setTrap(true);
               skeletonhorseentity.setAge(0);
               skeletonhorseentity.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
               this.addFreshEntity(skeletonhorseentity);
            }

            LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(this);
            lightningboltentity.moveTo(Vector3d.atBottomCenterOf(blockpos));
            lightningboltentity.setVisualOnly(flag1);
            this.addFreshEntity(lightningboltentity);
         }
      }

      iprofiler.popPush("iceandsnow");
      if (this.random.nextInt(16) == 0) {
         BlockPos blockpos2 = this.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, this.getBlockRandomPos(i, 0, j, 15));
         BlockPos blockpos3 = blockpos2.below();
         Biome biome = this.getBiome(blockpos2);
         if (this.isAreaLoaded(blockpos2, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
         if (biome.shouldFreeze(this, blockpos3)) {
            this.setBlockAndUpdate(blockpos3, Blocks.ICE.defaultBlockState());
         }

         if (flag && biome.shouldSnow(this, blockpos2)) {
            this.setBlockAndUpdate(blockpos2, Blocks.SNOW.defaultBlockState());
         }

         if (flag && this.getBiome(blockpos3).getPrecipitation() == Biome.RainType.RAIN) {
            this.getBlockState(blockpos3).getBlock().handleRain(this, blockpos3);
         }
      }

      iprofiler.popPush("tickBlocks");
      if (pRandomTickSpeed > 0) {
         for(ChunkSection chunksection : pChunk.getSections()) {
            if (chunksection != Chunk.EMPTY_SECTION && chunksection.isRandomlyTicking()) {
               int k = chunksection.bottomBlockY();

               for(int l = 0; l < pRandomTickSpeed; ++l) {
                  BlockPos blockpos1 = this.getBlockRandomPos(i, k, j, 15);
                  iprofiler.push("randomTick");
                  BlockState blockstate = chunksection.getBlockState(blockpos1.getX() - i, blockpos1.getY() - k, blockpos1.getZ() - j);
                  if (blockstate.isRandomlyTicking()) {
                     blockstate.randomTick(this, blockpos1, this.random);
                  }

                  FluidState fluidstate = blockstate.getFluidState();
                  if (fluidstate.isRandomlyTicking()) {
                     fluidstate.randomTick(this, blockpos1, this.random);
                  }

                  iprofiler.pop();
               }
            }
         }
      }

      iprofiler.pop();
   }

   protected BlockPos findLightingTargetAround(BlockPos p_175736_1_) {
      BlockPos blockpos = this.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, p_175736_1_);
      AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockpos, new BlockPos(blockpos.getX(), this.getMaxBuildHeight(), blockpos.getZ()))).inflate(3.0D);
      List<LivingEntity> list = this.getEntitiesOfClass(LivingEntity.class, axisalignedbb, (p_241115_1_) -> {
         return p_241115_1_ != null && p_241115_1_.isAlive() && this.canSeeSky(p_241115_1_.blockPosition());
      });
      if (!list.isEmpty()) {
         return list.get(this.random.nextInt(list.size())).blockPosition();
      } else {
         if (blockpos.getY() == -1) {
            blockpos = blockpos.above(2);
         }

         return blockpos;
      }
   }

   public boolean isHandlingTick() {
      return this.handlingTick;
   }

   /**
    * Updates the flag that indicates whether or not all players in the world are sleeping.
    */
   public void updateSleepingPlayerList() {
      this.allPlayersSleeping = false;
      if (!this.players.isEmpty()) {
         int i = 0;
         int j = 0;

         for(ServerPlayerEntity serverplayerentity : this.players) {
            if (serverplayerentity.isSpectator()) {
               ++i;
            } else if (serverplayerentity.isSleeping()) {
               ++j;
            }
         }

         this.allPlayersSleeping = j > 0 && j >= this.players.size() - i;
      }

   }

   public ServerScoreboard getScoreboard() {
      return this.server.getScoreboard();
   }

   /**
    * Clears the current rain and thunder weather states.
    */
   private void stopWeather() {
      this.serverLevelData.setRainTime(0);
      this.serverLevelData.setRaining(false);
      this.serverLevelData.setThunderTime(0);
      this.serverLevelData.setThundering(false);
   }

   /**
    * Resets the updateEntityTick field to 0
    */
   public void resetEmptyTime() {
      this.emptyTime = 0;
   }

   private void tickLiquid(NextTickListEntry<Fluid> p_205339_1_) {
      FluidState fluidstate = this.getFluidState(p_205339_1_.pos);
      if (fluidstate.getType() == p_205339_1_.getType()) {
         fluidstate.tick(this, p_205339_1_.pos);
      }

   }

   private void tickBlock(NextTickListEntry<Block> p_205338_1_) {
      BlockState blockstate = this.getBlockState(p_205338_1_.pos);
      if (blockstate.is(p_205338_1_.getType())) {
         blockstate.tick(this, p_205338_1_.pos, this.random);
      }

   }

   public void tickNonPassenger(Entity p_217479_1_) {
      if (!(p_217479_1_ instanceof PlayerEntity) && !this.getChunkSource().isEntityTickingChunk(p_217479_1_)) {
         this.updateChunkPos(p_217479_1_);
      } else {
         p_217479_1_.setPosAndOldPos(p_217479_1_.getX(), p_217479_1_.getY(), p_217479_1_.getZ());
         p_217479_1_.yRotO = p_217479_1_.yRot;
         p_217479_1_.xRotO = p_217479_1_.xRot;
         if (p_217479_1_.inChunk) {
            ++p_217479_1_.tickCount;
            IProfiler iprofiler = this.getProfiler();
            iprofiler.push(() -> {
               return p_217479_1_.getType().getRegistryName() == null ? p_217479_1_.getType().toString() : p_217479_1_.getType().getRegistryName().toString();
            });
            iprofiler.incrementCounter("tickNonPassenger");
            if (p_217479_1_.canUpdate())
            p_217479_1_.tick();
            iprofiler.pop();
         }

         this.updateChunkPos(p_217479_1_);
         if (p_217479_1_.inChunk) {
            for(Entity entity : p_217479_1_.getPassengers()) {
               this.tickPassenger(p_217479_1_, entity);
            }
         }

      }
   }

   public void tickPassenger(Entity pRidingEntity, Entity pPassengerEntity) {
      if (!pPassengerEntity.removed && pPassengerEntity.getVehicle() == pRidingEntity) {
         if (pPassengerEntity instanceof PlayerEntity || this.getChunkSource().isEntityTickingChunk(pPassengerEntity)) {
            pPassengerEntity.setPosAndOldPos(pPassengerEntity.getX(), pPassengerEntity.getY(), pPassengerEntity.getZ());
            pPassengerEntity.yRotO = pPassengerEntity.yRot;
            pPassengerEntity.xRotO = pPassengerEntity.xRot;
            if (pPassengerEntity.inChunk) {
               ++pPassengerEntity.tickCount;
               IProfiler iprofiler = this.getProfiler();
               iprofiler.push(() -> {
                  return Registry.ENTITY_TYPE.getKey(pPassengerEntity.getType()).toString();
               });
               iprofiler.incrementCounter("tickPassenger");
               pPassengerEntity.rideTick();
               iprofiler.pop();
            }

            this.updateChunkPos(pPassengerEntity);
            if (pPassengerEntity.inChunk) {
               for(Entity entity : pPassengerEntity.getPassengers()) {
                  this.tickPassenger(pPassengerEntity, entity);
               }
            }

         }
      } else {
         pPassengerEntity.stopRiding();
      }
   }

   public void updateChunkPos(Entity p_217464_1_) {
      if (p_217464_1_.checkAndResetUpdateChunkPos()) {
         this.getProfiler().push("chunkCheck");
         int i = MathHelper.floor(p_217464_1_.getX() / 16.0D);
         int j = MathHelper.floor(p_217464_1_.getY() / 16.0D);
         int k = MathHelper.floor(p_217464_1_.getZ() / 16.0D);
         if (!p_217464_1_.inChunk || p_217464_1_.xChunk != i || p_217464_1_.yChunk != j || p_217464_1_.zChunk != k) {
            if (p_217464_1_.inChunk && this.hasChunk(p_217464_1_.xChunk, p_217464_1_.zChunk)) {
               this.getChunk(p_217464_1_.xChunk, p_217464_1_.zChunk).removeEntity(p_217464_1_, p_217464_1_.yChunk);
            }

            if (!p_217464_1_.checkAndResetForcedChunkAdditionFlag() && !this.hasChunk(i, k)) {
               if (p_217464_1_.inChunk) {
                  LOGGER.warn("Entity {} left loaded chunk area", (Object)p_217464_1_);
               }

               p_217464_1_.inChunk = false;
            } else {
               this.getChunk(i, k).addEntity(p_217464_1_);
            }
         }

         this.getProfiler().pop();
      }
   }

   public boolean mayInteract(PlayerEntity pPlayer, BlockPos pPos) {
      return !this.server.isUnderSpawnProtection(this, pPos, pPlayer) && this.getWorldBorder().isWithinBounds(pPos);
   }

   public void save(@Nullable IProgressUpdate pProgress, boolean pFlush, boolean pSkipSave) {
      ServerChunkProvider serverchunkprovider = this.getChunkSource();
      if (!pSkipSave) {
         if (pProgress != null) {
            pProgress.progressStartNoAbort(new TranslationTextComponent("menu.savingLevel"));
         }

         this.saveLevelData();
         if (pProgress != null) {
            pProgress.progressStage(new TranslationTextComponent("menu.savingChunks"));
         }

         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Save(this));
         serverchunkprovider.save(pFlush);
      }
   }

   /**
    * Saves the chunks to disk.
    */
   private void saveLevelData() {
      if (this.dragonFight != null) {
         this.server.getWorldData().setEndDragonFightData(this.dragonFight.saveData());
      }

      this.getChunkSource().getDataStorage().save();
   }

   public List<Entity> getEntities(@Nullable EntityType<?> p_217482_1_, Predicate<? super Entity> p_217482_2_) {
      List<Entity> list = Lists.newArrayList();
      ServerChunkProvider serverchunkprovider = this.getChunkSource();

      for(Entity entity : this.entitiesById.values()) {
         if ((p_217482_1_ == null || entity.getType() == p_217482_1_) && serverchunkprovider.hasChunk(MathHelper.floor(entity.getX()) >> 4, MathHelper.floor(entity.getZ()) >> 4) && p_217482_2_.test(entity)) {
            list.add(entity);
         }
      }

      return list;
   }

   public List<EnderDragonEntity> getDragons() {
      List<EnderDragonEntity> list = Lists.newArrayList();

      for(Entity entity : this.entitiesById.values()) {
         if (entity instanceof EnderDragonEntity && entity.isAlive()) {
            list.add((EnderDragonEntity)entity);
         }
      }

      return list;
   }

   public List<ServerPlayerEntity> getPlayers(Predicate<? super ServerPlayerEntity> pPredicate) {
      List<ServerPlayerEntity> list = Lists.newArrayList();

      for(ServerPlayerEntity serverplayerentity : this.players) {
         if (pPredicate.test(serverplayerentity)) {
            list.add(serverplayerentity);
         }
      }

      return list;
   }

   @Nullable
   public ServerPlayerEntity getRandomPlayer() {
      List<ServerPlayerEntity> list = this.getPlayers(LivingEntity::isAlive);
      return list.isEmpty() ? null : list.get(this.random.nextInt(list.size()));
   }

   public boolean addFreshEntity(Entity pEntity) {
      return this.addEntity(pEntity);
   }

   /**
    * Used for "unnatural" ways of entities appearing in the world, e.g. summon command, interdimensional teleports
    */
   public boolean addWithUUID(Entity pEntity) {
      return this.addEntity(pEntity);
   }

   public void addFromAnotherDimension(Entity p_217460_1_) {
      boolean flag = p_217460_1_.forcedLoading;
      p_217460_1_.forcedLoading = true;
      this.addWithUUID(p_217460_1_);
      p_217460_1_.forcedLoading = flag;
      this.updateChunkPos(p_217460_1_);
   }

   public void addDuringCommandTeleport(ServerPlayerEntity pPlayer) {
      this.addPlayer(pPlayer);
      this.updateChunkPos(pPlayer);
   }

   public void addDuringPortalTeleport(ServerPlayerEntity pPlayer) {
      this.addPlayer(pPlayer);
      this.updateChunkPos(pPlayer);
   }

   public void addNewPlayer(ServerPlayerEntity pPlayer) {
      this.addPlayer(pPlayer);
   }

   public void addRespawnedPlayer(ServerPlayerEntity pPlayer) {
      this.addPlayer(pPlayer);
   }

   private void addPlayer(ServerPlayerEntity pPlayer) {
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(pPlayer, this))) return;
      Entity entity = this.entitiesByUuid.get(pPlayer.getUUID());
      if (entity != null) {
         LOGGER.warn("Force-added player with duplicate UUID {}", (Object)pPlayer.getUUID().toString());
         entity.unRide();
         this.removePlayerImmediately((ServerPlayerEntity)entity);
      }

      this.players.add(pPlayer);
      this.updateSleepingPlayerList();
      IChunk ichunk = this.getChunk(MathHelper.floor(pPlayer.getX() / 16.0D), MathHelper.floor(pPlayer.getZ() / 16.0D), ChunkStatus.FULL, true);
      if (ichunk instanceof Chunk) {
         ichunk.addEntity(pPlayer);
      }

      this.add(pPlayer);
   }

   /**
    * Called when an entity is spawned in the world. This includes players.
    */
   private boolean addEntity(Entity pEntity) {
      if (pEntity.removed) {
         LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityType.getKey(pEntity.getType()));
         return false;
      } else if (this.isUUIDUsed(pEntity)) {
         return false;
      } else {
         if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(pEntity, this))) return false;
         IChunk ichunk = this.getChunk(MathHelper.floor(pEntity.getX() / 16.0D), MathHelper.floor(pEntity.getZ() / 16.0D), ChunkStatus.FULL, pEntity.forcedLoading);
         if (!(ichunk instanceof Chunk)) {
            return false;
         } else {
            ichunk.addEntity(pEntity);
            this.add(pEntity);
            return true;
         }
      }
   }

   public boolean loadFromChunk(Entity p_217440_1_) {
      if (this.isUUIDUsed(p_217440_1_)) {
         return false;
      } else {
         if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(p_217440_1_, this))) return false;
         this.add(p_217440_1_);
         return true;
      }
   }

   private boolean isUUIDUsed(Entity p_217478_1_) {
      UUID uuid = p_217478_1_.getUUID();
      Entity entity = this.findAddedOrPendingEntity(uuid);
      if (entity == null) {
         return false;
      } else {
         LOGGER.warn("Trying to add entity with duplicated UUID {}. Existing {}#{}, new: {}#{}", uuid, EntityType.getKey(entity.getType()), entity.getId(), EntityType.getKey(p_217478_1_.getType()), p_217478_1_.getId());
         return true;
      }
   }

   @Nullable
   private Entity findAddedOrPendingEntity(UUID p_242105_1_) {
      Entity entity = this.entitiesByUuid.get(p_242105_1_);
      if (entity != null) {
         return entity;
      } else {
         if (this.tickingEntities) {
            for(Entity entity1 : this.toAddAfterTick) {
               if (entity1.getUUID().equals(p_242105_1_)) {
                  return entity1;
               }
            }
         }

         return null;
      }
   }

   /**
    * Attempts to summon an entity and it's passangers. They will only be summoned if all entities are unique and not
    * already in queue to be summoned.
    */
   public boolean tryAddFreshEntityWithPassengers(Entity pEntity) {
      if (pEntity.getSelfAndPassengers().anyMatch(this::isUUIDUsed)) {
         return false;
      } else {
         this.addFreshEntityWithPassengers(pEntity);
         return true;
      }
   }

   public void unload(Chunk pChunk) {
      this.blockEntitiesToUnload.addAll(pChunk.getBlockEntities().values());
      ClassInheritanceMultiMap<Entity>[] aclassinheritancemultimap = pChunk.getEntitySections();
      int i = aclassinheritancemultimap.length;

      for(int j = 0; j < i; ++j) {
         for(Entity entity : aclassinheritancemultimap[j]) {
            if (!(entity instanceof ServerPlayerEntity)) {
               if (this.tickingEntities) {
                  throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Removing entity while ticking!"));
               }

               this.entitiesById.remove(entity.getId());
               this.onEntityRemoved(entity);
            }
         }
      }

   }

   @Deprecated //Forge: Use removeEntityComplete(entity,boolean)
   public void onEntityRemoved(Entity p_217484_1_) {
      removeEntityComplete(p_217484_1_, false);
   }
   public void removeEntityComplete(Entity p_217484_1_, boolean keepData) {
      if (p_217484_1_.isMultipartEntity()) {
         for(net.minecraftforge.entity.PartEntity<?> enderdragonpartentity : p_217484_1_.getParts()) {
            enderdragonpartentity.remove(keepData);
            this.partEntities.remove(enderdragonpartentity.getId());
         }
      }

      this.entitiesByUuid.remove(p_217484_1_.getUUID());
      this.getChunkSource().removeEntity(p_217484_1_);
      if (p_217484_1_ instanceof ServerPlayerEntity) {
         ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)p_217484_1_;
         this.players.remove(serverplayerentity);
      }

      this.getScoreboard().entityRemoved(p_217484_1_);
      if (p_217484_1_ instanceof MobEntity) {
         this.navigations.remove(((MobEntity)p_217484_1_).getNavigation());
      }

      p_217484_1_.onRemovedFromWorld();
      p_217484_1_.remove(keepData);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityLeaveWorldEvent(p_217484_1_, this));
   }

   private void add(Entity p_217465_1_) {
      if (this.tickingEntities) {
         this.toAddAfterTick.add(p_217465_1_);
      } else {
         this.entitiesById.put(p_217465_1_.getId(), p_217465_1_);
         if (p_217465_1_.isMultipartEntity()) {
            for(net.minecraftforge.entity.PartEntity<?> enderdragonpartentity : p_217465_1_.getParts()) {
               this.entitiesById.put(enderdragonpartentity.getId(), enderdragonpartentity);
               this.partEntities.put(enderdragonpartentity.getId(), enderdragonpartentity);
            }
         }

         this.entitiesByUuid.put(p_217465_1_.getUUID(), p_217465_1_);
         this.getChunkSource().addEntity(p_217465_1_);
         if (p_217465_1_ instanceof MobEntity) {
            this.navigations.add(((MobEntity)p_217465_1_).getNavigation());
         }
      }

      p_217465_1_.onAddedToWorld();
   }

   public void despawn(Entity p_217467_1_) {
      removeEntity(p_217467_1_, false);
   }
   public void removeEntity(Entity p_217467_1_, boolean keepData) {
      if (this.tickingEntities) {
         throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Removing entity while ticking!"));
      } else {
         this.removeFromChunk(p_217467_1_);
         this.entitiesById.remove(p_217467_1_.getId());
         this.removeEntityComplete(p_217467_1_, keepData);
      }
   }

   private void removeFromChunk(Entity p_217454_1_) {
      IChunk ichunk = this.getChunk(p_217454_1_.xChunk, p_217454_1_.zChunk, ChunkStatus.FULL, false);
      if (ichunk instanceof Chunk) {
         ((Chunk)ichunk).removeEntity(p_217454_1_);
      }

   }

   public void removePlayerImmediately(ServerPlayerEntity p_217434_1_) {
      removePlayer(p_217434_1_, false);
   }
   public void removePlayer(ServerPlayerEntity p_217434_1_, boolean keepData) {
      p_217434_1_.remove(keepData);
      this.removeEntity(p_217434_1_, keepData);
      this.updateSleepingPlayerList();
   }

   public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {
      for(ServerPlayerEntity serverplayerentity : this.server.getPlayerList().getPlayers()) {
         if (serverplayerentity != null && serverplayerentity.level == this && serverplayerentity.getId() != pBreakerId) {
            double d0 = (double)pPos.getX() - serverplayerentity.getX();
            double d1 = (double)pPos.getY() - serverplayerentity.getY();
            double d2 = (double)pPos.getZ() - serverplayerentity.getZ();
            if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
               serverplayerentity.connection.send(new SAnimateBlockBreakPacket(pBreakerId, pPos, pProgress));
            }
         }
      }

   }

   public void playSound(@Nullable PlayerEntity pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
      net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pPlayer, pSound, pCategory, pVolume, pPitch);
      if (event.isCanceled() || event.getSound() == null) return;
      pSound = event.getSound();
      pCategory = event.getCategory();
      pVolume = event.getVolume();
      this.server.getPlayerList().broadcast(pPlayer, pX, pY, pZ, pVolume > 1.0F ? (double)(16.0F * pVolume) : 16.0D, this.dimension(), new SPlaySoundEffectPacket(pSound, pCategory, pX, pY, pZ, pVolume, pPitch));
   }

   public void playSound(@Nullable PlayerEntity pPlayer, Entity pEntity, SoundEvent pEvent, SoundCategory pCategory, float pVolume, float pPitch) {
      net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pPlayer, pEvent, pCategory, pVolume, pPitch);
      if (event.isCanceled() || event.getSound() == null) return;
      pEvent = event.getSound();
      pCategory = event.getCategory();
      pVolume = event.getVolume();
      this.server.getPlayerList().broadcast(pPlayer, pEntity.getX(), pEntity.getY(), pEntity.getZ(), pVolume > 1.0F ? (double)(16.0F * pVolume) : 16.0D, this.dimension(), new SSpawnMovingSoundEffectPacket(pEvent, pCategory, pEntity, pVolume, pPitch));
   }

   public void globalLevelEvent(int pId, BlockPos pPos, int pData) {
      this.server.getPlayerList().broadcastAll(new SPlaySoundEventPacket(pId, pPos, pData, true));
   }

   public void levelEvent(@Nullable PlayerEntity pPlayer, int pType, BlockPos pPos, int pData) {
      this.server.getPlayerList().broadcast(pPlayer, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), 64.0D, this.dimension(), new SPlaySoundEventPacket(pType, pPos, pData, false));
   }

   /**
    * Flags are as in setBlockState
    */
   public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
      this.getChunkSource().blockChanged(pPos);
      VoxelShape voxelshape = pOldState.getCollisionShape(this, pPos);
      VoxelShape voxelshape1 = pNewState.getCollisionShape(this, pPos);
      if (VoxelShapes.joinIsNotEmpty(voxelshape, voxelshape1, IBooleanFunction.NOT_SAME)) {
         for(PathNavigator pathnavigator : this.navigations) {
            if (!pathnavigator.hasDelayedRecomputation()) {
               pathnavigator.recomputePath(pPos);
            }
         }

      }
   }

   /**
    * sends a Packet 38 (Entity Status) to all tracked players of that entity
    */
   public void broadcastEntityEvent(Entity pEntity, byte pState) {
      this.getChunkSource().broadcastAndSend(pEntity, new SEntityStatusPacket(pEntity, pState));
   }

   /**
    * Gets the world's chunk provider
    */
   public ServerChunkProvider getChunkSource() {
      return this.chunkSource;
   }

   public Explosion explode(@Nullable Entity pExploder, @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pContext, double pX, double pY, double pZ, float pSize, boolean pCausesFire, Explosion.Mode pMode) {
      Explosion explosion = new Explosion(this, pExploder, pDamageSource, pContext, pX, pY, pZ, pSize, pCausesFire, pMode);
      if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this, explosion)) return explosion;
      explosion.explode();
      explosion.finalizeExplosion(false);
      if (pMode == Explosion.Mode.NONE) {
         explosion.clearToBlow();
      }

      for(ServerPlayerEntity serverplayerentity : this.players) {
         if (serverplayerentity.distanceToSqr(pX, pY, pZ) < 4096.0D) {
            serverplayerentity.connection.send(new SExplosionPacket(pX, pY, pZ, pSize, explosion.getToBlow(), explosion.getHitPlayers().get(serverplayerentity)));
         }
      }

      return explosion;
   }

   public void blockEvent(BlockPos pPos, Block pBlock, int pEventID, int pEventParam) {
      this.blockEvents.add(new BlockEventData(pPos, pBlock, pEventID, pEventParam));
   }

   private void runBlockEvents() {
      while(!this.blockEvents.isEmpty()) {
         BlockEventData blockeventdata = this.blockEvents.removeFirst();
         if (this.doBlockEvent(blockeventdata)) {
            this.server.getPlayerList().broadcast((PlayerEntity)null, (double)blockeventdata.getPos().getX(), (double)blockeventdata.getPos().getY(), (double)blockeventdata.getPos().getZ(), 64.0D, this.dimension(), new SBlockActionPacket(blockeventdata.getPos(), blockeventdata.getBlock(), blockeventdata.getParamA(), blockeventdata.getParamB()));
         }
      }

   }

   private boolean doBlockEvent(BlockEventData pEvent) {
      BlockState blockstate = this.getBlockState(pEvent.getPos());
      return blockstate.is(pEvent.getBlock()) ? blockstate.triggerEvent(this, pEvent.getPos(), pEvent.getParamA(), pEvent.getParamB()) : false;
   }

   public ServerTickList<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public ServerTickList<Fluid> getLiquidTicks() {
      return this.liquidTicks;
   }

   @Nonnull
   public MinecraftServer getServer() {
      return this.server;
   }

   public Teleporter getPortalForcer() {
      return this.portalForcer;
   }

   public TemplateManager getStructureManager() {
      return this.server.getStructureManager();
   }

   public <T extends IParticleData> int sendParticles(T pType, double pPosX, double pPosY, double pPosZ, int pParticleCount, double pXOffset, double pYOffset, double pZOffset, double pSpeed) {
      SSpawnParticlePacket sspawnparticlepacket = new SSpawnParticlePacket(pType, false, pPosX, pPosY, pPosZ, (float)pXOffset, (float)pYOffset, (float)pZOffset, (float)pSpeed, pParticleCount);
      int i = 0;

      for(int j = 0; j < this.players.size(); ++j) {
         ServerPlayerEntity serverplayerentity = this.players.get(j);
         if (this.sendParticles(serverplayerentity, false, pPosX, pPosY, pPosZ, sspawnparticlepacket)) {
            ++i;
         }
      }

      return i;
   }

   public <T extends IParticleData> boolean sendParticles(ServerPlayerEntity pPlayer, T pType, boolean pLongDistance, double pPosX, double pPosY, double pPosZ, int pParticleCount, double pXOffset, double pYOffset, double pZOffset, double pSpeed) {
      IPacket<?> ipacket = new SSpawnParticlePacket(pType, pLongDistance, pPosX, pPosY, pPosZ, (float)pXOffset, (float)pYOffset, (float)pZOffset, (float)pSpeed, pParticleCount);
      return this.sendParticles(pPlayer, pLongDistance, pPosX, pPosY, pPosZ, ipacket);
   }

   private boolean sendParticles(ServerPlayerEntity pPlayer, boolean pLongDistance, double pPosX, double pPosY, double pPosZ, IPacket<?> pPacket) {
      if (pPlayer.getLevel() != this) {
         return false;
      } else {
         BlockPos blockpos = pPlayer.blockPosition();
         if (blockpos.closerThan(new Vector3d(pPosX, pPosY, pPosZ), pLongDistance ? 512.0D : 32.0D)) {
            pPlayer.connection.send(pPacket);
            return true;
         } else {
            return false;
         }
      }
   }

   /**
    * Returns the Entity with the given ID, or null if it doesn't exist in this World.
    */
   @Nullable
   public Entity getEntity(int pId) {
      return this.entitiesById.get(pId);
   }

   @Nullable
   public Entity getEntity(UUID pUniqueId) {
      return this.entitiesByUuid.get(pUniqueId);
   }

   @Nullable
   public BlockPos findNearestMapFeature(Structure<?> pStructure, BlockPos pPos, int pRadius, boolean pSkipExistingChunks) {
      return !this.server.getWorldData().worldGenSettings().generateFeatures() ? null : this.getChunkSource().getGenerator().findNearestMapFeature(this, pStructure, pPos, pRadius, pSkipExistingChunks);
   }

   @Nullable
   public BlockPos findNearestBiome(Biome pBiome, BlockPos pPos, int pRadius, int pIncrement) {
      return this.getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(pPos.getX(), pPos.getY(), pPos.getZ(), pRadius, pIncrement, (p_242102_1_) -> {
         return p_242102_1_ == pBiome;
      }, this.random, true);
   }

   public RecipeManager getRecipeManager() {
      return this.server.getRecipeManager();
   }

   public ITagCollectionSupplier getTagManager() {
      return this.server.getTags();
   }

   public boolean noSave() {
      return this.noSave;
   }

   public DynamicRegistries registryAccess() {
      return this.server.registryAccess();
   }

   public DimensionSavedDataManager getDataStorage() {
      return this.getChunkSource().getDataStorage();
   }

   @Nullable
   public MapData getMapData(String pMapName) {
      return this.getServer().overworld().getDataStorage().get(() -> {
         return new MapData(pMapName);
      }, pMapName);
   }

   public void setMapData(MapData p_217399_1_) {
      this.getServer().overworld().getDataStorage().set(p_217399_1_);
   }

   public int getFreeMapId() {
      return this.getServer().overworld().getDataStorage().computeIfAbsent(MapIdTracker::new, "idcounts").getFreeAuxValueForMap();
   }

   public void setDefaultSpawnPos(BlockPos pPos, float pAngle) {
      ChunkPos chunkpos = new ChunkPos(new BlockPos(this.levelData.getXSpawn(), 0, this.levelData.getZSpawn()));
      this.levelData.setSpawn(pPos, pAngle);
      this.getChunkSource().removeRegionTicket(TicketType.START, chunkpos, 11, Unit.INSTANCE);
      this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(pPos), 11, Unit.INSTANCE);
      this.getServer().getPlayerList().broadcastAll(new SWorldSpawnChangedPacket(pPos, pAngle));
   }

   public BlockPos getSharedSpawnPos() {
      BlockPos blockpos = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
      if (!this.getWorldBorder().isWithinBounds(blockpos)) {
         blockpos = this.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
      }

      return blockpos;
   }

   public float getSharedSpawnAngle() {
      return this.levelData.getSpawnAngle();
   }

   public LongSet getForcedChunks() {
      ForcedChunksSaveData forcedchunkssavedata = this.getDataStorage().get(ForcedChunksSaveData::new, "chunks");
      return (LongSet)(forcedchunkssavedata != null ? LongSets.unmodifiable(forcedchunkssavedata.getChunks()) : LongSets.EMPTY_SET);
   }

   public boolean setChunkForced(int pChunkX, int pChunkZ, boolean pAdd) {
      ForcedChunksSaveData forcedchunkssavedata = this.getDataStorage().computeIfAbsent(ForcedChunksSaveData::new, "chunks");
      ChunkPos chunkpos = new ChunkPos(pChunkX, pChunkZ);
      long i = chunkpos.toLong();
      boolean flag;
      if (pAdd) {
         flag = forcedchunkssavedata.getChunks().add(i);
         if (flag) {
            this.getChunk(pChunkX, pChunkZ);
         }
      } else {
         flag = forcedchunkssavedata.getChunks().remove(i);
      }

      forcedchunkssavedata.setDirty(flag);
      if (flag) {
         this.getChunkSource().updateChunkForced(chunkpos, pAdd);
      }

      return flag;
   }

   public List<ServerPlayerEntity> players() {
      return this.players;
   }

   public void onBlockStateChange(BlockPos pPos, BlockState pBlockState, BlockState pNewState) {
      Optional<PointOfInterestType> optional = PointOfInterestType.forState(pBlockState);
      Optional<PointOfInterestType> optional1 = PointOfInterestType.forState(pNewState);
      if (!Objects.equals(optional, optional1)) {
         BlockPos blockpos = pPos.immutable();
         optional.ifPresent((p_241130_2_) -> {
            this.getServer().execute(() -> {
               this.getPoiManager().remove(blockpos);
               DebugPacketSender.sendPoiRemovedPacket(this, blockpos);
            });
         });
         optional1.ifPresent((p_217476_2_) -> {
            this.getServer().execute(() -> {
               this.getPoiManager().add(blockpos, p_217476_2_);
               DebugPacketSender.sendPoiAddedPacket(this, blockpos);
            });
         });
      }
   }

   public PointOfInterestManager getPoiManager() {
      return this.getChunkSource().getPoiManager();
   }

   public boolean isVillage(BlockPos pPos) {
      return this.isCloseToVillage(pPos, 1);
   }

   public boolean isVillage(SectionPos pPos) {
      return this.isVillage(pPos.center());
   }

   public boolean isCloseToVillage(BlockPos pPos, int pSections) {
      if (pSections > 6) {
         return false;
      } else {
         return this.sectionsToVillage(SectionPos.of(pPos)) <= pSections;
      }
   }

   public int sectionsToVillage(SectionPos pPos) {
      return this.getPoiManager().sectionsToVillage(pPos);
   }

   public RaidManager getRaids() {
      return this.raids;
   }

   @Nullable
   public Raid getRaidAt(BlockPos pPos) {
      return this.raids.getNearbyRaid(pPos, 9216);
   }

   public boolean isRaided(BlockPos pPos) {
      return this.getRaidAt(pPos) != null;
   }

   public void onReputationEvent(IReputationType pType, Entity pTarget, IReputationTracking pHost) {
      pHost.onReputationEventFrom(pType, pTarget);
   }

   public void saveDebugReport(Path pPath) throws IOException {
      ChunkManager chunkmanager = this.getChunkSource().chunkMap;

      try (Writer writer = Files.newBufferedWriter(pPath.resolve("stats.txt"))) {
         writer.write(String.format("spawning_chunks: %d\n", chunkmanager.getDistanceManager().getNaturalSpawnChunkCount()));
         WorldEntitySpawner.EntityDensityManager worldentityspawner$entitydensitymanager = this.getChunkSource().getLastSpawnState();
         if (worldentityspawner$entitydensitymanager != null) {
            for(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<EntityClassification> entry : worldentityspawner$entitydensitymanager.getMobCategoryCounts().object2IntEntrySet()) {
               writer.write(String.format("spawn_count.%s: %d\n", entry.getKey().getName(), entry.getIntValue()));
            }
         }

         writer.write(String.format("entities: %d\n", this.entitiesById.size()));
         writer.write(String.format("block_entities: %d\n", this.blockEntityList.size()));
         writer.write(String.format("block_ticks: %d\n", this.getBlockTicks().size()));
         writer.write(String.format("fluid_ticks: %d\n", this.getLiquidTicks().size()));
         writer.write("distance_manager: " + chunkmanager.getDistanceManager().getDebugStatus() + "\n");
         writer.write(String.format("pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
      }

      CrashReport crashreport = new CrashReport("Level dump", new Exception("dummy"));
      this.fillReportDetails(crashreport);

      try (Writer writer1 = Files.newBufferedWriter(pPath.resolve("example_crash.txt"))) {
         writer1.write(crashreport.getFriendlyReport());
      }

      Path path = pPath.resolve("chunks.csv");

      try (Writer writer2 = Files.newBufferedWriter(path)) {
         chunkmanager.dumpChunks(writer2);
      }

      Path path1 = pPath.resolve("entities.csv");

      try (Writer writer3 = Files.newBufferedWriter(path1)) {
         dumpEntities(writer3, this.entitiesById.values());
      }

      Path path2 = pPath.resolve("block_entities.csv");

      try (Writer writer4 = Files.newBufferedWriter(path2)) {
         this.dumpBlockEntities(writer4);
      }

   }

   private static void dumpEntities(Writer pWriter, Iterable<Entity> pEntities) throws IOException {
      CSVWriter csvwriter = CSVWriter.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("uuid").addColumn("type").addColumn("alive").addColumn("display_name").addColumn("custom_name").build(pWriter);

      for(Entity entity : pEntities) {
         ITextComponent itextcomponent = entity.getCustomName();
         ITextComponent itextcomponent1 = entity.getDisplayName();
         csvwriter.writeRow(entity.getX(), entity.getY(), entity.getZ(), entity.getUUID(), Registry.ENTITY_TYPE.getKey(entity.getType()), entity.isAlive(), itextcomponent1.getString(), itextcomponent != null ? itextcomponent.getString() : null);
      }

   }

   private void dumpBlockEntities(Writer p_225321_1_) throws IOException {
      CSVWriter csvwriter = CSVWriter.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("type").build(p_225321_1_);

      for(TileEntity tileentity : this.blockEntityList) {
         BlockPos blockpos = tileentity.getBlockPos();
         csvwriter.writeRow(blockpos.getX(), blockpos.getY(), blockpos.getZ(), Registry.BLOCK_ENTITY_TYPE.getKey(tileentity.getType()));
      }

   }

   @VisibleForTesting
   public void clearBlockEvents(MutableBoundingBox pBoundingBox) {
      this.blockEvents.removeIf((p_241118_1_) -> {
         return pBoundingBox.isInside(p_241118_1_.getPos());
      });
   }

   public void blockUpdated(BlockPos pPos, Block pBlock) {
      if (!this.isDebug()) {
         this.updateNeighborsAt(pPos, pBlock);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public float getShade(Direction pDirection, boolean pIsShade) {
      return 1.0F;
   }

   /**
    * Gets an unmodifiable iterator of all loaded entities in the world.
    */
   public Iterable<Entity> getAllEntities() {
      return Iterables.unmodifiableIterable(this.entitiesById.values());
   }

   public String toString() {
      return "ServerLevel[" + this.serverLevelData.getLevelName() + "]";
   }

   public boolean isFlat() {
      return this.server.getWorldData().worldGenSettings().isFlatWorld();
   }

   /**
    * gets the random world seed
    */
   public long getSeed() {
      return this.server.getWorldData().worldGenSettings().seed();
   }

   @Nullable
   public DragonFightManager dragonFight() {
      return this.dragonFight;
   }

   public Stream<? extends StructureStart<?>> startsForFeature(SectionPos pPos, Structure<?> pStructure) {
      return this.structureFeatureManager().startsForFeature(pPos, pStructure);
   }

   public ServerWorld getLevel() {
      return this;
   }

   @VisibleForTesting
   public String getWatchdogStats() {
      return String.format("players: %s, entities: %d [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s", this.players.size(), this.entitiesById.size(), getTypeCount(this.entitiesById.values(), (p_244527_0_) -> {
         return Registry.ENTITY_TYPE.getKey(p_244527_0_.getType());
      }), this.tickableBlockEntities.size(), getTypeCount(this.tickableBlockEntities, (p_244526_0_) -> {
         return Registry.BLOCK_ENTITY_TYPE.getKey(p_244526_0_.getType());
      }), this.getBlockTicks().size(), this.getLiquidTicks().size(), this.gatherChunkSourceStats());
   }

   private static <T> String getTypeCount(Collection<T> p_244524_0_, Function<T, ResourceLocation> p_244524_1_) {
      try {
         Object2IntOpenHashMap<ResourceLocation> object2intopenhashmap = new Object2IntOpenHashMap<>();

         for(T t : p_244524_0_) {
            ResourceLocation resourcelocation = p_244524_1_.apply(t);
            object2intopenhashmap.addTo(resourcelocation, 1);
         }

         return object2intopenhashmap.object2IntEntrySet().stream().sorted(Comparator.comparing(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ResourceLocation>::getIntValue).reversed()).limit(5L).map((p_244523_0_) -> {
            return p_244523_0_.getKey() + ":" + p_244523_0_.getIntValue();
         }).collect(Collectors.joining(","));
      } catch (Exception exception) {
         return "";
      }
   }

   public static void makeObsidianPlatform(ServerWorld pServerLevel) {
      BlockPos blockpos = END_SPAWN_POINT;
      int i = blockpos.getX();
      int j = blockpos.getY() - 2;
      int k = blockpos.getZ();
      BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach((p_244430_1_) -> {
         pServerLevel.setBlockAndUpdate(p_244430_1_, Blocks.AIR.defaultBlockState());
      });
      BlockPos.betweenClosed(i - 2, j, k - 2, i + 2, j, k + 2).forEach((p_241122_1_) -> {
         pServerLevel.setBlockAndUpdate(p_241122_1_, Blocks.OBSIDIAN.defaultBlockState());
      });
   }

   protected void initCapabilities() {
      this.gatherCapabilities();
      capabilityData = this.getDataStorage().computeIfAbsent(() -> new net.minecraftforge.common.util.WorldCapabilityData(getCapabilities()), net.minecraftforge.common.util.WorldCapabilityData.ID);
      capabilityData.setCapabilities(getCapabilities());
   }

   public java.util.stream.Stream<Entity> getEntities() {
       return entitiesById.values().stream();
   }

   @Override
   public java.util.Collection<net.minecraftforge.entity.PartEntity<?>> getPartEntities() {
      return this.partEntities.values();
   }
}
