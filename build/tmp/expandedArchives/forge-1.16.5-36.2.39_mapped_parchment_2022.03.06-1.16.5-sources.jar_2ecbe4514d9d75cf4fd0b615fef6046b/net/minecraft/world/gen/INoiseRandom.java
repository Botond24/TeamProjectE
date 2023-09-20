package net.minecraft.world.gen;

public interface INoiseRandom {
   int nextRandom(int pBound);

   ImprovedNoiseGenerator getBiomeNoise();
}