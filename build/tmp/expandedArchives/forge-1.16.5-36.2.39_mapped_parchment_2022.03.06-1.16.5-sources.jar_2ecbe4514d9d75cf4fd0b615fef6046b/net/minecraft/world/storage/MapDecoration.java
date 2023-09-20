package net.minecraft.world.storage;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MapDecoration {
   private final MapDecoration.Type type;
   private byte x;
   private byte y;
   private byte rot;
   private final ITextComponent name;

   public MapDecoration(MapDecoration.Type pType, byte pX, byte pY, byte pRot, @Nullable ITextComponent pName) {
      this.type = pType;
      this.x = pX;
      this.y = pY;
      this.rot = pRot;
      this.name = pName;
   }

   @OnlyIn(Dist.CLIENT)
   public byte getImage() {
      return this.type.getIcon();
   }

   public MapDecoration.Type getType() {
      return this.type;
   }

   public byte getX() {
      return this.x;
   }

   public byte getY() {
      return this.y;
   }

   public byte getRot() {
      return this.rot;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean renderOnFrame() {
      return this.type.isRenderedOnFrame();
   }

   @Nullable
   public ITextComponent getName() {
      return this.name;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof MapDecoration)) {
         return false;
      } else {
         MapDecoration mapdecoration = (MapDecoration)p_equals_1_;
         if (this.type != mapdecoration.type) {
            return false;
         } else if (this.rot != mapdecoration.rot) {
            return false;
         } else if (this.x != mapdecoration.x) {
            return false;
         } else if (this.y != mapdecoration.y) {
            return false;
         } else {
            return Objects.equals(this.name, mapdecoration.name);
         }
      }
   }

   public int hashCode() {
      int i = this.type.getIcon();
      i = 31 * i + this.x;
      i = 31 * i + this.y;
      i = 31 * i + this.rot;
      return 31 * i + Objects.hashCode(this.name);
   }

   /**
    * Renders this decoration, useful for custom sprite sheets.
    * @param index The index of this icon in the MapData's list. Used by vanilla to offset the Z-coordinate to prevent Z-fighting
    * @return false to run vanilla logic for this decoration, true to skip it
    */
   @OnlyIn(Dist.CLIENT)
   public boolean render(int index) {
      return false;
   }

   public static enum Type {
      PLAYER(false),
      FRAME(true),
      RED_MARKER(false),
      BLUE_MARKER(false),
      TARGET_X(true),
      TARGET_POINT(true),
      PLAYER_OFF_MAP(false),
      PLAYER_OFF_LIMITS(false),
      MANSION(true, 5393476),
      MONUMENT(true, 3830373),
      BANNER_WHITE(true),
      BANNER_ORANGE(true),
      BANNER_MAGENTA(true),
      BANNER_LIGHT_BLUE(true),
      BANNER_YELLOW(true),
      BANNER_LIME(true),
      BANNER_PINK(true),
      BANNER_GRAY(true),
      BANNER_LIGHT_GRAY(true),
      BANNER_CYAN(true),
      BANNER_PURPLE(true),
      BANNER_BLUE(true),
      BANNER_BROWN(true),
      BANNER_GREEN(true),
      BANNER_RED(true),
      BANNER_BLACK(true),
      RED_X(true);

      private final byte icon = (byte)this.ordinal();
      private final boolean renderedOnFrame;
      private final int mapColor;

      private Type(boolean p_i47343_3_) {
         this(p_i47343_3_, -1);
      }

      private Type(boolean p_i47344_3_, int p_i47344_4_) {
         this.renderedOnFrame = p_i47344_3_;
         this.mapColor = p_i47344_4_;
      }

      public byte getIcon() {
         return this.icon;
      }

      @OnlyIn(Dist.CLIENT)
      public boolean isRenderedOnFrame() {
         return this.renderedOnFrame;
      }

      public boolean hasMapColor() {
         return this.mapColor >= 0;
      }

      public int getMapColor() {
         return this.mapColor;
      }

      public static MapDecoration.Type byIcon(byte pIconId) {
         return values()[MathHelper.clamp(pIconId, 0, values().length - 1)];
      }
   }
}
