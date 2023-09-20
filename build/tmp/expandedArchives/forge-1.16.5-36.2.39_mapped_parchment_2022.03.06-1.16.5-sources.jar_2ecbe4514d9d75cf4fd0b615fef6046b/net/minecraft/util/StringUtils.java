package net.minecraft.util;

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StringUtils {
   private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");

   /**
    * Returns the time elapsed for the given number of ticks, in "mm:ss" format.
    */
   @OnlyIn(Dist.CLIENT)
   public static String formatTickDuration(int pTicks) {
      int i = pTicks / 20;
      int j = i / 60;
      i = i % 60;
      return i < 10 ? j + ":0" + i : j + ":" + i;
   }

   @OnlyIn(Dist.CLIENT)
   public static String stripColor(String pText) {
      return STRIP_COLOR_PATTERN.matcher(pText).replaceAll("");
   }

   /**
    * Returns a value indicating whether the given string is null or empty.
    */
   public static boolean isNullOrEmpty(@Nullable String pString) {
      return org.apache.commons.lang3.StringUtils.isEmpty(pString);
   }
}