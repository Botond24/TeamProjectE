package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DemoScreen extends Screen {
   private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");
   private IBidiRenderer movementMessage = IBidiRenderer.EMPTY;
   private IBidiRenderer durationMessage = IBidiRenderer.EMPTY;

   public DemoScreen() {
      super(new TranslationTextComponent("demo.help.title"));
   }

   protected void init() {
      int i = -16;
      this.addButton(new Button(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, new TranslationTextComponent("demo.help.buy"), (p_213019_0_) -> {
         p_213019_0_.active = false;
         Util.getPlatform().openUri("http://www.minecraft.net/store?source=demo");
      }));
      this.addButton(new Button(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, new TranslationTextComponent("demo.help.later"), (p_213018_1_) -> {
         this.minecraft.setScreen((Screen)null);
         this.minecraft.mouseHandler.grabMouse();
      }));
      GameSettings gamesettings = this.minecraft.options;
      this.movementMessage = IBidiRenderer.create(this.font, new TranslationTextComponent("demo.help.movementShort", gamesettings.keyUp.getTranslatedKeyMessage(), gamesettings.keyLeft.getTranslatedKeyMessage(), gamesettings.keyDown.getTranslatedKeyMessage(), gamesettings.keyRight.getTranslatedKeyMessage()), new TranslationTextComponent("demo.help.movementMouse"), new TranslationTextComponent("demo.help.jump", gamesettings.keyJump.getTranslatedKeyMessage()), new TranslationTextComponent("demo.help.inventory", gamesettings.keyInventory.getTranslatedKeyMessage()));
      this.durationMessage = IBidiRenderer.create(this.font, new TranslationTextComponent("demo.help.fullWrapped"), 218);
   }

   public void renderBackground(MatrixStack pMatrixStack) {
      super.renderBackground(pMatrixStack);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(DEMO_BACKGROUND_LOCATION);
      int i = (this.width - 248) / 2;
      int j = (this.height - 166) / 2;
      this.blit(pMatrixStack, i, j, 0, 0, 248, 166);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      int i = (this.width - 248) / 2 + 10;
      int j = (this.height - 166) / 2 + 8;
      this.font.draw(pMatrixStack, this.title, (float)i, (float)j, 2039583);
      j = this.movementMessage.renderLeftAlignedNoShadow(pMatrixStack, i, j + 12, 12, 5197647);
      this.durationMessage.renderLeftAlignedNoShadow(pMatrixStack, i, j + 20, 9, 2039583);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }
}