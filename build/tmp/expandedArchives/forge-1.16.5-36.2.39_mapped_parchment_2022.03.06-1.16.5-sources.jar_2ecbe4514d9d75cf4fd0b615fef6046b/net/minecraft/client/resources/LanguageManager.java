package net.minecraft.client.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Stream;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.resources.IResourcePack;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LanguageManager implements IResourceManagerReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Language DEFAULT_LANGUAGE = new Language("en_us", "US", "English", false);
   private Map<String, Language> languages = ImmutableMap.of("en_us", DEFAULT_LANGUAGE);
   private String currentCode;
   private Language currentLanguage = DEFAULT_LANGUAGE;

   public LanguageManager(String pCurrentCode) {
      this.currentCode = pCurrentCode;
   }

   private static Map<String, Language> extractLanguages(Stream<IResourcePack> pResources) {
      Map<String, Language> map = Maps.newHashMap();
      pResources.forEach((p_239505_1_) -> {
         try {
            LanguageMetadataSection languagemetadatasection = p_239505_1_.getMetadataSection(LanguageMetadataSection.SERIALIZER);
            if (languagemetadatasection != null) {
               for(Language language : languagemetadatasection.getLanguages()) {
                  map.putIfAbsent(language.getCode(), language);
               }
            }
         } catch (IOException | RuntimeException runtimeexception) {
            LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", p_239505_1_.getName(), runtimeexception);
         }

      });
      return ImmutableMap.copyOf(map);
   }

   public void onResourceManagerReload(IResourceManager pResourceManager) {
      this.languages = extractLanguages(pResourceManager.listPacks());
      Language language = this.languages.getOrDefault("en_us", DEFAULT_LANGUAGE);
      this.currentLanguage = this.languages.getOrDefault(this.currentCode, language);
      List<Language> list = Lists.newArrayList(language);
      if (this.currentLanguage != language) {
         list.add(this.currentLanguage);
      }

      ClientLanguageMap clientlanguagemap = ClientLanguageMap.loadFrom(pResourceManager, list);
      I18n.setLanguage(clientlanguagemap);
      LanguageMap.inject(clientlanguagemap);
   }

   public void setSelected(Language pCurrentLanguage) {
      this.currentCode = pCurrentLanguage.getCode();
      this.currentLanguage = pCurrentLanguage;
   }

   public Language getSelected() {
      return this.currentLanguage;
   }

   public SortedSet<Language> getLanguages() {
      return Sets.newTreeSet(this.languages.values());
   }

   public Language getLanguage(String pCode) {
      return this.languages.get(pCode);
   }

   @Override
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.LANGUAGES;
   }
}
