package net.minecraft.world.gen.layer.traits;

public interface IDimOffset0Transformer extends IDimTransformer {
   default int getParentX(int pX) {
      return pX;
   }

   default int getParentY(int pZ) {
      return pZ;
   }
}