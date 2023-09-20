package net.minecraft.world.lighting;

import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorldLightManager implements ILightListener {
   @Nullable
   private final LightEngine<?, ?> blockEngine;
   @Nullable
   private final LightEngine<?, ?> skyEngine;

   public WorldLightManager(IChunkLightProvider p_i51290_1_, boolean p_i51290_2_, boolean p_i51290_3_) {
      this.blockEngine = p_i51290_2_ ? new BlockLightEngine(p_i51290_1_) : null;
      this.skyEngine = p_i51290_3_ ? new SkyLightEngine(p_i51290_1_) : null;
   }

   public void checkBlock(BlockPos p_215568_1_) {
      if (this.blockEngine != null) {
         this.blockEngine.checkBlock(p_215568_1_);
      }

      if (this.skyEngine != null) {
         this.skyEngine.checkBlock(p_215568_1_);
      }

   }

   public void onBlockEmissionIncrease(BlockPos p_215573_1_, int p_215573_2_) {
      if (this.blockEngine != null) {
         this.blockEngine.onBlockEmissionIncrease(p_215573_1_, p_215573_2_);
      }

   }

   public boolean hasLightWork() {
      if (this.skyEngine != null && this.skyEngine.hasLightWork()) {
         return true;
      } else {
         return this.blockEngine != null && this.blockEngine.hasLightWork();
      }
   }

   public int runUpdates(int p_215575_1_, boolean p_215575_2_, boolean p_215575_3_) {
      if (this.blockEngine != null && this.skyEngine != null) {
         int i = p_215575_1_ / 2;
         int j = this.blockEngine.runUpdates(i, p_215575_2_, p_215575_3_);
         int k = p_215575_1_ - i + j;
         int l = this.skyEngine.runUpdates(k, p_215575_2_, p_215575_3_);
         return j == 0 && l > 0 ? this.blockEngine.runUpdates(l, p_215575_2_, p_215575_3_) : l;
      } else if (this.blockEngine != null) {
         return this.blockEngine.runUpdates(p_215575_1_, p_215575_2_, p_215575_3_);
      } else {
         return this.skyEngine != null ? this.skyEngine.runUpdates(p_215575_1_, p_215575_2_, p_215575_3_) : p_215575_1_;
      }
   }

   public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {
      if (this.blockEngine != null) {
         this.blockEngine.updateSectionStatus(pPos, pIsEmpty);
      }

      if (this.skyEngine != null) {
         this.skyEngine.updateSectionStatus(pPos, pIsEmpty);
      }

   }

   public void enableLightSources(ChunkPos p_215571_1_, boolean p_215571_2_) {
      if (this.blockEngine != null) {
         this.blockEngine.enableLightSources(p_215571_1_, p_215571_2_);
      }

      if (this.skyEngine != null) {
         this.skyEngine.enableLightSources(p_215571_1_, p_215571_2_);
      }

   }

   public IWorldLightListener getLayerListener(LightType pType) {
      if (pType == LightType.BLOCK) {
         return (IWorldLightListener)(this.blockEngine == null ? IWorldLightListener.Dummy.INSTANCE : this.blockEngine);
      } else {
         return (IWorldLightListener)(this.skyEngine == null ? IWorldLightListener.Dummy.INSTANCE : this.skyEngine);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public String getDebugData(LightType p_215572_1_, SectionPos p_215572_2_) {
      if (p_215572_1_ == LightType.BLOCK) {
         if (this.blockEngine != null) {
            return this.blockEngine.getDebugData(p_215572_2_.asLong());
         }
      } else if (this.skyEngine != null) {
         return this.skyEngine.getDebugData(p_215572_2_.asLong());
      }

      return "n/a";
   }

   public void queueSectionData(LightType pType, SectionPos pPos, @Nullable NibbleArray pArray, boolean p_215574_4_) {
      if (pType == LightType.BLOCK) {
         if (this.blockEngine != null) {
            this.blockEngine.queueSectionData(pPos.asLong(), pArray, p_215574_4_);
         }
      } else if (this.skyEngine != null) {
         this.skyEngine.queueSectionData(pPos.asLong(), pArray, p_215574_4_);
      }

   }

   public void retainData(ChunkPos pPos, boolean pRetain) {
      if (this.blockEngine != null) {
         this.blockEngine.retainData(pPos, pRetain);
      }

      if (this.skyEngine != null) {
         this.skyEngine.retainData(pPos, pRetain);
      }

   }

   public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
      int i = this.skyEngine == null ? 0 : this.skyEngine.getLightValue(pBlockPos) - pAmount;
      int j = this.blockEngine == null ? 0 : this.blockEngine.getLightValue(pBlockPos);
      return Math.max(j, i);
   }
}