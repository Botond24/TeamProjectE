package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Scoreboard {
   private final Map<String, ScoreObjective> objectivesByName = Maps.newHashMap();
   private final Map<ScoreCriteria, List<ScoreObjective>> objectivesByCriteria = Maps.newHashMap();
   private final Map<String, Map<ScoreObjective, Score>> playerScores = Maps.newHashMap();
   private final ScoreObjective[] displayObjectives = new ScoreObjective[19];
   private final Map<String, ScorePlayerTeam> teamsByName = Maps.newHashMap();
   private final Map<String, ScorePlayerTeam> teamsByPlayer = Maps.newHashMap();
   private static String[] displaySlotNames;

   @OnlyIn(Dist.CLIENT)
   public boolean hasObjective(String pObjective) {
      return this.objectivesByName.containsKey(pObjective);
   }

   public ScoreObjective getOrCreateObjective(String pObjective) {
      return this.objectivesByName.get(pObjective);
   }

   /**
    * Returns a ScoreObjective for the objective name
    */
   @Nullable
   public ScoreObjective getObjective(@Nullable String pName) {
      return this.objectivesByName.get(pName);
   }

   public ScoreObjective addObjective(String pName, ScoreCriteria pCriteria, ITextComponent pDisplayName, ScoreCriteria.RenderType pRenderType) {
      if (pName.length() > 16) {
         throw new IllegalArgumentException("The objective name '" + pName + "' is too long!");
      } else if (this.objectivesByName.containsKey(pName)) {
         throw new IllegalArgumentException("An objective with the name '" + pName + "' already exists!");
      } else {
         ScoreObjective scoreobjective = new ScoreObjective(this, pName, pCriteria, pDisplayName, pRenderType);
         this.objectivesByCriteria.computeIfAbsent(pCriteria, (p_197903_0_) -> {
            return Lists.newArrayList();
         }).add(scoreobjective);
         this.objectivesByName.put(pName, scoreobjective);
         this.onObjectiveAdded(scoreobjective);
         return scoreobjective;
      }
   }

   public final void forAllObjectives(ScoreCriteria pCriteria, String pScoreboardName, Consumer<Score> pPoints) {
      this.objectivesByCriteria.getOrDefault(pCriteria, Collections.emptyList()).forEach((p_197906_3_) -> {
         pPoints.accept(this.getOrCreatePlayerScore(pScoreboardName, p_197906_3_));
      });
   }

   /**
    * Returns if the entity has the given ScoreObjective
    */
   public boolean hasPlayerScore(String pName, ScoreObjective pObjective) {
      Map<ScoreObjective, Score> map = this.playerScores.get(pName);
      if (map == null) {
         return false;
      } else {
         Score score = map.get(pObjective);
         return score != null;
      }
   }

   /**
    * Get a player's score or create it if it does not exist
    */
   public Score getOrCreatePlayerScore(String pUsername, ScoreObjective pObjective) {
      if (pUsername.length() > 40) {
         throw new IllegalArgumentException("The player name '" + pUsername + "' is too long!");
      } else {
         Map<ScoreObjective, Score> map = this.playerScores.computeIfAbsent(pUsername, (p_197898_0_) -> {
            return Maps.newHashMap();
         });
         return map.computeIfAbsent(pObjective, (p_197904_2_) -> {
            Score score = new Score(this, p_197904_2_, pUsername);
            score.setScore(0);
            return score;
         });
      }
   }

   /**
    * Returns an array of Score objects, sorting by Score.getScorePoints()
    */
   public Collection<Score> getPlayerScores(ScoreObjective pObjective) {
      List<Score> list = Lists.newArrayList();

      for(Map<ScoreObjective, Score> map : this.playerScores.values()) {
         Score score = map.get(pObjective);
         if (score != null) {
            list.add(score);
         }
      }

      list.sort(Score.SCORE_COMPARATOR);
      return list;
   }

   public Collection<ScoreObjective> getObjectives() {
      return this.objectivesByName.values();
   }

   public Collection<String> getObjectiveNames() {
      return this.objectivesByName.keySet();
   }

   public Collection<String> getTrackedPlayers() {
      return Lists.newArrayList(this.playerScores.keySet());
   }

   /**
    * Remove the given ScoreObjective for the given Entity name.
    */
   public void resetPlayerScore(String pName, @Nullable ScoreObjective pObjective) {
      if (pObjective == null) {
         Map<ScoreObjective, Score> map = this.playerScores.remove(pName);
         if (map != null) {
            this.onPlayerRemoved(pName);
         }
      } else {
         Map<ScoreObjective, Score> map2 = this.playerScores.get(pName);
         if (map2 != null) {
            Score score = map2.remove(pObjective);
            if (map2.size() < 1) {
               Map<ScoreObjective, Score> map1 = this.playerScores.remove(pName);
               if (map1 != null) {
                  this.onPlayerRemoved(pName);
               }
            } else if (score != null) {
               this.onPlayerScoreRemoved(pName, pObjective);
            }
         }
      }

   }

   /**
    * Returns all the objectives for the given entity
    */
   public Map<ScoreObjective, Score> getPlayerScores(String pName) {
      Map<ScoreObjective, Score> map = this.playerScores.get(pName);
      if (map == null) {
         map = Maps.newHashMap();
      }

      return map;
   }

   public void removeObjective(ScoreObjective pObjective) {
      this.objectivesByName.remove(pObjective.getName());

      for(int i = 0; i < 19; ++i) {
         if (this.getDisplayObjective(i) == pObjective) {
            this.setDisplayObjective(i, (ScoreObjective)null);
         }
      }

      List<ScoreObjective> list = this.objectivesByCriteria.get(pObjective.getCriteria());
      if (list != null) {
         list.remove(pObjective);
      }

      for(Map<ScoreObjective, Score> map : this.playerScores.values()) {
         map.remove(pObjective);
      }

      this.onObjectiveRemoved(pObjective);
   }

   /**
    * 0 is tab menu, 1 is sidebar, 2 is below name
    */
   public void setDisplayObjective(int pObjectiveSlot, @Nullable ScoreObjective pObjective) {
      this.displayObjectives[pObjectiveSlot] = pObjective;
   }

   /**
    * 0 is tab menu, 1 is sidebar, 2 is below name
    */
   @Nullable
   public ScoreObjective getDisplayObjective(int pSlot) {
      return this.displayObjectives[pSlot];
   }

   /**
    * Retrieve the ScorePlayerTeam instance identified by the passed team name
    */
   public ScorePlayerTeam getPlayerTeam(String pTeamName) {
      return this.teamsByName.get(pTeamName);
   }

   public ScorePlayerTeam addPlayerTeam(String pName) {
      if (pName.length() > 16) {
         throw new IllegalArgumentException("The team name '" + pName + "' is too long!");
      } else {
         ScorePlayerTeam scoreplayerteam = this.getPlayerTeam(pName);
         if (scoreplayerteam != null) {
            throw new IllegalArgumentException("A team with the name '" + pName + "' already exists!");
         } else {
            scoreplayerteam = new ScorePlayerTeam(this, pName);
            this.teamsByName.put(pName, scoreplayerteam);
            this.onTeamAdded(scoreplayerteam);
            return scoreplayerteam;
         }
      }
   }

   /**
    * Removes the team from the scoreboard, updates all player memberships and broadcasts the deletion to all players
    */
   public void removePlayerTeam(ScorePlayerTeam pPlayerTeam) {
      this.teamsByName.remove(pPlayerTeam.getName());

      for(String s : pPlayerTeam.getPlayers()) {
         this.teamsByPlayer.remove(s);
      }

      this.onTeamRemoved(pPlayerTeam);
   }

   public boolean addPlayerToTeam(String pPlayerName, ScorePlayerTeam pTeam) {
      if (pPlayerName.length() > 40) {
         throw new IllegalArgumentException("The player name '" + pPlayerName + "' is too long!");
      } else {
         if (this.getPlayersTeam(pPlayerName) != null) {
            this.removePlayerFromTeam(pPlayerName);
         }

         this.teamsByPlayer.put(pPlayerName, pTeam);
         return pTeam.getPlayers().add(pPlayerName);
      }
   }

   public boolean removePlayerFromTeam(String pPlayerName) {
      ScorePlayerTeam scoreplayerteam = this.getPlayersTeam(pPlayerName);
      if (scoreplayerteam != null) {
         this.removePlayerFromTeam(pPlayerName, scoreplayerteam);
         return true;
      } else {
         return false;
      }
   }

   /**
    * Removes the given username from the given ScorePlayerTeam. If the player is not on the team then an
    * IllegalStateException is thrown.
    */
   public void removePlayerFromTeam(String pUsername, ScorePlayerTeam pPlayerTeam) {
      if (this.getPlayersTeam(pUsername) != pPlayerTeam) {
         throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + pPlayerTeam.getName() + "'.");
      } else {
         this.teamsByPlayer.remove(pUsername);
         pPlayerTeam.getPlayers().remove(pUsername);
      }
   }

   /**
    * Retrieve all registered ScorePlayerTeam names
    */
   public Collection<String> getTeamNames() {
      return this.teamsByName.keySet();
   }

   /**
    * Retrieve all registered ScorePlayerTeam instances
    */
   public Collection<ScorePlayerTeam> getPlayerTeams() {
      return this.teamsByName.values();
   }

   /**
    * Gets the ScorePlayerTeam object for the given username.
    */
   @Nullable
   public ScorePlayerTeam getPlayersTeam(String pUsername) {
      return this.teamsByPlayer.get(pUsername);
   }

   public void onObjectiveAdded(ScoreObjective pObjective) {
   }

   public void onObjectiveChanged(ScoreObjective pObjective) {
   }

   public void onObjectiveRemoved(ScoreObjective pObjective) {
   }

   public void onScoreChanged(Score pScore) {
   }

   public void onPlayerRemoved(String pScoreName) {
   }

   public void onPlayerScoreRemoved(String pScoreName, ScoreObjective pObjective) {
   }

   public void onTeamAdded(ScorePlayerTeam pPlayerTeam) {
   }

   public void onTeamChanged(ScorePlayerTeam pPlayerTeam) {
   }

   public void onTeamRemoved(ScorePlayerTeam pPlayerTeam) {
   }

   /**
    * Returns 'list' for 0, 'sidebar' for 1, 'belowName for 2, otherwise null.
    */
   public static String getDisplaySlotName(int pId) {
      switch(pId) {
      case 0:
         return "list";
      case 1:
         return "sidebar";
      case 2:
         return "belowName";
      default:
         if (pId >= 3 && pId <= 18) {
            TextFormatting textformatting = TextFormatting.getById(pId - 3);
            if (textformatting != null && textformatting != TextFormatting.RESET) {
               return "sidebar.team." + textformatting.getName();
            }
         }

         return null;
      }
   }

   /**
    * Returns 0 for (case-insensitive) 'list', 1 for 'sidebar', 2 for 'belowName', otherwise -1.
    */
   public static int getDisplaySlotByName(String pName) {
      if ("list".equalsIgnoreCase(pName)) {
         return 0;
      } else if ("sidebar".equalsIgnoreCase(pName)) {
         return 1;
      } else if ("belowName".equalsIgnoreCase(pName)) {
         return 2;
      } else {
         if (pName.startsWith("sidebar.team.")) {
            String s = pName.substring("sidebar.team.".length());
            TextFormatting textformatting = TextFormatting.getByName(s);
            if (textformatting != null && textformatting.getId() >= 0) {
               return textformatting.getId() + 3;
            }
         }

         return -1;
      }
   }

   public static String[] getDisplaySlotNames() {
      if (displaySlotNames == null) {
         displaySlotNames = new String[19];

         for(int i = 0; i < 19; ++i) {
            displaySlotNames[i] = getDisplaySlotName(i);
         }
      }

      return displaySlotNames;
   }

   public void entityRemoved(Entity pEntity) {
      if (pEntity != null && !(pEntity instanceof PlayerEntity) && !pEntity.isAlive()) {
         String s = pEntity.getStringUUID();
         this.resetPlayerScore(s, (ScoreObjective)null);
         this.removePlayerFromTeam(s);
      }
   }

   protected ListNBT savePlayerScores() {
      ListNBT listnbt = new ListNBT();
      this.playerScores.values().stream().map(Map::values).forEach((p_197894_1_) -> {
         p_197894_1_.stream().filter((p_209546_0_) -> {
            return p_209546_0_.getObjective() != null;
         }).forEach((p_197896_1_) -> {
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.putString("Name", p_197896_1_.getOwner());
            compoundnbt.putString("Objective", p_197896_1_.getObjective().getName());
            compoundnbt.putInt("Score", p_197896_1_.getScore());
            compoundnbt.putBoolean("Locked", p_197896_1_.isLocked());
            listnbt.add(compoundnbt);
         });
      });
      return listnbt;
   }

   protected void loadPlayerScores(ListNBT pTag) {
      for(int i = 0; i < pTag.size(); ++i) {
         CompoundNBT compoundnbt = pTag.getCompound(i);
         ScoreObjective scoreobjective = this.getOrCreateObjective(compoundnbt.getString("Objective"));
         String s = compoundnbt.getString("Name");
         if (s.length() > 40) {
            s = s.substring(0, 40);
         }

         Score score = this.getOrCreatePlayerScore(s, scoreobjective);
         score.setScore(compoundnbt.getInt("Score"));
         if (compoundnbt.contains("Locked")) {
            score.setLocked(compoundnbt.getBoolean("Locked"));
         }
      }

   }
}