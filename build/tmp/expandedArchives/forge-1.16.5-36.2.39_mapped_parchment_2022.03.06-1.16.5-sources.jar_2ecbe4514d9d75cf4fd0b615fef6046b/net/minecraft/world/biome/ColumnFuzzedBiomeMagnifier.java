package net.minecraft.world.biome;

public enum ColumnFuzzedBiomeMagnifier implements IBiomeMagnifier {
   INSTANCE;

   public Biome getBiome(long pSeed, int pX, int pY, int pZ, BiomeManager.IBiomeReader pBiomeReader) {
      return FuzzedBiomeMagnifier.INSTANCE.getBiome(pSeed, pX, 0, pZ, pBiomeReader);
   }
}