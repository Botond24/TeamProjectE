package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Widget extends AbstractGui implements IRenderable, IGuiEventListener {
   public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   protected int width;
   protected int height;
   public int x;
   public int y;
   private ITextComponent message;
   private boolean wasHovered;
   protected boolean isHovered;
   public boolean active = true;
   public boolean visible = true;
   protected float alpha = 1.0F;
   protected long nextNarration = Long.MAX_VALUE;
   private boolean focused;

   public Widget(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage) {
      this.x = pX;
      this.y = pY;
      this.width = pWidth;
      this.height = pHeight;
      this.message = pMessage;
   }

   public int getHeight() {
      return this.height;
   }

   protected int getYImage(boolean pIsHovered) {
      int i = 1;
      if (!this.active) {
         i = 0;
      } else if (pIsHovered) {
         i = 2;
      }

      return i;
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      if (this.visible) {
         this.isHovered = pMouseX >= this.x && pMouseY >= this.y && pMouseX < this.x + this.width && pMouseY < this.y + this.height;
         if (this.wasHovered != this.isHovered()) {
            if (this.isHovered()) {
               if (this.focused) {
                  this.queueNarration(200);
               } else {
                  this.queueNarration(750);
               }
            } else {
               this.nextNarration = Long.MAX_VALUE;
            }
         }

         if (this.visible) {
            this.renderButton(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         }

         this.narrate();
         this.wasHovered = this.isHovered();
      }
   }

   protected void narrate() {
      if (this.active && this.isHovered() && Util.getMillis() > this.nextNarration) {
         String s = this.createNarrationMessage().getString();
         if (!s.isEmpty()) {
            NarratorChatListener.INSTANCE.sayNow(s);
            this.nextNarration = Long.MAX_VALUE;
         }
      }

   }

   protected IFormattableTextComponent createNarrationMessage() {
      return new TranslationTextComponent("gui.narrate.button", this.getMessage());
   }

   public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      Minecraft minecraft = Minecraft.getInstance();
      FontRenderer fontrenderer = minecraft.font;
      minecraft.getTextureManager().bind(WIDGETS_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
      int i = this.getYImage(this.isHovered());
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      this.blit(pMatrixStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
      this.blit(pMatrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
      this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
      int j = getFGColor();
      drawCenteredString(pMatrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
   }

   protected void renderBg(MatrixStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
   }

   public void onClick(double pMouseX, double pMouseY) {
   }

   public void onRelease(double pMouseX, double pMouseY) {
   }

   protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.active && this.visible) {
         if (this.isValidClickButton(pButton)) {
            boolean flag = this.clicked(pMouseX, pMouseY);
            if (flag) {
               this.playDownSound(Minecraft.getInstance().getSoundManager());
               this.onClick(pMouseX, pMouseY);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      if (this.isValidClickButton(pButton)) {
         this.onRelease(pMouseX, pMouseY);
         return true;
      } else {
         return false;
      }
   }

   protected boolean isValidClickButton(int pButton) {
      return pButton == 0;
   }

   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (this.isValidClickButton(pButton)) {
         this.onDrag(pMouseX, pMouseY, pDragX, pDragY);
         return true;
      } else {
         return false;
      }
   }

   protected boolean clicked(double pMouseX, double pMouseY) {
      return this.active && this.visible && pMouseX >= (double)this.x && pMouseY >= (double)this.y && pMouseX < (double)(this.x + this.width) && pMouseY < (double)(this.y + this.height);
   }

   public boolean isHovered() {
      return this.isHovered || this.focused;
   }

   public boolean changeFocus(boolean pFocus) {
      if (this.active && this.visible) {
         this.focused = !this.focused;
         this.onFocusedChanged(this.focused);
         return this.focused;
      } else {
         return false;
      }
   }

   protected void onFocusedChanged(boolean pFocused) {
   }

   public boolean isMouseOver(double pMouseX, double pMouseY) {
      return this.active && this.visible && pMouseX >= (double)this.x && pMouseY >= (double)this.y && pMouseX < (double)(this.x + this.width) && pMouseY < (double)(this.y + this.height);
   }

   public void renderToolTip(MatrixStack pPoseStack, int pMouseX, int pMouseY) {
   }

   public void playDownSound(SoundHandler pHandler) {
      pHandler.play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int pWidth) {
      this.width = pWidth;
   }

   public void setHeight(int value) {
      this.height = value;
   }

   public void setAlpha(float pAlpha) {
      this.alpha = pAlpha;
   }

   public void setMessage(ITextComponent pMessage) {
      if (!Objects.equals(pMessage.getString(), this.message.getString())) {
         this.queueNarration(250);
      }

      this.message = pMessage;
   }

   public void queueNarration(int p_230994_1_) {
      this.nextNarration = Util.getMillis() + (long)p_230994_1_;
   }

   public ITextComponent getMessage() {
      return this.message;
   }

   public boolean isFocused() {
      return this.focused;
   }

   protected void setFocused(boolean pFocused) {
      this.focused = pFocused;
   }

   public static final int UNSET_FG_COLOR = -1;
   protected int packedFGColor = UNSET_FG_COLOR;
   public int getFGColor() {
      if (packedFGColor != UNSET_FG_COLOR) return packedFGColor;
      return this.active ? 16777215 : 10526880; // White : Light Grey
   }
   public void setFGColor(int color) {
      this.packedFGColor = color;
   }
   public void clearFGColor() {
      this.packedFGColor = UNSET_FG_COLOR;
   }
}
