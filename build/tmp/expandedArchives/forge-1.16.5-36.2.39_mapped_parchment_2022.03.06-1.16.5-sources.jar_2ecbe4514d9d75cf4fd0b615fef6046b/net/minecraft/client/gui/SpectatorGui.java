package net.minecraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuRecipient;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.categories.SpectatorDetails;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectatorGui extends AbstractGui implements ISpectatorMenuRecipient {
   private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   public static final ResourceLocation SPECTATOR_LOCATION = new ResourceLocation("textures/gui/spectator_widgets.png");
   private final Minecraft minecraft;
   private long lastSelectionTime;
   private SpectatorMenu menu;

   public SpectatorGui(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void onHotbarSelected(int pSlot) {
      this.lastSelectionTime = Util.getMillis();
      if (this.menu != null) {
         this.menu.selectSlot(pSlot);
      } else {
         this.menu = new SpectatorMenu(this);
      }

   }

   private float getHotbarAlpha() {
      long i = this.lastSelectionTime - Util.getMillis() + 5000L;
      return MathHelper.clamp((float)i / 2000.0F, 0.0F, 1.0F);
   }

   public void renderHotbar(MatrixStack pPoseStack, float p_238528_2_) {
      if (this.menu != null) {
         float f = this.getHotbarAlpha();
         if (f <= 0.0F) {
            this.menu.exit();
         } else {
            int i = this.minecraft.getWindow().getGuiScaledWidth() / 2;
            int j = this.getBlitOffset();
            this.setBlitOffset(-90);
            int k = MathHelper.floor((float)this.minecraft.getWindow().getGuiScaledHeight() - 22.0F * f);
            SpectatorDetails spectatordetails = this.menu.getCurrentPage();
            this.renderPage(pPoseStack, f, i, k, spectatordetails);
            this.setBlitOffset(j);
         }
      }
   }

   protected void renderPage(MatrixStack pPoseStack, float p_238529_2_, int p_238529_3_, int p_238529_4_, SpectatorDetails pSpectatorPage) {
      RenderSystem.enableRescaleNormal();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, p_238529_2_);
      this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
      this.blit(pPoseStack, p_238529_3_ - 91, p_238529_4_, 0, 0, 182, 22);
      if (pSpectatorPage.getSelectedSlot() >= 0) {
         this.blit(pPoseStack, p_238529_3_ - 91 - 1 + pSpectatorPage.getSelectedSlot() * 20, p_238529_4_ - 1, 0, 22, 24, 22);
      }

      for(int i = 0; i < 9; ++i) {
         this.renderSlot(pPoseStack, i, this.minecraft.getWindow().getGuiScaledWidth() / 2 - 90 + i * 20 + 2, (float)(p_238529_4_ + 3), p_238529_2_, pSpectatorPage.getItem(i));
      }

      RenderSystem.disableRescaleNormal();
      RenderSystem.disableBlend();
   }

   private void renderSlot(MatrixStack pPoseStack, int p_238530_2_, int p_238530_3_, float p_238530_4_, float p_238530_5_, ISpectatorMenuObject pSpectatorMenuItem) {
      this.minecraft.getTextureManager().bind(SPECTATOR_LOCATION);
      if (pSpectatorMenuItem != SpectatorMenu.EMPTY_SLOT) {
         int i = (int)(p_238530_5_ * 255.0F);
         RenderSystem.pushMatrix();
         RenderSystem.translatef((float)p_238530_3_, p_238530_4_, 0.0F);
         float f = pSpectatorMenuItem.isEnabled() ? 1.0F : 0.25F;
         RenderSystem.color4f(f, f, f, p_238530_5_);
         pSpectatorMenuItem.renderIcon(pPoseStack, f, i);
         RenderSystem.popMatrix();
         if (i > 3 && pSpectatorMenuItem.isEnabled()) {
            ITextComponent itextcomponent = this.minecraft.options.keyHotbarSlots[p_238530_2_].getTranslatedKeyMessage();
            this.minecraft.font.drawShadow(pPoseStack, itextcomponent, (float)(p_238530_3_ + 19 - 2 - this.minecraft.font.width(itextcomponent)), p_238530_4_ + 6.0F + 3.0F, 16777215 + (i << 24));
         }
      }

   }

   public void renderTooltip(MatrixStack pPoseStack) {
      int i = (int)(this.getHotbarAlpha() * 255.0F);
      if (i > 3 && this.menu != null) {
         ISpectatorMenuObject ispectatormenuobject = this.menu.getSelectedItem();
         ITextComponent itextcomponent = ispectatormenuobject == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : ispectatormenuobject.getName();
         if (itextcomponent != null) {
            int j = (this.minecraft.getWindow().getGuiScaledWidth() - this.minecraft.font.width(itextcomponent)) / 2;
            int k = this.minecraft.getWindow().getGuiScaledHeight() - 35;
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.minecraft.font.drawShadow(pPoseStack, itextcomponent, (float)j, (float)k, 16777215 + (i << 24));
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
         }
      }

   }

   public void onSpectatorMenuClosed(SpectatorMenu pMenu) {
      this.menu = null;
      this.lastSelectionTime = 0L;
   }

   public boolean isMenuActive() {
      return this.menu != null;
   }

   public void onMouseScrolled(double pAmount) {
      int i;
      for(i = this.menu.getSelectedSlot() + (int)pAmount; i >= 0 && i <= 8 && (this.menu.getItem(i) == SpectatorMenu.EMPTY_SLOT || !this.menu.getItem(i).isEnabled()); i = (int)((double)i + pAmount)) {
      }

      if (i >= 0 && i <= 8) {
         this.menu.selectSlot(i);
         this.lastSelectionTime = Util.getMillis();
      }

   }

   public void onMouseMiddleClick() {
      this.lastSelectionTime = Util.getMillis();
      if (this.isMenuActive()) {
         int i = this.menu.getSelectedSlot();
         if (i != -1) {
            this.menu.selectSlot(i);
         }
      } else {
         this.menu = new SpectatorMenu(this);
      }

   }
}