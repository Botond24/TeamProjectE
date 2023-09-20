package net.minecraft.client.settings;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.widget.OptionSlider;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SliderPercentageOption extends AbstractOption {
   protected final float steps;
   protected final double minValue;
   protected double maxValue;
   private final Function<GameSettings, Double> getter;
   private final BiConsumer<GameSettings, Double> setter;
   private final BiFunction<GameSettings, SliderPercentageOption, ITextComponent> toString;

   public SliderPercentageOption(String p_i51155_1_, double p_i51155_2_, double p_i51155_4_, float p_i51155_6_, Function<GameSettings, Double> p_i51155_7_, BiConsumer<GameSettings, Double> p_i51155_8_, BiFunction<GameSettings, SliderPercentageOption, ITextComponent> p_i51155_9_) {
      super(p_i51155_1_);
      this.minValue = p_i51155_2_;
      this.maxValue = p_i51155_4_;
      this.steps = p_i51155_6_;
      this.getter = p_i51155_7_;
      this.setter = p_i51155_8_;
      this.toString = p_i51155_9_;
   }

   public Widget createButton(GameSettings pOptions, int pX, int pY, int pWidth) {
      return new OptionSlider(pOptions, pX, pY, pWidth, 20, this);
   }

   public double toPct(double pValue) {
      return MathHelper.clamp((this.clamp(pValue) - this.minValue) / (this.maxValue - this.minValue), 0.0D, 1.0D);
   }

   public double toValue(double pValue) {
      return this.clamp(MathHelper.lerp(MathHelper.clamp(pValue, 0.0D, 1.0D), this.minValue, this.maxValue));
   }

   private double clamp(double pValue) {
      if (this.steps > 0.0F) {
         pValue = (double)(this.steps * (float)Math.round(pValue / (double)this.steps));
      }

      return MathHelper.clamp(pValue, this.minValue, this.maxValue);
   }

   public double getMinValue() {
      return this.minValue;
   }

   public double getMaxValue() {
      return this.maxValue;
   }

   public void setMaxValue(float pValue) {
      this.maxValue = (double)pValue;
   }

   public void set(GameSettings pOptions, double pValue) {
      this.setter.accept(pOptions, pValue);
   }

   public double get(GameSettings pOptions) {
      return this.getter.apply(pOptions);
   }

   public ITextComponent getMessage(GameSettings p_238334_1_) {
      return this.toString.apply(p_238334_1_, this);
   }
}