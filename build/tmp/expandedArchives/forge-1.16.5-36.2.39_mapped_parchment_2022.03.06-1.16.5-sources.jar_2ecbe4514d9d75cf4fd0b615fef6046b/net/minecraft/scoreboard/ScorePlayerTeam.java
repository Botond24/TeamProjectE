package net.minecraft.scoreboard;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ScorePlayerTeam extends Team {
   private final Scoreboard scoreboard;
   private final String name;
   private final Set<String> players = Sets.newHashSet();
   private ITextComponent displayName;
   private ITextComponent playerPrefix = StringTextComponent.EMPTY;
   private ITextComponent playerSuffix = StringTextComponent.EMPTY;
   private boolean allowFriendlyFire = true;
   private boolean seeFriendlyInvisibles = true;
   private Team.Visible nameTagVisibility = Team.Visible.ALWAYS;
   private Team.Visible deathMessageVisibility = Team.Visible.ALWAYS;
   private TextFormatting color = TextFormatting.RESET;
   private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;
   private final Style displayNameStyle;

   public ScorePlayerTeam(Scoreboard pScoreboard, String pName) {
      this.scoreboard = pScoreboard;
      this.name = pName;
      this.displayName = new StringTextComponent(pName);
      this.displayNameStyle = Style.EMPTY.withInsertion(pName).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(pName)));
   }

   /**
    * Retrieve the name by which this team is registered in the scoreboard
    */
   public String getName() {
      return this.name;
   }

   /**
    * Gets the display name for this team.
    */
   public ITextComponent getDisplayName() {
      return this.displayName;
   }

   public IFormattableTextComponent getFormattedDisplayName() {
      IFormattableTextComponent iformattabletextcomponent = TextComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle(this.displayNameStyle));
      TextFormatting textformatting = this.getColor();
      if (textformatting != TextFormatting.RESET) {
         iformattabletextcomponent.withStyle(textformatting);
      }

      return iformattabletextcomponent;
   }

   /**
    * Sets the display name for this team.
    */
   public void setDisplayName(ITextComponent pName) {
      if (pName == null) {
         throw new IllegalArgumentException("Name cannot be null");
      } else {
         this.displayName = pName;
         this.scoreboard.onTeamChanged(this);
      }
   }

   public void setPlayerPrefix(@Nullable ITextComponent pPlayerPrefix) {
      this.playerPrefix = pPlayerPrefix == null ? StringTextComponent.EMPTY : pPlayerPrefix;
      this.scoreboard.onTeamChanged(this);
   }

   public ITextComponent getPlayerPrefix() {
      return this.playerPrefix;
   }

   public void setPlayerSuffix(@Nullable ITextComponent pPlayerSuffix) {
      this.playerSuffix = pPlayerSuffix == null ? StringTextComponent.EMPTY : pPlayerSuffix;
      this.scoreboard.onTeamChanged(this);
   }

   public ITextComponent getPlayerSuffix() {
      return this.playerSuffix;
   }

   /**
    * Gets a collection of all members of this team.
    */
   public Collection<String> getPlayers() {
      return this.players;
   }

   public IFormattableTextComponent getFormattedName(ITextComponent pFormattedName) {
      IFormattableTextComponent iformattabletextcomponent = (new StringTextComponent("")).append(this.playerPrefix).append(pFormattedName).append(this.playerSuffix);
      TextFormatting textformatting = this.getColor();
      if (textformatting != TextFormatting.RESET) {
         iformattabletextcomponent.withStyle(textformatting);
      }

      return iformattabletextcomponent;
   }

   public static IFormattableTextComponent formatNameForTeam(@Nullable Team pPlayerTeam, ITextComponent pPlayerName) {
      return pPlayerTeam == null ? pPlayerName.copy() : pPlayerTeam.getFormattedName(pPlayerName);
   }

   /**
    * Checks whether friendly fire (PVP between members of the team) is allowed.
    */
   public boolean isAllowFriendlyFire() {
      return this.allowFriendlyFire;
   }

   /**
    * Sets whether friendly fire (PVP between members of the team) is allowed.
    */
   public void setAllowFriendlyFire(boolean pFriendlyFire) {
      this.allowFriendlyFire = pFriendlyFire;
      this.scoreboard.onTeamChanged(this);
   }

   /**
    * Checks whether members of this team can see other members that are invisible.
    */
   public boolean canSeeFriendlyInvisibles() {
      return this.seeFriendlyInvisibles;
   }

   /**
    * Sets whether members of this team can see other members that are invisible.
    */
   public void setSeeFriendlyInvisibles(boolean pFriendlyInvisibles) {
      this.seeFriendlyInvisibles = pFriendlyInvisibles;
      this.scoreboard.onTeamChanged(this);
   }

   /**
    * Gets the visibility flags for player name tags.
    */
   public Team.Visible getNameTagVisibility() {
      return this.nameTagVisibility;
   }

   /**
    * Gets the visibility flags for player death messages.
    */
   public Team.Visible getDeathMessageVisibility() {
      return this.deathMessageVisibility;
   }

   /**
    * Sets the visibility flags for player name tags.
    */
   public void setNameTagVisibility(Team.Visible pVisibility) {
      this.nameTagVisibility = pVisibility;
      this.scoreboard.onTeamChanged(this);
   }

   /**
    * Sets the visibility flags for player death messages.
    */
   public void setDeathMessageVisibility(Team.Visible pVisibility) {
      this.deathMessageVisibility = pVisibility;
      this.scoreboard.onTeamChanged(this);
   }

   /**
    * Gets the rule to be used for handling collisions with members of this team.
    */
   public Team.CollisionRule getCollisionRule() {
      return this.collisionRule;
   }

   /**
    * Sets the rule to be used for handling collisions with members of this team.
    */
   public void setCollisionRule(Team.CollisionRule pRule) {
      this.collisionRule = pRule;
      this.scoreboard.onTeamChanged(this);
   }

   /**
    * Gets a bitmask containing the friendly fire and invisibles flags.
    */
   public int packOptions() {
      int i = 0;
      if (this.isAllowFriendlyFire()) {
         i |= 1;
      }

      if (this.canSeeFriendlyInvisibles()) {
         i |= 2;
      }

      return i;
   }

   /**
    * Sets friendly fire and invisibles flags based off of the given bitmask.
    */
   @OnlyIn(Dist.CLIENT)
   public void unpackOptions(int pFlags) {
      this.setAllowFriendlyFire((pFlags & 1) > 0);
      this.setSeeFriendlyInvisibles((pFlags & 2) > 0);
   }

   /**
    * Sets the color for this team. The team color is used mainly for team kill objectives and team-specific setDisplay
    * usage" it does _not_ affect all situations (for instance, the prefix is used for the glowing effect).
    */
   public void setColor(TextFormatting pColor) {
      this.color = pColor;
      this.scoreboard.onTeamChanged(this);
   }

   /**
    * Gets the color for this team. The team color is used mainly for team kill objectives and team-specific setDisplay
    * usage" it does _not_ affect all situations (for instance, the prefix is used for the glowing effect).
    */
   public TextFormatting getColor() {
      return this.color;
   }
}