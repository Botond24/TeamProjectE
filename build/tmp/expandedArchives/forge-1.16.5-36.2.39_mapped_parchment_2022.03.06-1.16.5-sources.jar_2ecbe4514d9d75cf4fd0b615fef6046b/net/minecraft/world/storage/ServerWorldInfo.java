package net.minecraft.world.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.command.TimerCallbackManager;
import net.minecraft.command.TimerCallbackSerializers;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.UUIDCodec;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldGenSettingsExport;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerWorldInfo implements IServerWorldInfo, IServerConfiguration {
   private static final Logger LOGGER = LogManager.getLogger();
   private WorldSettings settings;
   private final DimensionGeneratorSettings worldGenSettings;
   private final Lifecycle worldGenSettingsLifecycle;
   private int xSpawn;
   private int ySpawn;
   private int zSpawn;
   private float spawnAngle;
   private long gameTime;
   private long dayTime;
   @Nullable
   private final DataFixer fixerUpper;
   private final int playerDataVersion;
   private boolean upgradedPlayerTag;
   @Nullable
   private CompoundNBT loadedPlayerTag;
   private final int version;
   private int clearWeatherTime;
   private boolean raining;
   private int rainTime;
   private boolean thundering;
   private int thunderTime;
   private boolean initialized;
   private boolean difficultyLocked;
   private WorldBorder.Serializer worldBorder;
   private CompoundNBT endDragonFightData;
   @Nullable
   private CompoundNBT customBossEvents;
   private int wanderingTraderSpawnDelay;
   private int wanderingTraderSpawnChance;
   @Nullable
   private UUID wanderingTraderId;
   private final Set<String> knownServerBrands;
   private boolean wasModded;
   private final TimerCallbackManager<MinecraftServer> scheduledEvents;

   private ServerWorldInfo(@Nullable DataFixer p_i242043_1_, int p_i242043_2_, @Nullable CompoundNBT p_i242043_3_, boolean p_i242043_4_, int p_i242043_5_, int p_i242043_6_, int p_i242043_7_, float p_i242043_8_, long p_i242043_9_, long p_i242043_11_, int p_i242043_13_, int p_i242043_14_, int p_i242043_15_, boolean p_i242043_16_, int p_i242043_17_, boolean p_i242043_18_, boolean p_i242043_19_, boolean p_i242043_20_, WorldBorder.Serializer p_i242043_21_, int p_i242043_22_, int p_i242043_23_, @Nullable UUID p_i242043_24_, LinkedHashSet<String> p_i242043_25_, TimerCallbackManager<MinecraftServer> p_i242043_26_, @Nullable CompoundNBT p_i242043_27_, CompoundNBT p_i242043_28_, WorldSettings p_i242043_29_, DimensionGeneratorSettings p_i242043_30_, Lifecycle p_i242043_31_) {
      this.fixerUpper = p_i242043_1_;
      this.wasModded = p_i242043_4_;
      this.xSpawn = p_i242043_5_;
      this.ySpawn = p_i242043_6_;
      this.zSpawn = p_i242043_7_;
      this.spawnAngle = p_i242043_8_;
      this.gameTime = p_i242043_9_;
      this.dayTime = p_i242043_11_;
      this.version = p_i242043_13_;
      this.clearWeatherTime = p_i242043_14_;
      this.rainTime = p_i242043_15_;
      this.raining = p_i242043_16_;
      this.thunderTime = p_i242043_17_;
      this.thundering = p_i242043_18_;
      this.initialized = p_i242043_19_;
      this.difficultyLocked = p_i242043_20_;
      this.worldBorder = p_i242043_21_;
      this.wanderingTraderSpawnDelay = p_i242043_22_;
      this.wanderingTraderSpawnChance = p_i242043_23_;
      this.wanderingTraderId = p_i242043_24_;
      this.knownServerBrands = p_i242043_25_;
      this.loadedPlayerTag = p_i242043_3_;
      this.playerDataVersion = p_i242043_2_;
      this.scheduledEvents = p_i242043_26_;
      this.customBossEvents = p_i242043_27_;
      this.endDragonFightData = p_i242043_28_;
      this.settings = p_i242043_29_;
      this.worldGenSettings = p_i242043_30_;
      this.worldGenSettingsLifecycle = p_i242043_31_;
   }

   public ServerWorldInfo(WorldSettings p_i232158_1_, DimensionGeneratorSettings p_i232158_2_, Lifecycle p_i232158_3_) {
      this((DataFixer)null, SharedConstants.getCurrentVersion().getWorldVersion(), (CompoundNBT)null, false, 0, 0, 0, 0.0F, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_SETTINGS, 0, 0, (UUID)null, Sets.newLinkedHashSet(), new TimerCallbackManager<>(TimerCallbackSerializers.SERVER_CALLBACKS), (CompoundNBT)null, new CompoundNBT(), p_i232158_1_.copy(), p_i232158_2_, p_i232158_3_);
   }

   public static ServerWorldInfo parse(Dynamic<INBT> pDynamic, DataFixer pDataFixer, int pVersion, @Nullable CompoundNBT pPlayerNBT, WorldSettings pLevelSettings, VersionData pVersionData, DimensionGeneratorSettings pGeneratorSettings, Lifecycle pLifecycle) {
      long i = pDynamic.get("Time").asLong(0L);
      CompoundNBT compoundnbt = (CompoundNBT)pDynamic.get("DragonFight").result().map(Dynamic::getValue).orElseGet(() -> {
         return pDynamic.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap().getValue();
      });
      return new ServerWorldInfo(pDataFixer, pVersion, pPlayerNBT, pDynamic.get("WasModded").asBoolean(false), pDynamic.get("SpawnX").asInt(0), pDynamic.get("SpawnY").asInt(0), pDynamic.get("SpawnZ").asInt(0), pDynamic.get("SpawnAngle").asFloat(0.0F), i, pDynamic.get("DayTime").asLong(i), pVersionData.levelDataVersion(), pDynamic.get("clearWeatherTime").asInt(0), pDynamic.get("rainTime").asInt(0), pDynamic.get("raining").asBoolean(false), pDynamic.get("thunderTime").asInt(0), pDynamic.get("thundering").asBoolean(false), pDynamic.get("initialized").asBoolean(true), pDynamic.get("DifficultyLocked").asBoolean(false), WorldBorder.Serializer.read(pDynamic, WorldBorder.DEFAULT_SETTINGS), pDynamic.get("WanderingTraderSpawnDelay").asInt(0), pDynamic.get("WanderingTraderSpawnChance").asInt(0), pDynamic.get("WanderingTraderId").read(UUIDCodec.CODEC).result().orElse((UUID)null), pDynamic.get("ServerBrands").asStream().flatMap((p_237368_0_) -> {
         return Util.toStream(p_237368_0_.asString().result());
      }).collect(Collectors.toCollection(Sets::newLinkedHashSet)), new TimerCallbackManager<>(TimerCallbackSerializers.SERVER_CALLBACKS, pDynamic.get("ScheduledEvents").asStream()), (CompoundNBT)pDynamic.get("CustomBossEvents").orElseEmptyMap().getValue(), compoundnbt, pLevelSettings, pGeneratorSettings, pLifecycle);
   }

   public CompoundNBT createTag(DynamicRegistries pRegistries, @Nullable CompoundNBT pHostPlayerNBT) {
      this.updatePlayerTag();
      if (pHostPlayerNBT == null) {
         pHostPlayerNBT = this.loadedPlayerTag;
      }

      CompoundNBT compoundnbt = new CompoundNBT();
      this.setTagData(pRegistries, compoundnbt, pHostPlayerNBT);
      return compoundnbt;
   }

   private void setTagData(DynamicRegistries pRegistry, CompoundNBT pNbt, @Nullable CompoundNBT pPlayerNBT) {
      ListNBT listnbt = new ListNBT();
      this.knownServerBrands.stream().map(StringNBT::valueOf).forEach(listnbt::add);
      pNbt.put("ServerBrands", listnbt);
      pNbt.putBoolean("WasModded", this.wasModded);
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putString("Name", SharedConstants.getCurrentVersion().getName());
      compoundnbt.putInt("Id", SharedConstants.getCurrentVersion().getWorldVersion());
      compoundnbt.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
      pNbt.put("Version", compoundnbt);
      pNbt.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
      WorldGenSettingsExport<INBT> worldgensettingsexport = WorldGenSettingsExport.create(NBTDynamicOps.INSTANCE, pRegistry);
      DimensionGeneratorSettings.CODEC.encodeStart(worldgensettingsexport, this.worldGenSettings).resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).ifPresent((p_237373_1_) -> {
         pNbt.put("WorldGenSettings", p_237373_1_);
      });
      pNbt.putInt("GameType", this.settings.gameType().getId());
      pNbt.putInt("SpawnX", this.xSpawn);
      pNbt.putInt("SpawnY", this.ySpawn);
      pNbt.putInt("SpawnZ", this.zSpawn);
      pNbt.putFloat("SpawnAngle", this.spawnAngle);
      pNbt.putLong("Time", this.gameTime);
      pNbt.putLong("DayTime", this.dayTime);
      pNbt.putLong("LastPlayed", Util.getEpochMillis());
      pNbt.putString("LevelName", this.settings.levelName());
      pNbt.putInt("version", 19133);
      pNbt.putInt("clearWeatherTime", this.clearWeatherTime);
      pNbt.putInt("rainTime", this.rainTime);
      pNbt.putBoolean("raining", this.raining);
      pNbt.putInt("thunderTime", this.thunderTime);
      pNbt.putBoolean("thundering", this.thundering);
      pNbt.putBoolean("hardcore", this.settings.hardcore());
      pNbt.putBoolean("allowCommands", this.settings.allowCommands());
      pNbt.putBoolean("initialized", this.initialized);
      this.worldBorder.write(pNbt);
      pNbt.putByte("Difficulty", (byte)this.settings.difficulty().getId());
      pNbt.putBoolean("DifficultyLocked", this.difficultyLocked);
      pNbt.put("GameRules", this.settings.gameRules().createTag());
      pNbt.put("DragonFight", this.endDragonFightData);
      if (pPlayerNBT != null) {
         pNbt.put("Player", pPlayerNBT);
      }

      DatapackCodec.CODEC.encodeStart(NBTDynamicOps.INSTANCE, this.settings.getDataPackConfig()).result().ifPresent((p_237371_1_) -> {
         pNbt.put("DataPacks", p_237371_1_);
      });
      if (this.customBossEvents != null) {
         pNbt.put("CustomBossEvents", this.customBossEvents);
      }

      pNbt.put("ScheduledEvents", this.scheduledEvents.store());
      pNbt.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
      pNbt.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
      if (this.wanderingTraderId != null) {
         pNbt.putUUID("WanderingTraderId", this.wanderingTraderId);
      }

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

   private void updatePlayerTag() {
      if (!this.upgradedPlayerTag && this.loadedPlayerTag != null) {
         if (this.playerDataVersion < SharedConstants.getCurrentVersion().getWorldVersion()) {
            if (this.fixerUpper == null) {
               throw (NullPointerException)Util.pauseInIde(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded."));
            }

            this.loadedPlayerTag = NBTUtil.update(this.fixerUpper, DefaultTypeReferences.PLAYER, this.loadedPlayerTag, this.playerDataVersion);
         }

         this.upgradedPlayerTag = true;
      }
   }

   public CompoundNBT getLoadedPlayerTag() {
      this.updatePlayerTag();
      return this.loadedPlayerTag;
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

   /**
    * Set current world time
    */
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
    * Get current world name
    */
   public String getLevelName() {
      return this.settings.levelName();
   }

   public int getVersion() {
      return this.version;
   }

   public int getClearWeatherTime() {
      return this.clearWeatherTime;
   }

   public void setClearWeatherTime(int pTime) {
      this.clearWeatherTime = pTime;
   }

   /**
    * Returns true if it is thundering, false otherwise.
    */
   public boolean isThundering() {
      return this.thundering;
   }

   /**
    * Sets whether it is thundering or not.
    */
   public void setThundering(boolean pThundering) {
      this.thundering = pThundering;
   }

   /**
    * Returns the number of ticks until next thunderbolt.
    */
   public int getThunderTime() {
      return this.thunderTime;
   }

   /**
    * Defines the number of ticks until next thunderbolt.
    */
   public void setThunderTime(int pTime) {
      this.thunderTime = pTime;
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
    * Return the number of ticks until rain.
    */
   public int getRainTime() {
      return this.rainTime;
   }

   /**
    * Sets the number of ticks until rain.
    */
   public void setRainTime(int pTime) {
      this.rainTime = pTime;
   }

   /**
    * Gets the GameType.
    */
   public GameType getGameType() {
      return this.settings.gameType();
   }

   public void setGameType(GameType pType) {
      this.settings = this.settings.withGameType(pType);
   }

   /**
    * Returns true if hardcore mode is enabled, otherwise false
    */
   public boolean isHardcore() {
      return this.settings.hardcore();
   }

   /**
    * Returns true if commands are allowed on this World.
    */
   public boolean getAllowCommands() {
      return this.settings.allowCommands();
   }

   /**
    * Returns true if the World is initialized.
    */
   public boolean isInitialized() {
      return this.initialized;
   }

   /**
    * Sets the initialization status of the World.
    */
   public void setInitialized(boolean pInitialized) {
      this.initialized = pInitialized;
   }

   /**
    * Gets the GameRules class Instance.
    */
   public GameRules getGameRules() {
      return this.settings.gameRules();
   }

   public WorldBorder.Serializer getWorldBorder() {
      return this.worldBorder;
   }

   public void setWorldBorder(WorldBorder.Serializer pSerializer) {
      this.worldBorder = pSerializer;
   }

   public Difficulty getDifficulty() {
      return this.settings.difficulty();
   }

   public void setDifficulty(Difficulty pDifficulty) {
      this.settings = this.settings.withDifficulty(pDifficulty);
   }

   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }

   public void setDifficultyLocked(boolean pLocked) {
      this.difficultyLocked = pLocked;
   }

   public TimerCallbackManager<MinecraftServer> getScheduledEvents() {
      return this.scheduledEvents;
   }

   public void fillCrashReportCategory(CrashReportCategory p_85118_1_) {
      IServerWorldInfo.super.fillCrashReportCategory(p_85118_1_);
      IServerConfiguration.super.fillCrashReportCategory(p_85118_1_);
   }

   public DimensionGeneratorSettings worldGenSettings() {
      return this.worldGenSettings;
   }

   @OnlyIn(Dist.CLIENT)
   public Lifecycle worldGenSettingsLifecycle() {
      return this.worldGenSettingsLifecycle;
   }

   public CompoundNBT endDragonFightData() {
      return this.endDragonFightData;
   }

   public void setEndDragonFightData(CompoundNBT pNbt) {
      this.endDragonFightData = pNbt;
   }

   public DatapackCodec getDataPackConfig() {
      return this.settings.getDataPackConfig();
   }

   public void setDataPackConfig(DatapackCodec pCodec) {
      this.settings = this.settings.withDataPackConfig(pCodec);
   }

   @Nullable
   public CompoundNBT getCustomBossEvents() {
      return this.customBossEvents;
   }

   public void setCustomBossEvents(@Nullable CompoundNBT pNbt) {
      this.customBossEvents = pNbt;
   }

   public int getWanderingTraderSpawnDelay() {
      return this.wanderingTraderSpawnDelay;
   }

   public void setWanderingTraderSpawnDelay(int pDelay) {
      this.wanderingTraderSpawnDelay = pDelay;
   }

   public int getWanderingTraderSpawnChance() {
      return this.wanderingTraderSpawnChance;
   }

   public void setWanderingTraderSpawnChance(int pChance) {
      this.wanderingTraderSpawnChance = pChance;
   }

   public void setWanderingTraderId(UUID pId) {
      this.wanderingTraderId = pId;
   }

   public void setModdedInfo(String pName, boolean pIsModded) {
      this.knownServerBrands.add(pName);
      this.wasModded |= pIsModded;
   }

   public boolean wasModded() {
      return this.wasModded;
   }

   public Set<String> getKnownServerBrands() {
      return ImmutableSet.copyOf(this.knownServerBrands);
   }

   public IServerWorldInfo overworldData() {
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   public WorldSettings getLevelSettings() {
      return this.settings.copy();
   }
}