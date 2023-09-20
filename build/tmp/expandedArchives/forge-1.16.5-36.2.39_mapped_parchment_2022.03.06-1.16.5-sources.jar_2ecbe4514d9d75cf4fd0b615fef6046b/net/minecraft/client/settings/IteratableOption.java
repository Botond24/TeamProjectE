package net.minecraft.client.settings;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.OptionButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IteratableOption extends AbstractOption {
   private final BiConsumer<GameSettings, Integer> setter;
   private final BiFunction<GameSettings, IteratableOption, ITextComponent> toString;

   public IteratableOption(String p_i51164_1_, BiConsumer<GameSettings, Integer> p_i51164_2_, BiFunction<GameSettings, IteratableOption, ITextComponent> p_i51164_3_) {
      super(p_i51164_1_);
      this.setter = p_i51164_2_;
      this.toString = p_i51164_3_;
   }

   public void toggle(GameSettings p_216722_1_, int p_216722_2_) {
      this.setter.accept(p_216722_1_, p_216722_2_);
      p_216722_1_.save();
   }

   public Widget createButton(GameSettings pOptions, int pX, int pY, int pWidth) {
      return new OptionButton(pX, pY, pWidth, 20, this, this.getMessage(pOptions), (p_216721_2_) -> {
         this.toggle(pOptions, 1);
         p_216721_2_.setMessage(this.getMessage(pOptions));
      });
   }

   public ITextComponent getMessage(GameSettings p_238157_1_) {
      return this.toString.apply(p_238157_1_, this);
   }
}