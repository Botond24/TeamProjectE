package net.minecraft.client.renderer.model;

import java.util.Locale;
import net.minecraft.util.ResourceLocation;

public class ModelResourceLocation extends ResourceLocation {
   private final String variant;

   protected ModelResourceLocation(String[] p_i48111_1_) {
      super(p_i48111_1_);
      this.variant = p_i48111_1_[2].toLowerCase(Locale.ROOT);
   }

   public ModelResourceLocation(String p_i46079_1_) {
      this(decompose(p_i46079_1_));
   }

   public ModelResourceLocation(ResourceLocation p_i46080_1_, String p_i46080_2_) {
      this(p_i46080_1_.toString(), p_i46080_2_);
   }

   public ModelResourceLocation(String p_i46081_1_, String p_i46081_2_) {
      this(decompose(p_i46081_1_ + '#' + p_i46081_2_));
   }

   protected static String[] decompose(String pPath) {
      String[] astring = new String[]{null, pPath, ""};
      int i = pPath.indexOf(35);
      String s = pPath;
      if (i >= 0) {
         astring[2] = pPath.substring(i + 1, pPath.length());
         if (i > 1) {
            s = pPath.substring(0, i);
         }
      }

      System.arraycopy(ResourceLocation.decompose(s, ':'), 0, astring, 0, 2);
      return astring;
   }

   public String getVariant() {
      return this.variant;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ instanceof ModelResourceLocation && super.equals(p_equals_1_)) {
         ModelResourceLocation modelresourcelocation = (ModelResourceLocation)p_equals_1_;
         return this.variant.equals(modelresourcelocation.variant);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return 31 * super.hashCode() + this.variant.hashCode();
   }

   public String toString() {
      return super.toString() + '#' + this.variant;
   }
}