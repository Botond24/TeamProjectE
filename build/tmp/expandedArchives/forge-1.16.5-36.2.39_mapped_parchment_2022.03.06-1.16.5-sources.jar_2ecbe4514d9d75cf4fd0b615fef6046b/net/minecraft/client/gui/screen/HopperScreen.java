package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HopperScreen extends ContainerScreen<HopperContainer> {
   /** The ResourceLocation containing the gui texture for the hopper */
   private static final ResourceLocation HOPPER_LOCATION = new ResourceLocation("textures/gui/container/hopper.png");

   public HopperScreen(HopperContainer pHopperMenu, PlayerInventory pPlayerInventory, ITextComponent pTitle) {
      super(pHopperMenu, pPlayerInventory, pTitle);
      this.passEvents = false;
      this.imageHeight = 133;
      this.inventoryLabelY = this.imageHeight - 94;
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
   }

   protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(HOPPER_LOCATION);
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
   }
}