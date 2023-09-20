package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;

public interface IChunkLightProvider {
   @Nullable
   IBlockReader getChunkForLighting(int pChunkX, int pChunkZ);

   default void onLightUpdate(LightType pType, SectionPos pPos) {
   }

   IBlockReader getLevel();
}