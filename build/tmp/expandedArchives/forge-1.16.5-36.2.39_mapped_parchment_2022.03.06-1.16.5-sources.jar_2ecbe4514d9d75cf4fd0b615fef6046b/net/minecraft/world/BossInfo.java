package net.minecraft.world;

import java.util.UUID;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public abstract class BossInfo {
   private final UUID id;
   protected ITextComponent name;
   protected float percent;
   protected BossInfo.Color color;
   protected BossInfo.Overlay overlay;
   protected boolean darkenScreen;
   protected boolean playBossMusic;
   protected boolean createWorldFog;

   public BossInfo(UUID pId, ITextComponent pName, BossInfo.Color pColor, BossInfo.Overlay pOverlay) {
      this.id = pId;
      this.name = pName;
      this.color = pColor;
      this.overlay = pOverlay;
      this.percent = 1.0F;
   }

   public UUID getId() {
      return this.id;
   }

   public ITextComponent getName() {
      return this.name;
   }

   public void setName(ITextComponent pName) {
      this.name = pName;
   }

   public float getPercent() {
      return this.percent;
   }

   public void setPercent(float p_186735_1_) {
      this.percent = p_186735_1_;
   }

   public BossInfo.Color getColor() {
      return this.color;
   }

   public void setColor(BossInfo.Color pColor) {
      this.color = pColor;
   }

   public BossInfo.Overlay getOverlay() {
      return this.overlay;
   }

   public void setOverlay(BossInfo.Overlay pOverlay) {
      this.overlay = pOverlay;
   }

   public boolean shouldDarkenScreen() {
      return this.darkenScreen;
   }

   public BossInfo setDarkenScreen(boolean pDarkenSky) {
      this.darkenScreen = pDarkenSky;
      return this;
   }

   public boolean shouldPlayBossMusic() {
      return this.playBossMusic;
   }

   public BossInfo setPlayBossMusic(boolean pPlayEndBossMusic) {
      this.playBossMusic = pPlayEndBossMusic;
      return this;
   }

   public BossInfo setCreateWorldFog(boolean pCreateFog) {
      this.createWorldFog = pCreateFog;
      return this;
   }

   public boolean shouldCreateWorldFog() {
      return this.createWorldFog;
   }

   public static enum Color {
      PINK("pink", TextFormatting.RED),
      BLUE("blue", TextFormatting.BLUE),
      RED("red", TextFormatting.DARK_RED),
      GREEN("green", TextFormatting.GREEN),
      YELLOW("yellow", TextFormatting.YELLOW),
      PURPLE("purple", TextFormatting.DARK_BLUE),
      WHITE("white", TextFormatting.WHITE);

      private final String name;
      private final TextFormatting formatting;

      private Color(String pName, TextFormatting pFormatting) {
         this.name = pName;
         this.formatting = pFormatting;
      }

      public TextFormatting getFormatting() {
         return this.formatting;
      }

      public String getName() {
         return this.name;
      }

      public static BossInfo.Color byName(String pName) {
         for(BossInfo.Color bossinfo$color : values()) {
            if (bossinfo$color.name.equals(pName)) {
               return bossinfo$color;
            }
         }

         return WHITE;
      }
   }

   public static enum Overlay {
      PROGRESS("progress"),
      NOTCHED_6("notched_6"),
      NOTCHED_10("notched_10"),
      NOTCHED_12("notched_12"),
      NOTCHED_20("notched_20");

      private final String name;

      private Overlay(String pName) {
         this.name = pName;
      }

      public String getName() {
         return this.name;
      }

      public static BossInfo.Overlay byName(String pName) {
         for(BossInfo.Overlay bossinfo$overlay : values()) {
            if (bossinfo$overlay.name.equals(pName)) {
               return bossinfo$overlay;
            }
         }

         return PROGRESS;
      }
   }
}