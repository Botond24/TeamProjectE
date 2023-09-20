package net.minecraft.client.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.EntityTickableSound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.particle.FireworkParticle;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.color.ColorCache;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColors;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientWorld extends World {
   private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectOpenHashMap<>();
   private final ClientPlayNetHandler connection;
   private final WorldRenderer levelRenderer;
   private final ClientWorld.ClientWorldInfo clientLevelData;
   private final DimensionRenderInfo effects;
   private final Minecraft minecraft = Minecraft.getInstance();
   private final List<AbstractClientPlayerEntity> players = Lists.newArrayList();
   private Scoreboard scoreboard = new Scoreboard();
   private final Map<String, MapData> mapData = Maps.newHashMap();
   private int skyFlashTime;
   private final Object2ObjectArrayMap<ColorResolver, ColorCache> tintCaches = Util.make(new Object2ObjectArrayMap<>(3), (p_228319_0_) -> {
      p_228319_0_.put(BiomeColors.GRASS_COLOR_RESOLVER, new ColorCache());
      p_228319_0_.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, new ColorCache());
      p_228319_0_.put(BiomeColors.WATER_COLOR_RESOLVER, new ColorCache());
   });
   private final ClientChunkProvider chunkSource;
   private final it.unimi.dsi.fastutil.ints.Int2ObjectMap<net.minecraftforge.entity.PartEntity<?>> partEntities = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();

   public ClientWorld(ClientPlayNetHandler p_i242067_1_, ClientWorld.ClientWorldInfo p_i242067_2_, RegistryKey<World> p_i242067_3_, DimensionType p_i242067_4_, int p_i242067_5_, Supplier<IProfiler> p_i242067_6_, WorldRenderer p_i242067_7_, boolean p_i242067_8_, long p_i242067_9_) {
      super(p_i242067_2_, p_i242067_3_, p_i242067_4_, p_i242067_6_, true, p_i242067_8_, p_i242067_9_);
      this.connection = p_i242067_1_;
      this.chunkSource = new ClientChunkProvider(this, p_i242067_5_);
      this.clientLevelData = p_i242067_2_;
      this.levelRenderer = p_i242067_7_;
      this.effects = DimensionRenderInfo.forType(p_i242067_4_);
      this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
      this.updateSkyBrightness();
      this.prepareWeather();
      this.gatherCapabilities();
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Load(this));
   }

   public DimensionRenderInfo effects() {
      return this.effects;
   }

   /**
    * Runs a single tick for the world
    */
   public void tick(BooleanSupplier pHasTimeLeft) {
      this.getWorldBorder().tick();
      this.tickTime();
      this.getProfiler().push("blocks");
      this.chunkSource.tick(pHasTimeLeft);
      this.getProfiler().pop();
   }

   private void tickTime() {
      this.setGameTime(this.levelData.getGameTime() + 1L);
      if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
         this.setDayTime(this.levelData.getDayTime() + 1L);
      }

   }

   public void setGameTime(long pTime) {
      this.clientLevelData.setGameTime(pTime);
   }

   /**
    * Sets the world time.
    */
   public void setDayTime(long pTime) {
      if (pTime < 0L) {
         pTime = -pTime;
         this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, (MinecraftServer)null);
      } else {
         this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, (MinecraftServer)null);
      }

      this.clientLevelData.setDayTime(pTime);
   }

   public Iterable<Entity> entitiesForRendering() {
      return this.entitiesById.values();
   }

   public void tickEntities() {
      IProfiler iprofiler = this.getProfiler();
      iprofiler.push("entities");
      ObjectIterator<Entry<Entity>> objectiterator = this.entitiesById.int2ObjectEntrySet().iterator();

      while(objectiterator.hasNext()) {
         Entry<Entity> entry = objectiterator.next();
         Entity entity = entry.getValue();
         if (!entity.isPassenger()) {
            iprofiler.push("tick");
            if (!entity.removed) {
               this.guardEntityTick(this::tickNonPassenger, entity);
            }

            iprofiler.pop();
            iprofiler.push("remove");
            if (entity.removed) {
               objectiterator.remove();
               this.onEntityRemoved(entity);
            }

            iprofiler.pop();
         }
      }

      this.tickBlockEntities();
      iprofiler.pop();
   }

   public void tickNonPassenger(Entity p_217418_1_) {
      if (!(p_217418_1_ instanceof PlayerEntity) && !this.getChunkSource().isEntityTickingChunk(p_217418_1_)) {
         this.updateChunkPos(p_217418_1_);
      } else {
         p_217418_1_.setPosAndOldPos(p_217418_1_.getX(), p_217418_1_.getY(), p_217418_1_.getZ());
         p_217418_1_.yRotO = p_217418_1_.yRot;
         p_217418_1_.xRotO = p_217418_1_.xRot;
         if (p_217418_1_.inChunk || p_217418_1_.isSpectator()) {
            ++p_217418_1_.tickCount;
            this.getProfiler().push(() -> {
               return Registry.ENTITY_TYPE.getKey(p_217418_1_.getType()).toString();
            });
            if (p_217418_1_.canUpdate())
            p_217418_1_.tick();
            this.getProfiler().pop();
         }

         this.updateChunkPos(p_217418_1_);
         if (p_217418_1_.inChunk) {
            for(Entity entity : p_217418_1_.getPassengers()) {
               this.tickPassenger(p_217418_1_, entity);
            }
         }

      }
   }

   public void tickPassenger(Entity pMount, Entity pRider) {
      if (!pRider.removed && pRider.getVehicle() == pMount) {
         if (pRider instanceof PlayerEntity || this.getChunkSource().isEntityTickingChunk(pRider)) {
            pRider.setPosAndOldPos(pRider.getX(), pRider.getY(), pRider.getZ());
            pRider.yRotO = pRider.yRot;
            pRider.xRotO = pRider.xRot;
            if (pRider.inChunk) {
               ++pRider.tickCount;
               pRider.rideTick();
            }

            this.updateChunkPos(pRider);
            if (pRider.inChunk) {
               for(Entity entity : pRider.getPassengers()) {
                  this.tickPassenger(pRider, entity);
               }
            }

         }
      } else {
         pRider.stopRiding();
      }
   }

   private void updateChunkPos(Entity p_217423_1_) {
      if (p_217423_1_.checkAndResetUpdateChunkPos()) {
         this.getProfiler().push("chunkCheck");
         int i = MathHelper.floor(p_217423_1_.getX() / 16.0D);
         int j = MathHelper.floor(p_217423_1_.getY() / 16.0D);
         int k = MathHelper.floor(p_217423_1_.getZ() / 16.0D);
         if (!p_217423_1_.inChunk || p_217423_1_.xChunk != i || p_217423_1_.yChunk != j || p_217423_1_.zChunk != k) {
            if (p_217423_1_.inChunk && this.hasChunk(p_217423_1_.xChunk, p_217423_1_.zChunk)) {
               this.getChunk(p_217423_1_.xChunk, p_217423_1_.zChunk).removeEntity(p_217423_1_, p_217423_1_.yChunk);
            }

            if (!p_217423_1_.checkAndResetForcedChunkAdditionFlag() && !this.hasChunk(i, k)) {
               if (p_217423_1_.inChunk) {
                  LOGGER.warn("Entity {} left loaded chunk area", (Object)p_217423_1_);
               }

               p_217423_1_.inChunk = false;
            } else {
               this.getChunk(i, k).addEntity(p_217423_1_);
            }
         }

         this.getProfiler().pop();
      }
   }

   public void unload(Chunk pChunk) {
      this.blockEntitiesToUnload.addAll(pChunk.getBlockEntities().values());
      this.chunkSource.getLightEngine().enableLightSources(pChunk.getPos(), false);
   }

   public void onChunkLoaded(int p_228323_1_, int p_228323_2_) {
      this.tintCaches.forEach((p_228316_2_, p_228316_3_) -> {
         p_228316_3_.invalidateForChunk(p_228323_1_, p_228323_2_);
      });
   }

   public void clearTintCaches() {
      this.tintCaches.forEach((p_228320_0_, p_228320_1_) -> {
         p_228320_1_.invalidateAll();
      });
   }

   public boolean hasChunk(int pChunkX, int pChunkZ) {
      return true;
   }

   public int getEntityCount() {
      return this.entitiesById.size();
   }

   public void addPlayer(int pPlayerId, AbstractClientPlayerEntity pPlayerEntity) {
      this.addEntity(pPlayerId, pPlayerEntity);
      this.players.add(pPlayerEntity);
   }

   public void putNonPlayerEntity(int pEntityId, Entity pEntityToSpawn) {
      this.addEntity(pEntityId, pEntityToSpawn);
   }

   private void addEntity(int pEntityId, Entity pEntityToSpawn) {
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(pEntityToSpawn, this))) return;
      this.removeEntity(pEntityId);
      this.entitiesById.put(pEntityId, pEntityToSpawn);
      this.getChunkSource().getChunk(MathHelper.floor(pEntityToSpawn.getX() / 16.0D), MathHelper.floor(pEntityToSpawn.getZ() / 16.0D), ChunkStatus.FULL, true).addEntity(pEntityToSpawn);
      pEntityToSpawn.onAddedToWorld();
      if (pEntityToSpawn.isMultipartEntity()) {
         for(net.minecraftforge.entity.PartEntity<?> part : pEntityToSpawn.getParts()) {
            this.partEntities.put(part.getId(), part);
         }
      }
   }

   public void removeEntity(int p_217413_1_) {
      Entity entity = this.entitiesById.remove(p_217413_1_);
      if (entity != null) {
         entity.remove();
         this.onEntityRemoved(entity);
      }

   }

   private void onEntityRemoved(Entity p_217414_1_) {
      p_217414_1_.unRide();
      if (p_217414_1_.inChunk) {
         this.getChunk(p_217414_1_.xChunk, p_217414_1_.zChunk).removeEntity(p_217414_1_);
      }

      this.players.remove(p_217414_1_);
      p_217414_1_.onRemovedFromWorld();
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityLeaveWorldEvent(p_217414_1_, this));
      if (p_217414_1_.isMultipartEntity()) {
         for(net.minecraftforge.entity.PartEntity<?> part : p_217414_1_.getParts()) {
            this.partEntities.remove(part.getId());
         }
      }
   }

   public void reAddEntitiesToChunk(Chunk p_217417_1_) {
      for(Entry<Entity> entry : this.entitiesById.int2ObjectEntrySet()) {
         Entity entity = entry.getValue();
         int i = MathHelper.floor(entity.getX() / 16.0D);
         int j = MathHelper.floor(entity.getZ() / 16.0D);
         if (i == p_217417_1_.getPos().x && j == p_217417_1_.getPos().z) {
            p_217417_1_.addEntity(entity);
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

   public void setKnownState(BlockPos pPos, BlockState pState) {
      this.setBlock(pPos, pState, 19);
   }

   /**
    * If on MP, sends a quitting packet.
    */
   public void disconnect() {
      this.connection.getConnection().disconnect(new TranslationTextComponent("multiplayer.status.quitting"));
   }

   public void animateTick(int pPosX, int pPosY, int pPosZ) {
      int i = 32;
      Random random = new Random();
      boolean flag = false;
      if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE) {
         for(ItemStack itemstack : this.minecraft.player.getHandSlots()) {
            if (itemstack.getItem() == Blocks.BARRIER.asItem()) {
               flag = true;
               break;
            }
         }
      }

      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int j = 0; j < 667; ++j) {
         this.doAnimateTick(pPosX, pPosY, pPosZ, 16, random, flag, blockpos$mutable);
         this.doAnimateTick(pPosX, pPosY, pPosZ, 32, random, flag, blockpos$mutable);
      }

   }

   public void doAnimateTick(int p_184153_1_, int p_184153_2_, int p_184153_3_, int p_184153_4_, Random p_184153_5_, boolean p_184153_6_, BlockPos.Mutable p_184153_7_) {
      int i = p_184153_1_ + this.random.nextInt(p_184153_4_) - this.random.nextInt(p_184153_4_);
      int j = p_184153_2_ + this.random.nextInt(p_184153_4_) - this.random.nextInt(p_184153_4_);
      int k = p_184153_3_ + this.random.nextInt(p_184153_4_) - this.random.nextInt(p_184153_4_);
      p_184153_7_.set(i, j, k);
      BlockState blockstate = this.getBlockState(p_184153_7_);
      blockstate.getBlock().animateTick(blockstate, this, p_184153_7_, p_184153_5_);
      FluidState fluidstate = this.getFluidState(p_184153_7_);
      if (!fluidstate.isEmpty()) {
         fluidstate.animateTick(this, p_184153_7_, p_184153_5_);
         IParticleData iparticledata = fluidstate.getDripParticle();
         if (iparticledata != null && this.random.nextInt(10) == 0) {
            boolean flag = blockstate.isFaceSturdy(this, p_184153_7_, Direction.DOWN);
            BlockPos blockpos = p_184153_7_.below();
            this.trySpawnDripParticles(blockpos, this.getBlockState(blockpos), iparticledata, flag);
         }
      }

      if (p_184153_6_ && blockstate.is(Blocks.BARRIER)) {
         this.addParticle(ParticleTypes.BARRIER, (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, 0.0D, 0.0D, 0.0D);
      }

      if (!blockstate.isCollisionShapeFullBlock(this, p_184153_7_)) {
         this.getBiome(p_184153_7_).getAmbientParticle().ifPresent((p_239135_2_) -> {
            if (p_239135_2_.canSpawn(this.random)) {
               this.addParticle(p_239135_2_.getOptions(), (double)p_184153_7_.getX() + this.random.nextDouble(), (double)p_184153_7_.getY() + this.random.nextDouble(), (double)p_184153_7_.getZ() + this.random.nextDouble(), 0.0D, 0.0D, 0.0D);
            }

         });
      }

   }

   private void trySpawnDripParticles(BlockPos pBlockPos, BlockState pBlockState, IParticleData pParticleData, boolean pShapeDownSolid) {
      if (pBlockState.getFluidState().isEmpty()) {
         VoxelShape voxelshape = pBlockState.getCollisionShape(this, pBlockPos);
         double d0 = voxelshape.max(Direction.Axis.Y);
         if (d0 < 1.0D) {
            if (pShapeDownSolid) {
               this.spawnFluidParticle((double)pBlockPos.getX(), (double)(pBlockPos.getX() + 1), (double)pBlockPos.getZ(), (double)(pBlockPos.getZ() + 1), (double)(pBlockPos.getY() + 1) - 0.05D, pParticleData);
            }
         } else if (!pBlockState.is(BlockTags.IMPERMEABLE)) {
            double d1 = voxelshape.min(Direction.Axis.Y);
            if (d1 > 0.0D) {
               this.spawnParticle(pBlockPos, pParticleData, voxelshape, (double)pBlockPos.getY() + d1 - 0.05D);
            } else {
               BlockPos blockpos = pBlockPos.below();
               BlockState blockstate = this.getBlockState(blockpos);
               VoxelShape voxelshape1 = blockstate.getCollisionShape(this, blockpos);
               double d2 = voxelshape1.max(Direction.Axis.Y);
               if (d2 < 1.0D && blockstate.getFluidState().isEmpty()) {
                  this.spawnParticle(pBlockPos, pParticleData, voxelshape, (double)pBlockPos.getY() - 0.05D);
               }
            }
         }

      }
   }

   private void spawnParticle(BlockPos pPos, IParticleData pParticleData, VoxelShape pVoxelShape, double pY) {
      this.spawnFluidParticle((double)pPos.getX() + pVoxelShape.min(Direction.Axis.X), (double)pPos.getX() + pVoxelShape.max(Direction.Axis.X), (double)pPos.getZ() + pVoxelShape.min(Direction.Axis.Z), (double)pPos.getZ() + pVoxelShape.max(Direction.Axis.Z), pY, pParticleData);
   }

   private void spawnFluidParticle(double pXStart, double pXEnd, double pZStart, double pZEnd, double pY, IParticleData pParticleData) {
      this.addParticle(pParticleData, MathHelper.lerp(this.random.nextDouble(), pXStart, pXEnd), pY, MathHelper.lerp(this.random.nextDouble(), pZStart, pZEnd), 0.0D, 0.0D, 0.0D);
   }

   public void removeAllPendingEntityRemovals() {
      ObjectIterator<Entry<Entity>> objectiterator = this.entitiesById.int2ObjectEntrySet().iterator();

      while(objectiterator.hasNext()) {
         Entry<Entity> entry = objectiterator.next();
         Entity entity = entry.getValue();
         if (entity.removed) {
            objectiterator.remove();
            this.onEntityRemoved(entity);
         }
      }

   }

   /**
    * Adds some basic stats of the world to the given crash report.
    */
   public CrashReportCategory fillReportDetails(CrashReport pReport) {
      CrashReportCategory crashreportcategory = super.fillReportDetails(pReport);
      crashreportcategory.setDetail("Server brand", () -> {
         return this.minecraft.player.getServerBrand();
      });
      crashreportcategory.setDetail("Server type", () -> {
         return this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
      });
      return crashreportcategory;
   }

   public void playSound(@Nullable PlayerEntity pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
      net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pPlayer, pSound, pCategory, pVolume, pPitch);
      if (event.isCanceled() || event.getSound() == null) return;
      pSound = event.getSound();
      pCategory = event.getCategory();
      pVolume = event.getVolume();
      if (pPlayer == this.minecraft.player) {
         this.playLocalSound(pX, pY, pZ, pSound, pCategory, pVolume, pPitch, false);
      }

   }

   public void playSound(@Nullable PlayerEntity pPlayer, Entity pEntity, SoundEvent pEvent, SoundCategory pCategory, float pVolume, float pPitch) {
      net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pPlayer, pEvent, pCategory, pVolume, pPitch);
      if (event.isCanceled() || event.getSound() == null) return;
      pEvent = event.getSound();
      pCategory = event.getCategory();
      pVolume = event.getVolume();
      if (pPlayer == this.minecraft.player) {
         this.minecraft.getSoundManager().play(new EntityTickableSound(pEvent, pCategory, pEntity));
      }

   }

   public void playLocalSound(BlockPos pPos, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
      this.playLocalSound((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, pSound, pCategory, pVolume, pPitch, pDistanceDelay);
   }

   public void playLocalSound(double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
      double d0 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(pX, pY, pZ);
      SimpleSound simplesound = new SimpleSound(pSound, pCategory, pVolume, pPitch, pX, pY, pZ);
      if (pDistanceDelay && d0 > 100.0D) {
         double d1 = Math.sqrt(d0) / 40.0D;
         this.minecraft.getSoundManager().playDelayed(simplesound, (int)(d1 * 20.0D));
      } else {
         this.minecraft.getSoundManager().play(simplesound);
      }

   }

   public void createFireworks(double pX, double pY, double pZ, double pMotionX, double pMotionY, double pMotionZ, @Nullable CompoundNBT pCompound) {
      this.minecraft.particleEngine.add(new FireworkParticle.Starter(this, pX, pY, pZ, pMotionX, pMotionY, pMotionZ, this.minecraft.particleEngine, pCompound));
   }

   public void sendPacketToServer(IPacket<?> pPacket) {
      this.connection.send(pPacket);
   }

   public RecipeManager getRecipeManager() {
      return this.connection.getRecipeManager();
   }

   public void setScoreboard(Scoreboard pScoreboard) {
      this.scoreboard = pScoreboard;
   }

   public ITickList<Block> getBlockTicks() {
      return EmptyTickList.empty();
   }

   public ITickList<Fluid> getLiquidTicks() {
      return EmptyTickList.empty();
   }

   /**
    * Gets the world's chunk provider
    */
   public ClientChunkProvider getChunkSource() {
      return this.chunkSource;
   }

   @Nullable
   public MapData getMapData(String pMapName) {
      return this.mapData.get(pMapName);
   }

   public void setMapData(MapData p_217399_1_) {
      this.mapData.put(p_217399_1_.getId(), p_217399_1_);
   }

   public int getFreeMapId() {
      return 0;
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public ITagCollectionSupplier getTagManager() {
      return this.connection.getTags();
   }

   public DynamicRegistries registryAccess() {
      return this.connection.registryAccess();
   }

   /**
    * Flags are as in setBlockState
    */
   public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
      this.levelRenderer.blockChanged(this, pPos, pOldState, pNewState, pFlags);
   }

   public void setBlocksDirty(BlockPos pBlockPos, BlockState pOldState, BlockState pNewState) {
      this.levelRenderer.setBlockDirty(pBlockPos, pOldState, pNewState);
   }

   public void setSectionDirtyWithNeighbors(int pSectionX, int pSectionY, int pSectionZ) {
      this.levelRenderer.setSectionDirtyWithNeighbors(pSectionX, pSectionY, pSectionZ);
   }

   public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {
      this.levelRenderer.destroyBlockProgress(pBreakerId, pPos, pProgress);
   }

   public void globalLevelEvent(int pId, BlockPos pPos, int pData) {
      this.levelRenderer.globalLevelEvent(pId, pPos, pData);
   }

   public void levelEvent(@Nullable PlayerEntity pPlayer, int pType, BlockPos pPos, int pData) {
      try {
         this.levelRenderer.levelEvent(pPlayer, pType, pPos, pData);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Playing level event");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Level event being played");
         crashreportcategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(pPos));
         crashreportcategory.setDetail("Event source", pPlayer);
         crashreportcategory.setDetail("Event type", pType);
         crashreportcategory.setDetail("Event data", pData);
         throw new ReportedException(crashreport);
      }
   }

   public void addParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter(), pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public void addParticle(IParticleData pParticleData, boolean pForceAlwaysRender, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter() || pForceAlwaysRender, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public void addAlwaysVisibleParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.levelRenderer.addParticle(pParticleData, false, true, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public void addAlwaysVisibleParticle(IParticleData pParticleData, boolean pIgnoreRange, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter() || pIgnoreRange, true, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public List<AbstractClientPlayerEntity> players() {
      return this.players;
   }

   public Biome getUncachedNoiseBiome(int pX, int pY, int pZ) {
      return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
   }

   public float getSkyDarken(float pPartialTicks) {
      float f = this.getTimeOfDay(pPartialTicks);
      float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      f1 = 1.0F - f1;
      f1 = (float)((double)f1 * (1.0D - (double)(this.getRainLevel(pPartialTicks) * 5.0F) / 16.0D));
      f1 = (float)((double)f1 * (1.0D - (double)(this.getThunderLevel(pPartialTicks) * 5.0F) / 16.0D));
      return f1 * 0.8F + 0.2F;
   }

   public Vector3d getSkyColor(BlockPos p_228318_1_, float p_228318_2_) {
      float f = this.getTimeOfDay(p_228318_2_);
      float f1 = MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      Biome biome = this.getBiome(p_228318_1_);
      int i = biome.getSkyColor();
      float f2 = (float)(i >> 16 & 255) / 255.0F;
      float f3 = (float)(i >> 8 & 255) / 255.0F;
      float f4 = (float)(i & 255) / 255.0F;
      f2 = f2 * f1;
      f3 = f3 * f1;
      f4 = f4 * f1;
      float f5 = this.getRainLevel(p_228318_2_);
      if (f5 > 0.0F) {
         float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
         float f7 = 1.0F - f5 * 0.75F;
         f2 = f2 * f7 + f6 * (1.0F - f7);
         f3 = f3 * f7 + f6 * (1.0F - f7);
         f4 = f4 * f7 + f6 * (1.0F - f7);
      }

      float f9 = this.getThunderLevel(p_228318_2_);
      if (f9 > 0.0F) {
         float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
         float f8 = 1.0F - f9 * 0.75F;
         f2 = f2 * f8 + f10 * (1.0F - f8);
         f3 = f3 * f8 + f10 * (1.0F - f8);
         f4 = f4 * f8 + f10 * (1.0F - f8);
      }

      if (this.skyFlashTime > 0) {
         float f11 = (float)this.skyFlashTime - p_228318_2_;
         if (f11 > 1.0F) {
            f11 = 1.0F;
         }

         f11 = f11 * 0.45F;
         f2 = f2 * (1.0F - f11) + 0.8F * f11;
         f3 = f3 * (1.0F - f11) + 0.8F * f11;
         f4 = f4 * (1.0F - f11) + 1.0F * f11;
      }

      return new Vector3d((double)f2, (double)f3, (double)f4);
   }

   public Vector3d getCloudColor(float pPartialTicks) {
      float f = this.getTimeOfDay(pPartialTicks);
      float f1 = MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      float f2 = 1.0F;
      float f3 = 1.0F;
      float f4 = 1.0F;
      float f5 = this.getRainLevel(pPartialTicks);
      if (f5 > 0.0F) {
         float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
         float f7 = 1.0F - f5 * 0.95F;
         f2 = f2 * f7 + f6 * (1.0F - f7);
         f3 = f3 * f7 + f6 * (1.0F - f7);
         f4 = f4 * f7 + f6 * (1.0F - f7);
      }

      f2 = f2 * (f1 * 0.9F + 0.1F);
      f3 = f3 * (f1 * 0.9F + 0.1F);
      f4 = f4 * (f1 * 0.85F + 0.15F);
      float f9 = this.getThunderLevel(pPartialTicks);
      if (f9 > 0.0F) {
         float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
         float f8 = 1.0F - f9 * 0.95F;
         f2 = f2 * f8 + f10 * (1.0F - f8);
         f3 = f3 * f8 + f10 * (1.0F - f8);
         f4 = f4 * f8 + f10 * (1.0F - f8);
      }

      return new Vector3d((double)f2, (double)f3, (double)f4);
   }

   public float getStarBrightness(float pPartialTicks) {
      float f = this.getTimeOfDay(pPartialTicks);
      float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      return f1 * f1 * 0.5F;
   }

   public int getSkyFlashTime() {
      return this.skyFlashTime;
   }

   public void setSkyFlashTime(int pTimeFlash) {
      this.skyFlashTime = pTimeFlash;
   }

   public float getShade(Direction pDirection, boolean pIsShade) {
      boolean flag = this.effects().constantAmbientLight();
      if (!pIsShade) {
         return flag ? 0.9F : 1.0F;
      } else {
         switch(pDirection) {
         case DOWN:
            return flag ? 0.9F : 0.5F;
         case UP:
            return flag ? 0.9F : 1.0F;
         case NORTH:
         case SOUTH:
            return 0.8F;
         case WEST:
         case EAST:
            return 0.6F;
         default:
            return 1.0F;
         }
      }
   }

   public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
      ColorCache colorcache = this.tintCaches.get(pColorResolver);
      return colorcache.getColor(pBlockPos, () -> {
         return this.calculateBlockTint(pBlockPos, pColorResolver);
      });
   }

   public int calculateBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
      int i = Minecraft.getInstance().options.biomeBlendRadius;
      if (i == 0) {
         return pColorResolver.getColor(this.getBiome(pBlockPos), (double)pBlockPos.getX(), (double)pBlockPos.getZ());
      } else {
         int j = (i * 2 + 1) * (i * 2 + 1);
         int k = 0;
         int l = 0;
         int i1 = 0;
         CubeCoordinateIterator cubecoordinateiterator = new CubeCoordinateIterator(pBlockPos.getX() - i, pBlockPos.getY(), pBlockPos.getZ() - i, pBlockPos.getX() + i, pBlockPos.getY(), pBlockPos.getZ() + i);

         int j1;
         for(BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(); cubecoordinateiterator.advance(); i1 += j1 & 255) {
            blockpos$mutable.set(cubecoordinateiterator.nextX(), cubecoordinateiterator.nextY(), cubecoordinateiterator.nextZ());
            j1 = pColorResolver.getColor(this.getBiome(blockpos$mutable), (double)blockpos$mutable.getX(), (double)blockpos$mutable.getZ());
            k += (j1 & 16711680) >> 16;
            l += (j1 & '\uff00') >> 8;
         }

         return (k / j & 255) << 16 | (l / j & 255) << 8 | i1 / j & 255;
      }
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

   public void setDefaultSpawnPos(BlockPos pSpawnPos, float p_239136_2_) {
      this.levelData.setSpawn(pSpawnPos, p_239136_2_);
   }

   public String toString() {
      return "ClientLevel";
   }

   /**
    * Returns the world's WorldInfo object
    */
   public ClientWorld.ClientWorldInfo getLevelData() {
      return this.clientLevelData;
   }

   @OnlyIn(Dist.CLIENT)
   public static class ClientWorldInfo implements ISpawnWorldInfo {
      private final boolean hardcore;
      private final GameRules gameRules;
      private final boolean isFlat;
      private int xSpawn;
      private int ySpawn;
      private int zSpawn;
      private float spawnAngle;
      private long gameTime;
      private long dayTime;
      private boolean raining;
      private Difficulty difficulty;
      private boolean difficultyLocked;

      public ClientWorldInfo(Difficulty p_i232338_1_, boolean p_i232338_2_, boolean p_i232338_3_) {
         this.difficulty = p_i232338_1_;
         this.hardcore = p_i232338_2_;
         this.isFlat = p_i232338_3_;
         this.gameRules = new GameRules();
      }

      /**
       * Returns the x spawn position
       */
      public int getXSpawn() {
         return this.xSpawn;
      }

      /**
       * Return the Y axis spawning point of the player.
       */
      public int getYSpawn() {
         return this.ySpawn;
      }

      /**
       * Returns the z spawn position
       */
      public int getZSpawn() {
         return this.zSpawn;
      }

      public float getSpawnAngle() {
         return this.spawnAngle;
      }

      public long getGameTime() {
         return this.gameTime;
      }

      /**
       * Get current world time
       */
      public long getDayTime() {
         return this.dayTime;
      }

      /**
       * Set the x spawn position to the passed in value
       */
      public void setXSpawn(int pX) {
         this.xSpawn = pX;
      }

      /**
       * Sets the y spawn position
       */
      public void setYSpawn(int pY) {
         this.ySpawn = pY;
      }

      /**
       * Set the z spawn position to the passed in value
       */
      public void setZSpawn(int pZ) {
         this.zSpawn = pZ;
      }

      public void setSpawnAngle(float pAngle) {
         this.spawnAngle = pAngle;
      }

      public void setGameTime(long pTime) {
         this.gameTime = pTime;
      }

      public void setDayTime(long pTime) {
         this.dayTime = pTime;
      }

      public void setSpawn(BlockPos pSpawnPoint, float pAngle) {
         this.xSpawn = pSpawnPoint.getX();
         this.ySpawn = pSpawnPoint.getY();
         this.zSpawn = pSpawnPoint.getZ();
         this.spawnAngle = pAngle;
      }

      /**
       * Returns true if it is thundering, false otherwise.
       */
      public boolean isThundering() {
         return false;
      }

      /**
       * Returns true if it is raining, false otherwise.
       */
      public boolean isRaining() {
         return this.raining;
      }

      /**
       * Sets whether it is raining or not.
       */
      public void setRaining(boolean pIsRaining) {
         this.raining = pIsRaining;
      }

      /**
       * Returns true if hardcore mode is enabled, otherwise false
       */
      public boolean isHardcore() {
         return this.hardcore;
      }

      /**
       * Gets the GameRules class Instance.
       */
      public GameRules getGameRules() {
         return this.gameRules;
      }

      public Difficulty getDifficulty() {
         return this.difficulty;
      }

      public boolean isDifficultyLocked() {
         return this.difficultyLocked;
      }

      public void fillCrashReportCategory(CrashReportCategory p_85118_1_) {
         ISpawnWorldInfo.super.fillCrashReportCategory(p_85118_1_);
      }

      public void setDifficulty(Difficulty pDifficulty) {
         net.minecraftforge.common.ForgeHooks.onDifficultyChange(pDifficulty, this.difficulty);
         this.difficulty = pDifficulty;
      }

      public void setDifficultyLocked(boolean pDifficultyLocked) {
         this.difficultyLocked = pDifficultyLocked;
      }

      public double getHorizonHeight() {
         return this.isFlat ? 0.0D : 63.0D;
      }

      public double getClearColorScale() {
         return this.isFlat ? 1.0D : 0.03125D;
      }
   }

   @Override
   public java.util.Collection<net.minecraftforge.entity.PartEntity<?>> getPartEntities() {
      return this.partEntities.values();
   }
}
