package net.minecraft.client.renderer.texture;

import java.util.stream.Stream;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PaintingSpriteUploader extends SpriteUploader {
   private static final ResourceLocation BACK_SPRITE_LOCATION = new ResourceLocation("back");

   public PaintingSpriteUploader(TextureManager pManager) {
      super(pManager, new ResourceLocation("textures/atlas/paintings.png"), "painting");
   }

   protected Stream<ResourceLocation> getResourcesToLoad() {
      return Stream.concat(Registry.MOTIVE.keySet().stream(), Stream.of(BACK_SPRITE_LOCATION));
   }

   /**
    * Gets the sprite used for a specific painting type.
    */
   public TextureAtlasSprite get(PaintingType pPaintingType) {
      return this.getSprite(Registry.MOTIVE.getKey(pPaintingType));
   }

   public TextureAtlasSprite getBackSprite() {
      return this.getSprite(BACK_SPRITE_LOCATION);
   }
}