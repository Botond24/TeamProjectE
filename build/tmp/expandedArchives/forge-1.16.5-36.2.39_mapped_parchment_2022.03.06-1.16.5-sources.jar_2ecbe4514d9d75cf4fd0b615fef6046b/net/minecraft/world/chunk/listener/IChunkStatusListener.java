package net.minecraft.world.chunk.listener;

import javax.annotation.Nullable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;

public interface IChunkStatusListener {
   void updateSpawnPos(ChunkPos pCenter);

   void onStatusChange(ChunkPos pChunkPosition, @Nullable ChunkStatus pNewStatus);

   void stop();
}