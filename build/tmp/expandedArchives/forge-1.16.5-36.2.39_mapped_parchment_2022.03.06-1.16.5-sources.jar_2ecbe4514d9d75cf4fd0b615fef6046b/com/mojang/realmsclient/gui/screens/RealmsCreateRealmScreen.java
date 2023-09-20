package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.action.CreateWorldRealmsAction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
   private static final ITextComponent NAME_LABEL = new TranslationTextComponent("mco.configure.world.name");
   private static final ITextComponent DESCRIPTION_LABEL = new TranslationTextComponent("mco.configure.world.description");
   private final RealmsServer server;
   private final RealmsMainScreen lastScreen;
   private TextFieldWidget nameBox;
   private TextFieldWidget descriptionBox;
   private Button createButton;
   private RealmsLabel createRealmLabel;

   public RealmsCreateRealmScreen(RealmsServer p_i51772_1_, RealmsMainScreen p_i51772_2_) {
      this.server = p_i51772_1_;
      this.lastScreen = p_i51772_2_;
   }

   public void tick() {
      if (this.nameBox != null) {
         this.nameBox.tick();
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.tick();
      }

   }

   public void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.createButton = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 17, 97, 20, new TranslationTextComponent("mco.create.world"), (p_237828_1_) -> {
         this.createWorld();
      }));
      this.addButton(new Button(this.width / 2 + 5, this.height / 4 + 120 + 17, 95, 20, DialogTexts.GUI_CANCEL, (p_237827_1_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
      this.createButton.active = false;
      this.nameBox = new TextFieldWidget(this.minecraft.font, this.width / 2 - 100, 65, 200, 20, (TextFieldWidget)null, new TranslationTextComponent("mco.configure.world.name"));
      this.addWidget(this.nameBox);
      this.setInitialFocus(this.nameBox);
      this.descriptionBox = new TextFieldWidget(this.minecraft.font, this.width / 2 - 100, 115, 200, 20, (TextFieldWidget)null, new TranslationTextComponent("mco.configure.world.description"));
      this.addWidget(this.descriptionBox);
      this.createRealmLabel = new RealmsLabel(new TranslationTextComponent("mco.selectServer.create"), this.width / 2, 11, 16777215);
      this.addWidget(this.createRealmLabel);
      this.narrateLabels();
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public boolean charTyped(char pCodePoint, int pModifiers) {
      boolean flag = super.charTyped(pCodePoint, pModifiers);
      this.createButton.active = this.valid();
      return flag;
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         boolean flag = super.keyPressed(pKeyCode, pScanCode, pModifiers);
         this.createButton.active = this.valid();
         return flag;
      }
   }

   private void createWorld() {
      if (this.valid()) {
         RealmsResetWorldScreen realmsresetworldscreen = new RealmsResetWorldScreen(this.lastScreen, this.server, new TranslationTextComponent("mco.selectServer.create"), new TranslationTextComponent("mco.create.world.subtitle"), 10526880, new TranslationTextComponent("mco.create.world.skip"), () -> {
            this.minecraft.setScreen(this.lastScreen.newScreen());
         }, () -> {
            this.minecraft.setScreen(this.lastScreen.newScreen());
         });
         realmsresetworldscreen.setResetTitle(new TranslationTextComponent("mco.create.world.reset.title"));
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new CreateWorldRealmsAction(this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), realmsresetworldscreen)));
      }

   }

   private boolean valid() {
      return !this.nameBox.getValue().trim().isEmpty();
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      this.createRealmLabel.render(this, pMatrixStack);
      this.font.draw(pMatrixStack, NAME_LABEL, (float)(this.width / 2 - 100), 52.0F, 10526880);
      this.font.draw(pMatrixStack, DESCRIPTION_LABEL, (float)(this.width / 2 - 100), 102.0F, 10526880);
      if (this.nameBox != null) {
         this.nameBox.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      }

      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }
}