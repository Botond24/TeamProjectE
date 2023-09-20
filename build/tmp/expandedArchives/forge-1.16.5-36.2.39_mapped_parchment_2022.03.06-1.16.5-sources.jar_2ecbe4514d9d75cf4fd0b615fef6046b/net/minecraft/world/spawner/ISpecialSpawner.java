package net.minecraft.world.spawner;

import net.minecraft.world.server.ServerWorld;

public interface ISpecialSpawner {
   int tick(ServerWorld pLevel, boolean pSpawnHostiles, boolean pSpawnPassives);
}