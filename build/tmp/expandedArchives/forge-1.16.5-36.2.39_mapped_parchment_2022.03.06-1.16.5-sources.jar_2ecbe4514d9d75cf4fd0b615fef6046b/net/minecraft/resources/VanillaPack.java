package net.minecraft.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaPack implements IResourcePack {
   public static Path generatedDir;
   private static final Logger LOGGER = LogManager.getLogger();
   public static Class<?> clientObject;
   private static final Map<ResourcePackType, FileSystem> JAR_FILESYSTEM_BY_TYPE = Util.make(Maps.newHashMap(), (p_217809_0_) -> {
      synchronized(VanillaPack.class) {
         for(ResourcePackType resourcepacktype : ResourcePackType.values()) {
            URL url = VanillaPack.class.getResource("/" + resourcepacktype.getDirectory() + "/.mcassetsroot");

            try {
               URI uri = url.toURI();
               if ("jar".equals(uri.getScheme())) {
                  FileSystem filesystem;
                  try {
                     filesystem = FileSystems.getFileSystem(uri);
                  } catch (FileSystemNotFoundException filesystemnotfoundexception) {
                     filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                  }

                  p_217809_0_.put(resourcepacktype, filesystem);
               }
            } catch (IOException | URISyntaxException urisyntaxexception) {
               LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)urisyntaxexception);
            }
         }

      }
   });
   public final Set<String> namespaces;

   public VanillaPack(String... p_i47912_1_) {
      this.namespaces = ImmutableSet.copyOf(p_i47912_1_);
   }

   public InputStream getRootResource(String pFileName) throws IOException {
      if (!pFileName.contains("/") && !pFileName.contains("\\")) {
         if (generatedDir != null) {
            Path path = generatedDir.resolve(pFileName);
            if (Files.exists(path)) {
               return Files.newInputStream(path);
            }
         }

         return this.getResourceAsStream(pFileName);
      } else {
         throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
      }
   }

   public InputStream getResource(ResourcePackType pType, ResourceLocation pLocation) throws IOException {
      InputStream inputstream = this.getResourceAsStream(pType, pLocation);
      if (inputstream != null) {
         return inputstream;
      } else {
         throw new FileNotFoundException(pLocation.getPath());
      }
   }

   public Collection<ResourceLocation> getResources(ResourcePackType pType, String pNamespace, String pPath, int pMaxDepth, Predicate<String> pFilter) {
      Set<ResourceLocation> set = Sets.newHashSet();
      if (generatedDir != null) {
         try {
            getResources(set, pMaxDepth, pNamespace, generatedDir.resolve(pType.getDirectory()), pPath, pFilter);
         } catch (IOException ioexception1) {
         }

         if (pType == ResourcePackType.CLIENT_RESOURCES) {
            Enumeration<URL> enumeration = null;

            try {
               enumeration = clientObject.getClassLoader().getResources(pType.getDirectory() + "/");
            } catch (IOException ioexception) {
            }

            while(enumeration != null && enumeration.hasMoreElements()) {
               try {
                  URI uri = enumeration.nextElement().toURI();
                  if ("file".equals(uri.getScheme())) {
                     getResources(set, pMaxDepth, pNamespace, Paths.get(uri), pPath, pFilter);
                  }
               } catch (IOException | URISyntaxException urisyntaxexception1) {
               }
            }
         }
      }

      try {
         URL url1 = VanillaPack.class.getResource("/" + pType.getDirectory() + "/.mcassetsroot");
         if (url1 == null) {
            LOGGER.error("Couldn't find .mcassetsroot, cannot load vanilla resources");
            return set;
         }

         URI uri1 = url1.toURI();
         if ("file".equals(uri1.getScheme())) {
            URL url = new URL(url1.toString().substring(0, url1.toString().length() - ".mcassetsroot".length()));
            Path path = Paths.get(url.toURI());
            getResources(set, pMaxDepth, pNamespace, path, pPath, pFilter);
         } else if ("jar".equals(uri1.getScheme())) {
            Path path1 = JAR_FILESYSTEM_BY_TYPE.get(pType).getPath("/" + pType.getDirectory());
            getResources(set, pMaxDepth, "minecraft", path1, pPath, pFilter);
         } else {
            LOGGER.error("Unsupported scheme {} trying to list vanilla resources (NYI?)", (Object)uri1);
         }
      } catch (NoSuchFileException | FileNotFoundException filenotfoundexception) {
      } catch (IOException | URISyntaxException urisyntaxexception) {
         LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)urisyntaxexception);
      }

      return set;
   }

   private static void getResources(Collection<ResourceLocation> pResourceLocations, int pMaxDepth, String pNamespace, Path pPath, String pPathName, Predicate<String> pFilter) throws IOException {
      Path path = pPath.resolve(pNamespace);

      try (Stream<Path> stream = Files.walk(path.resolve(pPathName), pMaxDepth)) {
         stream.filter((p_229868_1_) -> {
            return !p_229868_1_.endsWith(".mcmeta") && Files.isRegularFile(p_229868_1_) && pFilter.test(p_229868_1_.getFileName().toString());
         }).map((p_229866_2_) -> {
            return new ResourceLocation(pNamespace, path.relativize(p_229866_2_).toString().replaceAll("\\\\", "/"));
         }).forEach(pResourceLocations::add);
      }

   }

   @Nullable
   protected InputStream getResourceAsStream(ResourcePackType pType, ResourceLocation pLocation) {
      String s = createPath(pType, pLocation);
      if (generatedDir != null) {
         Path path = generatedDir.resolve(pType.getDirectory() + "/" + pLocation.getNamespace() + "/" + pLocation.getPath());
         if (Files.exists(path)) {
            try {
               return Files.newInputStream(path);
            } catch (IOException ioexception1) {
            }
         }
      }

      try {
         URL url = VanillaPack.class.getResource(s);
         return isResourceUrlValid(s, url) ? getExtraInputStream(pType, s) : null;
      } catch (IOException ioexception) {
         return VanillaPack.class.getResourceAsStream(s);
      }
   }

   private static String createPath(ResourcePackType pPackType, ResourceLocation pLocation) {
      return "/" + pPackType.getDirectory() + "/" + pLocation.getNamespace() + "/" + pLocation.getPath();
   }

   private static boolean isResourceUrlValid(String pPath, @Nullable URL pUrl) throws IOException {
      return pUrl != null && (pUrl.getProtocol().equals("jar") || FolderPack.validatePath(new File(pUrl.getFile()), pPath));
   }

   @Nullable
   protected InputStream getResourceAsStream(String pPath) {
      return getExtraInputStream(ResourcePackType.SERVER_DATA, "/" + pPath);
   }

   public boolean hasResource(ResourcePackType pType, ResourceLocation pLocation) {
      String s = createPath(pType, pLocation);
      if (generatedDir != null) {
         Path path = generatedDir.resolve(pType.getDirectory() + "/" + pLocation.getNamespace() + "/" + pLocation.getPath());
         if (Files.exists(path)) {
            return true;
         }
      }

      try {
         URL url = VanillaPack.class.getResource(s);
         return isResourceUrlValid(s, url);
      } catch (IOException ioexception) {
         return false;
      }
   }

   public Set<String> getNamespaces(ResourcePackType pType) {
      return this.namespaces;
   }

   @Nullable
   public <T> T getMetadataSection(IMetadataSectionSerializer<T> pDeserializer) throws IOException {
      try (InputStream inputstream = this.getRootResource("pack.mcmeta")) {
         return ResourcePack.getMetadataFromStream(pDeserializer, inputstream);
      } catch (FileNotFoundException | RuntimeException runtimeexception) {
         return (T)null;
      }
   }

   public String getName() {
      return "Default";
   }

   public void close() {
   }

   //Vanilla used to just grab from the classpath, this breaks dev environments, and Forge runtime
   //as forge ships vanilla assets in an 'extra' jar with no classes.
   //So find that extra jar using the .mcassetsroot marker.
   private InputStream getExtraInputStream(ResourcePackType type, String resource) {
      try {
         FileSystem fs = JAR_FILESYSTEM_BY_TYPE.get(type);
         if (fs != null)
            return Files.newInputStream(fs.getPath(resource));
         return VanillaPack.class.getResourceAsStream(resource);
      } catch (IOException e) {
         return VanillaPack.class.getResourceAsStream(resource);
      }
   }
}
