package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.SoundSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.OptionButton;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsSoundsScreen extends SettingsScreen {
   public OptionsSoundsScreen(Screen pLastScreen, GameSettings pOptions) {
      super(pLastScreen, pOptions, new TranslationTextComponent("options.sounds.title"));
   }

   protected void init() {
      int i = 0;
      this.addButton(new SoundSlider(this.minecraft, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), SoundCategory.MASTER, 310));
      i = i + 2;

      for(SoundCategory soundcategory : SoundCategory.values()) {
         if (soundcategory != SoundCategory.MASTER) {
            this.addButton(new SoundSlider(this.minecraft, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), soundcategory, 150));
            ++i;
         }
      }

      int j = this.width / 2 - 75;
      int k = this.height / 6 - 12;
      ++i;
      this.addButton(new OptionButton(j, k + 24 * (i >> 1), 150, 20, AbstractOption.SHOW_SUBTITLES, AbstractOption.SHOW_SUBTITLES.getMessage(this.options), (p_213105_1_) -> {
         AbstractOption.SHOW_SUBTITLES.toggle(this.minecraft.options);
         p_213105_1_.setMessage(AbstractOption.SHOW_SUBTITLES.getMessage(this.minecraft.options));
         this.minecraft.options.save();
      }));
      this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, DialogTexts.GUI_DONE, (p_213104_1_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 15, 16777215);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }
}