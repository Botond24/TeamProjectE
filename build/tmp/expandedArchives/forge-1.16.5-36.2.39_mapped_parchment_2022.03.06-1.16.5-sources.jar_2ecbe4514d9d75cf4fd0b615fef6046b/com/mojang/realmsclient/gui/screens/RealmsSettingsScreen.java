package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSettingsScreen extends RealmsScreen {
   private static final ITextComponent NAME_LABEL = new TranslationTextComponent("mco.configure.world.name");
   private static final ITextComponent DESCRIPTION_LABEL = new TranslationTextComponent("mco.configure.world.description");
   private final RealmsConfigureWorldScreen configureWorldScreen;
   private final RealmsServer serverData;
   private Button doneButton;
   private TextFieldWidget descEdit;
   private TextFieldWidget nameEdit;
   private RealmsLabel titleLabel;

   public RealmsSettingsScreen(RealmsConfigureWorldScreen p_i51751_1_, RealmsServer p_i51751_2_) {
      this.configureWorldScreen = p_i51751_1_;
      this.serverData = p_i51751_2_;
   }

   public void tick() {
      this.nameEdit.tick();
      this.descEdit.tick();
      this.doneButton.active = !this.nameEdit.getValue().trim().isEmpty();
   }

   public void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      int i = this.width / 2 - 106;
      this.doneButton = this.addButton(new Button(i - 2, row(12), 106, 20, new TranslationTextComponent("mco.configure.world.buttons.done"), (p_238033_1_) -> {
         this.save();
      }));
      this.addButton(new Button(this.width / 2 + 2, row(12), 106, 20, DialogTexts.GUI_CANCEL, (p_238032_1_) -> {
         this.minecraft.setScreen(this.configureWorldScreen);
      }));
      String s = this.serverData.state == RealmsServer.Status.OPEN ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open";
      Button button = new Button(this.width / 2 - 53, row(0), 106, 20, new TranslationTextComponent(s), (p_238031_1_) -> {
         if (this.serverData.state == RealmsServer.Status.OPEN) {
            ITextComponent itextcomponent = new TranslationTextComponent("mco.configure.world.close.question.line1");
            ITextComponent itextcomponent1 = new TranslationTextComponent("mco.configure.world.close.question.line2");
            this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_238034_1_) -> {
               if (p_238034_1_) {
                  this.configureWorldScreen.closeTheWorld(this);
               } else {
                  this.minecraft.setScreen(this);
               }

            }, RealmsLongConfirmationScreen.Type.Info, itextcomponent, itextcomponent1, true));
         } else {
            this.configureWorldScreen.openTheWorld(false, this);
         }

      });
      this.addButton(button);
      this.nameEdit = new TextFieldWidget(this.minecraft.font, i, row(4), 212, 20, (TextFieldWidget)null, new TranslationTextComponent("mco.configure.world.name"));
      this.nameEdit.setMaxLength(32);
      this.nameEdit.setValue(this.serverData.getName());
      this.addWidget(this.nameEdit);
      this.magicalSpecialHackyFocus(this.nameEdit);
      this.descEdit = new TextFieldWidget(this.minecraft.font, i, row(8), 212, 20, (TextFieldWidget)null, new TranslationTextComponent("mco.configure.world.description"));
      this.descEdit.setMaxLength(32);
      this.descEdit.setValue(this.serverData.getDescription());
      this.addWidget(this.descEdit);
      this.titleLabel = this.addWidget(new RealmsLabel(new TranslationTextComponent("mco.configure.world.settings.title"), this.width / 2, 17, 16777215));
      this.narrateLabels();
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.minecraft.setScreen(this.configureWorldScreen);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      this.titleLabel.render(this, pMatrixStack);
      this.font.draw(pMatrixStack, NAME_LABEL, (float)(this.width / 2 - 106), (float)row(3), 10526880);
      this.font.draw(pMatrixStack, DESCRIPTION_LABEL, (float)(this.width / 2 - 106), (float)row(7), 10526880);
      this.nameEdit.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      this.descEdit.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   public void save() {
      this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
   }
}