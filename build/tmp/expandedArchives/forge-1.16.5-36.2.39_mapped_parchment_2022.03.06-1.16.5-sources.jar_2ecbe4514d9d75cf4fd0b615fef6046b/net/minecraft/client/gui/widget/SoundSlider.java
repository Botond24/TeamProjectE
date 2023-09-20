package net.minecraft.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundSlider extends GameSettingsSlider {
   private final SoundCategory source;

   public SoundSlider(Minecraft pMinecraft, int pX, int pY, SoundCategory pSource, int pWidth) {
      super(pMinecraft.options, pX, pY, pWidth, 20, (double)pMinecraft.options.getSoundSourceVolume(pSource));
      this.source = pSource;
      this.updateMessage();
   }

   protected void updateMessage() {
      ITextComponent itextcomponent = (ITextComponent)((float)this.value == (float)this.getYImage(false) ? DialogTexts.OPTION_OFF : new StringTextComponent((int)(this.value * 100.0D) + "%"));
      this.setMessage((new TranslationTextComponent("soundCategory." + this.source.getName())).append(": ").append(itextcomponent));
   }

   protected void applyValue() {
      this.options.setSoundCategoryVolume(this.source, (float)this.value);
      this.options.save();
   }
}