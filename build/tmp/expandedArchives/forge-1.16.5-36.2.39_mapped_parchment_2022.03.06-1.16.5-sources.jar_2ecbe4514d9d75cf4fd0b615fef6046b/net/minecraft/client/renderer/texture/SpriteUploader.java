package net.minecraft.client.renderer.texture;

import java.util.stream.Stream;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SpriteUploader extends ReloadListener<AtlasTexture.SheetData> implements AutoCloseable {
   private final AtlasTexture textureAtlas;
   private final String prefix;

   public SpriteUploader(TextureManager pTextureManager, ResourceLocation pTextureAtlasLocation, String pPrefix) {
      this.prefix = pPrefix;
      this.textureAtlas = new AtlasTexture(pTextureAtlasLocation);
      pTextureManager.register(this.textureAtlas.location(), this.textureAtlas);
   }

   protected abstract Stream<ResourceLocation> getResourcesToLoad();

   /**
    * Gets a sprite associated with the passed resource location.
    */
   protected TextureAtlasSprite getSprite(ResourceLocation pLocation) {
      return this.textureAtlas.getSprite(this.resolveLocation(pLocation));
   }

   private ResourceLocation resolveLocation(ResourceLocation p_229299_1_) {
      return new ResourceLocation(p_229299_1_.getNamespace(), this.prefix + "/" + p_229299_1_.getPath());
   }

   /**
    * Performs any reloading that can be done off-thread, such as file IO
    */
   protected AtlasTexture.SheetData prepare(IResourceManager pResourceManager, IProfiler pProfiler) {
      pProfiler.startTick();
      pProfiler.push("stitching");
      AtlasTexture.SheetData atlastexture$sheetdata = this.textureAtlas.prepareToStitch(pResourceManager, this.getResourcesToLoad().map(this::resolveLocation), pProfiler, 0);
      pProfiler.pop();
      pProfiler.endTick();
      return atlastexture$sheetdata;
   }

   protected void apply(AtlasTexture.SheetData pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
      pProfiler.startTick();
      pProfiler.push("upload");
      this.textureAtlas.reload(pObject);
      pProfiler.pop();
      pProfiler.endTick();
   }

   public void close() {
      this.textureAtlas.clearTextureData();
   }
}