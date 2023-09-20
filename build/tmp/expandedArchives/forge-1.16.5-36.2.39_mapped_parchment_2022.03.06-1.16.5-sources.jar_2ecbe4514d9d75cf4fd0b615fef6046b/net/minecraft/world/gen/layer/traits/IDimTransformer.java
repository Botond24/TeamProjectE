package net.minecraft.world.gen.layer.traits;

public interface IDimTransformer {
   int getParentX(int pX);

   int getParentY(int pZ);
}