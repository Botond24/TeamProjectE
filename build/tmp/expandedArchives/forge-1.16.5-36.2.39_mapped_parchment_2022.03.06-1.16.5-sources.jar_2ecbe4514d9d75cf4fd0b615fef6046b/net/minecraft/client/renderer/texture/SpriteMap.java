package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpriteMap implements AutoCloseable {
   private final Map<ResourceLocation, AtlasTexture> atlases;

   public SpriteMap(Collection<AtlasTexture> pAtlases) {
      this.atlases = pAtlases.stream().collect(Collectors.toMap(AtlasTexture::location, Function.identity()));
   }

   public AtlasTexture getAtlas(ResourceLocation pLocation) {
      return this.atlases.get(pLocation);
   }

   public TextureAtlasSprite getSprite(RenderMaterial pMaterial) {
      return this.atlases.get(pMaterial.atlasLocation()).getSprite(pMaterial.texture());
   }

   public void close() {
      this.atlases.values().forEach(AtlasTexture::clearTextureData);
      this.atlases.clear();
   }
}