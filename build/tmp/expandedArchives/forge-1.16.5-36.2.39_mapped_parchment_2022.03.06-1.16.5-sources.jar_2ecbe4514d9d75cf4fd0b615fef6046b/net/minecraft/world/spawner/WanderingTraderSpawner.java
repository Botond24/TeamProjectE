package net.minecraft.world.spawner;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.passive.horse.TraderLlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;

public class WanderingTraderSpawner implements ISpecialSpawner {
   private final Random random = new Random();
   private final IServerWorldInfo serverLevelData;
   private int tickDelay;
   private int spawnDelay;
   private int spawnChance;

   public WanderingTraderSpawner(IServerWorldInfo pServerLevelData) {
      this.serverLevelData = pServerLevelData;
      this.tickDelay = 1200;
      this.spawnDelay = pServerLevelData.getWanderingTraderSpawnDelay();
      this.spawnChance = pServerLevelData.getWanderingTraderSpawnChance();
      if (this.spawnDelay == 0 && this.spawnChance == 0) {
         this.spawnDelay = 24000;
         pServerLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
         this.spawnChance = 25;
         pServerLevelData.setWanderingTraderSpawnChance(this.spawnChance);
      }

   }

   public int tick(ServerWorld pLevel, boolean pSpawnHostiles, boolean pSpawnPassives) {
      if (!pLevel.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) {
         return 0;
      } else if (--this.tickDelay > 0) {
         return 0;
      } else {
         this.tickDelay = 1200;
         this.spawnDelay -= 1200;
         this.serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
         if (this.spawnDelay > 0) {
            return 0;
         } else {
            this.spawnDelay = 24000;
            if (!pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
               return 0;
            } else {
               int i = this.spawnChance;
               this.spawnChance = MathHelper.clamp(this.spawnChance + 25, 25, 75);
               this.serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
               if (this.random.nextInt(100) > i) {
                  return 0;
               } else if (this.spawn(pLevel)) {
                  this.spawnChance = 25;
                  return 1;
               } else {
                  return 0;
               }
            }
         }
      }
   }

   private boolean spawn(ServerWorld pServerLevel) {
      PlayerEntity playerentity = pServerLevel.getRandomPlayer();
      if (playerentity == null) {
         return true;
      } else if (this.random.nextInt(10) != 0) {
         return false;
      } else {
         BlockPos blockpos = playerentity.blockPosition();
         int i = 48;
         PointOfInterestManager pointofinterestmanager = pServerLevel.getPoiManager();
         Optional<BlockPos> optional = pointofinterestmanager.find(PointOfInterestType.MEETING.getPredicate(), (p_221241_0_) -> {
            return true;
         }, blockpos, 48, PointOfInterestManager.Status.ANY);
         BlockPos blockpos1 = optional.orElse(blockpos);
         BlockPos blockpos2 = this.findSpawnPositionNear(pServerLevel, blockpos1, 48);
         if (blockpos2 != null && this.hasEnoughSpace(pServerLevel, blockpos2)) {
            if (pServerLevel.getBiomeName(blockpos2).equals(Optional.of(Biomes.THE_VOID))) {
               return false;
            }

            WanderingTraderEntity wanderingtraderentity = EntityType.WANDERING_TRADER.spawn(pServerLevel, (CompoundNBT)null, (ITextComponent)null, (PlayerEntity)null, blockpos2, SpawnReason.EVENT, false, false);
            if (wanderingtraderentity != null) {
               for(int j = 0; j < 2; ++j) {
                  this.tryToSpawnLlamaFor(pServerLevel, wanderingtraderentity, 4);
               }

               this.serverLevelData.setWanderingTraderId(wanderingtraderentity.getUUID());
               wanderingtraderentity.setDespawnDelay(48000);
               wanderingtraderentity.setWanderTarget(blockpos1);
               wanderingtraderentity.restrictTo(blockpos1, 16);
               return true;
            }
         }

         return false;
      }
   }

   private void tryToSpawnLlamaFor(ServerWorld pServerLevel, WanderingTraderEntity pTrader, int pMaxDistance) {
      BlockPos blockpos = this.findSpawnPositionNear(pServerLevel, pTrader.blockPosition(), pMaxDistance);
      if (blockpos != null) {
         TraderLlamaEntity traderllamaentity = EntityType.TRADER_LLAMA.spawn(pServerLevel, (CompoundNBT)null, (ITextComponent)null, (PlayerEntity)null, blockpos, SpawnReason.EVENT, false, false);
         if (traderllamaentity != null) {
            traderllamaentity.setLeashedTo(pTrader, true);
         }
      }
   }

   @Nullable
   private BlockPos findSpawnPositionNear(IWorldReader pLevel, BlockPos pPos, int pMaxDistance) {
      BlockPos blockpos = null;

      for(int i = 0; i < 10; ++i) {
         int j = pPos.getX() + this.random.nextInt(pMaxDistance * 2) - pMaxDistance;
         int k = pPos.getZ() + this.random.nextInt(pMaxDistance * 2) - pMaxDistance;
         int l = pLevel.getHeight(Heightmap.Type.WORLD_SURFACE, j, k);
         BlockPos blockpos1 = new BlockPos(j, l, k);
         if (WorldEntitySpawner.isSpawnPositionOk(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, pLevel, blockpos1, EntityType.WANDERING_TRADER)) {
            blockpos = blockpos1;
            break;
         }
      }

      return blockpos;
   }

   private boolean hasEnoughSpace(IBlockReader pLevel, BlockPos pPos) {
      for(BlockPos blockpos : BlockPos.betweenClosed(pPos, pPos.offset(1, 2, 1))) {
         if (!pLevel.getBlockState(blockpos).getCollisionShape(pLevel, blockpos).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}