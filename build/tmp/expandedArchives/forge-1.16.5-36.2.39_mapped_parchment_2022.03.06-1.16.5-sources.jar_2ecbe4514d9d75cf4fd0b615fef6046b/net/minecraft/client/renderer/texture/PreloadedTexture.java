package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PreloadedTexture extends SimpleTexture {
   @Nullable
   private CompletableFuture<SimpleTexture.TextureData> future;

   public PreloadedTexture(IResourceManager pManager, ResourceLocation pLocation, Executor pExecutor) {
      super(pLocation);
      this.future = CompletableFuture.supplyAsync(() -> {
         return SimpleTexture.TextureData.load(pManager, pLocation);
      }, pExecutor);
   }

   protected SimpleTexture.TextureData getTextureImage(IResourceManager pResourceManager) {
      if (this.future != null) {
         SimpleTexture.TextureData simpletexture$texturedata = this.future.join();
         this.future = null;
         return simpletexture$texturedata;
      } else {
         return SimpleTexture.TextureData.load(pResourceManager, this.location);
      }
   }

   public CompletableFuture<Void> getFuture() {
      return this.future == null ? CompletableFuture.completedFuture((Void)null) : this.future.thenApply((p_215247_0_) -> {
         return null;
      });
   }

   public void reset(TextureManager pTextureManager, IResourceManager pResourceManager, ResourceLocation pResourceLocation, Executor pExecutor) {
      this.future = CompletableFuture.supplyAsync(() -> {
         return SimpleTexture.TextureData.load(pResourceManager, this.location);
      }, Util.backgroundExecutor());
      this.future.thenRunAsync(() -> {
         pTextureManager.register(this.location, this);
      }, executor(pExecutor));
   }

   private static Executor executor(Executor pExecutor) {
      return (p_229206_1_) -> {
         pExecutor.execute(() -> {
            RenderSystem.recordRenderCall(p_229206_1_::run);
         });
      };
   }
}