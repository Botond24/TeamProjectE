package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorkingScreen extends Screen implements IProgressUpdate {
   @Nullable
   private ITextComponent header;
   @Nullable
   private ITextComponent stage;
   private int progress;
   private boolean stop;

   public WorkingScreen() {
      super(NarratorChatListener.NO_TITLE);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void progressStartNoAbort(ITextComponent pComponent) {
      this.progressStart(pComponent);
   }

   public void progressStart(ITextComponent pComponent) {
      this.header = pComponent;
      this.progressStage(new TranslationTextComponent("progress.working"));
   }

   public void progressStage(ITextComponent pComponent) {
      this.stage = pComponent;
      this.progressStagePercentage(0);
   }

   /**
    * Updates the progress bar on the loading screen to the specified amount.
    */
   public void progressStagePercentage(int pProgress) {
      this.progress = pProgress;
   }

   public void stop() {
      this.stop = true;
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      if (this.stop) {
         if (!this.minecraft.isConnectedToRealms()) {
            this.minecraft.setScreen((Screen)null);
         }

      } else {
         this.renderBackground(pMatrixStack);
         if (this.header != null) {
            drawCenteredString(pMatrixStack, this.font, this.header, this.width / 2, 70, 16777215);
         }

         if (this.stage != null && this.progress != 0) {
            drawCenteredString(pMatrixStack, this.font, (new StringTextComponent("")).append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, 16777215);
         }

         super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      }
   }
}