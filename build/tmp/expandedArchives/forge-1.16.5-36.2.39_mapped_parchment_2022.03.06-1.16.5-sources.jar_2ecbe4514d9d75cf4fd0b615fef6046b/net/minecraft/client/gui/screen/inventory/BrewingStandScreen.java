package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrewingStandScreen extends ContainerScreen<BrewingStandContainer> {
   private static final ResourceLocation BREWING_STAND_LOCATION = new ResourceLocation("textures/gui/container/brewing_stand.png");
   private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

   public BrewingStandScreen(BrewingStandContainer pBrewingStandMenu, PlayerInventory pPlayerInventory, ITextComponent pTitle) {
      super(pBrewingStandMenu, pPlayerInventory, pTitle);
   }

   protected void init() {
      super.init();
      this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
   }

   protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(BREWING_STAND_LOCATION);
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
      int k = this.menu.getFuel();
      int l = MathHelper.clamp((18 * k + 20 - 1) / 20, 0, 18);
      if (l > 0) {
         this.blit(pMatrixStack, i + 60, j + 44, 176, 29, l, 4);
      }

      int i1 = this.menu.getBrewingTicks();
      if (i1 > 0) {
         int j1 = (int)(28.0F * (1.0F - (float)i1 / 400.0F));
         if (j1 > 0) {
            this.blit(pMatrixStack, i + 97, j + 16, 176, 0, 9, j1);
         }

         j1 = BUBBLELENGTHS[i1 / 2 % 7];
         if (j1 > 0) {
            this.blit(pMatrixStack, i + 63, j + 14 + 29 - j1, 185, 29 - j1, 12, j1);
         }
      }

   }
}