package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.fonts.EmptyGlyph;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.fonts.IGlyph;
import net.minecraft.client.gui.fonts.TexturedGlyph;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.CharacterManager;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextProcessing;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FontRenderer {
   private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
   public final int lineHeight = 9;
   public final Random random = new Random();
   private final Function<ResourceLocation, Font> fonts;
   private final CharacterManager splitter;

   public FontRenderer(Function<ResourceLocation, Font> p_i232249_1_) {
      this.fonts = p_i232249_1_;
      this.splitter = new CharacterManager((p_238404_1_, p_238404_2_) -> {
         return this.getFontSet(p_238404_2_.getFont()).getGlyphInfo(p_238404_1_).getAdvance(p_238404_2_.isBold());
      });
   }

   private Font getFontSet(ResourceLocation pFontLocation) {
      return this.fonts.apply(pFontLocation);
   }

   public int drawShadow(MatrixStack pMatrixStack, String pText, float pX, float pY, int pColor) {
      return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), true, this.isBidirectional());
   }

   public int drawShadow(MatrixStack pMatrixStack, String pText, float pX, float pY, int pColor, boolean pTransparency) {
      RenderSystem.enableAlphaTest();
      return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), true, pTransparency);
   }

   public int draw(MatrixStack pMatrixStack, String pText, float pX, float pY, int pColor) {
      RenderSystem.enableAlphaTest();
      return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), false, this.isBidirectional());
   }

   public int drawShadow(MatrixStack pMatrixStack, IReorderingProcessor pText, float pX, float pY, int pColor) {
      RenderSystem.enableAlphaTest();
      return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), true);
   }

   public int drawShadow(MatrixStack pMatrixStack, ITextComponent pText, float pX, float pY, int pColor) {
      RenderSystem.enableAlphaTest();
      return this.drawInternal(pText.getVisualOrderText(), pX, pY, pColor, pMatrixStack.last().pose(), true);
   }

   public int draw(MatrixStack p_238422_1_, IReorderingProcessor p_238422_2_, float p_238422_3_, float p_238422_4_, int p_238422_5_) {
      RenderSystem.enableAlphaTest();
      return this.drawInternal(p_238422_2_, p_238422_3_, p_238422_4_, p_238422_5_, p_238422_1_.last().pose(), false);
   }

   public int draw(MatrixStack pMatrixStack, ITextComponent pText, float pX, float pY, int pColor) {
      RenderSystem.enableAlphaTest();
      return this.drawInternal(pText.getVisualOrderText(), pX, pY, pColor, pMatrixStack.last().pose(), false);
   }

   /**
    * Apply Unicode Bidirectional Algorithm to string and return a new possibly reordered string for visual rendering.
    */
   public String bidirectionalShaping(String pText) {
      try {
         Bidi bidi = new Bidi((new ArabicShaping(8)).shape(pText), 127);
         bidi.setReorderingMode(0);
         return bidi.writeReordered(2);
      } catch (ArabicShapingException arabicshapingexception) {
         return pText;
      }
   }

   private int drawInternal(String pText, float pX, float pY, int pColor, Matrix4f pMatrix, boolean pDropShadow, boolean pTransparency) {
      if (pText == null) {
         return 0;
      } else {
         IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
         int i = this.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, irendertypebuffer$impl, false, 0, 15728880, pTransparency);
         irendertypebuffer$impl.endBatch();
         return i;
      }
   }

   private int drawInternal(IReorderingProcessor pReorderingProcessor, float pX, float pY, int pColor, Matrix4f pMatrix, boolean pDrawShadow) {
      IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
      int i = this.drawInBatch(pReorderingProcessor, pX, pY, pColor, pDrawShadow, pMatrix, irendertypebuffer$impl, false, 0, 15728880);
      irendertypebuffer$impl.endBatch();
      return i;
   }

   public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, IRenderTypeBuffer pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight) {
      return this.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight, this.isBidirectional());
   }

   public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, IRenderTypeBuffer pBuffer, boolean pTransparency, int pColorBackground, int pPackedLight, boolean pBidiFlag) {
      return this.drawInternal(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparency, pColorBackground, pPackedLight, pBidiFlag);
   }

   public int drawInBatch(ITextComponent p_243247_1_, float p_243247_2_, float p_243247_3_, int p_243247_4_, boolean p_243247_5_, Matrix4f p_243247_6_, IRenderTypeBuffer p_243247_7_, boolean p_243247_8_, int p_243247_9_, int p_243247_10_) {
      return this.drawInBatch(p_243247_1_.getVisualOrderText(), p_243247_2_, p_243247_3_, p_243247_4_, p_243247_5_, p_243247_6_, p_243247_7_, p_243247_8_, p_243247_9_, p_243247_10_);
   }

   public int drawInBatch(IReorderingProcessor pProcessor, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, IRenderTypeBuffer pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight) {
      return this.drawInternal(pProcessor, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight);
   }

   private static int adjustColor(int pColor) {
      return (pColor & -67108864) == 0 ? pColor | -16777216 : pColor;
   }

   private int drawInternal(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, IRenderTypeBuffer pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight, boolean pBidiFlag) {
      if (pBidiFlag) {
         pText = this.bidirectionalShaping(pText);
      }

      pColor = adjustColor(pColor);
      Matrix4f matrix4f = pMatrix.copy();
      if (pDropShadow) {
         this.renderText(pText, pX, pY, pColor, true, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight);
         matrix4f.translate(SHADOW_OFFSET);
      }

      pX = this.renderText(pText, pX, pY, pColor, false, matrix4f, pBuffer, pTransparent, pColorBackground, pPackedLight);
      return (int)pX + (pDropShadow ? 1 : 0);
   }

   private int drawInternal(IReorderingProcessor pProcessor, float pX, float pY, int pColor, boolean pDrawShadow, Matrix4f pMatrix, IRenderTypeBuffer pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight) {
      pColor = adjustColor(pColor);
      Matrix4f matrix4f = pMatrix.copy();
      if (pDrawShadow) {
         this.renderText(pProcessor, pX, pY, pColor, true, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight);
         matrix4f.translate(SHADOW_OFFSET);
      }

      pX = this.renderText(pProcessor, pX, pY, pColor, false, matrix4f, pBuffer, pTransparent, pColorBackground, pPackedLight);
      return (int)pX + (pDrawShadow ? 1 : 0);
   }

   private float renderText(String pText, float pX, float pY, int pColor, boolean pIsShadow, Matrix4f pMatrix, IRenderTypeBuffer pBuffer, boolean pIsTransparent, int pColorBackground, int pPackedLight) {
      FontRenderer.CharacterRenderer fontrenderer$characterrenderer = new FontRenderer.CharacterRenderer(pBuffer, pX, pY, pColor, pIsShadow, pMatrix, pIsTransparent, pPackedLight);
      TextProcessing.iterateFormatted(pText, Style.EMPTY, fontrenderer$characterrenderer);
      return fontrenderer$characterrenderer.finish(pColorBackground, pX);
   }

   private float renderText(IReorderingProcessor p_238426_1_, float p_238426_2_, float p_238426_3_, int p_238426_4_, boolean p_238426_5_, Matrix4f p_238426_6_, IRenderTypeBuffer p_238426_7_, boolean p_238426_8_, int p_238426_9_, int p_238426_10_) {
      FontRenderer.CharacterRenderer fontrenderer$characterrenderer = new FontRenderer.CharacterRenderer(p_238426_7_, p_238426_2_, p_238426_3_, p_238426_4_, p_238426_5_, p_238426_6_, p_238426_8_, p_238426_10_);
      p_238426_1_.accept(fontrenderer$characterrenderer);
      return fontrenderer$characterrenderer.finish(p_238426_9_, p_238426_2_);
   }

   private void renderChar(TexturedGlyph pGlyph, boolean pBold, boolean pItalic, float pBoldOffset, float pX, float pY, Matrix4f pMatrix, IVertexBuilder pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight) {
      pGlyph.render(pItalic, pX, pY, pMatrix, pBuffer, pRed, pGreen, pBlue, pAlpha, pPackedLight);
      if (pBold) {
         pGlyph.render(pItalic, pX + pBoldOffset, pY, pMatrix, pBuffer, pRed, pGreen, pBlue, pAlpha, pPackedLight);
      }

   }

   /**
    * Returns the width of this string. Equivalent of FontMetrics.stringWidth(String s).
    */
   public int width(String pText) {
      return MathHelper.ceil(this.splitter.stringWidth(pText));
   }

   public int width(ITextProperties pProperties) {
      return MathHelper.ceil(this.splitter.stringWidth(pProperties));
   }

   public int width(IReorderingProcessor p_243245_1_) {
      return MathHelper.ceil(this.splitter.stringWidth(p_243245_1_));
   }

   public String plainSubstrByWidth(String pText, int pMaxLength, boolean p_238413_3_) {
      return p_238413_3_ ? this.splitter.plainTailByWidth(pText, pMaxLength, Style.EMPTY) : this.splitter.plainHeadByWidth(pText, pMaxLength, Style.EMPTY);
   }

   public String plainSubstrByWidth(String pText, int pMaxLength) {
      return this.splitter.plainHeadByWidth(pText, pMaxLength, Style.EMPTY);
   }

   public ITextProperties substrByWidth(ITextProperties p_238417_1_, int p_238417_2_) {
      return this.splitter.headByWidth(p_238417_1_, p_238417_2_, Style.EMPTY);
   }

   public void drawWordWrap(ITextProperties p_238418_1_, int p_238418_2_, int p_238418_3_, int p_238418_4_, int p_238418_5_) {
      Matrix4f matrix4f = TransformationMatrix.identity().getMatrix();

      for(IReorderingProcessor ireorderingprocessor : this.split(p_238418_1_, p_238418_4_)) {
         this.drawInternal(ireorderingprocessor, (float)p_238418_2_, (float)p_238418_3_, p_238418_5_, matrix4f, false);
         p_238418_3_ += 9;
      }

   }

   /**
    * Returns the height (in pixels) of the given string if it is wordwrapped to the given max width.
    */
   public int wordWrapHeight(String pStr, int pMaxLength) {
      return 9 * this.splitter.splitLines(pStr, pMaxLength, Style.EMPTY).size();
   }

   public List<IReorderingProcessor> split(ITextProperties p_238425_1_, int p_238425_2_) {
      return LanguageMap.getInstance().getVisualOrder(this.splitter.splitLines(p_238425_1_, p_238425_2_, Style.EMPTY));
   }

   /**
    * Get bidiFlag that controls if the Unicode Bidirectional Algorithm should be run before rendering any string
    */
   public boolean isBidirectional() {
      return LanguageMap.getInstance().isDefaultRightToLeft();
   }

   public CharacterManager getSplitter() {
      return this.splitter;
   }

   @OnlyIn(Dist.CLIENT)
   class CharacterRenderer implements ICharacterConsumer {
      final IRenderTypeBuffer bufferSource;
      private final boolean dropShadow;
      private final float dimFactor;
      private final float r;
      private final float g;
      private final float b;
      private final float a;
      private final Matrix4f pose;
      private final boolean seeThrough;
      private final int packedLightCoords;
      private float x;
      private float y;
      @Nullable
      private List<TexturedGlyph.Effect> effects;

      private void addEffect(TexturedGlyph.Effect pEffect) {
         if (this.effects == null) {
            this.effects = Lists.newArrayList();
         }

         this.effects.add(pEffect);
      }

      public CharacterRenderer(IRenderTypeBuffer p_i232250_2_, float p_i232250_3_, float p_i232250_4_, int p_i232250_5_, boolean p_i232250_6_, Matrix4f p_i232250_7_, boolean p_i232250_8_, int p_i232250_9_) {
         this.bufferSource = p_i232250_2_;
         this.x = p_i232250_3_;
         this.y = p_i232250_4_;
         this.dropShadow = p_i232250_6_;
         this.dimFactor = p_i232250_6_ ? 0.25F : 1.0F;
         this.r = (float)(p_i232250_5_ >> 16 & 255) / 255.0F * this.dimFactor;
         this.g = (float)(p_i232250_5_ >> 8 & 255) / 255.0F * this.dimFactor;
         this.b = (float)(p_i232250_5_ & 255) / 255.0F * this.dimFactor;
         this.a = (float)(p_i232250_5_ >> 24 & 255) / 255.0F;
         this.pose = p_i232250_7_;
         this.seeThrough = p_i232250_8_;
         this.packedLightCoords = p_i232250_9_;
      }

      public boolean accept(int p_accept_1_, Style p_accept_2_, int p_accept_3_) {
         Font font = FontRenderer.this.getFontSet(p_accept_2_.getFont());
         IGlyph iglyph = font.getGlyphInfo(p_accept_3_);
         TexturedGlyph texturedglyph = p_accept_2_.isObfuscated() && p_accept_3_ != 32 ? font.getRandomGlyph(iglyph) : font.getGlyph(p_accept_3_);
         boolean flag = p_accept_2_.isBold();
         float f3 = this.a;
         Color color = p_accept_2_.getColor();
         float f;
         float f1;
         float f2;
         if (color != null) {
            int i = color.getValue();
            f = (float)(i >> 16 & 255) / 255.0F * this.dimFactor;
            f1 = (float)(i >> 8 & 255) / 255.0F * this.dimFactor;
            f2 = (float)(i & 255) / 255.0F * this.dimFactor;
         } else {
            f = this.r;
            f1 = this.g;
            f2 = this.b;
         }

         if (!(texturedglyph instanceof EmptyGlyph)) {
            float f5 = flag ? iglyph.getBoldOffset() : 0.0F;
            float f4 = this.dropShadow ? iglyph.getShadowOffset() : 0.0F;
            IVertexBuilder ivertexbuilder = this.bufferSource.getBuffer(texturedglyph.renderType(this.seeThrough));
            FontRenderer.this.renderChar(texturedglyph, flag, p_accept_2_.isItalic(), f5, this.x + f4, this.y + f4, this.pose, ivertexbuilder, f, f1, f2, f3, this.packedLightCoords);
         }

         float f6 = iglyph.getAdvance(flag);
         float f7 = this.dropShadow ? 1.0F : 0.0F;
         if (p_accept_2_.isStrikethrough()) {
            this.addEffect(new TexturedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, f3));
         }

         if (p_accept_2_.isUnderlined()) {
            this.addEffect(new TexturedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f, f1, f2, f3));
         }

         this.x += f6;
         return true;
      }

      public float finish(int p_238441_1_, float p_238441_2_) {
         if (p_238441_1_ != 0) {
            float f = (float)(p_238441_1_ >> 24 & 255) / 255.0F;
            float f1 = (float)(p_238441_1_ >> 16 & 255) / 255.0F;
            float f2 = (float)(p_238441_1_ >> 8 & 255) / 255.0F;
            float f3 = (float)(p_238441_1_ & 255) / 255.0F;
            this.addEffect(new TexturedGlyph.Effect(p_238441_2_ - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
         }

         if (this.effects != null) {
            TexturedGlyph texturedglyph = FontRenderer.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
            IVertexBuilder ivertexbuilder = this.bufferSource.getBuffer(texturedglyph.renderType(this.seeThrough));

            for(TexturedGlyph.Effect texturedglyph$effect : this.effects) {
               texturedglyph.renderEffect(texturedglyph$effect, this.pose, ivertexbuilder, this.packedLightCoords);
            }
         }

         return this.x;
      }
   }
}