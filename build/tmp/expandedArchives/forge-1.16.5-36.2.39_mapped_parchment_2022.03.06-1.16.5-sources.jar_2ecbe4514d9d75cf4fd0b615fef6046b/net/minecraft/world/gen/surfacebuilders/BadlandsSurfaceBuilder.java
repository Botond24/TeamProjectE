package net.minecraft.world.gen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.PerlinNoiseGenerator;

public class BadlandsSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderConfig> {
   private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
   private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
   private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
   private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
   private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
   private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
   private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
   protected BlockState[] clayBands;
   protected long seed;
   protected PerlinNoiseGenerator pillarNoise;
   protected PerlinNoiseGenerator pillarRoofNoise;
   protected PerlinNoiseGenerator clayBandsOffsetNoise;

   public BadlandsSurfaceBuilder(Codec<SurfaceBuilderConfig> p_i232122_1_) {
      super(p_i232122_1_);
   }

   public void apply(Random p_205610_1_, IChunk p_205610_2_, Biome p_205610_3_, int p_205610_4_, int p_205610_5_, int p_205610_6_, double p_205610_7_, BlockState p_205610_9_, BlockState p_205610_10_, int p_205610_11_, long p_205610_12_, SurfaceBuilderConfig p_205610_14_) {
      int i = p_205610_4_ & 15;
      int j = p_205610_5_ & 15;
      BlockState blockstate = WHITE_TERRACOTTA;
      ISurfaceBuilderConfig isurfacebuilderconfig = p_205610_3_.getGenerationSettings().getSurfaceBuilderConfig();
      BlockState blockstate1 = isurfacebuilderconfig.getUnderMaterial();
      BlockState blockstate2 = isurfacebuilderconfig.getTopMaterial();
      BlockState blockstate3 = blockstate1;
      int k = (int)(p_205610_7_ / 3.0D + 3.0D + p_205610_1_.nextDouble() * 0.25D);
      boolean flag = Math.cos(p_205610_7_ / 3.0D * Math.PI) > 0.0D;
      int l = -1;
      boolean flag1 = false;
      int i1 = 0;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int j1 = p_205610_6_; j1 >= 0; --j1) {
         if (i1 < 15) {
            blockpos$mutable.set(i, j1, j);
            BlockState blockstate4 = p_205610_2_.getBlockState(blockpos$mutable);
            if (blockstate4.isAir()) {
               l = -1;
            } else if (blockstate4.is(p_205610_9_.getBlock())) {
               if (l == -1) {
                  flag1 = false;
                  if (k <= 0) {
                     blockstate = Blocks.AIR.defaultBlockState();
                     blockstate3 = p_205610_9_;
                  } else if (j1 >= p_205610_11_ - 4 && j1 <= p_205610_11_ + 1) {
                     blockstate = WHITE_TERRACOTTA;
                     blockstate3 = blockstate1;
                  }

                  if (j1 < p_205610_11_ && (blockstate == null || blockstate.isAir())) {
                     blockstate = p_205610_10_;
                  }

                  l = k + Math.max(0, j1 - p_205610_11_);
                  if (j1 >= p_205610_11_ - 1) {
                     if (j1 > p_205610_11_ + 3 + k) {
                        BlockState blockstate5;
                        if (j1 >= 64 && j1 <= 127) {
                           if (flag) {
                              blockstate5 = TERRACOTTA;
                           } else {
                              blockstate5 = this.getBand(p_205610_4_, j1, p_205610_5_);
                           }
                        } else {
                           blockstate5 = ORANGE_TERRACOTTA;
                        }

                        p_205610_2_.setBlockState(blockpos$mutable, blockstate5, false);
                     } else {
                        p_205610_2_.setBlockState(blockpos$mutable, blockstate2, false);
                        flag1 = true;
                     }
                  } else {
                     p_205610_2_.setBlockState(blockpos$mutable, blockstate3, false);
                     Block block = blockstate3.getBlock();
                     if (block == Blocks.WHITE_TERRACOTTA || block == Blocks.ORANGE_TERRACOTTA || block == Blocks.MAGENTA_TERRACOTTA || block == Blocks.LIGHT_BLUE_TERRACOTTA || block == Blocks.YELLOW_TERRACOTTA || block == Blocks.LIME_TERRACOTTA || block == Blocks.PINK_TERRACOTTA || block == Blocks.GRAY_TERRACOTTA || block == Blocks.LIGHT_GRAY_TERRACOTTA || block == Blocks.CYAN_TERRACOTTA || block == Blocks.PURPLE_TERRACOTTA || block == Blocks.BLUE_TERRACOTTA || block == Blocks.BROWN_TERRACOTTA || block == Blocks.GREEN_TERRACOTTA || block == Blocks.RED_TERRACOTTA || block == Blocks.BLACK_TERRACOTTA) {
                        p_205610_2_.setBlockState(blockpos$mutable, ORANGE_TERRACOTTA, false);
                     }
                  }
               } else if (l > 0) {
                  --l;
                  if (flag1) {
                     p_205610_2_.setBlockState(blockpos$mutable, ORANGE_TERRACOTTA, false);
                  } else {
                     p_205610_2_.setBlockState(blockpos$mutable, this.getBand(p_205610_4_, j1, p_205610_5_), false);
                  }
               }

               ++i1;
            }
         }
      }

   }

   /**
    * Initialize this surface builder with the current world seed.
    * This is called prior to {@link #apply}. In general, most subclasses cache the world seed and only re-initialize if
    * the cached seed is different from the provided seed, for performance.
    */
   public void initNoise(long pSeed) {
      if (this.seed != pSeed || this.clayBands == null) {
         this.generateBands(pSeed);
      }

      if (this.seed != pSeed || this.pillarNoise == null || this.pillarRoofNoise == null) {
         SharedSeedRandom sharedseedrandom = new SharedSeedRandom(pSeed);
         this.pillarNoise = new PerlinNoiseGenerator(sharedseedrandom, IntStream.rangeClosed(-3, 0));
         this.pillarRoofNoise = new PerlinNoiseGenerator(sharedseedrandom, ImmutableList.of(0));
      }

      this.seed = pSeed;
   }

   /**
    * Generates an array of block states representing the colored clay bands in Badlands biomes.
    * The bands are then sampled via {@link #getBand(int, int, int)}, which additionally offsets the bands a little
    * vertically based on the local x and z position.
    */
   protected void generateBands(long pSeed) {
      this.clayBands = new BlockState[64];
      Arrays.fill(this.clayBands, TERRACOTTA);
      SharedSeedRandom sharedseedrandom = new SharedSeedRandom(pSeed);
      this.clayBandsOffsetNoise = new PerlinNoiseGenerator(sharedseedrandom, ImmutableList.of(0));

      for(int l1 = 0; l1 < 64; ++l1) {
         l1 += sharedseedrandom.nextInt(5) + 1;
         if (l1 < 64) {
            this.clayBands[l1] = ORANGE_TERRACOTTA;
         }
      }

      int i2 = sharedseedrandom.nextInt(4) + 2;

      for(int i = 0; i < i2; ++i) {
         int j = sharedseedrandom.nextInt(3) + 1;
         int k = sharedseedrandom.nextInt(64);

         for(int l = 0; k + l < 64 && l < j; ++l) {
            this.clayBands[k + l] = YELLOW_TERRACOTTA;
         }
      }

      int j2 = sharedseedrandom.nextInt(4) + 2;

      for(int k2 = 0; k2 < j2; ++k2) {
         int i3 = sharedseedrandom.nextInt(3) + 2;
         int l3 = sharedseedrandom.nextInt(64);

         for(int i1 = 0; l3 + i1 < 64 && i1 < i3; ++i1) {
            this.clayBands[l3 + i1] = BROWN_TERRACOTTA;
         }
      }

      int l2 = sharedseedrandom.nextInt(4) + 2;

      for(int j3 = 0; j3 < l2; ++j3) {
         int i4 = sharedseedrandom.nextInt(3) + 1;
         int k4 = sharedseedrandom.nextInt(64);

         for(int j1 = 0; k4 + j1 < 64 && j1 < i4; ++j1) {
            this.clayBands[k4 + j1] = RED_TERRACOTTA;
         }
      }

      int k3 = sharedseedrandom.nextInt(3) + 3;
      int j4 = 0;

      for(int l4 = 0; l4 < k3; ++l4) {
         int i5 = 1;
         j4 += sharedseedrandom.nextInt(16) + 4;

         for(int k1 = 0; j4 + k1 < 64 && k1 < 1; ++k1) {
            this.clayBands[j4 + k1] = WHITE_TERRACOTTA;
            if (j4 + k1 > 1 && sharedseedrandom.nextBoolean()) {
               this.clayBands[j4 + k1 - 1] = LIGHT_GRAY_TERRACOTTA;
            }

            if (j4 + k1 < 63 && sharedseedrandom.nextBoolean()) {
               this.clayBands[j4 + k1 + 1] = LIGHT_GRAY_TERRACOTTA;
            }
         }
      }

   }

   /**
    * Samples the clay band at the given position.
    */
   protected BlockState getBand(int pX, int pY, int pZ) {
      int i = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)pX / 512.0D, (double)pZ / 512.0D, false) * 2.0D);
      return this.clayBands[(pY + i + 64) % 64];
   }
}