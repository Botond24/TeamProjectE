package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldSelectionScreen extends Screen {
   protected final Screen lastScreen;
   private List<IReorderingProcessor> toolTip;
   private Button deleteButton;
   private Button selectButton;
   private Button renameButton;
   private Button copyButton;
   protected TextFieldWidget searchBox;
   private WorldSelectionList list;

   public WorldSelectionScreen(Screen p_i46592_1_) {
      super(new TranslationTextComponent("selectWorld.title"));
      this.lastScreen = p_i46592_1_;
   }

   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      return super.mouseScrolled(pMouseX, pMouseY, pDelta);
   }

   public void tick() {
      this.searchBox.tick();
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.searchBox = new TextFieldWidget(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslationTextComponent("selectWorld.search"));
      this.searchBox.setResponder((p_214329_1_) -> {
         this.list.refreshList(() -> {
            return p_214329_1_;
         }, false);
      });
      this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, () -> {
         return this.searchBox.getValue();
      }, this.list);
      this.children.add(this.searchBox);
      this.children.add(this.list);
      this.selectButton = this.addButton(new Button(this.width / 2 - 154, this.height - 52, 150, 20, new TranslationTextComponent("selectWorld.select"), (p_214325_1_) -> {
         this.list.getSelectedOpt().ifPresent(WorldSelectionList.Entry::joinWorld);
      }));
      this.addButton(new Button(this.width / 2 + 4, this.height - 52, 150, 20, new TranslationTextComponent("selectWorld.create"), (p_214326_1_) -> {
         this.minecraft.setScreen(CreateWorldScreen.create(this));
      }));
      this.renameButton = this.addButton(new Button(this.width / 2 - 154, this.height - 28, 72, 20, new TranslationTextComponent("selectWorld.edit"), (p_214323_1_) -> {
         this.list.getSelectedOpt().ifPresent(WorldSelectionList.Entry::editWorld);
      }));
      this.deleteButton = this.addButton(new Button(this.width / 2 - 76, this.height - 28, 72, 20, new TranslationTextComponent("selectWorld.delete"), (p_214330_1_) -> {
         this.list.getSelectedOpt().ifPresent(WorldSelectionList.Entry::deleteWorld);
      }));
      this.copyButton = this.addButton(new Button(this.width / 2 + 4, this.height - 28, 72, 20, new TranslationTextComponent("selectWorld.recreate"), (p_214328_1_) -> {
         this.list.getSelectedOpt().ifPresent(WorldSelectionList.Entry::recreateWorld);
      }));
      this.addButton(new Button(this.width / 2 + 82, this.height - 28, 72, 20, DialogTexts.GUI_CANCEL, (p_214327_1_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
      this.updateButtonStatus(false);
      this.setInitialFocus(this.searchBox);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      return super.keyPressed(pKeyCode, pScanCode, pModifiers) ? true : this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public boolean charTyped(char pCodePoint, int pModifiers) {
      return this.searchBox.charTyped(pCodePoint, pModifiers);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.toolTip = null;
      this.list.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      this.searchBox.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 8, 16777215);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      if (this.toolTip != null) {
         this.renderTooltip(pMatrixStack, this.toolTip, pMouseX, pMouseY);
      }

   }

   public void setToolTip(List<IReorderingProcessor> p_239026_1_) {
      this.toolTip = p_239026_1_;
   }

   public void updateButtonStatus(boolean p_214324_1_) {
      this.selectButton.active = p_214324_1_;
      this.deleteButton.active = p_214324_1_;
      this.renameButton.active = p_214324_1_;
      this.copyButton.active = p_214324_1_;
   }

   public void removed() {
      if (this.list != null) {
         this.list.children().forEach(WorldSelectionList.Entry::close);
      }

   }
}