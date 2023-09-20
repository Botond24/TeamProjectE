package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.OptionButton;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageScreen extends SettingsScreen {
   private static final ITextComponent WARNING_LABEL = (new StringTextComponent("(")).append(new TranslationTextComponent("options.languageWarning")).append(")").withStyle(TextFormatting.GRAY);
   /** The List GuiSlot object reference. */
   private LanguageScreen.List packSelectionList;
   /** Reference to the LanguageManager object. */
   private final LanguageManager languageManager;
   private OptionButton forceUnicodeButton;
   private Button doneButton;

   public LanguageScreen(Screen pScreen, GameSettings pOptions, LanguageManager pLanguageManager) {
      super(pScreen, pOptions, new TranslationTextComponent("options.language"));
      this.languageManager = pLanguageManager;
   }

   protected void init() {
      this.packSelectionList = new LanguageScreen.List(this.minecraft);
      this.children.add(this.packSelectionList);
      this.forceUnicodeButton = this.addButton(new OptionButton(this.width / 2 - 155, this.height - 38, 150, 20, AbstractOption.FORCE_UNICODE_FONT, AbstractOption.FORCE_UNICODE_FONT.getMessage(this.options), (p_213037_1_) -> {
         AbstractOption.FORCE_UNICODE_FONT.toggle(this.options);
         this.options.save();
         p_213037_1_.setMessage(AbstractOption.FORCE_UNICODE_FONT.getMessage(this.options));
         this.minecraft.resizeDisplay();
      }));
      this.doneButton = this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 38, 150, 20, DialogTexts.GUI_DONE, (p_213036_1_) -> {
         LanguageScreen.List.LanguageEntry languagescreen$list$languageentry = this.packSelectionList.getSelected();
         if (languagescreen$list$languageentry != null && !languagescreen$list$languageentry.language.getCode().equals(this.languageManager.getSelected().getCode())) {
            this.languageManager.setSelected(languagescreen$list$languageentry.language);
            this.options.languageCode = languagescreen$list$languageentry.language.getCode();
            net.minecraftforge.client.ForgeHooksClient.refreshResources(this.minecraft, net.minecraftforge.resource.VanillaResourceType.LANGUAGES);
            this.doneButton.setMessage(DialogTexts.GUI_DONE);
            this.forceUnicodeButton.setMessage(AbstractOption.FORCE_UNICODE_FONT.getMessage(this.options));
            this.options.save();
         }

         this.minecraft.setScreen(this.lastScreen);
      }));
      super.init();
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.packSelectionList.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 16, 16777215);
      drawCenteredString(pMatrixStack, this.font, WARNING_LABEL, this.width / 2, this.height - 56, 8421504);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   @OnlyIn(Dist.CLIENT)
   class List extends ExtendedList<LanguageScreen.List.LanguageEntry> {
      public List(Minecraft p_i45519_2_) {
         super(p_i45519_2_, LanguageScreen.this.width, LanguageScreen.this.height, 32, LanguageScreen.this.height - 65 + 4, 18);

         for(Language language : LanguageScreen.this.languageManager.getLanguages()) {
            LanguageScreen.List.LanguageEntry languagescreen$list$languageentry = new LanguageScreen.List.LanguageEntry(language);
            this.addEntry(languagescreen$list$languageentry);
            if (LanguageScreen.this.languageManager.getSelected().getCode().equals(language.getCode())) {
               this.setSelected(languagescreen$list$languageentry);
            }
         }

         if (this.getSelected() != null) {
            this.centerScrollOn(this.getSelected());
         }

      }

      protected int getScrollbarPosition() {
         return super.getScrollbarPosition() + 20;
      }

      public int getRowWidth() {
         return super.getRowWidth() + 50;
      }

      public void setSelected(@Nullable LanguageScreen.List.LanguageEntry pEntry) {
         super.setSelected(pEntry);
         if (pEntry != null) {
            NarratorChatListener.INSTANCE.sayNow((new TranslationTextComponent("narrator.select", pEntry.language)).getString());
         }

      }

      protected void renderBackground(MatrixStack pMatrixStack) {
         LanguageScreen.this.renderBackground(pMatrixStack);
      }

      protected boolean isFocused() {
         return LanguageScreen.this.getFocused() == this;
      }

      @OnlyIn(Dist.CLIENT)
      public class LanguageEntry extends ExtendedList.AbstractListEntry<LanguageScreen.List.LanguageEntry> {
         private final Language language;

         public LanguageEntry(Language pLanguage) {
            this.language = pLanguage;
         }

         public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            String s = this.language.toString();
            LanguageScreen.this.font.drawShadow(pMatrixStack, s, (float)(List.this.width / 2 - LanguageScreen.this.font.width(s) / 2), (float)(pTop + 1), 16777215, true);
         }

         public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (pButton == 0) {
               this.select();
               return true;
            } else {
               return false;
            }
         }

         private void select() {
            List.this.setSelected(this);
         }
      }
   }
}
