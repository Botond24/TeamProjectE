package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsClientOutdatedScreen extends RealmsScreen {
   private static final ITextComponent OUTDATED_TITLE = new TranslationTextComponent("mco.client.outdated.title");
   private static final ITextComponent[] OUTDATED_MESSAGES = new ITextComponent[]{new TranslationTextComponent("mco.client.outdated.msg.line1"), new TranslationTextComponent("mco.client.outdated.msg.line2")};
   private static final ITextComponent INCOMPATIBLE_TITLE = new TranslationTextComponent("mco.client.incompatible.title");
   private static final ITextComponent[] INCOMPATIBLE_MESSAGES = new ITextComponent[]{new TranslationTextComponent("mco.client.incompatible.msg.line1"), new TranslationTextComponent("mco.client.incompatible.msg.line2"), new TranslationTextComponent("mco.client.incompatible.msg.line3")};
   private final Screen lastScreen;
   private final boolean outdated;

   public RealmsClientOutdatedScreen(Screen p_i232201_1_, boolean p_i232201_2_) {
      this.lastScreen = p_i232201_1_;
      this.outdated = p_i232201_2_;
   }

   public void init() {
      this.addButton(new Button(this.width / 2 - 100, row(12), 200, 20, DialogTexts.GUI_BACK, (p_237786_1_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      ITextComponent itextcomponent;
      ITextComponent[] aitextcomponent;
      if (this.outdated) {
         itextcomponent = INCOMPATIBLE_TITLE;
         aitextcomponent = INCOMPATIBLE_MESSAGES;
      } else {
         itextcomponent = OUTDATED_TITLE;
         aitextcomponent = OUTDATED_MESSAGES;
      }

      drawCenteredString(pMatrixStack, this.font, itextcomponent, this.width / 2, row(3), 16711680);

      for(int i = 0; i < aitextcomponent.length; ++i) {
         drawCenteredString(pMatrixStack, this.font, aitextcomponent[i], this.width / 2, row(5) + i * 12, 16777215);
      }

      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode != 257 && pKeyCode != 335 && pKeyCode != 256) {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      } else {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      }
   }
}