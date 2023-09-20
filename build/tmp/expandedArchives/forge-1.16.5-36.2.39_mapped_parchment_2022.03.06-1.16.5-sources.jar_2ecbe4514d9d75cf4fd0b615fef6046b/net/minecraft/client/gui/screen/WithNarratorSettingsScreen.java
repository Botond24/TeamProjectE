package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class WithNarratorSettingsScreen extends SettingsScreen {
   private final AbstractOption[] smallOptions;
   @Nullable
   private Widget narratorButton;
   private OptionsRowList list;

   public WithNarratorSettingsScreen(Screen p_i242058_1_, GameSettings p_i242058_2_, ITextComponent p_i242058_3_, AbstractOption[] p_i242058_4_) {
      super(p_i242058_1_, p_i242058_2_, p_i242058_3_);
      this.smallOptions = p_i242058_4_;
   }

   protected void init() {
      this.list = new OptionsRowList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
      this.list.addSmall(this.smallOptions);
      this.children.add(this.list);
      this.createFooter();
      this.narratorButton = this.list.findOption(AbstractOption.NARRATOR);
      if (this.narratorButton != null) {
         this.narratorButton.active = NarratorChatListener.INSTANCE.isActive();
      }

   }

   protected void createFooter() {
      this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, DialogTexts.GUI_DONE, (p_243316_1_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      this.list.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 20, 16777215);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      List<IReorderingProcessor> list = tooltipAt(this.list, pMouseX, pMouseY);
      if (list != null) {
         this.renderTooltip(pMatrixStack, list, pMouseX, pMouseY);
      }

   }

   public void updateNarratorButton() {
      if (this.narratorButton != null) {
         this.narratorButton.setMessage(AbstractOption.NARRATOR.getMessage(this.options));
      }

   }
}