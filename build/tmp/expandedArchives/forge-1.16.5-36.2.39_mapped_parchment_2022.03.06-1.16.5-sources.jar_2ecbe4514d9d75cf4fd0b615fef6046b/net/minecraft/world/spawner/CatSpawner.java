package net.minecraft.world.spawner;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

public class CatSpawner implements ISpecialSpawner {
   private int nextTick;

   public int tick(ServerWorld pLevel, boolean pSpawnHostiles, boolean pSpawnPassives) {
      if (pSpawnPassives && pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
         --this.nextTick;
         if (this.nextTick > 0) {
            return 0;
         } else {
            this.nextTick = 1200;
            PlayerEntity playerentity = pLevel.getRandomPlayer();
            if (playerentity == null) {
               return 0;
            } else {
               Random random = pLevel.random;
               int i = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
               int j = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
               BlockPos blockpos = playerentity.blockPosition().offset(i, 0, j);
               if (!pLevel.hasChunksAt(blockpos.getX() - 10, blockpos.getY() - 10, blockpos.getZ() - 10, blockpos.getX() + 10, blockpos.getY() + 10, blockpos.getZ() + 10)) {
                  return 0;
               } else {
                  if (WorldEntitySpawner.isSpawnPositionOk(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, pLevel, blockpos, EntityType.CAT)) {
                     if (pLevel.isCloseToVillage(blockpos, 2)) {
                        return this.spawnInVillage(pLevel, blockpos);
                     }

                     if (pLevel.structureFeatureManager().getStructureAt(blockpos, true, Structure.SWAMP_HUT).isValid()) {
                        return this.spawnInHut(pLevel, blockpos);
                     }
                  }

                  return 0;
               }
            }
         }
      } else {
         return 0;
      }
   }

   private int spawnInVillage(ServerWorld pServerLevel, BlockPos pPos) {
      int i = 48;
      if (pServerLevel.getPoiManager().getCountInRange(PointOfInterestType.HOME.getPredicate(), pPos, 48, PointOfInterestManager.Status.IS_OCCUPIED) > 4L) {
         List<CatEntity> list = pServerLevel.getEntitiesOfClass(CatEntity.class, (new AxisAlignedBB(pPos)).inflate(48.0D, 8.0D, 48.0D));
         if (list.size() < 5) {
            return this.spawnCat(pPos, pServerLevel);
         }
      }

      return 0;
   }

   private int spawnInHut(ServerWorld pServerLevel, BlockPos pPos) {
      int i = 16;
      List<CatEntity> list = pServerLevel.getEntitiesOfClass(CatEntity.class, (new AxisAlignedBB(pPos)).inflate(16.0D, 8.0D, 16.0D));
      return list.size() < 1 ? this.spawnCat(pPos, pServerLevel) : 0;
   }

   private int spawnCat(BlockPos pPos, ServerWorld pServerLevel) {
      CatEntity catentity = EntityType.CAT.create(pServerLevel);
      if (catentity == null) {
         return 0;
      } else {
         catentity.moveTo(pPos, 0.0F, 0.0F); // Fix MC-147659: Some witch huts spawn the incorrect cat
         if(net.minecraftforge.common.ForgeHooks.canEntitySpawn(catentity, pServerLevel, pPos.getX(), pPos.getY(), pPos.getZ(), null, SpawnReason.NATURAL) == -1) return 0;
         catentity.finalizeSpawn(pServerLevel, pServerLevel.getCurrentDifficultyAt(pPos), SpawnReason.NATURAL, (ILivingEntityData)null, (CompoundNBT)null);
         pServerLevel.addFreshEntityWithPassengers(catentity);
         return 1;
      }
   }
}
