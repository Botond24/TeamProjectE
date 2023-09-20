package net.minecraft.world.biome;

import com.google.common.hash.Hashing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BiomeManager {
   private final BiomeManager.IBiomeReader noiseBiomeSource;
   private final long biomeZoomSeed;
   private final IBiomeMagnifier zoomer;

   public BiomeManager(BiomeManager.IBiomeReader pNoiseBiomeSource, long pBiomeZoomSeed, IBiomeMagnifier pZoomer) {
      this.noiseBiomeSource = pNoiseBiomeSource;
      this.biomeZoomSeed = pBiomeZoomSeed;
      this.zoomer = pZoomer;
   }

   public static long obfuscateSeed(long pSeed) {
      return Hashing.sha256().hashLong(pSeed).asLong();
   }

   public BiomeManager withDifferentSource(BiomeProvider pNewProvider) {
      return new BiomeManager(pNewProvider, this.biomeZoomSeed, this.zoomer);
   }

   public Biome getBiome(BlockPos pPos) {
      return this.zoomer.getBiome(this.biomeZoomSeed, pPos.getX(), pPos.getY(), pPos.getZ(), this.noiseBiomeSource);
   }

   @OnlyIn(Dist.CLIENT)
   public Biome getNoiseBiomeAtPosition(double pX, double pY, double pZ) {
      int i = MathHelper.floor(pX) >> 2;
      int j = MathHelper.floor(pY) >> 2;
      int k = MathHelper.floor(pZ) >> 2;
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   @OnlyIn(Dist.CLIENT)
   public Biome getNoiseBiomeAtPosition(BlockPos pPos) {
      int i = pPos.getX() >> 2;
      int j = pPos.getY() >> 2;
      int k = pPos.getZ() >> 2;
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   @OnlyIn(Dist.CLIENT)
   public Biome getNoiseBiomeAtQuart(int pX, int pY, int pZ) {
      return this.noiseBiomeSource.getNoiseBiome(pX, pY, pZ);
   }

   public interface IBiomeReader {
      Biome getNoiseBiome(int pX, int pY, int pZ);
   }
}