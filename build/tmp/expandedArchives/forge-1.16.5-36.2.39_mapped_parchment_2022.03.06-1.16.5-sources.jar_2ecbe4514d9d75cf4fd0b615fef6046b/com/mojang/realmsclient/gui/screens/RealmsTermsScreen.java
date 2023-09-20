package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.action.ConnectingToRealmsAction;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsTermsScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ITextComponent TITLE = new TranslationTextComponent("mco.terms.title");
   private static final ITextComponent TERMS_STATIC_TEXT = new TranslationTextComponent("mco.terms.sentence.1");
   private static final ITextComponent TERMS_LINK_TEXT = (new StringTextComponent(" ")).append((new TranslationTextComponent("mco.terms.sentence.2")).withStyle(Style.EMPTY.withUnderlined(true)));
   private final Screen lastScreen;
   private final RealmsMainScreen mainScreen;
   /**
    * The screen to display when OK is clicked on the disconnect screen.
    * 
    * Seems to be either null (integrated server) or an instance of either {@link MultiplayerScreen} (when connecting to
    * a server) or {@link com.mojang.realmsclient.gui.screens.RealmsTermsScreen} (when connecting to MCO server)
    */
   private final RealmsServer realmsServer;
   private boolean onLink;
   private final String realmsToSUrl = "https://aka.ms/MinecraftRealmsTerms";

   public RealmsTermsScreen(Screen p_i232225_1_, RealmsMainScreen p_i232225_2_, RealmsServer p_i232225_3_) {
      this.lastScreen = p_i232225_1_;
      this.mainScreen = p_i232225_2_;
      this.realmsServer = p_i232225_3_;
   }

   public void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      int i = this.width / 4 - 2;
      this.addButton(new Button(this.width / 4, row(12), i, 20, new TranslationTextComponent("mco.terms.buttons.agree"), (p_238078_1_) -> {
         this.agreedToTos();
      }));
      this.addButton(new Button(this.width / 2 + 4, row(12), i, 20, new TranslationTextComponent("mco.terms.buttons.disagree"), (p_238077_1_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   private void agreedToTos() {
      RealmsClient realmsclient = RealmsClient.create();

      try {
         realmsclient.agreeToTos();
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectingToRealmsAction(this.mainScreen, this.lastScreen, this.realmsServer, new ReentrantLock())));
      } catch (RealmsServiceException realmsserviceexception) {
         LOGGER.error("Couldn't agree to TOS");
      }

   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.onLink) {
         this.minecraft.keyboardHandler.setClipboard("https://aka.ms/MinecraftRealmsTerms");
         Util.getPlatform().openUri("https://aka.ms/MinecraftRealmsTerms");
         return true;
      } else {
         return super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   public String getNarrationMessage() {
      return super.getNarrationMessage() + ". " + TERMS_STATIC_TEXT.getString() + " " + TERMS_LINK_TEXT.getString();
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      drawCenteredString(pMatrixStack, this.font, TITLE, this.width / 2, 17, 16777215);
      this.font.draw(pMatrixStack, TERMS_STATIC_TEXT, (float)(this.width / 2 - 120), (float)row(5), 16777215);
      int i = this.font.width(TERMS_STATIC_TEXT);
      int j = this.width / 2 - 121 + i;
      int k = row(5);
      int l = j + this.font.width(TERMS_LINK_TEXT) + 1;
      int i1 = k + 1 + 9;
      this.onLink = j <= pMouseX && pMouseX <= l && k <= pMouseY && pMouseY <= i1;
      this.font.draw(pMatrixStack, TERMS_LINK_TEXT, (float)(this.width / 2 - 120 + i), (float)row(5), this.onLink ? 7107012 : 3368635);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }
}