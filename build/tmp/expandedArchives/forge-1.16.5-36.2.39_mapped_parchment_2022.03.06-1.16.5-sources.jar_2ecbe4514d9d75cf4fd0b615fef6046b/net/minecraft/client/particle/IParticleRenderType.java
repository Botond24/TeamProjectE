package net.minecraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IParticleRenderType {
   IParticleRenderType TERRAIN_SHEET = new IParticleRenderType() {
      public void begin(BufferBuilder pBuilder, TextureManager pTextureManager) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.depthMask(true);
         pTextureManager.bind(AtlasTexture.LOCATION_BLOCKS);
         pBuilder.begin(7, DefaultVertexFormats.PARTICLE);
      }

      public void end(Tessellator pTesselator) {
         pTesselator.end();
      }

      public String toString() {
         return "TERRAIN_SHEET";
      }
   };
   IParticleRenderType PARTICLE_SHEET_OPAQUE = new IParticleRenderType() {
      public void begin(BufferBuilder pBuilder, TextureManager pTextureManager) {
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         pTextureManager.bind(AtlasTexture.LOCATION_PARTICLES);
         pBuilder.begin(7, DefaultVertexFormats.PARTICLE);
      }

      public void end(Tessellator pTesselator) {
         pTesselator.end();
      }

      public String toString() {
         return "PARTICLE_SHEET_OPAQUE";
      }
   };
   IParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new IParticleRenderType() {
      public void begin(BufferBuilder pBuilder, TextureManager pTextureManager) {
         RenderSystem.depthMask(true);
         pTextureManager.bind(AtlasTexture.LOCATION_PARTICLES);
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.alphaFunc(516, 0.003921569F);
         pBuilder.begin(7, DefaultVertexFormats.PARTICLE);
      }

      public void end(Tessellator pTesselator) {
         pTesselator.end();
      }

      public String toString() {
         return "PARTICLE_SHEET_TRANSLUCENT";
      }
   };
   IParticleRenderType PARTICLE_SHEET_LIT = new IParticleRenderType() {
      public void begin(BufferBuilder pBuilder, TextureManager pTextureManager) {
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         pTextureManager.bind(AtlasTexture.LOCATION_PARTICLES);
         pBuilder.begin(7, DefaultVertexFormats.PARTICLE);
      }

      public void end(Tessellator pTesselator) {
         pTesselator.end();
      }

      public String toString() {
         return "PARTICLE_SHEET_LIT";
      }
   };
   IParticleRenderType CUSTOM = new IParticleRenderType() {
      public void begin(BufferBuilder pBuilder, TextureManager pTextureManager) {
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
      }

      public void end(Tessellator pTesselator) {
      }

      public String toString() {
         return "CUSTOM";
      }
   };
   IParticleRenderType NO_RENDER = new IParticleRenderType() {
      public void begin(BufferBuilder pBuilder, TextureManager pTextureManager) {
      }

      public void end(Tessellator pTesselator) {
      }

      public String toString() {
         return "NO_RENDER";
      }
   };

   void begin(BufferBuilder pBuilder, TextureManager pTextureManager);

   void end(Tessellator pTesselator);
}