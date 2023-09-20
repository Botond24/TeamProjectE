package net.minecraft.data;

import java.nio.file.Path;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class FluidTagsProvider extends TagsProvider<Fluid> {
   @Deprecated
   public FluidTagsProvider(DataGenerator pGenerator) {
      super(pGenerator, Registry.FLUID);
   }
   public FluidTagsProvider(DataGenerator pGenerator, String modId, @javax.annotation.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pGenerator, Registry.FLUID, modId, existingFileHelper);
   }

   protected void addTags() {
      this.tag(FluidTags.WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
      this.tag(FluidTags.LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
   }

   /**
    * Resolves a Path for the location to save the given tag.
    */
   protected Path getPath(ResourceLocation pId) {
      return this.generator.getOutputFolder().resolve("data/" + pId.getNamespace() + "/tags/fluids/" + pId.getPath() + ".json");
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public String getName() {
      return "Fluid Tags";
   }
}
