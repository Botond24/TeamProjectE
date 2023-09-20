package net.minecraft.util.text;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class Color {
   private static final Map<TextFormatting, Color> LEGACY_FORMAT_TO_COLOR = Stream.of(TextFormatting.values()).filter(TextFormatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), (p_240748_0_) -> {
      return new Color(p_240748_0_.getColor(), p_240748_0_.getName());
   }));
   private static final Map<String, Color> NAMED_COLORS = LEGACY_FORMAT_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap((p_240746_0_) -> {
      return p_240746_0_.name;
   }, Function.identity()));
   private final int value;
   @Nullable
   private final String name;

   private Color(int pValue, String pName) {
      this.value = pValue;
      this.name = pName;
   }

   private Color(int pValue) {
      this.value = pValue;
      this.name = null;
   }

   @OnlyIn(Dist.CLIENT)
   public int getValue() {
      return this.value;
   }

   public String serialize() {
      return this.name != null ? this.name : this.formatValue();
   }

   private String formatValue() {
      return String.format("#%06X", this.value);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         Color color = (Color)p_equals_1_;
         return this.value == color.value;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.value, this.name);
   }

   public String toString() {
      return this.name != null ? this.name : this.formatValue();
   }

   @Nullable
   public static Color fromLegacyFormat(TextFormatting pFormatting) {
      return LEGACY_FORMAT_TO_COLOR.get(pFormatting);
   }

   public static Color fromRgb(int pColor) {
      return new Color(pColor);
   }

   @Nullable
   public static Color parseColor(String pHexString) {
      if (pHexString.startsWith("#")) {
         try {
            int i = Integer.parseInt(pHexString.substring(1), 16);
            return fromRgb(i);
         } catch (NumberFormatException numberformatexception) {
            return null;
         }
      } else {
         return NAMED_COLORS.get(pHexString);
      }
   }
}