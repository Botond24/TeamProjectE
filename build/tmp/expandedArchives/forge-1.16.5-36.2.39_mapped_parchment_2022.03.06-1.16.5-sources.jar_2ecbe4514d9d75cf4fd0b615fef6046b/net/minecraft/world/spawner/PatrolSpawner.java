package net.minecraft.world.spawner;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.PatrollerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

public class PatrolSpawner implements ISpecialSpawner {
   private int nextTick;

   public int tick(ServerWorld pLevel, boolean pSpawnHostiles, boolean pSpawnPassives) {
      if (!pSpawnHostiles) {
         return 0;
      } else if (!pLevel.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
         return 0;
      } else {
         Random random = pLevel.random;
         --this.nextTick;
         if (this.nextTick > 0) {
            return 0;
         } else {
            this.nextTick += 12000 + random.nextInt(1200);
            long i = pLevel.getDayTime() / 24000L;
            if (i >= 5L && pLevel.isDay()) {
               if (random.nextInt(5) != 0) {
                  return 0;
               } else {
                  int j = pLevel.players().size();
                  if (j < 1) {
                     return 0;
                  } else {
                     PlayerEntity playerentity = pLevel.players().get(random.nextInt(j));
                     if (playerentity.isSpectator()) {
                        return 0;
                     } else if (pLevel.isCloseToVillage(playerentity.blockPosition(), 2)) {
                        return 0;
                     } else {
                        int k = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                        int l = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                        BlockPos.Mutable blockpos$mutable = playerentity.blockPosition().mutable().move(k, 0, l);
                        if (!pLevel.hasChunksAt(blockpos$mutable.getX() - 10, blockpos$mutable.getY() - 10, blockpos$mutable.getZ() - 10, blockpos$mutable.getX() + 10, blockpos$mutable.getY() + 10, blockpos$mutable.getZ() + 10)) {
                           return 0;
                        } else {
                           Biome biome = pLevel.getBiome(blockpos$mutable);
                           Biome.Category biome$category = biome.getBiomeCategory();
                           if (biome$category == Biome.Category.MUSHROOM) {
                              return 0;
                           } else {
                              int i1 = 0;
                              int j1 = (int)Math.ceil((double)pLevel.getCurrentDifficultyAt(blockpos$mutable).getEffectiveDifficulty()) + 1;

                              for(int k1 = 0; k1 < j1; ++k1) {
                                 ++i1;
                                 blockpos$mutable.setY(pLevel.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, blockpos$mutable).getY());
                                 if (k1 == 0) {
                                    if (!this.spawnPatrolMember(pLevel, blockpos$mutable, random, true)) {
                                       break;
                                    }
                                 } else {
                                    this.spawnPatrolMember(pLevel, blockpos$mutable, random, false);
                                 }

                                 blockpos$mutable.setX(blockpos$mutable.getX() + random.nextInt(5) - random.nextInt(5));
                                 blockpos$mutable.setZ(blockpos$mutable.getZ() + random.nextInt(5) - random.nextInt(5));
                              }

                              return i1;
                           }
                        }
                     }
                  }
               }
            } else {
               return 0;
            }
         }
      }
   }

   private boolean spawnPatrolMember(ServerWorld pLevel, BlockPos pPos, Random pRandom, boolean pLeader) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (!WorldEntitySpawner.isValidEmptySpawnBlock(pLevel, pPos, blockstate, blockstate.getFluidState(), EntityType.PILLAGER)) {
         return false;
      } else if (!PatrollerEntity.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, pLevel, SpawnReason.PATROL, pPos, pRandom)) {
         return false;
      } else {
         PatrollerEntity patrollerentity = EntityType.PILLAGER.create(pLevel);
         if (patrollerentity != null) {
            if (pLeader) {
               patrollerentity.setPatrolLeader(true);
               patrollerentity.findPatrolTarget();
            }

            patrollerentity.setPos((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ());
            if(net.minecraftforge.common.ForgeHooks.canEntitySpawn(patrollerentity, pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), null, SpawnReason.PATROL) == -1) return false;
            patrollerentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(pPos), SpawnReason.PATROL, (ILivingEntityData)null, (CompoundNBT)null);
            pLevel.addFreshEntityWithPassengers(patrollerentity);
            return true;
         } else {
            return false;
         }
      }
   }
}
