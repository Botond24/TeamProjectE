package net.minecraft.world.storage;

import com.mojang.serialization.Lifecycle;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IServerConfiguration {
   DatapackCodec getDataPackConfig();

   void setDataPackConfig(DatapackCodec pCodec);

   boolean wasModded();

   Set<String> getKnownServerBrands();

   void setModdedInfo(String pName, boolean pIsModded);

   default void fillCrashReportCategory(CrashReportCategory p_85118_1_) {
      p_85118_1_.setDetail("Known server brands", () -> {
         return String.join(", ", this.getKnownServerBrands());
      });
      p_85118_1_.setDetail("Level was modded", () -> {
         return Boolean.toString(this.wasModded());
      });
      p_85118_1_.setDetail("Level storage version", () -> {
         int i = this.getVersion();
         return String.format("0x%05X - %s", i, this.getStorageVersionName(i));
      });
   }

   default String getStorageVersionName(int pStorageVersionId) {
      switch(pStorageVersionId) {
      case 19132:
         return "McRegion";
      case 19133:
         return "Anvil";
      default:
         return "Unknown?";
      }
   }

   @Nullable
   CompoundNBT getCustomBossEvents();

   void setCustomBossEvents(@Nullable CompoundNBT pNbt);

   IServerWorldInfo overworldData();

   @OnlyIn(Dist.CLIENT)
   WorldSettings getLevelSettings();

   CompoundNBT createTag(DynamicRegistries pRegistries, @Nullable CompoundNBT pHostPlayerNBT);

   /**
    * Returns true if hardcore mode is enabled, otherwise false
    */
   boolean isHardcore();

   int getVersion();

   /**
    * Get current world name
    */
   String getLevelName();

   /**
    * Gets the GameType.
    */
   GameType getGameType();

   void setGameType(GameType pType);

   /**
    * Returns true if commands are allowed on this World.
    */
   boolean getAllowCommands();

   Difficulty getDifficulty();

   void setDifficulty(Difficulty pDifficulty);

   boolean isDifficultyLocked();

   void setDifficultyLocked(boolean pLocked);

   /**
    * Gets the GameRules class Instance.
    */
   GameRules getGameRules();

   CompoundNBT getLoadedPlayerTag();

   CompoundNBT endDragonFightData();

   void setEndDragonFightData(CompoundNBT pNbt);

   DimensionGeneratorSettings worldGenSettings();

   @OnlyIn(Dist.CLIENT)
   Lifecycle worldGenSettingsLifecycle();
}