package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AlertScreen extends Screen {
   private final Runnable callback;
   protected final ITextComponent text;
   private IBidiRenderer message = IBidiRenderer.EMPTY;
   protected final ITextComponent okButton;
   private int delayTicker;

   public AlertScreen(Runnable pCallback, ITextComponent pTitle, ITextComponent pText) {
      this(pCallback, pTitle, pText, DialogTexts.GUI_BACK);
   }

   public AlertScreen(Runnable pCallback, ITextComponent pTitle, ITextComponent pText, ITextComponent pOkButton) {
      super(pTitle);
      this.callback = pCallback;
      this.text = pText;
      this.okButton = pOkButton;
   }

   protected void init() {
      super.init();
      this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.okButton, (p_212983_1_) -> {
         this.callback.run();
      }));
      this.message = IBidiRenderer.create(this.font, this.text, this.width - 50);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 70, 16777215);
      this.message.renderCentered(pMatrixStack, this.width / 2, 90);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   public void tick() {
      super.tick();
      if (--this.delayTicker == 0) {
         for(Widget widget : this.buttons) {
            widget.active = true;
         }
      }

   }
}