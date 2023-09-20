package net.minecraft.client.gui.widget.list;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractList<E extends AbstractList.AbstractListEntry<E>> extends FocusableGui implements IRenderable {
   protected final Minecraft minecraft;
   protected final int itemHeight;
   private final List<E> children = new AbstractList.SimpleArrayList();
   protected int width;
   protected int height;
   protected int y0;
   protected int y1;
   protected int x1;
   protected int x0;
   protected boolean centerListVertically = true;
   private double scrollAmount;
   private boolean renderSelection = true;
   private boolean renderHeader;
   protected int headerHeight;
   private boolean scrolling;
   private E selected;
   private boolean renderBackground = true;
   private boolean renderTopAndBottom = true;

   public AbstractList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
      this.minecraft = pMinecraft;
      this.width = pWidth;
      this.height = pHeight;
      this.y0 = pY0;
      this.y1 = pY1;
      this.itemHeight = pItemHeight;
      this.x0 = 0;
      this.x1 = pWidth;
   }

   public void setRenderSelection(boolean pValue) {
      this.renderSelection = pValue;
   }

   protected void setRenderHeader(boolean pValue, int pHeight) {
      this.renderHeader = pValue;
      this.headerHeight = pHeight;
      if (!pValue) {
         this.headerHeight = 0;
      }

   }

   public int getRowWidth() {
      return 220;
   }

   @Nullable
   public E getSelected() {
      return this.selected;
   }

   public void setSelected(@Nullable E pEntry) {
      this.selected = pEntry;
   }

   public void setRenderBackground(boolean pRenderBackground) {
      this.renderBackground = pRenderBackground;
   }

   public void setRenderTopAndBottom(boolean pRenderTopAndButton) {
      this.renderTopAndBottom = pRenderTopAndButton;
   }

   @Nullable
   public E getFocused() {
      return (E)(super.getFocused());
   }

   public final List<E> children() {
      return this.children;
   }

   protected final void clearEntries() {
      this.children.clear();
   }

   protected void replaceEntries(Collection<E> pEntries) {
      this.children.clear();
      this.children.addAll(pEntries);
   }

   protected E getEntry(int pIndex) {
      return this.children().get(pIndex);
   }

   protected int addEntry(E pEntry) {
      this.children.add(pEntry);
      return this.children.size() - 1;
   }

   protected int getItemCount() {
      return this.children().size();
   }

   protected boolean isSelectedItem(int pIndex) {
      return Objects.equals(this.getSelected(), this.children().get(pIndex));
   }

   @Nullable
   protected final E getEntryAtPosition(double pMouseX, double pMouseY) {
      int i = this.getRowWidth() / 2;
      int j = this.x0 + this.width / 2;
      int k = j - i;
      int l = j + i;
      int i1 = MathHelper.floor(pMouseY - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
      int j1 = i1 / this.itemHeight;
      return (E)(pMouseX < (double)this.getScrollbarPosition() && pMouseX >= (double)k && pMouseX <= (double)l && j1 >= 0 && i1 >= 0 && j1 < this.getItemCount() ? this.children().get(j1) : null);
   }

   public void updateSize(int pWidth, int pHeight, int pY0, int pY1) {
      this.width = pWidth;
      this.height = pHeight;
      this.y0 = pY0;
      this.y1 = pY1;
      this.x0 = 0;
      this.x1 = pWidth;
   }

   public void setLeftPos(int pLeft) {
      this.x0 = pLeft;
      this.x1 = pLeft + this.width;
   }

   protected int getMaxPosition() {
      return this.getItemCount() * this.itemHeight + this.headerHeight;
   }

   protected void clickedHeader(int p_230938_1_, int p_230938_2_) {
   }

   protected void renderHeader(MatrixStack pMatrixStack, int pX, int pY, Tessellator pTessellator) {
   }

   protected void renderBackground(MatrixStack pMatrixStack) {
   }

   protected void renderDecorations(MatrixStack pPoseStack, int pMouseX, int pMouseY) {
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      int i = this.getScrollbarPosition();
      int j = i + 6;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuilder();
      if (this.renderBackground) {
         this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         float f = 32.0F;
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
         bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
         bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
         bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
         tessellator.end();
      }

      int j1 = this.getRowLeft();
      int k = this.y0 + 4 - (int)this.getScrollAmount();
      if (this.renderHeader) {
         this.renderHeader(pMatrixStack, j1, k, tessellator);
      }

      this.renderList(pMatrixStack, j1, k, pMouseX, pMouseY, pPartialTicks);
      if (this.renderTopAndBottom) {
         this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(519);
         float f1 = 32.0F;
         int l = -100;
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         bufferbuilder.vertex((double)this.x0, (double)this.y0, -100.0D).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0D).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferbuilder.vertex((double)(this.x0 + this.width), 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
         bufferbuilder.vertex((double)this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
         bufferbuilder.vertex((double)this.x0, (double)this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0D).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
         bufferbuilder.vertex((double)this.x0, (double)this.y1, -100.0D).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
         tessellator.end();
         RenderSystem.depthFunc(515);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         RenderSystem.disableAlphaTest();
         RenderSystem.shadeModel(7425);
         RenderSystem.disableTexture();
         int i1 = 4;
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         bufferbuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
         bufferbuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
         bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
         bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
         bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
         bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
         bufferbuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
         bufferbuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
         tessellator.end();
      }

      int k1 = this.getMaxScroll();
      if (k1 > 0) {
         RenderSystem.disableTexture();
         int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
         l1 = MathHelper.clamp(l1, 32, this.y1 - this.y0 - 8);
         int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
         if (i2 < this.y0) {
            i2 = this.y0;
         }

         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         bufferbuilder.vertex((double)i, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
         bufferbuilder.vertex((double)j, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
         bufferbuilder.vertex((double)j, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
         bufferbuilder.vertex((double)i, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
         bufferbuilder.vertex((double)i, (double)(i2 + l1), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
         bufferbuilder.vertex((double)j, (double)(i2 + l1), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
         bufferbuilder.vertex((double)j, (double)i2, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
         bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
         bufferbuilder.vertex((double)i, (double)(i2 + l1 - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
         bufferbuilder.vertex((double)(j - 1), (double)(i2 + l1 - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
         bufferbuilder.vertex((double)(j - 1), (double)i2, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
         bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
         tessellator.end();
      }

      this.renderDecorations(pMatrixStack, pMouseX, pMouseY);
      RenderSystem.enableTexture();
      RenderSystem.shadeModel(7424);
      RenderSystem.enableAlphaTest();
      RenderSystem.disableBlend();
   }

   protected void centerScrollOn(E pEntry) {
      this.setScrollAmount((double)(this.children().indexOf(pEntry) * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2));
   }

   protected void ensureVisible(E pEntry) {
      int i = this.getRowTop(this.children().indexOf(pEntry));
      int j = i - this.y0 - 4 - this.itemHeight;
      if (j < 0) {
         this.scroll(j);
      }

      int k = this.y1 - i - this.itemHeight - this.itemHeight;
      if (k < 0) {
         this.scroll(-k);
      }

   }

   private void scroll(int pScroll) {
      this.setScrollAmount(this.getScrollAmount() + (double)pScroll);
   }

   public double getScrollAmount() {
      return this.scrollAmount;
   }

   public void setScrollAmount(double pScroll) {
      this.scrollAmount = MathHelper.clamp(pScroll, 0.0D, (double)this.getMaxScroll());
   }

   public int getMaxScroll() {
      return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
   }

   protected void updateScrollingState(double pMouseX, double pMouseY, int pButton) {
      this.scrolling = pButton == 0 && pMouseX >= (double)this.getScrollbarPosition() && pMouseX < (double)(this.getScrollbarPosition() + 6);
   }

   protected int getScrollbarPosition() {
      return this.width / 2 + 124;
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      this.updateScrollingState(pMouseX, pMouseY, pButton);
      if (!this.isMouseOver(pMouseX, pMouseY)) {
         return false;
      } else {
         E e = this.getEntryAtPosition(pMouseX, pMouseY);
         if (e != null) {
            if (e.mouseClicked(pMouseX, pMouseY, pButton)) {
               this.setFocused(e);
               this.setDragging(true);
               return true;
            }
         } else if (pButton == 0) {
            this.clickedHeader((int)(pMouseX - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(pMouseY - (double)this.y0) + (int)this.getScrollAmount() - 4);
            return true;
         }

         return this.scrolling;
      }
   }

   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      if (this.getFocused() != null) {
         this.getFocused().mouseReleased(pMouseX, pMouseY, pButton);
      }

      return false;
   }

   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
         return true;
      } else if (pButton == 0 && this.scrolling) {
         if (pMouseY < (double)this.y0) {
            this.setScrollAmount(0.0D);
         } else if (pMouseY > (double)this.y1) {
            this.setScrollAmount((double)this.getMaxScroll());
         } else {
            double d0 = (double)Math.max(1, this.getMaxScroll());
            int i = this.y1 - this.y0;
            int j = MathHelper.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
            double d1 = Math.max(1.0D, d0 / (double)(i - j));
            this.setScrollAmount(this.getScrollAmount() + pDragY * d1);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      this.setScrollAmount(this.getScrollAmount() - pDelta * (double)this.itemHeight / 2.0D);
      return true;
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (pKeyCode == 264) {
         this.moveSelection(AbstractList.Ordering.DOWN);
         return true;
      } else if (pKeyCode == 265) {
         this.moveSelection(AbstractList.Ordering.UP);
         return true;
      } else {
         return false;
      }
   }

   protected void moveSelection(AbstractList.Ordering pOrdering) {
      this.moveSelection(pOrdering, (p_241573_0_) -> {
         return true;
      });
   }

   protected void refreshSelection() {
      E e = this.getSelected();
      if (e != null) {
         this.setSelected(e);
         this.ensureVisible(e);
      }

   }

   protected void moveSelection(AbstractList.Ordering pOrdering, Predicate<E> pFilter) {
      int i = pOrdering == AbstractList.Ordering.UP ? -1 : 1;
      if (!this.children().isEmpty()) {
         int j = this.children().indexOf(this.getSelected());

         while(true) {
            int k = MathHelper.clamp(j + i, 0, this.getItemCount() - 1);
            if (j == k) {
               break;
            }

            E e = this.children().get(k);
            if (pFilter.test(e)) {
               this.setSelected(e);
               this.ensureVisible(e);
               break;
            }

            j = k;
         }
      }

   }

   public boolean isMouseOver(double pMouseX, double pMouseY) {
      return pMouseY >= (double)this.y0 && pMouseY <= (double)this.y1 && pMouseX >= (double)this.x0 && pMouseX <= (double)this.x1;
   }

   protected void renderList(MatrixStack pMatrixStack, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
      int i = this.getItemCount();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuilder();

      for(int j = 0; j < i; ++j) {
         int k = this.getRowTop(j);
         int l = this.getRowBottom(j);
         if (l >= this.y0 && k <= this.y1) {
            int i1 = pY + j * this.itemHeight + this.headerHeight;
            int j1 = this.itemHeight - 4;
            E e = this.getEntry(j);
            int k1 = this.getRowWidth();
            if (this.renderSelection && this.isSelectedItem(j)) {
               int l1 = this.x0 + this.width / 2 - k1 / 2;
               int i2 = this.x0 + this.width / 2 + k1 / 2;
               RenderSystem.disableTexture();
               float f = this.isFocused() ? 1.0F : 0.5F;
               RenderSystem.color4f(f, f, f, 1.0F);
               bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
               bufferbuilder.vertex((double)l1, (double)(i1 + j1 + 2), 0.0D).endVertex();
               bufferbuilder.vertex((double)i2, (double)(i1 + j1 + 2), 0.0D).endVertex();
               bufferbuilder.vertex((double)i2, (double)(i1 - 2), 0.0D).endVertex();
               bufferbuilder.vertex((double)l1, (double)(i1 - 2), 0.0D).endVertex();
               tessellator.end();
               RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
               bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
               bufferbuilder.vertex((double)(l1 + 1), (double)(i1 + j1 + 1), 0.0D).endVertex();
               bufferbuilder.vertex((double)(i2 - 1), (double)(i1 + j1 + 1), 0.0D).endVertex();
               bufferbuilder.vertex((double)(i2 - 1), (double)(i1 - 1), 0.0D).endVertex();
               bufferbuilder.vertex((double)(l1 + 1), (double)(i1 - 1), 0.0D).endVertex();
               tessellator.end();
               RenderSystem.enableTexture();
            }

            int j2 = this.getRowLeft();
            e.render(pMatrixStack, j, k, j2, k1, j1, pMouseX, pMouseY, this.isMouseOver((double)pMouseX, (double)pMouseY) && Objects.equals(this.getEntryAtPosition((double)pMouseX, (double)pMouseY), e), pPartialTicks);
         }
      }

   }

   public int getRowLeft() {
      return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
   }

   public int getRowRight() {
      return this.getRowLeft() + this.getRowWidth();
   }

   protected int getRowTop(int pIndex) {
      return this.y0 + 4 - (int)this.getScrollAmount() + pIndex * this.itemHeight + this.headerHeight;
   }

   private int getRowBottom(int pIndex) {
      return this.getRowTop(pIndex) + this.itemHeight;
   }

   protected boolean isFocused() {
      return false;
   }

   protected E remove(int pIndex) {
      E e = this.children.get(pIndex);
      return (E)(this.removeEntry(this.children.get(pIndex)) ? e : null);
   }

   protected boolean removeEntry(E pEntry) {
      boolean flag = this.children.remove(pEntry);
      if (flag && pEntry == this.getSelected()) {
         this.setSelected((E)null);
      }

      return flag;
   }

   private void bindEntryToSelf(AbstractList.AbstractListEntry<E> p_238480_1_) {
      p_238480_1_.list = this;
   }

   public int getWidth() { return this.width; }
   public int getHeight() { return this.height; }
   public int getTop() { return this.y0; }
   public int getBottom() { return this.y1; }
   public int getLeft() { return this.x0; }
   public int getRight() { return this.x1; }

   @OnlyIn(Dist.CLIENT)
   public abstract static class AbstractListEntry<E extends AbstractList.AbstractListEntry<E>> implements IGuiEventListener {
      @Deprecated
      protected AbstractList<E> list;

      public abstract void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks);

      public boolean isMouseOver(double pMouseX, double pMouseY) {
         return Objects.equals(this.list.getEntryAtPosition(pMouseX, pMouseY), this);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Ordering {
      UP,
      DOWN;
   }

   @OnlyIn(Dist.CLIENT)
   class SimpleArrayList extends java.util.AbstractList<E> {
      private final List<E> delegate = Lists.newArrayList();

      private SimpleArrayList() {
      }

      public E get(int p_get_1_) {
         return this.delegate.get(p_get_1_);
      }

      public int size() {
         return this.delegate.size();
      }

      public E set(int p_set_1_, E p_set_2_) {
         E e = this.delegate.set(p_set_1_, p_set_2_);
         AbstractList.this.bindEntryToSelf(p_set_2_);
         return e;
      }

      public void add(int p_add_1_, E p_add_2_) {
         this.delegate.add(p_add_1_, p_add_2_);
         AbstractList.this.bindEntryToSelf(p_add_2_);
      }

      public E remove(int p_remove_1_) {
         return this.delegate.remove(p_remove_1_);
      }
   }
}
