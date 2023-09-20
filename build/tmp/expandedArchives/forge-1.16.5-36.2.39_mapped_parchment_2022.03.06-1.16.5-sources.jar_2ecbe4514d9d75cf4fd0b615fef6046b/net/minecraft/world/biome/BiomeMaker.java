package net.minecraft.world.biome;

import net.minecraft.client.audio.BackgroundMusicTracks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilders;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;

public class BiomeMaker {
   private static int calculateSkyColor(float pTemperature) {
      float lvt_1_1_ = pTemperature / 3.0F;
      lvt_1_1_ = MathHelper.clamp(lvt_1_1_, -1.0F, 1.0F);
      return MathHelper.hsvToRgb(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
   }

   public static Biome giantTreeTaiga(float pDepth, float pScale, float pTemperature, boolean pIsSpruceVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.farmAnimals(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.WOLF, 8, 4, 4));
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.RABBIT, 4, 2, 3));
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.FOX, 8, 2, 4));
      if (pIsSpruceVariant) {
         DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      } else {
         DefaultBiomeFeatures.ambientSpawns(mobspawninfo$builder);
         DefaultBiomeFeatures.monsters(mobspawninfo$builder, 100, 25, 100);
      }

      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GIANT_TREE_TAIGA);
      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addMossyStoneBlock(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addFerns(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, pIsSpruceVariant ? Features.TREES_GIANT_SPRUCE : Features.TREES_GIANT);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addGiantTaigaVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSparseBerryBushes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.TAIGA).depth(pDepth).scale(pScale).temperature(pTemperature).downfall(0.8F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome birchForestBiome(float pDepth, float pScale, boolean pIsTallVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.farmAnimals(mobspawninfo$builder);
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addForestFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsTallVariant) {
         DefaultBiomeFeatures.addTallBirchTrees(biomegenerationsettings$builder);
      } else {
         DefaultBiomeFeatures.addBirchTrees(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addForestGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.FOREST).depth(pDepth).scale(pScale).temperature(0.6F).downfall(0.6F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.6F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome jungleBiome() {
      return jungleBiome(0.1F, 0.2F, 40, 2, 3);
   }

   public static Biome jungleEdgeBiome() {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.baseJungleSpawns(mobspawninfo$builder);
      return baseJungleBiome(0.1F, 0.2F, 0.8F, false, true, false, mobspawninfo$builder);
   }

   public static Biome modifiedJungleEdgeBiome() {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.baseJungleSpawns(mobspawninfo$builder);
      return baseJungleBiome(0.2F, 0.4F, 0.8F, false, true, true, mobspawninfo$builder);
   }

   public static Biome modifiedJungleBiome() {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.baseJungleSpawns(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.PARROT, 10, 1, 1)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.OCELOT, 2, 1, 1));
      return baseJungleBiome(0.2F, 0.4F, 0.9F, false, false, true, mobspawninfo$builder);
   }

   public static Biome jungleHillsBiome() {
      return jungleBiome(0.45F, 0.3F, 10, 1, 1);
   }

   public static Biome bambooJungleBiome() {
      return bambooJungleBiome(0.1F, 0.2F, 40, 2);
   }

   public static Biome bambooJungleHillsBiome() {
      return bambooJungleBiome(0.45F, 0.3F, 10, 1);
   }

   private static Biome jungleBiome(float pDepth, float pScale, int pParrotWeight, int pParrotMaxCount, int pOcelotMaxCount) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.baseJungleSpawns(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.PARROT, pParrotWeight, 1, pParrotMaxCount)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.OCELOT, 2, 1, pOcelotMaxCount)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.PANDA, 1, 1, 2));
      mobspawninfo$builder.setPlayerCanSpawn();
      return baseJungleBiome(pDepth, pScale, 0.9F, false, false, false, mobspawninfo$builder);
   }

   private static Biome bambooJungleBiome(float pDepth, float pScale, int pParrotWeight, int pParrotMaxCount) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.baseJungleSpawns(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.PARROT, pParrotWeight, 1, pParrotMaxCount)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.PANDA, 80, 1, 2)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.OCELOT, 2, 1, 1));
      return baseJungleBiome(pDepth, pScale, 0.9F, true, false, false, mobspawninfo$builder);
   }

   private static Biome baseJungleBiome(float pDepth, float pScale, float pDownfall, boolean pHasOnlyBambooVegetation, boolean pIsEdgeBiome, boolean pIsModified, MobSpawnInfo.Builder pMobSpawnBuilder) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
      if (!pIsEdgeBiome && !pIsModified) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.JUNGLE_TEMPLE);
      }

      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_JUNGLE);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pHasOnlyBambooVegetation) {
         DefaultBiomeFeatures.addBambooVegetation(biomegenerationsettings$builder);
      } else {
         if (!pIsEdgeBiome && !pIsModified) {
            DefaultBiomeFeatures.addLightBambooVegetation(biomegenerationsettings$builder);
         }

         if (pIsEdgeBiome) {
            DefaultBiomeFeatures.addJungleEdgeTrees(biomegenerationsettings$builder);
         } else {
            DefaultBiomeFeatures.addJungleTrees(biomegenerationsettings$builder);
         }
      }

      DefaultBiomeFeatures.addWarmFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addJungleGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addJungleExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.JUNGLE).depth(pDepth).scale(pScale).temperature(0.95F).downfall(pDownfall).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.95F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(pMobSpawnBuilder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome mountainBiome(float pDepth, float pScale, ConfiguredSurfaceBuilder<SurfaceBuilderConfig> pSurfaceBuilder, boolean pIsEdgeBiome) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.farmAnimals(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.LLAMA, 5, 4, 6));
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pSurfaceBuilder);
      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsEdgeBiome) {
         DefaultBiomeFeatures.addMountainEdgeTrees(biomegenerationsettings$builder);
      } else {
         DefaultBiomeFeatures.addMountainTrees(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addInfestedStone(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.EXTREME_HILLS).depth(pDepth).scale(pScale).temperature(0.2F).downfall(0.3F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.2F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome desertBiome(float pDepth, float pScale, boolean pHasVillageAndOutpost, boolean pHasDesertPyramid, boolean pHasFossils) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.desertSpawns(mobspawninfo$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.DESERT);
      if (pHasVillageAndOutpost) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_DESERT);
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      if (pHasDesertPyramid) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.DESERT_PYRAMID);
      }

      if (pHasFossils) {
         DefaultBiomeFeatures.addFossilDecoration(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_DESERT);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDesertLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDesertVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDesertExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDesertExtraDecoration(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.DESERT).depth(pDepth).scale(pScale).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome plainsBiome(boolean pIsSunflowerVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.plainsSpawns(mobspawninfo$builder);
      if (!pIsSunflowerVariant) {
         mobspawninfo$builder.setPlayerCanSpawn();
      }

      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
      if (!pIsSunflowerVariant) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_PLAINS).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addPlainGrass(biomegenerationsettings$builder);
      if (pIsSunflowerVariant) {
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_SUNFLOWER);
      }

      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addPlainVegetation(biomegenerationsettings$builder);
      if (pIsSunflowerVariant) {
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
      }

      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      if (pIsSunflowerVariant) {
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
      } else {
         DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.PLAINS).depth(0.125F).scale(0.05F).temperature(0.8F).downfall(0.4F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static Biome baseEndBiome(BiomeGenerationSettings.Builder pGenerationSettingsBuilder) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.endSpawns(mobspawninfo$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.THEEND).depth(0.1F).scale(0.2F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(10518688).skyColor(0).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(pGenerationSettingsBuilder.build()).build();
   }

   public static Biome endBarrensBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.END);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome theEndBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.END).addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome endMidlandsBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.END).addStructureStart(StructureFeatures.END_CITY);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome endHighlandsBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.END).addStructureStart(StructureFeatures.END_CITY).addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome smallEndIslandsBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.END).addFeature(GenerationStage.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome mushroomFieldsBiome(float pDepth, float pScale) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.mooshroomSpawns(mobspawninfo$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.MYCELIUM);
      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addMushroomFieldVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.MUSHROOM).depth(pDepth).scale(pScale).temperature(0.9F).downfall(1.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.9F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static Biome baseSavannaBiome(float pDepth, float pScale, float pTemperature, boolean pIsHighland, boolean pIsShatteredSavanna, MobSpawnInfo.Builder pMobSpawnBuilder) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pIsShatteredSavanna ? ConfiguredSurfaceBuilders.SHATTERED_SAVANNA : ConfiguredSurfaceBuilders.GRASS);
      if (!pIsHighland && !pIsShatteredSavanna) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_SAVANNA).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(pIsHighland ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      if (!pIsShatteredSavanna) {
         DefaultBiomeFeatures.addSavannaGrass(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsShatteredSavanna) {
         DefaultBiomeFeatures.addShatteredSavannaTrees(biomegenerationsettings$builder);
         DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
         DefaultBiomeFeatures.addShatteredSavannaGrass(biomegenerationsettings$builder);
      } else {
         DefaultBiomeFeatures.addSavannaTrees(biomegenerationsettings$builder);
         DefaultBiomeFeatures.addWarmFlowers(biomegenerationsettings$builder);
         DefaultBiomeFeatures.addSavannaExtraGrass(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.SAVANNA).depth(pDepth).scale(pScale).temperature(pTemperature).downfall(0.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(pMobSpawnBuilder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome savannaBiome(float pDepth, float pScale, float pTemperature, boolean pIsHighland, boolean pIsShatteredSavanna) {
      MobSpawnInfo.Builder mobspawninfo$builder = savannaMobs();
      return baseSavannaBiome(pDepth, pScale, pTemperature, pIsHighland, pIsShatteredSavanna, mobspawninfo$builder);
   }

   private static MobSpawnInfo.Builder savannaMobs() {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.farmAnimals(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.HORSE, 1, 2, 6)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.DONKEY, 1, 1, 1));
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      return mobspawninfo$builder;
   }

   public static Biome savanaPlateauBiome() {
      MobSpawnInfo.Builder mobspawninfo$builder = savannaMobs();
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.LLAMA, 8, 4, 4));
      return baseSavannaBiome(1.5F, 0.025F, 1.0F, true, false, mobspawninfo$builder);
   }

   private static Biome baseBadlandsBiome(ConfiguredSurfaceBuilder<SurfaceBuilderConfig> pSurfaceBuilder, float pDepth, float pScale, boolean pIsHighland, boolean pHasOakTrees) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pSurfaceBuilder);
      DefaultBiomeFeatures.addDefaultOverworldLandMesaStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(pIsHighland ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addExtraGold(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pHasOakTrees) {
         DefaultBiomeFeatures.addBadlandsTrees(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addBadlandGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addBadlandExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.MESA).depth(pDepth).scale(pScale).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).foliageColorOverride(10387789).grassColorOverride(9470285).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome badlandsBiome(float pDepth, float pScale, boolean pIsHighland) {
      return baseBadlandsBiome(ConfiguredSurfaceBuilders.BADLANDS, pDepth, pScale, pIsHighland, false);
   }

   public static Biome woodedBadlandsPlateauBiome(float pDepth, float pScale) {
      return baseBadlandsBiome(ConfiguredSurfaceBuilders.WOODED_BADLANDS, pDepth, pScale, true, true);
   }

   public static Biome erodedBadlandsBiome() {
      return baseBadlandsBiome(ConfiguredSurfaceBuilders.ERODED_BADLANDS, 0.1F, 0.2F, true, false);
   }

   private static Biome baseOceanBiome(MobSpawnInfo.Builder pMobSpawnBuilder, int pWaterColor, int pWaterFogColor, boolean pIsDeepVariant, BiomeGenerationSettings.Builder pGenerationSettingsBuilder) {
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.OCEAN).depth(pIsDeepVariant ? -1.8F : -1.0F).scale(0.1F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeAmbience.Builder()).waterColor(pWaterColor).waterFogColor(pWaterFogColor).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(pMobSpawnBuilder.build()).generationSettings(pGenerationSettingsBuilder.build()).build();
   }

   private static BiomeGenerationSettings.Builder baseOceanGeneration(ConfiguredSurfaceBuilder<SurfaceBuilderConfig> pSurfaceBuilder, boolean pHasOceanMonument, boolean pIsWarmOcean, boolean pIsDeepVariant) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pSurfaceBuilder);
      StructureFeature<?, ?> structurefeature = pIsWarmOcean ? StructureFeatures.OCEAN_RUIN_WARM : StructureFeatures.OCEAN_RUIN_COLD;
      if (pIsDeepVariant) {
         if (pHasOceanMonument) {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
         }

         DefaultBiomeFeatures.addDefaultOverworldOceanStructures(biomegenerationsettings$builder);
         biomegenerationsettings$builder.addStructureStart(structurefeature);
      } else {
         biomegenerationsettings$builder.addStructureStart(structurefeature);
         if (pHasOceanMonument) {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
         }

         DefaultBiomeFeatures.addDefaultOverworldOceanStructures(biomegenerationsettings$builder);
      }

      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
      DefaultBiomeFeatures.addOceanCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addWaterTrees(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      return biomegenerationsettings$builder;
   }

   public static Biome coldOceanBiome(boolean pIsDeepVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.oceanSpawns(mobspawninfo$builder, 3, 4, 15);
      mobspawninfo$builder.addSpawn(EntityClassification.WATER_AMBIENT, new MobSpawnInfo.Spawners(EntityType.SALMON, 15, 1, 5));
      boolean flag = !pIsDeepVariant;
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(ConfiguredSurfaceBuilders.GRASS, pIsDeepVariant, false, flag);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, pIsDeepVariant ? Features.SEAGRASS_DEEP_COLD : Features.SEAGRASS_COLD);
      DefaultBiomeFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawninfo$builder, 4020182, 329011, pIsDeepVariant, biomegenerationsettings$builder);
   }

   public static Biome oceanBiome(boolean pIsDeepVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.oceanSpawns(mobspawninfo$builder, 1, 4, 10);
      mobspawninfo$builder.addSpawn(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(EntityType.DOLPHIN, 1, 1, 2));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(ConfiguredSurfaceBuilders.GRASS, pIsDeepVariant, false, true);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, pIsDeepVariant ? Features.SEAGRASS_DEEP : Features.SEAGRASS_NORMAL);
      DefaultBiomeFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawninfo$builder, 4159204, 329011, pIsDeepVariant, biomegenerationsettings$builder);
   }

   public static Biome lukeWarmOceanBiome(boolean pIsDeepVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      if (pIsDeepVariant) {
         DefaultBiomeFeatures.oceanSpawns(mobspawninfo$builder, 8, 4, 8);
      } else {
         DefaultBiomeFeatures.oceanSpawns(mobspawninfo$builder, 10, 2, 15);
      }

      mobspawninfo$builder.addSpawn(EntityClassification.WATER_AMBIENT, new MobSpawnInfo.Spawners(EntityType.PUFFERFISH, 5, 1, 3)).addSpawn(EntityClassification.WATER_AMBIENT, new MobSpawnInfo.Spawners(EntityType.TROPICAL_FISH, 25, 8, 8)).addSpawn(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(EntityType.DOLPHIN, 2, 1, 2));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(ConfiguredSurfaceBuilders.OCEAN_SAND, pIsDeepVariant, true, false);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, pIsDeepVariant ? Features.SEAGRASS_DEEP_WARM : Features.SEAGRASS_WARM);
      if (pIsDeepVariant) {
         DefaultBiomeFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addLukeWarmKelp(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawninfo$builder, 4566514, 267827, pIsDeepVariant, biomegenerationsettings$builder);
   }

   public static Biome warmOceanBiome() {
      MobSpawnInfo.Builder mobspawninfo$builder = (new MobSpawnInfo.Builder()).addSpawn(EntityClassification.WATER_AMBIENT, new MobSpawnInfo.Spawners(EntityType.PUFFERFISH, 15, 1, 3));
      DefaultBiomeFeatures.warmOceanSpawns(mobspawninfo$builder, 10, 4);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(ConfiguredSurfaceBuilders.FULL_SAND, false, true, false).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.WARM_OCEAN_VEGETATION).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_WARM).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SEA_PICKLE);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawninfo$builder, 4445678, 270131, false, biomegenerationsettings$builder);
   }

   public static Biome deepWarmOceanBiome() {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.warmOceanSpawns(mobspawninfo$builder, 5, 1);
      mobspawninfo$builder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.DROWNED, 5, 1, 1));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(ConfiguredSurfaceBuilders.FULL_SAND, true, true, false).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
      DefaultBiomeFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawninfo$builder, 4445678, 270131, true, biomegenerationsettings$builder);
   }

   public static Biome frozenOceanBiome(boolean pIsDeepVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = (new MobSpawnInfo.Builder()).addSpawn(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(EntityType.SQUID, 1, 1, 4)).addSpawn(EntityClassification.WATER_AMBIENT, new MobSpawnInfo.Spawners(EntityType.SALMON, 15, 1, 5)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.POLAR_BEAR, 1, 1, 2));
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.DROWNED, 5, 1, 1));
      float f = pIsDeepVariant ? 0.5F : 0.0F;
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.FROZEN_OCEAN);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_RUIN_COLD);
      if (pIsDeepVariant) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
      }

      DefaultBiomeFeatures.addDefaultOverworldOceanStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
      DefaultBiomeFeatures.addOceanCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addIcebergs(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addBlueIce(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addWaterTrees(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(pIsDeepVariant ? Biome.RainType.RAIN : Biome.RainType.SNOW).biomeCategory(Biome.Category.OCEAN).depth(pIsDeepVariant ? -1.8F : -1.0F).scale(0.1F).temperature(f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).downfall(0.5F).specialEffects((new BiomeAmbience.Builder()).waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static Biome baseForestBiome(float pDepth, float pScale, boolean pIsFlowerForestVariant, MobSpawnInfo.Builder pMobSpawnBuilder) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      if (pIsFlowerForestVariant) {
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
      } else {
         DefaultBiomeFeatures.addForestFlowers(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsFlowerForestVariant) {
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_TREES);
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FLOWER_FOREST);
         DefaultBiomeFeatures.addDefaultGrass(biomegenerationsettings$builder);
      } else {
         DefaultBiomeFeatures.addOtherBirchTrees(biomegenerationsettings$builder);
         DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
         DefaultBiomeFeatures.addForestGrass(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.FOREST).depth(pDepth).scale(pScale).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(pMobSpawnBuilder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static MobSpawnInfo.Builder defaultSpawns() {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.farmAnimals(mobspawninfo$builder);
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      return mobspawninfo$builder;
   }

   public static Biome forestBiome(float pDepth, float pScale) {
      MobSpawnInfo.Builder mobspawninfo$builder = defaultSpawns().addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.WOLF, 5, 4, 4)).setPlayerCanSpawn();
      return baseForestBiome(pDepth, pScale, false, mobspawninfo$builder);
   }

   public static Biome flowerForestBiome() {
      MobSpawnInfo.Builder mobspawninfo$builder = defaultSpawns().addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.RABBIT, 4, 2, 3));
      return baseForestBiome(0.1F, 0.4F, true, mobspawninfo$builder);
   }

   public static Biome taigaBiome(float pDepth, float pScale, boolean pIsSnowyVariant, boolean pIsMountainVariant, boolean pHasVillageAndOutpost, boolean pHasIgloos) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.farmAnimals(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.WOLF, 8, 4, 4)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.RABBIT, 4, 2, 3)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.FOX, 8, 2, 4));
      if (!pIsSnowyVariant && !pIsMountainVariant) {
         mobspawninfo$builder.setPlayerCanSpawn();
      }

      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      float f = pIsSnowyVariant ? -0.5F : 0.25F;
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
      if (pHasVillageAndOutpost) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_TAIGA);
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      if (pHasIgloos) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.IGLOO);
      }

      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(pIsMountainVariant ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addFerns(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addTaigaTrees(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addTaigaGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      if (pIsSnowyVariant) {
         DefaultBiomeFeatures.addBerryBushes(biomegenerationsettings$builder);
      } else {
         DefaultBiomeFeatures.addSparseBerryBushes(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(pIsSnowyVariant ? Biome.RainType.SNOW : Biome.RainType.RAIN).biomeCategory(Biome.Category.TAIGA).depth(pDepth).scale(pScale).temperature(f).downfall(pIsSnowyVariant ? 0.4F : 0.8F).specialEffects((new BiomeAmbience.Builder()).waterColor(pIsSnowyVariant ? 4020182 : 4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome darkForestBiome(float pDepth, float pScale, boolean pIsHillsVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.farmAnimals(mobspawninfo$builder);
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.WOODLAND_MANSION);
      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, pIsHillsVariant ? Features.DARK_FOREST_VEGETATION_RED : Features.DARK_FOREST_VEGETATION_BROWN);
      DefaultBiomeFeatures.addForestFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addForestGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.FOREST).depth(pDepth).scale(pScale).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).grassColorModifier(BiomeAmbience.GrassColorModifier.DARK_FOREST).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome swampBiome(float pDepth, float pScale, boolean pIsHillsVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      DefaultBiomeFeatures.farmAnimals(mobspawninfo$builder);
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SLIME, 1, 1, 1));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.SWAMP);
      if (!pIsHillsVariant) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.SWAMP_HUT);
      }

      biomegenerationsettings$builder.addStructureStart(StructureFeatures.MINESHAFT);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      if (!pIsHillsVariant) {
         DefaultBiomeFeatures.addFossilDecoration(biomegenerationsettings$builder);
      }

      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSwampClayDisk(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSwampVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSwampExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      if (pIsHillsVariant) {
         DefaultBiomeFeatures.addFossilDecoration(biomegenerationsettings$builder);
      } else {
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SWAMP);
      }

      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.RAIN).biomeCategory(Biome.Category.SWAMP).depth(pDepth).scale(pScale).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeAmbience.Builder()).waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(6975545).grassColorModifier(BiomeAmbience.GrassColorModifier.SWAMP).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome tundraBiome(float pDepth, float pScale, boolean pIsIceSpikesBiome, boolean pIsMountainVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = (new MobSpawnInfo.Builder()).creatureGenerationProbability(0.07F);
      DefaultBiomeFeatures.snowySpawns(mobspawninfo$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pIsIceSpikesBiome ? ConfiguredSurfaceBuilders.ICE_SPIKES : ConfiguredSurfaceBuilders.GRASS);
      if (!pIsIceSpikesBiome && !pIsMountainVariant) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_SNOWY).addStructureStart(StructureFeatures.IGLOO);
      }

      DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      if (!pIsIceSpikesBiome && !pIsMountainVariant) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      biomegenerationsettings$builder.addStructureStart(pIsMountainVariant ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      if (pIsIceSpikesBiome) {
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.ICE_SPIKE);
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.ICE_PATCH);
      }

      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSnowyTrees(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.SNOW).biomeCategory(Biome.Category.ICY).depth(pDepth).scale(pScale).temperature(0.0F).downfall(0.5F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.0F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome riverBiome(float pDepth, float pScale, float pTemperature, int pWaterColor, boolean pIsSnowy) {
      MobSpawnInfo.Builder mobspawninfo$builder = (new MobSpawnInfo.Builder()).addSpawn(EntityClassification.WATER_CREATURE, new MobSpawnInfo.Spawners(EntityType.SQUID, 2, 1, 4)).addSpawn(EntityClassification.WATER_AMBIENT, new MobSpawnInfo.Spawners(EntityType.SALMON, 5, 1, 5));
      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      mobspawninfo$builder.addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.DROWNED, pIsSnowy ? 1 : 100, 1, 1));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.MINESHAFT);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addWaterTrees(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      if (!pIsSnowy) {
         biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_RIVER);
      }

      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(pIsSnowy ? Biome.RainType.SNOW : Biome.RainType.RAIN).biomeCategory(Biome.Category.RIVER).depth(pDepth).scale(pScale).temperature(pTemperature).downfall(0.5F).specialEffects((new BiomeAmbience.Builder()).waterColor(pWaterColor).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome beachBiome(float pDepth, float pScale, float pTemperature, float pDownfall, int pWaterColor, boolean pIsColdBiome, boolean pIsStoneVariant) {
      MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
      if (!pIsStoneVariant && !pIsColdBiome) {
         mobspawninfo$builder.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.TURTLE, 5, 2, 5));
      }

      DefaultBiomeFeatures.commonSpawns(mobspawninfo$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pIsStoneVariant ? ConfiguredSurfaceBuilders.STONE : ConfiguredSurfaceBuilders.DESERT);
      if (pIsStoneVariant) {
         DefaultBiomeFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      } else {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.MINESHAFT);
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.BURIED_TREASURE);
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
      }

      biomegenerationsettings$builder.addStructureStart(pIsStoneVariant ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      DefaultBiomeFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultLakes(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultOres(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultGrass(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addDefaultSprings(biomegenerationsettings$builder);
      DefaultBiomeFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(pIsColdBiome ? Biome.RainType.SNOW : Biome.RainType.RAIN).biomeCategory(pIsStoneVariant ? Biome.Category.NONE : Biome.Category.BEACH).depth(pDepth).scale(pScale).temperature(pTemperature).downfall(pDownfall).specialEffects((new BiomeAmbience.Builder()).waterColor(pWaterColor).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawninfo$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome theVoidBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.NOPE);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, Features.VOID_START_PLATFORM);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.NONE).depth(0.1F).scale(0.2F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(MobSpawnInfo.EMPTY).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome netherWastesBiome() {
      MobSpawnInfo mobspawninfo = (new MobSpawnInfo.Builder()).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.GHAST, 50, 4, 4)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.MAGMA_CUBE, 2, 4, 4)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.PIGLIN, 15, 4, 4)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.NETHER).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.NETHER_CAVE).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
      DefaultBiomeFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(3344392).skyColor(calculateSkyColor(2.0F)).ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP).ambientMoodSound(new MoodSoundAmbience(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new SoundAdditionsAmbience(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111D)).backgroundMusic(BackgroundMusicTracks.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES)).build()).mobSpawnSettings(mobspawninfo).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome soulSandValleyBiome() {
      double d0 = 0.7D;
      double d1 = 0.15D;
      MobSpawnInfo mobspawninfo = (new MobSpawnInfo.Builder()).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SKELETON, 20, 5, 5)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.GHAST, 50, 4, 4)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.STRIDER, 60, 1, 2)).addMobCharge(EntityType.SKELETON, 0.7D, 0.15D).addMobCharge(EntityType.GHAST, 0.7D, 0.15D).addMobCharge(EntityType.ENDERMAN, 0.7D, 0.15D).addMobCharge(EntityType.STRIDER, 0.7D, 0.15D).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.SOUL_SAND_VALLEY).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.NETHER_FOSSIL).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addStructureStart(StructureFeatures.BASTION_REMNANT).addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.NETHER_CAVE).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA).addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, Features.BASALT_PILLAR).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_CRIMSON_ROOTS).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_SOUL_SAND);
      DefaultBiomeFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(1787717).skyColor(calculateSkyColor(2.0F)).ambientParticle(new ParticleEffectAmbience(ParticleTypes.ASH, 0.00625F)).ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP).ambientMoodSound(new MoodSoundAmbience(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new SoundAdditionsAmbience(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111D)).backgroundMusic(BackgroundMusicTracks.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY)).build()).mobSpawnSettings(mobspawninfo).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome basaltDeltasBiome() {
      MobSpawnInfo mobspawninfo = (new MobSpawnInfo.Builder()).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.GHAST, 40, 1, 1)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.MAGMA_CUBE, 100, 2, 5)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.BASALT_DELTAS).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.NETHER_CAVE).addStructureStart(StructureFeatures.NETHER_BRIDGE).addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.DELTA).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA_DOUBLE).addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.SMALL_BASALT_COLUMNS).addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.LARGE_BASALT_COLUMNS).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.BASALT_BLOBS).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.BLACKSTONE_BLOBS).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_DELTA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED_DOUBLE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_DELTAS).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_DELTAS);
      DefaultBiomeFeatures.addAncientDebris(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(4341314).fogColor(6840176).skyColor(calculateSkyColor(2.0F)).ambientParticle(new ParticleEffectAmbience(ParticleTypes.WHITE_ASH, 0.118093334F)).ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP).ambientMoodSound(new MoodSoundAmbience(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new SoundAdditionsAmbience(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111D)).backgroundMusic(BackgroundMusicTracks.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS)).build()).mobSpawnSettings(mobspawninfo).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome crimsonForestBiome() {
      MobSpawnInfo mobspawninfo = (new MobSpawnInfo.Builder()).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.HOGLIN, 9, 3, 4)).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.PIGLIN, 5, 3, 4)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.CRIMSON_FOREST).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.NETHER_CAVE).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.WEEPING_VINES).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FUNGI).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FOREST_VEGETATION);
      DefaultBiomeFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(3343107).skyColor(calculateSkyColor(2.0F)).ambientParticle(new ParticleEffectAmbience(ParticleTypes.CRIMSON_SPORE, 0.025F)).ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP).ambientMoodSound(new MoodSoundAmbience(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new SoundAdditionsAmbience(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111D)).backgroundMusic(BackgroundMusicTracks.createGameMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST)).build()).mobSpawnSettings(mobspawninfo).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome warpedForestBiome() {
      MobSpawnInfo mobspawninfo = (new MobSpawnInfo.Builder()).addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.STRIDER, 60, 1, 2)).addMobCharge(EntityType.ENDERMAN, 1.0D, 0.12D).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(ConfiguredSurfaceBuilders.WARPED_FOREST).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.NETHER_CAVE).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      DefaultBiomeFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.WARPED_FUNGI).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.WARPED_FOREST_VEGETATION).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.NETHER_SPROUTS).addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.TWISTING_VINES);
      DefaultBiomeFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
      return (new Biome.Builder()).precipitation(Biome.RainType.NONE).biomeCategory(Biome.Category.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeAmbience.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(1705242).skyColor(calculateSkyColor(2.0F)).ambientParticle(new ParticleEffectAmbience(ParticleTypes.WARPED_SPORE, 0.01428F)).ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP).ambientMoodSound(new MoodSoundAmbience(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new SoundAdditionsAmbience(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111D)).backgroundMusic(BackgroundMusicTracks.createGameMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST)).build()).mobSpawnSettings(mobspawninfo).generationSettings(biomegenerationsettings$builder.build()).build();
   }
}