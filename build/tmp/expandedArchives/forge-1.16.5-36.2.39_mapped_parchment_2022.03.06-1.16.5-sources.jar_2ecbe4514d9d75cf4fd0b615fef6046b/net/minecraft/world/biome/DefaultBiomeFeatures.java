package net.minecraft.world.biome;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.structure.StructureFeatures;

public class DefaultBiomeFeatures {
   public static void addDefaultOverworldLandMesaStructures(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addStructureStart(StructureFeatures.MINESHAFT_MESA);
      pBuilder.addStructureStart(StructureFeatures.STRONGHOLD);
   }

   public static void addDefaultOverworldLandStructures(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addStructureStart(StructureFeatures.MINESHAFT);
      pBuilder.addStructureStart(StructureFeatures.STRONGHOLD);
   }

   public static void addDefaultOverworldOceanStructures(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addStructureStart(StructureFeatures.MINESHAFT);
      pBuilder.addStructureStart(StructureFeatures.SHIPWRECK);
   }

   public static void addDefaultCarvers(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.CAVE);
      pBuilder.addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.CANYON);
   }

   public static void addOceanCarvers(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.OCEAN_CAVE);
      pBuilder.addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.CANYON);
      pBuilder.addCarver(GenerationStage.Carving.LIQUID, ConfiguredCarvers.UNDERWATER_CANYON);
      pBuilder.addCarver(GenerationStage.Carving.LIQUID, ConfiguredCarvers.UNDERWATER_CAVE);
   }

   public static void addDefaultLakes(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.LAKES, Features.LAKE_WATER);
      pBuilder.addFeature(GenerationStage.Decoration.LAKES, Features.LAKE_LAVA);
   }

   public static void addDesertLakes(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.LAKES, Features.LAKE_LAVA);
   }

   public static void addDefaultMonsterRoom(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, Features.MONSTER_ROOM);
   }

   public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_DIRT);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_GRAVEL);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_GRANITE);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_DIORITE);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_ANDESITE);
   }

   public static void addDefaultOres(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_COAL);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_IRON);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_REDSTONE);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_LAPIS);
   }

   public static void addExtraGold(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD_EXTRA);
   }

   public static void addExtraEmeralds(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.ORE_EMERALD);
   }

   public static void addInfestedStone(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_INFESTED);
   }

   public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.DISK_SAND);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.DISK_GRAVEL);
   }

   public static void addSwampClayDisk(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
   }

   public static void addMossyStoneBlock(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, Features.FOREST_ROCK);
   }

   public static void addFerns(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_LARGE_FERN);
   }

   public static void addBerryBushes(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_DECORATED);
   }

   public static void addSparseBerryBushes(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_SPARSE);
   }

   public static void addLightBambooVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BAMBOO_LIGHT);
   }

   public static void addBambooVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BAMBOO);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BAMBOO_VEGETATION);
   }

   public static void addTaigaTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TAIGA_VEGETATION);
   }

   public static void addWaterTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TREES_WATER);
   }

   public static void addBirchTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TREES_BIRCH);
   }

   public static void addOtherBirchTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BIRCH_OTHER);
   }

   public static void addTallBirchTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BIRCH_TALL);
   }

   public static void addSavannaTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TREES_SAVANNA);
   }

   public static void addShatteredSavannaTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TREES_SHATTERED_SAVANNA);
   }

   public static void addMountainTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN);
   }

   public static void addMountainEdgeTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN_EDGE);
   }

   public static void addJungleTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE);
   }

   public static void addJungleEdgeTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE_EDGE);
   }

   public static void addBadlandsTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.OAK_BADLANDS);
   }

   public static void addSnowyTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SPRUCE_SNOWY);
   }

   public static void addJungleGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_JUNGLE);
   }

   public static void addSavannaGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS);
   }

   public static void addShatteredSavannaGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
   }

   public static void addSavannaExtraGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_SAVANNA);
   }

   public static void addBadlandGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_BADLANDS);
   }

   public static void addForestFlowers(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION);
   }

   public static void addForestGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_FOREST);
   }

   public static void addSwampVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SWAMP_TREE);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FLOWER_SWAMP);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_WATERLILLY);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_SWAMP);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_SWAMP);
   }

   public static void addMushroomFieldVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.MUSHROOM_FIELD_VEGETATION);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
   }

   public static void addPlainVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PLAIN_VEGETATION);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FLOWER_PLAIN_DECORATED);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
   }

   public static void addDesertVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_2);
   }

   public static void addGiantTaigaVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_GIANT);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_GIANT);
   }

   public static void addDefaultFlowers(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FLOWER_DEFAULT);
   }

   public static void addWarmFlowers(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FLOWER_WARM);
   }

   public static void addDefaultGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
   }

   public static void addTaigaGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA_2);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
   }

   public static void addPlainGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS_2);
   }

   public static void addDefaultMushrooms(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_NORMAL);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_NORMAL);
   }

   public static void addDefaultExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
   }

   public static void addBadlandExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_BADLANDS);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DECORATED);
   }

   public static void addJungleExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_MELON);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.VINES);
   }

   public static void addDesertExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_DESERT);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DESERT);
   }

   public static void addSwampExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_SWAMP);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
   }

   public static void addDesertExtraDecoration(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.WELL);
   }

   public static void addFossilDecoration(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, Features.FOSSIL);
   }

   public static void addColdOceanExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.KELP_COLD);
   }

   public static void addDefaultSeagrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SIMPLE);
   }

   public static void addLukeWarmKelp(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.KELP_WARM);
   }

   public static void addDefaultSprings(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SPRING_WATER);
      pBuilder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
   }

   public static void addIcebergs(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_PACKED);
      pBuilder.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_BLUE);
   }

   public static void addBlueIce(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.BLUE_ICE);
   }

   public static void addSurfaceFreezing(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, Features.FREEZE_TOP_LAYER);
   }

   public static void addNetherDefaultOres(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_GRAVEL_NETHER);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_BLACKSTONE);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_NETHER);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_NETHER);
      addAncientDebris(pBuilder);
   }

   public static void addAncientDebris(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_LARGE);
      pBuilder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_SMALL);
   }

   public static void farmAnimals(MobSpawnInfo.Builder pBuilder) {
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.SHEEP, 12, 4, 4));
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.PIG, 10, 4, 4));
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.CHICKEN, 10, 4, 4));
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.COW, 8, 4, 4));
   }

   public static void ambientSpawns(MobSpawnInfo.Builder p_243734_0_) {
      p_243734_0_.addSpawn(EntityClassification.AMBIENT, new MobSpawnInfo.Spawners(EntityType.BAT, 10, 8, 8));
   }

   public static void commonSpawns(MobSpawnInfo.Builder pBuilder) {
      ambientSpawns(pBuilder);
      monsters(pBuilder, 95, 5, 100);
   }

   public static void oceanSpawns(MobSpawnInfo.Builder pBuilder, int pSquidWeight, int pSquidMaxCount, int pCodWeight) {
      pBuilder.addSpawn(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(EntityType.SQUID, pSquidWeight, 1, pSquidMaxCount));
      pBuilder.addSpawn(EntityClassification.WATER_AMBIENT, new MobSpawnInfo.Spawners(EntityType.COD, pCodWeight, 3, 6));
      commonSpawns(pBuilder);
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.DROWNED, 5, 1, 1));
   }

   public static void warmOceanSpawns(MobSpawnInfo.Builder pBuilder, int pSquidWeight, int pSquidMinCount) {
      pBuilder.addSpawn(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(EntityType.SQUID, pSquidWeight, pSquidMinCount, 4));
      pBuilder.addSpawn(EntityClassification.WATER_AMBIENT, new MobSpawnInfo.Spawners(EntityType.TROPICAL_FISH, 25, 8, 8));
      pBuilder.addSpawn(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(EntityType.DOLPHIN, 2, 1, 2));
      commonSpawns(pBuilder);
   }

   public static void plainsSpawns(MobSpawnInfo.Builder pBuilder) {
      farmAnimals(pBuilder);
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.HORSE, 5, 2, 6));
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.DONKEY, 1, 1, 3));
      commonSpawns(pBuilder);
   }

   public static void snowySpawns(MobSpawnInfo.Builder pBuilder) {
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.RABBIT, 10, 2, 3));
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.POLAR_BEAR, 1, 1, 2));
      ambientSpawns(pBuilder);
      monsters(pBuilder, 95, 5, 20);
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.STRAY, 80, 4, 4));
   }

   public static void desertSpawns(MobSpawnInfo.Builder pBuilder) {
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.RABBIT, 4, 2, 3));
      ambientSpawns(pBuilder);
      monsters(pBuilder, 19, 1, 100);
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.HUSK, 80, 4, 4));
   }

   public static void monsters(MobSpawnInfo.Builder pBuilder, int pZombieWeight, int pZombieVillagerWeight, int pSkeletonWeight) {
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SPIDER, 100, 4, 4));
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ZOMBIE, pZombieWeight, 4, 4));
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ZOMBIE_VILLAGER, pZombieVillagerWeight, 1, 1));
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SKELETON, pSkeletonWeight, 4, 4));
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.CREEPER, 100, 4, 4));
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SLIME, 100, 4, 4));
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 10, 1, 4));
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.WITCH, 5, 1, 1));
   }

   public static void mooshroomSpawns(MobSpawnInfo.Builder pBuilder) {
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.MOOSHROOM, 8, 4, 8));
      ambientSpawns(pBuilder);
   }

   public static void baseJungleSpawns(MobSpawnInfo.Builder pBuilder) {
      farmAnimals(pBuilder);
      pBuilder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.CHICKEN, 10, 4, 4));
      commonSpawns(pBuilder);
   }

   public static void endSpawns(MobSpawnInfo.Builder pBuilder) {
      pBuilder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 10, 4, 4));
   }
}