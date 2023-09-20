package net.minecraft.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NBTToSNBTConverter implements IDataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private final DataGenerator generator;

   public NBTToSNBTConverter(DataGenerator pGenerator) {
      this.generator = pGenerator;
   }

   /**
    * Performs this provider's action.
    */
   public void run(DirectoryCache pCache) throws IOException {
      Path path = this.generator.getOutputFolder();

      for(Path path1 : this.generator.getInputFolders()) {
         Files.walk(path1).filter((p_200416_0_) -> {
            return p_200416_0_.toString().endsWith(".nbt");
         }).forEach((p_200415_3_) -> {
            convertStructure(p_200415_3_, this.getName(path1, p_200415_3_), path);
         });
      }

   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public String getName() {
      return "NBT to SNBT";
   }

   /**
    * Gets the name of the given NBT file, based on its path and the input directory. The result does not have the
    * ".nbt" extension.
    */
   private String getName(Path pInputFolder, Path pFile) {
      String s = pInputFolder.relativize(pFile).toString().replaceAll("\\\\", "/");
      return s.substring(0, s.length() - ".nbt".length());
   }

   @Nullable
   public static Path convertStructure(Path pSnbtPath, String pName, Path pNbtPath) {
      try {
         CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(Files.newInputStream(pSnbtPath));
         ITextComponent itextcomponent = compoundnbt.getPrettyDisplay("    ", 0);
         String s = itextcomponent.getString() + "\n";
         Path path = pNbtPath.resolve(pName + ".snbt");
         Files.createDirectories(path.getParent());

         try (BufferedWriter bufferedwriter = Files.newBufferedWriter(path)) {
            bufferedwriter.write(s);
         }

         LOGGER.info("Converted {} from NBT to SNBT", (Object)pName);
         return path;
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", pName, pSnbtPath, ioexception);
         return null;
      }
   }
}