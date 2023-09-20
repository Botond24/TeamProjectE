package net.minecraft.world;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.codec.DatapackCodec;

public final class WorldSettings {
   private final String levelName;
   private final GameType gameType;
   private final boolean hardcore;
   private final Difficulty difficulty;
   private final boolean allowCommands;
   private final GameRules gameRules;
   private final DatapackCodec dataPackConfig;

   public WorldSettings(String pLevelName, GameType pGameType, boolean pHardcore, Difficulty pDifficulty, boolean pAllowCommands, GameRules pGameRules, DatapackCodec pDataPackConfig) {
      this.levelName = pLevelName;
      this.gameType = pGameType;
      this.hardcore = pHardcore;
      this.difficulty = pDifficulty;
      this.allowCommands = pAllowCommands;
      this.gameRules = pGameRules;
      this.dataPackConfig = pDataPackConfig;
   }

   public static WorldSettings parse(Dynamic<?> pDynamic, DatapackCodec pCodec) {
      GameType gametype = GameType.byId(pDynamic.get("GameType").asInt(0));
      return new WorldSettings(pDynamic.get("LevelName").asString(""), gametype, pDynamic.get("hardcore").asBoolean(false), pDynamic.get("Difficulty").asNumber().map((p_234952_0_) -> {
         return Difficulty.byId(p_234952_0_.byteValue());
      }).result().orElse(Difficulty.NORMAL), pDynamic.get("allowCommands").asBoolean(gametype == GameType.CREATIVE), new GameRules(pDynamic.get("GameRules")), pCodec);
   }

   public String levelName() {
      return this.levelName;
   }

   public GameType gameType() {
      return this.gameType;
   }

   public boolean hardcore() {
      return this.hardcore;
   }

   public Difficulty difficulty() {
      return this.difficulty;
   }

   public boolean allowCommands() {
      return this.allowCommands;
   }

   public GameRules gameRules() {
      return this.gameRules;
   }

   public DatapackCodec getDataPackConfig() {
      return this.dataPackConfig;
   }

   public WorldSettings withGameType(GameType pGameType) {
      return new WorldSettings(this.levelName, pGameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
   }

   public WorldSettings withDifficulty(Difficulty pDifficulty) {
      net.minecraftforge.common.ForgeHooks.onDifficultyChange(pDifficulty, this.difficulty);
      return new WorldSettings(this.levelName, this.gameType, this.hardcore, pDifficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
   }

   public WorldSettings withDataPackConfig(DatapackCodec pDatapackCodec) {
      return new WorldSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, pDatapackCodec);
   }

   public WorldSettings copy() {
      return new WorldSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules.copy(), this.dataPackConfig);
   }
}
