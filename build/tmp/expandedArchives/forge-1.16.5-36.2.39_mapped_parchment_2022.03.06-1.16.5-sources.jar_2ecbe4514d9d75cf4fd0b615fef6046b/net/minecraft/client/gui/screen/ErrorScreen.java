package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ErrorScreen extends Screen {
   private final ITextComponent message;

   public ErrorScreen(ITextComponent pTitle, ITextComponent pMessage) {
      super(pTitle);
      this.message = pMessage;
   }

   protected void init() {
      super.init();
      this.addButton(new Button(this.width / 2 - 100, 140, 200, 20, DialogTexts.GUI_CANCEL, (p_213034_1_) -> {
         this.minecraft.setScreen((Screen)null);
      }));
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.fillGradient(pMatrixStack, 0, 0, this.width, this.height, -12574688, -11530224);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 90, 16777215);
      drawCenteredString(pMatrixStack, this.font, this.message, this.width / 2, 110, 16777215);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}