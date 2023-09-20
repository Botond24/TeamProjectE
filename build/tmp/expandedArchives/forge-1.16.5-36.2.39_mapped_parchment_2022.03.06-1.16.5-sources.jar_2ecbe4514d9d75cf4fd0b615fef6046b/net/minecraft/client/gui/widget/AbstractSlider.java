package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSlider extends Widget {
   protected double value;

   public AbstractSlider(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, double pValue) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.value = pValue;
   }

   protected int getYImage(boolean pIsHovered) {
      return 0;
   }

   protected IFormattableTextComponent createNarrationMessage() {
      return new TranslationTextComponent("gui.narrate.slider", this.getMessage());
   }

   protected void renderBg(MatrixStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
      pMinecraft.getTextureManager().bind(WIDGETS_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      int i = (this.isHovered() ? 2 : 1) * 20;
      this.blit(pMatrixStack, this.x + (int)(this.value * (double)(this.width - 8)), this.y, 0, 46 + i, 4, 20);
      this.blit(pMatrixStack, this.x + (int)(this.value * (double)(this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
   }

   public void onClick(double pMouseX, double pMouseY) {
      this.setValueFromMouse(pMouseX);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      boolean flag = pKeyCode == 263;
      if (flag || pKeyCode == 262) {
         float f = flag ? -1.0F : 1.0F;
         this.setValue(this.value + (double)(f / (float)(this.width - 8)));
      }

      return false;
   }

   private void setValueFromMouse(double pMouseX) {
      this.setValue((pMouseX - (double)(this.x + 4)) / (double)(this.width - 8));
   }

   private void setValue(double pValue) {
      double d0 = this.value;
      this.value = MathHelper.clamp(pValue, 0.0D, 1.0D);
      if (d0 != this.value) {
         this.applyValue();
      }

      this.updateMessage();
   }

   protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
      this.setValueFromMouse(pMouseX);
      super.onDrag(pMouseX, pMouseY, pDragX, pDragY);
   }

   public void playDownSound(SoundHandler pHandler) {
   }

   public void onRelease(double pMouseX, double pMouseY) {
      super.playDownSound(Minecraft.getInstance().getSoundManager());
   }

   protected abstract void updateMessage();

   protected abstract void applyValue();
}