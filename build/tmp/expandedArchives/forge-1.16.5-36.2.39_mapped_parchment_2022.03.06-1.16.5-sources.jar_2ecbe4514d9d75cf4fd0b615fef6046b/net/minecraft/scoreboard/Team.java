package net.minecraft.scoreboard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Team {
   /**
    * Same as ==
    */
   public boolean isAlliedTo(@Nullable Team pOther) {
      if (pOther == null) {
         return false;
      } else {
         return this == pOther;
      }
   }

   /**
    * Retrieve the name by which this team is registered in the scoreboard
    */
   public abstract String getName();

   public abstract IFormattableTextComponent getFormattedName(ITextComponent pFormattedName);

   /**
    * Checks whether members of this team can see other members that are invisible.
    */
   @OnlyIn(Dist.CLIENT)
   public abstract boolean canSeeFriendlyInvisibles();

   /**
    * Checks whether friendly fire (PVP between members of the team) is allowed.
    */
   public abstract boolean isAllowFriendlyFire();

   /**
    * Gets the visibility flags for player name tags.
    */
   @OnlyIn(Dist.CLIENT)
   public abstract Team.Visible getNameTagVisibility();

   /**
    * Gets the color for this team. The team color is used mainly for team kill objectives and team-specific setDisplay
    * usage" it does _not_ affect all situations (for instance, the prefix is used for the glowing effect).
    */
   @OnlyIn(Dist.CLIENT)
   public abstract TextFormatting getColor();

   /**
    * Gets a collection of all members of this team.
    */
   public abstract Collection<String> getPlayers();

   /**
    * Gets the visibility flags for player death messages.
    */
   public abstract Team.Visible getDeathMessageVisibility();

   /**
    * Gets the rule to be used for handling collisions with members of this team.
    */
   public abstract Team.CollisionRule getCollisionRule();

   public static enum CollisionRule {
      ALWAYS("always", 0),
      NEVER("never", 1),
      PUSH_OTHER_TEAMS("pushOtherTeams", 2),
      PUSH_OWN_TEAM("pushOwnTeam", 3);

      private static final Map<String, Team.CollisionRule> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((p_199871_0_) -> {
         return p_199871_0_.name;
      }, (p_199870_0_) -> {
         return p_199870_0_;
      }));
      public final String name;
      public final int id;

      @Nullable
      public static Team.CollisionRule byName(String pName) {
         return BY_NAME.get(pName);
      }

      private CollisionRule(String pName, int pId) {
         this.name = pName;
         this.id = pId;
      }

      public ITextComponent getDisplayName() {
         return new TranslationTextComponent("team.collision." + this.name);
      }
   }

   public static enum Visible {
      ALWAYS("always", 0),
      NEVER("never", 1),
      HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
      HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

      private static final Map<String, Team.Visible> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((p_199873_0_) -> {
         return p_199873_0_.name;
      }, (p_199872_0_) -> {
         return p_199872_0_;
      }));
      public final String name;
      public final int id;

      @Nullable
      public static Team.Visible byName(String pName) {
         return BY_NAME.get(pName);
      }

      private Visible(String pName, int pId) {
         this.name = pName;
         this.id = pId;
      }

      public ITextComponent getDisplayName() {
         return new TranslationTextComponent("team.visibility." + this.name);
      }
   }
}