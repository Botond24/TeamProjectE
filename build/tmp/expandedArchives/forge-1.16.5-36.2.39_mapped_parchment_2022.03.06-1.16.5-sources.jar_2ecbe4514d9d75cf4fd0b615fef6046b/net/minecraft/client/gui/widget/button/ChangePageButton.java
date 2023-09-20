package net.minecraft.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.ReadBookScreen;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChangePageButton extends Button {
   private final boolean isForward;
   private final boolean playTurnSound;

   public ChangePageButton(int pX, int pY, boolean pIsForward, Button.IPressable pOnPress, boolean pPlayTurnSound) {
      super(pX, pY, 23, 13, StringTextComponent.EMPTY, pOnPress);
      this.isForward = pIsForward;
      this.playTurnSound = pPlayTurnSound;
   }

   public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      Minecraft.getInstance().getTextureManager().bind(ReadBookScreen.BOOK_LOCATION);
      int i = 0;
      int j = 192;
      if (this.isHovered()) {
         i += 23;
      }

      if (!this.isForward) {
         j += 13;
      }

      this.blit(pMatrixStack, this.x, this.y, i, j, 23, 13);
   }

   public void playDownSound(SoundHandler pHandler) {
      if (this.playTurnSound) {
         pHandler.play(SimpleSound.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
      }

   }
}