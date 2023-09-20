package net.minecraft.world.biome.provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SingleBiomeProvider extends BiomeProvider {
   public static final Codec<SingleBiomeProvider> CODEC = Biome.CODEC.fieldOf("biome").xmap(SingleBiomeProvider::new, (p_235261_0_) -> {
      return p_235261_0_.biome;
   }).stable().codec();
   private final Supplier<Biome> biome;

   public SingleBiomeProvider(Biome pBiome) {
      this(() -> {
         return pBiome;
      });
   }

   public SingleBiomeProvider(Supplier<Biome> p_i241945_1_) {
      super(ImmutableList.of(p_i241945_1_.get()));
      this.biome = p_i241945_1_;
   }

   protected Codec<? extends BiomeProvider> codec() {
      return CODEC;
   }

   @OnlyIn(Dist.CLIENT)
   public BiomeProvider withSeed(long pSeed) {
      return this;
   }

   public Biome getNoiseBiome(int pX, int pY, int pZ) {
      return this.biome.get();
   }

   @Nullable
   public BlockPos findBiomeHorizontal(int pX, int pY, int pZ, int pRadius, int pIncrement, Predicate<Biome> pBiomes, Random pRandom, boolean pFindClosest) {
      if (pBiomes.test(this.biome.get())) {
         return pFindClosest ? new BlockPos(pX, pY, pZ) : new BlockPos(pX - pRadius + pRandom.nextInt(pRadius * 2 + 1), pY, pZ - pRadius + pRandom.nextInt(pRadius * 2 + 1));
      } else {
         return null;
      }
   }

   /**
    * Returns the set of biomes contained in cube of side length 2 * radius + 1 centered at (xIn, yIn, zIn)
    */
   public Set<Biome> getBiomesWithin(int pX, int pY, int pZ, int pRadius) {
      return Sets.newHashSet(this.biome.get());
   }
}