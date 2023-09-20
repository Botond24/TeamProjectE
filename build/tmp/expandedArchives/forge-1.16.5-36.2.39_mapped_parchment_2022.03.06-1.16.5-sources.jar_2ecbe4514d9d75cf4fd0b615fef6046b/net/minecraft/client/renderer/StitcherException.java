package net.minecraft.client.renderer;

import java.util.Collection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StitcherException extends RuntimeException {
   private final Collection<TextureAtlasSprite.Info> allSprites;

   public StitcherException(TextureAtlasSprite.Info pInfo, Collection<TextureAtlasSprite.Info> pAllSprites) {
      super(String.format("Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", pInfo.name(), pInfo.width(), pInfo.height()));
      this.allSprites = pAllSprites;
   }

   public Collection<TextureAtlasSprite.Info> getAllSprites() {
      return this.allSprites;
   }
}