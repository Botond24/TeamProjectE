package net.minecraft.client.gui.widget.list;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.OptionButton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsRowList extends AbstractOptionList<OptionsRowList.Row> {
   public OptionsRowList(Minecraft p_i51130_1_, int p_i51130_2_, int p_i51130_3_, int p_i51130_4_, int p_i51130_5_, int p_i51130_6_) {
      super(p_i51130_1_, p_i51130_2_, p_i51130_3_, p_i51130_4_, p_i51130_5_, p_i51130_6_);
      this.centerListVertically = false;
   }

   public int addBig(AbstractOption pOption) {
      return this.addEntry(OptionsRowList.Row.big(this.minecraft.options, this.width, pOption));
   }

   public void addSmall(AbstractOption pLeftOption, @Nullable AbstractOption pRightOption) {
      this.addEntry(OptionsRowList.Row.small(this.minecraft.options, this.width, pLeftOption, pRightOption));
   }

   public void addSmall(AbstractOption[] pOptions) {
      for(int i = 0; i < pOptions.length; i += 2) {
         this.addSmall(pOptions[i], i < pOptions.length - 1 ? pOptions[i + 1] : null);
      }

   }

   public int getRowWidth() {
      return 400;
   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 32;
   }

   @Nullable
   public Widget findOption(AbstractOption pOption) {
      for(OptionsRowList.Row optionsrowlist$row : this.children()) {
         for(Widget widget : optionsrowlist$row.children) {
            if (widget instanceof OptionButton && ((OptionButton)widget).getOption() == pOption) {
               return widget;
            }
         }
      }

      return null;
   }

   public Optional<Widget> getMouseOver(double pMouseX, double pMouseY) {
      for(OptionsRowList.Row optionsrowlist$row : this.children()) {
         for(Widget widget : optionsrowlist$row.children) {
            if (widget.isMouseOver(pMouseX, pMouseY)) {
               return Optional.of(widget);
            }
         }
      }

      return Optional.empty();
   }

   @OnlyIn(Dist.CLIENT)
   public static class Row extends AbstractOptionList.Entry<OptionsRowList.Row> {
      private final List<Widget> children;

      private Row(List<Widget> p_i50481_1_) {
         this.children = p_i50481_1_;
      }

      /**
       * Creates an options row with button for the specified option
       */
      public static OptionsRowList.Row big(GameSettings pSettings, int pGuiWidth, AbstractOption pOption) {
         return new OptionsRowList.Row(ImmutableList.of(pOption.createButton(pSettings, pGuiWidth / 2 - 155, 0, 310)));
      }

      /**
       * Creates an options row with 1 or 2 buttons for specified options
       */
      public static OptionsRowList.Row small(GameSettings pSettings, int pGuiWidth, AbstractOption pLeftOption, @Nullable AbstractOption pRightOption) {
         Widget widget = pLeftOption.createButton(pSettings, pGuiWidth / 2 - 155, 0, 150);
         return pRightOption == null ? new OptionsRowList.Row(ImmutableList.of(widget)) : new OptionsRowList.Row(ImmutableList.of(widget, pRightOption.createButton(pSettings, pGuiWidth / 2 - 155 + 160, 0, 150)));
      }

      public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
         this.children.forEach((p_238519_5_) -> {
            p_238519_5_.y = pTop;
            p_238519_5_.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         });
      }

      public List<? extends IGuiEventListener> children() {
         return this.children;
      }
   }
}