package net.minecraft.client.gui.widget.list;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public class KeyBindingList extends AbstractOptionList<KeyBindingList.Entry> {
   private final ControlsScreen controlsScreen;
   private int maxNameWidth;

   public KeyBindingList(ControlsScreen p_i45031_1_, Minecraft p_i45031_2_) {
      super(p_i45031_2_, p_i45031_1_.width + 45, p_i45031_1_.height, 43, p_i45031_1_.height - 32, 20);
      this.controlsScreen = p_i45031_1_;
      KeyBinding[] akeybinding = ArrayUtils.clone(p_i45031_2_.options.keyMappings);
      Arrays.sort((Object[])akeybinding);
      String s = null;

      for(KeyBinding keybinding : akeybinding) {
         String s1 = keybinding.getCategory();
         if (!s1.equals(s)) {
            s = s1;
            this.addEntry(new KeyBindingList.CategoryEntry(new TranslationTextComponent(s1)));
         }

         ITextComponent itextcomponent = new TranslationTextComponent(keybinding.getName());
         int i = p_i45031_2_.font.width(itextcomponent);
         if (i > this.maxNameWidth) {
            this.maxNameWidth = i;
         }

         this.addEntry(new KeyBindingList.KeyEntry(keybinding, itextcomponent));
      }

   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 15 + 20;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 32;
   }

   @OnlyIn(Dist.CLIENT)
   public class CategoryEntry extends KeyBindingList.Entry {
      private final ITextComponent name;
      private final int width;

      public CategoryEntry(ITextComponent p_i232280_2_) {
         this.name = p_i232280_2_;
         this.width = KeyBindingList.this.minecraft.font.width(this.name);
      }

      public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
         KeyBindingList.this.minecraft.font.draw(pMatrixStack, this.name, (float)(KeyBindingList.this.minecraft.screen.width / 2 - this.width / 2), (float)(pTop + pHeight - 9 - 1), 16777215);
      }

      public boolean changeFocus(boolean pFocus) {
         return false;
      }

      public List<? extends IGuiEventListener> children() {
         return Collections.emptyList();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public abstract static class Entry extends AbstractOptionList.Entry<KeyBindingList.Entry> {
   }

   @OnlyIn(Dist.CLIENT)
   public class KeyEntry extends KeyBindingList.Entry {
      /** The keybinding specified for this KeyEntry */
      private final KeyBinding key;
      /** The localized key description for this KeyEntry */
      private final ITextComponent name;
      private final Button changeButton;
      private final Button resetButton;

      private KeyEntry(final KeyBinding p_i232281_2_, final ITextComponent p_i232281_3_) {
         this.key = p_i232281_2_;
         this.name = p_i232281_3_;
         this.changeButton = new Button(0, 0, 75 + 20 /*Forge: add space*/, 20, p_i232281_3_, (p_214386_2_) -> {
            KeyBindingList.this.controlsScreen.selectedKey = p_i232281_2_;
         }) {
            protected IFormattableTextComponent createNarrationMessage() {
               return p_i232281_2_.isUnbound() ? new TranslationTextComponent("narrator.controls.unbound", p_i232281_3_) : new TranslationTextComponent("narrator.controls.bound", p_i232281_3_, super.createNarrationMessage());
            }
         };
         this.resetButton = new Button(0, 0, 50, 20, new TranslationTextComponent("controls.reset"), (p_214387_2_) -> {
            key.setToDefault();
            KeyBindingList.this.minecraft.options.setKey(p_i232281_2_, p_i232281_2_.getDefaultKey());
            KeyBinding.resetMapping();
         }) {
            protected IFormattableTextComponent createNarrationMessage() {
               return new TranslationTextComponent("narrator.controls.reset", p_i232281_3_);
            }
         };
      }

      public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
         boolean flag = KeyBindingList.this.controlsScreen.selectedKey == this.key;
         KeyBindingList.this.minecraft.font.draw(pMatrixStack, this.name, (float)(pLeft + 90 - KeyBindingList.this.maxNameWidth), (float)(pTop + pHeight / 2 - 9 / 2), 16777215);
         this.resetButton.x = pLeft + 190 + 20;
         this.resetButton.y = pTop;
         this.resetButton.active = !this.key.isDefault();
         this.resetButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         this.changeButton.x = pLeft + 105;
         this.changeButton.y = pTop;
         this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
         boolean flag1 = false;
         boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
         if (!this.key.isUnbound()) {
            for(KeyBinding keybinding : KeyBindingList.this.minecraft.options.keyMappings) {
               if (keybinding != this.key && this.key.same(keybinding)) {
                  flag1 = true;
                  keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.key);
               }
            }
         }

         if (flag) {
            this.changeButton.setMessage((new StringTextComponent("> ")).append(this.changeButton.getMessage().copy().withStyle(TextFormatting.YELLOW)).append(" <").withStyle(TextFormatting.YELLOW));
         } else if (flag1) {
            this.changeButton.setMessage(this.changeButton.getMessage().copy().withStyle(keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED));
         }

         this.changeButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      }

      public List<? extends IGuiEventListener> children() {
         return ImmutableList.of(this.changeButton, this.resetButton);
      }

      public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
         if (this.changeButton.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
         } else {
            return this.resetButton.mouseClicked(pMouseX, pMouseY, pButton);
         }
      }

      public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
         return this.changeButton.mouseReleased(pMouseX, pMouseY, pButton) || this.resetButton.mouseReleased(pMouseX, pMouseY, pButton);
      }
   }
}