package net.minecraft.resources.data;

import net.minecraft.util.text.ITextComponent;

public class PackMetadataSection {
   public static final PackMetadataSectionSerializer SERIALIZER = new PackMetadataSectionSerializer();
   private final ITextComponent description;
   private final int packFormat;

   public PackMetadataSection(ITextComponent pDescription, int pPackFormat) {
      this.description = pDescription;
      this.packFormat = pPackFormat;
   }

   public ITextComponent getDescription() {
      return this.description;
   }

   public int getPackFormat() {
      return this.packFormat;
   }
}