package net.minecraft.client.gui;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DialogTexts {
   public static final ITextComponent OPTION_ON = new TranslationTextComponent("options.on");
   public static final ITextComponent OPTION_OFF = new TranslationTextComponent("options.off");
   public static final ITextComponent GUI_DONE = new TranslationTextComponent("gui.done");
   public static final ITextComponent GUI_CANCEL = new TranslationTextComponent("gui.cancel");
   public static final ITextComponent GUI_YES = new TranslationTextComponent("gui.yes");
   public static final ITextComponent GUI_NO = new TranslationTextComponent("gui.no");
   public static final ITextComponent GUI_PROCEED = new TranslationTextComponent("gui.proceed");
   public static final ITextComponent GUI_BACK = new TranslationTextComponent("gui.back");
   public static final ITextComponent CONNECT_FAILED = new TranslationTextComponent("connect.failed");

   public static ITextComponent optionStatus(boolean pIsEnabled) {
      return pIsEnabled ? OPTION_ON : OPTION_OFF;
   }

   public static IFormattableTextComponent optionStatus(ITextComponent pMessage, boolean pComposed) {
      return new TranslationTextComponent(pComposed ? "options.on.composed" : "options.off.composed", pMessage);
   }
}