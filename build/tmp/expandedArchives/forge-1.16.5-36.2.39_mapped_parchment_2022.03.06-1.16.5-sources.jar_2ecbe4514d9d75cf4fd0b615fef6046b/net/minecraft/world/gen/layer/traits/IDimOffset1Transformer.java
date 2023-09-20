package net.minecraft.world.gen.layer.traits;

public interface IDimOffset1Transformer extends IDimTransformer {
   default int getParentX(int pX) {
      return pX - 1;
   }

   default int getParentY(int pZ) {
      return pZ - 1;
   }
}