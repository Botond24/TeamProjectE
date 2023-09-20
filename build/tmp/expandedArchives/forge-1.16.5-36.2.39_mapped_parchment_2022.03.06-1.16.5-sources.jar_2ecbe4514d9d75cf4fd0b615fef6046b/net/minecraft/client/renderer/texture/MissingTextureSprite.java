package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MissingTextureSprite extends TextureAtlasSprite {
   private static final ResourceLocation MISSING_TEXTURE_LOCATION = new ResourceLocation("missingno");
   @Nullable
   private static DynamicTexture missingTexture;
   private static final LazyValue<NativeImage> MISSING_IMAGE_DATA = new LazyValue<>(() -> {
      NativeImage nativeimage = new NativeImage(16, 16, false);
      int i = -16777216;
      int j = -524040;

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            if (k < 8 ^ l < 8) {
               nativeimage.setPixelRGBA(l, k, -524040);
            } else {
               nativeimage.setPixelRGBA(l, k, -16777216);
            }
         }
      }

      nativeimage.untrack();
      return nativeimage;
   });
   private static final TextureAtlasSprite.Info INFO = new TextureAtlasSprite.Info(MISSING_TEXTURE_LOCATION, 16, 16, new AnimationMetadataSection(Lists.newArrayList(new AnimationFrame(0, -1)), 16, 16, 1, false));

   private MissingTextureSprite(AtlasTexture pAtlas, int p_i226044_2_, int p_i226044_3_, int p_i226044_4_, int pX, int pY) {
      super(pAtlas, INFO, p_i226044_2_, p_i226044_3_, p_i226044_4_, pX, pY, MISSING_IMAGE_DATA.get());
   }

   public static MissingTextureSprite newInstance(AtlasTexture pAtlasTexture, int pMipmapLevel, int pAtlasWidth, int pAtlasHeight, int pX, int pY) {
      return new MissingTextureSprite(pAtlasTexture, pMipmapLevel, pAtlasWidth, pAtlasHeight, pX, pY);
   }

   public static ResourceLocation getLocation() {
      return MISSING_TEXTURE_LOCATION;
   }

   public static TextureAtlasSprite.Info info() {
      return INFO;
   }

   public void close() {
      for(int i = 1; i < this.mainImage.length; ++i) {
         this.mainImage[i].close();
      }

   }

   public static DynamicTexture getTexture() {
      if (missingTexture == null) {
         missingTexture = new DynamicTexture(MISSING_IMAGE_DATA.get());
         Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
      }

      return missingTexture;
   }
}