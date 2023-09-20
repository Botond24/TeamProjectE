package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.LanServerPingThread;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.GameType;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class IntegratedServer extends MinecraftServer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft minecraft;
   private boolean paused;
   private int publishedPort = -1;
   private LanServerPingThread lanPinger;
   private UUID uuid;

   public IntegratedServer(Thread pServerThread, Minecraft pMinecraft, DynamicRegistries.Impl pRegistryHolder, SaveFormat.LevelSave pStorageSource, ResourcePackList pPackRepository, DataPackRegistries pResources, IServerConfiguration pWorldData, MinecraftSessionService pSessionService, GameProfileRepository pProfileRepository, PlayerProfileCache pProfileCache, IChunkStatusListenerFactory pProgressListenerfactory) {
      super(pServerThread, pRegistryHolder, pStorageSource, pWorldData, pPackRepository, pMinecraft.getProxy(), pMinecraft.getFixerUpper(), pResources, pSessionService, pProfileRepository, pProfileCache, pProgressListenerfactory);
      this.setSingleplayerName(pMinecraft.getUser().getName());
      this.setDemo(pMinecraft.isDemo());
      this.setMaxBuildHeight(256);
      this.setPlayerList(new IntegratedPlayerList(this, this.registryHolder, this.playerDataStorage));
      this.minecraft = pMinecraft;
   }

   /**
    * Initialises the server and starts it.
    */
   public boolean initServer() {
      LOGGER.info("Starting integrated minecraft server version " + SharedConstants.getCurrentVersion().getName());
      this.setUsesAuthentication(true);
      this.setPvpAllowed(true);
      this.setFlightAllowed(true);
      this.initializeKeyPair();
      if (!net.minecraftforge.fml.server.ServerLifecycleHooks.handleServerAboutToStart(this)) return false;
      this.loadLevel();
      this.setMotd(this.getSingleplayerName() + " - " + this.getWorldData().getLevelName());
      return net.minecraftforge.fml.server.ServerLifecycleHooks.handleServerStarting(this);
   }

   /**
    * Main function called by run() every loop.
    */
   public void tickServer(BooleanSupplier pHasTimeLeft) {
      boolean flag = this.paused;
      this.paused = Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().isPaused();
      IProfiler iprofiler = this.getProfiler();
      if (!flag && this.paused) {
         iprofiler.push("autoSave");
         LOGGER.info("Saving and pausing game...");
         this.getPlayerList().saveAll();
         this.saveAllChunks(false, false, false);
         iprofiler.pop();
      }

      if (!this.paused) {
         super.tickServer(pHasTimeLeft);
         int i = Math.max(2, this.minecraft.options.renderDistance + -1);
         if (i != this.getPlayerList().getViewDistance()) {
            LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
            this.getPlayerList().setViewDistance(i);
         }

      }
   }

   public boolean shouldRconBroadcast() {
      return true;
   }

   public boolean shouldInformAdmins() {
      return true;
   }

   public File getServerDirectory() {
      return this.minecraft.gameDirectory;
   }

   public boolean isDedicatedServer() {
      return false;
   }

   public int getRateLimitPacketsPerSecond() {
      return 0;
   }

   /**
    * Get if native transport should be used. Native transport means linux server performance improvements and optimized
    * packet sending/receiving on linux
    */
   public boolean isEpollEnabled() {
      return false;
   }

   /**
    * Called on exit from the main run() loop.
    */
   public void onServerCrash(CrashReport pReport) {
      this.minecraft.delayCrash(pReport);
   }

   public CrashReport fillReport(CrashReport pReport) {
      pReport = super.fillReport(pReport);
      pReport.getSystemDetails().setDetail("Type", "Integrated Server (map_client.txt)");
      pReport.getSystemDetails().setDetail("Is Modded", () -> {
         return this.getModdedStatus().orElse("Probably not. Jar signature remains and both client + server brands are untouched.");
      });
      return pReport;
   }

   public Optional<String> getModdedStatus() {
      String s = ClientBrandRetriever.getClientModName();
      if (!s.equals("vanilla")) {
         return Optional.of("Definitely; Client brand changed to '" + s + "'");
      } else {
         s = this.getServerModName();
         if (!"vanilla".equals(s)) {
            return Optional.of("Definitely; Server brand changed to '" + s + "'");
         } else {
            return Minecraft.class.getSigners() == null ? Optional.of("Very likely; Jar signature invalidated") : Optional.empty();
         }
      }
   }

   public void populateSnooper(Snooper pSnooper) {
      super.populateSnooper(pSnooper);
      pSnooper.setDynamicData("snooper_partner", this.minecraft.getSnooper().getToken());
   }

   public boolean publishServer(GameType pGameMode, boolean pCheats, int pPort) {
      try {
         this.getConnection().startTcpServerListener((InetAddress)null, pPort);
         LOGGER.info("Started serving on {}", (int)pPort);
         this.publishedPort = pPort;
         this.lanPinger = new LanServerPingThread(this.getMotd(), pPort + "");
         this.lanPinger.start();
         this.getPlayerList().setOverrideGameMode(pGameMode);
         this.getPlayerList().setAllowCheatsForAllPlayers(pCheats);
         int i = this.getProfilePermissions(this.minecraft.player.getGameProfile());
         this.minecraft.player.setPermissionLevel(i);

         for(ServerPlayerEntity serverplayerentity : this.getPlayerList().getPlayers()) {
            this.getCommands().sendCommands(serverplayerentity);
         }

         return true;
      } catch (IOException ioexception) {
         return false;
      }
   }

   /**
    * Saves all necessary data as preparation for stopping the server.
    */
   public void stopServer() {
      super.stopServer();
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   /**
    * Sets the serverRunning variable to false, in order to get the server to shut down.
    */
   public void halt(boolean pWaitForServer) {
      if (isRunning())
      this.executeBlocking(() -> {
         for(ServerPlayerEntity serverplayerentity : Lists.newArrayList(this.getPlayerList().getPlayers())) {
            if (!serverplayerentity.getUUID().equals(this.uuid)) {
               this.getPlayerList().remove(serverplayerentity);
            }
         }

      });
      super.halt(pWaitForServer);
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   /**
    * Returns true if this integrated server is open to LAN
    */
   public boolean isPublished() {
      return this.publishedPort > -1;
   }

   /**
    * Gets serverPort.
    */
   public int getPort() {
      return this.publishedPort;
   }

   /**
    * Sets the game type for all worlds.
    */
   public void setDefaultGameType(GameType pGameMode) {
      super.setDefaultGameType(pGameMode);
      this.getPlayerList().setOverrideGameMode(pGameMode);
   }

   /**
    * Return whether command blocks are enabled.
    */
   public boolean isCommandBlockEnabled() {
      return true;
   }

   public int getOperatorUserPermissionLevel() {
      return 2;
   }

   public int getFunctionCompilationLevel() {
      return 2;
   }

   public void setUUID(UUID pUuid) {
      this.uuid = pUuid;
   }

   public boolean isSingleplayerOwner(GameProfile pProfile) {
      return pProfile.getName().equalsIgnoreCase(this.getSingleplayerName());
   }

   public int getScaledTrackingDistance(int p_230512_1_) {
      return (int)(this.minecraft.options.entityDistanceScaling * (float)p_230512_1_);
   }

   public boolean forceSynchronousWrites() {
      return this.minecraft.options.syncWrites;
   }
}
