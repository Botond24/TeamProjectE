package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureManager implements IFutureReloadListener, ITickable, AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
   private final Map<ResourceLocation, Texture> byPath = Maps.newHashMap();
   private final Set<ITickable> tickableTextures = Sets.newHashSet();
   private final Map<String, Integer> prefixRegister = Maps.newHashMap();
   private final IResourceManager resourceManager;

   public TextureManager(IResourceManager pResourceManager) {
      this.resourceManager = pResourceManager;
   }

   public void bind(ResourceLocation pResource) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this._bind(pResource);
         });
      } else {
         this._bind(pResource);
      }

   }

   private void _bind(ResourceLocation pResource) {
      Texture texture = this.byPath.get(pResource);
      if (texture == null) {
         texture = new SimpleTexture(pResource);
         this.register(pResource, texture);
      }

      texture.bind();
   }

   public void register(ResourceLocation pTextureLocation, Texture pTextureObj) {
      pTextureObj = this.loadTexture(pTextureLocation, pTextureObj);
      Texture texture = this.byPath.put(pTextureLocation, pTextureObj);
      if (texture != pTextureObj) {
         if (texture != null && texture != MissingTextureSprite.getTexture()) {
            this.tickableTextures.remove(texture);
            this.safeClose(pTextureLocation, texture);
         }

         if (pTextureObj instanceof ITickable) {
            this.tickableTextures.add((ITickable)pTextureObj);
         }
      }

   }

   private void safeClose(ResourceLocation p_243505_1_, Texture p_243505_2_) {
      if (p_243505_2_ != MissingTextureSprite.getTexture()) {
         try {
            p_243505_2_.close();
         } catch (Exception exception) {
            LOGGER.warn("Failed to close texture {}", p_243505_1_, exception);
         }
      }

      p_243505_2_.releaseId();
   }

   private Texture loadTexture(ResourceLocation p_230183_1_, Texture p_230183_2_) {
      try {
         p_230183_2_.load(this.resourceManager);
         return p_230183_2_;
      } catch (IOException ioexception) {
         if (p_230183_1_ != INTENTIONAL_MISSING_TEXTURE) {
            LOGGER.warn("Failed to load texture: {}", p_230183_1_, ioexception);
         }

         return MissingTextureSprite.getTexture();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Registering texture");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Resource location being registered");
         crashreportcategory.setDetail("Resource location", p_230183_1_);
         crashreportcategory.setDetail("Texture object class", () -> {
            return p_230183_2_.getClass().getName();
         });
         throw new ReportedException(crashreport);
      }
   }

   @Nullable
   public Texture getTexture(ResourceLocation pTextureLocation) {
      return this.byPath.get(pTextureLocation);
   }

   public ResourceLocation register(String pName, DynamicTexture pTexture) {
      Integer integer = this.prefixRegister.get(pName);
      if (integer == null) {
         integer = 1;
      } else {
         integer = integer + 1;
      }

      this.prefixRegister.put(pName, integer);
      ResourceLocation resourcelocation = new ResourceLocation(String.format("dynamic/%s_%d", pName, integer));
      this.register(resourcelocation, pTexture);
      return resourcelocation;
   }

   public CompletableFuture<Void> preload(ResourceLocation pTextureLocation, Executor pExecutor) {
      if (!this.byPath.containsKey(pTextureLocation)) {
         PreloadedTexture preloadedtexture = new PreloadedTexture(this.resourceManager, pTextureLocation, pExecutor);
         this.byPath.put(pTextureLocation, preloadedtexture);
         return preloadedtexture.getFuture().thenRunAsync(() -> {
            this.register(pTextureLocation, preloadedtexture);
         }, TextureManager::execute);
      } else {
         return CompletableFuture.completedFuture((Void)null);
      }
   }

   private static void execute(Runnable p_229262_0_) {
      Minecraft.getInstance().execute(() -> {
         RenderSystem.recordRenderCall(p_229262_0_::run);
      });
   }

   public void tick() {
      for(ITickable itickable : this.tickableTextures) {
         itickable.tick();
      }

   }

   public void release(ResourceLocation pTextureLocation) {
      Texture texture = this.getTexture(pTextureLocation);
      if (texture != null) {
         this.byPath.remove(pTextureLocation); // Forge: fix MC-98707
         TextureUtil.releaseTextureId(texture.getId());
      }

   }

   public void close() {
      this.byPath.forEach(this::safeClose);
      this.byPath.clear();
      this.tickableTextures.clear();
      this.prefixRegister.clear();
   }

   public CompletableFuture<Void> reload(IFutureReloadListener.IStage pStage, IResourceManager pResourceManager, IProfiler pPreparationsProfiler, IProfiler pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      return CompletableFuture.allOf(MainMenuScreen.preloadResources(this, pBackgroundExecutor), this.preload(Widget.WIDGETS_LOCATION, pBackgroundExecutor)).thenCompose(pStage::wait).thenAcceptAsync((p_229265_3_) -> {
         MissingTextureSprite.getTexture();
         RealmsMainScreen.updateTeaserImages(this.resourceManager);
         Iterator<Entry<ResourceLocation, Texture>> iterator = this.byPath.entrySet().iterator();

         while(iterator.hasNext()) {
            Entry<ResourceLocation, Texture> entry = iterator.next();
            ResourceLocation resourcelocation = entry.getKey();
            Texture texture = entry.getValue();
            if (texture == MissingTextureSprite.getTexture() && !resourcelocation.equals(MissingTextureSprite.getLocation())) {
               iterator.remove();
            } else {
               texture.reset(this, pResourceManager, resourcelocation, pGameExecutor);
            }
         }

      }, (p_229266_0_) -> {
         RenderSystem.recordRenderCall(p_229266_0_::run);
      });
   }
}
