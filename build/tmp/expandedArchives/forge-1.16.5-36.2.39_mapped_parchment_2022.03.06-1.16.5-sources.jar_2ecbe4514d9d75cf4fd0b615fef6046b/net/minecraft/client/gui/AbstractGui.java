package net.minecraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractGui {
   public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
   public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
   public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
   private int blitOffset;

   protected void hLine(MatrixStack pPoseStack, int pMinX, int pMaxX, int pY, int pColor) {
      if (pMaxX < pMinX) {
         int i = pMinX;
         pMinX = pMaxX;
         pMaxX = i;
      }

      fill(pPoseStack, pMinX, pY, pMaxX + 1, pY + 1, pColor);
   }

   protected void vLine(MatrixStack pPoseStack, int pX, int pMinY, int pMaxY, int pColor) {
      if (pMaxY < pMinY) {
         int i = pMinY;
         pMinY = pMaxY;
         pMaxY = i;
      }

      fill(pPoseStack, pX, pMinY + 1, pX + 1, pMaxY, pColor);
   }

   public static void fill(MatrixStack pPoseStack, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor) {
      innerFill(pPoseStack.last().pose(), pMinX, pMinY, pMaxX, pMaxY, pColor);
   }

   private static void innerFill(Matrix4f pMatrix, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor) {
      if (pMinX < pMaxX) {
         int i = pMinX;
         pMinX = pMaxX;
         pMaxX = i;
      }

      if (pMinY < pMaxY) {
         int j = pMinY;
         pMinY = pMaxY;
         pMaxY = j;
      }

      float f3 = (float)(pColor >> 24 & 255) / 255.0F;
      float f = (float)(pColor >> 16 & 255) / 255.0F;
      float f1 = (float)(pColor >> 8 & 255) / 255.0F;
      float f2 = (float)(pColor & 255) / 255.0F;
      BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
      RenderSystem.enableBlend();
      RenderSystem.disableTexture();
      RenderSystem.defaultBlendFunc();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.vertex(pMatrix, (float)pMinX, (float)pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
      bufferbuilder.vertex(pMatrix, (float)pMaxX, (float)pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
      bufferbuilder.vertex(pMatrix, (float)pMaxX, (float)pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
      bufferbuilder.vertex(pMatrix, (float)pMinX, (float)pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
      bufferbuilder.end();
      WorldVertexBufferUploader.end(bufferbuilder);
      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
   }

   protected void fillGradient(MatrixStack pPoseStack, int pX1, int pY1, int pX2, int pY2, int pColorFrom, int pColorTo) {
      RenderSystem.disableTexture();
      RenderSystem.enableBlend();
      RenderSystem.disableAlphaTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuilder();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      fillGradient(pPoseStack.last().pose(), bufferbuilder, pX1, pY1, pX2, pY2, this.blitOffset, pColorFrom, pColorTo);
      tessellator.end();
      RenderSystem.shadeModel(7424);
      RenderSystem.disableBlend();
      RenderSystem.enableAlphaTest();
      RenderSystem.enableTexture();
   }

   protected static void fillGradient(Matrix4f pMatrix, BufferBuilder pBuilder, int pX1, int pY1, int pX2, int pY2, int pBlitOffset, int pColorA, int pColorB) {
      float f = (float)(pColorA >> 24 & 255) / 255.0F;
      float f1 = (float)(pColorA >> 16 & 255) / 255.0F;
      float f2 = (float)(pColorA >> 8 & 255) / 255.0F;
      float f3 = (float)(pColorA & 255) / 255.0F;
      float f4 = (float)(pColorB >> 24 & 255) / 255.0F;
      float f5 = (float)(pColorB >> 16 & 255) / 255.0F;
      float f6 = (float)(pColorB >> 8 & 255) / 255.0F;
      float f7 = (float)(pColorB & 255) / 255.0F;
      pBuilder.vertex(pMatrix, (float)pX2, (float)pY1, (float)pBlitOffset).color(f1, f2, f3, f).endVertex();
      pBuilder.vertex(pMatrix, (float)pX1, (float)pY1, (float)pBlitOffset).color(f1, f2, f3, f).endVertex();
      pBuilder.vertex(pMatrix, (float)pX1, (float)pY2, (float)pBlitOffset).color(f5, f6, f7, f4).endVertex();
      pBuilder.vertex(pMatrix, (float)pX2, (float)pY2, (float)pBlitOffset).color(f5, f6, f7, f4).endVertex();
   }

   public static void drawCenteredString(MatrixStack pPoseStack, FontRenderer pFont, String pText, int pX, int pY, int pColor) {
      pFont.drawShadow(pPoseStack, pText, (float)(pX - pFont.width(pText) / 2), (float)pY, pColor);
   }

   public static void drawCenteredString(MatrixStack pPoseStack, FontRenderer pFont, ITextComponent pText, int pX, int pY, int pColor) {
      IReorderingProcessor ireorderingprocessor = pText.getVisualOrderText();
      pFont.drawShadow(pPoseStack, ireorderingprocessor, (float)(pX - pFont.width(ireorderingprocessor) / 2), (float)pY, pColor);
   }

   public static void drawString(MatrixStack pPoseStack, FontRenderer pFont, String pText, int pX, int pY, int pColor) {
      pFont.drawShadow(pPoseStack, pText, (float)pX, (float)pY, pColor);
   }

   public static void drawString(MatrixStack pPoseStack, FontRenderer pFont, ITextComponent pText, int pX, int pY, int pColor) {
      pFont.drawShadow(pPoseStack, pText, (float)pX, (float)pY, pColor);
   }

   public void blitOutlineBlack(int pWidth, int pHeight, BiConsumer<Integer, Integer> pBoxXYConsumer) {
      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      pBoxXYConsumer.accept(pWidth + 1, pHeight);
      pBoxXYConsumer.accept(pWidth - 1, pHeight);
      pBoxXYConsumer.accept(pWidth, pHeight + 1);
      pBoxXYConsumer.accept(pWidth, pHeight - 1);
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      pBoxXYConsumer.accept(pWidth, pHeight);
   }

   public static void blit(MatrixStack pMatrixStack, int pX, int pY, int pBlitOffset, int pWidth, int pHeight, TextureAtlasSprite pSprite) {
      innerBlit(pMatrixStack.last().pose(), pX, pX + pWidth, pY, pY + pHeight, pBlitOffset, pSprite.getU0(), pSprite.getU1(), pSprite.getV0(), pSprite.getV1());
   }

   public void blit(MatrixStack pMatrixStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
      blit(pMatrixStack, pX, pY, this.blitOffset, (float)pUOffset, (float)pVOffset, pUWidth, pVHeight, 256, 256);
   }

   public static void blit(MatrixStack pMatrixStack, int pX, int pY, int pBlitOffset, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureHeight, int pTextureWidth) {
      innerBlit(pMatrixStack, pX, pX + pUWidth, pY, pY + pVHeight, pBlitOffset, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
   }

   public static void blit(MatrixStack pMatrixStack, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
      innerBlit(pMatrixStack, pX, pX + pWidth, pY, pY + pHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
   }

   public static void blit(MatrixStack pMatrixStack, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
      blit(pMatrixStack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, pTextureWidth, pTextureHeight);
   }

   private static void innerBlit(MatrixStack pMatrixStack, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, int pUWidth, int pVHeight, float pUOffset, float pVOffset, int pTextureWidth, int pTextureHeight) {
      innerBlit(pMatrixStack.last().pose(), pX1, pX2, pY1, pY2, pBlitOffset, (pUOffset + 0.0F) / (float)pTextureWidth, (pUOffset + (float)pUWidth) / (float)pTextureWidth, (pVOffset + 0.0F) / (float)pTextureHeight, (pVOffset + (float)pVHeight) / (float)pTextureHeight);
   }

   private static void innerBlit(Matrix4f pMatrix, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV) {
      BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
      bufferbuilder.vertex(pMatrix, (float)pX1, (float)pY2, (float)pBlitOffset).uv(pMinU, pMaxV).endVertex();
      bufferbuilder.vertex(pMatrix, (float)pX2, (float)pY2, (float)pBlitOffset).uv(pMaxU, pMaxV).endVertex();
      bufferbuilder.vertex(pMatrix, (float)pX2, (float)pY1, (float)pBlitOffset).uv(pMaxU, pMinV).endVertex();
      bufferbuilder.vertex(pMatrix, (float)pX1, (float)pY1, (float)pBlitOffset).uv(pMinU, pMinV).endVertex();
      bufferbuilder.end();
      RenderSystem.enableAlphaTest();
      WorldVertexBufferUploader.end(bufferbuilder);
   }

   public int getBlitOffset() {
      return this.blitOffset;
   }

   public void setBlitOffset(int pValue) {
      this.blitOffset = pValue;
   }
}