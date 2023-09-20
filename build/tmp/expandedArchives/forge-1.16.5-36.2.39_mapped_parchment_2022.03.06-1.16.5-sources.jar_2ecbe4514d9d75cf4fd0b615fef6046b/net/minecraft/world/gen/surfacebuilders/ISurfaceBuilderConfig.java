package net.minecraft.world.gen.surfacebuilders;

import net.minecraft.block.BlockState;

public interface ISurfaceBuilderConfig {
   /**
    * The state to be placed as the top level of surface, above water. Typically grass or sand.
    */
   BlockState getTopMaterial();

   /**
    * The state to be placed underneath the surface, above water. Typically dirt under grass, or more sand under sand.
    */
   BlockState getUnderMaterial();
}