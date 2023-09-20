package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerListScreen extends Screen {
   private static final ITextComponent ENTER_IP_LABEL = new TranslationTextComponent("addServer.enterIp");
   private Button selectButton;
   private final ServerData serverData;
   private TextFieldWidget ipEdit;
   private final BooleanConsumer callback;
   private final Screen lastScreen;

   public ServerListScreen(Screen p_i225926_1_, BooleanConsumer p_i225926_2_, ServerData p_i225926_3_) {
      super(new TranslationTextComponent("selectServer.direct"));
      this.lastScreen = p_i225926_1_;
      this.serverData = p_i225926_3_;
      this.callback = p_i225926_2_;
   }

   public void tick() {
      this.ipEdit.tick();
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.getFocused() != this.ipEdit || pKeyCode != 257 && pKeyCode != 335) {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      } else {
         this.onSelect();
         return true;
      }
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.selectButton = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20, new TranslationTextComponent("selectServer.select"), (p_213026_1_) -> {
         this.onSelect();
      }));
      this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, DialogTexts.GUI_CANCEL, (p_213025_1_) -> {
         this.callback.accept(false);
      }));
      this.ipEdit = new TextFieldWidget(this.font, this.width / 2 - 100, 116, 200, 20, new TranslationTextComponent("addServer.enterIp"));
      this.ipEdit.setMaxLength(128);
      this.ipEdit.setFocus(true);
      this.ipEdit.setValue(this.minecraft.options.lastMpIp);
      this.ipEdit.setResponder((p_213024_1_) -> {
         this.updateSelectButtonStatus();
      });
      this.children.add(this.ipEdit);
      this.setInitialFocus(this.ipEdit);
      this.updateSelectButtonStatus();
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      String s = this.ipEdit.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.ipEdit.setValue(s);
   }

   private void onSelect() {
      this.serverData.ip = this.ipEdit.getValue();
      this.callback.accept(true);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
      this.minecraft.options.lastMpIp = this.ipEdit.getValue();
      this.minecraft.options.save();
   }

   private void updateSelectButtonStatus() {
      String s = this.ipEdit.getValue();
      this.selectButton.active = !s.isEmpty() && s.split(":").length > 0 && s.indexOf(32) == -1;
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 20, 16777215);
      drawString(pMatrixStack, this.font, ENTER_IP_LABEL, this.width / 2 - 100, 100, 10526880);
      this.ipEdit.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }
}