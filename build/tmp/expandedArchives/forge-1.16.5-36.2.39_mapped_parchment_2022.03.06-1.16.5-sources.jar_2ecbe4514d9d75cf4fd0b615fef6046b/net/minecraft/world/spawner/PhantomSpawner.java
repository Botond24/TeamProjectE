package net.minecraft.world.spawner;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public class PhantomSpawner implements ISpecialSpawner {
   private int nextTick;

   public int tick(ServerWorld pLevel, boolean pSpawnHostiles, boolean pSpawnPassives) {
      if (!pSpawnHostiles) {
         return 0;
      } else if (!pLevel.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA)) {
         return 0;
      } else {
         Random random = pLevel.random;
         --this.nextTick;
         if (this.nextTick > 0) {
            return 0;
         } else {
            this.nextTick += (60 + random.nextInt(60)) * 20;
            if (pLevel.getSkyDarken() < 5 && pLevel.dimensionType().hasSkyLight()) {
               return 0;
            } else {
               int i = 0;

               for(PlayerEntity playerentity : pLevel.players()) {
                  if (!playerentity.isSpectator()) {
                     BlockPos blockpos = playerentity.blockPosition();
                     if (!pLevel.dimensionType().hasSkyLight() || blockpos.getY() >= pLevel.getSeaLevel() && pLevel.canSeeSky(blockpos)) {
                        DifficultyInstance difficultyinstance = pLevel.getCurrentDifficultyAt(blockpos);
                        if (difficultyinstance.isHarderThan(random.nextFloat() * 3.0F)) {
                           ServerStatisticsManager serverstatisticsmanager = ((ServerPlayerEntity)playerentity).getStats();
                           int j = MathHelper.clamp(serverstatisticsmanager.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
                           int k = 24000;
                           if (random.nextInt(j) >= 72000) {
                              BlockPos blockpos1 = blockpos.above(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
                              BlockState blockstate = pLevel.getBlockState(blockpos1);
                              FluidState fluidstate = pLevel.getFluidState(blockpos1);
                              if (WorldEntitySpawner.isValidEmptySpawnBlock(pLevel, blockpos1, blockstate, fluidstate, EntityType.PHANTOM)) {
                                 ILivingEntityData ilivingentitydata = null;
                                 int l = 1 + random.nextInt(difficultyinstance.getDifficulty().getId() + 1);

                                 for(int i1 = 0; i1 < l; ++i1) {
                                    PhantomEntity phantomentity = EntityType.PHANTOM.create(pLevel);
                                    phantomentity.moveTo(blockpos1, 0.0F, 0.0F);
                                    if(net.minecraftforge.common.ForgeHooks.canEntitySpawn(phantomentity, pLevel, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), null, SpawnReason.NATURAL) == -1) return 0;
                                    ilivingentitydata = phantomentity.finalizeSpawn(pLevel, difficultyinstance, SpawnReason.NATURAL, ilivingentitydata, (CompoundNBT)null);
                                    pLevel.addFreshEntityWithPassengers(phantomentity);
                                 }

                                 i += l;
                              }
                           }
                        }
                     }
                  }
               }

               return i;
            }
         }
      }
   }
}