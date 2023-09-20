package net.minecraft.client.resources;

import java.util.IllegalFormatException;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class I18n {
   private static volatile LanguageMap language = LanguageMap.getInstance();

   static void setLanguage(LanguageMap pLanguage) {
      language = pLanguage;
      net.minecraftforge.fml.ForgeI18n.loadLanguageData(pLanguage.getLanguageData());
   }

   /**
    * Translates the given string and then formats it. Equivilant to String.format(translate(key), parameters).
    */
   public static String get(String pTranslateKey, Object... pParameters) {
      String s = language.getOrDefault(pTranslateKey);

      try {
         return String.format(s, pParameters);
      } catch (IllegalFormatException illegalformatexception) {
         return "Format error: " + s;
      }
   }

   public static boolean exists(String pKey) {
      return language.has(pKey);
   }
}
