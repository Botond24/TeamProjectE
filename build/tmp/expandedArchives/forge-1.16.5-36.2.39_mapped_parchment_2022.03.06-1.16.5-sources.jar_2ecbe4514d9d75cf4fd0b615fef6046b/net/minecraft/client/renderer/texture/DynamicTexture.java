package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DynamicTexture extends Texture {
   private static final Logger LOGGER = LogManager.getLogger();
   @Nullable
   private NativeImage pixels;

   public DynamicTexture(NativeImage pPixels) {
      this.pixels = pPixels;
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
            this.upload();
         });
      } else {
         TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
         this.upload();
      }

   }

   public DynamicTexture(int pWidth, int pHeight, boolean p_i48125_3_) {
      RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
      this.pixels = new NativeImage(pWidth, pHeight, p_i48125_3_);
      TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
   }

   public void load(IResourceManager pManager) {
   }

   public void upload() {
      if (this.pixels != null) {
         this.bind();
         this.pixels.upload(0, 0, 0, false);
      } else {
         LOGGER.warn("Trying to upload disposed texture {}", (int)this.getId());
      }

   }

   @Nullable
   public NativeImage getPixels() {
      return this.pixels;
   }

   public void setPixels(NativeImage pNativeImage) {
      if (this.pixels != null) {
         this.pixels.close();
      }

      this.pixels = pNativeImage;
   }

   public void close() {
      if (this.pixels != null) {
         this.pixels.close();
         this.releaseId();
         this.pixels = null;
      }

   }
}