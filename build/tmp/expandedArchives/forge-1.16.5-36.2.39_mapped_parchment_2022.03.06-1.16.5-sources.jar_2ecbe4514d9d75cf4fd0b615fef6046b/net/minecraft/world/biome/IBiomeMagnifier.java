package net.minecraft.world.biome;

public interface IBiomeMagnifier {
   Biome getBiome(long pSeed, int pX, int pY, int pZ, BiomeManager.IBiomeReader pBiomeReader);
}