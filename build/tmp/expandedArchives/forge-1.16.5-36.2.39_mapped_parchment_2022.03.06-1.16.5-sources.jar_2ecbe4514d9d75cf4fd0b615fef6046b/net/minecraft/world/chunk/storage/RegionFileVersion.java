package net.minecraft.world.chunk.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;

/**
 * A decorator for input and output streams used to read and write the chunk data from region files. This exists as
 * there are different ways of compressing the chunk data inside a region file.
 * @see net.minecraft.world.level.chunk.storage.RegionFileVersion#VERSION_GZIP
 * @see net.minecraft.world.level.chunk.storage.RegionFileVersion#VERSION_DEFLATE
 * @see net.minecraft.world.level.chunk.storage.RegionFileVersion#VERSION_NONE
 */
public class RegionFileVersion {
   private static final Int2ObjectMap<RegionFileVersion> VERSIONS = new Int2ObjectOpenHashMap<>();
   /** Used to store the chunk data in gzip format. Unused in practice. */
   public static final RegionFileVersion VERSION_GZIP = register(new RegionFileVersion(1, GZIPInputStream::new, GZIPOutputStream::new));
   /** Used to store the chunk data in zlib format. This is the default. */
   public static final RegionFileVersion VERSION_DEFLATE = register(new RegionFileVersion(2, InflaterInputStream::new, DeflaterOutputStream::new));
   /** Used to keep the chunk data uncompressed. Unused in practice. */
   public static final RegionFileVersion VERSION_NONE = register(new RegionFileVersion(3, (p_227171_0_) -> {
      return p_227171_0_;
   }, (p_227172_0_) -> {
      return p_227172_0_;
   }));
   private final int id;
   private final RegionFileVersion.IWrapper<InputStream> inputWrapper;
   private final RegionFileVersion.IWrapper<OutputStream> outputWrapper;

   private RegionFileVersion(int pId, RegionFileVersion.IWrapper<InputStream> pInputWrapper, RegionFileVersion.IWrapper<OutputStream> pOutputWrapper) {
      this.id = pId;
      this.inputWrapper = pInputWrapper;
      this.outputWrapper = pOutputWrapper;
   }

   private static RegionFileVersion register(RegionFileVersion pFileVersion) {
      VERSIONS.put(pFileVersion.id, pFileVersion);
      return pFileVersion;
   }

   @Nullable
   public static RegionFileVersion fromId(int pId) {
      return VERSIONS.get(pId);
   }

   public static boolean isValidVersion(int pId) {
      return VERSIONS.containsKey(pId);
   }

   public int getId() {
      return this.id;
   }

   public OutputStream wrap(OutputStream pOutputStream) throws IOException {
      return this.outputWrapper.wrap(pOutputStream);
   }

   public InputStream wrap(InputStream pInputStream) throws IOException {
      return this.inputWrapper.wrap(pInputStream);
   }

   @FunctionalInterface
   interface IWrapper<O> {
      O wrap(O p_wrap_1_) throws IOException;
   }
}