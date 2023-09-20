package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeathScreen extends Screen {
   /** The integer value containing the number of ticks that have passed since the player's death */
   private int delayTicker;
   private final ITextComponent causeOfDeath;
   private final boolean hardcore;
   private ITextComponent deathScore;

   public DeathScreen(@Nullable ITextComponent p_i51118_1_, boolean p_i51118_2_) {
      super(new TranslationTextComponent(p_i51118_2_ ? "deathScreen.title.hardcore" : "deathScreen.title"));
      this.causeOfDeath = p_i51118_1_;
      this.hardcore = p_i51118_2_;
   }

   protected void init() {
      this.delayTicker = 0;
      this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72, 200, 20, this.hardcore ? new TranslationTextComponent("deathScreen.spectate") : new TranslationTextComponent("deathScreen.respawn"), (p_213021_1_) -> {
         this.minecraft.player.respawn();
         this.minecraft.setScreen((Screen)null);
      }));
      Button button = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96, 200, 20, new TranslationTextComponent("deathScreen.titleScreen"), (p_213020_1_) -> {
         if (this.hardcore) {
            confirmResult(true);
            this.exitToTitleScreen();
         } else {
            ConfirmScreen confirmscreen = new ConfirmScreen(this::confirmResult, new TranslationTextComponent("deathScreen.quit.confirm"), StringTextComponent.EMPTY, new TranslationTextComponent("deathScreen.titleScreen"), new TranslationTextComponent("deathScreen.respawn"));
            this.minecraft.setScreen(confirmscreen);
            confirmscreen.setDelay(20);
         }
      }));
      if (!this.hardcore && this.minecraft.getUser() == null) {
         button.active = false;
      }

      for(Widget widget : this.buttons) {
         widget.active = false;
      }

      this.deathScore = (new TranslationTextComponent("deathScreen.score")).append(": ").append((new StringTextComponent(Integer.toString(this.minecraft.player.getScore()))).withStyle(TextFormatting.YELLOW));
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   private void confirmResult(boolean p_213022_1_) {
      if (p_213022_1_) {
         this.exitToTitleScreen();
      } else {
         this.minecraft.player.respawn();
         this.minecraft.setScreen((Screen)null);
      }

   }

   private void exitToTitleScreen() {
      if (this.minecraft.level != null) {
         this.minecraft.level.disconnect();
      }

      this.minecraft.clearLevel(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
      this.minecraft.setScreen(new MainMenuScreen());
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.fillGradient(pMatrixStack, 0, 0, this.width, this.height, 1615855616, -1602211792);
      RenderSystem.pushMatrix();
      RenderSystem.scalef(2.0F, 2.0F, 2.0F);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2 / 2, 30, 16777215);
      RenderSystem.popMatrix();
      if (this.causeOfDeath != null) {
         drawCenteredString(pMatrixStack, this.font, this.causeOfDeath, this.width / 2, 85, 16777215);
      }

      drawCenteredString(pMatrixStack, this.font, this.deathScore, this.width / 2, 100, 16777215);
      if (this.causeOfDeath != null && pMouseY > 85 && pMouseY < 85 + 9) {
         Style style = this.getClickedComponentStyleAt(pMouseX);
         this.renderComponentHoverEffect(pMatrixStack, style, pMouseX, pMouseY);
      }

      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   @Nullable
   private Style getClickedComponentStyleAt(int p_238623_1_) {
      if (this.causeOfDeath == null) {
         return null;
      } else {
         int i = this.minecraft.font.width(this.causeOfDeath);
         int j = this.width / 2 - i / 2;
         int k = this.width / 2 + i / 2;
         return p_238623_1_ >= j && p_238623_1_ <= k ? this.minecraft.font.getSplitter().componentStyleAtWidth(this.causeOfDeath, p_238623_1_ - j) : null;
      }
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.causeOfDeath != null && pMouseY > 85.0D && pMouseY < (double)(85 + 9)) {
         Style style = this.getClickedComponentStyleAt((int)pMouseX);
         if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
            this.handleComponentClicked(style);
            return false;
         }
      }

      return super.mouseClicked(pMouseX, pMouseY, pButton);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void tick() {
      super.tick();
      ++this.delayTicker;
      if (this.delayTicker == 20) {
         for(Widget widget : this.buttons) {
            widget.active = true;
         }
      }

   }
}
