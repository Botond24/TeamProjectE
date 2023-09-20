package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InventoryScreen extends DisplayEffectsScreen<PlayerContainer> implements IRecipeShownListener {
   private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
   /** The old x position of the mouse pointer */
   private float xMouse;
   /** The old y position of the mouse pointer */
   private float yMouse;
   private final RecipeBookGui recipeBookComponent = new RecipeBookGui();
   private boolean recipeBookComponentInitialized;
   private boolean widthTooNarrow;
   private boolean buttonClicked;

   public InventoryScreen(PlayerEntity pPlayer) {
      super(pPlayer.inventoryMenu, pPlayer.inventory, new TranslationTextComponent("container.crafting"));
      this.passEvents = true;
      this.titleLabelX = 97;
   }

   public void tick() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         this.minecraft.setScreen(new CreativeScreen(this.minecraft.player));
      } else {
         this.recipeBookComponent.tick();
      }
   }

   protected void init() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         this.minecraft.setScreen(new CreativeScreen(this.minecraft.player));
      } else {
         super.init();
         this.widthTooNarrow = this.width < 379;
         this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
         this.recipeBookComponentInitialized = true;
         this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
         this.children.add(this.recipeBookComponent);
         this.setInitialFocus(this.recipeBookComponent);
         this.addButton(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_214086_1_) -> {
            this.recipeBookComponent.initVisuals(this.widthTooNarrow);
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
            ((ImageButton)p_214086_1_).setPosition(this.leftPos + 104, this.height / 2 - 22);
            this.buttonClicked = true;
         }));
      }
   }

   protected void renderLabels(MatrixStack pMatrixStack, int pX, int pY) {
      this.font.draw(pMatrixStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      this.doRenderEffects = !this.recipeBookComponent.isVisible();
      if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
         this.renderBg(pMatrixStack, pPartialTicks, pMouseX, pMouseY);
         this.recipeBookComponent.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      } else {
         this.recipeBookComponent.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         this.recipeBookComponent.renderGhostRecipe(pMatrixStack, this.leftPos, this.topPos, false, pPartialTicks);
      }

      this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
      this.recipeBookComponent.renderTooltip(pMatrixStack, this.leftPos, this.topPos, pMouseX, pMouseY);
      this.xMouse = (float)pMouseX;
      this.yMouse = (float)pMouseY;
   }

   protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(INVENTORY_LOCATION);
      int i = this.leftPos;
      int j = this.topPos;
      this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
      renderEntityInInventory(i + 51, j + 75, 30, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.minecraft.player);
   }

   public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, float pMouseX, float pMouseY, LivingEntity pLivingEntity) {
      float f = (float)Math.atan((double)(pMouseX / 40.0F));
      float f1 = (float)Math.atan((double)(pMouseY / 40.0F));
      RenderSystem.pushMatrix();
      RenderSystem.translatef((float)pPosX, (float)pPosY, 1050.0F);
      RenderSystem.scalef(1.0F, 1.0F, -1.0F);
      MatrixStack matrixstack = new MatrixStack();
      matrixstack.translate(0.0D, 0.0D, 1000.0D);
      matrixstack.scale((float)pScale, (float)pScale, (float)pScale);
      Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
      Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
      quaternion.mul(quaternion1);
      matrixstack.mulPose(quaternion);
      float f2 = pLivingEntity.yBodyRot;
      float f3 = pLivingEntity.yRot;
      float f4 = pLivingEntity.xRot;
      float f5 = pLivingEntity.yHeadRotO;
      float f6 = pLivingEntity.yHeadRot;
      pLivingEntity.yBodyRot = 180.0F + f * 20.0F;
      pLivingEntity.yRot = 180.0F + f * 40.0F;
      pLivingEntity.xRot = -f1 * 20.0F;
      pLivingEntity.yHeadRot = pLivingEntity.yRot;
      pLivingEntity.yHeadRotO = pLivingEntity.yRot;
      EntityRendererManager entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
      quaternion1.conj();
      entityrenderermanager.overrideCameraOrientation(quaternion1);
      entityrenderermanager.setRenderShadow(false);
      IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
      RenderSystem.runAsFancy(() -> {
         entityrenderermanager.render(pLivingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
      });
      irendertypebuffer$impl.endBatch();
      entityrenderermanager.setRenderShadow(true);
      pLivingEntity.yBodyRot = f2;
      pLivingEntity.yRot = f3;
      pLivingEntity.xRot = f4;
      pLivingEntity.yHeadRotO = f5;
      pLivingEntity.yHeadRot = f6;
      RenderSystem.popMatrix();
   }

   protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
      return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.recipeBookComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
         this.setFocused(this.recipeBookComponent);
         return true;
      } else {
         return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      if (this.buttonClicked) {
         this.buttonClicked = false;
         return true;
      } else {
         return super.mouseReleased(pMouseX, pMouseY, pButton);
      }
   }

   protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
      boolean flag = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
      return this.recipeBookComponent.hasClickedOutside(pMouseX, pMouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pMouseButton) && flag;
   }

   /**
    * Called when the mouse is clicked over a slot or outside the gui.
    */
   protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
      super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
      this.recipeBookComponent.slotClicked(pSlot);
   }

   public void recipesUpdated() {
      this.recipeBookComponent.recipesUpdated();
   }

   public void removed() {
      if (this.recipeBookComponentInitialized) {
         this.recipeBookComponent.removed();
      }

      super.removed();
   }

   public RecipeBookGui getRecipeBookComponent() {
      return this.recipeBookComponent;
   }
}