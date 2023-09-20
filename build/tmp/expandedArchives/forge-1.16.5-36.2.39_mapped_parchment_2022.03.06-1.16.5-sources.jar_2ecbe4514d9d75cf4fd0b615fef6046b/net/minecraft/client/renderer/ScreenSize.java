package net.minecraft.client.renderer;

import java.util.OptionalInt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenSize {
   public final int width;
   public final int height;
   public final OptionalInt fullscreenWidth;
   public final OptionalInt fullscreenHeight;
   public final boolean isFullscreen;

   public ScreenSize(int pWidth, int pHeight, OptionalInt pFullscreenWidth, OptionalInt pFullscreenHeight, boolean pIsFullscreen) {
      this.width = pWidth;
      this.height = pHeight;
      this.fullscreenWidth = pFullscreenWidth;
      this.fullscreenHeight = pFullscreenHeight;
      this.isFullscreen = pIsFullscreen;
   }
}