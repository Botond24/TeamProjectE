package net.minecraft.world.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.SessionLockManager;
import net.minecraft.util.FileUtil;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.datafix.TypeReferences;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveFormat {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DateTimeFormatter FORMATTER = (new DateTimeFormatterBuilder()).appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
   private static final ImmutableList<String> OLD_SETTINGS_KEYS = ImmutableList.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");
   private final Path baseDir;
   private final Path backupDir;
   private final DataFixer fixerUpper;

   public SaveFormat(Path p_i51277_1_, Path p_i51277_2_, DataFixer p_i51277_3_) {
      this.fixerUpper = p_i51277_3_;

      try {
         Files.createDirectories(Files.exists(p_i51277_1_) ? p_i51277_1_.toRealPath() : p_i51277_1_);
      } catch (IOException ioexception) {
         throw new RuntimeException(ioexception);
      }

      this.baseDir = p_i51277_1_;
      this.backupDir = p_i51277_2_;
   }

   public static SaveFormat createDefault(Path pSavesDir) {
      return new SaveFormat(pSavesDir, pSavesDir.resolve("../backups"), DataFixesManager.getDataFixer());
   }

   private static <T> Pair<DimensionGeneratorSettings, Lifecycle> readWorldGenSettings(Dynamic<T> pNbt, DataFixer pFixer, int pVersion) {
      Dynamic<T> dynamic = pNbt.get("WorldGenSettings").orElseEmptyMap();

      for(String s : OLD_SETTINGS_KEYS) {
         Optional<? extends Dynamic<?>> optional = pNbt.get(s).result();
         if (optional.isPresent()) {
            dynamic = dynamic.set(s, optional.get());
         }
      }

      Dynamic<T> dynamic1 = net.minecraftforge.common.ForgeHooks.fixUpDimensionsData(pFixer.update(TypeReferences.WORLD_GEN_SETTINGS, dynamic, pVersion, SharedConstants.getCurrentVersion().getWorldVersion()));
      DataResult<DimensionGeneratorSettings> dataresult = DimensionGeneratorSettings.CODEC.parse(dynamic1);
      return Pair.of(dataresult.resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).orElseGet(() -> {
         Registry<DimensionType> registry = RegistryLookupCodec.create(Registry.DIMENSION_TYPE_REGISTRY).codec().parse(dynamic1).resultOrPartial(Util.prefix("Dimension type registry: ", LOGGER::error)).orElseThrow(() -> {
            return new IllegalStateException("Failed to get dimension registry");
         });
         Registry<Biome> registry1 = RegistryLookupCodec.create(Registry.BIOME_REGISTRY).codec().parse(dynamic1).resultOrPartial(Util.prefix("Biome registry: ", LOGGER::error)).orElseThrow(() -> {
            return new IllegalStateException("Failed to get biome registry");
         });
         Registry<DimensionSettings> registry2 = RegistryLookupCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).codec().parse(dynamic1).resultOrPartial(Util.prefix("Noise settings registry: ", LOGGER::error)).orElseThrow(() -> {
            return new IllegalStateException("Failed to get noise settings registry");
         });
         return DimensionGeneratorSettings.makeDefault(registry, registry1, registry2);
      }), dataresult.lifecycle());
   }

   private static DatapackCodec readDataPackConfig(Dynamic<?> p_237258_0_) {
      return DatapackCodec.CODEC.parse(p_237258_0_).resultOrPartial(LOGGER::error).orElse(DatapackCodec.DEFAULT);
   }

   @OnlyIn(Dist.CLIENT)
   public List<WorldSummary> getLevelList() throws AnvilConverterException {
      if (!Files.isDirectory(this.baseDir)) {
         throw new AnvilConverterException((new TranslationTextComponent("selectWorld.load_folder_access")).getString());
      } else {
         List<WorldSummary> list = Lists.newArrayList();
         File[] afile = this.baseDir.toFile().listFiles();

         for(File file1 : afile) {
            if (file1.isDirectory()) {
               boolean flag;
               try {
                  flag = SessionLockManager.isLocked(file1.toPath());
               } catch (Exception exception) {
                  LOGGER.warn("Failed to read {} lock", file1, exception);
                  continue;
               }

               WorldSummary worldsummary = this.readLevelData(file1, this.levelSummaryReader(file1, flag));
               if (worldsummary != null) {
                  list.add(worldsummary);
               }
            }
         }

         return list;
      }
   }

   private int getStorageVersion() {
      return 19133;
   }

   @Nullable
   private <T> T readLevelData(File pSaveDir, BiFunction<File, DataFixer, T> pLevelDatReader) {
      if (!pSaveDir.exists()) {
         return (T)null;
      } else {
         File file1 = new File(pSaveDir, "level.dat");
         if (file1.exists()) {
            T t = pLevelDatReader.apply(file1, this.fixerUpper);
            if (t != null) {
               return t;
            }
         }

         file1 = new File(pSaveDir, "level.dat_old");
         return (T)(file1.exists() ? pLevelDatReader.apply(file1, this.fixerUpper) : null);
      }
   }

   @Nullable
   private static DatapackCodec getDataPacks(File pLevelDat, DataFixer pFixer) {
      try {
         CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(pLevelDat);
         CompoundNBT compoundnbt1 = compoundnbt.getCompound("Data");
         compoundnbt1.remove("Player");
         int i = compoundnbt1.contains("DataVersion", 99) ? compoundnbt1.getInt("DataVersion") : -1;
         Dynamic<INBT> dynamic = pFixer.update(DefaultTypeReferences.LEVEL.getType(), new Dynamic<>(NBTDynamicOps.INSTANCE, compoundnbt1), i, SharedConstants.getCurrentVersion().getWorldVersion());
         return dynamic.get("DataPacks").result().map(SaveFormat::readDataPackConfig).orElse(DatapackCodec.DEFAULT);
      } catch (Exception exception) {
         LOGGER.error("Exception reading {}", pLevelDat, exception);
         return null;
      }
   }

   private static BiFunction<File, DataFixer, ServerWorldInfo> getLevelData(DynamicOps<INBT> pNbt, DatapackCodec pDatapackCodec) {
       return getReader(pNbt, pDatapackCodec, null);
   }

   private static BiFunction<File, DataFixer, ServerWorldInfo> getReader(DynamicOps<INBT> pNbt, DatapackCodec pDatapackCodec, @Nullable LevelSave levelSave) {
      return (p_242976_2_, p_242976_3_) -> {
         try {
            CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(p_242976_2_);
            CompoundNBT compoundnbt1 = compoundnbt.getCompound("Data");
            CompoundNBT compoundnbt2 = compoundnbt1.contains("Player", 10) ? compoundnbt1.getCompound("Player") : null;
            compoundnbt1.remove("Player");
            int i = compoundnbt1.contains("DataVersion", 99) ? compoundnbt1.getInt("DataVersion") : -1;
            Dynamic<INBT> dynamic = p_242976_3_.update(DefaultTypeReferences.LEVEL.getType(), new Dynamic<>(pNbt, compoundnbt1), i, SharedConstants.getCurrentVersion().getWorldVersion());
            Pair<DimensionGeneratorSettings, Lifecycle> pair = readWorldGenSettings(dynamic, p_242976_3_, i);
            VersionData versiondata = VersionData.parse(dynamic);
            WorldSettings worldsettings = WorldSettings.parse(dynamic, pDatapackCodec);
            ServerWorldInfo info = ServerWorldInfo.parse(dynamic, p_242976_3_, i, compoundnbt2, worldsettings, versiondata, pair.getFirst(), pair.getSecond());
            if (levelSave != null)
                net.minecraftforge.fml.WorldPersistenceHooks.handleWorldDataLoad(levelSave, info, compoundnbt);
            return info;
         } catch (Exception exception) {
            LOGGER.error("Exception reading {}", p_242976_2_, exception);
            return null;
         }
      };
   }

   private BiFunction<File, DataFixer, WorldSummary> levelSummaryReader(File pSaveDir, boolean pLocked) {
      return (p_242977_3_, p_242977_4_) -> {
         try {
            CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(p_242977_3_);
            CompoundNBT compoundnbt1 = compoundnbt.getCompound("Data");
            compoundnbt1.remove("Player");
            int i = compoundnbt1.contains("DataVersion", 99) ? compoundnbt1.getInt("DataVersion") : -1;
            Dynamic<INBT> dynamic = p_242977_4_.update(DefaultTypeReferences.LEVEL.getType(), new Dynamic<>(NBTDynamicOps.INSTANCE, compoundnbt1), i, SharedConstants.getCurrentVersion().getWorldVersion());
            VersionData versiondata = VersionData.parse(dynamic);
            int j = versiondata.levelDataVersion();
            if (j != 19132 && j != 19133) {
               return null;
            } else {
               boolean flag = j != this.getStorageVersion();
               File file1 = new File(pSaveDir, "icon.png");
               DatapackCodec datapackcodec = dynamic.get("DataPacks").result().map(SaveFormat::readDataPackConfig).orElse(DatapackCodec.DEFAULT);
               WorldSettings worldsettings = WorldSettings.parse(dynamic, datapackcodec);
               return new WorldSummary(worldsettings, versiondata, pSaveDir.getName(), flag, pLocked, file1);
            }
         } catch (Exception exception) {
            LOGGER.error("Exception reading {}", p_242977_3_, exception);
            return null;
         }
      };
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isNewLevelIdAcceptable(String pSaveName) {
      try {
         Path path = this.baseDir.resolve(pSaveName);
         Files.createDirectory(path);
         Files.deleteIfExists(path);
         return true;
      } catch (IOException ioexception) {
         return false;
      }
   }

   /**
    * Return whether the given world can be loaded.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean levelExists(String pSaveName) {
      return Files.isDirectory(this.baseDir.resolve(pSaveName));
   }

   @OnlyIn(Dist.CLIENT)
   public Path getBaseDir() {
      return this.baseDir;
   }

   /**
    * Gets the folder where backups are stored
    */
   @OnlyIn(Dist.CLIENT)
   public Path getBackupPath() {
      return this.backupDir;
   }

   public SaveFormat.LevelSave createAccess(String pSaveName) throws IOException {
      return new SaveFormat.LevelSave(pSaveName);
   }

   public class LevelSave implements AutoCloseable {
      private final SessionLockManager lock;
      private final Path levelPath;
      private final String levelId;
      private final Map<FolderName, Path> resources = Maps.newHashMap();

      public LevelSave(String p_i232152_2_) throws IOException {
         this.levelId = p_i232152_2_;
         this.levelPath = SaveFormat.this.baseDir.resolve(p_i232152_2_);
         this.lock = SessionLockManager.create(this.levelPath);
      }

      public String getLevelId() {
         return this.levelId;
      }

      public Path getLevelPath(FolderName pFolderName) {
         return this.resources.computeIfAbsent(pFolderName, (p_237293_1_) -> {
            return this.levelPath.resolve(p_237293_1_.getId());
         });
      }

      public File getDimensionPath(RegistryKey<World> pDimensionKey) {
         return DimensionType.getStorageFolder(pDimensionKey, this.levelPath.toFile());
      }

      private void checkLock() {
         if (!this.lock.isValid()) {
            throw new IllegalStateException("Lock is no longer valid");
         }
      }

      public PlayerData createPlayerStorage() {
         this.checkLock();
         return new PlayerData(this, SaveFormat.this.fixerUpper);
      }

      public boolean requiresConversion() {
         WorldSummary worldsummary = this.getSummary();
         return worldsummary != null && worldsummary.levelVersion().levelDataVersion() != SaveFormat.this.getStorageVersion();
      }

      public boolean convertLevel(IProgressUpdate pProgress) {
         this.checkLock();
         return AnvilSaveConverter.convertLevel(this, pProgress);
      }

      @Nullable
      public WorldSummary getSummary() {
         this.checkLock();
         return SaveFormat.this.readLevelData(this.levelPath.toFile(), SaveFormat.this.levelSummaryReader(this.levelPath.toFile(), false));
      }

      @Nullable
      public IServerConfiguration getDataTag(DynamicOps<INBT> pNbt, DatapackCodec pDatapackCodec) {
         this.checkLock();
         return SaveFormat.this.readLevelData(this.levelPath.toFile(), SaveFormat.getReader(pNbt, pDatapackCodec, this));
      }

      @Nullable
      public DatapackCodec getDataPacks() {
         this.checkLock();
         return SaveFormat.this.readLevelData(this.levelPath.toFile(), (p_237289_0_, p_237289_1_) -> {
            return SaveFormat.getDataPacks(p_237289_0_, p_237289_1_);
         });
      }

      public void saveDataTag(DynamicRegistries pRegistries, IServerConfiguration pServerConfiguration) {
         this.saveDataTag(pRegistries, pServerConfiguration, (CompoundNBT)null);
      }

      public void saveDataTag(DynamicRegistries pRegistries, IServerConfiguration pServerConfiguration, @Nullable CompoundNBT pHostPlayerNBT) {
         File file1 = this.levelPath.toFile();
         CompoundNBT compoundnbt = pServerConfiguration.createTag(pRegistries, pHostPlayerNBT);
         CompoundNBT compoundnbt1 = new CompoundNBT();
         compoundnbt1.put("Data", compoundnbt);

         net.minecraftforge.fml.WorldPersistenceHooks.handleWorldDataSave(this, pServerConfiguration, compoundnbt1);

         try {
            File file2 = File.createTempFile("level", ".dat", file1);
            CompressedStreamTools.writeCompressed(compoundnbt1, file2);
            File file3 = new File(file1, "level.dat_old");
            File file4 = new File(file1, "level.dat");
            Util.safeReplaceFile(file4, file2, file3);
         } catch (Exception exception) {
            SaveFormat.LOGGER.error("Failed to save level {}", file1, exception);
         }

      }

      public File getIconFile() {
         this.checkLock();
         return this.levelPath.resolve("icon.png").toFile();
      }

      public Path getWorldDir() {
          return levelPath;
      }

      @OnlyIn(Dist.CLIENT)
      public void deleteLevel() throws IOException {
         this.checkLock();
         final Path path = this.levelPath.resolve("session.lock");

         for(int i = 1; i <= 5; ++i) {
            SaveFormat.LOGGER.info("Attempt {}...", (int)i);

            try {
               Files.walkFileTree(this.levelPath, new SimpleFileVisitor<Path>() {
                  public FileVisitResult visitFile(Path p_visitFile_1_, BasicFileAttributes p_visitFile_2_) throws IOException {
                     if (!p_visitFile_1_.equals(path)) {
                        SaveFormat.LOGGER.debug("Deleting {}", (Object)p_visitFile_1_);
                        Files.delete(p_visitFile_1_);
                     }

                     return FileVisitResult.CONTINUE;
                  }

                  public FileVisitResult postVisitDirectory(Path p_postVisitDirectory_1_, IOException p_postVisitDirectory_2_) throws IOException {
                     if (p_postVisitDirectory_2_ != null) {
                        throw p_postVisitDirectory_2_;
                     } else {
                        if (p_postVisitDirectory_1_.equals(LevelSave.this.levelPath)) {
                           LevelSave.this.lock.close();
                           Files.deleteIfExists(path);
                        }

                        Files.delete(p_postVisitDirectory_1_);
                        return FileVisitResult.CONTINUE;
                     }
                  }
               });
               break;
            } catch (IOException ioexception) {
               if (i >= 5) {
                  throw ioexception;
               }

               SaveFormat.LOGGER.warn("Failed to delete {}", this.levelPath, ioexception);

               try {
                  Thread.sleep(500L);
               } catch (InterruptedException interruptedexception) {
               }
            }
         }

      }

      @OnlyIn(Dist.CLIENT)
      public void renameLevel(String pSaveName) throws IOException {
         this.checkLock();
         File file1 = new File(SaveFormat.this.baseDir.toFile(), this.levelId);
         if (file1.exists()) {
            File file2 = new File(file1, "level.dat");
            if (file2.exists()) {
               CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(file2);
               CompoundNBT compoundnbt1 = compoundnbt.getCompound("Data");
               compoundnbt1.putString("LevelName", pSaveName);
               CompressedStreamTools.writeCompressed(compoundnbt, file2);
            }

         }
      }

      @OnlyIn(Dist.CLIENT)
      public long makeWorldBackup() throws IOException {
         this.checkLock();
         String s = LocalDateTime.now().format(SaveFormat.FORMATTER) + "_" + this.levelId;
         Path path = SaveFormat.this.getBackupPath();

         try {
            Files.createDirectories(Files.exists(path) ? path.toRealPath() : path);
         } catch (IOException ioexception) {
            throw new RuntimeException(ioexception);
         }

         Path path1 = path.resolve(FileUtil.findAvailableName(path, s, ".zip"));

         try (final ZipOutputStream zipoutputstream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path1)))) {
            final Path path2 = Paths.get(this.levelId);
            Files.walkFileTree(this.levelPath, new SimpleFileVisitor<Path>() {
               public FileVisitResult visitFile(Path p_visitFile_1_, BasicFileAttributes p_visitFile_2_) throws IOException {
                  if (p_visitFile_1_.endsWith("session.lock")) {
                     return FileVisitResult.CONTINUE;
                  } else {
                     String s1 = path2.resolve(LevelSave.this.levelPath.relativize(p_visitFile_1_)).toString().replace('\\', '/');
                     ZipEntry zipentry = new ZipEntry(s1);
                     zipoutputstream.putNextEntry(zipentry);
                     com.google.common.io.Files.asByteSource(p_visitFile_1_.toFile()).copyTo(zipoutputstream);
                     zipoutputstream.closeEntry();
                     return FileVisitResult.CONTINUE;
                  }
               }
            });
         }

         return Files.size(path1);
      }

      public void close() throws IOException {
         this.lock.close();
      }
   }
}
