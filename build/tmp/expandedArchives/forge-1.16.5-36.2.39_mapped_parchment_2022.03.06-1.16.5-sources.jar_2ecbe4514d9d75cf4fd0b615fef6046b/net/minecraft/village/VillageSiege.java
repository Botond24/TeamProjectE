package net.minecraft.village;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VillageSiege implements ISpecialSpawner {
   private static final Logger LOGGER = LogManager.getLogger();
   private boolean hasSetupSiege;
   private VillageSiege.State siegeState = VillageSiege.State.SIEGE_DONE;
   private int zombiesToSpawn;
   private int nextSpawnTime;
   private int spawnX;
   private int spawnY;
   private int spawnZ;

   public int tick(ServerWorld pLevel, boolean pSpawnHostiles, boolean pSpawnPassives) {
      if (!pLevel.isDay() && pSpawnHostiles) {
         float f = pLevel.getTimeOfDay(0.0F);
         if ((double)f == 0.5D) {
            this.siegeState = pLevel.random.nextInt(10) == 0 ? VillageSiege.State.SIEGE_TONIGHT : VillageSiege.State.SIEGE_DONE;
         }

         if (this.siegeState == VillageSiege.State.SIEGE_DONE) {
            return 0;
         } else {
            if (!this.hasSetupSiege) {
               if (!this.tryToSetupSiege(pLevel)) {
                  return 0;
               }

               this.hasSetupSiege = true;
            }

            if (this.nextSpawnTime > 0) {
               --this.nextSpawnTime;
               return 0;
            } else {
               this.nextSpawnTime = 2;
               if (this.zombiesToSpawn > 0) {
                  this.trySpawn(pLevel);
                  --this.zombiesToSpawn;
               } else {
                  this.siegeState = VillageSiege.State.SIEGE_DONE;
               }

               return 1;
            }
         }
      } else {
         this.siegeState = VillageSiege.State.SIEGE_DONE;
         this.hasSetupSiege = false;
         return 0;
      }
   }

   private boolean tryToSetupSiege(ServerWorld pLevel) {
      for(PlayerEntity playerentity : pLevel.players()) {
         if (!playerentity.isSpectator()) {
            BlockPos blockpos = playerentity.blockPosition();
            if (pLevel.isVillage(blockpos) && pLevel.getBiome(blockpos).getBiomeCategory() != Biome.Category.MUSHROOM) {
               for(int i = 0; i < 10; ++i) {
                  float f = pLevel.random.nextFloat() * ((float)Math.PI * 2F);
                  this.spawnX = blockpos.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0F);
                  this.spawnY = blockpos.getY();
                  this.spawnZ = blockpos.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0F);
                  Vector3d siegeLocation = this.findRandomSpawnPos(pLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
                  if (siegeLocation != null) {
                     if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.village.VillageSiegeEvent(this, pLevel, playerentity, siegeLocation))) return false;
                     this.nextSpawnTime = 0;
                     this.zombiesToSpawn = 20;
                     break;
                  }
               }

               return true;
            }
         }
      }

      return false;
   }

   private void trySpawn(ServerWorld pLevel) {
      Vector3d vector3d = this.findRandomSpawnPos(pLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
      if (vector3d != null) {
         ZombieEntity zombieentity;
         try {
            zombieentity = EntityType.ZOMBIE.create(pLevel); //Forge: Direct Initialization is deprecated, use EntityType.
            zombieentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(zombieentity.blockPosition()), SpawnReason.EVENT, (ILivingEntityData)null, (CompoundNBT)null);
         } catch (Exception exception) {
            LOGGER.warn("Failed to create zombie for village siege at {}", vector3d, exception);
            return;
         }

         zombieentity.moveTo(vector3d.x, vector3d.y, vector3d.z, pLevel.random.nextFloat() * 360.0F, 0.0F);
         pLevel.addFreshEntityWithPassengers(zombieentity);
      }
   }

   @Nullable
   private Vector3d findRandomSpawnPos(ServerWorld pLevel, BlockPos pPos) {
      for(int i = 0; i < 10; ++i) {
         int j = pPos.getX() + pLevel.random.nextInt(16) - 8;
         int k = pPos.getZ() + pLevel.random.nextInt(16) - 8;
         int l = pLevel.getHeight(Heightmap.Type.WORLD_SURFACE, j, k);
         BlockPos blockpos = new BlockPos(j, l, k);
         if (pLevel.isVillage(blockpos) && MonsterEntity.checkMonsterSpawnRules(EntityType.ZOMBIE, pLevel, SpawnReason.EVENT, blockpos, pLevel.random)) {
            return Vector3d.atBottomCenterOf(blockpos);
         }
      }

      return null;
   }

   static enum State {
      SIEGE_CAN_ACTIVATE,
      SIEGE_TONIGHT,
      SIEGE_DONE;
   }
}
