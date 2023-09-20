package com.mojang.blaze3d.vertex;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexBuilderUtils {
   public static IVertexBuilder create(IVertexBuilder pFirst, IVertexBuilder pSecond) {
      return new VertexBuilderUtils.DelegatingVertexBuilder(pFirst, pSecond);
   }

   @OnlyIn(Dist.CLIENT)
   static class DelegatingVertexBuilder implements IVertexBuilder {
      private final IVertexBuilder first;
      private final IVertexBuilder second;

      public DelegatingVertexBuilder(IVertexBuilder pFirst, IVertexBuilder pSecond) {
         if (pFirst == pSecond) {
            throw new IllegalArgumentException("Duplicate delegates");
         } else {
            this.first = pFirst;
            this.second = pSecond;
         }
      }

      public IVertexBuilder vertex(double pX, double pY, double pZ) {
         this.first.vertex(pX, pY, pZ);
         this.second.vertex(pX, pY, pZ);
         return this;
      }

      public IVertexBuilder color(int pRed, int pGreen, int pBlue, int pAlpha) {
         this.first.color(pRed, pGreen, pBlue, pAlpha);
         this.second.color(pRed, pGreen, pBlue, pAlpha);
         return this;
      }

      public IVertexBuilder uv(float pU, float pV) {
         this.first.uv(pU, pV);
         this.second.uv(pU, pV);
         return this;
      }

      public IVertexBuilder overlayCoords(int pU, int pV) {
         this.first.overlayCoords(pU, pV);
         this.second.overlayCoords(pU, pV);
         return this;
      }

      public IVertexBuilder uv2(int pU, int pV) {
         this.first.uv2(pU, pV);
         this.second.uv2(pU, pV);
         return this;
      }

      public IVertexBuilder normal(float pX, float pY, float pZ) {
         this.first.normal(pX, pY, pZ);
         this.second.normal(pX, pY, pZ);
         return this;
      }

      public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
         this.first.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
         this.second.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
      }

      public void endVertex() {
         this.first.endVertex();
         this.second.endVertex();
      }
   }
}