package net.minecraft.client.gui.advancements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.CharacterManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementEntryGui extends AbstractGui {
   private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
   private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
   private final AdvancementTabGui tab;
   private final Advancement advancement;
   private final DisplayInfo display;
   private final IReorderingProcessor title;
   private final int width;
   private final List<IReorderingProcessor> description;
   private final Minecraft minecraft;
   private AdvancementEntryGui parent;
   private final List<AdvancementEntryGui> children = Lists.newArrayList();
   private AdvancementProgress progress;
   private final int x;
   private final int y;

   public AdvancementEntryGui(AdvancementTabGui p_i47385_1_, Minecraft p_i47385_2_, Advancement p_i47385_3_, DisplayInfo p_i47385_4_) {
      this.tab = p_i47385_1_;
      this.advancement = p_i47385_3_;
      this.display = p_i47385_4_;
      this.minecraft = p_i47385_2_;
      this.title = LanguageMap.getInstance().getVisualOrder(p_i47385_2_.font.substrByWidth(p_i47385_4_.getTitle(), 163));
      this.x = MathHelper.floor(p_i47385_4_.getX() * 28.0F);
      this.y = MathHelper.floor(p_i47385_4_.getY() * 27.0F);
      int i = p_i47385_3_.getMaxCriteraRequired();
      int j = String.valueOf(i).length();
      int k = i > 1 ? p_i47385_2_.font.width("  ") + p_i47385_2_.font.width("0") * j * 2 + p_i47385_2_.font.width("/") : 0;
      int l = 29 + p_i47385_2_.font.width(this.title) + k;
      this.description = LanguageMap.getInstance().getVisualOrder(this.findOptimalLines(TextComponentUtils.mergeStyles(p_i47385_4_.getDescription().copy(), Style.EMPTY.withColor(p_i47385_4_.getFrame().getChatColor())), l));

      for(IReorderingProcessor ireorderingprocessor : this.description) {
         l = Math.max(l, p_i47385_2_.font.width(ireorderingprocessor));
      }

      this.width = l + 3 + 5;
   }

   private static float getMaxWidth(CharacterManager pManager, List<ITextProperties> pText) {
      return (float)pText.stream().mapToDouble(pManager::stringWidth).max().orElse(0.0D);
   }

   private List<ITextProperties> findOptimalLines(ITextComponent pComponent, int pMaxWidth) {
      CharacterManager charactermanager = this.minecraft.font.getSplitter();
      List<ITextProperties> list = null;
      float f = Float.MAX_VALUE;

      for(int i : TEST_SPLIT_OFFSETS) {
         List<ITextProperties> list1 = charactermanager.splitLines(pComponent, pMaxWidth - i, Style.EMPTY);
         float f1 = Math.abs(getMaxWidth(charactermanager, list1) - (float)pMaxWidth);
         if (f1 <= 10.0F) {
            return list1;
         }

         if (f1 < f) {
            f = f1;
            list = list1;
         }
      }

      return list;
   }

   @Nullable
   private AdvancementEntryGui getFirstVisibleParent(Advancement pAdvancement) {
      do {
         pAdvancement = pAdvancement.getParent();
      } while(pAdvancement != null && pAdvancement.getDisplay() == null);

      return pAdvancement != null && pAdvancement.getDisplay() != null ? this.tab.getWidget(pAdvancement) : null;
   }

   public void drawConnectivity(MatrixStack pMatrixStack, int pX, int pY, boolean pDropShadow) {
      if (this.parent != null) {
         int i = pX + this.parent.x + 13;
         int j = pX + this.parent.x + 26 + 4;
         int k = pY + this.parent.y + 13;
         int l = pX + this.x + 13;
         int i1 = pY + this.y + 13;
         int j1 = pDropShadow ? -16777216 : -1;
         if (pDropShadow) {
            this.hLine(pMatrixStack, j, i, k - 1, j1);
            this.hLine(pMatrixStack, j + 1, i, k, j1);
            this.hLine(pMatrixStack, j, i, k + 1, j1);
            this.hLine(pMatrixStack, l, j - 1, i1 - 1, j1);
            this.hLine(pMatrixStack, l, j - 1, i1, j1);
            this.hLine(pMatrixStack, l, j - 1, i1 + 1, j1);
            this.vLine(pMatrixStack, j - 1, i1, k, j1);
            this.vLine(pMatrixStack, j + 1, i1, k, j1);
         } else {
            this.hLine(pMatrixStack, j, i, k, j1);
            this.hLine(pMatrixStack, l, j, i1, j1);
            this.vLine(pMatrixStack, j, i1, k, j1);
         }
      }

      for(AdvancementEntryGui advancemententrygui : this.children) {
         advancemententrygui.drawConnectivity(pMatrixStack, pX, pY, pDropShadow);
      }

   }

   public void draw(MatrixStack pMatrixStack, int pX, int pY) {
      if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
         float f = this.progress == null ? 0.0F : this.progress.getPercent();
         AdvancementState advancementstate;
         if (f >= 1.0F) {
            advancementstate = AdvancementState.OBTAINED;
         } else {
            advancementstate = AdvancementState.UNOBTAINED;
         }

         this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
         this.blit(pMatrixStack, pX + this.x + 3, pY + this.y, this.display.getFrame().getTexture(), 128 + advancementstate.getIndex() * 26, 26, 26);
         this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), pX + this.x + 8, pY + this.y + 5);
      }

      for(AdvancementEntryGui advancemententrygui : this.children) {
         advancemententrygui.draw(pMatrixStack, pX, pY);
      }

   }

   public void setProgress(AdvancementProgress pAdvancementProgress) {
      this.progress = pAdvancementProgress;
   }

   public void addChild(AdvancementEntryGui pGuiAdvancement) {
      this.children.add(pGuiAdvancement);
   }

   public void drawHover(MatrixStack pMatrixStack, int pX, int pY, float pFade, int pWidth, int pHeight) {
      boolean flag = pWidth + pX + this.x + this.width + 26 >= this.tab.getScreen().width;
      String s = this.progress == null ? null : this.progress.getProgressText();
      int i = s == null ? 0 : this.minecraft.font.width(s);
      boolean flag1 = 113 - pY - this.y - 26 <= 6 + this.description.size() * 9;
      float f = this.progress == null ? 0.0F : this.progress.getPercent();
      int j = MathHelper.floor(f * (float)this.width);
      AdvancementState advancementstate;
      AdvancementState advancementstate1;
      AdvancementState advancementstate2;
      if (f >= 1.0F) {
         j = this.width / 2;
         advancementstate = AdvancementState.OBTAINED;
         advancementstate1 = AdvancementState.OBTAINED;
         advancementstate2 = AdvancementState.OBTAINED;
      } else if (j < 2) {
         j = this.width / 2;
         advancementstate = AdvancementState.UNOBTAINED;
         advancementstate1 = AdvancementState.UNOBTAINED;
         advancementstate2 = AdvancementState.UNOBTAINED;
      } else if (j > this.width - 2) {
         j = this.width / 2;
         advancementstate = AdvancementState.OBTAINED;
         advancementstate1 = AdvancementState.OBTAINED;
         advancementstate2 = AdvancementState.UNOBTAINED;
      } else {
         advancementstate = AdvancementState.OBTAINED;
         advancementstate1 = AdvancementState.UNOBTAINED;
         advancementstate2 = AdvancementState.UNOBTAINED;
      }

      int k = this.width - j;
      this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      int l = pY + this.y;
      int i1;
      if (flag) {
         i1 = pX + this.x - this.width + 26 + 6;
      } else {
         i1 = pX + this.x;
      }

      int j1 = 32 + this.description.size() * 9;
      if (!this.description.isEmpty()) {
         if (flag1) {
            this.render9Sprite(pMatrixStack, i1, l + 26 - j1, this.width, j1, 10, 200, 26, 0, 52);
         } else {
            this.render9Sprite(pMatrixStack, i1, l, this.width, j1, 10, 200, 26, 0, 52);
         }
      }

      this.blit(pMatrixStack, i1, l, 0, advancementstate.getIndex() * 26, j, 26);
      this.blit(pMatrixStack, i1 + j, l, 200 - k, advancementstate1.getIndex() * 26, k, 26);
      this.blit(pMatrixStack, pX + this.x + 3, pY + this.y, this.display.getFrame().getTexture(), 128 + advancementstate2.getIndex() * 26, 26, 26);
      if (flag) {
         this.minecraft.font.drawShadow(pMatrixStack, this.title, (float)(i1 + 5), (float)(pY + this.y + 9), -1);
         if (s != null) {
            this.minecraft.font.drawShadow(pMatrixStack, s, (float)(pX + this.x - i), (float)(pY + this.y + 9), -1);
         }
      } else {
         this.minecraft.font.drawShadow(pMatrixStack, this.title, (float)(pX + this.x + 32), (float)(pY + this.y + 9), -1);
         if (s != null) {
            this.minecraft.font.drawShadow(pMatrixStack, s, (float)(pX + this.x + this.width - i - 5), (float)(pY + this.y + 9), -1);
         }
      }

      if (flag1) {
         for(int k1 = 0; k1 < this.description.size(); ++k1) {
            this.minecraft.font.draw(pMatrixStack, this.description.get(k1), (float)(i1 + 5), (float)(l + 26 - j1 + 7 + k1 * 9), -5592406);
         }
      } else {
         for(int l1 = 0; l1 < this.description.size(); ++l1) {
            this.minecraft.font.draw(pMatrixStack, this.description.get(l1), (float)(i1 + 5), (float)(pY + this.y + 9 + 17 + l1 * 9), -5592406);
         }
      }

      this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), pX + this.x + 8, pY + this.y + 5);
   }

   protected void render9Sprite(MatrixStack pMatrixStack, int pX, int pY, int pWidth, int pHeight, int pPadding, int pUWidth, int pVHeight, int pUOffset, int pVOffset) {
      this.blit(pMatrixStack, pX, pY, pUOffset, pVOffset, pPadding, pPadding);
      this.renderRepeating(pMatrixStack, pX + pPadding, pY, pWidth - pPadding - pPadding, pPadding, pUOffset + pPadding, pVOffset, pUWidth - pPadding - pPadding, pVHeight);
      this.blit(pMatrixStack, pX + pWidth - pPadding, pY, pUOffset + pUWidth - pPadding, pVOffset, pPadding, pPadding);
      this.blit(pMatrixStack, pX, pY + pHeight - pPadding, pUOffset, pVOffset + pVHeight - pPadding, pPadding, pPadding);
      this.renderRepeating(pMatrixStack, pX + pPadding, pY + pHeight - pPadding, pWidth - pPadding - pPadding, pPadding, pUOffset + pPadding, pVOffset + pVHeight - pPadding, pUWidth - pPadding - pPadding, pVHeight);
      this.blit(pMatrixStack, pX + pWidth - pPadding, pY + pHeight - pPadding, pUOffset + pUWidth - pPadding, pVOffset + pVHeight - pPadding, pPadding, pPadding);
      this.renderRepeating(pMatrixStack, pX, pY + pPadding, pPadding, pHeight - pPadding - pPadding, pUOffset, pVOffset + pPadding, pUWidth, pVHeight - pPadding - pPadding);
      this.renderRepeating(pMatrixStack, pX + pPadding, pY + pPadding, pWidth - pPadding - pPadding, pHeight - pPadding - pPadding, pUOffset + pPadding, pVOffset + pPadding, pUWidth - pPadding - pPadding, pVHeight - pPadding - pPadding);
      this.renderRepeating(pMatrixStack, pX + pWidth - pPadding, pY + pPadding, pPadding, pHeight - pPadding - pPadding, pUOffset + pUWidth - pPadding, pVOffset + pPadding, pUWidth, pVHeight - pPadding - pPadding);
   }

   protected void renderRepeating(MatrixStack pMatrixStack, int pX, int pY, int pBorderToU, int pBorderToV, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
      for(int i = 0; i < pBorderToU; i += pUWidth) {
         int j = pX + i;
         int k = Math.min(pUWidth, pBorderToU - i);

         for(int l = 0; l < pBorderToV; l += pVHeight) {
            int i1 = pY + l;
            int j1 = Math.min(pVHeight, pBorderToV - l);
            this.blit(pMatrixStack, j, i1, pUOffset, pVOffset, k, j1);
         }
      }

   }

   public boolean isMouseOver(int pX, int pY, int pMouseX, int pMouseY) {
      if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
         int i = pX + this.x;
         int j = i + 26;
         int k = pY + this.y;
         int l = k + 26;
         return pMouseX >= i && pMouseX <= j && pMouseY >= k && pMouseY <= l;
      } else {
         return false;
      }
   }

   public void attachToParent() {
      if (this.parent == null && this.advancement.getParent() != null) {
         this.parent = this.getFirstVisibleParent(this.advancement);
         if (this.parent != null) {
            this.parent.addChild(this);
         }
      }

   }

   public int getY() {
      return this.y;
   }

   public int getX() {
      return this.x;
   }
}