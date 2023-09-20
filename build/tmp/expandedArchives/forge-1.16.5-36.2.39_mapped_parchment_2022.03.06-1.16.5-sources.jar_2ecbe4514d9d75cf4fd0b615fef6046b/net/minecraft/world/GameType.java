package net.minecraft.world;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum GameType {
   NOT_SET(-1, ""),
   SURVIVAL(0, "survival"),
   CREATIVE(1, "creative"),
   ADVENTURE(2, "adventure"),
   SPECTATOR(3, "spectator");

   private final int id;
   private final String name;

   private GameType(int pId, String pName) {
      this.id = pId;
      this.name = pName;
   }

   /**
    * Returns the ID of this game type
    */
   public int getId() {
      return this.id;
   }

   /**
    * Returns the name of this game type
    */
   public String getName() {
      return this.name;
   }

   public ITextComponent getDisplayName() {
      return new TranslationTextComponent("gameMode." + this.name);
   }

   /**
    * Configures the player capabilities based on the game type
    */
   public void updatePlayerAbilities(PlayerAbilities pCapabilities) {
      if (this == CREATIVE) {
         pCapabilities.mayfly = true;
         pCapabilities.instabuild = true;
         pCapabilities.invulnerable = true;
      } else if (this == SPECTATOR) {
         pCapabilities.mayfly = true;
         pCapabilities.instabuild = false;
         pCapabilities.invulnerable = true;
         pCapabilities.flying = true;
      } else {
         pCapabilities.mayfly = false;
         pCapabilities.instabuild = false;
         pCapabilities.invulnerable = false;
         pCapabilities.flying = false;
      }

      pCapabilities.mayBuild = !this.isBlockPlacingRestricted();
   }

   /**
    * Returns true if this is the ADVENTURE game type
    */
   public boolean isBlockPlacingRestricted() {
      return this == ADVENTURE || this == SPECTATOR;
   }

   /**
    * Returns true if this is the CREATIVE game type
    */
   public boolean isCreative() {
      return this == CREATIVE;
   }

   /**
    * Returns true if this is the SURVIVAL or ADVENTURE game type
    */
   public boolean isSurvival() {
      return this == SURVIVAL || this == ADVENTURE;
   }

   /**
    * Gets the game type by it's ID. Will be survival if none was found.
    */
   public static GameType byId(int pId) {
      return byId(pId, SURVIVAL);
   }

   public static GameType byId(int pTargetId, GameType pFallback) {
      for(GameType gametype : values()) {
         if (gametype.id == pTargetId) {
            return gametype;
         }
      }

      return pFallback;
   }

   /**
    * Gets the game type registered with the specified name. If no matches were found, survival will be returned.
    */
   public static GameType byName(String pGamemodeName) {
      return byName(pGamemodeName, SURVIVAL);
   }

   public static GameType byName(String pTargetName, GameType pFallback) {
      for(GameType gametype : values()) {
         if (gametype.name.equals(pTargetName)) {
            return gametype;
         }
      }

      return pFallback;
   }
}