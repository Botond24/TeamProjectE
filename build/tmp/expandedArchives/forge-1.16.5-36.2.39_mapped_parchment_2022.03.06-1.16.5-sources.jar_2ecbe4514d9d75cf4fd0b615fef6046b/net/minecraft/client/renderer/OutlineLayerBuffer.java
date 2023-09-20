package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.DefaultColorVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import java.util.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutlineLayerBuffer implements IRenderTypeBuffer {
   private final IRenderTypeBuffer.Impl bufferSource;
   private final IRenderTypeBuffer.Impl outlineBufferSource = IRenderTypeBuffer.immediate(new BufferBuilder(256));
   private int teamR = 255;
   private int teamG = 255;
   private int teamB = 255;
   private int teamA = 255;

   public OutlineLayerBuffer(IRenderTypeBuffer.Impl p_i225970_1_) {
      this.bufferSource = p_i225970_1_;
   }

   public IVertexBuilder getBuffer(RenderType p_getBuffer_1_) {
      if (p_getBuffer_1_.isOutline()) {
         IVertexBuilder ivertexbuilder2 = this.outlineBufferSource.getBuffer(p_getBuffer_1_);
         return new OutlineLayerBuffer.ColoredOutline(ivertexbuilder2, this.teamR, this.teamG, this.teamB, this.teamA);
      } else {
         IVertexBuilder ivertexbuilder = this.bufferSource.getBuffer(p_getBuffer_1_);
         Optional<RenderType> optional = p_getBuffer_1_.outline();
         if (optional.isPresent()) {
            IVertexBuilder ivertexbuilder1 = this.outlineBufferSource.getBuffer(optional.get());
            OutlineLayerBuffer.ColoredOutline outlinelayerbuffer$coloredoutline = new OutlineLayerBuffer.ColoredOutline(ivertexbuilder1, this.teamR, this.teamG, this.teamB, this.teamA);
            return VertexBuilderUtils.create(outlinelayerbuffer$coloredoutline, ivertexbuilder);
         } else {
            return ivertexbuilder;
         }
      }
   }

   public void setColor(int pRed, int pGreen, int pBlue, int pAlpha) {
      this.teamR = pRed;
      this.teamG = pGreen;
      this.teamB = pBlue;
      this.teamA = pAlpha;
   }

   public void endOutlineBatch() {
      this.outlineBufferSource.endBatch();
   }

   @OnlyIn(Dist.CLIENT)
   static class ColoredOutline extends DefaultColorVertexBuilder {
      private final IVertexBuilder delegate;
      private double x;
      private double y;
      private double z;
      private float u;
      private float v;

      private ColoredOutline(IVertexBuilder p_i225971_1_, int p_i225971_2_, int p_i225971_3_, int p_i225971_4_, int p_i225971_5_) {
         this.delegate = p_i225971_1_;
         super.defaultColor(p_i225971_2_, p_i225971_3_, p_i225971_4_, p_i225971_5_);
      }

      public void defaultColor(int p_225611_1_, int p_225611_2_, int p_225611_3_, int p_225611_4_) {
      }

      public IVertexBuilder vertex(double pX, double pY, double pZ) {
         this.x = pX;
         this.y = pY;
         this.z = pZ;
         return this;
      }

      public IVertexBuilder color(int pRed, int pGreen, int pBlue, int pAlpha) {
         return this;
      }

      public IVertexBuilder uv(float pU, float pV) {
         this.u = pU;
         this.v = pV;
         return this;
      }

      public IVertexBuilder overlayCoords(int pU, int pV) {
         return this;
      }

      public IVertexBuilder uv2(int pU, int pV) {
         return this;
      }

      public IVertexBuilder normal(float pX, float pY, float pZ) {
         return this;
      }

      public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
         this.delegate.vertex((double)pX, (double)pY, (double)pZ).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(pTexU, pTexV).endVertex();
      }

      public void endVertex() {
         this.delegate.vertex(this.x, this.y, this.z).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(this.u, this.v).endVertex();
      }
   }
}