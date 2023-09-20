package net.minecraft.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CheckboxButton extends AbstractButton {
   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
   private boolean selected;
   private final boolean showLabel;

   public CheckboxButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, boolean pSelected) {
      this(pX, pY, pWidth, pHeight, pMessage, pSelected, true);
   }

   public CheckboxButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, boolean pSelected, boolean pShowLabel) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.selected = pSelected;
      this.showLabel = pShowLabel;
   }

   public void onPress() {
      this.selected = !this.selected;
   }

   public boolean selected() {
      return this.selected;
   }

   public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getTextureManager().bind(TEXTURE);
      RenderSystem.enableDepthTest();
      FontRenderer fontrenderer = minecraft.font;
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      blit(pMatrixStack, this.x, this.y, this.isFocused() ? 20.0F : 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 64, 64);
      this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
      if (this.showLabel) {
         drawString(pMatrixStack, fontrenderer, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24);
      }

   }
}