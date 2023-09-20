package net.minecraft.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.util.text.TranslationTextComponent;

public class LocationPart {
   public static final SimpleCommandExceptionType ERROR_EXPECTED_DOUBLE = new SimpleCommandExceptionType(new TranslationTextComponent("argument.pos.missing.double"));
   public static final SimpleCommandExceptionType ERROR_EXPECTED_INT = new SimpleCommandExceptionType(new TranslationTextComponent("argument.pos.missing.int"));
   private final boolean relative;
   private final double value;

   public LocationPart(boolean pRelative, double pValue) {
      this.relative = pRelative;
      this.value = pValue;
   }

   public double get(double pCoord) {
      return this.relative ? this.value + pCoord : this.value;
   }

   public static LocationPart parseDouble(StringReader pReader, boolean pCenterCorrect) throws CommandSyntaxException {
      if (pReader.canRead() && pReader.peek() == '^') {
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(pReader);
      } else if (!pReader.canRead()) {
         throw ERROR_EXPECTED_DOUBLE.createWithContext(pReader);
      } else {
         boolean flag = isRelative(pReader);
         int i = pReader.getCursor();
         double d0 = pReader.canRead() && pReader.peek() != ' ' ? pReader.readDouble() : 0.0D;
         String s = pReader.getString().substring(i, pReader.getCursor());
         if (flag && s.isEmpty()) {
            return new LocationPart(true, 0.0D);
         } else {
            if (!s.contains(".") && !flag && pCenterCorrect) {
               d0 += 0.5D;
            }

            return new LocationPart(flag, d0);
         }
      }
   }

   public static LocationPart parseInt(StringReader pReader) throws CommandSyntaxException {
      if (pReader.canRead() && pReader.peek() == '^') {
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(pReader);
      } else if (!pReader.canRead()) {
         throw ERROR_EXPECTED_INT.createWithContext(pReader);
      } else {
         boolean flag = isRelative(pReader);
         double d0;
         if (pReader.canRead() && pReader.peek() != ' ') {
            d0 = flag ? pReader.readDouble() : (double)pReader.readInt();
         } else {
            d0 = 0.0D;
         }

         return new LocationPart(flag, d0);
      }
   }

   public static boolean isRelative(StringReader pReader) {
      boolean flag;
      if (pReader.peek() == '~') {
         flag = true;
         pReader.skip();
      } else {
         flag = false;
      }

      return flag;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof LocationPart)) {
         return false;
      } else {
         LocationPart locationpart = (LocationPart)p_equals_1_;
         if (this.relative != locationpart.relative) {
            return false;
         } else {
            return Double.compare(locationpart.value, this.value) == 0;
         }
      }
   }

   public int hashCode() {
      int i = this.relative ? 1 : 0;
      long j = Double.doubleToLongBits(this.value);
      return 31 * i + (int)(j ^ j >>> 32);
   }

   public boolean isRelative() {
      return this.relative;
   }
}