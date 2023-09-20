package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmBackupScreen extends Screen {
   @Nullable
   private final Screen lastScreen;
   protected final ConfirmBackupScreen.ICallback listener;
   private final ITextComponent description;
   private final boolean promptForCacheErase;
   private IBidiRenderer message = IBidiRenderer.EMPTY;
   private CheckboxButton eraseCache;

   public ConfirmBackupScreen(@Nullable Screen p_i51122_1_, ConfirmBackupScreen.ICallback p_i51122_2_, ITextComponent p_i51122_3_, ITextComponent p_i51122_4_, boolean p_i51122_5_) {
      super(p_i51122_3_);
      this.lastScreen = p_i51122_1_;
      this.listener = p_i51122_2_;
      this.description = p_i51122_4_;
      this.promptForCacheErase = p_i51122_5_;
   }

   protected void init() {
      super.init();
      this.message = IBidiRenderer.create(this.font, this.description, this.width - 50);
      int i = (this.message.getLineCount() + 1) * 9;
      this.addButton(new Button(this.width / 2 - 155, 100 + i, 150, 20, new TranslationTextComponent("selectWorld.backupJoinConfirmButton"), (p_212993_1_) -> {
         this.listener.proceed(true, this.eraseCache.selected());
      }));
      this.addButton(new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20, new TranslationTextComponent("selectWorld.backupJoinSkipButton"), (p_212992_1_) -> {
         this.listener.proceed(false, this.eraseCache.selected());
      }));
      this.addButton(new Button(this.width / 2 - 155 + 80, 124 + i, 150, 20, DialogTexts.GUI_CANCEL, (p_212991_1_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
      this.eraseCache = new CheckboxButton(this.width / 2 - 155 + 80, 76 + i, 150, 20, new TranslationTextComponent("selectWorld.backupEraseCache"), false);
      if (this.promptForCacheErase) {
         this.addButton(this.eraseCache);
      }

   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 50, 16777215);
      this.message.renderCentered(pMatrixStack, this.width / 2, 70);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public interface ICallback {
      void proceed(boolean p_proceed_1_, boolean p_proceed_2_);
   }
}