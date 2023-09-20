package net.minecraft.client.gui.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementTabGui extends AbstractGui {
   private final Minecraft minecraft;
   private final AdvancementsScreen screen;
   private final AdvancementTabType type;
   private final int index;
   private final Advancement advancement;
   private final DisplayInfo display;
   private final ItemStack icon;
   private final ITextComponent title;
   private final AdvancementEntryGui root;
   private final Map<Advancement, AdvancementEntryGui> widgets = Maps.newLinkedHashMap();
   private double scrollX;
   private double scrollY;
   private int minX = Integer.MAX_VALUE;
   private int minY = Integer.MAX_VALUE;
   private int maxX = Integer.MIN_VALUE;
   private int maxY = Integer.MIN_VALUE;
   private float fade;
   private boolean centered;
   private int page;

   public AdvancementTabGui(Minecraft p_i47589_1_, AdvancementsScreen p_i47589_2_, AdvancementTabType p_i47589_3_, int p_i47589_4_, Advancement p_i47589_5_, DisplayInfo p_i47589_6_) {
      this.minecraft = p_i47589_1_;
      this.screen = p_i47589_2_;
      this.type = p_i47589_3_;
      this.index = p_i47589_4_;
      this.advancement = p_i47589_5_;
      this.display = p_i47589_6_;
      this.icon = p_i47589_6_.getIcon();
      this.title = p_i47589_6_.getTitle();
      this.root = new AdvancementEntryGui(this, p_i47589_1_, p_i47589_5_, p_i47589_6_);
      this.addWidget(this.root, p_i47589_5_);
   }

   public AdvancementTabGui(Minecraft mc, AdvancementsScreen screen, AdvancementTabType type, int index, int page, Advancement adv, DisplayInfo info) {
      this(mc, screen, type, index, adv, info);
      this.page = page;
   }

   public int getPage() {
      return page;
   }

   public Advancement getAdvancement() {
      return this.advancement;
   }

   public ITextComponent getTitle() {
      return this.title;
   }

   public void drawTab(MatrixStack pMatrixStack, int pOffsetX, int pOffsetY, boolean pIsSelected) {
      this.type.draw(pMatrixStack, this, pOffsetX, pOffsetY, pIsSelected, this.index);
   }

   public void drawIcon(int pOffsetX, int pOffsetY, ItemRenderer pRenderer) {
      this.type.drawIcon(pOffsetX, pOffsetY, this.index, pRenderer, this.icon);
   }

   public void drawContents(MatrixStack pMatrixStack) {
      if (!this.centered) {
         this.scrollX = (double)(117 - (this.maxX + this.minX) / 2);
         this.scrollY = (double)(56 - (this.maxY + this.minY) / 2);
         this.centered = true;
      }

      RenderSystem.pushMatrix();
      RenderSystem.enableDepthTest();
      RenderSystem.translatef(0.0F, 0.0F, 950.0F);
      RenderSystem.colorMask(false, false, false, false);
      fill(pMatrixStack, 4680, 2260, -4680, -2260, -16777216);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.translatef(0.0F, 0.0F, -950.0F);
      RenderSystem.depthFunc(518);
      fill(pMatrixStack, 234, 113, 0, 0, -16777216);
      RenderSystem.depthFunc(515);
      ResourceLocation resourcelocation = this.display.getBackground();
      if (resourcelocation != null) {
         this.minecraft.getTextureManager().bind(resourcelocation);
      } else {
         this.minecraft.getTextureManager().bind(TextureManager.INTENTIONAL_MISSING_TEXTURE);
      }

      int i = MathHelper.floor(this.scrollX);
      int j = MathHelper.floor(this.scrollY);
      int k = i % 16;
      int l = j % 16;

      for(int i1 = -1; i1 <= 15; ++i1) {
         for(int j1 = -1; j1 <= 8; ++j1) {
            blit(pMatrixStack, k + 16 * i1, l + 16 * j1, 0.0F, 0.0F, 16, 16, 16, 16);
         }
      }

      this.root.drawConnectivity(pMatrixStack, i, j, true);
      this.root.drawConnectivity(pMatrixStack, i, j, false);
      this.root.draw(pMatrixStack, i, j);
      RenderSystem.depthFunc(518);
      RenderSystem.translatef(0.0F, 0.0F, -950.0F);
      RenderSystem.colorMask(false, false, false, false);
      fill(pMatrixStack, 4680, 2260, -4680, -2260, -16777216);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.translatef(0.0F, 0.0F, 950.0F);
      RenderSystem.depthFunc(515);
      RenderSystem.popMatrix();
   }

   public void drawTooltips(MatrixStack pMatrixStack, int pMouseX, int pMouseY, int pWidth, int pHeight) {
      RenderSystem.pushMatrix();
      RenderSystem.translatef(0.0F, 0.0F, 200.0F);
      fill(pMatrixStack, 0, 0, 234, 113, MathHelper.floor(this.fade * 255.0F) << 24);
      boolean flag = false;
      int i = MathHelper.floor(this.scrollX);
      int j = MathHelper.floor(this.scrollY);
      if (pMouseX > 0 && pMouseX < 234 && pMouseY > 0 && pMouseY < 113) {
         for(AdvancementEntryGui advancemententrygui : this.widgets.values()) {
            if (advancemententrygui.isMouseOver(i, j, pMouseX, pMouseY)) {
               flag = true;
               advancemententrygui.drawHover(pMatrixStack, i, j, this.fade, pWidth, pHeight);
               break;
            }
         }
      }

      RenderSystem.popMatrix();
      if (flag) {
         this.fade = MathHelper.clamp(this.fade + 0.02F, 0.0F, 0.3F);
      } else {
         this.fade = MathHelper.clamp(this.fade - 0.04F, 0.0F, 1.0F);
      }

   }

   public boolean isMouseOver(int pOffsetX, int pOffsetY, double pMouseX, double pMouseY) {
      return this.type.isMouseOver(pOffsetX, pOffsetY, this.index, pMouseX, pMouseY);
   }

   @Nullable
   public static AdvancementTabGui create(Minecraft pMinecraft, AdvancementsScreen pScreen, int pTabIndex, Advancement pAdvancement) {
      if (pAdvancement.getDisplay() == null) {
         return null;
      } else {
         for(AdvancementTabType advancementtabtype : AdvancementTabType.values()) {
            if ((pTabIndex % AdvancementTabType.MAX_TABS) < advancementtabtype.getMax()) {
               return new AdvancementTabGui(pMinecraft, pScreen, advancementtabtype, pTabIndex % AdvancementTabType.MAX_TABS, pTabIndex / AdvancementTabType.MAX_TABS, pAdvancement, pAdvancement.getDisplay());
            }

            pTabIndex -= advancementtabtype.getMax();
         }

         return null;
      }
   }

   public void scroll(double pDragX, double pDragY) {
      if (this.maxX - this.minX > 234) {
         this.scrollX = MathHelper.clamp(this.scrollX + pDragX, (double)(-(this.maxX - 234)), 0.0D);
      }

      if (this.maxY - this.minY > 113) {
         this.scrollY = MathHelper.clamp(this.scrollY + pDragY, (double)(-(this.maxY - 113)), 0.0D);
      }

   }

   public void addAdvancement(Advancement pAdvancement) {
      if (pAdvancement.getDisplay() != null) {
         AdvancementEntryGui advancemententrygui = new AdvancementEntryGui(this, this.minecraft, pAdvancement, pAdvancement.getDisplay());
         this.addWidget(advancemententrygui, pAdvancement);
      }
   }

   private void addWidget(AdvancementEntryGui pGui, Advancement pAdvancement) {
      this.widgets.put(pAdvancement, pGui);
      int i = pGui.getX();
      int j = i + 28;
      int k = pGui.getY();
      int l = k + 27;
      this.minX = Math.min(this.minX, i);
      this.maxX = Math.max(this.maxX, j);
      this.minY = Math.min(this.minY, k);
      this.maxY = Math.max(this.maxY, l);

      for(AdvancementEntryGui advancemententrygui : this.widgets.values()) {
         advancemententrygui.attachToParent();
      }

   }

   @Nullable
   public AdvancementEntryGui getWidget(Advancement pAdvancement) {
      return this.widgets.get(pAdvancement);
   }

   public AdvancementsScreen getScreen() {
      return this.screen;
   }
}
