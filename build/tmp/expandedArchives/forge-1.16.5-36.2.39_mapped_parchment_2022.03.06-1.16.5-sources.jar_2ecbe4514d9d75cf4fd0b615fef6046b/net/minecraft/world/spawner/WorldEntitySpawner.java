package net.minecraft.world.spawner;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeMagnifier;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WorldEntitySpawner {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int MAGIC_NUMBER = (int)Math.pow(17.0D, 2.0D);
   private static final EntityClassification[] SPAWNING_CATEGORIES = Stream.of(EntityClassification.values()).filter((p_234965_0_) -> {
      return p_234965_0_ != EntityClassification.MISC;
   }).toArray((p_234963_0_) -> {
      return new EntityClassification[p_234963_0_];
   });

   public static WorldEntitySpawner.EntityDensityManager createState(int pSpawnableChunkCount, Iterable<Entity> pEntities, WorldEntitySpawner.IInitialDensityAdder pChunkGetter) {
      MobDensityTracker mobdensitytracker = new MobDensityTracker();
      Object2IntOpenHashMap<EntityClassification> object2intopenhashmap = new Object2IntOpenHashMap<>();
      Iterator iterator = pEntities.iterator();

      while(true) {
         Entity entity;
         MobEntity mobentity;
         do {
            if (!iterator.hasNext()) {
               return new WorldEntitySpawner.EntityDensityManager(pSpawnableChunkCount, object2intopenhashmap, mobdensitytracker);
            }

            entity = (Entity)iterator.next();
            if (!(entity instanceof MobEntity)) {
               break;
            }

            mobentity = (MobEntity)entity;
         } while(mobentity.isPersistenceRequired() || mobentity.requiresCustomPersistence());

         final Entity entity_f = entity;
         EntityClassification entityclassification = entity.getClassification(true);
         if (entityclassification != EntityClassification.MISC) {
            BlockPos blockpos = entity.blockPosition();
            long i = ChunkPos.asLong(blockpos.getX() >> 4, blockpos.getZ() >> 4);
            pChunkGetter.query(i, (p_234971_5_) -> {
               MobSpawnInfo.SpawnCosts mobspawninfo$spawncosts = getRoughBiome(blockpos, p_234971_5_).getMobSettings().getMobSpawnCost(entity_f.getType());
               if (mobspawninfo$spawncosts != null) {
                  mobdensitytracker.addCharge(entity_f.blockPosition(), mobspawninfo$spawncosts.getCharge());
               }

               object2intopenhashmap.addTo(entityclassification, 1);
            });
         }
      }
   }

   private static Biome getRoughBiome(BlockPos pPos, IChunk pChunk) {
      return DefaultBiomeMagnifier.INSTANCE.getBiome(0L, pPos.getX(), pPos.getY(), pPos.getZ(), pChunk.getBiomes());
   }

   public static void spawnForChunk(ServerWorld pLevel, Chunk pChunk, WorldEntitySpawner.EntityDensityManager pSpawnState, boolean pSpawnFriendlies, boolean pSpawnMonsters, boolean p_234979_5_) {
      pLevel.getProfiler().push("spawner");

      for(EntityClassification entityclassification : SPAWNING_CATEGORIES) {
         if ((pSpawnFriendlies || !entityclassification.isFriendly()) && (pSpawnMonsters || entityclassification.isFriendly()) && (p_234979_5_ || !entityclassification.isPersistent()) && pSpawnState.canSpawnForCategory(entityclassification)) {
            spawnCategoryForChunk(entityclassification, pLevel, pChunk, (p_234969_1_, p_234969_2_, p_234969_3_) -> {
               return pSpawnState.canSpawn(p_234969_1_, p_234969_2_, p_234969_3_);
            }, (p_234970_1_, p_234970_2_) -> {
               pSpawnState.afterSpawn(p_234970_1_, p_234970_2_);
            });
         }
      }

      pLevel.getProfiler().pop();
   }

   public static void spawnCategoryForChunk(EntityClassification pCategory, ServerWorld pLevel, Chunk pChunk, WorldEntitySpawner.IDensityCheck pFilter, WorldEntitySpawner.IOnSpawnDensityAdder pCallback) {
      BlockPos blockpos = getRandomPosWithin(pLevel, pChunk);
      if (blockpos.getY() >= 1) {
         spawnCategoryForPosition(pCategory, pLevel, pChunk, blockpos, pFilter, pCallback);
      }
   }

   public static void spawnCategoryForPosition(EntityClassification pCategory, ServerWorld pLevel, IChunk pChunk, BlockPos pPos, WorldEntitySpawner.IDensityCheck pFilter, WorldEntitySpawner.IOnSpawnDensityAdder pCallback) {
      StructureManager structuremanager = pLevel.structureFeatureManager();
      ChunkGenerator chunkgenerator = pLevel.getChunkSource().getGenerator();
      int i = pPos.getY();
      BlockState blockstate = pChunk.getBlockState(pPos);
      if (!blockstate.isRedstoneConductor(pChunk, pPos)) {
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
         int j = 0;

         for(int k = 0; k < 3; ++k) {
            int l = pPos.getX();
            int i1 = pPos.getZ();
            int j1 = 6;
            MobSpawnInfo.Spawners mobspawninfo$spawners = null;
            ILivingEntityData ilivingentitydata = null;
            int k1 = MathHelper.ceil(pLevel.random.nextFloat() * 4.0F);
            int l1 = 0;

            for(int i2 = 0; i2 < k1; ++i2) {
               l += pLevel.random.nextInt(6) - pLevel.random.nextInt(6);
               i1 += pLevel.random.nextInt(6) - pLevel.random.nextInt(6);
               blockpos$mutable.set(l, i, i1);
               double d0 = (double)l + 0.5D;
               double d1 = (double)i1 + 0.5D;
               PlayerEntity playerentity = pLevel.getNearestPlayer(d0, (double)i, d1, -1.0D, false);
               if (playerentity != null) {
                  double d2 = playerentity.distanceToSqr(d0, (double)i, d1);
                  if (isRightDistanceToPlayerAndSpawnPoint(pLevel, pChunk, blockpos$mutable, d2)) {
                     if (mobspawninfo$spawners == null) {
                        mobspawninfo$spawners = getRandomSpawnMobAt(pLevel, structuremanager, chunkgenerator, pCategory, pLevel.random, blockpos$mutable);
                        if (mobspawninfo$spawners == null) {
                           break;
                        }

                        k1 = mobspawninfo$spawners.minCount + pLevel.random.nextInt(1 + mobspawninfo$spawners.maxCount - mobspawninfo$spawners.minCount);
                     }

                     if (isValidSpawnPostitionForType(pLevel, pCategory, structuremanager, chunkgenerator, mobspawninfo$spawners, blockpos$mutable, d2) && pFilter.test(mobspawninfo$spawners.type, blockpos$mutable, pChunk)) {
                        MobEntity mobentity = getMobForSpawn(pLevel, mobspawninfo$spawners.type);
                        if (mobentity == null) {
                           return;
                        }

                        mobentity.moveTo(d0, (double)i, d1, pLevel.random.nextFloat() * 360.0F, 0.0F);
                        int canSpawn = net.minecraftforge.common.ForgeHooks.canEntitySpawn(mobentity, pLevel, d0, i, d1, null, SpawnReason.NATURAL);
                        if (canSpawn != -1 && (canSpawn == 1 || isValidPositionForMob(pLevel, mobentity, d2))) {
                           if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(mobentity, pLevel, (float)d0, (float)i, (float)d1, null, SpawnReason.NATURAL))
                           ilivingentitydata = mobentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(mobentity.blockPosition()), SpawnReason.NATURAL, ilivingentitydata, (CompoundNBT)null);
                           ++j;
                           ++l1;
                           pLevel.addFreshEntityWithPassengers(mobentity);
                           pCallback.run(mobentity, pChunk);
                           if (j >= net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(mobentity)) {
                              return;
                           }

                           if (mobentity.isMaxGroupSizeReached(l1)) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerWorld pLevel, IChunk pChunk, BlockPos.Mutable pPos, double pDistance) {
      if (pDistance <= 576.0D) {
         return false;
      } else if (pLevel.getSharedSpawnPos().closerThan(new Vector3d((double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D), 24.0D)) {
         return false;
      } else {
         ChunkPos chunkpos = new ChunkPos(pPos);
         return Objects.equals(chunkpos, pChunk.getPos()) || pLevel.getChunkSource().isEntityTickingChunk(chunkpos);
      }
   }

   private static boolean isValidSpawnPostitionForType(ServerWorld pLevel, EntityClassification pCategory, StructureManager pStructureManager, ChunkGenerator pGenerator, MobSpawnInfo.Spawners pData, BlockPos.Mutable pPos, double pDistance) {
      EntityType<?> entitytype = pData.type;
      if (entitytype.getCategory() == EntityClassification.MISC) {
         return false;
      } else if (!entitytype.canSpawnFarFromPlayer() && pDistance > (double)(entitytype.getCategory().getDespawnDistance() * entitytype.getCategory().getDespawnDistance())) {
         return false;
      } else if (entitytype.canSummon() && canSpawnMobAt(pLevel, pStructureManager, pGenerator, pCategory, pData, pPos)) {
         EntitySpawnPlacementRegistry.PlacementType entityspawnplacementregistry$placementtype = EntitySpawnPlacementRegistry.getPlacementType(entitytype);
         if (!isSpawnPositionOk(entityspawnplacementregistry$placementtype, pLevel, pPos, entitytype)) {
            return false;
         } else if (!EntitySpawnPlacementRegistry.checkSpawnRules(entitytype, pLevel, SpawnReason.NATURAL, pPos, pLevel.random)) {
            return false;
         } else {
            return pLevel.noCollision(entitytype.getAABB((double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D));
         }
      } else {
         return false;
      }
   }

   @Nullable
   private static MobEntity getMobForSpawn(ServerWorld pLevel, EntityType<?> pEntityType) {
      try {
         Entity entity = pEntityType.create(pLevel);
         if (!(entity instanceof MobEntity)) {
            throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getKey(pEntityType));
         } else {
            return (MobEntity)entity;
         }
      } catch (Exception exception) {
         LOGGER.warn("Failed to create mob", (Throwable)exception);
         return null;
      }
   }

   private static boolean isValidPositionForMob(ServerWorld pLevel, MobEntity pMob, double pDistance) {
      if (pDistance > (double)(pMob.getType().getCategory().getDespawnDistance() * pMob.getType().getCategory().getDespawnDistance()) && pMob.removeWhenFarAway(pDistance)) {
         return false;
      } else {
         return pMob.checkSpawnRules(pLevel, SpawnReason.NATURAL) && pMob.checkSpawnObstruction(pLevel);
      }
   }

   @Nullable
   private static MobSpawnInfo.Spawners getRandomSpawnMobAt(ServerWorld p_234977_0_, StructureManager p_234977_1_, ChunkGenerator p_234977_2_, EntityClassification p_234977_3_, Random p_234977_4_, BlockPos p_234977_5_) {
      Biome biome = p_234977_0_.getBiome(p_234977_5_);
      if (p_234977_3_ == EntityClassification.WATER_AMBIENT && biome.getBiomeCategory() == Biome.Category.RIVER && p_234977_4_.nextFloat() < 0.98F) {
         return null;
      } else {
         List<MobSpawnInfo.Spawners> list = mobsAt(p_234977_0_, p_234977_1_, p_234977_2_, p_234977_3_, p_234977_5_, biome);
         list = net.minecraftforge.event.ForgeEventFactory.getPotentialSpawns(p_234977_0_, p_234977_3_, p_234977_5_, list);
         return list.isEmpty() ? null : WeightedRandom.getRandomItem(p_234977_4_, list);
      }
   }

   private static boolean canSpawnMobAt(ServerWorld pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, EntityClassification pCategory, MobSpawnInfo.Spawners pData, BlockPos pPos) {
      return mobsAt(pLevel, pStructureManager, pGenerator, pCategory, pPos, (Biome)null).contains(pData);
   }

   private static List<MobSpawnInfo.Spawners> mobsAt(ServerWorld p_241463_0_, StructureManager p_241463_1_, ChunkGenerator p_241463_2_, EntityClassification p_241463_3_, BlockPos p_241463_4_, @Nullable Biome p_241463_5_) {
      return p_241463_3_ == EntityClassification.MONSTER && p_241463_0_.getBlockState(p_241463_4_.below()).getBlock() == Blocks.NETHER_BRICKS && p_241463_1_.getStructureAt(p_241463_4_, false, Structure.NETHER_BRIDGE).isValid() ? Structure.NETHER_BRIDGE.getSpecialEnemies() : p_241463_2_.getMobsAt(p_241463_5_ != null ? p_241463_5_ : p_241463_0_.getBiome(p_241463_4_), p_241463_1_, p_241463_3_, p_241463_4_);
   }

   private static BlockPos getRandomPosWithin(World pLevel, Chunk pChunk) {
      ChunkPos chunkpos = pChunk.getPos();
      int i = chunkpos.getMinBlockX() + pLevel.random.nextInt(16);
      int j = chunkpos.getMinBlockZ() + pLevel.random.nextInt(16);
      int k = pChunk.getHeight(Heightmap.Type.WORLD_SURFACE, i, j) + 1;
      int l = pLevel.random.nextInt(k + 1);
      return new BlockPos(i, l, j);
   }

   public static boolean isValidEmptySpawnBlock(IBlockReader pBlock, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, EntityType<?> pEntityType) {
      if (pBlockState.isCollisionShapeFullBlock(pBlock, pPos)) {
         return false;
      } else if (pBlockState.isSignalSource()) {
         return false;
      } else if (!pFluidState.isEmpty()) {
         return false;
      } else if (pBlockState.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
         return false;
      } else {
         return !pEntityType.isBlockDangerous(pBlockState);
      }
   }

   public static boolean isSpawnPositionOk(EntitySpawnPlacementRegistry.PlacementType pPlaceType, IWorldReader pLevel, BlockPos pPos, @Nullable EntityType<?> pEntityType) {
      if (pPlaceType == EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS) {
         return true;
      } else if (pEntityType != null && pLevel.getWorldBorder().isWithinBounds(pPos)) {
         return pPlaceType.canSpawnAt(pLevel, pPos, pEntityType);
      }
      return false;
   }

   public static boolean canSpawnAtBody(EntitySpawnPlacementRegistry.PlacementType pPlaceType, IWorldReader pLevel, BlockPos pPos, @Nullable EntityType<?> pEntityType) {
      {
         BlockState blockstate = pLevel.getBlockState(pPos);
         FluidState fluidstate = pLevel.getFluidState(pPos);
         BlockPos blockpos = pPos.above();
         BlockPos blockpos1 = pPos.below();
         switch(pPlaceType) {
         case IN_WATER:
            return fluidstate.is(FluidTags.WATER) && pLevel.getFluidState(blockpos1).is(FluidTags.WATER) && !pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos);
         case IN_LAVA:
            return fluidstate.is(FluidTags.LAVA);
         case ON_GROUND:
         default:
            BlockState blockstate1 = pLevel.getBlockState(blockpos1);
            if (!blockstate1.canCreatureSpawn(pLevel, blockpos1, pPlaceType, pEntityType)) {
               return false;
            } else {
               return isValidEmptySpawnBlock(pLevel, pPos, blockstate, fluidstate, pEntityType) && isValidEmptySpawnBlock(pLevel, blockpos, pLevel.getBlockState(blockpos), pLevel.getFluidState(blockpos), pEntityType);
            }
         }
      }
   }

   public static void spawnMobsForChunkGeneration(IServerWorld p_77191_0_, Biome p_77191_1_, int p_77191_2_, int p_77191_3_, Random p_77191_4_) {
      MobSpawnInfo mobspawninfo = p_77191_1_.getMobSettings();
      List<MobSpawnInfo.Spawners> list = mobspawninfo.getMobs(EntityClassification.CREATURE);
      if (!list.isEmpty()) {
         int i = p_77191_2_ << 4;
         int j = p_77191_3_ << 4;

         while(p_77191_4_.nextFloat() < mobspawninfo.getCreatureProbability()) {
            MobSpawnInfo.Spawners mobspawninfo$spawners = WeightedRandom.getRandomItem(p_77191_4_, list);
            int k = mobspawninfo$spawners.minCount + p_77191_4_.nextInt(1 + mobspawninfo$spawners.maxCount - mobspawninfo$spawners.minCount);
            ILivingEntityData ilivingentitydata = null;
            int l = i + p_77191_4_.nextInt(16);
            int i1 = j + p_77191_4_.nextInt(16);
            int j1 = l;
            int k1 = i1;

            for(int l1 = 0; l1 < k; ++l1) {
               boolean flag = false;

               for(int i2 = 0; !flag && i2 < 4; ++i2) {
                  BlockPos blockpos = getTopNonCollidingPos(p_77191_0_, mobspawninfo$spawners.type, l, i1);
                  if (mobspawninfo$spawners.type.canSummon() && isSpawnPositionOk(EntitySpawnPlacementRegistry.getPlacementType(mobspawninfo$spawners.type), p_77191_0_, blockpos, mobspawninfo$spawners.type)) {
                     float f = mobspawninfo$spawners.type.getWidth();
                     double d0 = MathHelper.clamp((double)l, (double)i + (double)f, (double)i + 16.0D - (double)f);
                     double d1 = MathHelper.clamp((double)i1, (double)j + (double)f, (double)j + 16.0D - (double)f);
                     if (!p_77191_0_.noCollision(mobspawninfo$spawners.type.getAABB(d0, (double)blockpos.getY(), d1)) || !EntitySpawnPlacementRegistry.checkSpawnRules(mobspawninfo$spawners.type, p_77191_0_, SpawnReason.CHUNK_GENERATION, new BlockPos(d0, (double)blockpos.getY(), d1), p_77191_0_.getRandom())) {
                        continue;
                     }

                     Entity entity;
                     try {
                        entity = mobspawninfo$spawners.type.create(p_77191_0_.getLevel());
                     } catch (Exception exception) {
                        LOGGER.warn("Failed to create mob", (Throwable)exception);
                        continue;
                     }

                     entity.moveTo(d0, (double)blockpos.getY(), d1, p_77191_4_.nextFloat() * 360.0F, 0.0F);
                     if (entity instanceof MobEntity) {
                        MobEntity mobentity = (MobEntity)entity;
                        if (net.minecraftforge.common.ForgeHooks.canEntitySpawn(mobentity, p_77191_0_, d0, blockpos.getY(), d1, null, SpawnReason.CHUNK_GENERATION) == -1) continue;
                        if (mobentity.checkSpawnRules(p_77191_0_, SpawnReason.CHUNK_GENERATION) && mobentity.checkSpawnObstruction(p_77191_0_)) {
                           ilivingentitydata = mobentity.finalizeSpawn(p_77191_0_, p_77191_0_.getCurrentDifficultyAt(mobentity.blockPosition()), SpawnReason.CHUNK_GENERATION, ilivingentitydata, (CompoundNBT)null);
                           p_77191_0_.addFreshEntityWithPassengers(mobentity);
                           flag = true;
                        }
                     }
                  }

                  l += p_77191_4_.nextInt(5) - p_77191_4_.nextInt(5);

                  for(i1 += p_77191_4_.nextInt(5) - p_77191_4_.nextInt(5); l < i || l >= i + 16 || i1 < j || i1 >= j + 16; i1 = k1 + p_77191_4_.nextInt(5) - p_77191_4_.nextInt(5)) {
                     l = j1 + p_77191_4_.nextInt(5) - p_77191_4_.nextInt(5);
                  }
               }
            }
         }

      }
   }

   private static BlockPos getTopNonCollidingPos(IWorldReader pLevel, EntityType<?> pEntityType, int pX, int pZ) {
      int i = pLevel.getHeight(EntitySpawnPlacementRegistry.getHeightmapType(pEntityType), pX, pZ);
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(pX, i, pZ);
      if (pLevel.dimensionType().hasCeiling()) {
         do {
            blockpos$mutable.move(Direction.DOWN);
         } while(!pLevel.getBlockState(blockpos$mutable).isAir());

         do {
            blockpos$mutable.move(Direction.DOWN);
         } while(pLevel.getBlockState(blockpos$mutable).isAir() && blockpos$mutable.getY() > 0);
      }

      if (EntitySpawnPlacementRegistry.getPlacementType(pEntityType) == EntitySpawnPlacementRegistry.PlacementType.ON_GROUND) {
         BlockPos blockpos = blockpos$mutable.below();
         if (pLevel.getBlockState(blockpos).isPathfindable(pLevel, blockpos, PathType.LAND)) {
            return blockpos;
         }
      }

      return blockpos$mutable.immutable();
   }

   public static class EntityDensityManager {
      private final int spawnableChunkCount;
      private final Object2IntOpenHashMap<EntityClassification> mobCategoryCounts;
      private final MobDensityTracker spawnPotential;
      private final Object2IntMap<EntityClassification> unmodifiableMobCategoryCounts;
      @Nullable
      private BlockPos lastCheckedPos;
      @Nullable
      private EntityType<?> lastCheckedType;
      private double lastCharge;

      private EntityDensityManager(int pSpawnableChunkCount, Object2IntOpenHashMap<EntityClassification> pMobCategoryCounts, MobDensityTracker pSpawnPotential) {
         this.spawnableChunkCount = pSpawnableChunkCount;
         this.mobCategoryCounts = pMobCategoryCounts;
         this.spawnPotential = pSpawnPotential;
         this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(pMobCategoryCounts);
      }

      private boolean canSpawn(EntityType<?> pEntityType, BlockPos pPos, IChunk pChunk) {
         this.lastCheckedPos = pPos;
         this.lastCheckedType = pEntityType;
         MobSpawnInfo.SpawnCosts mobspawninfo$spawncosts = WorldEntitySpawner.getRoughBiome(pPos, pChunk).getMobSettings().getMobSpawnCost(pEntityType);
         if (mobspawninfo$spawncosts == null) {
            this.lastCharge = 0.0D;
            return true;
         } else {
            double d0 = mobspawninfo$spawncosts.getCharge();
            this.lastCharge = d0;
            double d1 = this.spawnPotential.getPotentialEnergyChange(pPos, d0);
            return d1 <= mobspawninfo$spawncosts.getEnergyBudget();
         }
      }

      private void afterSpawn(MobEntity pMob, IChunk pChunk) {
         EntityType<?> entitytype = pMob.getType();
         BlockPos blockpos = pMob.blockPosition();
         double d0;
         if (blockpos.equals(this.lastCheckedPos) && entitytype == this.lastCheckedType) {
            d0 = this.lastCharge;
         } else {
            MobSpawnInfo.SpawnCosts mobspawninfo$spawncosts = WorldEntitySpawner.getRoughBiome(blockpos, pChunk).getMobSettings().getMobSpawnCost(entitytype);
            if (mobspawninfo$spawncosts != null) {
               d0 = mobspawninfo$spawncosts.getCharge();
            } else {
               d0 = 0.0D;
            }
         }

         this.spawnPotential.addCharge(blockpos, d0);
         this.mobCategoryCounts.addTo(entitytype.getCategory(), 1);
      }

      @OnlyIn(Dist.CLIENT)
      public int getSpawnableChunkCount() {
         return this.spawnableChunkCount;
      }

      public Object2IntMap<EntityClassification> getMobCategoryCounts() {
         return this.unmodifiableMobCategoryCounts;
      }

      private boolean canSpawnForCategory(EntityClassification pCategory) {
         int i = pCategory.getMaxInstancesPerChunk() * this.spawnableChunkCount / WorldEntitySpawner.MAGIC_NUMBER;
         return this.mobCategoryCounts.getInt(pCategory) < i;
      }
   }

   @FunctionalInterface
   public interface IDensityCheck {
      boolean test(EntityType<?> p_test_1_, BlockPos p_test_2_, IChunk p_test_3_);
   }

   @FunctionalInterface
   public interface IInitialDensityAdder {
      void query(long p_query_1_, Consumer<Chunk> p_query_3_);
   }

   @FunctionalInterface
   public interface IOnSpawnDensityAdder {
      void run(MobEntity p_run_1_, IChunk p_run_2_);
   }
}
