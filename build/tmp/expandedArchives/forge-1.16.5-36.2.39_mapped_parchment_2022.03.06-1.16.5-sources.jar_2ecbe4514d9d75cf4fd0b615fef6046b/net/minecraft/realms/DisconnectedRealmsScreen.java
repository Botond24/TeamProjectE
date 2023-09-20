package net.minecraft.realms;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
   private final ITextComponent title;
   private final ITextComponent reason;
   private IBidiRenderer message = IBidiRenderer.EMPTY;
   private final Screen parent;
   private int textHeight;

   public DisconnectedRealmsScreen(Screen pParent, ITextComponent pTitle, ITextComponent pReason) {
      this.parent = pParent;
      this.title = pTitle;
      this.reason = pReason;
   }

   public void init() {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.setConnectedToRealms(false);
      minecraft.getClientPackSource().clearServerPack();
      RealmsNarratorHelper.now(this.title.getString() + ": " + this.reason.getString());
      this.message = IBidiRenderer.create(this.font, this.reason, this.width - 50);
      this.textHeight = this.message.getLineCount() * 9;
      this.addButton(new Button(this.width / 2 - 100, this.height / 2 + this.textHeight / 2 + 9, 200, 20, DialogTexts.GUI_BACK, (p_239547_2_) -> {
         minecraft.setScreen(this.parent);
      }));
   }

   public void onClose() {
      Minecraft.getInstance().setScreen(this.parent);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
      this.message.renderCentered(pMatrixStack, this.width / 2, this.height / 2 - this.textHeight / 2);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }
}