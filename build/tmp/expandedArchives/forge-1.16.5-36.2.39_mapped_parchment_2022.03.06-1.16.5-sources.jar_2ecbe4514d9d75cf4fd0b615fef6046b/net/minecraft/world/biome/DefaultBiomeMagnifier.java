package net.minecraft.world.biome;

public enum DefaultBiomeMagnifier implements IBiomeMagnifier {
   INSTANCE;

   public Biome getBiome(long pSeed, int pX, int pY, int pZ, BiomeManager.IBiomeReader pBiomeReader) {
      return pBiomeReader.getNoiseBiome(pX >> 2, pY >> 2, pZ >> 2);
   }
}