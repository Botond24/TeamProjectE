package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinManager {
   private final TextureManager textureManager;
   private final File skinsDirectory;
   private final MinecraftSessionService sessionService;
   private final LoadingCache<String, Map<Type, MinecraftProfileTexture>> insecureSkinCache;

   public SkinManager(TextureManager pManager, File pSkinsDirectory, final MinecraftSessionService pSessionService) {
      this.textureManager = pManager;
      this.skinsDirectory = pSkinsDirectory;
      this.sessionService = pSessionService;
      this.insecureSkinCache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<String, Map<Type, MinecraftProfileTexture>>() {
         public Map<Type, MinecraftProfileTexture> load(String p_load_1_) {
            GameProfile gameprofile = new GameProfile((UUID)null, "dummy_mcdummyface");
            gameprofile.getProperties().put("textures", new Property("textures", p_load_1_, ""));

            try {
               return pSessionService.getTextures(gameprofile, false);
            } catch (Throwable throwable) {
               return ImmutableMap.of();
            }
         }
      });
   }

   /**
    * Used in the Skull renderer to fetch a skin. May download the skin if it's not in the cache
    */
   public ResourceLocation registerTexture(MinecraftProfileTexture pProfileTexture, Type pTextureType) {
      return this.registerTexture(pProfileTexture, pTextureType, (SkinManager.ISkinAvailableCallback)null);
   }

   /**
    * May download the skin if its not in the cache, can be passed a SkinManager#SkinAvailableCallback for handling
    */
   private ResourceLocation registerTexture(MinecraftProfileTexture pProfileTexture, Type pTextureType, @Nullable SkinManager.ISkinAvailableCallback pSkinAvailableCallback) {
      String s = Hashing.sha1().hashUnencodedChars(pProfileTexture.getHash()).toString();
      ResourceLocation resourcelocation = new ResourceLocation("skins/" + s);
      Texture texture = this.textureManager.getTexture(resourcelocation);
      if (texture != null) {
         if (pSkinAvailableCallback != null) {
            pSkinAvailableCallback.onSkinTextureAvailable(pTextureType, resourcelocation, pProfileTexture);
         }
      } else {
         File file1 = new File(this.skinsDirectory, s.length() > 2 ? s.substring(0, 2) : "xx");
         File file2 = new File(file1, s);
         DownloadingTexture downloadingtexture = new DownloadingTexture(file2, pProfileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), pTextureType == Type.SKIN, () -> {
            if (pSkinAvailableCallback != null) {
               pSkinAvailableCallback.onSkinTextureAvailable(pTextureType, resourcelocation, pProfileTexture);
            }

         });
         this.textureManager.register(resourcelocation, downloadingtexture);
      }

      return resourcelocation;
   }

   public void registerSkins(GameProfile pProfile, SkinManager.ISkinAvailableCallback pSkinAvailableCallback, boolean pRequireSecure) {
      Runnable runnable = () -> {
         Map<Type, MinecraftProfileTexture> map = Maps.newHashMap();

         try {
            map.putAll(this.sessionService.getTextures(pProfile, pRequireSecure));
         } catch (InsecureTextureException insecuretextureexception1) {
         }

         if (map.isEmpty()) {
            pProfile.getProperties().clear();
            if (pProfile.getId().equals(Minecraft.getInstance().getUser().getGameProfile().getId())) {
               pProfile.getProperties().putAll(Minecraft.getInstance().getProfileProperties());
               map.putAll(this.sessionService.getTextures(pProfile, false));
            } else {
               this.sessionService.fillProfileProperties(pProfile, pRequireSecure);

               try {
                  map.putAll(this.sessionService.getTextures(pProfile, pRequireSecure));
               } catch (InsecureTextureException insecuretextureexception) {
               }
            }
         }

         Minecraft.getInstance().execute(() -> {
            RenderSystem.recordRenderCall(() -> {
               ImmutableList.of(Type.SKIN, Type.CAPE).forEach((p_229296_3_) -> {
                  if (map.containsKey(p_229296_3_)) {
                     this.registerTexture(map.get(p_229296_3_), p_229296_3_, pSkinAvailableCallback);
                  }

               });
            });
         });
      };
      Util.backgroundExecutor().execute(runnable);
   }

   public Map<Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile pProfile) {
      Property property = Iterables.getFirst(pProfile.getProperties().get("textures"), (Property)null);
      return (Map<Type, MinecraftProfileTexture>)(property == null ? ImmutableMap.of() : this.insecureSkinCache.getUnchecked(property.getValue()));
   }

   @OnlyIn(Dist.CLIENT)
   public interface ISkinAvailableCallback {
      void onSkinTextureAvailable(Type p_onSkinTextureAvailable_1_, ResourceLocation p_onSkinTextureAvailable_2_, MinecraftProfileTexture p_onSkinTextureAvailable_3_);
   }
}