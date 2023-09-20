package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.IC0Transformer;

public class BiomeLayer implements IC0Transformer {
   private static final int[] LEGACY_WARM_BIOMES = new int[]{2, 4, 3, 6, 1, 5};
   private static final int[] WARM_BIOMES = new int[]{2, 2, 2, 35, 35, 1};
   private static final int[] MEDIUM_BIOMES = new int[]{4, 29, 3, 1, 27, 6};
   private static final int[] COLD_BIOMES = new int[]{4, 3, 5, 1};
   private static final int[] ICE_BIOMES = new int[]{12, 12, 12, 30};
   private int[] warmBiomes = WARM_BIOMES;
   private final boolean legacyDesert;
   private java.util.List<net.minecraftforge.common.BiomeManager.BiomeEntry>[] biomes = new java.util.ArrayList[net.minecraftforge.common.BiomeManager.BiomeType.values().length];

   public BiomeLayer(boolean p_i232147_1_) {
      this.legacyDesert = p_i232147_1_;
      for (net.minecraftforge.common.BiomeManager.BiomeType type : net.minecraftforge.common.BiomeManager.BiomeType.values())
         biomes[type.ordinal()] = new java.util.ArrayList<>(net.minecraftforge.common.BiomeManager.getBiomes(type));
   }

   public int apply(INoiseRandom pContext, int pValue) {
      int i = (pValue & 3840) >> 8;
      pValue = pValue & -3841;
      if (!LayerUtil.isOcean(pValue) && pValue != 14) {
         switch(pValue) {
         case 1:
            if (i > 0) {
               return pContext.nextRandom(3) == 0 ? 39 : 38;
            }

            return getBiomeId(net.minecraftforge.common.BiomeManager.BiomeType.DESERT, pContext);
         case 2:
            if (i > 0) {
               return 21;
            }

            return getBiomeId(net.minecraftforge.common.BiomeManager.BiomeType.WARM, pContext);
         case 3:
            if (i > 0) {
               return 32;
            }

            return getBiomeId(net.minecraftforge.common.BiomeManager.BiomeType.COOL, pContext);
         case 4:
            return getBiomeId(net.minecraftforge.common.BiomeManager.BiomeType.ICY, pContext);
         default:
            return 14;
         }
      } else {
         return pValue;
      }
   }

   private int getBiomeId(net.minecraftforge.common.BiomeManager.BiomeType type, INoiseRandom context) {
      return net.minecraft.util.registry.WorldGenRegistries.BIOME.getId(
         net.minecraft.util.registry.WorldGenRegistries.BIOME.get(getBiome(type, context)));
   }
   protected net.minecraft.util.RegistryKey<net.minecraft.world.biome.Biome> getBiome(net.minecraftforge.common.BiomeManager.BiomeType type, INoiseRandom context) {
      if (type == net.minecraftforge.common.BiomeManager.BiomeType.DESERT && this.legacyDesert)
         type = net.minecraftforge.common.BiomeManager.BiomeType.DESERT_LEGACY;
      java.util.List<net.minecraftforge.common.BiomeManager.BiomeEntry> biomeList = biomes[type.ordinal()];
      int totalWeight = net.minecraft.util.WeightedRandom.getTotalWeight(biomeList);
      int weight = net.minecraftforge.common.BiomeManager.isTypeListModded(type) ? context.nextRandom(totalWeight) : context.nextRandom(totalWeight / 10) * 10;
      return net.minecraft.util.WeightedRandom.getWeightedItem(biomeList, weight).getKey();
   }
}
