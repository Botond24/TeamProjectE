package net.minecraft.world.end;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;

public enum DragonSpawnState {
   START {
      public void tick(ServerWorld pLevel, DragonFightManager pManager, List<EnderCrystalEntity> pCrystals, int pTicks, BlockPos pPos) {
         BlockPos blockpos = new BlockPos(0, 128, 0);

         for(EnderCrystalEntity endercrystalentity : pCrystals) {
            endercrystalentity.setBeamTarget(blockpos);
         }

         pManager.setRespawnStage(PREPARING_TO_SUMMON_PILLARS);
      }
   },
   PREPARING_TO_SUMMON_PILLARS {
      public void tick(ServerWorld pLevel, DragonFightManager pManager, List<EnderCrystalEntity> pCrystals, int pTicks, BlockPos pPos) {
         if (pTicks < 100) {
            if (pTicks == 0 || pTicks == 50 || pTicks == 51 || pTicks == 52 || pTicks >= 95) {
               pLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
            }
         } else {
            pManager.setRespawnStage(SUMMONING_PILLARS);
         }

      }
   },
   SUMMONING_PILLARS {
      public void tick(ServerWorld pLevel, DragonFightManager pManager, List<EnderCrystalEntity> pCrystals, int pTicks, BlockPos pPos) {
         int i = 40;
         boolean flag = pTicks % 40 == 0;
         boolean flag1 = pTicks % 40 == 39;
         if (flag || flag1) {
            List<EndSpikeFeature.EndSpike> list = EndSpikeFeature.getSpikesForLevel(pLevel);
            int j = pTicks / 40;
            if (j < list.size()) {
               EndSpikeFeature.EndSpike endspikefeature$endspike = list.get(j);
               if (flag) {
                  for(EnderCrystalEntity endercrystalentity : pCrystals) {
                     endercrystalentity.setBeamTarget(new BlockPos(endspikefeature$endspike.getCenterX(), endspikefeature$endspike.getHeight() + 1, endspikefeature$endspike.getCenterZ()));
                  }
               } else {
                  int k = 10;

                  for(BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(endspikefeature$endspike.getCenterX() - 10, endspikefeature$endspike.getHeight() - 10, endspikefeature$endspike.getCenterZ() - 10), new BlockPos(endspikefeature$endspike.getCenterX() + 10, endspikefeature$endspike.getHeight() + 10, endspikefeature$endspike.getCenterZ() + 10))) {
                     pLevel.removeBlock(blockpos, false);
                  }

                  pLevel.explode((Entity)null, (double)((float)endspikefeature$endspike.getCenterX() + 0.5F), (double)endspikefeature$endspike.getHeight(), (double)((float)endspikefeature$endspike.getCenterZ() + 0.5F), 5.0F, Explosion.Mode.DESTROY);
                  EndSpikeFeatureConfig endspikefeatureconfig = new EndSpikeFeatureConfig(true, ImmutableList.of(endspikefeature$endspike), new BlockPos(0, 128, 0));
                  Feature.END_SPIKE.configured(endspikefeatureconfig).place(pLevel, pLevel.getChunkSource().getGenerator(), new Random(), new BlockPos(endspikefeature$endspike.getCenterX(), 45, endspikefeature$endspike.getCenterZ()));
               }
            } else if (flag) {
               pManager.setRespawnStage(SUMMONING_DRAGON);
            }
         }

      }
   },
   SUMMONING_DRAGON {
      public void tick(ServerWorld pLevel, DragonFightManager pManager, List<EnderCrystalEntity> pCrystals, int pTicks, BlockPos pPos) {
         if (pTicks >= 100) {
            pManager.setRespawnStage(END);
            pManager.resetSpikeCrystals();

            for(EnderCrystalEntity endercrystalentity : pCrystals) {
               endercrystalentity.setBeamTarget((BlockPos)null);
               pLevel.explode(endercrystalentity, endercrystalentity.getX(), endercrystalentity.getY(), endercrystalentity.getZ(), 6.0F, Explosion.Mode.NONE);
               endercrystalentity.remove();
            }
         } else if (pTicks >= 80) {
            pLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
         } else if (pTicks == 0) {
            for(EnderCrystalEntity endercrystalentity1 : pCrystals) {
               endercrystalentity1.setBeamTarget(new BlockPos(0, 128, 0));
            }
         } else if (pTicks < 5) {
            pLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
         }

      }
   },
   END {
      public void tick(ServerWorld pLevel, DragonFightManager pManager, List<EnderCrystalEntity> pCrystals, int pTicks, BlockPos pPos) {
      }
   };

   private DragonSpawnState() {
   }

   public abstract void tick(ServerWorld pLevel, DragonFightManager pManager, List<EnderCrystalEntity> pCrystals, int pTicks, BlockPos pPos);
}