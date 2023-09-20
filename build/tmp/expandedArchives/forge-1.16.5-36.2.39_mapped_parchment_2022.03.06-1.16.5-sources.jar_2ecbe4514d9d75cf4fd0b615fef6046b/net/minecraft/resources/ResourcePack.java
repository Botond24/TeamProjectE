package net.minecraft.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ResourcePack implements IResourcePack {
   private static final Logger LOGGER = LogManager.getLogger();
   public final File file;

   public ResourcePack(File p_i1287_1_) {
      this.file = p_i1287_1_;
   }

   private static String getPathFromLocation(ResourcePackType pType, ResourceLocation pLocation) {
      return String.format("%s/%s/%s", pType.getDirectory(), pLocation.getNamespace(), pLocation.getPath());
   }

   protected static String getRelativePath(File pFile1, File pFile2) {
      return pFile1.toURI().relativize(pFile2.toURI()).getPath();
   }

   public InputStream getResource(ResourcePackType pType, ResourceLocation pLocation) throws IOException {
      return this.getResource(getPathFromLocation(pType, pLocation));
   }

   public boolean hasResource(ResourcePackType pType, ResourceLocation pLocation) {
      return this.hasResource(getPathFromLocation(pType, pLocation));
   }

   protected abstract InputStream getResource(String pResourcePath) throws IOException;

   @OnlyIn(Dist.CLIENT)
   public InputStream getRootResource(String pFileName) throws IOException {
      if (!pFileName.contains("/") && !pFileName.contains("\\")) {
         return this.getResource(pFileName);
      } else {
         throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
      }
   }

   protected abstract boolean hasResource(String pResourcePath);

   protected void logWarning(String pNamespace) {
      LOGGER.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", pNamespace, this.file);
   }

   @Nullable
   public <T> T getMetadataSection(IMetadataSectionSerializer<T> pDeserializer) throws IOException {
      Object object;
      try (InputStream inputstream = this.getResource("pack.mcmeta")) {
         object = getMetadataFromStream(pDeserializer, inputstream);
      }

      return (T)object;
   }

   @Nullable
   public static <T> T getMetadataFromStream(IMetadataSectionSerializer<T> pDeserializer, InputStream pInputStream) {
      JsonObject jsonobject;
      try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(pInputStream, StandardCharsets.UTF_8))) {
         jsonobject = JSONUtils.parse(bufferedreader);
      } catch (JsonParseException | IOException ioexception) {
         LOGGER.error("Couldn't load {} metadata", pDeserializer.getMetadataSectionName(), ioexception);
         return (T)null;
      }

      if (!jsonobject.has(pDeserializer.getMetadataSectionName())) {
         return (T)null;
      } else {
         try {
            return pDeserializer.fromJson(JSONUtils.getAsJsonObject(jsonobject, pDeserializer.getMetadataSectionName()));
         } catch (JsonParseException jsonparseexception) {
            LOGGER.error("Couldn't load {} metadata", pDeserializer.getMetadataSectionName(), jsonparseexception);
            return (T)null;
         }
      }
   }

   public String getName() {
      return this.file.getName();
   }
}