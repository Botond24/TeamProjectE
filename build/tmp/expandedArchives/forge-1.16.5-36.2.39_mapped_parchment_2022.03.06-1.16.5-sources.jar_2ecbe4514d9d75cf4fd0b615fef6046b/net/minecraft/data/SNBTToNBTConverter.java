package net.minecraft.data;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SNBTToNBTConverter implements IDataProvider {
   @Nullable
   private static final Path dumpSnbtTo = null;
   private static final Logger LOGGER = LogManager.getLogger();
   private final DataGenerator generator;
   private final List<SNBTToNBTConverter.ITransformer> filters = Lists.newArrayList();

   public SNBTToNBTConverter(DataGenerator pGenerator) {
      this.generator = pGenerator;
   }

   public SNBTToNBTConverter addFilter(SNBTToNBTConverter.ITransformer pFilter) {
      this.filters.add(pFilter);
      return this;
   }

   private CompoundNBT applyFilters(String pFileName, CompoundNBT pTag) {
      CompoundNBT compoundnbt = pTag;

      for(SNBTToNBTConverter.ITransformer snbttonbtconverter$itransformer : this.filters) {
         compoundnbt = snbttonbtconverter$itransformer.apply(pFileName, compoundnbt);
      }

      return compoundnbt;
   }

   /**
    * Performs this provider's action.
    */
   public void run(DirectoryCache pCache) throws IOException {
      Path path = this.generator.getOutputFolder();
      List<CompletableFuture<SNBTToNBTConverter.TaskResult>> list = Lists.newArrayList();

      for(Path path1 : this.generator.getInputFolders()) {
         Files.walk(path1).filter((p_200422_0_) -> {
            return p_200422_0_.toString().endsWith(".snbt");
         }).forEach((p_229447_3_) -> {
            list.add(CompletableFuture.supplyAsync(() -> {
               return this.readStructure(p_229447_3_, this.getName(path1, p_229447_3_));
            }, Util.backgroundExecutor()));
         });
      }

      Util.sequence(list).join().stream().filter(Objects::nonNull).forEach((p_229445_3_) -> {
         this.storeStructureIfChanged(pCache, p_229445_3_, path);
      });
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public String getName() {
      return "SNBT -> NBT";
   }

   /**
    * Gets the name of the given SNBT file, based on its path and the input directory. The result does not have the
    * ".snbt" extension.
    */
   private String getName(Path pInputFolder, Path pFile) {
      String s = pInputFolder.relativize(pFile).toString().replaceAll("\\\\", "/");
      return s.substring(0, s.length() - ".snbt".length());
   }

   @Nullable
   private SNBTToNBTConverter.TaskResult readStructure(Path pFilePath, String pFileName) {
      try (BufferedReader bufferedreader = Files.newBufferedReader(pFilePath)) {
         String s = IOUtils.toString((Reader)bufferedreader);
         CompoundNBT compoundnbt = this.applyFilters(pFileName, JsonToNBT.parseTag(s));
         ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
         CompressedStreamTools.writeCompressed(compoundnbt, bytearrayoutputstream);
         byte[] abyte = bytearrayoutputstream.toByteArray();
         String s1 = SHA1.hashBytes(abyte).toString();
         String s2;
         if (dumpSnbtTo != null) {
            s2 = compoundnbt.getPrettyDisplay("    ", 0).getString() + "\n";
         } else {
            s2 = null;
         }

         return new SNBTToNBTConverter.TaskResult(pFileName, abyte, s2, s1);
      } catch (CommandSyntaxException commandsyntaxexception) {
         LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", pFileName, pFilePath, commandsyntaxexception);
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", pFileName, pFilePath, ioexception);
      }

      return null;
   }

   private void storeStructureIfChanged(DirectoryCache pCache, SNBTToNBTConverter.TaskResult pTaskResult, Path pDirectoryPath) {
      if (pTaskResult.snbtPayload != null) {
         Path path = dumpSnbtTo.resolve(pTaskResult.name + ".snbt");

         try {
            FileUtils.write(path.toFile(), pTaskResult.snbtPayload, StandardCharsets.UTF_8);
         } catch (IOException ioexception) {
            LOGGER.error("Couldn't write structure SNBT {} at {}", pTaskResult.name, path, ioexception);
         }
      }

      Path path1 = pDirectoryPath.resolve(pTaskResult.name + ".nbt");

      try {
         if (!Objects.equals(pCache.getHash(path1), pTaskResult.hash) || !Files.exists(path1)) {
            Files.createDirectories(path1.getParent());

            try (OutputStream outputstream = Files.newOutputStream(path1)) {
               outputstream.write(pTaskResult.payload);
            }
         }

         pCache.putNew(path1, pTaskResult.hash);
      } catch (IOException ioexception1) {
         LOGGER.error("Couldn't write structure {} at {}", pTaskResult.name, path1, ioexception1);
      }

   }

   @FunctionalInterface
   public interface ITransformer {
      CompoundNBT apply(String pStructureLocationPath, CompoundNBT pTag);
   }

   static class TaskResult {
      private final String name;
      private final byte[] payload;
      @Nullable
      private final String snbtPayload;
      private final String hash;

      public TaskResult(String pName, byte[] pPayload, @Nullable String pSnbtPayload, String pHash) {
         this.name = pName;
         this.payload = pPayload;
         this.snbtPayload = pSnbtPayload;
         this.hash = pHash;
      }
   }
}