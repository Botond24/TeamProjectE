package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmScreen extends Screen {
   private final ITextComponent title2;
   private IBidiRenderer message = IBidiRenderer.EMPTY;
   /** The text shown for the first button in GuiYesNo */
   protected ITextComponent yesButton;
   /** The text shown for the second button in GuiYesNo */
   protected ITextComponent noButton;
   private int delayTicker;
   protected final BooleanConsumer callback;

   public ConfirmScreen(BooleanConsumer p_i51119_1_, ITextComponent p_i51119_2_, ITextComponent p_i51119_3_) {
      this(p_i51119_1_, p_i51119_2_, p_i51119_3_, DialogTexts.GUI_YES, DialogTexts.GUI_NO);
   }

   public ConfirmScreen(BooleanConsumer p_i232270_1_, ITextComponent p_i232270_2_, ITextComponent p_i232270_3_, ITextComponent p_i232270_4_, ITextComponent p_i232270_5_) {
      super(p_i232270_2_);
      this.callback = p_i232270_1_;
      this.title2 = p_i232270_3_;
      this.yesButton = p_i232270_4_;
      this.noButton = p_i232270_5_;
   }

   public String getNarrationMessage() {
      return super.getNarrationMessage() + ". " + this.title2.getString();
   }

   protected void init() {
      super.init();
      this.addButton(new Button(this.width / 2 - 155, this.height / 6 + 96, 150, 20, this.yesButton, (p_213002_1_) -> {
         this.callback.accept(true);
      }));
      this.addButton(new Button(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, this.noButton, (p_213001_1_) -> {
         this.callback.accept(false);
      }));
      this.message = IBidiRenderer.create(this.font, this.title2, this.width - 50);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 70, 16777215);
      this.message.renderCentered(pMatrixStack, this.width / 2, 90);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   /**
    * Sets the number of ticks to wait before enabling the buttons.
    */
   public void setDelay(int pTicksUntilEnable) {
      this.delayTicker = pTicksUntilEnable;

      for(Widget widget : this.buttons) {
         widget.active = false;
      }

   }

   public void tick() {
      super.tick();
      if (--this.delayTicker == 0) {
         for(Widget widget : this.buttons) {
            widget.active = true;
         }
      }

   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.callback.accept(false);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }
}