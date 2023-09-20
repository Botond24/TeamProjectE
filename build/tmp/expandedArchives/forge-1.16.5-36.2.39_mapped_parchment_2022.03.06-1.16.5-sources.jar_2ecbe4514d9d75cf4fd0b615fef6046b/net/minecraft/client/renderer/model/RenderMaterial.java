package net.minecraft.client.renderer.model;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderMaterial {
   private final ResourceLocation atlasLocation;
   private final ResourceLocation texture;
   @Nullable
   private RenderType renderType;

   public RenderMaterial(ResourceLocation p_i226055_1_, ResourceLocation p_i226055_2_) {
      this.atlasLocation = p_i226055_1_;
      this.texture = p_i226055_2_;
   }

   public ResourceLocation atlasLocation() {
      return this.atlasLocation;
   }

   public ResourceLocation texture() {
      return this.texture;
   }

   public TextureAtlasSprite sprite() {
      return Minecraft.getInstance().getTextureAtlas(this.atlasLocation()).apply(this.texture());
   }

   public RenderType renderType(Function<ResourceLocation, RenderType> pRenderTypeGetter) {
      if (this.renderType == null) {
         this.renderType = pRenderTypeGetter.apply(this.atlasLocation);
      }

      return this.renderType;
   }

   public IVertexBuilder buffer(IRenderTypeBuffer pBuffer, Function<ResourceLocation, RenderType> pRenderTypeGetter) {
      return this.sprite().wrap(pBuffer.getBuffer(this.renderType(pRenderTypeGetter)));
   }

   public IVertexBuilder buffer(IRenderTypeBuffer pBuffer, Function<ResourceLocation, RenderType> pRenderTypeGetter, boolean pWithGlint) {
      return this.sprite().wrap(ItemRenderer.getFoilBufferDirect(pBuffer, this.renderType(pRenderTypeGetter), true, pWithGlint));
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         RenderMaterial rendermaterial = (RenderMaterial)p_equals_1_;
         return this.atlasLocation.equals(rendermaterial.atlasLocation) && this.texture.equals(rendermaterial.texture);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.atlasLocation, this.texture);
   }

   public String toString() {
      return "Material{atlasLocation=" + this.atlasLocation + ", texture=" + this.texture + '}';
   }
}