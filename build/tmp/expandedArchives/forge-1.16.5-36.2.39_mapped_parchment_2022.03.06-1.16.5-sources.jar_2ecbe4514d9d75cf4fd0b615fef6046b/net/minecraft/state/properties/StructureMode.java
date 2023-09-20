package net.minecraft.state.properties;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum StructureMode implements IStringSerializable {
   SAVE("save"),
   LOAD("load"),
   CORNER("corner"),
   DATA("data");

   private final String name;
   private final ITextComponent displayName;

   private StructureMode(String pName) {
      this.name = pName;
      this.displayName = new TranslationTextComponent("structure_block.mode_info." + pName);
   }

   public String getSerializedName() {
      return this.name;
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getDisplayName() {
      return this.displayName;
   }
}