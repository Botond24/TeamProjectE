package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.WorkingScreen;
import net.minecraft.resources.FilePack;
import net.minecraft.resources.FolderPack;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.PackCompatibility;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.VanillaPack;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.HTTPUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DownloadingPackFinder implements IPackFinder {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
   private final VanillaPack vanillaPack;
   private final File serverPackDir;
   private final ReentrantLock downloadLock = new ReentrantLock();
   private final ResourceIndex assetIndex;
   @Nullable
   private CompletableFuture<?> currentDownload;
   @Nullable
   private ResourcePackInfo serverPack;

   public DownloadingPackFinder(File pServerPackDirectory, ResourceIndex pAssetIndex) {
      this.serverPackDir = pServerPackDirectory;
      this.assetIndex = pAssetIndex;
      this.vanillaPack = new VirtualAssetsPack(pAssetIndex);
   }

   public void loadPacks(Consumer<ResourcePackInfo> pInfoConsumer, ResourcePackInfo.IFactory pInfoFactory) {
      ResourcePackInfo resourcepackinfo = ResourcePackInfo.create("vanilla", true, () -> {
         return this.vanillaPack;
      }, pInfoFactory, ResourcePackInfo.Priority.BOTTOM, IPackNameDecorator.BUILT_IN);
      if (resourcepackinfo != null) {
         pInfoConsumer.accept(resourcepackinfo);
      }

      if (this.serverPack != null) {
         pInfoConsumer.accept(this.serverPack);
      }

      ResourcePackInfo resourcepackinfo1 = this.createProgrammerArtPack(pInfoFactory);
      if (resourcepackinfo1 != null) {
         pInfoConsumer.accept(resourcepackinfo1);
      }

   }

   public VanillaPack getVanillaPack() {
      return this.vanillaPack;
   }

   private static Map<String, String> getDownloadHeaders() {
      Map<String, String> map = Maps.newHashMap();
      map.put("X-Minecraft-Username", Minecraft.getInstance().getUser().getName());
      map.put("X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid());
      map.put("X-Minecraft-Version", SharedConstants.getCurrentVersion().getName());
      map.put("X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId());
      map.put("X-Minecraft-Pack-Format", String.valueOf(SharedConstants.getCurrentVersion().getPackVersion()));
      map.put("User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().getName());
      return map;
   }

   public CompletableFuture<?> downloadAndSelectResourcePack(String p_217818_1_, String p_217818_2_) {
      String s = DigestUtils.sha1Hex(p_217818_1_);
      String s1 = SHA1.matcher(p_217818_2_).matches() ? p_217818_2_ : "";
      this.downloadLock.lock();

      CompletableFuture completablefuture1;
      try {
         this.clearServerPack();
         this.clearOldDownloads();
         File file1 = new File(this.serverPackDir, s);
         CompletableFuture<?> completablefuture;
         if (file1.exists()) {
            completablefuture = CompletableFuture.completedFuture("");
         } else {
            WorkingScreen workingscreen = new WorkingScreen();
            Map<String, String> map = getDownloadHeaders();
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.executeBlocking(() -> {
               minecraft.setScreen(workingscreen);
            });
            completablefuture = HTTPUtil.downloadTo(file1, p_217818_1_, map, 104857600, workingscreen, minecraft.getProxy());
         }

         this.currentDownload = completablefuture.thenCompose((p_217812_3_) -> {
            return !this.checkHash(s1, file1) ? Util.failedFuture(new RuntimeException("Hash check failure for file " + file1 + ", see log")) : this.setServerPack(file1, IPackNameDecorator.SERVER);
         }).whenComplete((p_217815_1_, p_217815_2_) -> {
            if (p_217815_2_ != null) {
               LOGGER.warn("Pack application failed: {}, deleting file {}", p_217815_2_.getMessage(), file1);
               deleteQuietly(file1);
            }

         });
         completablefuture1 = this.currentDownload;
      } finally {
         this.downloadLock.unlock();
      }

      return completablefuture1;
   }

   private static void deleteQuietly(File pFile) {
      try {
         Files.delete(pFile.toPath());
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to delete file {}: {}", pFile, ioexception.getMessage());
      }

   }

   public void clearServerPack() {
      this.downloadLock.lock();

      try {
         if (this.currentDownload != null) {
            this.currentDownload.cancel(true);
         }

         this.currentDownload = null;
         if (this.serverPack != null) {
            this.serverPack = null;
            Minecraft.getInstance().delayTextureReload();
         }
      } finally {
         this.downloadLock.unlock();
      }

   }

   private boolean checkHash(String pExpectedHash, File pFile) {
      try (FileInputStream fileinputstream = new FileInputStream(pFile)) {
         String s = DigestUtils.sha1Hex((InputStream)fileinputstream);
         if (pExpectedHash.isEmpty()) {
            LOGGER.info("Found file {} without verification hash", (Object)pFile);
            return true;
         }

         if (s.toLowerCase(Locale.ROOT).equals(pExpectedHash.toLowerCase(Locale.ROOT))) {
            LOGGER.info("Found file {} matching requested hash {}", pFile, pExpectedHash);
            return true;
         }

         LOGGER.warn("File {} had wrong hash (expected {}, found {}).", pFile, pExpectedHash, s);
      } catch (IOException ioexception) {
         LOGGER.warn("File {} couldn't be hashed.", pFile, ioexception);
      }

      return false;
   }

   private void clearOldDownloads() {
      try {
         List<File> list = Lists.newArrayList(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, (IOFileFilter)null));
         list.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
         int i = 0;

         for(File file1 : list) {
            if (i++ >= 10) {
               LOGGER.info("Deleting old server resource pack {}", (Object)file1.getName());
               FileUtils.deleteQuietly(file1);
            }
         }
      } catch (IllegalArgumentException illegalargumentexception) {
         LOGGER.error("Error while deleting old server resource pack : {}", (Object)illegalargumentexception.getMessage());
      }

   }

   public CompletableFuture<Void> setServerPack(File pFile, IPackNameDecorator pPackSource) {
      PackMetadataSection packmetadatasection;
      try (FilePack filepack = new FilePack(pFile)) {
         packmetadatasection = filepack.getMetadataSection(PackMetadataSection.SERIALIZER);
      } catch (IOException ioexception) {
         return Util.failedFuture(new IOException(String.format("Invalid resourcepack at %s", pFile), ioexception));
      }

      LOGGER.info("Applying server pack {}", (Object)pFile);
      this.serverPack = new ResourcePackInfo("server", true, () -> {
         return new FilePack(pFile);
      }, new TranslationTextComponent("resourcePack.server.name"), packmetadatasection.getDescription(), PackCompatibility.forFormat(packmetadatasection.getPackFormat()), ResourcePackInfo.Priority.TOP, true, pPackSource);
      return Minecraft.getInstance().delayTextureReload();
   }

   @Nullable
   private ResourcePackInfo createProgrammerArtPack(ResourcePackInfo.IFactory p_239453_1_) {
      ResourcePackInfo resourcepackinfo = null;
      File file1 = this.assetIndex.getFile(new ResourceLocation("resourcepacks/programmer_art.zip"));
      if (file1 != null && file1.isFile()) {
         resourcepackinfo = createProgrammerArtPack(p_239453_1_, () -> {
            return createProgrammerArtZipPack(file1);
         });
      }

      if (resourcepackinfo == null && SharedConstants.IS_RUNNING_IN_IDE) {
         File file2 = this.assetIndex.getRootFile("../resourcepacks/programmer_art");
         if (file2 != null && file2.isDirectory()) {
            resourcepackinfo = createProgrammerArtPack(p_239453_1_, () -> {
               return createProgrammerArtDirPack(file2);
            });
         }
      }

      return resourcepackinfo;
   }

   @Nullable
   private static ResourcePackInfo createProgrammerArtPack(ResourcePackInfo.IFactory p_239454_0_, Supplier<IResourcePack> pResources) {
      return ResourcePackInfo.create("programer_art", false, pResources, p_239454_0_, ResourcePackInfo.Priority.TOP, IPackNameDecorator.BUILT_IN);
   }

   private static FolderPack createProgrammerArtDirPack(File pFile) {
      return new FolderPack(pFile) {
         public String getName() {
            return "Programmer Art";
         }
      };
   }

   private static IResourcePack createProgrammerArtZipPack(File pFile) {
      return new FilePack(pFile) {
         public String getName() {
            return "Programmer Art";
         }
      };
   }
}