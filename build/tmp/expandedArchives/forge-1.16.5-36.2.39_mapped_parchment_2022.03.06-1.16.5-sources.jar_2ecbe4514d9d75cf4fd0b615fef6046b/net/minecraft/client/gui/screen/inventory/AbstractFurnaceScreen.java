package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.recipebook.AbstractRecipeBookGui;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.AbstractFurnaceContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractFurnaceScreen<T extends AbstractFurnaceContainer> extends ContainerScreen<T> implements IRecipeShownListener {
   private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
   public final AbstractRecipeBookGui recipeBookComponent;
   private boolean widthTooNarrow;
   private final ResourceLocation texture;

   public AbstractFurnaceScreen(T pAbstractFurnaceMenu, AbstractRecipeBookGui pRecipeBookComponent, PlayerInventory pPlayerInventory, ITextComponent pTitle, ResourceLocation pTexture) {
      super(pAbstractFurnaceMenu, pPlayerInventory, pTitle);
      this.recipeBookComponent = pRecipeBookComponent;
      this.texture = pTexture;
   }

   public void init() {
      super.init();
      this.widthTooNarrow = this.width < 379;
      this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
      this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
      this.addButton(new ImageButton(this.leftPos + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_214087_1_) -> {
         this.recipeBookComponent.initVisuals(this.widthTooNarrow);
         this.recipeBookComponent.toggleVisibility();
         this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
         ((ImageButton)p_214087_1_).setPosition(this.leftPos + 20, this.height / 2 - 49);
      }));
      this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
   }

   public void tick() {
      super.tick();
      this.recipeBookComponent.tick();
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
         this.renderBg(pMatrixStack, pPartialTicks, pMouseX, pMouseY);
         this.recipeBookComponent.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      } else {
         this.recipeBookComponent.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         this.recipeBookComponent.renderGhostRecipe(pMatrixStack, this.leftPos, this.topPos, true, pPartialTicks);
      }

      this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
      this.recipeBookComponent.renderTooltip(pMatrixStack, this.leftPos, this.topPos, pMouseX, pMouseY);
   }

   protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(this.texture);
      int i = this.leftPos;
      int j = this.topPos;
      this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
      if (this.menu.isLit()) {
         int k = this.menu.getLitProgress();
         this.blit(pMatrixStack, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
      }

      int l = this.menu.getBurnProgress();
      this.blit(pMatrixStack, i + 79, j + 34, 176, 14, l + 1, 16);
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.recipeBookComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
         return true;
      } else {
         return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   /**
    * Called when the mouse is clicked over a slot or outside the gui.
    */
   protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
      super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
      this.recipeBookComponent.slotClicked(pSlot);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      return this.recipeBookComponent.keyPressed(pKeyCode, pScanCode, pModifiers) ? false : super.keyPressed(pKeyCode, pScanCode, pModifiers);
   }

   protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
      boolean flag = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
      return this.recipeBookComponent.hasClickedOutside(pMouseX, pMouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pMouseButton) && flag;
   }

   public boolean charTyped(char pCodePoint, int pModifiers) {
      return this.recipeBookComponent.charTyped(pCodePoint, pModifiers) ? true : super.charTyped(pCodePoint, pModifiers);
   }

   public void recipesUpdated() {
      this.recipeBookComponent.recipesUpdated();
   }

   public RecipeBookGui getRecipeBookComponent() {
      return this.recipeBookComponent;
   }

   public void removed() {
      this.recipeBookComponent.removed();
      super.removed();
   }
}