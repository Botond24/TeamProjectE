package net.minecraft.world.storage;

import java.util.UUID;
import net.minecraft.command.TimerCallbackManager;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;
import net.minecraft.world.border.WorldBorder;

public interface IServerWorldInfo extends ISpawnWorldInfo {
   /**
    * Get current world name
    */
   String getLevelName();

   /**
    * Sets whether it is thundering or not.
    */
   void setThundering(boolean pThundering);

   /**
    * Return the number of ticks until rain.
    */
   int getRainTime();

   /**
    * Sets the number of ticks until rain.
    */
   void setRainTime(int pTime);

   /**
    * Defines the number of ticks until next thunderbolt.
    */
   void setThunderTime(int pTime);

   /**
    * Returns the number of ticks until next thunderbolt.
    */
   int getThunderTime();

   default void fillCrashReportCategory(CrashReportCategory p_85118_1_) {
      ISpawnWorldInfo.super.fillCrashReportCategory(p_85118_1_);
      p_85118_1_.setDetail("Level name", this::getLevelName);
      p_85118_1_.setDetail("Level game mode", () -> {
         return String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.getGameType().getName(), this.getGameType().getId(), this.isHardcore(), this.getAllowCommands());
      });
      p_85118_1_.setDetail("Level weather", () -> {
         return String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering());
      });
   }

   int getClearWeatherTime();

   void setClearWeatherTime(int pTime);

   int getWanderingTraderSpawnDelay();

   void setWanderingTraderSpawnDelay(int pDelay);

   int getWanderingTraderSpawnChance();

   void setWanderingTraderSpawnChance(int pChance);

   void setWanderingTraderId(UUID pId);

   /**
    * Gets the GameType.
    */
   GameType getGameType();

   void setWorldBorder(WorldBorder.Serializer pSerializer);

   WorldBorder.Serializer getWorldBorder();

   /**
    * Returns true if the World is initialized.
    */
   boolean isInitialized();

   /**
    * Sets the initialization status of the World.
    */
   void setInitialized(boolean pInitialized);

   /**
    * Returns true if commands are allowed on this World.
    */
   boolean getAllowCommands();

   void setGameType(GameType pType);

   TimerCallbackManager<MinecraftServer> getScheduledEvents();

   void setGameTime(long pTime);

   /**
    * Set current world time
    */
   void setDayTime(long pTime);
}