package net.minecraft.client.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import net.minecraft.client.util.BidiReorderer;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientLanguageMap extends LanguageMap {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<String, String> storage;
   private final boolean defaultRightToLeft;

   private ClientLanguageMap(Map<String, String> pStorage, boolean pDefaultRightToLeft) {
      this.storage = pStorage;
      this.defaultRightToLeft = pDefaultRightToLeft;
   }

   public static ClientLanguageMap loadFrom(IResourceManager pManager, List<Language> pLanguageInfos) {
      Map<String, String> map = Maps.newHashMap();
      boolean flag = false;

      for(Language language : pLanguageInfos) {
         flag |= language.isBidirectional();
         String s = String.format("lang/%s.json", language.getCode());

         for(String s1 : pManager.getNamespaces()) {
            try {
               ResourceLocation resourcelocation = new ResourceLocation(s1, s);
               appendFrom(pManager.getResources(resourcelocation), map);
            } catch (FileNotFoundException filenotfoundexception) {
            } catch (Exception exception) {
               LOGGER.warn("Skipped language file: {}:{} ({})", s1, s, exception.toString());
            }
         }
      }

      return new ClientLanguageMap(ImmutableMap.copyOf(map), flag);
   }

   private static void appendFrom(List<IResource> pResources, Map<String, String> pStorage) {
      for(IResource iresource : pResources) {
         try (InputStream inputstream = iresource.getInputStream()) {
            LanguageMap.loadFromJson(inputstream, pStorage::put);
         } catch (IOException ioexception) {
            LOGGER.warn("Failed to load translations from {}", iresource, ioexception);
         }
      }

   }

   public String getOrDefault(String pId) {
      return this.storage.getOrDefault(pId, pId);
   }

   public boolean has(String pId) {
      return this.storage.containsKey(pId);
   }

   public boolean isDefaultRightToLeft() {
      return this.defaultRightToLeft;
   }

   public IReorderingProcessor getVisualOrder(ITextProperties pText) {
      return BidiReorderer.reorder(pText, this.defaultRightToLeft);
   }

   @Override
   public Map<String, String> getLanguageData() {
      return storage;
   }
}
