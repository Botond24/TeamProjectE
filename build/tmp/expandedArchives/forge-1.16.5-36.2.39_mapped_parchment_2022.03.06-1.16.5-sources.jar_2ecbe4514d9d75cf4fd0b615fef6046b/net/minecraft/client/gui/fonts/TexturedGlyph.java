package net.minecraft.client.gui.fonts;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TexturedGlyph {
   private final RenderType normalType;
   private final RenderType seeThroughType;
   private final float u0;
   private final float u1;
   private final float v0;
   private final float v1;
   private final float left;
   private final float right;
   private final float up;
   private final float down;

   public TexturedGlyph(RenderType p_i225922_1_, RenderType p_i225922_2_, float p_i225922_3_, float p_i225922_4_, float p_i225922_5_, float p_i225922_6_, float p_i225922_7_, float p_i225922_8_, float p_i225922_9_, float p_i225922_10_) {
      this.normalType = p_i225922_1_;
      this.seeThroughType = p_i225922_2_;
      this.u0 = p_i225922_3_;
      this.u1 = p_i225922_4_;
      this.v0 = p_i225922_5_;
      this.v1 = p_i225922_6_;
      this.left = p_i225922_7_;
      this.right = p_i225922_8_;
      this.up = p_i225922_9_;
      this.down = p_i225922_10_;
   }

   public void render(boolean pItalic, float pX, float pY, Matrix4f pMatrix, IVertexBuilder pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight) {
      int i = 3;
      float f = pX + this.left;
      float f1 = pX + this.right;
      float f2 = this.up - 3.0F;
      float f3 = this.down - 3.0F;
      float f4 = pY + f2;
      float f5 = pY + f3;
      float f6 = pItalic ? 1.0F - 0.25F * f2 : 0.0F;
      float f7 = pItalic ? 1.0F - 0.25F * f3 : 0.0F;
      pBuffer.vertex(pMatrix, f + f6, f4, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u0, this.v0).uv2(pPackedLight).endVertex();
      pBuffer.vertex(pMatrix, f + f7, f5, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u0, this.v1).uv2(pPackedLight).endVertex();
      pBuffer.vertex(pMatrix, f1 + f7, f5, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u1, this.v1).uv2(pPackedLight).endVertex();
      pBuffer.vertex(pMatrix, f1 + f6, f4, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u1, this.v0).uv2(pPackedLight).endVertex();
   }

   public void renderEffect(TexturedGlyph.Effect pEffect, Matrix4f pMatrix, IVertexBuilder pBuffer, int pPackedLight) {
      pBuffer.vertex(pMatrix, pEffect.x0, pEffect.y0, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u0, this.v0).uv2(pPackedLight).endVertex();
      pBuffer.vertex(pMatrix, pEffect.x1, pEffect.y0, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u0, this.v1).uv2(pPackedLight).endVertex();
      pBuffer.vertex(pMatrix, pEffect.x1, pEffect.y1, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u1, this.v1).uv2(pPackedLight).endVertex();
      pBuffer.vertex(pMatrix, pEffect.x0, pEffect.y1, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u1, this.v0).uv2(pPackedLight).endVertex();
   }

   public RenderType renderType(boolean p_228163_1_) {
      return p_228163_1_ ? this.seeThroughType : this.normalType;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Effect {
      protected final float x0;
      protected final float y0;
      protected final float x1;
      protected final float y1;
      protected final float depth;
      protected final float r;
      protected final float g;
      protected final float b;
      protected final float a;

      public Effect(float p_i225923_1_, float p_i225923_2_, float p_i225923_3_, float p_i225923_4_, float p_i225923_5_, float p_i225923_6_, float p_i225923_7_, float p_i225923_8_, float p_i225923_9_) {
         this.x0 = p_i225923_1_;
         this.y0 = p_i225923_2_;
         this.x1 = p_i225923_3_;
         this.y1 = p_i225923_4_;
         this.depth = p_i225923_5_;
         this.r = p_i225923_6_;
         this.g = p_i225923_7_;
         this.b = p_i225923_8_;
         this.a = p_i225923_9_;
      }
   }
}