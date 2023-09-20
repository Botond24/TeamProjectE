package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.SmithingTableContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmithingTableScreen extends AbstractRepairScreen<SmithingTableContainer> {
   private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/smithing.png");

   public SmithingTableScreen(SmithingTableContainer pSmithingMenu, PlayerInventory pPlayerInventory, ITextComponent pTitle) {
      super(pSmithingMenu, pPlayerInventory, pTitle, SMITHING_LOCATION);
      this.titleLabelX = 60;
      this.titleLabelY = 18;
   }

   protected void renderLabels(MatrixStack pMatrixStack, int pX, int pY) {
      RenderSystem.disableBlend();
      super.renderLabels(pMatrixStack, pX, pY);
   }
}