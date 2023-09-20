package net.minecraft.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.world.gen.feature.template.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureUpdater implements SNBTToNBTConverter.ITransformer {
   private static final Logger LOGGER = LogManager.getLogger();

   public CompoundNBT apply(String pStructureLocationPath, CompoundNBT pTag) {
      return pStructureLocationPath.startsWith("data/minecraft/structures/") ? updateStructure(pStructureLocationPath, patchVersion(pTag)) : pTag;
   }

   private static CompoundNBT patchVersion(CompoundNBT pTag) {
      if (!pTag.contains("DataVersion", 99)) {
         pTag.putInt("DataVersion", 500);
      }

      return pTag;
   }

   private static CompoundNBT updateStructure(String pStructureLocationPath, CompoundNBT pTag) {
      Template template = new Template();
      int i = pTag.getInt("DataVersion");
      int j = 2532;
      if (i < 2532) {
         LOGGER.warn("SNBT Too old, do not forget to update: " + i + " < " + 2532 + ": " + pStructureLocationPath);
      }

      CompoundNBT compoundnbt = NBTUtil.update(DataFixesManager.getDataFixer(), DefaultTypeReferences.STRUCTURE, pTag, i);
      template.load(compoundnbt);
      return template.save(new CompoundNBT());
   }
}