package net.minecraft.advancements.criterion;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.text.TranslationTextComponent;

public class MinMaxBoundsWrapped {
   public static final MinMaxBoundsWrapped ANY = new MinMaxBoundsWrapped((Float)null, (Float)null);
   public static final SimpleCommandExceptionType ERROR_INTS_ONLY = new SimpleCommandExceptionType(new TranslationTextComponent("argument.range.ints"));
   private final Float min;
   private final Float max;

   public MinMaxBoundsWrapped(@Nullable Float pMin, @Nullable Float pMax) {
      this.min = pMin;
      this.max = pMax;
   }

   @Nullable
   public Float getMin() {
      return this.min;
   }

   @Nullable
   public Float getMax() {
      return this.max;
   }

   public static MinMaxBoundsWrapped fromReader(StringReader pReader, boolean pIsFloatingPoint, Function<Float, Float> pValueFunction) throws CommandSyntaxException {
      if (!pReader.canRead()) {
         throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
      } else {
         int i = pReader.getCursor();
         Float f = optionallyFormat(readNumber(pReader, pIsFloatingPoint), pValueFunction);
         Float f1;
         if (pReader.canRead(2) && pReader.peek() == '.' && pReader.peek(1) == '.') {
            pReader.skip();
            pReader.skip();
            f1 = optionallyFormat(readNumber(pReader, pIsFloatingPoint), pValueFunction);
            if (f == null && f1 == null) {
               pReader.setCursor(i);
               throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
            }
         } else {
            if (!pIsFloatingPoint && pReader.canRead() && pReader.peek() == '.') {
               pReader.setCursor(i);
               throw ERROR_INTS_ONLY.createWithContext(pReader);
            }

            f1 = f;
         }

         if (f == null && f1 == null) {
            pReader.setCursor(i);
            throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
         } else {
            return new MinMaxBoundsWrapped(f, f1);
         }
      }
   }

   @Nullable
   private static Float readNumber(StringReader pReader, boolean pIsFloatingPoint) throws CommandSyntaxException {
      int i = pReader.getCursor();

      while(pReader.canRead() && isAllowedNumber(pReader, pIsFloatingPoint)) {
         pReader.skip();
      }

      String s = pReader.getString().substring(i, pReader.getCursor());
      if (s.isEmpty()) {
         return null;
      } else {
         try {
            return Float.parseFloat(s);
         } catch (NumberFormatException numberformatexception) {
            if (pIsFloatingPoint) {
               throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext(pReader, s);
            } else {
               throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(pReader, s);
            }
         }
      }
   }

   private static boolean isAllowedNumber(StringReader pReader, boolean pIsFloatingPoint) {
      char c0 = pReader.peek();
      if ((c0 < '0' || c0 > '9') && c0 != '-') {
         if (pIsFloatingPoint && c0 == '.') {
            return !pReader.canRead(2) || pReader.peek(1) != '.';
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   @Nullable
   private static Float optionallyFormat(@Nullable Float pValue, Function<Float, Float> pValueFunction) {
      return pValue == null ? null : pValueFunction.apply(pValue);
   }
}